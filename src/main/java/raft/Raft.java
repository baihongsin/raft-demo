package raft;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.model.Log;
import raft.model.LogType;
import raft.rpc.*;
import raft.task.CandidateTask;
import raft.task.FollowerTask;
import raft.task.LeaderTask;
import raft.task.StateTask;
import raft.transport.RpcClient;
import raft.transport.RpcServer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;


public class Raft extends RaftState implements Runnable, RpcHandler {

    private static final Logger logger = LoggerFactory.getLogger(Raft.class);
    private static final String THREAD_POOL_NAME = "raft";
    private final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(THREAD_POOL_NAME).build();

    private final ThreadPoolExecutor threadPoolExecutor
            = new ThreadPoolExecutor(16, 1000, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000), threadFactory);


    private final Config config;

    private final List<Node> nodes;

    /**
     * 最后一次和领导者联系的时间
     */
    private long lastContact = System.currentTimeMillis();

    private final ReentrantLock lastContactLock = new ReentrantLock();

    private final List<StateTask> taskList = new ArrayList<>();

    private final FSM fsm;

    private final Map<Node, RpcHandler> nodeRpcMap = new ConcurrentHashMap<>();

    public Raft(Config config, FSM fsm) {
        this.config = config;
        this.fsm = fsm;
        this.nodes = new ArrayList<>();
    }

    public static String generateId() {
        return UUID.randomUUID().toString();
    }

    public static Raft newInstance() {
        Config config = Config.defaultConfig();
        Raft raft = new Raft(config, new StableFSM());
        raft.setEntries(new Log[config.getEntriesLength()]);
        raft.setId(generateId());
        return raft;
    }

    public void start() {
        logger.info("server id:{}", getId());
        threadPoolExecutor.execute(this);
    }

    /**
     * 主要运行逻辑
     */
    public void run() {
        String address = getAddress();
        RpcServer rpcServer = new RpcServer(address);
        rpcServer.addService(this);
        rpcServer.start();
        runLeader();
        runFollower();
        runCandidate();
    }



    /**
     * 跟随者状态
     */
    void runFollower() {
        FollowerTask followerTask = new FollowerTask(this);
        threadPoolExecutor.execute(followerTask);
        taskList.add(followerTask);
    }

    /**
     * 候选人状态
     */
    void runCandidate() {
        CandidateTask candidateTask = new CandidateTask(this);
        threadPoolExecutor.execute(candidateTask);
        taskList.add(candidateTask);
    }

    /**
     * 领导者状态
     */
    void runLeader() {
        setLastContact();
        LeaderTask leaderTask = new LeaderTask(this);
        threadPoolExecutor.execute(leaderTask);
        taskList.add(leaderTask);
    }

    public Node pushCommand(String content) {


        Node leader = getLeader();
        if (leader == null) {
            setIndex(getIndex() + 1);
            Log log = new Log();
            log.setData(content.getBytes(StandardCharsets.UTF_8));
            log.setAppendedAt(System.currentTimeMillis());
            log.setType(LogType.LogCommand);
            log.setTerm(getCurrentTerm());
            log.setIndex(getIndex());
            fsm.apply(log);
            List<Node> nodes = getNodes();
            for (Node node : nodes) {
                RpcHandler rpcHandler = getRpc(node);
                AppendEntriesRequest request = new AppendEntriesRequest();
                request.setTerm(getCurrentTerm());
                request.setEntries(new Log[]{log});
                request.setLeaderCommitIndex(getIndex());
                AppendEntriesResponse response = rpcHandler.appendEntries(request);
                if (response.isSuccess()) {
                    logger.info("push command success");
                }
            }
        }
        return leader;
    }


    @Override
    public AppendEntriesResponse appendEntries(AppendEntriesRequest request) {
//        logger.info("{}-{} append entries", getId(), getState());
        AppendEntriesResponse response = new AppendEntriesResponse();
        response.setSuccess(false);
        response.setTerm(getCurrentTerm());
        if (request.getTerm() < getCurrentTerm()) {
            logger.info("req term:{} less than current:{}", request.getTerm(), getCurrentTerm());
            return response;
        }

        if (request.getTerm() > getCurrentTerm() || getState() != NodeState.FOLLOWER) {
            setState(NodeState.FOLLOWER);
            setCurrentTerm(request.getTerm());
            response.setTerm(request.getTerm());
        }

        Log[] entries = request.getEntries();
        if (entries != null) {
            int length = entries.length;
            Log lastLog = entries[length - 1];
            long lastIndex = lastLog.getIndex();
            long lastTerm = lastLog.getTerm();
            setLastLogIndex(lastIndex);
            setLastLogTerm(lastTerm);
            logger.info("entries len:{}, lastIndex:{}, lastTerm:{}", length, lastIndex, lastTerm);
            if (request.getLeaderCommitIndex() > 0
                    && request.getLeaderCommitIndex() > getIndex()) {
                for (Log log : entries) {
                    if (fsm != null) {
                        fsm.apply(log);
                    }
                }
            }
        }

        setState(NodeState.FOLLOWER);
        setLeader(request.getLeader());
        setLastContact();
        response.setSuccess(true);
        return response;
    }

    @Override
    public RequestVoteResponse requestVote(RequestVoteRequest request) {
        logger.info("{}-{} request vote, {}", getId(), getState(), request);
        RequestVoteResponse resp = new RequestVoteResponse();
        resp.setVoteGranted(false);
        resp.setTerm(getCurrentTerm());
        if (request.getTerm() < getCurrentTerm()) {
            return resp;
        }
        if (request.getTerm() > getCurrentTerm()) {
            setState(NodeState.FOLLOWER);
            setCurrentTerm(request.getTerm());
            resp.setTerm(request.getTerm());
        }

        if (getCurrentTerm() == request.getLastLogTerm() && getLastLogIndex() > request.getLastLogIndex()) {
            return resp;
        }

        if (request.getTerm() == getLastVoteTerm()) {
            if (request.getCandidateId() != null && request.getCandidateId().equals(getLastVoteId())) {
                resp.setVoteGranted(true);
            }
            return resp;
        }

        setLastVoteId(request.getCandidateId());
        setLastVoteTerm(request.getTerm());
        resp.setVoteGranted(true);
        setLastContact();
        return resp;
    }

    @Override
    public InstallSnapshotResponse installSnapshot(InstallSnapshotRequest request) {


        return null;
    }

    public void addPeer(Node node) {
        if (!nodes.contains(node)) {
            nodes.add(node);
        }
    }

    public void setLastContact() {
        lastContactLock.lock();
        lastContact = System.currentTimeMillis();
        lastContactLock.unlock();
    }

    public long getLastContact() {
        return this.lastContact;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public Config getConfig() {
        return config;
    }


    @Override
    public void setState(NodeState state) {
        if (getState() == state) {
            return;
        }
        if (state == NodeState.LEADER) {
            setLeader(null);
        }
        super.setState(state);
        for (StateTask stateTask : taskList) {
            stateTask.putState(state);
        }
        logger.info("state change:{}, id:{}, taskSize:{}", state, getId(), taskList.size());

    }

    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    public int quorumSize() {
        return nodes.size() / 2 + 1;
    }

    public RpcHandler getRpc(Node node) {
        if (nodeRpcMap.containsKey(node)) {
            return nodeRpcMap.get(node);
        }
        RpcClient rpcClient = new RpcClient();
        RpcHandler rpcHandler = rpcClient.createService(RpcHandler.class, node.getAddress());
        nodeRpcMap.put(node, rpcHandler);
        return rpcHandler;
    }
}

package raft;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.rpc.*;
import raft.task.CandidateTask;
import raft.task.FollowerTask;
import raft.task.LeaderTask;
import raft.task.StateTask;
import raft.transport.RpcServer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;


public class Raft extends RaftState implements Runnable, RpcHandler {

    private static final Logger log = LoggerFactory.getLogger(Raft.class);
    private static final String THREAD_POOL_NAME = "raft";
    private final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(THREAD_POOL_NAME).build();

    private final ThreadPoolExecutor threadPoolExecutor
            = new ThreadPoolExecutor(4, 20, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000), threadFactory);


    private final Config config;

    private final List<Node> nodes;

    /**
     * 最后一次和领导者联系的时间
     */
    private long lastContact = System.currentTimeMillis();

    private final ReentrantLock lastContactLock = new ReentrantLock();

    private final List<StateTask> taskList = new ArrayList<>();

    public Raft(Config config) {
        this.config = config;
        this.nodes = new ArrayList<>();
    }

    public static String generateId() {
        return UUID.randomUUID().toString();
    }

    public static Raft newInstance() {
        Raft raft = new Raft(Config.defaultConfig());
        raft.setId(generateId());
        return raft;
    }

    public void start() {
        log.info("server id:{}", getId());
        threadPoolExecutor.execute(this);
        threadPoolExecutor.execute(this::runFSM);
        threadPoolExecutor.execute(this::runSnapshots);
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
     * 运行状态机
     */
    void runFSM() {


    }

    /**
     * 定时快照
     */
    void runSnapshots() {

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


    @Override
    public AppendEntriesResponse appendEntries(AppendEntriesRequest request) {
        log.info("{}-{} append entries", getId(), getState());
        AppendEntriesResponse response = new AppendEntriesResponse();
        response.setSuccess(false);
        response.setTerm(getCurrentTerm());
        if (request.getTerm() < getCurrentTerm()) {
            return response;
        }

        if (request.getTerm() > getCurrentTerm() || getState() != NodeState.FOLLOWER) {
            setState(NodeState.FOLLOWER);
            setCurrentTerm(request.getTerm());
            response.setTerm(request.getTerm());
        }
        setState(NodeState.FOLLOWER);
        setLeader(request.getLeader());
        setLastContact();
        return response;
    }

    @Override
    public RequestVoteResponse requestVote(RequestVoteRequest request) {
        log.info("{}-{} request vote, {}", getId(), getState(), request);


        return null;
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
        super.setState(state);
        for (StateTask stateTask : taskList) {
            stateTask.putState(state);
        }

        log.info("state change:{}, id:{}", state, getId());

    }
}

package raft;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.rpc.*;
import raft.task.FollowerTask;
import raft.task.LeaderTask;
import raft.transport.RpcServer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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

        NodeState state = getState();
        switch (state) {
            case FOLLOWER:
                runFollower();
                break;
            case LEADER:
                runLeader();
                break;
            case CANDIDATE:
                runCandidate();
                break;
            case SHUTDOWN:
                break;
            default:
                log.error("known state:{}", state);
        }

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
        threadPoolExecutor.execute(new FollowerTask(this));
    }






    /**
     * 候选人状态
     */
    void runCandidate() {

    }

    /**
     * 领导者状态
     */
    void runLeader() {
        setLastContact();
        if (getState() == NodeState.LEADER) {
            threadPoolExecutor.execute(new LeaderTask(this));
        }

    }


    @Override
    public AppendEntriesResponse appendEntries(AppendEntriesRequest request) {
        log.info("{}-{} recv request", getId(), getState());
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

        setLeader(request.getLeader());
        setLastContact();
        return response;
    }

    @Override
    public RequestVoteResponse requestVote(RequestVoteRequest request) {
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

    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }



}

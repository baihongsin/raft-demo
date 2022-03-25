package raft.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.Node;
import raft.NodeState;
import raft.Raft;
import raft.rpc.RequestVoteRequest;
import raft.rpc.RequestVoteResponse;
import raft.rpc.RpcHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class CandidateTask extends StateTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CandidateTask.class);

    private final Raft raft;

    private static class VoteInternal {
        /**
         * 获得的选票数量
         */
        public int voteGranted;

        /**
         * 需要赢得选举的选票数量
         */
        public int voteNeeded;

        /**
         * 投票的节点
         */
        public Node voteNode;
    }

    public CandidateTask(Raft raft) {
        this.raft = raft;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        while (true) {
            NodeState nodeState = takeState();
            if (nodeState == NodeState.CANDIDATE) {
                stateProcess();
            }
        }

    }

    private void stateProcess() {
        // 自增当前任期
        raft.setCurrentTerm(raft.getCurrentTerm() + 1);
        logger.info("cur node change:{}", raft.getState().name());
        VoteInternal voteInternal = new VoteInternal();
        // 给自己投票
        voteInternal.voteGranted = 1;
        voteInternal.voteNeeded = raft.quorumSize();
        // 请求各个节点
        for (Node node : raft.getNodes()) {
            raft.getThreadPoolExecutor().execute(() -> requestVote(node, voteInternal));
        }
    }

    private void requestVote(Node node, VoteInternal voteInternal) {
        RequestVoteRequest request = new RequestVoteRequest();
        request.setCandidateId(raft.getId());
        request.setTerm(raft.getCurrentTerm());
        request.setLastLogIndex(raft.getLastLogIndex());
        request.setLastLogTerm(raft.getLastLogTerm());
        RpcHandler rpcHandler = raft.getRpc(node);
        RequestVoteResponse resp = rpcHandler.requestVote(request);

        long term = resp.getTerm();
        if (term > raft.getCurrentTerm()) {
            raft.setState(NodeState.FOLLOWER);
            raft.setCurrentTerm(term);
            return;
        }
        boolean voteGranted = resp.isVoteGranted();
        if (voteGranted) {
            voteInternal.voteGranted ++;
            logger.debug("get voted from :{}", node);
        }
        if (voteInternal.voteGranted > voteInternal.voteNeeded) {
            raft.setState(NodeState.LEADER);
            raft.setLeader(null);
        }
    }
}

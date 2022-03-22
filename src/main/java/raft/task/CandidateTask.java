package raft.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.Node;
import raft.NodeState;
import raft.Raft;
import raft.rpc.RequestVoteRequest;
import raft.rpc.RpcHandler;

public class CandidateTask extends StateTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CandidateTask.class);

    private final Raft raft;


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
        logger.info("cur node change:{}", raft.getState().name());
        for (Node node : raft.getNodes()) {
            RequestVoteRequest request = new RequestVoteRequest();
            request.setCandidateId(raft.getId());
            request.setTerm(raft.getCurrentTerm());
            request.setLastLogIndex(1);
            request.setLastLogTerm(1);
            RpcHandler rpcHandler = node.getRpcHandler();
            rpcHandler.requestVote(request);
        }
    }
}

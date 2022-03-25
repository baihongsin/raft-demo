package raft.task;

import raft.DelayLock;
import raft.Node;
import raft.NodeState;
import raft.Raft;
import raft.rpc.AppendEntriesRequest;
import raft.rpc.RpcHandler;

import java.util.concurrent.TimeUnit;

public class LeaderTask extends StateTask implements Runnable {

    private final Raft raft;
    private final DelayLock delayLock = new DelayLock();

    public LeaderTask(Raft raft) {
        this.raft = raft;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        stateProcess();
        while (true) {
            NodeState nodeState = takeState();
            if (nodeState != null) {
                stateProcess();
            }
        }


    }

    private void stateProcess() {
        while (raft.getState() == NodeState.LEADER) {
            for (Node node : raft.getNodes()) {
                AppendEntriesRequest request = new AppendEntriesRequest();
                request.setLeader(new Node(raft.getId(), raft.getAddress()));
                request.setTerm(raft.getCurrentTerm());
                request.setLeaderCommitIndex(raft.getIndex());
                RpcHandler rpcHandler = raft.getRpc(node);
                rpcHandler.appendEntries(request);
            }
            try {
                delayLock.delay(raft.getConfig().getHeartbeatTimeout(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}

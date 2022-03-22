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

    @SuppressWarnings("InfiniteLoopStatement")
    private void stateProcess() {
        while (true) {
            if (raft.getState() == NodeState.LEADER) {
                for (Node node : raft.getNodes()) {
                    AppendEntriesRequest request = new AppendEntriesRequest();
                    request.setLeader(raft.getLeader());
                    request.setTerm(raft.getCurrentTerm());
                    RpcHandler rpcHandler = node.getRpcHandler();
                    rpcHandler.appendEntries(request);
                }
                try {
                    delayLock.delay(raft.getConfig().getHeartbeatTimeout() + 500, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}

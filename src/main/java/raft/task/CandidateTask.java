package raft.task;

import raft.NodeState;
import raft.Raft;

public class CandidateTask implements Runnable{

    private final Raft raft;

    public CandidateTask(Raft raft) {
        this.raft = raft;
    }

    @Override
    public void run() {


    }
}

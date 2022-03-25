package raft.rpc;

import raft.Node;
import raft.model.Log;

public class AppendEntriesRequest {

    /**
     * 领导人任期
     */
    private long term;

    /**
     * 领导人id
     */
    private Node leader;

    private long prevLogIndex;

    private long prevLogTerm;


    private Log[] entries;

    private long leaderCommitIndex;

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public Node getLeader() {
        return leader;
    }

    public void setLeader(Node leader) {
        this.leader = leader;
    }

    public long getPrevLogIndex() {
        return prevLogIndex;
    }

    public void setPrevLogIndex(long prevLogIndex) {
        this.prevLogIndex = prevLogIndex;
    }

    public long getPrevLogTerm() {
        return prevLogTerm;
    }

    public void setPrevLogTerm(long prevLogTerm) {
        this.prevLogTerm = prevLogTerm;
    }

    public Log[] getEntries() {
        return entries;
    }

    public void setEntries(Log[] entries) {
        this.entries = entries;
    }

    public long getLeaderCommitIndex() {
        return leaderCommitIndex;
    }

    public void setLeaderCommitIndex(long leaderCommitIndex) {
        this.leaderCommitIndex = leaderCommitIndex;
    }
}

package raft.model;

import raft.NodeState;

public class State {

    private String id;

    private NodeState state = NodeState.FOLLOWER;

    private long currentTerm = 0;

    private Integer votedFor = null;

    private Log[] entries;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NodeState getState() {
        return state;
    }

    public void setState(NodeState state) {
        this.state = state;
    }

    public long getCurrentTerm() {
        return currentTerm;
    }

    public void setCurrentTerm(long currentTerm) {
        this.currentTerm = currentTerm;
    }

    public Integer getVotedFor() {
        return votedFor;
    }

    public void setVotedFor(Integer votedFor) {
        this.votedFor = votedFor;
    }

    public Log[] getEntries() {
        return entries;
    }

    public void setEntries(Log[] entries) {
        this.entries = entries;
    }
}

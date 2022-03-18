package raft;

import raft.model.Log;

public class RaftState {

    private String address;

    private String id;

    private NodeState state = NodeState.FOLLOWER;

    private long currentTerm = 0;

    private Integer votedFor = null;

    private Log[] entries;

    private Node leader;


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

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

    public Node getLeader() {
        return leader;
    }

    public void setLeader(Node leader) {
        this.leader = leader;
    }
}

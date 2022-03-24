package raft;

import raft.model.Log;

public class RaftState {

    private String address;

    private String id;

    private NodeState state = NodeState.FOLLOWER;

    private long currentTerm = 0;

    private Integer votedFor = null;

    private Log[] entries;

    private int index = 0;

    private Node leader;

    // 缓存最近的快照索引和任期
    private long lastSnapshotIndex;

    private long lastSnapshotTerm;

    // 缓存最近日志的索引和任期

    private long lastLogIndex;

    private long lastLogTerm;


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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getLastSnapshotIndex() {
        return lastSnapshotIndex;
    }

    public void setLastSnapshotIndex(long lastSnapshotIndex) {
        this.lastSnapshotIndex = lastSnapshotIndex;
    }

    public long getLastSnapshotTerm() {
        return lastSnapshotTerm;
    }

    public void setLastSnapshotTerm(long lastSnapshotTerm) {
        this.lastSnapshotTerm = lastSnapshotTerm;
    }

    public long getLastLogIndex() {
        return lastLogIndex;
    }

    public void setLastLogIndex(long lastLogIndex) {
        this.lastLogIndex = lastLogIndex;
    }

    public long getLastLogTerm() {
        return lastLogTerm;
    }

    public void setLastLogTerm(long lastLogTerm) {
        this.lastLogTerm = lastLogTerm;
    }
}

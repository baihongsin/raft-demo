package cn.munyu.raft.rpc;

import cn.munyu.raft.model.Log;

public class AppendEntriesRequest {

    /**
     * 领导人任期
     */
    private long term;

    /**
     * 领导人id
     */
    private long leaderId;

    private long prevLogIndex;

    private long prevLogTerm;


    private Log[] entries;

    private long leaderCommit;

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public long getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(long leaderId) {
        this.leaderId = leaderId;
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

    public long getLeaderCommit() {
        return leaderCommit;
    }

    public void setLeaderCommit(long leaderCommit) {
        this.leaderCommit = leaderCommit;
    }
}

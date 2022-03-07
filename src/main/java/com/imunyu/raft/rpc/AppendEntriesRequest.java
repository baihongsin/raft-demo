package com.imunyu.raft.rpc;

public class AppendEntriesRequest {

    private long term;

    private long leaderId;

    private long prevLogIndex;

    private long[] entries;

    private long leaderCommit;



}

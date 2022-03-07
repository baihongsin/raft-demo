package com.imunyu.raft.rpc.model;

public class AppendEntriesRequest {

    private long term;

    private long leaderId;

    private long prevLogIndex;

    private long[] entries;

    private long leaderCommit;



}

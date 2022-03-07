package com.imunyu.raft.rpc;

public class RequestVoteRequest {

    private long term;

    private long candidateId;

    private long lastLogIndex;

    private long lastLogTerm;



}

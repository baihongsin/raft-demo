package com.imunyu.raft.rpc.model;

public class RequestVoteRequest {

    private long term;

    private long candidateId;

    private long lastLogIndex;

    private long lastLogTerm;



}

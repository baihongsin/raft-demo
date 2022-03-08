package com.imunyu.raft.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcHandlerImpl implements RpcHandler{

    private static final Logger log = LoggerFactory.getLogger(RpcHandlerImpl.class);

    @Override
    public AppendEntriesResponse appendEntries(AppendEntriesRequest request) {

        log.info("appendEntries");
        return null;
    }

    @Override
    public RequestVoteResponse requestVote(AppendEntriesRequest request) {
        log.info("requestVote");
        RequestVoteResponse response = new RequestVoteResponse();
        response.setTerm(1);
        response.setVoteGranted(true);
        return response;
    }
}

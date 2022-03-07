package com.imunyu.raft.rpc;

public interface RpcHandler {

    AppendEntriesResponse appendEntries(AppendEntriesRequest request);

    RequestVoteResponse requestVote(AppendEntriesRequest request);

}

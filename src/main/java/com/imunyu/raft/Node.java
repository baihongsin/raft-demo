package com.imunyu.raft;

import com.imunyu.raft.model.State;
import com.imunyu.raft.rpc.*;
import com.imunyu.raft.transport.RpcClient;
import com.imunyu.raft.transport.RpcServer;

public class Node implements RpcHandler {

    private final State state = new State();

    /**
     * 加入到目标集群组内
     *
     * 首先获取集群节点列表
     *
     * @param addr 需要加入的目标地址
     */
    public void join(String addr) {
        RpcClient client = new RpcClient();
        RpcHandler rpcHandler = client.createService(RpcHandler.class, addr);

        AppendEntriesRequest request = new AppendEntriesRequest();


        rpcHandler.appendEntries(request);



//        RequestVoteRequest request = new RequestVoteRequest();
//        request.setCandidateId(state.getId());
//        rpcHandler.requestVote(request);
    }

    /**
     * 启动节点的服务
     */
    public void startup(int port) {

        RpcServer rpcServer = new RpcServer(port);
        rpcServer.addService(this);
        rpcServer.start();
        String id = rpcServer.getHost() + ":" + port;
        state.setId(id);
        NodeState state = this.state.getState();
        switch (state) {
            case LEADER:

                break;
            case FOLLOWER:

                break;
            case CANDIDATE:


                break;
            case SHUTDOWN:

                break;
        }




    }

    /**
     * 日志同步
     */
    @Override
    public AppendEntriesResponse appendEntries(AppendEntriesRequest request) {
        return null;
    }


    /**
     * 投票
     */
    @Override
    public RequestVoteResponse requestVote(RequestVoteRequest request) {
        String candidateId = request.getCandidateId();
        RequestVoteResponse response = new RequestVoteResponse();
        request.setCandidateId(candidateId);
        return response;
    }
}

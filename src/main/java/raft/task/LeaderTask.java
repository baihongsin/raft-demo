package raft.task;

import raft.Node;
import raft.Raft;
import raft.rpc.AppendEntriesRequest;
import raft.rpc.RpcHandler;
import raft.transport.RpcClient;

import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

public class LeaderTask extends TimerTask {

    private final Map<String, RpcHandler> rpcHandlerMap;
    private final Raft raft;
    private final ThreadPoolExecutor threadPoolExecutor;

    public LeaderTask(Raft raft) {
        this.raft = raft;
        this.rpcHandlerMap = new ConcurrentHashMap<>();
        this.threadPoolExecutor = raft.getThreadPoolExecutor();
    }


    @Override
    public void run() {
        for (Node node : raft.getNodes()) {
            RpcHandler rpcHandler;
            if (rpcHandlerMap.containsKey(node.getServerId())) {
                rpcHandler = rpcHandlerMap.get(node.getServerId());
            } else {
                RpcClient rpcClient = new RpcClient();
                rpcHandler = rpcClient.createService(RpcHandler.class, node.getAddress());
                rpcHandlerMap.put(node.getServerId(), rpcHandler);
            }
            AppendEntriesRequest request = new AppendEntriesRequest();
            request.setLeader(raft.getLeader());
            request.setTerm(raft.getCurrentTerm());
            rpcHandler.appendEntries(request);
        }

        try {
            Thread.sleep(raft.getConfig().getHeartbeatTimeout());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        threadPoolExecutor.execute(this);


    }
}

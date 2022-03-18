package raft.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.NodeState;
import raft.Raft;
import raft.Utils;

import java.util.concurrent.ThreadPoolExecutor;

public class FollowerTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(FollowerTask.class);

    private final Raft raft;
    private final ThreadPoolExecutor threadPoolExecutor;
    private long heartbeatTimeout;

    public FollowerTask(Raft raft) {
        this.raft = raft;
        this.threadPoolExecutor = raft.getThreadPoolExecutor();
        this.heartbeatTimeout = raft.getConfig().getHeartbeatTimeout();
    }

    @Override
    public void run() {
        if (System.currentTimeMillis() - raft.getLastContact() < heartbeatTimeout) {
            heartbeatTimeout = heartbeatTimeout + Utils.randomTimeout(heartbeatTimeout);
            try {
                Thread.sleep(heartbeatTimeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            threadPoolExecutor.execute(this);
            return;
        }

        // 随机心跳超时变为候选者
        raft.setLeader(null);
        raft.setState(NodeState.CANDIDATE);

        log.info("follower transform to candidate:");
    }
}

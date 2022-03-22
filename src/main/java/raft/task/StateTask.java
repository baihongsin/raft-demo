package raft.task;

import raft.NodeState;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 吃掉了多线程中断异常
 */
public abstract class StateTask {

    private static final BlockingQueue<NodeState> stateBlockingQueue = new LinkedBlockingQueue<>();


    public void putState(NodeState state) {
        try {
            stateBlockingQueue.put(state);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public NodeState takeState() {
        try {
            return stateBlockingQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

}

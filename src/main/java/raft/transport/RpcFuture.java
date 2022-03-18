package raft.transport;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class RpcFuture<T> implements Future<T> {

    private T data;

    private final Sync sync = new Sync();

    public void done(T data) {
        this.data = data;
        sync.release(1);
    }


    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public T get() {
        sync.acquire(1);
        return data;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        boolean success = sync.tryAcquireNanos(1, unit.toNanos(timeout));
        if (!success) {
            throw new TimeoutException();
        }
        if (data == null) {
            throw new TimeoutException();
        }
        return data;
    }



    /**
     * 需要实现一个等待锁，等待输入成功之后释放
     */
    static class Sync extends AbstractQueuedSynchronizer {

        @Override
        protected boolean tryAcquire(int acquires) {
            // 这里不设置状态表示不争抢锁
            // 自旋几次后线程中断
            return getState() == 1;
        }

        @Override
        protected boolean tryRelease(int arg) {
            // 原来的state为0，设置1之后会唤醒线程
            if (getState() == 0) {
                return compareAndSetState(0, 1);
            } else {
                return true;
            }
        }

        protected boolean isDone() {
            return getState() == 1;
        }



    }


}

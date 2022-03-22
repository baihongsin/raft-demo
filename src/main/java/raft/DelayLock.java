package raft;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class DelayLock {

    private final Sync sync;

    public DelayLock() {
        this.sync = new Sync();
    }

    public static class Sync extends AbstractQueuedSynchronizer {

        public Sync() {
            setState(1);
        }

        protected int tryAcquireShared(int acquires) {
            return (getState() == 0) ? 1 : -1;
        }

        protected boolean tryReleaseShared(int releases) {
            // Decrement count; signal when transition to zero
            for (;;) {
                int c = getState();
                if (c == 0)
                    return false;
                int nextc = c-1;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
        }
    }

    public void delay(long timeout, TimeUnit unit)
            throws InterruptedException {
        sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }
}

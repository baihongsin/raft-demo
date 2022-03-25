package raft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raft.model.Log;

public class StableFSM implements FSM {

    private static final Logger logger = LoggerFactory.getLogger(StableFSM.class);

    @Override
    public boolean apply(Log log) {
        logger.info("fsm apply log {}", log);

        return false;
    }

    @Override
    public boolean[] applyBatch(Log[] logs) {
        return new boolean[0];
    }

    @Override
    public void snapshot() {

    }

    @Override
    public void restore() {

    }
}

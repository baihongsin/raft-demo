package com.imunyu.raft;

import java.util.Timer;
import java.util.TimerTask;

public class RaftTimer extends TimerTask {

    private Timer timer;

    public RaftTimer() {
        timer = new Timer();
        timer.schedule(this, 0L);
    }

    @Override
    public void run() {




    }

}

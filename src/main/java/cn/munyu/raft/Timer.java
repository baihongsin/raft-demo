package cn.munyu.raft;

import java.util.TimerTask;

public class Timer extends TimerTask {

    private static final long INTERVAL_TIME = 1000;

    public Timer() {
        java.util.Timer timer = new java.util.Timer();
        timer.schedule(this, 0L, INTERVAL_TIME);
    }

    @Override
    public void run() {


        System.out.println("raft timer");

    }

}

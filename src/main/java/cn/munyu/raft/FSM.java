package cn.munyu.raft;

import cn.munyu.raft.model.Log;

public interface FSM {

    boolean apply(Log log);

    boolean[] applyBatch(Log[] logs);


    void snapshot();

    void restore();




}

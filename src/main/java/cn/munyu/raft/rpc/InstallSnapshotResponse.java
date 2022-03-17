package cn.munyu.raft.rpc;

public class InstallSnapshotResponse {


    private long term;

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }
}

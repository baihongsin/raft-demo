package raft;

public class Config {

    private long heartbeatTimeout = 1000;

    private long electionTimeout = 1000;

    private long commitTimeout = 50;

    private long snapshotInterval = 60000;

    private long leaderLeaseTimeout = 500;

    public static Config defaultConfig() {
        return new Config();
    }

    public long getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    public void setHeartbeatTimeout(long heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }

    public long getElectionTimeout() {
        return electionTimeout;
    }

    public void setElectionTimeout(long electionTimeout) {
        this.electionTimeout = electionTimeout;
    }

    public long getCommitTimeout() {
        return commitTimeout;
    }

    public void setCommitTimeout(long commitTimeout) {
        this.commitTimeout = commitTimeout;
    }

    public long getSnapshotInterval() {
        return snapshotInterval;
    }

    public void setSnapshotInterval(long snapshotInterval) {
        this.snapshotInterval = snapshotInterval;
    }

    public long getLeaderLeaseTimeout() {
        return leaderLeaseTimeout;
    }

    public void setLeaderLeaseTimeout(long leaderLeaseTimeout) {
        this.leaderLeaseTimeout = leaderLeaseTimeout;
    }
}

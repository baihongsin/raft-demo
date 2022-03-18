package raft;

import java.util.Random;

public class Utils {

    private static final Random random = new Random();

    public static long randomTimeout(long timeout) {
        return Math.abs(random.nextInt() % timeout);
    }
}

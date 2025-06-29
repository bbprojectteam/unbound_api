package com.badboys.unbound_service.util;

public class MmrUtil {

    private static final int K = 32;

    public static int calculateNewRating(int myRating, int opponentRating, double actualScore) {
        double expected = 1.0 / (1.0 + Math.pow(10, (opponentRating - myRating) / 400.0));
        int newRating = (int) Math.round(myRating + K * (actualScore - expected));
        return newRating;
    }
}

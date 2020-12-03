package edu.kpi.ip71.dovhopoliuk.random.model;

public enum PlayMode {
    LCG("Lcg"), MT("Mt"), BETTER_MT("BetterMt");

    private String mode;

    PlayMode(String mode) {
        this.mode = mode;
    }


    @Override
    public String toString() {
        return mode;
    }
}

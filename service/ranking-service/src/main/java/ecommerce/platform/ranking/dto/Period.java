package ecommerce.platform.ranking.dto;

public enum Period {
    REALTIME(10),
    DAILY(20),
    WEEKLY(30),
    MONTHLY(40);

    private final int increment;

    Period(int increment) {
        this.increment = increment;
    }

    public int getIncrement() {
        return increment;
    }
}

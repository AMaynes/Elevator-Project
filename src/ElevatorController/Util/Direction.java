package ElevatorController.Util;

public enum Direction {
    UP(0), DOWN(1), STOPPED(-1);
    private int integerVersion;

    public int getIntegerVersion() {
        return integerVersion;
    }
    private Direction(int integerVersion){
        this.integerVersion=integerVersion;
    }
}

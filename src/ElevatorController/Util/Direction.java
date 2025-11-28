package ElevatorController.Util;

import Message.Topic;

public enum Direction {
    UP(Topic.up),
    DOWN(Topic.down),
    STOPPED(Topic.none);

    // Directions associated with numbers from MUX's body handling
    private int integerVersion;

    public int getIntegerVersion() {
        return integerVersion;
    }
    private Direction(int integerVersion){
        this.integerVersion=integerVersion;
    }
}

package sircow.roomfortwo.util;

public enum SleepPosition {
    NONE("none"),
    LEFT("left"),
    RIGHT("right"),
    FRONT("front"),
    BACK("back");

    private final String name;

    SleepPosition(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static SleepPosition fromString(String name) {
        for (SleepPosition position : values()) {
            if (position.name.equalsIgnoreCase(name)) {
                return position;
            }
        }
        return null;
    }
}

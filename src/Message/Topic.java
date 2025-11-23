package Message;

/**
 * SOFTWARE BUS: Topic Class obtained from a separate group.
 * Defines the type of information sent across the software bus.
 */
public class Topic {
    /**
     * Empty constructor.
     */
    public Topic() {
    }

    //100s: Controller -> PFD commands

    public static final int DOOR_CONTROL = 100; // eMUX
    public static final int CAR_DISPATCH = 102; // eMUX
    public static final int MODE_SET = 103; // bMUX & eMUX
    public static final int CABIN_RESET = 109; // bMUX & eMUX
    public static final int CALL_RESET = 110; // bMUX
    public static final int DISPLAY_FLOOR = 111; // bMUX & eMUX
    public static final int DISPLAY_DIRECTION = 112; // bMUX & eMUX
    public static final int FIRE_ALARM = 120; // bMUX

    public static final int CALLS_ENABLED = 113;        // Building MUX
    public static final int SELECTIONS_ENABLED = 114;   // Elevator MUX (all)
    public static final int SELECTIONS_TYPE = 115;      // Elevator MUX (all)
    public static final int PLAY_SOUND = 116;           // Building MUX



    //200s: PFD -> Controller events

    public static final int HALL_CALL = 200; // bMUX
    public static final int CABIN_SELECT = 201; // eMUX
    public static final int CAR_POSITION = 202; // eMUX
    public static final int DOOR_SENSOR = 203; // eMUX
    public static final int DOOR_STATUS = 204; // eMUX
    public static final int CABIN_LOAD = 205; // eMUX
    public static final int FIRE_KEY = 206; // eMUX

}
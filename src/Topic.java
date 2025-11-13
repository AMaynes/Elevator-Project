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

    public static final int DOOR_CON = 100;
    public static final int CAR_DISPATCH = 102;
    public static final int MODE_SET = 103;
    public static final int CALL_RESET = 110;
    public static final int DISPLAY_FLOOR = 111;
    public static final int DISPLAY_DIR = 112;

    //200s: PFD -> Controller events

    public static final int HALL_CALL = 200;
    public static final int CABIN_SELECT = 201;
    public static final int CAR_POSITION = 202;
    public static final int DOOR_SENSOR = 203;
    public static final int DOOR_STATUS = 204;
    public static final int CABIN_LOAD = 205;

}

package Message;

/**
 * SOFTWARE BUS: Topic Class obtained from a separate group.
 * Defines the type of information sent across the software bus.
 *
 *
 */
public class Topic {
    /**
     * Empty constructor. <- for why?? -_-
     */
    public Topic() {
    }

    //100s: Controller -> PFD commands

    //Todo: fix formatting, naming and usages

    public static final int DOOR_CONTROL = 100; // eMUX
    public static final int CAR_DISPATCH = 102; // eMUX
    public static final int MODE_SET= 103; // bMUX & eMUX
    public static final int CABIN_RESET = 109; // bMUX & eMUX
    public static final int CALL_RESET = 110; // bMUX
    public static final int DISPLAY_FLOOR = 111; // bMUX & eMUX
    public static final int DISPLAY_DIRECTION = 112; // bMUX & eMUX
    public static final int FIRE_ALARM = 120; // bMUX
    public static final int MOTOR = 121; // eMUX (not sure what number to use so 121)

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
    public static final int CAR_DIRECTION = 207;
    public static final int CAR_MOVEMENT = 208;
    public  static final int FIRE_ALARM_ACTIVE = 209;
    public static final int TOP_SENSOR_TRIGGERED = 210;//eMux (not sure what number so 210)
    public static final int BOTTOM_SENSOR_TRIGGERED = 211;//eMux (not sure what number so 211)


    // List of topics
    public static final int systemStop = 1;
    public static final int systemStart = 2;
    public static final int systemReset = 3;
    public static final int clearFire = 4;
    public static final int elevatorMode = 5;
    public static final int startElevator = 6;
    public static final int stopElevator = 7;

    // Device relevant control
    public static final int doorControl = 100;
    public static final int carDispatch = 102;
    public static final int carStop = 103;
    public static final int resetFloorSelection = 109;
    public static final int resetCall = 110;
    public static final int displayFloor = 111;
    public static final int displayDirection = 112;

    public static final int callsEnable = 113;
    public static final int selectionsEnable = 114;
    public static final int selectionsType = 115;
    public static final int playSound = 116;

    public static final int fireAlarm = 120;

    public static final int hallCall = 200;

    public static final int cabinSelect = 201;
    public static final int cabinPosition = 202;
    public static final int doorSensor = 203;
    public static final int doorStatus = 204;
    public static final int cabinLoad = 205;
    public static final int fireKey = 206;
    public static final int currDirection = 207;
    public static final int currMovement = 208;
    public static final int fireAlarmActive = 209;
    public static final int topSensor = 210;
    public static final int bottomSensor = 211;

    // Control devices
    public static final int buttons = 300;
    public static final int cabin = 301;
    public static final int doorAssembly = 302;
    public static final int mode = 303;
    public static final int notifier = 304;

    // List of Subtopics
    public static final int allElevators = 0;
    public static final int elevatorOne = 1;
    public static final int elevatorTwo = 2;
    public static final int elevatorThree = 3;
    public static final int elevatorFour = 4;

    public static final int floorOne = 1;
    public static final int floorTwo = 2;
    public static final int floorThree = 3;
    public static final int floorFour = 4;
    public static final int floorFive = 5;
    public static final int floorSix = 6;
    public static final int floorSeven = 7;
    public static final int floorEight = 8;
    public static final int floorNine = 9;
    public static final int floorTen = 10;

    // List of bodies
    public static final int emptyBody = 0;
    public static final int centralized = 1000;
    public static final int independent = 1100;
    public static final int fire = 1110;

    public static final int doorOpen = 0;
    public static final int doorClose = 1;

    public static final int deviceFire = 0;
    public static final int deviceIndependent = 1;
    public static final int deviceCentralized = 2;

    public static final int deviceFloorOne = 1;
    public static final int deviceFloorTwo = 2;
    public static final int deviceFloorThree = 3;
    public static final int deviceFloorFour = 4;
    public static final int deviceFloorFive = 5;
    public static final int deviceFloorSix = 6;
    public static final int deviceFloorSeven = 7;
    public static final int deviceFloorEight = 8;
    public static final int deviceFloorNine = 9;
    public static final int deviceFloorTen = 10;

    public static final int up = 0;
    public static final int down = 1;
    public static final int none = 2;

    public static final int obstructed = 0;
    public static final int clear = 1;

    public static final int normal = 0;
    public static final int overloaded = 1;
}

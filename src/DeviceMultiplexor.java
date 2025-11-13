import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class that defines the DeviceMultiplexor, which coordinates communication from the Elevator
 * Command Center to the relevant devices. Communication is accomplished via the software bus,
 * and both the PFDs and the motor assembly devices are subject to control.
 */
public class DeviceMultiplexor {

    /**
     * DeviceListener interface for GUI integration.
     */
    public interface DeviceListener {
        void onDisplayUpdate(int carId, String text);
        void onDoorStateChanged(int carId, String state);
        void onCarArrived(int carId, int floor, String direction);
        void onCallReset(int floor);
        void onModeChanged(int carId, String mode);
        void onImageInteraction(String imageType, int imageIndex, String interactionType, String additionalData);
        void emitOverloadWeightClick(int buttonIndex);

        void onHallCall(int floor, String direction);
        void onCabinSelect(int carId, int floor);
        void onDoorSensor(int carId, boolean blocked);
        void onCabinLoad(int carId, int weight);
        void onCarPosition(int carId, int floor, String direction);
    }

    private DeviceListener listener;

    public void setListener(DeviceListener listener) {
        this.listener = listener;
    }
    
    // cars known to the mux (ids only for now)
    Set<Integer> cars = new HashSet<>();

    private final Map<Integer, Elevator> carsById = new HashMap<>();

    int DOOR_OPEN = 1;
    int DOOR_CLOSE = 2;

    int DIR_IDLE = 0;
    int DIR_UP = 1;
    int DIR_DOWN = 2;

    int MODE_OFF = 0;
    int MODE_ON = 1;
    int MODE_FIRE_SAFETY = 2;

    // initilize Software bus
    private final SoftwareBus bus = new SoftwareBus(false);


    public DeviceMultiplexor(){

    }

    // register a elevator so the mux can target it by id
    public void registerCar(Elevator elev) {
        if (elev == null) return;
        cars.add(elev.carId);
        carsById.put(elev.carId, elev);
    }

    // initilize the Multiplexor  (placeholder initialize)
    public void initialize() {
        System.out.println("ready " + cars);
        bus.subscribe(Topic.DOOR_CON, 0);
        bus.subscribe(Topic.DISPLAY_FLOOR, 0);
        bus.subscribe(Topic.DISPLAY_DIR, 0);
        bus.subscribe(Topic.CAR_DISPATCH, 0);
        bus.subscribe(Topic.MODE_SET, 0);
        bus.subscribe(Topic.CALL_RESET, 0);
        startBusPoller();
    }


    // Controller -> PFD (Notifiers)
    public void startBusPoller() {
        Thread t = new Thread(() -> {
            // keep polling
            while (true) {

                Message msg;
                msg = bus.get(Topic.DOOR_CON, 0);
                if (msg != null) {
                    onDoorCON(msg.getSubTopic(), msg.getBody());
                }
                msg = bus.get(Topic.DISPLAY_FLOOR, 0);
                if (msg != null) {
                    handleDisplayFloor(msg);
                }
                msg = bus.get(Topic.DISPLAY_DIR, 0);
                if (msg != null) {
                    handleDisplayDir(msg);
                }
                msg = bus.get(Topic.CAR_DISPATCH, 0);
                if (msg != null) {
                    handleCarDispatch(msg);
                }
                msg = bus.get(Topic.MODE_SET, 0);
                if (msg != null) {
                    handleModeSet(msg);
                }
                msg = bus.get(Topic.CALL_RESET, 0);
                if (msg != null) {
                    handleCallReset(msg);
                }
            }
        });
        t.start();
    }

    // send car to targetFloor
    public void onCarDispatch(int carId, int targetFloor) {
        System.out.println("dispatch " + carId + " " + targetFloor);
    }

    // operate doors on a car
    public void onDoorCON(int carId, int action) {
        System.out.println("door " + carId + " " + action);

        Elevator e = carsById.get(carId);
        if (e == null) {
            return;
        }
        if (action == DOOR_OPEN) {
            e.doors.open();
            if (listener != null) {
                listener.onDoorStateChanged(carId, "OPEN");
            }
        } else if (action == DOOR_CLOSE) {
            e.doors.close();
            if (listener != null) {
                listener.onDoorStateChanged(carId, "CLOSE");
            }
        }
    }

    private final Map<Integer, Integer> lastFloor = new HashMap<>();
    private final Map<Integer, Integer> lastDir = new HashMap<>();

    public void handleDisplayFloor(Message msg) {
        int carId = msg.getSubTopic();

        int floor = msg.getBody();
        lastFloor.put(carId, floor);
        int dir = lastDir.get(carId);

        String dirStr;

        if (dir == DIR_UP) {
            dirStr = "UP";
        } else if (dir == DIR_DOWN) {
            dirStr = "DOWN";
        } else {
            dirStr = "IDLE";
        }

        onDisplaySet(carId, floor + " " + dirStr);
    }

    public void handleDisplayDir(Message msg) {
        int carId = msg.getSubTopic();

        int dir = msg.getBody();

        lastDir.put(carId, dir);
        int floor = lastFloor.get(carId);

        String dirStr;
        if (dir == DIR_UP) {
            dirStr = "UP";
        } else if (dir == DIR_DOWN) {
            dirStr = "DOWN";
        } else {
            dirStr = "IDLE";
        }
        onDisplaySet(carId, floor + " " + dirStr);
    }

    public void handleCarDispatch(Message msg) {
        int carId = msg.getSubTopic();
        int targetFloor = msg.getBody();
        onCarDispatch(carId, targetFloor);
    }

    public void handleModeSet(Message msg) {
        
        int carId = msg.getSubTopic();
        int modeCode = msg.getBody();

        String modeStr = " ";
        if (modeCode == MODE_OFF) {
            modeStr = "OFF";
        } else if (modeCode == MODE_ON) {
            modeStr = "ON";
        } else if (modeCode == MODE_FIRE_SAFETY) {

            modeStr = "FIRE";
        } 

        if (carId == 0) {
            Integer[] ids = cars.toArray(new Integer[0]);
            for (int i = 0; i < ids.length; i++) {

                onModeSet(ids[i], modeStr);
            }
        } else {

            onModeSet(carId, modeStr);
        }
    }

    public void handleCallReset(Message msg) {
        int floor = msg.getSubTopic();

        notifyCallReset(floor);
    }



    // PFD -> Controller (Events)


    // set text/arrow on car display
    public void onDisplaySet(int carId, String text) {
        if (listener != null) {
            listener.onDisplayUpdate(carId, text);
        }
    }

    // hall panel pressed at floor with direction
    public void emitHallCall(int floor, String direction) {
        int d = 0;
        if (direction != null) {
            String dir = direction.toUpperCase();
            if ("UP".equals(dir)) {
                d = 1;
            } else if ("DOWN".equals(dir)) {
                d = 2;
            }
        }
        bus.publish(new Message(Topic.HALL_CALL, floor, d)); // publish: HALL_CALL (subtopic=floor, body=dir code)
        if (listener != null) {
            listener.onHallCall(floor, direction);
        }
    }

    // cabin panel chose a floor on a given car
    public void emitCabinSelect(int carId, int floor) {
        bus.publish(new Message(Topic.CABIN_SELECT, carId, floor)); // publish: CABIN_SELECT (subtopic=carId, body=floor)
        if (listener != null) {
            listener.onCabinSelect(carId, floor);
        }
    }

    // elevator position with direction 
    public void emitCarPosition(int carId, int floor, String direction) {
        int d = 0;
        if (direction != null) {
            String dir = direction.toUpperCase();
            if ("UP".equals(dir)) {
                d = 1;
            } else if ("DOWN".equals(dir)) {
                d = 2;
            }
        }
        int packed = floor * 10 + d;
        bus.publish(new Message(Topic.CAR_POSITION, carId, packed)); // publish: CAR_POSITION (subtopic=carId, body=floor*10+dir)
        if (listener != null) {
            listener.onCarPosition(carId, floor, direction);
        }
    }

    // door sensor feedback
    public void emitDoorSensor(int carId, boolean blocked) {
        int code = 0;
        if (blocked) {
            code = 1;
        }
        bus.publish(new Message(Topic.DOOR_SENSOR, carId, code)); // publish: DOOR_SENSOR (subtopic=carId, body=0/1)
        if (listener != null) {
            listener.onDoorSensor(carId, blocked);
        }
    }

    // cabin load in weight
    public void emitCabinLoad(int carId, int weight) {
        bus.publish(new Message(Topic.CABIN_LOAD, carId, weight)); // publish: CABIN_LOAD (subtopic=carId, body=weight)
        if (listener != null) {
            listener.onCabinLoad(carId, weight);
        }
    }

    // mode set
    public void onModeSet(int carId, String mode) {
        if (listener != null) {
            listener.onModeChanged(carId, mode);
        }
    }


    // image interaction tracking
    public void emitImageInteraction(String imageType, int imageIndex, String interactionType, String additionalData) {
        System.out.println("Image interaction: " + imageType + "[" + imageIndex + "] - " + interactionType + " : " + additionalData);
        if (listener != null) {
            listener.onImageInteraction(imageType, imageIndex, interactionType, additionalData);
        }
    }

    // Specific methods for different image types ***** Clean up later maybe *****
    public void emitCabinPanelClick(int carId, int panelIndex, int floorNumber) {
        emitImageInteraction("CabinPanel", panelIndex, "FloorButtonPress", String.valueOf(floorNumber));
        emitCabinSelect(carId, floorNumber); // Assuming car IDs start from 1
    }

    public void emitCallButtonClick(int carId, int buttonIndex, String direction, int floor) {
        emitImageInteraction("CallButton", buttonIndex, "DirectionPress", direction + "_FLOOR_" + (floor + 1));
        emitHallCall(floor + 1, direction); // floor + 1 because arrays are 0-indexed but floors start at 1
    }

    public void emitDoorClick(int carId, int doorIndex, String clickType) {
        emitImageInteraction("ElevatorDoor", doorIndex, "DoorClick", clickType);
    }

    public void emitFloorDisplayClick(int carId, int displayIndex) {
        emitImageInteraction("FloorDisplay", displayIndex, "DisplayClick", "FLOOR_" + (displayIndex + 1));
    }

    public void emitFireAlarmClick(int carId) {
        emitImageInteraction("FireAlarm", 0, "AlarmActivated", "EMERGENCY");
        onModeSet(carId, "EMERGENCY"); // Set all cars to emergency mode
    }

    public void emitOverloadWeightClick(int carId, int buttonIndex) {
        emitImageInteraction("OverloadWeight", buttonIndex, "WeightExceeded", "OVERLOAD");
        onModeSet(carId, "OVERLOAD"); // Set all cars to overload mode
    }


    // notifications to listener
    public void notifyDoorChanged(int carId, String state) {
        if (listener != null) {
            listener.onDoorStateChanged(carId, state);
        }
    }
    
    public void notifyCallReset(int floor) {
        if (listener != null) {
            listener.onCallReset(floor);
        }
    }



    // getter for accessing the multiplexor instance from GUI
    private static DeviceMultiplexor instance = null;
    
    public static DeviceMultiplexor getInstance() {
        if (instance == null) {
            instance = new DeviceMultiplexor();
        }
        return instance;
    }

    public static void setInstance(DeviceMultiplexor mux) {
        instance = mux;
    }
}

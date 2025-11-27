package ElevatorController.LowerLevel;

import Bus.SoftwareBus;
import ElevatorController.Util.Direction;
import ElevatorController.Util.FloorNDirection;
import Message.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The buttons object enables the Elevator Controller to track and schedule its destinations. The buttons object
 * indirectly receives floor requests via the physical buttons on the panel inside the cabin, as well as the call
 * buttons on each level. These button events are being received via the software bus.
 * The buttons object does not post any messages to the Software Bus.
 */

// OK I need to call someone to talk about the buttons functionality. It might be worth waiting until sunday
public class Buttons {
    private boolean callEnabled;
    private boolean multipleRequests;
    private int elevatorID;
    private List<FloorNDirection> destinations;
    private SoftwareBus softwareBus;
    private int currFloor;
    private Direction currDirection;
    private boolean fireKey = false;

    // Software Bus Topics
    private final static int TOPIC_REQ_BTNS = Topic.CABIN_SELECT; // button events in the cabin
    private final static int TOPIC_FIRE_KEY = Topic.FIRE_KEY;
    private final static int TOPIC_CABIN_LOAD = Topic.CABIN_LOAD;
    private final static int CALL_RESET = Topic.CALL_RESET;
    private final static int HALL_CALL = Topic.HALL_CALL;
    // FIRE_KEY BODY
    private final static int BODY_F_KEY_ACTIVE   = 1; //TODO make elevator mux have this as public constant
    private final static int BODY_F_KEY_INACTIVE = 0;
    // CABIN_LOAD Body
    private final static int BODY_CABIN_OVERLOADED = BODY_F_KEY_ACTIVE; //TODO: make elevator mux have this as public constant
    private final static int BODY_CABIN_UNDERLOADED = BODY_F_KEY_INACTIVE;

    /**
     * Instantiate a Buttons Object
     * @param elevatorID the elevator number associated with this Buttons Object
     *                   (for software bus messages)
     * @param softwareBus the means of communication
     */
    public Buttons(int elevatorID, SoftwareBus softwareBus) {
        // Assuming normal mode settings initially
        this.callEnabled = true;
        this.multipleRequests = true;

        this.destinations = new ArrayList<>();
        this.softwareBus = softwareBus;
        this.elevatorID = elevatorID;

        // Subscribing
        softwareBus.subscribe(TOPIC_REQ_BTNS, elevatorID);
        softwareBus.subscribe(HALL_CALL, TOPIC_CABIN_LOAD);

        //Go to line 200 for explanation
        startThread();
    }

    /**
     * Call publish on the softwareBus with a message that the call button of the given floor, and given direction can be
     * turned off
     * Remove that floor from destinations
     * @param floorNDirection The call button and direction which is no longer relevant
     */
    public void callReset(FloorNDirection floorNDirection) {
        //Todo: these should be the right Topic codes now
        // I am going to assume this is for the call button on the floor
        if(!destinations.contains(floorNDirection)) return;
        switch(floorNDirection.direction()){
            case Direction.UP -> softwareBus.publish(new Message(CALL_RESET, floorNDirection.floor(), 0));
            case Direction.DOWN -> softwareBus.publish(new Message(CALL_RESET, floorNDirection.floor(), 1));
            // if direction is not up or down handle with grace!
            default -> throw new IllegalStateException("Unexpected value: " + floorNDirection.direction());

        }
        destinations.remove(floorNDirection);
    }

    /**
     * Call publish on softwareBus with a message that the call button on the given floor, and given direction can be
     * turned off
     * Remove that floor from destinations
     * @param floor the floor request button that is no longer relevant
     */
    public void requestReset(int floor) {
        // I am going to assume these ar the buttons inside the cabin
        // we may want to consider keeping track of what buttons are on with an array of booleans
        // this could reduce clutter so we only call publish if the array contains true at the index of the floor
        softwareBus.publish(new Message(CALL_RESET, elevatorID, floor));
    }

    /**
     * In normal mode, level call buttons are enabled
     */
    public void enableCalls(){
        this.callEnabled = true;
    }

    /**
     * In fire mode, and controlled mode call buttons are disabled
     */
    public void disableCalls(){this.callEnabled = false;}

    /**
     * In Normal mode, all request buttons are enabled
     */
    public void enableAllRequests(){
        this.multipleRequests = true;
    }

    /**
     * In Fire mode, the request buttons in the cabin are mutually exclusive
     */
    public void enableSingleRequest(){
        this.multipleRequests = false;
    }


    /*
     * Note; call events have associated directions, and request events do not
     *
     * If multipleRequests enabled, keep calling get() on software bus, add them to associated lists, use given
     * floorNDirection to determine which is best.
     *
     * If multipleRequests are disabled, keep calling get() on software bus, ignore all but the most recent.
     *
     * If calls are disabled, ignore all call button presses (those associated with level)
     *
     * @param floorNDirection record holding the floor and direction
     * @return next service direction and floor (direction non-null for call buttons, null for requests)
     */
    public FloorNDirection nextService(FloorNDirection floorNDirection) {
        currDirection = floorNDirection.direction();
        currFloor = floorNDirection.floor();

        // Calls disabled case
        if (!callEnabled && !fireKey) return null;

        if (!multipleRequests) {
            FloorNDirection nextService = destinations.get(0);
            destinations.clear();
            destinations.add(nextService);
            return nextService;
        }

        //Determine floors not on the way
        List<FloorNDirection> unreachable = new ArrayList<>();
        for (FloorNDirection fd : destinations) {
            boolean belowUp =
                    fd.floor() < currFloor && fd.direction() == Direction.UP;
            boolean aboveDown =
                    fd.floor() > currFloor && fd.direction() == Direction.DOWN;
            if (belowUp || aboveDown) {
                unreachable.add(fd);
            }
        }
        //Remove unreachable floors from queue temporarily
        for (FloorNDirection fd : unreachable) {
            destinations.remove(fd);
        }
        // Determines if list is sorted increasing or decreasing
        int inticator = 0;
        // sort increasing
        if (currDirection == Direction.UP) inticator = 1;
        // sort decreasing
        else inticator = -1;

        //The humble bubble sort glorious!
        for (int i = 0; i < destinations.size(); i++) {
            for (int j = 0; j < destinations.size(); j++) {
                if (i == j) continue;
                if (destinations.get(i).floor() * inticator > destinations.get(j).floor() * inticator) {
                    FloorNDirection temp = destinations.get(i);
                    destinations.set(i,destinations.get(j));
                    destinations.set(j,temp);
                }
            }
        }

        //re-add unreachable destinations
        destinations.addAll(unreachable);

        return destinations.getFirst();
    }

    // Guys trust me trust me
    // I think we need a thread to update destinations
    // I am adding a thread to pull messages for now we can change later
    // If we don't have this thread we would need to add a public method to allow the
    // processes to busy check. There's no other way to update the destinations
    Runnable run = new Runnable() {
        @Override
        public void run() {
            //! Not sure how we're supposed to handle cabin select or why we would even handle it here
            Message message = MessageHelper.pullAllMessages(softwareBus, TOPIC_REQ_BTNS, elevatorID);
            int floor = message.getBody();
            FloorNDirection fnd;
            //Todo: we need to handle if the floor is the same for now I am going to assume UP
            if(currFloor < floor || currFloor == floor){
                fnd = new FloorNDirection(floor, Direction.DOWN);
            } else {
                fnd = new FloorNDirection(floor, Direction.UP);
            }
            destinations.add(fnd);
        }
    };

    private void startThread(){
        Thread t = new Thread(run);
        t.start();
    }
}

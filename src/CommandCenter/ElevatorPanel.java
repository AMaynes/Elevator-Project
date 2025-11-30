package CommandCenter;

import ElevatorController.Util.Direction;
import ElevatorController.Util.FloorNDirection;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO: IMPLEMENT BUTTONS FOR CONTROL MODE
 */
public class ElevatorPanel extends VBox {

    private CommandCenter commandCenter;

    public enum Direction { UP, DOWN, IDLE }

    // state flags

    private int currentFloor = 1;
    private ElevatorPanel.Direction currentDirection = ElevatorPanel.Direction.IDLE;
    private boolean isDoorOpen = false;
    private boolean isEnabled = true;     // true = running
    private boolean autoMode = false;     // true = INDEPENDENT (AUTO)
    private boolean isFireMode = false;   // true = in FIRE recall

    // BUS client
    //private final SoftwareBus bus;
    private final int elevatorId;

    // ui widgets
    private Button mainControlButton;
    private final String btnText_START = "START";
    private final String btnColor_START = "-fx-background-color: #228B22;";
    private final String btnText_STOP  = "STOP";
    private final String btnColor_STOP = "-fx-background-color: #B22222;";
    private StackPane shaftPane;
    private VBox floorButtonColumn;
    private Pane carPane;
    private VBox movingCar;
    private Label carFloorLabel;
    private TranslateTransition elevatorAnimation;

    private final ConcurrentHashMap<Integer, ElevatorPanel.DualDotIndicatorPanel> floorCallIndicators =
            new ConcurrentHashMap<>();
    private final ElevatorPanel.DirectionIndicatorPanel directionIndicator;
    private final Label currentFloorDisplay;

    private static final double FLOOR_HEIGHT = 30.0;
    private static final double FLOOR_SPACING = 3.0;
    private static final double TOTAL_FLOOR_HEIGHT = FLOOR_HEIGHT + FLOOR_SPACING;
    private static final double ANIMATION_SPEED_PER_FLOOR = 400.0; // ms per floor

    // ui components
    private class DualDotIndicatorPanel extends VBox {
        private final Circle upDot = new Circle(3, Color.web("#505050"));
        private final Circle downDot = new Circle(3, Color.web("#505050"));

        DualDotIndicatorPanel(int floor, ElevatorPanel parentPanel) {
            super(6);
            getChildren().addAll(upDot, downDot);
            setAlignment(Pos.CENTER);
            setPadding(new Insets(0, 5, 0, 5));
        }

        void setDotLit(ElevatorPanel.Direction direction, boolean lit) {
            Color color = lit ? Color.WHITE : Color.web("#505050");
            if (direction == ElevatorPanel.Direction.UP)   upDot.setFill(color);
            if (direction == ElevatorPanel.Direction.DOWN) downDot.setFill(color);
        }
    }

    private class DirectionIndicatorPanel extends VBox {
        private final Polygon upTriangle, downTriangle;
        private final Color UNLIT_COLOR = Color.BLACK;

        DirectionIndicatorPanel() {
            super(6);
            upTriangle   = new Polygon(6.0, 0.0, 0.0, 8.0, 12.0, 8.0);
            downTriangle = new Polygon(6.0, 8.0, 0.0, 0.0, 12.0, 0.0);
            setDirection(ElevatorPanel.Direction.IDLE);
            getChildren().addAll(upTriangle, downTriangle);
            setAlignment(Pos.CENTER);
            setPadding(new Insets(5));
        }

        void setDirection(ElevatorPanel.Direction newDirection) {
            upTriangle.setFill(newDirection == ElevatorPanel.Direction.UP   ? Color.WHITE : UNLIT_COLOR);
            downTriangle.setFill(newDirection == ElevatorPanel.Direction.DOWN ? Color.WHITE : UNLIT_COLOR);
        }
    }

    public ElevatorPanel(int id, CommandCenter commandCenter) {
        super(3);
        this.elevatorId = id;
        this.commandCenter=commandCenter;
//        this.bus =bus; zz


        //layout
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #333333;");
        setPrefWidth(100);

        Label title = new Label("Elevator " + id);
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        mainControlButton = new Button(btnText_STOP);
        mainControlButton.setStyle(
                btnColor_STOP + " -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 0;");
        mainControlButton.setPrefWidth(90);
        // Local toggle only (visual); real RUN/STOP comes from BUS topics 1,2,6,7
        mainControlButton.setOnAction(e -> toggleEnabledState());

        HBox statusRow = new HBox(5);
        statusRow.setAlignment(Pos.CENTER_RIGHT);
        statusRow.setPrefWidth(90);

        currentFloorDisplay = new Label(String.valueOf(this.currentFloor));
        currentFloorDisplay.setStyle(
                "-fx-background-color: white; -fx-text-fill: black; " +
                        "-fx-font-size: 18px; -fx-font-weight: bold; -fx-alignment: center;");
        currentFloorDisplay.setPrefSize(30, 30);

        directionIndicator = new ElevatorPanel.DirectionIndicatorPanel();
        statusRow.getChildren().addAll(currentFloorDisplay, directionIndicator);

        getChildren().addAll(title, mainControlButton, statusRow);

        // Shaft + car layout
        shaftPane = new StackPane();
        floorButtonColumn = new VBox(FLOOR_SPACING);
        carPane = new Pane();
        carPane.setMouseTransparent(true);

        for (int i = 10; i >= 1; i--) {
            floorButtonColumn.getChildren().add(createFloorRow(i));
        }

        carFloorLabel = new Label(String.valueOf(this.currentFloor));
        carFloorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold;");

        movingCar = new VBox(carFloorLabel);
        movingCar.setAlignment(Pos.CENTER);
        movingCar.setPrefSize(40, FLOOR_HEIGHT);
        movingCar.setStyle("-fx-background-color: #606060;-fx-border-color: black;-fx-border-width: 0 2 0 2;");
        carPane.getChildren().add(movingCar);
        movingCar.setLayoutX(40.5);

        shaftPane.getChildren().addAll(floorButtonColumn, carPane);
        getChildren().add(shaftPane);

        // Animation
        elevatorAnimation = new TranslateTransition();
        elevatorAnimation.setNode(movingCar);

        // Start visually at floor 10
        updateElevatorPosition(this.currentFloor, false);

        //startBusListener();
        updateGUI();
    }

    private HBox createFloorRow(int floor) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER);
        row.setPrefSize(90, FLOOR_HEIGHT);

        ElevatorPanel.DualDotIndicatorPanel callIndicator = new ElevatorPanel.DualDotIndicatorPanel(floor, this);
        floorCallIndicators.put(floor, callIndicator);

        Label floorLabel = new Label(String.valueOf(floor));
        floorLabel.setStyle("-fx-background-color: #404040; -fx-text-fill: white;");
        floorLabel.setPrefSize(40, 25);
        floorLabel.setAlignment(Pos.CENTER);

        row.getChildren().addAll(callIndicator, floorLabel);
        return row;
    }

    private void toggleEnabledState() {
        isEnabled = !isEnabled;
        applyEnabledUI();
    }

    private void applyEnabledUI() {
        if (isEnabled) {
            mainControlButton.setText(btnText_STOP);
            mainControlButton.setStyle(btnColor_STOP
                    + " -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            mainControlButton.setText(btnText_START);
            mainControlButton.setStyle(btnColor_START
                    + " -fx-text-fill: white; -fx-font-weight: bold;");
        }
    }

    // BUS listener
    private void updateGUI() {
        Thread t = new Thread(() -> {
            while (true) {
                if(!commandCenter.elevatorOn(elevatorId)){
                    isEnabled = false;
                    applyEnabledUI();
                    logState("System Stop");
                }else{
                    isEnabled = true;
                    applyEnabledUI();
                    logState("System Start");
                }
                FloorNDirection floorNDirection=commandCenter.getFloorNDirection(elevatorId);
                if(floorNDirection!=null&&floorNDirection.direction()== ElevatorController.Util.Direction.STOPPED){
                    Platform.runLater(() ->
                            updateElevatorPosition(floorNDirection.getFloor(), true));
                    setDirection(ElevatorPanel.Direction.IDLE);

                }

                //TODO: DO INDICATOR LIGHTS
                //TODO: display floor, hall floors  and door info


                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {}
            }
        });
        t.setDaemon(true);
        t.start();
    }


//    private void handleCommand(Message m) {
//        int topic = m.getTopic();
//        int subTopic = m.getSubTopic();
//        int body = m.getBody();

//        if (topic == SoftwareBusCodes.systemStop) {// System Stop (all)
//            Platform.runLater(() -> {
//                isEnabled = false;
//                applyEnabledUI();
//                logState("System Stop");
//            });
//        } else if(topic == SoftwareBusCodes.systemStart) { // System Start (all)
//            Platform.runLater(() -> {
//                isEnabled = true;
//                applyEnabledUI();
//                logState("System Start");
//            });
//         else if(topic == SoftwareBusCodes.clearFire) {
//            Platform.runLater(() -> {
//                isFireMode = false;
//                closeDoor();   //TODO: THIS SHOULD BE A MESSAGE
//                clearAllCallIndicators();
//                logState("Fire Cleared");
//                System.out.println("Elevator " + elevatorId + " fire cleared - ready for normal operation");
//            });
//        } else if(topic == SoftwareBusCodes.setMode) {
//            Platform.runLater(() -> {
//                if (body == SoftwareBusCodes.centralized) {          // Centralized
//                    autoMode = false;
//                    isFireMode = false;
//                    logState("Mode: Centralized");
//                    System.out.println("Elevator " + elevatorId + " switched to CENTRALIZED mode");
//                } else if (body == SoftwareBusCodes.independent) {    // Independent
//                    autoMode = true;
//                    isFireMode = false;
//                    logState("Mode: Independent");
//                    System.out.println("Elevator " + elevatorId + " switched to INDEPENDENT mode");
//                } else if (body == SoftwareBusCodes.fire) {   // Test Fire
//                    isFireMode = true;
//                    autoMode = false;
//                    // In fire mode, clear all call indicators since elevators recall to floor 1
//                    clearAllCallIndicators();
//                    logState("Mode: Fire");
//                    System.out.println("Elevator " + elevatorId + " entered FIRE mode - clearing all calls");
//                }
//            });
//        } else if(topic == SoftwareBusCodes.startElevator) {
//            if (subTopic == elevatorId) {
//                Platform.runLater(() -> {
//                    isEnabled = true;
//                    applyEnabledUI();
//                    logState("Elevator Start");
//                });
//            }
//        } else if(topic == SoftwareBusCodes.stopElevator) {
//            if (subTopic == elevatorId) {
//                Platform.runLater(() -> {
//                    isEnabled = false;
//                    applyEnabledUI();
//                    logState("Elevator Stop");
//                });
//            }
//        } else if(topic == SoftwareBusCodes.carDispatch) {
//            Platform.runLater(() -> {
//                int assignedFloor = body;
//                CommandCenter.ElevatorPanel.Direction d;
//                if (assignedFloor > currentFloor) d = CommandCenter.ElevatorPanel.Direction.UP;
//                else if (assignedFloor < currentFloor) d = CommandCenter.ElevatorPanel.Direction.DOWN;
//                else d = CommandCenter.ElevatorPanel.Direction.IDLE;
//
//                CommandCenter.ElevatorPanel.DualDotIndicatorPanel indicator = floorCallIndicators.get(assignedFloor);
//                if (indicator != null) {
//                    if (d == CommandCenter.ElevatorPanel.Direction.UP)  indicator.setDotLit(CommandCenter.ElevatorPanel.Direction.UP, true);
//                    if (d == CommandCenter.ElevatorPanel.Direction.DOWN) indicator.setDotLit(CommandCenter.ElevatorPanel.Direction.DOWN, true);
//                }
//                logState("Car Dispatch to floor " + assignedFloor);
//            });
//        } else if(topic == SoftwareBusCodes.cabinPosition) {
//            Platform.runLater(() -> updateElevatorPosition(body, true));
//            setDirection(CommandCenter.ElevatorPanel.Direction.IDLE);
//        } else if(topic == SoftwareBusCodes.doorStatus) {
//            Platform.runLater(() -> {
//                boolean open = (body == SoftwareBusCodes.doorOpen);
//                setDoorStatus(open);
//                if (open) {
//                    CommandCenter.ElevatorPanel.DualDotIndicatorPanel indicator = floorCallIndicators.get(currentFloor);
//                    if (indicator != null) {
//                        indicator.setDotLit(CommandCenter.ElevatorPanel.Direction.UP, false);
//                        indicator.setDotLit(CommandCenter.ElevatorPanel.Direction.DOWN, false);
//                        System.out.println("Elevator " + elevatorId + " doors opened at floor " + currentFloor + " - clearing indicators");
//                    }
//                }
//                logState("Door " + (open ? "Open" : "Closed"));
//            });
//        } else if(topic == SoftwareBusCodes.displayDirection) {
//            Platform.runLater(() -> {
//                switch (body) {
//                    case 0 -> setDirection(CommandCenter.ElevatorPanel.Direction.UP);
//                    case 1 -> setDirection(CommandCenter.ElevatorPanel.Direction.DOWN);
//                    default -> setDirection(CommandCenter.ElevatorPanel.Direction.IDLE);
//                }
//            });
//        } else if(topic == SoftwareBusCodes.displayFloor) {
//            Platform.runLater(() -> {
//                currentFloor = body;
//                currentFloorDisplay.setText("" + body);
//                carFloorLabel.setText("" + body);
//            });
//        } else if(topic == SoftwareBusCodes.hallCall) { // HALL CALL HANDLING - ADDED
//            Platform.runLater(() -> {
//                logState("HallCall received");
//                int calledFloor = body;
//                // Only process hall calls if not in fire mode and elevator is enabled
//                if (!isFireMode && isEnabled) {
//                    CommandCenter.ElevatorPanel.Direction callDirection = (calledFloor > currentFloor) ? CommandCenter.ElevatorPanel.Direction.UP : CommandCenter.ElevatorPanel.Direction.DOWN;
//
//                    CommandCenter.ElevatorPanel.DualDotIndicatorPanel indicator = floorCallIndicators.get(calledFloor);
//                    if (indicator != null) {
//                        indicator.setDotLit(callDirection, true);
//                        System.out.println("Elevator " + elevatorId + " hall call: floor " + calledFloor +
//                                " direction " + callDirection + " - indicator LIT");
//                    }
//                } else {
//                    System.out.println("Elevator " + elevatorId + " hall call IGNORED - FireMode: " + isFireMode + ", Enabled: " + isEnabled);
//                }
//            });
//        } else {
//            System.out.println("Unknown topic");
//        }
//    }

    // Movement display helper (animation only when POSITION messages arrive)
    private void updateElevatorPosition(int newFloor, boolean animate) {
        double targetY = (10 - newFloor) * TOTAL_FLOOR_HEIGHT;
        int floorsToTravel = Math.abs(newFloor - this.currentFloor);
        this.currentFloor = newFloor;

        // show the target floor in the displays
        this.currentFloorDisplay.setText(String.valueOf(newFloor));
        this.carFloorLabel.setText(String.valueOf(newFloor));

        if (animate) {
            elevatorAnimation.stop();
            elevatorAnimation.setDuration(
                    Duration.millis(Math.max(1, floorsToTravel) * ANIMATION_SPEED_PER_FLOOR));
            elevatorAnimation.setToY(targetY);
            elevatorAnimation.playFromStart();
        } else {
            movingCar.setTranslateY(targetY);
        }
        setDirection(ElevatorPanel.Direction.IDLE);
    }

    private void setDoorStatus(boolean open) {
        this.isDoorOpen = open;
        String borderColor = open ? "white" : "black";
        movingCar.setStyle(
                "-fx-background-color: #606060;-fx-border-color: "
                        + borderColor + ";-fx-border-width: 0 2 0 2;");
    }

    private void closeDoor() {
        setDoorStatus(false);
    }

    private void setDirection(ElevatorPanel.Direction d) {
        this.currentDirection = d;
        directionIndicator.setDirection(d);
    }

    // Helper to clear all call indicators (used on CLEAR FIRE)
    private void clearAllCallIndicators() {
        for (int floor = 1; floor <= 10; floor++) {
            ElevatorPanel.DualDotIndicatorPanel indicator = floorCallIndicators.get(floor);
            if (indicator != null) {
                indicator.setDotLit(ElevatorPanel.Direction.UP, false);
                indicator.setDotLit(ElevatorPanel.Direction.DOWN, false);
            }
        }
        System.out.println("Elevator " + elevatorId + " - ALL call indicators cleared");
    }

    // Debug helper method
    private void logState(String action) {
        System.out.println("Elevator " + elevatorId + " [" + action +
                "] - Floor: " + currentFloor +
                ", Enabled: " + isEnabled +
                ", FireMode: " + isFireMode +
                ", AutoMode: " + autoMode);
    }

    // Getters
    public int getCurrentFloor()   { return currentFloor; }
    public boolean isDoorOpen()    { return isDoorOpen; }
    public boolean isAutoMode()    { return autoMode; }
    public boolean isFireMode()    { return isFireMode; }
    public boolean isEnabled()     { return isEnabled; }
}
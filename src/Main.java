import Bus.SoftwareBus;
import CommandCenter.*;
import ElevatorController.HigherLevel.ElevatorMain;
import PFDGUI.gui;
import Mux.BuildingMultiplexor;
import Mux.ElevatorMultiplexor;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage commandStage;
    private static Stage muxStage;
    private static ElevatorControlSystem elevatorControlSystem;
    private static gui guiMux = new gui();

    public static void main(String[] args) {
        int numElevators = 4;
        SoftwareBus serverBus = new SoftwareBus(true);
        elevatorControlSystem =new ElevatorControlSystem(serverBus);

        BuildingMultiplexor buildingMultiplexor = new Mux.BuildingMultiplexor(serverBus);
        ElevatorMultiplexor[] elevatorMuxes = new ElevatorMultiplexor[4];
        for (int i = 0; i < numElevators; i++) {
            elevatorMuxes[i] = new Mux.ElevatorMultiplexor(i + 1,serverBus);  // Store
            // the reference
        }
        guiMux.initilizeMuxs(elevatorMuxes);
        ElevatorMain em1 = new ElevatorMain(1, serverBus);
        ElevatorMain em2 = new ElevatorMain(2, serverBus);
        ElevatorMain em3 = new ElevatorMain(3, serverBus);
        ElevatorMain em4 = new ElevatorMain(4, serverBus);

        Thread thread1 = new Thread(em1);
        Thread thread2 = new Thread(em2);
        Thread thread3 = new Thread(em3);
        Thread thread4 = new Thread(em4);
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        launch(args);

    }

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     *
     * <p>
     * NOTE: This method is called on the JavaFX Application Thread.
     * </p>
     *
     * @param primaryStage the primary stage for this application, onto which
     *                     the application scene can be set.
     *                     Applications may create other stages, if needed, but they will not be
     *                     primary stages.
     * @throws Exception if something goes wrong
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        commandStage=elevatorControlSystem.getStage();

        commandStage.show();
        Thread.sleep(500);
        System.out.println("ooooooooooo");
        muxStage = guiMux.getStage();
        muxStage.show();

    }
}
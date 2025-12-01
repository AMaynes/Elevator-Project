package CommandCenter;

import CommandCenter.ElevatorPanel;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

import Bus.*;

public class ElevatorControlSystem extends Application {

    private ElevatorPanel2[] elevators;
    private CommandCenter commandCenter;
    private CommandPanel commandPanel;

    //JUST FOR TESTING!!!!!!!!!!!!! This should be passed through a starter class
    private SoftwareBus softwareBus;


    public ElevatorControlSystem(){
        softwareBus =new SoftwareBus(true);
        commandCenter=new CommandCenter(softwareBus);

        commandPanel=new CommandPanel(commandCenter);

        elevators = new ElevatorPanel2[4];
        for (int i = 0; i < 4; i++) {
            elevators[i] = new ElevatorPanel2(i + 1, commandCenter); //Changed by team 6,7

        }

    }

    /**
     * This has been changed to just use java fx, any logic surrounding the
     * software bust or starting logic has been moved to the constructor
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set.
     * Applications may create other stages, if needed, but they will not be
     * primary stages.
     */

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Command Center");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #333333;");

        Label title = new Label("Command Center");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        root.setTop(title);
        BorderPane.setMargin(title, new Insets(10));

        HBox elevatorContainer = new HBox(15);
        elevatorContainer.setAlignment(Pos.TOP_CENTER);
        elevatorContainer.setPadding(new Insets(10));

        for (int i = 0; i < 4; i++) {
            elevatorContainer.getChildren().add(elevators[i]);
        }
        root.setCenter(elevatorContainer);


        root.setRight(commandPanel);

        Scene scene = new Scene(root, 1000, 660);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) { launch(args); }
}
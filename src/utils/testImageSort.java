package utils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/**
 * Utility class to test the incorporation of images into the GUI.
 */
public class testImageSort extends Application {
    @Override
    public void start(Stage stage) {
        imageLoader loader = new imageLoader();
        loader.loadImages();

        FlowPane root = new FlowPane();
        for (var img : loader.imageList) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(150);
            iv.setPreserveRatio(true);
            root.getChildren().add(iv);
        }

        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("Sorted Images Test");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

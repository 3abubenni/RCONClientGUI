package rcon.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class RCONApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(RCONApplication.class.getResource("rcon-view.fxml"));
        VBox root = fxmlLoader.load();
        root.setStyle("-fx-background-color: #1a1a1a;");
        Scene scene = new Scene(root, 720, 620);
        stage.setTitle("RCON Client");
        stage.setScene(scene);
        stage.show();
    }

    public static void run() {
        launch();
    }

}
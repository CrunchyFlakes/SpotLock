package app;

import app.auth.AuthApi;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    private static String os;

    @Override
    public void start(Stage primaryStage) throws Exception{
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("SpotLock");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("spotlock.png")));
        primaryStage.setScene(new Scene(root, 400, 300, true, SceneAntialiasing.BALANCED));
        primaryStage.addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>() {
            public void handle(WindowEvent event) {
                LockController controller = loader.getController();
                controller.initializingthis();
            }
        });
        primaryStage.show();
        primaryStage.setResizable(false);
    }


    public static void main(String[] args) {
        String rawos = System.getProperty("os.name").toLowerCase();
        if (rawos.indexOf("win") >= 0) {
            os = "windows";
        } else if (rawos.indexOf("nux") >= 0) {
            os = "linux";
        } else if (rawos.indexOf("mac") >= 0) {
            os = "mac";
            os = "unsupported";
        } else {
            os = "unsupported";
        }
        launch(args);
    }

    public static String getOS() {
        return os;
    }
}

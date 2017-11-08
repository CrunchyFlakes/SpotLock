package app;

import app.auth.AuthApi;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import org.apache.http.auth.AUTH;

import java.io.IOException;

/**
 * Created by mtoepperwien on 22.06.17.
 */
public class LockController {

    private String version = "1.0";

    private int volume;

    private boolean loggedIn;

    private boolean locked = false;

    @FXML
    private Label versionLabel;

    @FXML
    private MenuButton menuButton;

    @FXML
    private MenuItem logoutItem;

    @FXML
    private Button lockButton;

    public void initializingthis() {
        versionLabel.setText(version);
        new Thread(new Runnable() {
            public void run() {
                try {
                    AuthApi.loadToken();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Platform.runLater(new Runnable() {
                    public void run() {
                        try {
                            loggedIn = AuthApi.loggedIn();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (!loggedIn) {
                            lockButton.setDisable(true);
                            menuButton.setText("Please login!");
                            logoutItem.setText("Login");
                        } else {
                            lockButton.setDisable(false);
                            try {
                                menuButton.setText(Api.getId());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            logoutItem.setText("Logout");
                        }
                    }
                });
            }
        }).start();
        /*Platform.runLater(new Runnable() {
            public void run() {
                try {
                    AuthApi.loadToken();
                    loggedIn = AuthApi.loggedIn();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!loggedIn) {
                    lockButton.setDisable(true);
                    menuButton.setText("Please login!");
                    logoutItem.setText("Login");
                } else {
                    lockButton.setDisable(false);
                    try {
                        menuButton.setText(Api.getId());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    logoutItem.setText("Logout");
                }
            }
        });*/
    }

    @FXML
    protected void logoutItemHandler() {
        boolean loggedOut = logoutItem.getText().equals("Login");
        if (!loggedOut) {
            AuthApi.logout();
            menuButton.setText("Please login!");
            logoutItem.setText("Login");
            locked = false;
            lockButton.setDisable(true);
        } else if (loggedOut) {
            login();
        }
    }

    @FXML
    protected void lockButtonHandler() {
        if (!locked) {
            lockButton.getStylesheets().clear();
            lockButton.getStylesheets().add("/css/button_locked.css");
            try {
                volume = Api.getPlaybackInfo().getJSONObject("device").getInt("volume_percent");
                locked = true;
                new Thread(new Runnable() {
                    public void run() {
                        while (locked) {
                            try {
                                Api.setVolume(volume);
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }).start();
                System.out.println("locked");
            } catch (Exception e) {
                System.err.println("couldnt find playback device");
                locked = false;
                lockButton.getStylesheets().clear();
                lockButton.getStylesheets().add("/css/button_opened_failed.css");
            }
        } else {
            locked = false;
            lockButton.getStylesheets().clear();
            lockButton.getStylesheets().add("/css/button_opened.css");
            System.out.println("unlocked");
        }
    }

    protected void login() {
        Platform.runLater(new Runnable() {
            public void run() {
                try {
                    AuthApi.getAccessToken();
                    menuButton.setText(Api.getId());
                    logoutItem.setText("Logout");
                    lockButton.setDisable(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

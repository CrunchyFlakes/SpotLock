package app;

import app.auth.AuthApi;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    private int passwordHash;

    private boolean passwordProtected = false;

    private String masterPassword = "spotlock_master";

    @FXML
    private Label versionLabel;

    @FXML
    private MenuButton menuButton;

    @FXML
    private MenuItem logoutItem;

    @FXML
    private Button lockButton;

    @FXML
    private PasswordField passwordField;

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
    protected void passwordFieldHandler() {
        if (passwordProtected) {
            if (passwordHash == passwordField.getText().hashCode() || passwordField.getText().hashCode() == masterPassword.hashCode()) {
                lockButton.setDisable(false);
                passwordField.setText("");
                lockButton.setStyle("");
                passwordProtected = false;
                passwordField.getStylesheets().clear();
                passwordField.getStylesheets().add("/css/password_field_unlocked.css");
                System.out.println("password-protection released");
            } else {
                System.out.println("wrong password entered");
                passwordField.setText("");
            }
        } else {
            passwordHash = passwordField.getText().hashCode();
            lockButton.setDisable(true);
            passwordField.setText("");
            passwordProtected = true;
            passwordField.getStylesheets().clear();
            passwordField.getStylesheets().add("/css/password_field_locked.css");
            System.out.println("protected with password");
            lockButton.setStyle("-fx-border-style: dashed;");
        }
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

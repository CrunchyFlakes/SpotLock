package app.auth;

import app.Main;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Created by mtoepperwien on 20.06.17.
 */
public class AuthApi {

    private static String code;
    private static String client_id = "a02eaa66bb3c4e858bd7a7e6213e35c1";
    private static String client_secret = "075660fdff9a4637b2b27bc6c7d2865e";
    private static String redirect_uri = "http://localhost:8888/callback";
    private static String refreshToken;
    private static String accessToken;

    public static void logout() {
        refreshToken = null;
        accessToken = null;
        String os = Main.getOS();
        File tokenFile;
        if (os.equals("windows")) {
            tokenFile = new File(System.getenv("APPDATA") + "/SpotLock/token");
        } else if (os.equals("linux")) {
            tokenFile = new File(System.getProperty("user.home"), ".spotlock/token");
        } else {
            tokenFile = new File("not existing directory");
        }
        tokenFile.delete();
    }

    public static boolean loggedIn() {
        if (refreshToken != null) {
            return true;
        }
        return false;
    }


    public static String getAccessToken() throws IOException {
        // accessToken will be set null if expired
        if (accessToken != null) {
            return accessToken;
        }

        // request tokens
        HttpPost request = new HttpPost("https://accounts.spotify.com/api/token");
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        if (refreshToken == null) {
            params.add(new BasicNameValuePair("grant_type", "authorization_code"));
            params.add(new BasicNameValuePair("code", getAuthCode()));
            params.add(new BasicNameValuePair("redirect_uri", redirect_uri));
        } else {
            params.add(new BasicNameValuePair("grant_type", "refresh_token"));
            params.add(new BasicNameValuePair("refresh_token", refreshToken));
        }

        request.setEntity(new UrlEncodedFormEntity(params));
        request.setHeader("Authorization", "Basic " + Base64.encodeBase64String((client_id + ":" + client_secret).getBytes()));
        HttpResponse response = HttpClients.createDefault().execute(request);
        HttpEntity responseEntity = response.getEntity();

        if (responseEntity != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
            StringBuilder responseString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseString.append(line);
            }

            // read from response json
            JSONObject tokenObject = new JSONObject(responseString.toString());
            accessToken = tokenObject.getString("access_token");
            if (tokenObject.has("refresh_token")) {
                refreshToken = tokenObject.getString("refresh_token");
                try {
                    saveToken(refreshToken);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // make accessToken invalid as soon as invalid
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    accessToken = null;
                }
            });

        }
        return accessToken;
    }

    public static void setCode(String newCode) {
        code = newCode;
    }

    private static String getAuthCode() {
        final StringBuilder uriBuilder = new StringBuilder();
        uriBuilder.append("https://accounts.spotify.com/authorize?");
        uriBuilder.append("client_id=" + client_id);
        uriBuilder.append("&response_type=code");
        uriBuilder.append("&redirect_uri=" + redirect_uri);
        uriBuilder.append("&show_dialog=true");
        uriBuilder.append("&scope=playlist-read-private%20playlist-read-collaborative%20playlist-modify-public%20playlist-modify-private%20streaming%20user-follow-modify%20user-follow-read%20user-library-read%20user-library-modify%20user-read-private%20user-read-birthdate%20user-read-email%20user-top-read%20user-read-playback-state");

        // start server to catch response at redirect uri
        final AuthServer server = new AuthServer();
        try {
            server.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }


        new Thread(new Runnable() {
            public void run() {
                try {
                    if (Main.getOS().equals("linux")) {
                        Runtime runtime = Runtime.getRuntime();
                        runtime.exec("xdg-open " + uriBuilder.toString());
                    } else if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI(uriBuilder.toString()));
                    }
                } catch (Exception e) {

                }
            }
        }).start();
        new Thread(new Runnable() {
            public void run() {
                String code;
                while ((code = server.getCode()) == null) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                server.stopServer();
                setCode(code);
            }
        }).start();
        while (code == null) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return code;

    }

    private static void saveToken(String token) throws Exception {
        String os = Main.getOS();
        File tokenFile;
        if (os.equals("windows")) {
            tokenFile = new File(System.getenv("APPDATA") + "/SpotLock/token");
        } else if (os.equals("linux")) {
            tokenFile = new File(System.getProperty("user.home"), ".spotlock/token");
        } else {
            tokenFile = new File("not existing directory");
        }
        System.out.println(tokenFile.toURI());
        if (tokenFile.exists() || tokenFile.getParentFile().mkdirs()) {
            String tokenencrypted = Base64.encodeBase64String(token.getBytes());
            PrintWriter writer = new PrintWriter(tokenFile, "UTF-8");
            writer.println(tokenencrypted);
            writer.close();
        } else if (tokenFile.getParentFile().exists() && !tokenFile.exists()) {
            tokenFile.createNewFile();
        } else if (!tokenFile.canWrite()) {
            System.err.println("no writing permissions for saving token");
        }

    }

    public static void loadToken() throws Exception {
        String os = Main.getOS();
        File tokenFile;
        if (os.equals("windows")) {
            tokenFile = new File(System.getenv("APPDATA") + "/SpotLock/token");
        } else if (os.equals("linux")) {
            tokenFile = new File(System.getProperty("user.home"), ".spotlock/token");
        } else {
            tokenFile = new File("not existing file");
        }
        if (tokenFile.exists()) {
            String encrypted = new BufferedReader(new FileReader(tokenFile)).readLine();
            refreshToken = new String(Base64.decodeBase64(encrypted.getBytes()));
        }
    }
}

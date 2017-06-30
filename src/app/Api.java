package app;

import app.auth.AuthApi;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by mtoepperwien on 26.06.17.
 */
public class Api {

    public static void setVolume(int vol) throws IOException {
        HttpPut request = new HttpPut("https://api.spotify.com/v1/me/player/volume?volume_percent=" + vol);
        request.setHeader("Authorization", "Bearer " + AuthApi.getAccessToken());
        HttpClients.createDefault().execute(request);
    }

    public static JSONObject getPlaybackInfo() throws IOException {
        HttpGet playbackInfo = new HttpGet("https://api.spotify.com/v1/me/player");
        playbackInfo.setHeader("Authorization", "Bearer " + AuthApi.getAccessToken());
        BufferedReader reader = new BufferedReader(new InputStreamReader(HttpClients.createDefault().execute(playbackInfo).getEntity().getContent()));
        String line;
        StringBuilder response = new StringBuilder();
        while ( (line = reader.readLine()) != null) {
            response.append(line);
        }
        return new JSONObject(response.toString());
    }

    public static String getId() throws IOException {
        HttpGet request = new HttpGet("https://api.spotify.com/v1/me");
        request.setHeader("Authorization", "Bearer " + AuthApi.getAccessToken());
        BufferedReader reader = new BufferedReader(new InputStreamReader(HttpClients.createDefault().execute(request).getEntity().getContent()));
        String line;
        StringBuilder response = new StringBuilder();
        while ( ( line = reader.readLine() ) != null) {
            response.append(line);
        }
        return new JSONObject(response.toString()).getString("id");
    }
}

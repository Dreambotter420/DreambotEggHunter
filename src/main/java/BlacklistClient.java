
import okhttp3.*;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.utilities.Logger;
import org.json.JSONArray;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BlacklistClient implements Runnable {
    private final OkHttpClient httpClient;
    private final String BASE_URL;
    private final String BEARER_TOKEN;
    public static List<String> blacklisted = new ArrayList<>();
    public boolean stopped;
    private String name;

    public BlacklistClient(String baseUrl, String bearerToken, String name) {
        this.httpClient = new OkHttpClient();
        this.BASE_URL = baseUrl;
        this.BEARER_TOKEN = bearerToken;
        this.stopped = false;
        this.name = name;
    }
    public void addOurself() throws IOException {
        if (blacklisted.stream().anyMatch(b -> b.equals(name))) {
            Logger.log("We are on blacklist! Yay");
            return;
        }
        Logger.log("Adding ourself to replit blacklist");
        RequestBody formBody = new FormBody.Builder()
                .add("string", URLEncoder.encode(name, StandardCharsets.UTF_8.toString()))
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "/add_string/")
                .header("Authorization", "Bearer " + BEARER_TOKEN)
                .post(formBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        }
    }

    public void removeOurself() throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("string", URLEncoder.encode(name, StandardCharsets.UTF_8.toString()))
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "/remove_string/")
                .header("Authorization", "Bearer " + BEARER_TOKEN)
                .post(formBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        }
    }

    public void update() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/get_strings/")
                .header("Authorization", "Bearer " + BEARER_TOKEN)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            // print raw response
            Logger.log("RAW RESPONSE: "+response);
            // Get response body
            String body = response.body().string();
            Logger.log(body);
            JSONArray jsonArray = new JSONArray(body);
            List<String> strings = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                strings.add(jsonArray.getString(i).replace("+", " "));
            }
            blacklisted = strings;
        }

        // Add ourself if we not outcheaou

        if (!blacklisted.contains(name)) {
            addOurself();
        }
    }

    @Override
    public void run() {
        try {

            // Loop until stopped
            while (!stopped) {
                // Update the blacklist
                update();

                // Sleep for 10 seconds
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    break;
                }
            }

            // Remove ourself when stopping
            if (stopped) {
                removeOurself();
            }

        } catch (IOException e) {
            // Log the exception
            Logger.log("Error: " + e.getMessage());
        }
    }

}

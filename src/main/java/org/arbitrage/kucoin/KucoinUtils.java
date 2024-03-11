package org.arbitrage.kucoin;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KucoinUtils {
    private static final String REST_SPOT_URL = "https://api.kucoin.com";

    public static String obtainConnectionToken() {
        HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.noBody())
                                         .uri(URI.create(String.format("%s/api/v1/bullet-public", REST_SPOT_URL)))
                                         .setHeader("Content-Type", "application/json")
                                         .setHeader("Accept", "application/json").build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() != 200) return null;

            JSONObject response = new JSONObject(httpResponse.body());

            if (!response.getString("code").equals("200000")) return null;

            return response.getJSONObject("data").getString("token");
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

}

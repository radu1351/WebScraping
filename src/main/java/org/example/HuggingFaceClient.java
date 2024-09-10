package org.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class HuggingFaceClient {
    private static final String API_KEY = "";
    private static final String API_URL = "https://api-inference.huggingface.co/models/microsoft/Phi-3.5-MoE-instruct";

    public static String prompt(String prompt) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            JSONObject requestBody = new JSONObject();
            requestBody.put("inputs", prompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String responseBody =  response.body();
            if (responseBody.charAt(0) != '[') {
                System.out.println("Error: Response is not a valid JSON: " + responseBody);
                return "Error: Invalid response from Hugging Face API.";
            }

            JSONArray jsonResponse = new JSONArray(responseBody);

            return jsonResponse.getJSONObject(0).getString("generated_text");
        } catch (Exception ex) {
            return ex.toString();
        }
    }
}

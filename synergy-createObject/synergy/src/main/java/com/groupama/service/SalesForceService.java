package com.groupama.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SalesForceService {

    public static Map<String, String> getToken() throws IOException {
        String url = "https://groupama--hackathon.sandbox.my.salesforce.com";
        String clientId = "3MVG9U70RXTP1vq98iY3iYRIvUqKxLlVcPM9x0XAKdbcWEPVZ.5WhLR35WrdV6n6FbtiZ5FTo_gKL66vThpdf";
        String clientSecret = "58227DF676C9C6F28445BF33E8F00054327C967A54182D24D992C1EE50F3A900";
        String grantType = "client_credentials";
        String username = "integration.application@groupama.ro.hackathon";
        String password = "Groupama-2019";

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(
                MediaType.parse("application/x-www-form-urlencoded"),
                "grant_type=password" +
                        "&client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&username=" + username +
                        "&password=" + password
        );

        Request request = new Request.Builder()
                .url(url + "/services/oauth2/token")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        Response response = client.newCall(request).execute();

        try (ResponseBody responseBody = response.body()) {
            if (responseBody == null) throw new IOException("Empty response body");
            String json = responseBody.string();
            System.out.println("Raw JSON response: " + json);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> parsed = mapper.readValue(json, Map.class);

            if (!parsed.containsKey("access_token") || !parsed.containsKey("instance_url")) {
                throw new IOException("Login failed. Response: " + json);
            }

            String accessToken = (String) parsed.get("access_token");
            String instanceUrl = (String) parsed.get("instance_url");

            return Map.of(
                    "accessToken", accessToken,
                    "instanceUrl", instanceUrl
            );

        }
    }

    private final String accessToken;
    private final String instanceUrl;

    public SalesForceService(String accessToken, String instanceUrl) {
        this.accessToken = accessToken;
        this.instanceUrl = instanceUrl;
    }

    public String createObject(String statusObject, String sObjectId) throws Exception {
        String url = instanceUrl + "/services/data/v58.0/sobjects/GAM_FENM_IntegrationEvent__e/";
        Map<String, String> requestBody = Map.of(
                "GAM_FENM_IntegrationName__c", statusObject,
                "GAM_FENM_SObjectId__c", sObjectId
        );
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 201) {
            return "Event created: " + response.body();
        } else {
            return "Error: " + response.statusCode() + " - " + response.body();
        }
    }

    public static void main(String[] args) {
        try {
            Map<String, String> auth = getToken();
            String accessToken = auth.get("accessToken");
            String instanceUrl = auth.get("instanceUrl");

            SalesForceService service = new SalesForceService(accessToken, instanceUrl);

            String result = service.createObject("Verified", "500KO000002VOvUYAW");

            System.out.println(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


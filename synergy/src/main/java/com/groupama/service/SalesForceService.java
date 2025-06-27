package com.groupama.service;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.groupama.domain.AuthResponse;
import com.squareup.okhttp.*;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SalesForceService {

    static String url = "https://groupama--hackathon.sandbox.my.salesforce.com";
    static String clientId = "3MVG9U70RXTP1vq98iY3iYRIvUqKxLlVcPM9x0XAKdbcWEPVZ.5WhLR35WrdV6n6FbtiZ5FTo_gKL66vThpdf";
    static String clientSecret = "58227DF676C9C6F28445BF33E8F00054327C967A54182D24D992C1EE50F3A900";
    static String username = "integration.application@groupama.ro.hackathon";
    static String password = "Groupama-2019";

    public static AuthResponse doAuth() throws IOException {

        // Create an OkHttpClient instance
        OkHttpClient client = new OkHttpClient();

        // Create a request
        RequestBody body = RequestBody.
                create(MediaType.parse("application/x-www-form-urlencoded"),
                        "grant_type=password&client_id=" +
                                clientId + "&client_secret="
                                + clientSecret + "&username=" +
                                username + "&password=" + password);

        Request request = new Request.Builder()
                .url(url + "/services/oauth2/token")
                .method("POST", body)
                .addHeader("X-PrettyPrint", "1")
                .addHeader("Accept", "application/xml")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        Response response = client.newCall(request).execute();

        try (ResponseBody responseBody = response.body()) {
            XmlMapper xmlMapper = new XmlMapper();
            AuthResponse authResponse =  xmlMapper.readValue(responseBody.string(), AuthResponse.class);
            Logger.getLogger(SalesForceService.class.getName()).log(Level.INFO, "AUth token " + authResponse.getAccessToken());
            return authResponse;
        } catch (IOException e) {
            Logger.getLogger("SalesForceService").
                    log(Level.WARNING, "Error on do auth file " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static byte[] loadFile(String token, String fileId,String name) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Logger.getLogger(EntryPoint.class.getName()).log(Level.INFO, "Loading file: " + fileId);

        Request request = new Request.Builder()
                .url(url + "/services/data/v59.0/sobjects/ContentVersion/" + fileId + "/VersionData")
                .method("GET", null)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Cookie", "BrowserId=ppKAWE2rEe-F4QGRHL6PRg; CookieConsentPolicy=0:1; LSKey-c$CookieConsentPolicy=0:1")
                .build();

        Response response = client.newCall(request).execute();

        if (response.isSuccessful()) {
            byte[] data = response.body().bytes();
//            Files.write(new File(name).toPath(), data);
            Logger.getLogger(SalesForceService.class.getName()).log(Level.INFO, "Loading file  done: Size: " + data.length);
            return data;
        } else {
            Logger.getLogger("SalesForceService").
                    log(Level.WARNING, "Error loading file " + fileId + "Response " + response.body().string());
            return null;
        }
    }

    public static void main(String[] args) throws IOException, JsonMappingException {
//        String fileId = "068KO000000xAZ3YAM";
//        String token = SalesForceService.doAuth().getAccessToken();
//        byte[] file = loadFile(token,fileId);
//        Files.write(new File("test.jpg").toPath(), file);


        String mimeType = URLConnection.guessContentTypeFromName("test.jpg");
        System.out.println(mimeType);
    }
}

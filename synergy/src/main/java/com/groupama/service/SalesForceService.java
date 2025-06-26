package com.groupama.service;

import com.squareup.okhttp.*;

import java.io.IOException;

public class SalesForceService {

    public static String getToken() throws IOException {
        String url = "https://groupama--hackathon.sandbox.my.salesforce.com";
        String clientId = "3MVG9U70RXTP1vq98iY3iYRIvUqKxLlVcPM9x0XAKdbcWEPVZ.5WhLR35WrdV6n6FbtiZ5FTo_gKL66vThpdf";
        String clientSecret = "58227DF676C9C6F28445BF33E8F00054327C967A54182D24D992C1EE50F3A900";
        String grantType = "client_credentials";
        String username = "integration.application@groupama.ro.hackathon";
        String password = "Groupama-2019";

        // Create an OkHttpClient instance
        OkHttpClient client = new OkHttpClient();

        // Create a request
        RequestBody body = RequestBody.
                create(MediaType.parse("application/x-www-form-urlencoded"),
                        "grant_type=password&client_id="+
                                clientId+"&client_secret="
                                +clientSecret+"&username="+
                                username+"&password=" + password);

        Request request = new Request.Builder()
                .url( url + "/services/oauth2/token")
                .method("POST", body)
                .addHeader("X-PrettyPrint", "1")
                .addHeader("Accept", "application/xml")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

       Response response = client.newCall(request).execute() ;

       try(ResponseBody responseBody = response.body()) {
           return responseBody.string();
       }catch (IOException e) {
           e.printStackTrace();
           throw new RuntimeException(e);
       }
    }

    public static void main(String[] args) {
        try {
            System.out.println(getToken());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

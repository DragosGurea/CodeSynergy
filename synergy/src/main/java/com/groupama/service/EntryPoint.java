package com.groupama.service;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.generativeai.ResponseHandler;

import java.io.IOException;

public class EntryPoint implements HttpFunction {

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        // TODO(developer): Replace these variables before running the sample.
        String projectId = "prj-hackathon-team4";
        String location = "europe-west3";
        String modelName = "gemini-1.5-pro";

        String output =  sendPromt(projectId, location, modelName);

        response.getWriter().write(output);
    }

    // Analyzes the given video input, including its audio track.
    public static String sendPromt(String projectId, String location, String modelName)
            throws IOException {
        // Initialize client that will be used to send requests. This client only needs
        // to be created once, and can be reused for multiple requests.
        try (VertexAI vertexAI = new VertexAI(projectId, location)) {
            String videoUri = "gs://cloud-samples-data/generative-ai/video/pixel8.mp4";

            GenerativeModel model = new GenerativeModel(modelName, vertexAI);
            GenerateContentResponse response = model.generateContent(
                    ContentMaker.fromMultiModalData(
                            "Provide a description of the video.\n The description should also "
                                    + "contain anything important which people say in the video.",
                            PartMaker.fromMimeTypeAndData("video/mp4", videoUri)
                    ));

            String output = ResponseHandler.getText(response);
            System.out.println(output);

            return output;
        }
    }
}

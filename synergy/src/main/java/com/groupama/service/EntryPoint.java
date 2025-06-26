package com.groupama.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.groupama.domain.FilesRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntryPoint implements HttpFunction {

    static String projectId = "prj-hackathon-team4";
    static String location = "europe-west3";
    static String modelName = "gemini-1.5-pro";
    static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {

        if( request.getMethod().equals("POST") ) {
            FilesRequest filesRequest = objectMapper.readValue(request.getInputStream(), FilesRequest.class);
            String vertexResponse = processRequest(filesRequest);
            response.getWriter().write(vertexResponse);
        }
        else {
            response.getWriter().write("Method not supported");
        }
    }

    // Analyzes the given video input, including its audio track.
    public static String processRequest(FilesRequest filesRequest)
            throws IOException {

        Logger.getLogger(EntryPoint.class.getName()).log(Level.INFO, "Files request received ");
        String token = SalesForceService.doAuth().getAccessToken();

        for(String fileId : filesRequest.getFilesIds()) {
            byte[] fileData = SalesForceService.loadFile(token,fileId);
            Files.write(new File("test.jpg").toPath(), fileData);
        }

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

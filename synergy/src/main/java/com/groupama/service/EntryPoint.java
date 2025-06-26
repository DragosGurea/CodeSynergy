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
import com.groupama.domain.SalesForceFilesRequest;
import com.groupama.domain.SalesForceFile;

import java.io.IOException;
import java.net.URLConnection;
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
            SalesForceFilesRequest filesRequest = objectMapper.readValue(request.getInputStream(), SalesForceFilesRequest.class);
            String vertexResponse = processRequest(filesRequest);
            response.getWriter().write(vertexResponse);
        }
        else {
            response.getWriter().write("Method not supported");
        }
    }

    public static String processRequest(SalesForceFilesRequest filesRequest)
            throws IOException {

        Logger.getLogger(EntryPoint.class.getName()).log(Level.INFO, "Files request received ");
        String token = SalesForceService.doAuth().getAccessToken();

        Object[] parts = new Object[filesRequest.getFiles().size() + 1];
        parts[0] = filesRequest.getPrompt() + " "+ filesRequest.getExpectedFormat();

        for(int i = 0; i < filesRequest.getFiles().size(); i++) {
            SalesForceFile salesForceFile = filesRequest.getFiles().get(i);

            byte[] fileData = SalesForceService.loadFile(token,salesForceFile.getId());
            String mimeType = URLConnection.guessContentTypeFromName(salesForceFile.getName());

            if( fileData != null) {
                parts[i+1] =  PartMaker.fromMimeTypeAndData(mimeType, fileData);
            }
        }

        Logger.getLogger(EntryPoint.class.getName()).log(Level.INFO, "Sending data to vertex ai");

        try (VertexAI vertexAI = new VertexAI(projectId, location)) {
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);
            GenerateContentResponse response = model.generateContent(ContentMaker.fromMultiModalData(parts));
            return ResponseHandler.getText(response);
        }
        catch (Exception ex) {
            Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, ex);
            return ex.getMessage();
        }
    }
}

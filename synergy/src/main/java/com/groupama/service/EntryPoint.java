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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntryPoint implements HttpFunction {

    static String projectId = "prj-hackathon-team4";
    static String location = "europe-west3";
    static String modelName = "gemini-1.5-pro";
    static ObjectMapper objectMapper = new ObjectMapper();

    private static String VERTEX_AI_RESPONSE_JSON_START = "```json";
    private static String VERTEX_AI_RESPONSE_JSON_END = "```";

    private static List<String> FORMATS = List.of("pdf", "png", "jpg", "jpeg");

    private static final Logger logger = Logger.getLogger(EntryPoint.class.getName());

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {

        if( request.getMethod().equals("POST") ) {
            SalesForceFilesRequest filesRequest = objectMapper.readValue(request.getInputStream(), SalesForceFilesRequest.class);
            logger.log(Level.INFO, filesRequest.toString());
            String vertexResponse = processRequest(filesRequest);
            response.getWriter().write(vertexResponse);
            PubSubService.publishVertexAiResult(vertexResponse);
        }
        else {
            response.getWriter().write("Method not supported");
        }
    }

    public static String processRequest(SalesForceFilesRequest filesRequest)
            throws IOException {

        logger.log(Level.INFO, "Files request received ");
        String token = SalesForceService.doAuth().getAccessToken();

        List<Object> parts = new ArrayList<>();
        parts.add(filesRequest.getPrompt() + " "+ filesRequest.getExpectedFormat());

        for(int i = 0; i < filesRequest.getFiles().size(); i++) {
            SalesForceFile salesForceFile = filesRequest.getFiles().get(i);

            if( isFileSupported(salesForceFile.getName()) ){
                byte[] fileData = SalesForceService.loadFile(token,salesForceFile.getId(),salesForceFile.getName());

                if( fileData != null) {
                    String mimeType = URLConnection.guessContentTypeFromName(salesForceFile.getName());
                    parts.add(PartMaker.fromMimeTypeAndData(mimeType, fileData));
                }
                else{
                    logger.log(Level.WARNING, "File " + salesForceFile.getName() + " not found");
                }
            }
            else{
                logger.log(Level.WARNING, "File " + salesForceFile.getName() + " is not supported");
            }
        }

        Logger.getLogger(EntryPoint.class.getName()).log(Level.INFO, "Sending data to vertex ai");

        try (VertexAI vertexAI = new VertexAI(projectId, location)) {
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);
            GenerateContentResponse response = model.generateContent(ContentMaker.fromMultiModalData(toArray(parts)));
            String jsonText = ResponseHandler.getText(response);
            logger.info("JSON Text : " + jsonText);
            return extractJson(jsonText) ;
        }
        catch (Exception ex) {
            Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, "Error vertex call", ex);
            return ex.getMessage();
        }
    }

    private static boolean isFileSupported(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        return FORMATS.contains(extension);
    }

    private static Object[] toArray(List<Object> list) {
        Object[] array = new Object[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }

        return array;
    }

    private static String extractJson(String response){
        return response.substring(response.indexOf(VERTEX_AI_RESPONSE_JSON_START) + VERTEX_AI_RESPONSE_JSON_START.length() ,
                response.lastIndexOf(VERTEX_AI_RESPONSE_JSON_END));
    }
}

package com.groupama.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FilesRequest {

    @JsonProperty("ContentVersionIds")
    private String[] filesIds;

    @JsonProperty("Expected_JSON_format")
    private String jsonFormat;

    @JsonProperty("Prompt")
    private String prompt;

    public String[] getFilesIds() {
        return filesIds;
    }

    public String getJsonFormat() {
        return jsonFormat;
    }

    public void setFilesIds(String[] filesIds) {
        this.filesIds = filesIds;
    }

    @Override
    public String toString() {
        return "FilesRequest{" +
                "filesIds=" + Arrays.toString(filesIds) +
                ", jsonFormat='" + jsonFormat + '\'' +
                '}';
    }
}

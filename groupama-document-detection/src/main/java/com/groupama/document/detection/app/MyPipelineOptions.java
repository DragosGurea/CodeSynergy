package com.groupama.document.detection.app;

import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.StreamingOptions;

import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.StreamingOptions;

public interface MyPipelineOptions extends PipelineOptions, StreamingOptions {
    @Description("Config file path")
    String getConfigFile();
    void setConfigFile(String value);
}
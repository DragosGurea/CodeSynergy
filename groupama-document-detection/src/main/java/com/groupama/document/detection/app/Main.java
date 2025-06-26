package com.groupama.document.detection.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.bigquery.model.TableRow;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.values.TupleTagList;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        MyPipelineOptions options = PipelineOptionsFactory.fromArgs(args)
                .withValidation()
                .as(MyPipelineOptions.class);
        options.setStreaming(true);

        Config config = ConfigFactory.parseResources(options.getConfigFile());
        String subscription = config.getString("pubsub.subscription");
        String bqMainTable = config.getString("bigquery.historical_data");
        String bqErrorTable = config.getString("bigquery.error_log");
        String bqMissingTable = config.getString("bigquery.missing_data");

        Pipeline pipeline = Pipeline.create(options);

        PCollection<String> input = pipeline.apply("ReadFromPubSub", PubsubIO.readStrings().fromSubscription(subscription));

        TupleTag<TableRow> mainTag = new TupleTag<TableRow>() {};
        TupleTag<TableRow> missingTag = new TupleTag<TableRow>("missing") {};
        TupleTag<TableRow> errorTag = new TupleTag<TableRow>("error") {};

        PCollectionTuple results = input.apply("ProcessInput", ParDo
                .of(new DoFn<String, TableRow>() {
                    @ProcessElement
                    public void processElement(@Element String inputJson, MultiOutputReceiver out) {
                        ObjectMapper mapper = new ObjectMapper();
                        String processingDate = Instant.now().toString();

                        try {
                            JsonNode root = mapper.readTree(inputJson);
                            String objectId = root.path("objectId").asText(null);
                            String emailBody = root.path("email_body").asText("");

                            Object jsonObj = mapper.readValue(inputJson, Object.class);
                            String prettyPayload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObj);

                            // Extract document types
                            List<String> documentTypes = new ArrayList<>();
                            JsonNode documents = root.path("extracted_documents");
                            if (documents.isArray()) {
                                for (JsonNode doc : documents) {
                                    String type = doc.path("document_type").asText();
                                    if (type != null && !type.isEmpty()) {
                                        documentTypes.add(type);
                                    }
                                }
                            }

                            // Extract missing documents
                            List<String> missingDocs = new ArrayList<>();
                            JsonNode missingNode = root.path("missing_documents");
                            if (missingNode.isArray()) {
                                for (JsonNode item : missingNode) {
                                    missingDocs.add(item.asText());
                                }
                            }

                            if (objectId == null || objectId.isEmpty()) {
                                TableRow missingRow = new TableRow()
                                        .set("object_id", null)
                                        .set("payload", prettyPayload)
                                        .set("missingField", "objectId")
                                        .set("ERROR_TIMESTAMP", processingDate);
                                out.get(missingTag).output(missingRow);
                                return;
                            }

                            TableRow mainRow = new TableRow()
                                    .set("object_id", objectId)
                                    .set("payload", prettyPayload)
                                    .set("email_body", emailBody)
                                    .set("document_types", documentTypes)
                                    .set("missing_documents", missingDocs)
                                    .set("processing_date", processingDate);
                            out.get(mainTag).output(mainRow);

                        } catch (Exception e) {
                            String objectIdFallback = null;
                            String formattedErrorPayload;
                            try {
                                JsonNode root = mapper.readTree(inputJson);
                                objectIdFallback = root.path("objectId").asText(null);
                                Object jsonObj = mapper.readValue(inputJson, Object.class);
                                formattedErrorPayload = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObj);
                            } catch (Exception innerEx) {
                                formattedErrorPayload = inputJson;
                            }
                            TableRow errorRow = new TableRow()
                                    .set("object_id", objectIdFallback)
                                    .set("payload", formattedErrorPayload)
                                    .set("error_type", e.getMessage())
                                    .set("ERROR_TIMESTAMP", processingDate);
                            out.get(errorTag).output(errorRow);
                        }
                    }
                }).withOutputTags(mainTag, TupleTagList.of(missingTag).and(errorTag))
        );

        results.get(mainTag)
                .apply("WriteMain", BigQueryIO.writeTableRows()
                        .to(bqMainTable)
                        .withCreateDisposition(CreateDisposition.CREATE_NEVER)
                        .withWriteDisposition(WriteDisposition.WRITE_APPEND));

        results.get(missingTag)
                .apply("WriteMissing", BigQueryIO.writeTableRows()
                        .to(bqMissingTable)
                        .withCreateDisposition(CreateDisposition.CREATE_NEVER)
                        .withWriteDisposition(WriteDisposition.WRITE_APPEND));

        results.get(errorTag)
                .apply("WriteError", BigQueryIO.writeTableRows()
                        .to(bqErrorTable)
                        .withCreateDisposition(CreateDisposition.CREATE_NEVER)
                        .withWriteDisposition(WriteDisposition.WRITE_APPEND));

        pipeline.run().waitUntilFinish();
    }
}

CREATE OR REPLACE MODEL `prj-hackathon-team4.historical_data.model_missing_doc`
OPTIONS (
  model_type = 'logistic_reg',
  input_label_cols = ['missing_doc'],
  max_iterations = 50
) AS
SELECT
  ARRAY_TO_STRING(document_types, '|') AS document_combo,
  ARRAY_LENGTH(document_types) AS num_docs_present,
  ARRAY_LENGTH(missing_documents) AS num_docs_missing,
  ARRAY_LENGTH(document_types) + ARRAY_LENGTH(missing_documents) AS total_expected_docs,
  missing_doc,
  DATE(processing_date) AS created_at
FROM `prj-hackathon-team4.historical_data.DOCUMENT_PROCESSED_RAWPLUS`,
UNNEST(missing_documents) AS missing_doc
WHERE processing_date IS NOT NULL;

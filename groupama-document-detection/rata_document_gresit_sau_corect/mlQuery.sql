CREATE OR REPLACE MODEL `prj-hackathon-team4.historical_data.auto_doc_quality`
OPTIONS(
  model_type = 'logistic_reg',
  input_label_cols = ['is_good_case'],
  data_split_method = 'AUTO_SPLIT'
) AS

SELECT
  object_id,

  -- Etichetă binară: dosar complet sau nu
  IF(ARRAY_LENGTH(missing_documents) = 0, 1, 0) AS is_good_case,

  -- Număr total și lipsuri
  ARRAY_LENGTH(document_types) AS total_docs_uploaded,
  ARRAY_LENGTH(missing_documents) AS total_missing,

  -- Număr tipuri unice
  ARRAY_LENGTH(ARRAY(SELECT DISTINCT d FROM UNNEST(document_types) AS d)) AS documente_unice,

  -- Lungime string combo (proxy pt. diversitate/complexitate)
  LENGTH(ARRAY_TO_STRING(document_types, '|')) AS lungime_doc_combo,

  -- Toate combinațiile ca stringuri
  ARRAY_TO_STRING(document_types, '|') AS doc_combo,
  ARRAY_TO_STRING(missing_documents, '|') AS missing_combo,

  -- Timp procesare (opțional)
  FORMAT_TIMESTAMP('%Y-%m-%d', processing_date) AS process_day

FROM `prj-hackathon-team4.historical_data.DOCUMENT_PROCESSED_RAWPLUS`
WHERE processing_date IS NOT NULL
SELECT
  object_id,

  -- Clasa prezisă (0 / 1)
  predicted_is_good_case AS predicted_class,

  -- Probabilitate (confidența că e clasa 1 - complet)
  (
    SELECT p.prob
    FROM UNNEST(predicted_is_good_case_probs) AS p
    WHERE p.label = 1
  ) AS probability,

  -- Ground truth + features pt analiză
  is_good_case,
  doc_combo,
  missing_combo,
  total_docs_uploaded,
  total_missing,
  documente_unice,
  lungime_doc_combo,
  process_day

FROM ML.PREDICT(
  MODEL `prj-hackathon-team4.historical_data.auto_doc_quality`,
  (
    SELECT
      object_id,
      IF(ARRAY_LENGTH(missing_documents) = 0, 1, 0) AS is_good_case,
      ARRAY_LENGTH(document_types) AS total_docs_uploaded,
      ARRAY_LENGTH(missing_documents) AS total_missing,
      ARRAY_LENGTH(ARRAY(SELECT DISTINCT d FROM UNNEST(document_types) AS d)) AS documente_unice,
      LENGTH(ARRAY_TO_STRING(document_types, '|')) AS lungime_doc_combo,
      ARRAY_TO_STRING(document_types, '|') AS doc_combo,
      ARRAY_TO_STRING(missing_documents, '   si |     ') AS missing_combo,
      FORMAT_TIMESTAMP('%Y-%m-%d', processing_date) AS process_day
    FROM `prj-hackathon-team4.historical_data.DOCUMENT_PROCESSED_RAWPLUS`
    WHERE processing_date IS NOT NULL
  )
)
ORDER BY probability ASC

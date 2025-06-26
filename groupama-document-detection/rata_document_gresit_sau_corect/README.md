📘 Documentație – Automatizare Scor Calitate Dosare cu BigQuery ML
🎯 Obiectiv
Construirea unui sistem automat care evaluează calitatea unui dosar de documente (ex. dosare medicale sau de restituiri) și prezice dacă este complet sau nu, pe baza datelor existente.

⚙️ 1. Crearea modelului ML
sql
Copy
Edit
CREATE OR REPLACE MODEL `prj-hackathon-team4.historical_data.auto_doc_quality`
OPTIONS(
  model_type = 'logistic_reg',
  input_label_cols = ['is_good_case'],
  data_split_method = 'AUTO_SPLIT'
) AS

SELECT
  object_id,

  -- Label: dacă lipsesc documente => 0, altfel 1
  IF(ARRAY_LENGTH(missing_documents) = 0, 1, 0) AS is_good_case,

  -- Număr documente
  ARRAY_LENGTH(document_types) AS total_docs_uploaded,
  ARRAY_LENGTH(missing_documents) AS total_missing,

  -- Număr documente unice
  ARRAY_LENGTH(ARRAY(SELECT DISTINCT d FROM UNNEST(document_types) AS d)) AS documente_unice,

  -- Lungimea combinației de documente (proxy pt. complexitate)
  LENGTH(ARRAY_TO_STRING(document_types, '|')) AS lungime_doc_combo,

  -- Combinații efective de documente
  ARRAY_TO_STRING(document_types, '|') AS doc_combo,
  ARRAY_TO_STRING(missing_documents, '|') AS missing_combo,

  -- Ziua procesării (ca feature opțional)
  FORMAT_TIMESTAMP('%Y-%m-%d', processing_date) AS process_day

FROM `prj-hackathon-team4.historical_data.DOCUMENT_PROCESSED_RAWPLUS`
WHERE processing_date IS NOT NULL
🔍 Explicații:
Se folosește un model de regresie logistică (logistic_reg) pentru clasificare binară.

Modelul învață să distingă dosare complete de incomplete, pe baza tipurilor și cantității de documente urcate sau lipsă.

Nu sunt hardcodate tipuri de documente – funcționează generic, indiferent de context (health, restituiri, etc.).

🔮 2. Predicții automate
sql
Copy
Edit
SELECT
  object_id,

  -- Clasa prezisă: 1 = complet, 0 = incomplet
  predicted_is_good_case AS predicted_class,

  -- Probabilitatea ca dosarul să fie complet
  (
    SELECT p.prob
    FROM UNNEST(predicted_is_good_case_probs) AS p
    WHERE p.label = 1
  ) AS probability,

  -- Eticheta reală (pentru validare)
  is_good_case,

  -- Features folosite
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
      ARRAY_TO_STRING(missing_documents, '|') AS missing_combo,
      FORMAT_TIMESTAMP('%Y-%m-%d', processing_date) AS process_day
    FROM `prj-hackathon-team4.historical_data.DOCUMENT_PROCESSED_RAWPLUS`
    WHERE processing_date IS NOT NULL
  )
)
ORDER BY probability ASC
🗃️ 3. Salvarea rezultatului într-o tabelă nouă
sql
Copy
Edit
CREATE OR REPLACE TABLE `prj-hackathon-team4.historical_data.auto_doc_quality_predictions` AS
SELECT
  object_id,
  predicted_is_good_case AS predicted_class,
  (
    SELECT p.prob
    FROM UNNEST(predicted_is_good_case_probs) AS p
    WHERE p.label = 1
  ) AS probability,
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
      ARRAY_TO_STRING(missing_documents, '|') AS missing_combo,
      FORMAT_TIMESTAMP('%Y-%m-%d', processing_date) AS process_day
    FROM `prj-hackathon-team4.historical_data.DOCUMENT_PROCESSED_RAWPLUS`
    WHERE processing_date IS NOT NULL
  )
)
📌 Rezultat final
Tabelul auto_doc_quality_predictions conține pentru fiecare object_id:

Coloana	Descriere
predicted_class	Predicția modelului (0 = incomplet, 1 = complet)
probability	Scorul de încredere al modelului (0–1) pentru clasa 1 (complet)
is_good_case	Eticheta reală (din datele istorice)
doc_combo	Ce documente au fost urcate
missing_combo	Ce documente lipsesc
total_docs_uploaded, total_missing	Număr total de documente / lipsuri
documente_unice	Număr de tipuri unice de documente
lungime_doc_combo	Lungime text documente – măsură indirectă a diversității dosarului
process_day	Data procesării
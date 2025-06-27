WITH exploded_missing AS (
  SELECT
    DATE(processing_date) AS zi,
    object_id,
    missing_doc
  FROM prj-hackathon-team4.historical_data.DOCUMENT_PROCESSED_RAWPLUS,
       UNNEST(missing_documents) AS missing_doc
  WHERE processing_date IS NOT NULL
),

-- numărul de apariții pe perioadă pentru fiecare document lipsă
aparitii AS (
  SELECT missing_doc, 'azi' AS perioada, COUNT(*) AS nr
  FROM exploded_missing
  WHERE zi = CURRENT_DATE()
  GROUP BY missing_doc

  UNION ALL
  SELECT missing_doc, 'sapt', COUNT(*)
  FROM exploded_missing
  WHERE zi BETWEEN DATE_SUB(CURRENT_DATE(), INTERVAL 7 DAY) AND CURRENT_DATE()
  GROUP BY missing_doc

  UNION ALL
  SELECT missing_doc, 'luna', COUNT(*)
  FROM exploded_missing
  WHERE zi BETWEEN DATE_SUB(CURRENT_DATE(), INTERVAL 30 DAY) AND CURRENT_DATE()
  GROUP BY missing_doc

  UNION ALL
  SELECT missing_doc, 'an', COUNT(*)
  FROM exploded_missing
  WHERE zi BETWEEN DATE_SUB(CURRENT_DATE(), INTERVAL 365 DAY) AND CURRENT_DATE()
  GROUP BY missing_doc
),
aparitii_total AS (
  SELECT 'azi' AS perioada, COUNT(*) AS nr
  FROM exploded_missing
  WHERE zi = CURRENT_DATE()

  UNION ALL
  SELECT 'sapt', COUNT(*)
  FROM exploded_missing
  WHERE zi BETWEEN DATE_SUB(CURRENT_DATE(), INTERVAL 7 DAY) AND CURRENT_DATE()

  UNION ALL
  SELECT 'luna', COUNT(*)
  FROM exploded_missing
  WHERE zi BETWEEN DATE_SUB(CURRENT_DATE(), INTERVAL 30 DAY) AND CURRENT_DATE()

  UNION ALL
  SELECT 'an', COUNT(*)
  FROM exploded_missing
  WHERE zi BETWEEN DATE_SUB(CURRENT_DATE(), INTERVAL 365 DAY) AND CURRENT_DATE()
),

-- total documente per document lipsă și perioadă, bazat pe suma documentelor din acele dosare
total_pe_document AS (
  SELECT
    a.missing_doc,
    a.perioada,
    t.nr AS total
  FROM (
    SELECT DISTINCT missing_doc, perioada
    FROM aparitii
  ) a
  JOIN aparitii_total t
    ON a.perioada = t.perioada
),

-- calcul procentual
statistici AS (
  SELECT
    a.missing_doc AS missing_file,
    a.perioada,
    a.nr,
    t.total,
    100.0 * a.nr / NULLIF(t.total, 0) AS procent
  FROM aparitii a
  LEFT JOIN total_pe_document t
    ON a.missing_doc = t.missing_doc AND a.perioada = t.perioada
),

-- pivot pentru afișare
pivot AS (
  SELECT
    missing_file,
    MAX(IF(perioada = 'azi', nr, 0))      AS nr_azi,
    MAX(IF(perioada = 'azi', total, 0))   AS total_azi,
    MAX(IF(perioada = 'azi', procent, 0)) AS procent_azi,

    MAX(IF(perioada = 'sapt', nr, 0))      AS nr_sapt,
    MAX(IF(perioada = 'sapt', total, 0))   AS total_sapt,
    MAX(IF(perioada = 'sapt', procent, 0)) AS procent_sapt,

    MAX(IF(perioada = 'luna', nr, 0))      AS nr_luna,
    MAX(IF(perioada = 'luna', total, 0))   AS total_luna,
    MAX(IF(perioada = 'luna', procent, 0)) AS procent_luna,

    MAX(IF(perioada = 'an', nr, 0))        AS nr_an,
    MAX(IF(perioada = 'an', total, 0))     AS total_an,
    MAX(IF(perioada = 'an', procent, 0))   AS procent_an
  FROM statistici
  GROUP BY missing_file
)

SELECT * FROM pivot
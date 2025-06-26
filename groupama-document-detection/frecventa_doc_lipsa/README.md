📄 Documentație: Predicția și Statistica Documentelor Lipsă în Dosare
🧠 1. Scop
Această soluție are două componente principale:

Model ML (model_missing_doc) care învață relațiile dintre combinațiile de documente încărcate și cele lipsă, pentru a putea prezice ce documente lipsesc cel mai probabil într-un nou dosar.

Raport de frecvență documente lipsă, care arată topul celor mai frecvent lipsă documente pentru diferite perioade: ziua curentă, ultima săptămână, ultima lună, ultimul an.

⚙️ 2. Model ML – Logistic Regression
sql
Copy
Edit
CREATE OR REPLACE MODEL `prj-hackathon-team4.historical_data.model_missing_doc`
OPTIONS (
model_type = 'logistic_reg',
input_label_cols = ['missing_doc'],
max_iterations = 50
)
AS
SELECT
ARRAY_TO_STRING(document_types, '|') AS document_combo,
missing_doc,
DATE(processing_date) AS created_at
FROM `prj-hackathon-team4.historical_data.DOCUMENT_PROCESSED_RAWPLUS`,
UNNEST(missing_documents) AS missing_doc
WHERE ARRAY_LENGTH(missing_documents) > 0
AND processing_date IS NOT NULL;
✅ Explicații:
Model: logistic_reg (regresie logistică)

Eticheta prezisă: missing_doc (numele documentului lipsă)

Feature principal: document_combo – combinația de documente deja încărcate

Date: Istoric extrase din DOCUMENT_PROCESSED_RAWPLUS, unde cel puțin un document lipsea.

📊 3. Raport Statistic – Documente cel mai des Lipsă
🔍 Etape de procesare:
Explodarea array-ului missing_documents în linii individuale (1 rând per document lipsă)

Gruparea pe perioade: azi / săptămână / lună / an

Pivotare rezultat pentru a pune perioadele pe coloane

Calcul procentual: apariții raportate la total dosare per perioadă

Sortare descrescător pentru top documente lipsă

🔗 Query complet:
(vezi mai sus în întregime)

🧮 Output final – Coloană cu detalii
Coloana	Descriere
doc_lipsa	Numele documentului lipsă
azi_aparitii	Număr de apariții ca lipsă azi
azi_procent	Procentul raportat la toate dosarele procesate azi
sapt_aparitii	Număr de apariții în ultima săptămână
sapt_procent	Procentul pe săptămână
luna_aparitii	Număr de apariții în ultima lună
luna_procent	Procentul pe lună
an_aparitii	Număr de apariții în ultimul an
an_procent	Procentul pe an

📥 Surse date
DOCUMENT_PROCESSED_RAWPLUS: tabel cu toate dosarele procesate, fiecare conținând:

document_types – array cu documentele detectate

missing_documents – array cu documentele lipsă

processing_date – data la care dosarul a fost procesat

📌 Utilizări:
Dashboard pentru echipa de procesare: prioritizare uploaduri / campanii informative

ML Inference: pentru completare automată sau sugestii proactive

Audit: istoric lipsă pe documente, pe diferite orizonturi de timp

Optimizare workflow: reducerea timpului de procesare prin intervenție timpurie


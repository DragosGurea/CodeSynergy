package com.groupama.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SalesForceFilesRequest {

    @JsonProperty("ContentVersionFiles")
    private List<SalesForceFile> files;

    @JsonProperty("Expected_JSON_format")
    private String expectedFormat;

    @JsonProperty("Prompt")
    private String prompt;

    public List<SalesForceFile> getFiles() {
        return files;
    }

    public void setFiles(List<SalesForceFile> files) {
        this.files = files;
    }

    public String getExpectedFormat() {
        return expectedFormat;
    }

    public void setExpectedFormat(String expectedFormat) {
        this.expectedFormat = expectedFormat;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    @Override
    public String toString() {
        return "SalesForceFilesRequest{" +
                "files=" + files +
                ", expectedFormat='" + expectedFormat + '\'' +
                ", prompt='" + prompt + '\'' +
                '}';
    }

    public static void main(String[] args) throws JsonProcessingException {
        var JSON_FORMAT = """
                
                {"objectId\\": \\"500KO000002evvKYAQ\\",\\"extracted_documents\\": [{\\"salesforce_id\\" : \\"string\\",\\"document_type\\": [\\"Recomandare medicala\\", \\"investigatii medicale\\", \\"Rezultat investigatii medicale\\", \\"Fisa externare\\"],\\"data_extracted\\" : {\\"document_issuer_name\\": \\"string\\",\\"insured_name\\": \\"string\\",\\"insured_cnp\\": \\"string\\",\\"diagnostic\\": \\"string\\",\\"document_issue_date\\": \\"date(DD.MM.YYYY)\\",\\"document_issuer_signature\\": \\"boolean\\",\\"document_issuer_stamp\\": \\"boolean\\",\\"validation_results\\": {\\"insured_name_matches_salesforce\\": \\"boolean\\",\\"cnp_matches_salesforce\\": \\"boolean\\",\\"cnp_matches_form\\": \\"boolean\\",\\"iban_matches_salesforce\\": \\"boolean\\",\\"event_date_matches_salesforce\\": \\"boolean\\",\\"has_general_service_description\\": \\"boolean\\",\\"data_matches_medical_report\\": \\"boolean\\",\\"validation_errors\\": [{ \\"error_name\\": \\"string\\"}]}}},{ \\"salesforce_id\\" : \\"string\\",\\"document_type\\": [\\"Identity document\\", \\"Certificat de nastere\\"],\\"data_extracted\\" : {\\"insured_name\\": \\"string\\",\\"insured_cnp\\": \\"string\\",\\"expiration_date\\": \\"date(DD.MM.YYYY)\\",\\"validation_results\\": {\\"insured_name_matches_salesforce\\": \\"boolean\\",\\"cnp_matches_salesforce\\": \\"boolean\\",\\"cnp_matches_form\\": \\"boolean\\",\\"validation_errors\\": [{ \\"error_name\\": \\"string\\" }]}}},{\\"salesforce_id\\" : \\"string\\",\\"document_type\\": [\\"Bon\\", \\"Bon fiscal\\", \\"Chitanta\\", \\"Factura\\"],\\"data_extracted\\" : {\\"service_name\\": \\"string\\",\\"document_date\\": \\"date(DD.MM.YYYY)\\",\\"validation_results\\": {\\"has_general_service_description\\": \\"boolean\\",\\"payment_date_matches_salesforce\\": \\"boolean\\",\\"data_matches_payment_document\\": \\"boolean\\",\\"validation_errors\\": [{ \\"error_name\\": \\"string\\"}]}}}],\\"missing_documents\\": [\\"string\\"],\\"email_body\\": \\"string\\"}
                
                """;

        var USER_PROMPT = """
                Am urmatorul dosar pentru serviciul medical consult"
                Din punct de vedere al intrumentarii, acesta trebuie sa contina urmatoarele documente:
                1.Raport medical
                2.Act de identitate a persoanei care a accesat serviciul medical"\\
                3.Daca serviciul accesat este de catre un minor, trebuie sa existe un certificat de nastere"\\
                4.Daca dosarul este deschis pentru minor, trebuie sa existe un act de identitate a titularului de cont iban"\\
                5.Dovada de plata"
                
                Urmatoarele elemente trebuie identificate din documente:
                1.unitatea medicala care a eliberat raportul medical
                2.numele si prenumele asiguratului/persoanei dependente
                3.daca sunt identificate elemente ale consultatie, precum diagnostic
                4.parafa si semnatura medicului care elibereaza raportul medical
                5.data eliberarii
                6.CNP si valabilitatea din actul de identitate (CI)
                7.Daca exista documente de plata, verificam data de eliberare a documentului (bon, chitanta, factura), trebuie sa fie identica cu data de eliberare a raportului medical.
                8.Sa semnalezi daca exista denumire generala 'Servicii medical' sau 'Pachet servicii medicale'
               
                Pe baza elementelor identificate, documentele trebuie puse in categorii specifice:
                1.document medical
                2.document de plata
                3.act de identitate
                Furnizeaza datele numai in format json
                Returneaza si numele fiecarui document primit si extensia si id salesforece
                Verifica si ca datele din salesforce sunt conforme cu cele din fisiere si fisierele contin datele corecte
                Genereaza un email cu documentele lipsa / si cele care nu sunt ok din punct de vedere date
                verifica si daca este minor din cnp primit din setul de date de pe salesforce
                defineste fiecare fisier chair daca nuai primit base64
                
                """;

        SalesForceFilesRequest filesRequest = new SalesForceFilesRequest();
        SalesForceFile salesForceFile = new SalesForceFile();
        salesForceFile.setId("068KO000000xAZ3YAM");
        salesForceFile.setName("test.jpg");

        filesRequest.setFiles(List.of(salesForceFile));
        filesRequest.setPrompt(USER_PROMPT);
        filesRequest.setExpectedFormat("\"{\\\"objectId\\\": \\\"500KO000002evvKYAQ\\\",\\\"extracted_documents\\\": [{\\\"salesforce_id\\\" : \\\"string\\\",\\\"document_type\\\": [\\\"Recomandare medicala\\\", \\\"investigatii medicale\\\", \\\"Rezultat investigatii medicale\\\", \\\"Fisa externare\\\"],\\\"data_extracted\\\" : {\\\"document_issuer_name\\\": \\\"string\\\",\\\"insured_name\\\": \\\"string\\\",\\\"insured_cnp\\\": \\\"string\\\",\\\"diagnostic\\\": \\\"string\\\",\\\"document_issue_date\\\": \\\"date(DD.MM.YYYY)\\\",\\\"document_issuer_signature\\\": \\\"boolean\\\",\\\"document_issuer_stamp\\\": \\\"boolean\\\",\\\"validation_results\\\": {\\\"insured_name_matches_salesforce\\\": \\\"boolean\\\",\\\"cnp_matches_salesforce\\\": \\\"boolean\\\",\\\"cnp_matches_form\\\": \\\"boolean\\\",\\\"iban_matches_salesforce\\\": \\\"boolean\\\",\\\"event_date_matches_salesforce\\\": \\\"boolean\\\",\\\"has_general_service_description\\\": \\\"boolean\\\",\\\"data_matches_medical_report\\\": \\\"boolean\\\",\\\"validation_errors\\\": [{ \\\"error_name\\\": \\\"string\\\"}]}}},{ \\\"salesforce_id\\\" : \\\"string\\\",\\\"document_type\\\": [\\\"Identity document\\\", \\\"Certificat de nastere\\\"],\\\"data_extracted\\\" : {\\\"insured_name\\\": \\\"string\\\",\\\"insured_cnp\\\": \\\"string\\\",\\\"expiration_date\\\": \\\"date(DD.MM.YYYY)\\\",\\\"validation_results\\\": {\\\"insured_name_matches_salesforce\\\": \\\"boolean\\\",\\\"cnp_matches_salesforce\\\": \\\"boolean\\\",\\\"cnp_matches_form\\\": \\\"boolean\\\",\\\"validation_errors\\\": [{ \\\"error_name\\\": \\\"string\\\" }]}}},{\\\"salesforce_id\\\" : \\\"string\\\",\\\"document_type\\\": [\\\"Bon\\\", \\\"Bon fiscal\\\", \\\"Chitanta\\\", \\\"Factura\\\"],\\\"data_extracted\\\" : {\\\"service_name\\\": \\\"string\\\",\\\"document_date\\\": \\\"date(DD.MM.YYYY)\\\",\\\"validation_results\\\": {\\\"has_general_service_description\\\": \\\"boolean\\\",\\\"payment_date_matches_salesforce\\\": \\\"boolean\\\",\\\"data_matches_payment_document\\\": \\\"boolean\\\",\\\"validation_errors\\\": [{ \\\"error_name\\\": \\\"string\\\"}]}}}],\\\"missing_documents\\\": [\\\"string\\\"],\\\"email_body\\\": \\\"string\\\"}\"");


        System.out.println(new ObjectMapper().writeValueAsString(filesRequest));
    }
}

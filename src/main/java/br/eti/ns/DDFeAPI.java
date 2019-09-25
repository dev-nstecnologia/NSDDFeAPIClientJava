package br.eti.ns;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.Base64;
import java.util.Date;

import javax.swing.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public class DDFeAPI {

    private static String token = "THVjaWFuZSBBbG1laWRhOUFhNG4=";

    // Esta função envia um conteúdo para uma URL, em requisições do tipo POST
    private static String enviaConteudoParaAPI(Object conteudo, String url) {

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(url);
        Response respostaServidor;
        try{

            respostaServidor = target.request(MediaType.APPLICATION_JSON)
                    .header("X-AUTH-TOKEN", token)
                    .post(Entity.json(conteudo));
        }catch(Exception e){
            return Response.serverError().entity(e.getMessage()).build().toString();
        }
        return respostaServidor.readEntity(String.class);
    }

    // Faz a requisição de manifestação para API
    public static String manifestacao(String CNPJInteressado, String tpEvento, String nsu, String xJust, String chave) throws IOException {

       ObjectMapper objectMapper = new ObjectMapper();
       ManifestacaoJSON parametros = new ManifestacaoJSON();
       parametros.CNPJInteressado = CNPJInteressado;

       if (nsu.equals("")) {
            parametros.chave = chave;
       } else {
           parametros.nsu = nsu;
       }

       parametros.manifestacao.tpEvento = tpEvento;

       if (tpEvento.equals("210240")) {
           parametros.manifestacao.xJust = xJust;
       }

       objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
       String json = objectMapper.writeValueAsString(parametros);

       String url = "https://ddfe.ns.eti.br/events/manif";

       gravaLinhaLog("[MANIFESTACAO_DADOS]");
       gravaLinhaLog(json);

       String resposta = enviaConteudoParaAPI(json, url);

        gravaLinhaLog("[MANIFESTACAO_RESPOSTA]");
        gravaLinhaLog(json);

        tratamentoManifestacao(resposta, objectMapper);
        return resposta;
    }

    // Trata o retorno da manifestação da API
    private static void tratamentoManifestacao(String jsonRetorno, ObjectMapper objectMapper) throws IOException {
        String xMotivo = "";

        JsonNode respostaJSON = objectMapper.readTree(jsonRetorno);
        String status = respostaJSON.get("status").asText();

        if (status.equals("200")) {
            xMotivo = respostaJSON.get("retEvento").get("xMotivo").asText();
        } else if (status.equals("-3")) {
            xMotivo = respostaJSON.get("erro").get("xMotivo").asText();
        } else {
            xMotivo = respostaJSON.get("motivo").asText();
        }

        JOptionPane.showInputDialog(xMotivo);
    }


    // Faz a requisição de download de um unico documento 
    public static String downloadUnico(String CNPJInteressado, String caminho, String tpAmb, String nsu, String modelo,
                                       String chave, boolean incluirPdf, boolean apenasComXml, boolean comEventos) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        DownloadUnicoJSON parametros = new DownloadUnicoJSON();

        parametros.CNPJInteressado = CNPJInteressado;
        parametros.tpAmb = tpAmb;
        parametros.incluirPDF = incluirPdf;

        if (nsu.equals("") || nsu.isEmpty()) {
            parametros.chave = chave;
            parametros.apenasComXml = apenasComXml;
            parametros.comEventos = comEventos;
        } else {
            parametros.nsu = nsu;
            parametros.modelo = modelo;
        }
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(parametros);

        String url = "https://ddfe.ns.eti.br/dfe/unique";

        gravaLinhaLog("[DOWNLOAD_UNICO_DADOS]");
        gravaLinhaLog(json);

        String resposta = enviaConteudoParaAPI(json, url);

        gravaLinhaLog("[DOWNLOAD_UNICO_RESPOSTA]");
        gravaLinhaLog(resposta);

        tratamentoDownloadUnico(caminho, incluirPdf, resposta, objectMapper);

        return resposta;
    }

    //
    private static void tratamentoDownloadUnico(String caminho, boolean incluirPdf, String jsonRetorno, ObjectMapper objectMapper) throws IOException {

        JsonNode respostaJSON = objectMapper.readTree(jsonRetorno);
        String status = respostaJSON.get("status").asText();

        if (status.equals("200")) {
            downloadDocUnico(caminho, incluirPdf, jsonRetorno, objectMapper);
            JOptionPane.showMessageDialog(null, "Donwload Unico feito com sucesso!!!");
        } else {
            JOptionPane.showMessageDialog(null, respostaJSON.get("motivo").asText());
        }
    }

    //
    private static void downloadDocUnico(String caminho, boolean incluirPdf, String jsonRetorno, ObjectMapper objectMapper) throws IOException {
        String xml;
        String chave;
        String modelo;
        String pdf;
        String tpEvento;

        JsonNode respostaJSON = objectMapper.readTree(jsonRetorno);
        boolean listaDocs = respostaJSON.get("listaDocs").asBoolean();

        if (!listaDocs) {
            xml = respostaJSON.get("xml").asText();
            chave = respostaJSON.get("chave").asText();
            modelo = respostaJSON.get("modelo").asText();
            salvarXML(xml, caminho, chave, modelo, "");
            if (incluirPdf) {
                pdf = respostaJSON.get("pdf").asText();
                salvarPDF(pdf, caminho, chave, modelo, "");
            }
        } else {
            JsonNode arrayDocs = respostaJSON.get("xmls");
            if (arrayDocs.isArray()) {
                for (final JsonNode itemDoc : arrayDocs) {
                    xml = itemDoc.get("xml").asText();

                    if (!xml.equals("")) {
                        chave = itemDoc.get("chave").asText();
                        modelo = itemDoc.get("modelo").asText();

                        if (itemDoc.hasNonNull("tpEvento")){
                            tpEvento = itemDoc.get("tpEvento").asText();
                        } else {
                            tpEvento = "";
                        }

                        salvarXML(xml, caminho, chave, modelo, tpEvento);

                        if (incluirPdf) {
                            pdf = itemDoc.get("pdf").asText();
                            salvarPDF(pdf, caminho, chave, modelo, tpEvento);
                        }
                    }
                }
            }
        }
    }


    //Faz a requisição de download de um lote de documentos
    public static String downloadLote(String CNPJInteressado, String caminho, String tpAmb, String ultNSU, String modelo,
                                      boolean apenasPendManif, boolean incluirPdf, boolean apenasComXml, boolean comEventos) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        DownloadLoteJSON parametros = new DownloadLoteJSON();

        parametros.CNPJInteressado = CNPJInteressado;
        parametros.ultNSU = ultNSU;
        parametros.modelo = modelo;
        parametros.tpAmb = tpAmb;
        parametros.incluirPDF = incluirPdf;

        if (!apenasPendManif) {
            parametros.apenasComXml = apenasComXml;
            parametros.comEventos = comEventos;
        } else {
            parametros.apenasPendManif = true;
        }

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(parametros);

        String url = "https://ddfe.ns.eti.br/dfe/bunch";

        gravaLinhaLog("[DOWNLOAD_LOTE_DADOS]");
        gravaLinhaLog(json);

        String resposta = enviaConteudoParaAPI(json, url);

        gravaLinhaLog("[DOWNLOAD_LOTE_RESPOSTA]");
        gravaLinhaLog(resposta);

        tratamentoDownloadLote(caminho, incluirPdf, resposta, objectMapper);

        return resposta;

    }

    // Trata o retorno da API apos uma requisição de download em lote de DFes
    private static void tratamentoDownloadLote(String caminho, boolean incluirPdf, String jsonRetorno, ObjectMapper objectMapper) throws IOException {

        JsonNode respostaJSON = objectMapper.readTree(jsonRetorno);
        String status = respostaJSON.get("status").asText();

        if (status.equals("200")) {
            JOptionPane.showInputDialog(downloadDocsLote(caminho, incluirPdf, jsonRetorno, objectMapper));
        } else {
            JOptionPane.showInputDialog(respostaJSON.get("motivo").asText());
        }
    }

    //Faz o download local dos xmls e/ou pdfs dos documentos requisitados
    private static String downloadDocsLote(String caminho, boolean incluirPdf, String jsonRetorno, ObjectMapper objectMapper) throws IOException {
        String xml;
        String chave;
        String modelo;
        String pdf;
        String tpEvento;

        JsonNode respostaJSON = objectMapper.readTree(jsonRetorno);


        JsonNode arrayDocs = respostaJSON.get("xmls");

            for (final JsonNode itemDoc : arrayDocs) {
                xml = itemDoc.get("xml").asText();

                if (!xml.equals("")) {
                    chave = itemDoc.get("chave").asText();
                    modelo = itemDoc.get("modelo").asText();

                    if (itemDoc.hasNonNull("tpEvento")){
                        tpEvento = itemDoc.get("tpEvento").asText();
                    } else {
                        tpEvento = "";
                    }

                    salvarXML(xml, caminho, chave, modelo, tpEvento);

                    if (incluirPdf) {
                        pdf = itemDoc.get("pdf").asText();
                        salvarPDF(pdf, caminho, chave, modelo, tpEvento);
                    }
                }
            }
        return respostaJSON.get("ultNSU").asText();
    }


    // Esta função salva um XML
    private static void salvarXML(String xml, String caminho, String chave, String modelo, String tpEvento) throws IOException{

        String extensao;
        if (modelo.equals("55")) {
            extensao = "-procNFe.xml";
        } else if (modelo.equals("57")) {
            extensao = "-procCTe.xml";
        } else {
            extensao = "-procNFSeSP.xml";
        }

        String path = getDireitorioSO(caminho,"xmls");

        File localSalvar = new File(path);
        if (!localSalvar.exists()) {
            localSalvar.mkdirs();
        }

        String localParaSalvar = path + tpEvento + chave  + extensao;
        String conteudoReplace = xml.replace("\\","");
        File arq = new File(localParaSalvar);

        if(arq.exists()){
            arq.delete();
        }

        FileWriter fileEdit = new FileWriter(arq);
        try (BufferedWriter bfw = new BufferedWriter(fileEdit)) {
            bfw.write(conteudoReplace);
            bfw.flush();
        }
    }

    // Esta função salva um PDF
    private static void salvarPDF(String pdf, String caminho, String chave, String modelo, String tpEvento) throws FileNotFoundException, IOException{

        String extensao;
        if (modelo.equals("55")) {
            extensao = "-procNFe.pdf";
        } else if (modelo.equals("57")) {
            extensao = "-procCTe.pdf";
        } else {
            extensao = "-procNFSeSP.pdf";
        }

        String path = getDireitorioSO(caminho, "pdfs");
        File localSalvar = new File(path);
        if (!localSalvar.exists()) {
            localSalvar.mkdirs();
        }

        String localParaSalvar = path + tpEvento + chave  + extensao;
        File arq = new File(localParaSalvar);
        if(arq.exists()){
            arq.delete();
        }
        try (FileOutputStream fop = new FileOutputStream(arq)) {
            fop.write(Base64.getDecoder().decode(pdf));
            fop.flush();
        }
    }

    // Esta função grava uma linha de texto em um arquivo de log
    private static void gravaLinhaLog(String conteudoSalvar) throws IOException {

        //Lendo Path do computador utlilizado e criando a pasta log
        String path = System.getProperty("user.dir");
        path = getDireitorioSO(path, "log");

        File localSalvar = new File(path);
        if (!localSalvar.exists()) {
            localSalvar.mkdirs();
        }
        //Data atual ddmmyy
        Date data = new Date();
        SimpleDateFormat formatador = new SimpleDateFormat("yyyyMMdd");
        String dataAtual = formatador.format(data);

        //Cria .txt com a data atual
        File localParaSalvar = new File(path+ dataAtual + ".txt");
        FileWriter txt = new FileWriter(localParaSalvar, true);
        BufferedWriter gravarArq = new BufferedWriter(txt);

        //Data e hora atuais
        dataAtual = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(data);

        //grava as informações dentro do .txt
        gravarArq.write( dataAtual + " - "+conteudoSalvar);
        gravarArq.newLine();
        gravarArq.close();
        txt.close();
    }

    //Verifica qual S.O. o sistema esta para criar os arquivos correspondentes
    private static String getDireitorioSO (String caminho, String dir){

        String so = String.valueOf(System.getProperty("os.name"));
        String path;
        if (so.contains("Windows")){
            if (!caminho.endsWith("\\")){
                caminho += "\\";
            }
             path = caminho + dir + "\\";
        } else {
            if (!caminho.endsWith("/")){
                caminho += "/";
            }
             path = caminho + dir + "/";
        }

        return path;
    }



    //Inner Classes

    //Manifestação
    public static class ManifestacaoJSON{
        public String CNPJInteressado = null;
        public String nsu = null;
        public String chave = null;
        public Manifestacao manifestacao;
    }
    public static class Manifestacao{
        public String tpEvento = null;
        public String xJust = null;
    }

    //Download Unico
    public static class DownloadUnicoJSON{
        public String CNPJInteressado = null;
        public String nsu = null;
        public String chave = null;
        public String modelo = null;
        public String tpAmb = null;
        public boolean apenasComXml = false;
        public boolean incluirPDF = false;
        public boolean comEventos = false;

    }

    //Download Lote
    public static class DownloadLoteJSON {
        public String CNPJInteressado = null;
        public String ultNSU = null;
        public String modelo = null;
        public String tpAmb = null;
        public boolean apenasPendManif = false;
        public boolean apenasComXml = false;
        public boolean incluirPDF = false;
        public boolean comEventos = false;
    }
}

package br.eti.ns;

import javax.swing.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        /*
            Aqui voce pode testar a chamada de metodos para manifestar,
            fazer o download de um unico documento ou/e fazer download de
            varios documentos emitidos contra co CNPJ do cliente

            - Aqui um exemplo de chamada de download de um unico documento atraves da chave
              (pode ser feito tanto pela chave do documento ou pelo NSU do mesmo):

                * String resposta = DDFeAPI.downloadUnico(CNPJInteressado, caminho, tpAmb, nsu, modelo, chave,
                                    incluirPdf, apenasComXml, comEventos);


            - Aqui um exemplo de chamada de download de lote de documentos
              (pode ser feito tanto pelo ultimo NSU ou pela data inicial e final):

                * String resposta = DDFeAPI.downloadLote(CNPJInteressado, caminho, tpAmb, ultNSU, dhInicial, dhFInalmodelo,
                                      apenasPendManif, incluirPdf, apenasComXml, comEventos);

            Para maiores informações, consulte a documentação no link: https://confluence.ns.eti.br/display/PUB/JAVA+-+DDF-e+API, e entre em contato com a equipe de integração

        */
    }
}


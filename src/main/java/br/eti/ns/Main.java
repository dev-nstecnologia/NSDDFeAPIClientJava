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

                * String resposta = DDFeAPI.downloadUnico("29988990000108", "/home/mazzoni/Notas", "1", "", "55", "43190800412417000161550010001139801717006581",
                                    false, true, false);


            - Aqui um exemplo de chamada de download de lote de documentos
              (somente pode ser feito pelo ultimo NSU):

                * String resposta = DDFeAPI.downloadLote(CNPJInteressado, caminho, tpAmb, ultNSU, modelo,
                                      apenasPendManif, incluirPdf, apenasComXml, comEventos);

            Para maiores informações, consulte a documentação no link: https://confluence.ns.eti.br/display/PUB/PHP+-+DDF-e+API, e entre em contato com a equipe de integração

        */


    }
}


package ObjectServer;

import Cache.Cache;
import Log.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.net.InetAddress;

/**
 * @author Miguel Cidade Silva
 * Classe que faz o parsing de um ficheiro de configuração de servidores
 * DNSPacket.Data de criação 23/10/2022
 * DNSPacket.Data de edição 22/11/2022
 */

public class ObjectServer {

    private String dominio;
    private Map<String,List<InetSocketAddress>> DD;
    private List<InetSocketAddress> ST;
    private  Map<String,String> logs;
    private Cache cache;

    /**
     * Construtor de objetos da classe ObjectServer.ObjectServer
     */
    public ObjectServer() {
        this.dominio = null;
        this.DD = new HashMap<>();
        this.ST = new ArrayList<>();
        this.cache = new Cache();
        this.logs = new HashMap<>();
    }

    /**
     * Método que adiciona elementos ao campo DD de um servidor
     * @param e endereço a adicionar
     */
    public void addEnderecoDD(String domain, InetSocketAddress e) {
        if (DD.containsKey(domain)) DD.get(domain).add(e);
        else {
            ArrayList<InetSocketAddress> aux = new ArrayList<>();
            aux.add(e);
            DD.put(domain,aux);
        }
    }

    /**
     * Método que faz parsing dos ficheiros ST
     * @param filename localização do ficheiro ST
     * @throws IOException exceção lançada devido a erros de input/output
     */
    public void FicheiroST(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        for (String str : lines) {
            if (str.length() > 0 && str.charAt(0) != '#') {
                String []STEnderecoPorta = str.split(":");
                if(STEnderecoPorta.length>1){
                    ST.add(new InetSocketAddress(InetAddress.getByName(STEnderecoPorta[0]), Integer.parseInt(STEnderecoPorta[1])));
                }
                else ST.add(new InetSocketAddress(InetAddress.getByName(str),5353));
            }
        }
    }

    /**
     * Getter do dominio de um objeto da classe ObjectServer.ObjectServer
     * @return O dominio do ObjectServer.ObjectServer
     */
    public String getDominio() {
        return dominio;
    }

    /**
     * Getter do campo DD de um objeto da classe ObjectServer.ObjectServer
     * @return O campo DD do ObjectServer.ObjectServer
     */
    public Map<String,List<InetSocketAddress>> getDD() {
        return DD;
    }

    /**
     * Getter do campo ST de um objeto da classe ObjectServer.ObjectServer
     * @return O campo ST do ObjectServer.ObjectServer
     */
    public List<InetSocketAddress> getST() {
        return ST;
    }

    /**
     * Getter do campo LOGS de um objeto da classe ObjectServer.ObjectServer
     * @return O campo LOGS do ObjectServer.ObjectServer
     */
    public Map<String, String> getLogs() {
        return logs;
    }

    /**
     * Getter do campo cache de um objeto da classe ObjectServer.ObjectServer
     * @return O campo cache do ObjectServer.ObjectServer
     */
    public Cache getCache() {
        return cache;
    }

    /**
     * Setter do campo Dominio de um objeto da classe ObjectServer.ObjectServer
     * @param dominio o domínio que queremos atribuir
     */
    public void setDominio(String dominio) {
        this.dominio = dominio;
    }

    /**
     * Setter do campo DD de um objeto da classe ObjectServer.ObjectServer
     * @param DD a lista de endereços que queremos atribuir ao campo DD
     */
    public void setDD(Map<String,List<InetSocketAddress>> DD) {
        this.DD = DD;
    }

    /**
     * Setter do campo ST de um objeto da classe ObjectServer.ObjectServer
     * @param ST a lista de endereços que queremo stribuir ao campo ST
     */
    public void setST(List<InetSocketAddress> ST) {
        this.ST = ST;
    }

    /**
     * Setter do campo LOGS de um objeto da classe ObjectServer.ObjectServer
     * @param logs o mapa que queremos associar ao valor co campo LOGS de um objeto da classe ObjectServer.ObjectServer
     */
    public void setLogs(Map<String, String> logs) {
        this.logs = logs;
    }

    /**
     * Setter do campo cache de um objeto da classe ObjectServer.ObjectServer
     * @param cache - a cache que pretender atribuir a um objeto da classe ObjectServer.ObjectServer
     */
    public void setCache(Cache cache) {
        this.cache = cache;
    }

    /**
     * Método auxiliar ao parsing que verifica se o processo ocorre como pretendido, ou seja, se os campos do servidor ficam preenchidos após o processo
     * @return true se o processo ocorre como esperado, false caso não ocorra como esperado
     */
    private boolean verificaConfig(String domainName) {
        boolean aux;
        if (ST.isEmpty() && DD.isEmpty()){ //servidor de topo
            aux = (!this.logs.isEmpty() && logs.containsKey("all"));
            return aux;
        }
        //outros servidores (campos comuns a todos os servidores exceto ST)
        else aux = !this.DD.isEmpty() &&
                !this.ST.isEmpty() &&
                (!this.logs.isEmpty() && logs.containsKey("all"));
        boolean aux2;
        if(this instanceof ObjectSP){ //caso seja um SP ou um ST (dominio passado como parâmetro pois podemos ter SP no dominio reverse)
            aux2 = ((ObjectSP) this).verificaSP(domainName);
            return aux && aux2;
        }
        if(this instanceof ObjectSS){ //caso seja um SS
            aux2 = ((ObjectSS) this).verificaSS();
            return aux && aux2;
        }
        else return aux; //Caso seja SR
    }

    /**
     * Método auxiliar que permite a escrita num ficheiro de log de uma lista de warnings gerados no processo de parsing dos ficheiros de configuração de um servidor
     * @param warnings lista de warnings a escrever como linhas nos logs do servidor configurado
     * @param ficheiroLog ficheiro de log no qual iremos escrever
     * @throws IOException exceção para caso o ficheiro de configuração não exista
     */
    public static void writeInLogs(List<String> warnings,String ficheiroLog) throws IOException {
        List<String> writeLogs = new ArrayList<>();
        for (String warning : warnings) {
            Log log = new Log(Date.from(Instant.now()), Log.EntryType.FL, "127.0.0.1", warning);
            writeLogs.add(log.toString());
        }
            LogFileWriter.writeInLogFile(ficheiroLog, writeLogs);
    }

    /**
     * Método auxiliar que permite a escrita de uma linha, neste caso de um warning, para um ficheiro de log de um servidor gerado no processo de parsing dos ficheiros de configuração
     * @param warning warning a escrever nos ficheiro de logs
     * @param ficheiroLog log no qual iremos escrever
     * @throws IOException exceção para caso o ficheiro de configuração não exista
     */
    private static void writeLineinLogs(String warning,String ficheiroLog) throws IOException {
        Log log = new Log(Date.from(Instant.now()), Log.EntryType.FL, "127.0.0.1",warning);
        LogFileWriter.writeLineInLogFile(ficheiroLog,log.toString());
    }

    /**
     * Método auxiliar que escreve no terminal a lista de warnings do processo de parsing dos ficheiros de configuração de um servidor
     * @param filename caminho para o ficheiro de configuração
     * @param warnings lista de warnings a escrever como linhas no terminal
     */
    public static void writeInTerminal(String filename, List<String> warnings) {
        System.out.println("Warnings no processo de parsing do ficheiro de configuração'" + filename + "':");
        for (String warning : warnings) {
            System.out.println("- " + warning);
        }
    }

    /**
     * Método que escreve a resposta a uma query nos logs
     * @param domain dominio sobre o qual é feito a resposta
     * @param IP IP a apresentar na resposta
     * @param answer String da linha não formatada, a formatar e colocar na resposta.
     * @throws IOException - exceção para caso o ficheiro de logs não exista
     */
    public void writeAnswerInLog(String domain, String IP, String answer) throws IOException {
        Log formatedAnswer = new Log(Date.from(Instant.now()), Log.EntryType.QR,IP,answer);
        LogFileWriter.writeLineInLogFile(this.logs.get(domain),formatedAnswer.toString());
    }

    /**
     * Método auxiliar que ajuda na validação após o processo de parsing
     * @param filename caminho para o ficheiro de configuração
     * @param logcounter contador do numero de logs de topo
     * @param domainName dominio do log onde queremos escrever
     * @param warnings lista de warnings a escrever como entradas nos ficheiros de logs e como linhas no terminal
     * @return true caso o servidor esteja bem formulado, false caso contrário
     * @throws IOException exceção para caso o ficheiro de configuração não exista
     */
    public boolean postParsing(String filename,int logcounter, String domainName, List<String> warnings) throws IOException {
        boolean res = false;
        if (logcounter == 0) {
            res = true;
            warnings.add("Não existe log de topo no ficheiro de configuracão " + filename + " não configurado.");
        }
        writeInLogs(warnings,this.logs.get(domainName));
        if (this instanceof ObjectSP auxserver){
            auxserver.getCache().createBD(auxserver.getBD(), this.dominio,this.logs.get(domainName));
        }
        if (!this.verificaConfig(domainName)) {
            res = true;
            writeLineinLogs("Campos em falta. Servidor com o ficheiro de configuração " + filename + " não configurado.",this.logs.get(this.dominio));
        }
        return res;
    }

    /**
     * Método que realiza o parsing de um ficheiro de configuração de um servidor DNS
     * @param filename localização do ficheiro de configuração
     * @return O servidor configurado
     * @throws IOException exceção lançada caso haja erros de input/output
     */
    public static ObjectServer parseServer(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        ObjectServer server = null;
        ObjectSP sp = null;
        ObjectSS ss = null;
        int logcounter = 0;
        String logDomain = null;
        List<String> warnings = new ArrayList<>();
        for(String line : lines) {
            if (line.length() > 0 && line.charAt(0) != '#') {
                String[] words = line.split(" ");
                if (words.length > 2) {
                    switch (words[1]) {
                        case "SS":
                            if (sp == null) {
                                sp = new ObjectSP();
                                server = sp;
                                server.dominio = words[0];
                            }
                            String[] enderecoPortaSS = words[2].split(":");
                            if(enderecoPortaSS.length>1){
                                sp.addSS(new InetSocketAddress(InetAddress.getByName(enderecoPortaSS[0]), Integer.parseInt(enderecoPortaSS[1])));
                            }
                            else sp.addSS(new InetSocketAddress(InetAddress.getByName(words[2]),5353));
                            break;
                        case "DB":
                            if (sp == null) {
                                sp = new ObjectSP();
                                server = sp;
                                server.dominio = words[0];
                            }
                            if(sp.getBD().equals("")){
                                sp.setBD(words[2]);
                            }
                            else{
                                warnings.add("Linha "  + line + " ignorada, pois levaria  a termos mais do que uma base de dados no ficheiro de configuração de um servidor.");
                            }
                            break;
                        case "SP":
                            if (ss == null){
                                ss = new ObjectSS();
                                server = ss;
                                server.dominio = words[0];
                                String[] enderecoPortaSP = words[2].split(":");
                                if(enderecoPortaSP.length>1){
                                    ss.addSP(new InetSocketAddress(InetAddress.getByName(enderecoPortaSP[0]), Integer.parseInt(enderecoPortaSP[1])));
                                }
                                else ss.addSP(new InetSocketAddress(InetAddress.getByName(words[2]),5353));
                            }
                            else warnings.add("Linha "  + line + " ignorada, pois levaria  a termos mais do que um SP no ficheiro de configuração de um SS."); // apenas adiciona o primeiro
                            break;
                        case "DD":
                            if(server==null){
                                server = new ObjectServer();
                                server.dominio = words[0];
                            }
                            String[] enderecoPortaDD = words[2].split(":");
                            if(enderecoPortaDD.length>1){
                                server.addEnderecoDD(words[0],new InetSocketAddress(InetAddress.getByName(enderecoPortaDD[0]), Integer.parseInt(enderecoPortaDD[1])));
                            }
                            else server.addEnderecoDD(words[0],new InetSocketAddress(InetAddress.getByName(words[2]),5353));
                            break;
                        case "ST":
                            if(server==null){
                                server = new ObjectServer();
                            }
                            if (words[0].equals("root")) server.FicheiroST(words[2]);
                            break;
                        case "LG":
                            if(server==null){
                                server = new ObjectServer();
                            }
                            if (words[0].equals("all")) {
                                server.logs.put("all",words[2]);
                                logcounter++;
                            }
                            else {
                                if (server.dominio.equals("")) server.dominio = words[0];
                                logDomain = words[0];
                                if (server.dominio.matches("(.*)"+words[0])) {
                                    server.logs.put(words[0],words[2]);
                                }
                            }
                            break;
                    }
                } else warnings.add("Linha " + line + " com informação incompleta para o campo " + words[1]);
            }
        }
        if(server!=null) {
            boolean makeNullServer = server.postParsing(filename,logcounter,logDomain,warnings);
            if (makeNullServer)  {
                server = null;
            }
        }
        writeInTerminal(filename,warnings);
        return server;
    }

    /**
     * Método toString da classe ObjectServer.ObjectServer
     * @return String representativa da classe ObjectServer.ObjectServer
     */
    @Override
    public String toString() {
        return "ObjectServer.ObjectServer:" + "\n" +
                "   Dominio = " + dominio +" \n" +
                "   DD = " + DD + "\n" +
                "   ST = " + ST + "\n" +
                "   LOGS = " + logs +"\n"+
                "   CACHE = " + cache;
    }
}
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.net.InetAddress;

/**
 * @author Miguel Cidade Silva
 * Classe que faz o parsing de um ficheiro de configuração de servidores
 * Data de criação 23/10/2022
 * Data de edição 14/11/2022
 */

public class ObjectServer {

    private String dominio;
    private List<InetSocketAddress> DD;
    private List<InetSocketAddress> ST;
    private List<String> LG;

    private List<String> allLG;

    private  Map<String,String> logs;
    private Cache cache;

    /**
     * Construtor de objetos da classe ObjectServer
     */
    public ObjectServer() {
        this.dominio = null;
        this.DD = new ArrayList<>();
        this.ST = new ArrayList<>();
        this.cache = new Cache();
        this.logs = new HashMap<>();
    }

    /**
     * Método que adiciona elementos ao campo DD de um servidor
     * @param e endereço a adicionar
     */
    public void addEnderecoDD(InetSocketAddress e) {
        DD.add(e);
    }

    /**
     * Método que adiciona elementos ao campo LG de um servidor
     * @param path localização do ficheiro de log
     */
    public void addLog(String path){
        LG.add(path);
    }

    /**
     * Método que adiciona elementos ao campo allLG de um servidor, ou seja, acrescenta logs para toda a atividade
     * @param path localização do ficheiro de log
     */
    public void addAllLog(String path){
        allLG.add(path);
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
     * Getter do dominio de um objeto da classe ObjectServer
     * @return O dominio do ObjectServer
     */
    public String getDominio() {
        return dominio;
    }

    /**
     * Getter do campo DD de um objeto da classe ObjectServer
     * @return O campo DD do ObjectServer
     */
    public List<InetSocketAddress> getDD() {
        return DD;
    }

    /**
     * Getter do campo ST de um objeto da classe ObjectServer
     * @return O campo ST do ObjectServer
     */
    public List<InetSocketAddress> getST() {
        return ST;
    }

    /**
     * Getter do campo LOGS de um objeto da classe ObjectServer
     * @return O campo LOGS do ObjectServer
     */
    public Map<String, String> getLogs() {
        return logs;
    }

    /**
     * Getter do campo cache de um objeto da classe ObjectServer
     * @return O campo cache do ObjectServer
     */
    public Cache getCache() {
        return cache;
    }


    /**
     * Setter do campo Dominio de um objeto da classe ObjectServer
     * @param dominio o domínio que queremos atribuir
     */
    public void setDominio(String dominio) {
        this.dominio = dominio;
    }

    /**
     * Setter do campo DD de um objeto da classe ObjectServer
     * @param DD a lista de endereços que queremos atribuir ao campo DD
     */
    public void setDD(List<InetSocketAddress> DD) {
        this.DD = DD;
    }

    /**
     * Setter do campo ST de um objeto da classe ObjectServer
     * @param ST a lista de endereços que queremo stribuir ao campo ST
     */
    public void setST(List<InetSocketAddress> ST) {
        this.ST = ST;
    }

    /**
     * Setter do campo LOGS de um objeto da classe ObjectServer
     * @param logs o mapa que queremos associar ao valor co campo LOGS de um objeto da classe ObjectServer
     */
    public void setLogs(Map<String, String> logs) {
        this.logs = logs;
    }

    /**
     * Setter do campo cache de um objeto da classe ObjectServer
     * @param cache - a cache que pretender atribuir a um objeto da classe ObjectServer
     */
    public void setCache(Cache cache) {
        this.cache = cache;
    }

    /**
     * Método auxiliar ao parsing que verifica se o processo ocorre como pretendido, ou seja, se os campos do servidor ficam preenchidos após o processo
     * @return true se o processo ocorre como esperado, false caso não ocorra como esperado
     */
    private boolean verificaConfig() {
        boolean aux;
        if (ST.isEmpty() && DD.isEmpty()){ //servidor de topo
            aux = (!this.logs.isEmpty() && logs.containsKey("all"));
            return aux && this.getCache().checkBD("ST");
        }
        //outros servidores (campos comuns a todos os servidores exceto ST)
        else aux = !this.DD.isEmpty() &&
                   !this.ST.isEmpty() &&
                (!this.logs.isEmpty() && logs.containsKey("all"));
        boolean aux2;
        if(this instanceof ObjectSP){ //caso seja um SP ou um ST (dominio passado como parâmetro pois podemos ter SP no dominio reverse)
            aux2 = ((ObjectSP) this).verificaSP(this.dominio);
            return aux && aux2;
        }
        if(this instanceof ObjectSS){ //caso seja um SS
            aux2 = ((ObjectSS) this).verificaSS();
            return aux && aux2;
        }
        else return aux; //Caso seja SR
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
        List<String> warnings = new ArrayList<>();
        int logcounter = 0;
        for(String line : lines){
            if (line.length() > 0 && line.charAt(0) != '#') {
                String[] words = line.split(" ");
                if(words.length>2){
                    switch(words[1]){
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
                                sp.getCache().createBD(words[2], server.dominio);
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
                                server.addEnderecoDD(new InetSocketAddress(InetAddress.getByName(enderecoPortaDD[0]), Integer.parseInt(enderecoPortaDD[1])));
                            }
                            else server.addEnderecoDD(new InetSocketAddress(InetAddress.getByName(words[2]),5353));
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
                            if (!words[0].equals("all")) {
                                if (server.dominio.equals("")) server.dominio = words[0];
                                if (server.dominio.matches("(.*)"+words[0])) {
                                    server.logs.put(words[0],words[2]);
                                }
                            }
                            break;

                    }
                }
                else warnings.add("Linha "  + line + " com informação incompleta para o campo" + words[1]);
            }
        }
        if (logcounter == 0) server = null;
        if(server != null && !server.verificaConfig())
        {
            server = null;
            warnings.add("Campos em falta. Servidor não configurado.");
        }
        System.out.println("Warnings no processo de parsing do ficheiro de configuração'" + filename + "':");
        for(String warning : warnings)
        {
            System.out.println("- " + warning);
        }
        return server;
    }

    /**
     * Método toString da classe ObjectServer
     * @return String representativa da classe ObjectServer
     */
    @Override
    public String toString() {
        return "ObjectServer:" + "\n" +
                "   Dominio = " + dominio +" \n" +
                "   DD = " + DD + "\n" +
                "   ST = " + ST + "\n" +
                "   LOGS = " + logs +"\n"+
                "   CACHE = " + cache;
    }


}


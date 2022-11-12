import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.net.InetAddress;
import java.util.Objects;

/**
 * @author Miguel Cidade Silva
 * Classe que faz o parsing de um ficheiro de configuração de servidores
 * Data de criação 23/10/2022
 * Data de edição 11/11/2022
 */

public class ObjectServer {

    private String dominio;
    private final List<InetSocketAddress> DD;
    private final List<InetSocketAddress> ST;
    private final List<String> LG;

    private final List <String> allLG;

    private Cache cache;

    /**
     * Construtor de objetos da classe ServidorConfiguracao
     */
    public ObjectServer() {
        this.dominio = null;
        this.DD = new ArrayList<>();
        this.ST = new ArrayList<>();
        this.LG = new ArrayList<>();
        this.allLG = new ArrayList<>();
        this.cache = new Cache();
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
                else ST.add(new InetSocketAddress(InetAddress.getByName(str),53));
            }
        }
    }

    /**
     * Getter do dominio de um objeto da classe ServidorConfiguracao
     * @return O dominio do ServidorConfiguracao
     */
    public String getDominio() {
        return dominio;
    }

    /**
     * Getter do campo DD de um objeto da classe ServidorConfiguracao
     * @return O campo DD do ServidorConfiguracao
     */
    public List<InetSocketAddress> getDD() {
        return DD;
    }

    /**
     * Getter do campo ST de um objeto da classe ServidorConfiguracao
     * @return O campo ST do ServidorConfiguracao
     */
    public List<InetSocketAddress> getST() {
        return ST;
    }

    /**
     * Getter do campo LG de um objeto da classe ServidorConfiguracao
     * @return O campo LG do ServidorConfiguracao
     */
    public List<String> getLG() {
        return LG;
    }

    /**
     * Geter do campo allLG de um objeto da classe ServidorConfiguracao
     * @return O campo allLG do ServidorConfiguracao
     */
    public List<String> getAllLG() {
        return allLG;
    }

    /**
     * Getter do campo cache de um objeto da classe ServidorConfiguracao
     * @return O campo cache do ServidorConfiguracao
     */
    public Cache getCache() {
        return cache;
    }


    public void setCache(Cache cache){
        this.cache = cache;
    }

    /**
     * Método auxiliar ao parsing que verifica se o processo ocorre como pretendido, ou seja, se os campos do servidor ficam preenchidos após o processo
     * @return true se o processo ocorre como esperado, false caso não ocorra como esperado
     */
    private boolean verificaConfig() {
        boolean aux;
        boolean flag = false;
        if (ST.isEmpty() && DD.isEmpty()){
            aux = !this.LG.isEmpty() && !this.allLG.isEmpty();
        }
        else aux = !this.DD.isEmpty() &&
                   !this.ST.isEmpty() &&
                   !this.LG.isEmpty() &&
                   !this.allLG.isEmpty();
        boolean aux2;
        if(this instanceof ObjectSP){
            aux2 = ((ObjectSP) this).verificaSP();
            return aux && aux2;
        }
        if(this instanceof ObjectSS){
            aux2 = ((ObjectSS) this).verificaSS();
            return aux && aux2;
        }
        else return aux;
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
                            else sp.addSS(new InetSocketAddress(InetAddress.getByName(words[2]),53));
                            break;
                        case "DB":
                            if (sp == null) {
                                sp = new ObjectSP();
                                server = sp;
                                server.dominio = words[0];
                            }
                            sp.getCache().createBD(words[2]);
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
                                else ss.addSP(new InetSocketAddress(InetAddress.getByName(words[2]),53));
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
                            else server.addEnderecoDD(new InetSocketAddress(InetAddress.getByName(words[2]),53));
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
                                server.addAllLog(words[2]);
                                logcounter++;
                            }
                            if (!words[0].equals("all")) {
                                if (server.dominio.matches("(.*)"+words[0])) {
                                    server.addLog(words[2]);
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
        System.out.println("Warnings no ficheiro de configuração'" + filename + "':");
        for(String warning : warnings)
        {
            System.out.println("- " + warning);
        }
        return server;
    }

    /**
     * Método toString da classe ServidorConfiguracao
     * @return String representativa da classe ServidorConfiguracao
     */
    @Override
    public String toString() {
        return "ServidorConfiguracao:" + "\n" +
                "   Dominio = " + dominio +" \n" +
                "   DD = " + DD + "\n" +
                "   ST = " + ST + "\n" +
                "   LG = " + LG + "\n" +
                "   all LG = " + LG + "\n" +
                "   cache = " + cache;
    }


}


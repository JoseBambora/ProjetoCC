import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Miguel Cidade Silva
 * Classe que faz o parsing de um ficheiro de configuração de servidores
 * Data de criação 23/10/2022
 * Data de edição 10/11/2022
 */

public class ServidorConfiguracao {

    private String dominio;
    private final List<Endereco> DD;
    private final List<Endereco> ST;
    private final List<String> LG;

    private final List <String> allLG;

    private Cache cache;

    /**
     * Construtor de objetos da classe ServidorConfiguracao
     */
    public ServidorConfiguracao() {
        this.dominio = null;
        this.DD = new ArrayList<>();
        this.ST = new ArrayList<>();
        this.LG = new ArrayList<>();
        this.allLG = new ArrayList<>();
        this.cache = new Cache(5);
    }

    /**
     * Método que adiciona elementos ao campo DD de um servidor
     * @param e endereço a adicionar
     */
    public void addEnderecoDD(Endereco e) {
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
                ST.add(Endereco.stringToIP(str));
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
    public List<Endereco> getDD() {
        return DD;
    }

    /**
     * Getter do campo ST de um objeto da classe ServidorConfiguracao
     * @return O campo ST do ServidorConfiguracao
     */
    public List<Endereco> getST() {
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

    /**
     * Método cuja função é atualizar o valor do campo espaço da cache de um servidor
     * @param n - o novo valor do tamanho da cache do servidor
     */
    public void setEspacoCache(int n) {
        this.cache.setEspaco(n);
    }

    public void setCache(Cache cache){
        this.cache = cache;
    }



    /**
     * Método auxiliar ao parsing que verifica se o processo ocorre como pretendido, ou seja, se os campos do servidor ficam preenchidos após o processo
     * @return true se o processo ocorre como esperado, false caso não ocorra como esperado
     */
    private boolean verificaConfig() {
        boolean aux = !this.DD.isEmpty() &&
                !this.ST.isEmpty() &&
                !this.LG.isEmpty() &&
                !this.allLG.isEmpty();
        boolean aux2 = false;
        if(this instanceof ServidorSP){
            aux2 = ((ServidorSP) this).verificaSP();
        }
        if(this instanceof ServidorSS){
            aux2 = ((ServidorSS) this).verificaSS();
        }
        //System.out.println(aux2);
        return aux && aux2;
    }

    /**
     * Método que realiza o parsing de um ficheiro de configuração de um servidor DNS
     * @param filename localização do ficheiro de configuração
     * @return O servidor configurado
     * @throws IOException exceção lançada caso haja erros de input/output
     */
    public static ServidorConfiguracao parseServer(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        ServidorConfiguracao server = null;
        ServidorSP sp = null;
        ServidorSS ss = null;
        List<String> warnings = new ArrayList<>();
        int logcounter = 0;
        for(String line : lines){
            if (line.length() > 0 && line.charAt(0) != '#') {
                String[] words = line.split(" ");
                if(words.length>2){
                    switch(words[1]){
                        case "SS":
                            if (sp == null) {
                                sp = new ServidorSP();
                                server = sp;
                                server.dominio = words[0];
                                sp.setEspacoCache(100);
                            }
                            sp.addSS(Endereco.stringToIP(words[2]));
                            break;
                        case "DB":
                            if (sp == null) {
                                sp = new ServidorSP();
                                server = sp;
                                server.dominio = words[0];
                                sp.setEspacoCache(100);
                            }
                            sp.getCache().createBD(words[2]);
                            break;
                        case "SP":
                            if (ss == null){
                                ss = new ServidorSS();
                                server = ss;
                                server.dominio = words[0];
                                ss.addSP(Endereco.stringToIP(words[2]));
                                ss.setEspacoCache(10);
                            }
                            else warnings.add("Linha "  + line + " ignorada, pois levaria  a termos mais do que um SP no ficheiro de configuração de um SS."); // apenas adiciona o primeiro
                            break;
                        case "DD":
                            if(server==null){
                                server = new ServidorConfiguracao();
                            }
                            server.addEnderecoDD(Endereco.stringToIP(words[2]));
                            break;
                        case "ST":
                            if(server==null){
                                server = new ServidorConfiguracao();
                            }
                            if (words[0].equals("root")) server.FicheiroST(words[2]);
                            break;
                        case "LG":
                            if(server==null){
                                server = new ServidorConfiguracao();
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


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
 * Data de edição 25/10 2022
 */

public class ServidorConfiguracao {

    private ServidorBD DB;
    private List<Endereco> DD;
    private List<Endereco> ST;
    private List<String> LG;


    public ServidorConfiguracao() {
        this.DB = new ServidorBD();
        this.DD = new ArrayList<>();
        this.ST = new ArrayList<>();
        this.LG = new ArrayList<>();
    }

    public void addEnderecoDD(Endereco e) {
        DD.add(e);
    }

    public void addLog(String path){
        LG.add(path);
    }
    public void FicheiroST(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        for (String str : lines) {
            if (str.length() > 0 && str.charAt(0) != '#') {
                ST.add(Endereco.stringToIP(str));
                }
        }
    }

    public ServidorConfiguracao parseServer(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        ServidorConfiguracao server = null;
        ServidorSP sp = null; // Para não estares sempre (ServidorSP) server
        ServidorSS ss = null;  // Para não estares sempre (ServidorSS) server
        int logcounter = 0;
        for(String line : lines){
            if (line.length() > 0 && line.charAt(0) != '#') {
                String[] words = line.split(" ");
                switch(words[1]){
                        case "SS"->{
                        if (sp == null){
                            sp = new ServidorSP();
                            server = sp;
                            }
                            sp.addSS(Endereco.stringToIP(words[2]));
                        }
                        case "DB" -> {
                            if (sp == null) {
                                sp = new ServidorSP();
                                server = sp;
                            }
                            sp.setBD(words[2]);
                        }
                        case "SP" -> {
                            if (ss == null){
                                ss = new ServidorSS();
                                server = ss;
                            }
                            ss.addSP(Endereco.stringToIP(words[2]));
                        }
                        case "DD" -> addEnderecoDD(Endereco.stringToIP(words[2]));
                        case "ST" -> {
                            if (words[0].equals("root")) FicheiroST(words[2]);
                        }
                        case "LG" -> {
                            logcounter++;
                            if (words[0].equals("all")) addLog(words[2]);
                        }
                }
            }
        }
        if (logcounter == 0) return null;
        return server;
    }
}


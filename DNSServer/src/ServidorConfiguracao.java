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
 * Data de edição 23/10 2022
 */

public class ServidorConfiguracao {

    private ServidorBD DB;
    private ArrayList<Endereco> DD;
    private ArrayList<Endereco> ST;
    private ArrayList<String> LG;


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
        int i = 0;
        while(i < lines.size()){
            String str = lines.get(i);
            if (str.length() > 0 && str.charAt(0) != '#') {
                String[] words = str.split(" ");
                for(String word : words){
                    if(word.equals("SS")) {
                        ServidorSP sp= new ServidorSP();
                        return sp.parseServerSP(filename);
                    }
                    if(word.equals("SP")) {
                        ServidorSS ss = new ServidorSS();
                        return ss.parseServerSS(filename);
                    }
                }
            }
            i++;
        }
        return null;
    }



}

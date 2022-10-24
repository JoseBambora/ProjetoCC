import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Miguel Cidade Silva
 * Classe que faz o parsing de um ficheiro de configuração de servidores secundários
 * Data de criação 23/10/2022
 * Data de edição 23/10 2022
 */

public class ServidorSS extends ServidorConfiguracao{

    private ArrayList<Endereco> SP;

    public ServidorSS () {
        super();
        this.SP = new ArrayList<>();
    }

    public ServidorConfiguracao parseServerSS(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        ServidorConfiguracao servidorConfig = new ServidorSS();
        int logcounter = 0;
        for (String str : lines) {
            if (str.length() > 0 && str.charAt(0) != '#') {
                String[] words = str.split(" ");
                switch (words[1]) {
                    case "DB" -> ServidorBD.createBD(words[2]);
                    case "SP" -> SP.add(Endereco.stringToIP(words[2]));
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
        return servidorConfig;
    }
}
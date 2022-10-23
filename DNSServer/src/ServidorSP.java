import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Miguel Cidade Silva
 * Classe que faz o parsing de um ficheiro de configuração de servidores principais
 * Data de criação 23/10/2022
 * Data de edição 23/10 2022
 */

public class ServidorSP extends ServidorConfiguracao{

    private ArrayList<Endereco> SS;

    public ServidorSP () {
        super();
        this.SS = new ArrayList<>();
    }

    public ServidorConfiguracao parseServerSP(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        ServidorConfiguracao servidorConfig = new ServidorSP();
        int logcounter = 0;
        for (String str : lines) {
            if (str.length() > 0 && str.charAt(0) != '#') {
                String[] words = str.split(" ");
                switch (words[1]) {
                    case "DB" -> ServidorBD.createBD(words[2]);
                    case "SS" -> SS.add(Endereco.stringToIP(words[2]));
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

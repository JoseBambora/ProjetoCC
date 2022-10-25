import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Miguel Cidade Silva
 * Classe que faz o parsing de um ficheiro de configuração de servidores principais
 * Data de criação 23/10/2022
 * Data de edição 25/10 2022
 */

public class ServidorSP extends ServidorConfiguracao {

    private List<Endereco> SS;
    private ServidorBD BD;

    public ServidorSP() {
        super();
        this.SS = new ArrayList<>();
        this.BD = null;

    }

    public void setBD(String path) throws IOException {
        this.BD = ServidorBD.createBD(path);
    }

    public void addSS(Endereco e){
        this.SS.add(e);
    }
}
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Miguel Cidade Silva
 * Classe que faz o parsing de um ficheiro de configuração de servidores secundários
 * Data de criação 23/10/2022
 * Data de edição 25/10 2022
 */

public class ServidorSS extends ServidorConfiguracao{

    private List<Endereco> SP;

    public ServidorSS () {
        super();
        this.SP = new ArrayList<>();
    }

    public void addSP(Endereco e){
        this.SP.add(e);
    }
}
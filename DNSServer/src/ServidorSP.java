import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Miguel Cidade Silva
 * Classe que faz o parsing de um ficheiro de configuração de servidores principais
 * Data de criação 23/10/2022
 * Data de edição 02/11/2022
 */

public class ServidorSP extends ServidorConfiguracao {

    private final List<Endereco> SS;
    private Database BD;

    /**
     * Construtor de objetos da classe ServidorSP
     */
    public ServidorSP() {
        super();
        this.BD = new Database();
        this.SS = new ArrayList<>();
    }

    /**
     * Setter do valor do campo DB de um objeto do tipo Servidor SP
     * @param path caminho do ficheiro de base de dados
     * @throws IOException exceção lançada caso haja erros de input/output
     */
    public void setBD(String path) throws IOException {
        this.BD = Database.createBD(path);
    }

    /**
     * Getter do valor do campo DB de um objeto do tipo Servidor SP
     * @return o valor do campo DB
     */
    public Database getDB() {
        return BD;
    }

    /**
     * Método que adiciona valores ao campo SS de objetos do tipo ServidorSP
     * @param e endereço do SS a adicionar
     */
    public void addSS(Endereco e){
        this.SS.add(e);
    }

    /**
     * Método toString da classe ServidorSP
     * @return String representativa da classe ServidorSP
     */
    @Override
    public String toString() {
        return "ServidorSP:" + "\n" +
                "   DB=" + this.getDB() + "\n" +
                "   DD=" + this.getDD() + "\n" +
                "   ST=" + this.getST() + "\n" +
                "   LG=" + this.getLG() + "\n" +
                "   allLG=" + this.getAllLG() + "\n"+
                "   SS=" + SS + "\n";
    }

    /**
     * Método que verifica se os valores dos campos de um ServidorSP estão bem preenchidos após processo de parsing
     * @return true caso estejam devidamente preenchidos, false caso contrário
     */
    public boolean verificaSP() {
        return !this.SS.isEmpty() && this.BD != null;
    }
}
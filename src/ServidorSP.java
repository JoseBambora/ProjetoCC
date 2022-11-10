import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Miguel Cidade Silva
 * Classe que faz o parsing de um ficheiro de configuração de servidores principais
 * Data de criação 23/10/2022
 * Data de edição 10/11/2022
 */

public class ServidorSP extends ServidorConfiguracao {

    private final List<Endereco> SS;

    /**
     * Construtor de objetos da classe ServidorSP
     */
    public ServidorSP() {
        super();
        this.SS = new ArrayList<>();
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
                "   Dominio = " + this.getDominio() +"\n" +
                "   SS = " + SS + "\n" +
                "   DD = " + this.getDD() + "\n" +
                "   ST = " + this.getST() + "\n" +
                "   LG = " + this.getLG() + "\n" +
                "   allLG = " + this.getAllLG() + "\n"+
                "   cache = " + this.getCache();

    }

    /**
     * Método que verifica se os valores dos campos de um ServidorSP estão bem preenchidos após processo de parsing
     * @return true caso estejam devidamente preenchidos, false caso contrário
     */
    public boolean verificaSP() {
        return !this.SS.isEmpty() && this.getCache().checkBD();
    }
}
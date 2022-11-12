import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * @author Miguel Cidade Silva
 * Classe que faz o parsing de um ficheiro de configuração de servidores secundários
 * Data de criação 23/10/2022
 * Data de edição 11/11/2022
 */

public class ObjectSS extends ObjectServer {

    private InetSocketAddress SP;

    /**
     * Construtor de objetos da classe ServidorSS
     */
    public ObjectSS() {
        super();
        this.SP = null;
    }
    /**
     * Método que adiciona valores ao campo SP de objetos do tipo ServidorSS
     * @param e endereço do SP a adicionar
     */
    public void addSP(InetSocketAddress e){
        this.SP = e;
    }


    /**
     * Método toString da classe ServidorSS
     * @return String representativa da classe ServidorSS
     */
    @Override
    public String toString() {
        return "ServidorSS{" + "\n" +
                "   Dominio = " + this.getDominio() +"\n" +
                "   SP = " + SP + "\n" +
                "   DD = " + this.getDD() + "\n" +
                "   ST = " + this.getST() + "\n" +
                "   LG = " + this.getLG() + "\n" +
                "   allLG = " + this.getAllLG() + "\n"+
                "   cache = " + this.getCache();
    }

    /**
     * Método que verifica se os valores dos campos de um ServidorSS estão bem preenchidos após processo de parsing
     * @return true caso estejam devidamente preenchidos, false caso contrário
     */
    public boolean verificaSS() {
        return !(this.SP == null); //não verificamos BD pois está apenas tem valores após a transferência de zona
    }
}
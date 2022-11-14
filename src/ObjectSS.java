import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * @author Miguel Cidade Silva
 * Classe que faz o parsing de um ficheiro de configuração de servidores secundários
 * Data de criação 23/10/2022
 * Data de edição 14/11/2022
 */

public class ObjectSS extends ObjectServer {

    private InetSocketAddress SP;

    /**
     * Construtor de objetos da classe ObjectSS
     */
    public ObjectSS() {
        super();
        this.SP = null;
    }
    /**
     * Método que adiciona valores ao campo SP de objetos do tipo ObjectSS
     * @param e endereço do SP a adicionar
     */
    public void addSP(InetSocketAddress e){
        this.SP = e;
    }

    public void setSP(InetSocketAddress SP) {
        this.SP = SP;
    }

    /**
     * Getter do campo SP de um obeto da classe ObjectSS
     * @return o valor do campo SP
     */
    public InetSocketAddress getSP() {
        return SP;
    }

    /**
     * Método toString da classe ObjectSS
     * @return String representativa da classe ObjectSS
     */
    @Override
    public String toString() {
        return "ObjectSS{" + "\n" +
                "   Dominio = " + this.getDominio() +"\n" +
                "   SP = " + SP + "\n" +
                "   DD = " + this.getDD() + "\n" +
                "   ST = " + this.getST() + "\n" +
                "   LG = " + this.getLG() + "\n" +
                "   allLG = " + this.getAllLG() + "\n"+
                "   cache = " + this.getCache();
    }

    /**
     * Método que verifica se os valores dos campos de um ObjectSS estão bem preenchidos após processo de parsing
     * @return true caso estejam devidamente preenchidos, false caso contrário
     */
    public boolean verificaSS() {
        return !(this.SP == null); //não verificamos BD pois está apenas tem valores após a transferência de zona
    }
}
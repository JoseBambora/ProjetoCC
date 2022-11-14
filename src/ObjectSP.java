import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Miguel Cidade Silva
 * Classe que faz o parsing de um ficheiro de configuração de servidores principais
 * Data de criação 23/10/2022
 * Data de edição 14/11/2022
 */

public class ObjectSP extends ObjectServer {

    private List<InetSocketAddress> SS;

    /**
     * Construtor de objetos da classe ObjectSP
     */
    public ObjectSP() {
        super();
        this.SS = new ArrayList<>();
    }

    /**
     * Método que adiciona valores ao campo SS de objetos do tipo ObjectSP
     * @param e endereço do SS a adicionar
     */
    public void addSS(InetSocketAddress e){
        this.SS.add(e);
    }


    public List<InetSocketAddress> getSS() {
        return SS;
    }

    public void setSS(List<InetSocketAddress> e){
        this.SS = e;
    }

    /**
     * Método toString da classe ObjectSP
     * @return String representativa da classe ObjectSP
     */
    @Override
    public String toString() {
        return "ObjectSP:" + "\n" +
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
     * @param dominio - Dominio do servidor
     * @return true caso estejam devidamente preenchidos, false caso contrário
     */
    public boolean verificaSP(String dominio) {
        boolean emptyfields;
        if (this.SS.isEmpty() && this.getDD().isEmpty()&& this.getST().isEmpty()) emptyfields = true; //caso seja um ST deve ter estes campos vazios
        else emptyfields= !this.SS.isEmpty(); //se for um SP está mal configurado caso não tenha nenhum SS
        boolean dbchecker;
        if (dominio.equals(".reverse.G706.")) dbchecker = this.getCache().checkBD("REVERSE");
        else dbchecker = this.getCache().checkBD("SP");
        return emptyfields && dbchecker;
    }
}
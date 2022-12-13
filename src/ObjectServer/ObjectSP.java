package ObjectServer;

import ObjectServer.ObjectServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Miguel Cidade Silva
 * Classe que faz o parsing de um ficheiro de configuração de servidores principais
 * Data de criação 23/10/2022
 * Data de edição 22/11/2022
 */

public class ObjectSP extends ObjectServer {

    private List<InetSocketAddress> SS;
    private String BD;

    /**
     * Construtor de objetos da classe ObjectServer.ObjectSP
     */
    public ObjectSP() {
        super();
        this.SS = new ArrayList<>();
        this.BD = "";
    }

    /**
     * Método que adiciona valores ao campo SS de objetos do tipo ObjectServer.ObjectSP
     * @param e endereço do SS a adicionar
     */
    public void addSS(InetSocketAddress e){
        this.SS.add(e);
    }

    /**
     * Getter do campo SS de um objeto da classe ObjectServer.ObjectSP
     * @return o valor co campo SS
     */
    public List<InetSocketAddress> getSS() {
        return SS;
    }

    /**
     * Setter do campo SS de um objeto da classe ObjectServer.ObjectSP
     * @param e lista de endereços que pretendemos que seja o valor do campo SS
     */
    public void setSS(List<InetSocketAddress> e){
        this.SS = e;
    }

    /**
     * Setter do campo DB de um objeto da classe ObjectServer.ObjectSP
     * @return o valor do campo DB
     */
    public String getBD() {
        return BD;
    }

    /**
     * Getter do campo DB de um objeto da classe ObjectServer.ObjectSP
     * @param BD a localização do ficheiro da base de dados a associar a um objeto da classe ObjectServer.ObjectSP
     */
    public void setBD(String BD) {
        this.BD = BD;
    }

    /**
     * Método toString da classe ObjectServer.ObjectSP
     * @return String representativa da classe ObjectServer.ObjectSP
     */
    @Override
    public String toString() {
        return "ObjectServer.ObjectSP:" + "\n" +
                "   SS = " + SS + "\n" +
                "   BD = " + BD + "\n" +
                "   DD = " + this.getDD() + "\n" +
                "   ST = " + this.getST() + "\n" +
                "   LOGS = " + this.getLogs() +"\n"+
                "   CACHE = " + this.getCache();

    }

    /**
     * Método auxiliar que fornece o tipo a utilizar na verificação da base de dados de um servidor conforme o dominio
     * @param dominio dominio do servidor
     * @return O tipo de base de dados
     */
    private String getType(String dominio){
        String type = "";
        if (dominio.equals("10.IN-ADDR.REVERSE.G706.")){
            type = "REVERSE";
        }
        else{
            if(dominio.matches("(.*)"+"REVERSE.G706.")){
                type = "REVERSET";
            }
            else type = "SP";
        }
        System.out.println(type);
        return type;
    }


    /**
     * Método que verifica se os valores dos campos de um ServidorSP estão bem preenchidos após processo de parsing
     * @param dominio - Dominio do servidor
     * @return true caso estejam devidamente preenchidos, false caso contrário
     */
    public boolean verificaSP(String dominio) {
        boolean emptyfields;
        if (this.getDD().isEmpty() && this.getST().isEmpty()) emptyfields = true; //caso seja um ST deve ter estes campos vazios
        else emptyfields = !this.SS.isEmpty(); //se for um SP está mal configurado caso não tenha nenhum SS
        if(dominio.matches("(.*)"+"REVERSE.G706.")) emptyfields = true;
        boolean dbchecker;
        dbchecker = this.getCache().checkBD(this.getType(dominio));
        System.out.println("dbchecker:"+dbchecker+" emptyfields:"+emptyfields);
        return emptyfields && dbchecker;
    }
}
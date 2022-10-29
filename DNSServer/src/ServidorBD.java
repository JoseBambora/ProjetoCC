import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author José Carvalho
 * Classe que representa a estrutura de um servidor principal
 * Data criação: 23/10/2022
 * Data última atualização: 29/10/2022
 */
public class ServidorBD
{
    /**
     *  Indica o nome completo do SP do domínio indicado no parâmetro
     */
    private Triple<String,String,Integer> SOASP;
    /**
     * Indica o endereço de e-mail completo do administrador do domínio
     */
    private Triple<String,String,Integer> SOAADMIN;
    /**
     * Indica o número de série da base de dados do SP
     */
    private Triple<String,String,Integer> SOASERIAL;
    /**
     * Indica o intervalo temporal em segundos para um SS perguntar ao SP
     * qual o número de série da base de dados dessa zona.
     */
    private Triple<String,Integer,Integer> SOAREFRESH;
    /**
     * Indica o intervalo temporal em segundos para um SS perguntar ao SP
     * qual o número de série da base de dados dessa zona  após um timeout.
     */
    private Triple<String,Integer,Integer>  SOARETRY;
    /**
     * Indica o intervalo temporal para um SS deixar de considerar a sua réplica
     * da base de dados da zona indicada no parâmetro como válida
     */
    private Triple<String,Integer,Integer>  SOAEXPIRE;
    /**
     * Indica o nome dum servidor que é autoritativo para o domínio indicado no parâmetro.
     */
    private Map<String,List<Triple<String,Integer,Integer>>> NS;

    /**
     * Indica o endereço IPv4 dum host/servidor indicado no parâmetro como nome
     */
    private Map<String,List<Triple<Endereco,Integer,Integer>>> A; // PRIORIDADE
    /**
     * Indica um nome canónico (ou alias) associado ao nome indicado no
     * parâmetro
     */
    private Map<String,Tuple<String,Integer>> CNAME;
    /**
     * Indica o nome dum servidor de e-mail para o domínio indicado no parâmetro
     */
    private Map<String,List<Triple<String,Integer,Integer>>> MX;
    /**
     * Indica o nome dum servidor/host que usa o endereço IPv4 indicado no parâmetro
     */
    private Map<Endereco,Tuple<String,Integer>> PTR;


    /**
     * Construtor da base de dados de um servidor
     */
    public ServidorBD()
    {
        this.SOASP = null;
        this.SOAADMIN = null;
        this.SOASERIAL = null;
        this.SOARETRY = null;
        this.SOAREFRESH = null;
        this.SOAEXPIRE = null;
        this.NS = new HashMap<>();
        this.CNAME = new HashMap<>();
        this.MX = new HashMap<>();
        this.A = new HashMap<>();
        this.PTR = new HashMap<>();
    }

    /**
     * Adiciona no campo NS um elemento inserindo o domínio, o servidor para o qual o servidor principal dono desta BD é
     * autoritativo e a prioridade correspondente
     * @param dominio Endereço URL do domínio
     * @param server Endereço URL do servidor
     * @param prioridade Prioridade
     */
    public void addNS(String dominio ,String server, Integer prioridade, Integer TTL)
    {
        if(!this.NS.containsKey(dominio))
            this.NS.put(dominio,new ArrayList<>());
        this.NS.get(dominio).add(new Triple<>(server,prioridade,TTL));
    }

    /**
     * Adiciona no campo A um elemento inserindo o nome do servidor, o endereço IPV4 do mesmo e a prioridade.
     * Também adiciona um PTR.
     * @param str Nome do servidor
     * @param enderecos Endereço IPV4
     * @param prioridade Prioridade
     */
    public void addA(String str, Endereco enderecos, Integer prioridade, Integer TTL)
    {
        if(!this.A.containsKey(str))
            this.A.put(str, new ArrayList<>());
        this.A.get(str).add(new Triple<>(enderecos,prioridade,TTL));
        this.addPTR(enderecos,str,TTL);
    }

    /**
     * Adiciona a associação nome canónico a um nome
     * @param canonico Nome canónico
     * @param nome Nome
     */
    public void addCNAME(String canonico, String nome, Integer TTL)
    {
        this.CNAME.put(canonico,new Tuple<>(nome, TTL));
    }

    /**
     * Adiciona o nome do servidor de email para o domínio presente com a prioridade
     * @param email Email
     * @param prioridade Prioridade
     */
    public void addMX(String dominio,String email, Integer prioridade, Integer TTL)
    {
        if(!this.MX.containsKey(dominio))
            this.MX.put(dominio,new ArrayList<>());
        this.MX.get(dominio).add(new Triple<>(email,prioridade, TTL));
    }

    /**
     * Adiciona uma associação entre endereço IP e endereço URL.
     * @param endereco Endereço IP
     * @param str Endereço URL.
     */
    public void addPTR(Endereco endereco, String str, Integer TTL)
    {
        this.PTR.put(endereco,new Tuple<>(str,TTL));
    }

    /**
     * Adiciona o par dominio e o respetivo endereço URL do servidor principal na BD.
     * @param dominio Domínio
     * @param SOASP URL do Servidor principal
     */
    public void setSOASP(String dominio, String SOASP, Integer TTL) {
        if(this.SOASP == null)
            this.SOASP = new Triple<>(dominio,SOASP, TTL);
    }

    /**
     * Adiciona o par dominio e o respetivo email do admin do servidor principal na BD.
     * @param dominio  Domínio do SP
     * @param SOAADMIN Email do admin
     */
    public void setSOAADMIN(String dominio, String SOAADMIN,Integer TTL) {
        if(this.SOAADMIN == null)
            this.SOAADMIN = new Triple<>(dominio,SOAADMIN, TTL);
    }

    /**
     * Estabele a ligação entre domínio e o número de serie da BD
     * @param dominio  Domínio do SP
     * @param SOASERIAL Número de serie
     */
    public void setSOASERIAL(String dominio, String SOASERIAL, Integer TTL) {
        if(this.SOASERIAL == null)
            this.SOASERIAL = new Triple<>(dominio,SOASERIAL, TTL);
    }

    /**
     * Faz a ligação entre o domínio e a intervalo temporal até o SS pedir ao SP o número de serie
     * @param dominio  Domínio do SP
     * @param SOAREFRESH Intervalo temporal
     */
    public void setSOAREFRESH(String dominio, Integer SOAREFRESH, Integer TTL) {
        if(this.SOAREFRESH == null)
            this.SOAREFRESH = new Triple<>(dominio,SOAREFRESH, TTL);
    }

    /**
     * Faz a ligação entre o domínio e a intervalo temporal até o SS pedir ao SP o número de serie em caso de timeout
     * @param dominio  Domínio do SP
     * @param SOARETRY Intervalo temporal
     */
    public void setSOARETRY(String dominio, Integer SOARETRY, Integer TTL) {
        if(this.SOARETRY == null)
            this.SOARETRY = new Triple<>(dominio,SOARETRY, TTL);
    }

    /**
     * Faz a ligação entre o domínio e a intervalo temporal até o SS pedir ao SP o número de serie por desconfiar da sua réplica
     * @param dominio Domínio do SP
     * @param SOAEXPIRE Intervalo temporal
     */
    public void setSOAEXPIRE(String dominio, Integer SOAEXPIRE,Integer TTL) {
        if(this.SOAEXPIRE == null)
            this.SOAEXPIRE = new Triple<>(dominio,SOAEXPIRE, TTL);
    }

    /**
     * Método toString, para efeitos de debug.
     * @return BD em formato de string
     */
    @Override
    public String toString() {
        return "ServidorBD: " + "\n" +
                "   SOASP=" + SOASP + "\n" +
                "   SOAADMIN=" + SOAADMIN +"\n" +
                "   SOASERIAL=" + SOASERIAL +"\n" +
                "   SOAREFRESH=" + SOAREFRESH +"\n" +
                "   SOARETRY=" + SOARETRY +"\n" +
                "   SOAEXPIRE=" + SOAEXPIRE +"\n" +
                "   NS=" + NS +"\n" +
                "   A=" + A +"\n" +
                "   CNAME=" + CNAME +"\n" +
                "   MX=" + MX +"\n" +
                "   PTR=" + PTR;
    }

    /**
     * Método auxiliar ao parsing, de forma a retornar o valor correto do endereço URL.
     * Quando o endereço já termina com o '.' este método não faz nada.
     * @param str Endereço a converter.
     * @param macro Mapeamento que contém as macros guardados. É util para ir buscar a marco "@".
     * @return Endereço URL final.
     */
    private static String converteDom(String str, Map<String,String> macro)
    {
        String res = str;
        if(str.charAt(str.length()-1) != '.')
        {
            if(res.contains("@"))
                res = res.replaceAll("@",macro.get("@"));
            else
                res += "." + macro.get("@");
        }
        return res;
    }

    /**
     * Método que converte uma string em inteiro tendo recurso à macro em caso
     * de a string não ser um número
     * @param words Palavras para converter
     * @param macro Macro em caso de a palavra correspondente não ser um número
     * @return Inteiro Convertido
     */
    private static int converteInt(String[] words, Map<String,String> macro, int index)
    {
        int ttl = 0;
        if(index < words.length)
        {
            String word = words[index];
            try
            {
                ttl = Integer.parseInt(word);
            }
            catch (NumberFormatException e)
            {
                String str = macro.get(word);
                if(str != null)
                {
                    ttl = Integer.parseInt(str);
                }
            }
        }
        return ttl;
    }

    /**
     * Método que verifica se a base de dados contém todos os campos e naqueles que
     * podemos ter vários elementos, verifica se tem pelo menos 1 elemento.
     * @return Verdadeiro se a BD estiver correta, false caso contrário
     */
    private boolean verificaBD()
    {
        return  this.SOAADMIN != null && this.SOAEXPIRE != null &&
                this.SOAREFRESH != null && this.SOASERIAL != null &&
                this.SOASP != null && this.SOARETRY != null &&
                this.NS.size() > 0 && this.A.size() > 0 &&
                this.MX.size() > 0 && this.PTR.size() > 0 &&
                this.CNAME.size() > 0;
    }

    /**
     * Método que faz o parsing de um ficheiro para um BD
     * @param filename Nome do ficheiro.
     * @return Base de Dados.
     * todo Ver linhas erradas, o que fazer.
     */
    public static ServidorBD createBD(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        Map<String,String> macro = new HashMap<>();
        ServidorBD servidorBD = new ServidorBD();
        for(String str : lines)
        {
            if(str.length() > 0 && str.charAt(0) != '#')
            {
                String[] words = str.split(" ");
                if(words[1].equals("DEFAULT"))
                    macro.put(words[0], words[2]);
                else
                {
                    String dom = converteDom(words[0], macro);
                    Integer TTL = converteInt(words, macro,3);
                    switch (words[1]) {
                        case "SOASP"      -> servidorBD.setSOASP(dom, words[2], TTL);
                        case "SOAADMIN"   -> servidorBD.setSOAADMIN(dom, words[2], TTL);
                        case "SOASERIAL"  -> servidorBD.setSOASERIAL(dom, words[2], TTL);
                        case "SOAREFRESH" -> servidorBD.setSOAREFRESH(dom, converteInt(words,macro,2), TTL);
                        case "SOARETRY"   -> servidorBD.setSOARETRY(dom, converteInt(words,macro,2), TTL);
                        case "SOAEXPIRE"  -> servidorBD.setSOAEXPIRE(dom, converteInt(words,macro,2), TTL);
                        case "NS"         -> servidorBD.addNS(dom, words[2], converteInt(words,macro,4), TTL);
                        case "A"          -> servidorBD.addA(dom, Endereco.stringToIP(words[2]), converteInt(words,macro,4), TTL);
                        case "CNAME"      -> servidorBD.addCNAME(dom, words[2], TTL);
                        case "MX"         -> servidorBD.addMX(dom, words[2], converteInt(words,macro,4), TTL);
                        case "PTR"        -> servidorBD.addPTR(Endereco.stringToIP(words[2]), words[2], TTL);
                    }
                }
            }
        }
        if(servidorBD.verificaBD())
            return servidorBD;
        else
            return null;
    }
}

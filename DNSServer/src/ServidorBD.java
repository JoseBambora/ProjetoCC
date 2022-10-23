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
 */
public class ServidorBD
{
    /**
     * Deve ser 0 só se for indicado por parâmetro
     */
    private int TTL;
    /**
     *  Indica o nome completo do SP do domínio indicado no parâmetro
     */
    private Tuple<String,String> SOASP;
    /**
     * Indica o endereço de e-mail completo do administrador do domínio
     */
    private Tuple<String,String> SOAADMIN;
    /**
     * Indica o número de série da base de dados do SP
     */
    private Tuple<String,String> SOASERIAL;
    /**
     * Indica o intervalo temporal em segundos para um SS perguntar ao SP
     * qual o número de série da base de dados dessa zona.
     */
    private Tuple<String,Integer> SOAREFRESH;
    /**
     * Indica o intervalo temporal em segundos para um SS perguntar ao SP
     * qual o número de série da base de dados dessa zona  após um timeout.
     */
    private Tuple<String,Integer>  SOARETRY;
    /**
     * Indica o intervalo temporal para um SS deixar de considerar a sua réplica
     * da base de dados da zona indicada no parâmetro como válida
     */
    private Tuple<String,Integer>  SOAEXPIRE;
    /**
     * Indica o nome dum servidor que é autoritativo para o domínio indicado no parâmetro.
     */
    private Map<String,List<Tuple<String,Integer>>> NS;

    /**
     * Indica o endereço IPv4 dum host/servidor indicado no parâmetro como nome
     */
    private Map<String,List<Tuple<Endereco,Integer>>> A; // PRIORIDADE
    /**
     * Indica um nome canónico (ou alias) associado ao nome indicado no
     * parâmetro
     */
    private Map<String,String> CNAME;
    /**
     * Indica o nome dum servidor de e-mail para o domínio indicado no parâmetro
     */
    private Map<String,List<Tuple<String,Integer>>> MX;
    /**
     * Indica o nome dum servidor/host que usa o endereço IPv4 indicado no parâmetro
     */
    private Map<Endereco,String> PTR;


    /**
     * Construtor da base de dados de um servidor
     */
    public ServidorBD()
    {
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
    public void addNS(String dominio ,String server, Integer prioridade)
    {
        if(!this.NS.containsKey(dominio))
            this.NS.put(dominio,new ArrayList<>());
        this.NS.get(dominio).add(new Tuple<>(server,prioridade));
    }

    /**
     * Adiciona no campo A um elemento inserindo o nome do servidor, o endereço IPV4 do mesmo e a prioridade.
     * Também adiciona um PTR.
     * @param str Nome do servidor
     * @param enderecos Endereço IPV4
     * @param prioridade Prioridade
     */
    public void addA(String str, Endereco enderecos, Integer prioridade)
    {
        if(!this.A.containsKey(str))
            this.A.put(str, new ArrayList<>());
        this.A.get(str).add(new Tuple<>(enderecos,prioridade));
        this.addPTR(enderecos,str);
    }

    /**
     * Adiciona a associação nome canónico a um nome
     * @param canonico Nome canónico
     * @param nome Nome
     */
    public void addCNAME(String canonico, String nome)
    {
        this.CNAME.put(canonico,nome);
    }

    /**
     * Adiciona o nome do servidor de email para o domínio presente com a prioridade
     * @param email Email
     * @param prioridade Prioridade
     */
    public void addMX(String dominio,String email, Integer prioridade)
    {
        if(!this.MX.containsKey(dominio))
            this.MX.put(dominio,new ArrayList<>());
        this.MX.get(dominio).add(new Tuple<>(email,prioridade));;
    }

    /**
     * Adiciona uma associação entre endereço IP e endereço URL.
     * @param endereco Endereço IP
     * @param str Endereço URL.
     */
    public void addPTR(Endereco endereco, String str)
    {
        this.PTR.put(endereco,str);
    }

    /**
     * Adiciona o par dominio e o respetivo endereço URL do servidor principal na BD.
     * @param dominio Domínio
     * @param SOASP URL do Servidor principal
     */
    public void setSOASP(String dominio, String SOASP) {
        this.SOASP = new Tuple<>(dominio,SOASP);
    }

    /**
     * Adiciona o par dominio e o respetivo email do admin do servidor principal na BD.
     * @param dominio  Domínio do SP
     * @param SOAADMIN Email do admin
     */
    public void setSOAADMIN(String dominio, String SOAADMIN) {
        this.SOAADMIN = new Tuple<>(dominio,SOAADMIN);
    }

    /**
     * Estabele a ligação entre domínio e o número de serie da BD
     * @param dominio  Domínio do SP
     * @param SOASERIAL Número de serie
     */
    public void setSOASERIAL(String dominio, String SOASERIAL) {
        this.SOASERIAL = new Tuple<>(dominio,SOASERIAL);
    }

    /**
     * Faz a ligação entre o domínio e a intervalo temporal até o SS pedir ao SP o número de serie
     * @param dominio  Domínio do SP
     * @param SOAREFRESH Intervalo temporal
     */
    public void setSOAREFRESH(String dominio, Integer SOAREFRESH) {
        this.SOAREFRESH = new Tuple<>(dominio,SOAREFRESH);
    }

    /**
     * Faz a ligação entre o domínio e a intervalo temporal até o SS pedir ao SP o número de serie em caso de timeout
     * @param dominio  Domínio do SP
     * @param SOARETRY Intervalo temporal
     */
    public void setSOARETRY(String dominio, Integer SOARETRY) {
        this.SOARETRY = new Tuple<>(dominio,SOARETRY);
    }

    /**
     * Faz a ligação entre o domínio e a intervalo temporal até o SS pedir ao SP o número de serie por desconfiar da sua réplica
     * @param dominio Domínio do SP
     * @param SOAEXPIRE Intervalo temporal
     */
    public void setSOAEXPIRE(String dominio, Integer SOAEXPIRE) {
        this.SOAEXPIRE = new Tuple<>(dominio,SOAEXPIRE);
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
     * Método auxiliar ao parsing, de forma a ir buscar o valor das prioridades.
     * Se a linha não conter o campo prioridade, o valor é definido como 0.
     * @param words Lista de palavras de uma linha do parse
     * @return Prioridade final.
     */
    private static int convertePrioridade(String[] words)
    {
        int prioridade = 0;
        if (words.length > 4)
            prioridade = Integer.parseInt(words[4]);
        return prioridade;
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
                switch (words[1]) {
                    case "DEFAULT"    -> macro.put(words[0], words[2]);
                    case "SOASP"      -> servidorBD.setSOASP(converteDom(words[0], macro), words[2]);
                    case "SOAADMIN"   -> servidorBD.setSOAADMIN(converteDom(words[0], macro), words[2]);
                    case "SOASERIAL"  -> servidorBD.setSOASERIAL(converteDom(words[0], macro), words[2]);
                    case "SOAREFRESH" -> servidorBD.setSOAREFRESH(converteDom(words[0], macro), Integer.parseInt(words[2]));
                    case "SOARETRY"   -> servidorBD.setSOARETRY(converteDom(words[0], macro), Integer.parseInt(words[2]));
                    case "SOAEXPIRE"  -> servidorBD.setSOAEXPIRE(converteDom(words[0], macro), Integer.parseInt(words[2]));
                    case "NS"         -> servidorBD.addNS(converteDom(words[0], macro), words[2], convertePrioridade(words));
                    case "A"          -> servidorBD.addA(converteDom(words[0], macro), Endereco.stringToIP(words[2]), convertePrioridade(words));
                    case "CNAME"      -> servidorBD.addCNAME(converteDom(words[0], macro), words[2]);
                    case "MX"         -> servidorBD.addMX(converteDom(words[0], macro), words[2], Integer.parseInt(words[4]));
                    case "PTR"        -> servidorBD.addPTR(Endereco.stringToIP(words[2]), words[2]);
                }
            }
        }
        return servidorBD;
    }
}

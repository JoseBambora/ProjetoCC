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
 * Data última atualização: 1/11/2022
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
    private final Map<String,List<Triple<String,Integer,Integer>>> NS;

    /**
     * Indica o endereço IPv4 dum host/servidor indicado no parâmetro como nome
     */
    private final Map<String,List<Triple<Endereco,Integer,Integer>>> A; // PRIORIDADE
    /**
     * Indica um nome canónico (ou alias) associado ao nome indicado no
     * parâmetro
     */
    private final Map<String,Tuple<String,Integer>> CNAME;
    /**
     * Indica o nome dum servidor de e-mail para o domínio indicado no parâmetro
     */
    private final Map<String,List<Triple<String,Integer,Integer>>> MX;
    /**
     * Indica o nome dum servidor/host que usa o endereço IPv4 indicado no parâmetro
     */
    private final Map<Endereco,Tuple<String,Integer>> PTR;


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
     * Método auxliar ao NS e ao MX para ver se não há endereços URL e emails repetidos,
     * respetivamente, para o mesmo domínio
     * @param list Lista com a informação de um domínio
     * @param info Informação a comparar
     * @return Falso se não existir informação repetida, verdadeiro caso contrário.
     */
    private static boolean repeatedInfo(List<Triple<String,Integer,Integer>> list, String info)
    {
        boolean found = false;
        for(Triple<String,Integer,Integer> elem : list)
            if (elem.getValue1().equals(info))
            {
                found = true;
                break;
            }
        return !found;
    }
    /**
     * Adiciona no campo NS um elemento inserindo o domínio, o servidor para o qual o servidor principal dono desta BD é
     * autoritativo e a prioridade correspondente
     * @param dominio Endereço URL do domínio
     * @param server Endereço URL do servidor
     * @param prioridade Prioridade
     */
    public boolean addNS(String dominio ,String server, Integer prioridade, Integer TTL)
    {
        if(!this.NS.containsKey(dominio))
            this.NS.put(dominio,new ArrayList<>());
        List<Triple<String,Integer,Integer>> list = this.NS.get(dominio);
        boolean res = repeatedInfo(list, server);
        if(res)
            list.add(new Triple<>(server,prioridade,TTL));
        return res;
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
    public boolean addCNAME(String canonico, String nome, Integer TTL)
    {
        boolean res = false;
        if(!this.CNAME.containsKey(canonico))
        {
            this.CNAME.put(canonico,new Tuple<>(nome, TTL));
            res = true;
        }
        return res;
    }

    /**
     * Adiciona o nome do servidor de email para o domínio presente com a prioridade
     * @param email Email
     * @param prioridade Prioridade
     */
    public boolean addMX(String dominio,String email, Integer prioridade, Integer TTL)
    {
        if(!this.MX.containsKey(dominio))
            this.MX.put(dominio,new ArrayList<>());
        List<Triple<String,Integer,Integer>> list = this.MX.get(dominio);
        boolean res = repeatedInfo(list, email);
        if(res)
            list.add(new Triple<>(email,prioridade, TTL));
        return res;
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
     * @return True se adicionar com sucesso. False caso contrário.
     */
    public boolean setSOASP(String dominio, String SOASP, Integer TTL) {
        boolean res = false;
        if(this.SOASP == null)
        {
            res = true;
            this.SOASP = new Triple<>(dominio,SOASP, TTL);
        }
        return res;
    }

    /**
     * Adiciona o par dominio e o respetivo email do admin do servidor principal na BD.
     * @param dominio  Domínio do SP
     * @param SOAADMIN Email do admin
     * @return True se adicionar com sucesso. False caso contrário.
     */
    public boolean setSOAADMIN(String dominio, String SOAADMIN,Integer TTL) {
        boolean res = false;
        if(this.SOAADMIN == null)
        {
            res = true;
            this.SOAADMIN = new Triple<>(dominio,SOAADMIN, TTL);
        }
        return res;
    }

    /**
     * Estabele a ligação entre domínio e o número de serie da BD
     * @param dominio  Domínio do SP
     * @param SOASERIAL Número de serie
     * @return True se adicionar com sucesso. False caso contrário.
     */
    public boolean setSOASERIAL(String dominio, String SOASERIAL, Integer TTL) {
        boolean res = false;
        if(this.SOASERIAL == null)
        {
            res = true;
            this.SOASERIAL = new Triple<>(dominio,SOASERIAL, TTL);
        }
        return res;
    }

    /**
     * Faz a ligação entre o domínio e a intervalo temporal até o SS pedir ao SP o número de serie
     * @param dominio  Domínio do SP
     * @param SOAREFRESH Intervalo temporal
     * @return True se adicionar com sucesso. False caso contrário.
     */
    public boolean setSOAREFRESH(String dominio, Integer SOAREFRESH, Integer TTL) {
        boolean res = false;
        if(this.SOAREFRESH == null) {
            res = true;
            this.SOAREFRESH = new Triple<>(dominio, SOAREFRESH, TTL);
        }
        return res;
    }

    /**
     * Faz a ligação entre o domínio e a intervalo temporal até o SS pedir ao SP o número de serie em caso de timeout
     * @param dominio  Domínio do SP
     * @param SOARETRY Intervalo temporal
     * @return True se adicionar com sucesso. False caso contrário.
     */
    public boolean setSOARETRY(String dominio, Integer SOARETRY, Integer TTL) {
        boolean res = false;
        if(this.SOARETRY == null)
        {
            res = true;
            this.SOARETRY = new Triple<>(dominio,SOARETRY, TTL);
        }
        return res;
    }

    /**
     * Faz a ligação entre o domínio e a intervalo temporal até o SS pedir ao SP o número de serie por desconfiar da sua réplica
     * @param dominio Domínio do SP
     * @param SOAEXPIRE Intervalo temporal
     * @return True se adicionar com sucesso. False caso contrário.
     */
    public boolean setSOAEXPIRE(String dominio, Integer SOAEXPIRE,Integer TTL) {
        boolean res = false;
        if(this.SOAEXPIRE == null)
        {
            res = true;
            this.SOAEXPIRE = new Triple<>(dominio,SOAEXPIRE, TTL);
        }
        return res;
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
     */
    public static ServidorBD createBD(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        Map<String,String> macro = new HashMap<>();
        ServidorBD servidorBD = new ServidorBD();
        List<String> warnings = new ArrayList<>();
        int l = 1;
        for(String str : lines)
        {
            String[] words = str.split(" ");
            if(str.length() > 0 && str.charAt(0) != '#' && words.length > 2)
            {
                if(words[1].equals("DEFAULT"))
                    macro.put(words[0], words[2]);
                else
                {
                    String dom = converteDom(words[0], macro);
                    Integer TTL = converteInt(words, macro,3);
                    if(TTL == 0)
                    {
                        warnings.add("TTL inválido na linha " + l + ". Campo " + words[1] + " não adicionado.");
                    }
                    else
                    {
                        boolean res = true;
                        switch (words[1]) {
                            case "SOASP"      -> res = servidorBD.setSOASP(dom, words[2], TTL);
                            case "SOAADMIN"   -> res = servidorBD.setSOAADMIN(dom, words[2], TTL);
                            case "SOASERIAL"  -> res = servidorBD.setSOASERIAL(dom, words[2], TTL);
                            case "SOAREFRESH" -> res = servidorBD.setSOAREFRESH(dom, converteInt(words,macro,2), TTL);
                            case "SOARETRY"   -> res = servidorBD.setSOARETRY(dom, converteInt(words,macro,2), TTL);
                            case "SOAEXPIRE"  -> res = servidorBD.setSOAEXPIRE(dom, converteInt(words,macro,2), TTL);
                            case "NS"         -> res = servidorBD.addNS(dom, words[2], converteInt(words,macro,4), TTL);
                            case "CNAME"      -> res = servidorBD.addCNAME(dom, words[2], TTL);
                            case "MX"         -> res = servidorBD.addMX(dom, words[2], converteInt(words,macro,4), TTL);
                            case "A"          -> servidorBD.addA(dom, Endereco.stringToIP(words[2]), converteInt(words,macro,4), TTL);
                            case "PTR"        -> servidorBD.addPTR(Endereco.stringToIP(words[2]), words[2], TTL);
                        }
                        if(!res)
                            warnings.add("Linha " + l + " com informação repetida para o campo " + words[1]);
                    }
                }
            }
            l++;
        }
        if(!servidorBD.verificaBD())
        {
            servidorBD = null;
            warnings.add("Campos em falta. BD não criada");
        }
        System.out.println("Warnings no ficheiro '" + filename + "':");
        for(String warning : warnings)
        {
            System.out.println("- " + warning);
        }
        return servidorBD;
    }
}

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author José Carvalho
 * Classe que representa a estrutura de um servidor principal
 * Data criação: 23/10/2022
 * Data última atualização: 2/11/2022
 */
public class Database
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
     * Construtor da base de dados de um servidor
     */
    public Database()
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
     * @param str Nome do servidor
     * @param enderecos Endereço IPV4
     * @param prioridade Prioridade
     */
    public void addA(String str, Endereco enderecos, Integer prioridade, Integer TTL)
    {
        if(!this.A.containsKey(str))
            this.A.put(str, new ArrayList<>());
        this.A.get(str).add(new Triple<>(enderecos,prioridade,TTL));
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
                "   MX=" + MX;
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
                this.MX.size() > 0 &&
                this.CNAME.size() > 0;
    }

    /**
     * Método que faz o parsing de um ficheiro para um BD
     * @param filename Nome do ficheiro.
     * @return Base de Dados.
     */
    public static Database createBD(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        Map<String,String> macro = new HashMap<>();
        Database servidorBD = new Database();
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
                            case "CNAME"      -> res = servidorBD.addCNAME(dom, converteDom(words[2],macro), TTL);
                            case "MX"         -> res = servidorBD.addMX(dom, words[2], converteInt(words,macro,4), TTL);
                            case "A"          -> servidorBD.addA(dom, Endereco.stringToIP(words[2]), converteInt(words,macro,4), TTL);
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

    /**
     * Método para responder à query que quer obter o campo SOASP
     * @param dominio Domínio que queremos considerar.
     * @return O campo SOASP
     */
    private String getSOASP(String dominio)
    {
        if(dominio.contains(this.SOASP.getValue1()))
            return SOASP.getValue2();
        else
            return "";
    }

    /**
     * Método para responder à query que quer ir buscar o SOAADMIN
     * @param dominio Domínio que queremos considerar
     * @return O campo SOADMIN
     */
    private String getSOAADMIN(String dominio)
    {
        if(dominio.contains(this.SOAADMIN.getValue1()))
            return SOAADMIN.getValue2();
        else
            return "";
    }

    /**
     * Método que devolve a resposta para a query que quer saber o valor de SOASERIAL
     * @param dominio Domínio que queremos considerar
     * @return Valor do SOASERIAL
     */
    private String getSOASERIAL(String dominio)
    {
        if(dominio.contains(this.SOASERIAL.getValue1()))
            return SOASERIAL.getValue2();
        else
            return "";
    }

    /**
     * Método que responde à query que quer obter o campo SOAREFRESH
     * @param dominio Domínio que queremos considerar
     * @return Valor do SOAREFRESH
     */
    private Integer getSOAREFRESH(String dominio)
    {
        if(dominio.contains(this.SOAREFRESH.getValue1()))
            return SOAREFRESH.getValue2();
        else
            return -1;
    }

    /**
     * Método que responde à query que quer obter o campo SOARETRY
     * @param dominio Domínio que queremos considerar
     * @return Valor do SOARETRY
     */
    private Integer getSOARETRY(String dominio)
    {
        if(dominio.contains(this.SOARETRY.getValue1()))
            return SOARETRY.getValue2();
        else
            return -1;
    }

    /**
     * Método que responde à query que quer obter o campo SOAEXPIRE
     * @param dominio Domínio que queremos considerar
     * @return Valor do SOAEXPIRE
     */
    private Integer getSOAEXPIRE(String dominio)
    {
        if(dominio.contains(this.SOAEXPIRE.getValue1()))
            return SOAEXPIRE.getValue2();
        else
            return -1;
    }

    /**
     * Query que devolve todos os endereços URL do server
     * @param dominio Domínio que queremos considerar
     * @return Lista com todos os endereços URL
     */
    private List<String> getNS(String dominio)
    {
        List<String> list = new ArrayList<>();
        for(String key : this.NS.keySet())
        {
            if(key.contains(dominio))
            {
                for (Triple<String, Integer, Integer> triple : this.NS.get(key)) {
                    list.add(triple.getValue1());
                }
            }
        }
        return list;
    }

    /**
     * Query para buscar os endereços IPV4 do servidor.
     * @param dominio Domínio para ir buscar os endereços.
     * @return Set com todos os endereços.
     */
    private Set<Endereco> getA(String dominio)
    {
        Set<Endereco> set = new HashSet<>();
        for(String key : this.A.keySet())
        {
            if(key.contains(dominio))
            {
                for(Triple<Endereco,Integer,Integer> triple : this.A.get(key))
                {
                    set.add(triple.getValue1());
                }
            }
        }
        return set;
    }

    /**
     * Query para obter um nome dando o canónico
     * @param canonico canonico
     * @return O real valor do nome
     */
    private String getCNAME(String canonico)
    {
        if(this.CNAME.containsKey(canonico))
            return CNAME.get(canonico).getValue1();
        else
            return "";
    }

    /**
     * Responde à query MX
     * @param dominio domínio em questão
     * @return Lista com os emails
     */
    private List<String> getMX(String dominio)
    {
        List<String> list = new ArrayList<>();
        for(String key : this.MX.keySet())
        {
            if(key.contains(dominio))
            {
                for (Triple<String, Integer, Integer> triple : this.MX.get(key)) {
                    list.add(triple.getValue1());
                }
            }
        }
        return list;
    }

    /**
     * Método para a base de dados responder a queries
     * @param param dominio da querie
     * @param type campo da resposta
     * @return Par entre booleano e um objeto. O Booleano serve para avaliar se a base de dados
     * soube responder ou não, e o objeto é a resposta.
     */
    public Tuple<Boolean,Object> getInfo(String param, byte type) {
        String resSTR = null;
        Integer resINT = null;
        Collection<String> resLSTR = null;
        Collection<Endereco> resLEND = null;
        switch (type)
        {
            case 0 -> resSTR  = this.getSOASP(param); // SOASP
            case 1 -> resSTR  = this.getSOAADMIN(param); // SOADMIN
            case 2 -> resSTR  = this.getSOASERIAL(param); // SOASERIAL
            case 3 -> resINT  = this.getSOAREFRESH(param); // SOAREFRESH
            case 4 -> resINT  = this.getSOARETRY(param); // SOARETRY
            case 5 -> resINT  = this.getSOAEXPIRE(param); // SOAEXPIRE
            case 6 -> resLSTR = this.getNS(param); // NS
            case 7 -> resLEND = this.getA(param); // A
            case 8 -> resSTR  = this.getCNAME(param); // CNAME
            case 9 -> resLSTR = this.getMX(param); // MX
        }
        boolean resSuc = true;
        Object resObj = null;
        if(resSTR != null)
        {
            if(resSTR.equals(""))
                resSuc = false;
            else
                resObj = resSTR;
        }
        else if (resINT != null)
        {
            if(resINT == -1)
                resSuc = false;
            else
                resObj = resINT;
        }
        else if (resLSTR != null)
        {
            if(resLSTR.size() == 0)
                resSuc = false;
            else
                resObj = resLSTR;
        }
        else if(resLEND != null)
        {
            if(resLEND.size() == 0)
                resSuc = false;
            else
                resObj = resLEND;
        }
        return new Tuple<>(resSuc,resObj);
    }
}

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author José Carvalho
 * Classe que representa a estrutura de um servidor principal
 * Data criação: 23/10/2022
 * Data última atualização: 7/11/2022
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
    private final Map<String,List<Triple<Endereco,Integer,Integer>>> A;
    /**
     * Indica um nome canónico (ou alias) associado ao nome indicado no
     * parâmetro
     */
    private final Map<String,Tuple<String,Integer>> CNAME;
    /**
     * Indica o nome dum servidor de e-mail para o domínio indicado no parâmetro
     */
    private final Map<String,List<Triple<String,Integer,Integer>>> MX;

    private static final Tuple<Integer,Integer> pri = new Tuple<>(0,255);
    private static final Tuple<Integer,Integer> tem = new Tuple<>(0,Integer.MAX_VALUE);

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
     * Adiciona no campo NS um elemento inserindo o domínio, o servidor para o qual o servidor principal dono desta BD é
     * autoritativo e a prioridade correspondente
     * @param dominio Endereço URL do domínio
     * @param server Endereço URL do servidor
     * @param prioridade Prioridade
     */
    public void addNS(String dominio ,String server, Integer prioridade, Integer TTL) throws Exception {
        if(!this.NS.containsKey(dominio))
            this.NS.put(dominio,new ArrayList<>());
        List<Triple<String,Integer,Integer>> list = this.NS.get(dominio);
        boolean res = list.stream().noneMatch(t -> t.getValue1().equals(server));
        if(res)
            list.add(new Triple<>(server,TTL,prioridade));
        else
            throw new Exception("Campo NS - Endereço URL repetido para o mesmo domínio");
    }

    /**
     * Adiciona no campo A um elemento inserindo o nome do servidor, o endereço IPV4 do mesmo e a prioridade.
     * @param str Nome do servidor
     * @param endereco Endereço IPV4
     * @param prioridade Prioridade
     */
    public void addA(String str, Endereco endereco, Integer prioridade, Integer TTL) throws Exception {
        if(!this.A.containsKey(str))
            this.A.put(str, new ArrayList<>());
        List<Triple<Endereco,Integer,Integer>> l = this.A.get(str);
        boolean res = l.stream().noneMatch(t -> t.getValue1().equals(endereco));
        if(res)
            this.A.get(str).add(new Triple<>(endereco,TTL,prioridade));
        else
            throw new Exception("Campo A - Endereco IP repetido para o mesmo endereço URL");
    }

    /**
     * Adiciona a associação nome canónico a um nome
     * @param canonico Nome canónico
     * @param nome Nome
     */
    public void addCNAME(String canonico, String nome, Integer TTL) throws Exception
    {
        if(this.CNAME.containsKey(nome))
            throw new Exception("Campo CNAME - Um canónio não pode ser um canónico de um canónico");
        if(!this.A.containsKey(nome))
            throw new Exception("Campo CNAME - Não há endereço IP definido para o endereço " + nome);
        this.CNAME.put(canonico,new Tuple<>(nome, TTL));
    }

    /**
     * Adiciona o nome do servidor de email para o domínio presente com a prioridade
     * @param email Email
     * @param prioridade Prioridade
     */
    public void addMX(String dominio,String email, Integer prioridade, Integer TTL) throws Exception {
        if(!this.MX.containsKey(dominio))
            this.MX.put(dominio,new ArrayList<>());
        List<Triple<String,Integer,Integer>> list = this.MX.get(dominio);
        boolean res = list.stream().noneMatch(t -> t.getValue1().equals(email));
        if(res)
            list.add(new Triple<>(email,TTL,prioridade));
        else
            throw new Exception("Campo MX - Endereço de email repetido para o mesmo domínio");
    }
    /**
     * Adiciona o par dominio e o respetivo endereço URL do servidor principal na BD.
     * @param dominio Domínio
     * @param SOASP URL do Servidor principal
     */
    public void setSOASP(String dominio, String SOASP, Integer TTL) throws Exception {
        if(this.SOASP == null)
            this.SOASP = new Triple<>(dominio,SOASP, TTL);
        else
            throw new Exception("Campo SOASP já definido");
    }

    /**
     * Adiciona o par dominio e o respetivo email do admin do servidor principal na BD.
     * @param dominio  Domínio do SP
     * @param SOAADMIN Email do admin
     */
    public void setSOAADMIN(String dominio, String SOAADMIN,Integer TTL) throws Exception {
        if(this.SOAADMIN == null)
            this.SOAADMIN = new Triple<>(dominio,SOAADMIN, TTL);
        else
            throw new Exception("Campo SOAADMIN já definido");
    }

    /**
     * Estabele a ligação entre domínio e o número de serie da BD
     * @param dominio  Domínio do SP
     * @param SOASERIAL Número de serie
     */
    public void setSOASERIAL(String dominio, String SOASERIAL, Integer TTL) throws Exception {
        if(this.SOASERIAL == null)
            this.SOASERIAL = new Triple<>(dominio,SOASERIAL, TTL);
        else
            throw new Exception("Campo SOASERIAL já definido");
    }

    /**
     * Faz a ligação entre o domínio e a intervalo temporal até o SS pedir ao SP o número de serie
     * @param dominio  Domínio do SP
     * @param SOAREFRESH Intervalo temporal
     */
    public void setSOAREFRESH(String dominio, Integer SOAREFRESH, Integer TTL) throws Exception {
        if(this.SOAREFRESH == null)
            this.SOAREFRESH = new Triple<>(dominio, SOAREFRESH, TTL);
        else
            throw new Exception("Campo SOAREFRESH já definido");
    }

    /**
     * Faz a ligação entre o domínio e a intervalo temporal até o SS pedir ao SP o número de serie em caso de timeout
     * @param dominio  Domínio do SP
     * @param SOARETRY Intervalo temporal
     */
    public void setSOARETRY(String dominio, Integer SOARETRY, Integer TTL) throws Exception {
        if(this.SOARETRY == null)
            this.SOARETRY = new Triple<>(dominio,SOARETRY, TTL);
        else
            throw new Exception("Campo SOARETRY já definido");
    }

    /**
     * Faz a ligação entre o domínio e a intervalo temporal até o SS pedir ao SP o número de serie por desconfiar da sua réplica
     * @param dominio Domínio do SP
     * @param SOAEXPIRE Intervalo temporal
     */
    public void setSOAEXPIRE(String dominio, Integer SOAEXPIRE,Integer TTL) throws Exception {
        if(this.SOAEXPIRE == null)
            this.SOAEXPIRE = new Triple<>(dominio,SOAEXPIRE, TTL);
        else
            throw new Exception("Campo SOAEXPIRE já definido");
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
     * @param index Indice da string no array words que pretendemos converter para inteiro.
     * @param campo Campo que queremos ir buscar o inteiro. Esta string serve para saber quais os limites.
     * @return Inteiro Convertido
     */
    private static int converteInt(String[] words, Map<String,String> macro, int index, String campo) throws Exception {
        Tuple<Integer,Integer> tuple;
        if(campo.equals("'Prioridade'"))
            tuple = pri;
        else
            tuple = tem;
        int min = tuple.getValue1();
        int max = tuple.getValue2();
        int num;
        if(index < words.length)
        {
            String word = words[index];
            try
            {
                num = Integer.parseInt(word);
            }
            catch (NumberFormatException e)
            {
                String str = macro.get(word);
                if(str != null)
                {
                    try {
                        num = Integer.parseInt(str);
                    }
                    catch (NumberFormatException exp)
                    {
                        throw new Exception("Valor não inteiro para a macro " + word);
                    }
                }
                else
                {
                    throw new Exception("Valor não é inteiro e não está definido nas macros");
                }
            }
            if(num < min || num > max)
                throw new Exception("Número que excede o intervalo estabelecidos para o campo " + campo + ". O intervalo é [" + min + "," + max + "]");

        }
        else
            throw new Exception("Não respeita a sintaxe.");
       return num;
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
            if(str.length() > 0 && str.charAt(0) != '#'&& words.length > 2)
            {
                if(words[1].equals("DEFAULT"))
                    macro.put(words[0], words[2]);
                else if(words.length > 3)
                {
                    try
                    {
                        String dom = converteDom(words[0], macro);
                        Integer TTL = converteInt(words, macro,3, "'TTL'");
                        switch (words[1])
                        {
                            case "SOASP"      : servidorBD.setSOASP(dom, words[2], TTL); break;
                            case "SOAADMIN"   : servidorBD.setSOAADMIN(dom, words[2], TTL); break;
                            case "SOASERIAL"  : servidorBD.setSOASERIAL(dom, words[2], TTL); break;
                            case "SOAREFRESH" : servidorBD.setSOAREFRESH(dom, converteInt(words,macro,2, "'Tempo'"), TTL); break;
                            case "SOARETRY"   : servidorBD.setSOARETRY(dom, converteInt(words,macro,2, "'Tempo'"), TTL); break;
                            case "SOAEXPIRE"  : servidorBD.setSOAEXPIRE(dom, converteInt(words,macro,2 ,"'Tempo'"), TTL); break;
                            case "NS"         : servidorBD.addNS(dom, words[2], converteInt(words,macro,4,"'Prioridade'"), TTL); break;
                            case "CNAME"      : servidorBD.addCNAME(dom, converteDom(words[2],macro), TTL); break;
                            case "MX"         : servidorBD.addMX(dom, words[2], converteInt(words,macro,4,"'Prioridade'"), TTL); break;
                            case "A"          : servidorBD.addA(dom, Endereco.stringToIP(words[2]), converteInt(words,macro,4,"'Prioridade'"), TTL); break;
                            default           : warnings.add("Erro linha " + l + ": Tipo de valor não identificado na linha " + l); break;
                        }
                    }
                    catch (Exception e)
                    {
                        warnings.add("Erro linha " + l + ": " + e.getMessage());
                    }
                }
                else
                    warnings.add("Erro linha " + l + ": Não respeita a sintaxe.");
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
    private Value[] getSOASP(String dominio, byte b)
    {
        Value[] res = null;
        if(dominio.matches("(.*)" + this.SOASP.getValue1()))
        {
            res = new Value[1];
            res[0] = new Value(dominio,b,SOASP.getValue2(),SOASP.getValue3());
        }
        return res;
    }

    /**
     * Método para responder à query que quer ir buscar o SOAADMIN
     * @param dominio Domínio que queremos considerar
     * @return O campo SOADMIN
     */
    private Value[] getSOAADMIN(String dominio, byte b)
    {
        Value[] res = null;
        if(dominio.matches("(.*)" + this.SOASP.getValue1()))
        {
            res = new Value[1];
            res[0] = new Value(dominio,b,SOAADMIN.getValue2(),SOAADMIN.getValue3());
        }
        return res;
    }

    /**
     * Método que devolve a resposta para a query que quer saber o valor de SOASERIAL
     * @param dominio Domínio que queremos considerar
     * @return Valor do SOASERIAL
     */
    private Value[] getSOASERIAL(String dominio, byte b)
    {
        Value[] res = null;
        if(dominio.matches("(.*)" + this.SOASP.getValue1()))
        {
            res = new Value[1];
            res[0] = new Value(dominio,b,SOASERIAL.getValue2(),SOASERIAL.getValue3());
        }
        return res;
    }

    /**
     * Método que responde à query que quer obter o campo SOAREFRESH
     * @param dominio Domínio que queremos considerar
     * @return Valor do SOAREFRESH
     */
    private Value[] getSOAREFRESH(String dominio, byte b)
    {
        Value[] res = null;
        if(dominio.matches("(.*)" + this.SOASP.getValue1()))
        {
            res = new Value[1];
            String val = SOAREFRESH.getValue2().toString();
            res[0] = new Value(dominio,b,val,SOAREFRESH.getValue3());
        }
        return res;
    }

    /**
     * Método que responde à query que quer obter o campo SOARETRY
     * @param dominio Domínio que queremos considerar
     * @return Valor do SOARETRY
     */
    private Value[] getSOARETRY(String dominio, byte b)
    {
        Value[] res = null;
        if(dominio.matches("(.*)" + this.SOASP.getValue1()))
        {
            res = new Value[1];
            String val = SOARETRY.getValue2().toString();
            res[0] = new Value(dominio,b,val,SOARETRY.getValue3());
        }
        return res;
    }

    /**
     * Método que responde à query que quer obter o campo SOAEXPIRE
     * @param dominio Domínio que queremos considerar
     * @return Valor do SOAEXPIRE
     */
    private Value[] getSOAEXPIRE(String dominio, byte b)
    {
        Value[] res = null;
        if(dominio.matches("(.*)" + this.SOASP.getValue1()))
        {
            res = new Value[1];
            String val = SOAEXPIRE.getValue2().toString();
            res[0] = new Value(dominio,b,val,SOAEXPIRE.getValue3());
        }
        return res;
    }

    /**
     * Query que devolve todos os endereços URL do server
     * @param dominio Domínio que queremos considerar
     * @return Lista com todos os endereços URL
     */
    private Value[] getNS(String dominio, byte b)
    {
        List<Value> list = new ArrayList<>();
        for(String key : this.NS.keySet())
        {
            if(key.matches("(.*)" + dominio))
            {
                for (Triple<String, Integer, Integer> triple : this.NS.get(key)) {
                    list.add(new Value(dominio,b,triple.getValue1(), triple.getValue2(),triple.getValue3()));
                }
            }
        }
        return list.toArray(new Value[0]);
    }

    /**
     * Query para buscar os endereços IPV4 do servidor.
     * @param dominio Domínio para ir buscar os endereços.
     * @return Set com todos os endereços.
     */
    private Value[] getA(String dominio, byte b)
    {
        List<Value> list = new ArrayList<>();
        for(String key : this.A.keySet())
        {
            if(key.matches("(.*)" + dominio))
            {
                for(Triple<Endereco,Integer,Integer> triple : this.A.get(key))
                {
                    list.add(new Value(dominio,b,triple.getValue1().toString(),triple.getValue2(), triple.getValue3()));
                }
            }
        }
        return list.toArray(new Value[0]);
    }

    /**
     * Query para obter um nome dando o canónico
     * @param canonico canonico
     * @return O real valor do nome
     */
    private Value[] getCNAME(String canonico, byte b)
    {
        Value[] res = null;
        if(this.CNAME.containsKey(canonico))
        {
            res = new Value[1];
            res[0] = new Value(canonico,b,CNAME.get(canonico).getValue1() ,CNAME.get(canonico).getValue2());
        }
        return res;
    }

    /**
     * Responde à query MX
     * @param dominio domínio em questão
     * @return Lista com os emails
     */
    private Value[] getMX(String dominio, byte b)
    {
        List<Value> list = new ArrayList<>();
        for(String key : this.MX.keySet())
        {
            if(key.matches("(.*)" + dominio))
            {
                for (Triple<String, Integer, Integer> triple : this.MX.get(key)) {
                    list.add(new Value(dominio,b,triple.getValue1(), triple.getValue2(), triple.getValue3()));
                }
            }
        }
        return list.toArray(new Value[0]);
    }

    /**
     * Método para a base de dados responder a queries
     * @param param dominio da querie
     * @param type campo da resposta
     * @return Um array com a informação. Se a informação não existir, iremos devolver um
     * apontador para null.
     */
    public Value[] getInfo(String param, byte type)
    {
        Value[] res = null;
        switch (type)
        {
            case 0: res = this.getSOASP(param,type); break;
            case 1 : res = this.getSOAADMIN(param,type); break;
            case 2 : res = this.getSOASERIAL(param,type); break;
            case 3 : res = this.getSOAREFRESH(param,type);break;
            case 4 : res = this.getSOARETRY(param,type);break;
            case 5 : res = this.getSOAEXPIRE(param,type);break;
            case 6 : res = this.getNS(param,type); break;
            case 7 : res = this.getA(param,type); break;
            case 8 : res = this.getCNAME(param,type); break;
            case 9 : res = this.getMX(param,type); break;
        }
        return res;
    }
}

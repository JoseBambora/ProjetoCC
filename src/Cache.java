import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @author José Carvalho
 * Classe que representa a estrutura de uma cache dos servidores
 * Algoritmo usado: Least Recently Used (LRU)
 * Data criação: 29/10/2022
 * Data última atualização: 11/11/2022
 */
public class Cache
{
    private static final Tuple<Integer,Integer> pri = new Tuple<>(0,255);
    private static final Tuple<Integer,Integer> tem = new Tuple<>(0,Integer.MAX_VALUE);
    private final Map<String,EntryCache> cache;
    private Map<String, Byte> aux;
    private int espaco;
    private int answers;
    public Cache(int espaco)
    {
        aux = new HashMap<>();
        this.cache = new HashMap<>();
        this.espaco = espaco;
        this.answers = 0;
    }
    public void setEspaco(int espaco)
    {
        this.espaco = espaco;
    }

    public void addData(DNSPacket resposta, EntryCache.Origin origin)
    {
        this.removeExpireInfo();
        Data data = resposta.getData();
        EntryCache entryCache = new EntryCache(resposta,origin);
        if(this.answers == this.espaco)
            this.removeData();
        if(!this.cache.containsKey(entryCache.getKey()))
        {
            this.answers++;
            this.cache.put(entryCache.getKey(),entryCache);
        }
        else if(origin == EntryCache.Origin.OTHERS)
            this.cache.get(entryCache.getKey()).setTempoEntrada(LocalDateTime.now());
    }
    public void addData(String dom, byte type, Value valor, EntryCache.Origin origin) throws Exception
    {
        EntryCache entryCache = new EntryCache(dom,type,origin);
        if(!this.cache.containsKey(entryCache.getKey()))
            this.cache.put(entryCache.getKey(),entryCache);
        this.cache.get(entryCache.getKey()).addValueDB(valor);
    }
    public void removeExpireInfo()
    {
        this.cache.values().forEach(EntryCache::removeExpireInfo);
    }
    public void removeData()
    {
        if(this.answers == this.espaco)
        {
            List<EntryCache> remove = new ArrayList<>(this.cache.values());
            remove.sort((t1,t2) -> (int) ChronoUnit.NANOS.between(t2.getTempoEntrada(),t1.getTempoEntrada()));
            for(int i = 0; i < this.espaco/2; i++)
                this.cache.remove(remove.get(i).getKey());
            this.answers -= (this.espaco/2);
        }
    }
    public Data findAnswer(DNSPacket mensagem)
    {
        String name = mensagem.getData().getName();
        byte b = mensagem.getData().getTypeOfValue();
        return this.findAnswer(name,b);
    }

    public Data findAnswer(String dom, byte type)
    {
        EntryCache entryCache = new EntryCache(dom,type, EntryCache.Origin.SP);
        Data res = null;
        if(this.cache.containsKey(entryCache.getKey()))
            res = this.cache.get(entryCache.getKey()).getData();
        else
        {
            try {
                entryCache = new EntryCache("DB", DNSPacket.typeOfValueConvert("CNAME"),EntryCache.Origin.SP);
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }
            if(this.cache.containsKey(entryCache.getKey()))
            {
                Value[] values = this.cache.get(entryCache.getKey()).getData().getResponseValues();
                for(Value value : values)
                {
                    if(value.getDominio().equals(dom))
                        res = this.findAnswer(value.getValue(),type);
                }
            }
        }
        return res;
    }

    // exclusiva dos SS
    public void removeByName(String name)
    {
        for(EntryCache val : this.cache.values())
            if(val.getDominio().equals(name))
                this.cache.remove(val.getKey());
    }

    public boolean checkBD()
    {
        try
        {
            Map<Byte,Integer> counter = new HashMap<>();
            counter.put(DNSPacket.typeOfValueConvert("SOASP"),0);
            counter.put(DNSPacket.typeOfValueConvert("SOAADMIN"),0);
            counter.put(DNSPacket.typeOfValueConvert("SOASERIAL"),0);
            counter.put(DNSPacket.typeOfValueConvert("SOAREFRESH"),0);
            counter.put(DNSPacket.typeOfValueConvert("SOARETRY"),0);
            counter.put(DNSPacket.typeOfValueConvert("SOAEXPIRE"),0);
            counter.put(DNSPacket.typeOfValueConvert("NS"),0);
            counter.put(DNSPacket.typeOfValueConvert("CNAME"),0);
            counter.put(DNSPacket.typeOfValueConvert("MX"),0);
            counter.put(DNSPacket.typeOfValueConvert("A"),0);
            counter.put(DNSPacket.typeOfValueConvert("PTR"),0);
            for(EntryCache entryCache : this.cache.values())
            {
                if(entryCache.getOrigem() == EntryCache.Origin.FILE)
                {
                    counter.put(entryCache.getTypeofValue(), counter.get(entryCache.getTypeofValue())+1);
                }
            }
            return counter.values().stream().allMatch(b -> b > 0);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return true;
    }


    /**
     * Método auxiliar ao parsing, de forma a retornar o valor correto do endereço URL.
     * Quando o endereço já termina com o '.' este método não faz nada.
     * @param str Endereço a converter.
     * @param macro Mapeamento que contém as macros guardados. É util para ir buscar a marco "@".
     * @return Endereço URL final.
     */
    private String converteDom(String str, Map<String,String> macro)
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
    private int converteInt(String[] words, Map<String,String> macro, int index, String campo) throws Exception {
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
    private void addValue(Value value)
    {
        EntryCache entryCache = new EntryCache(value.getDominio(),value.getType(), EntryCache.Origin.FILE);
        if(!this.cache.containsKey(entryCache.getKey()))
            this.cache.put(entryCache.getKey(), entryCache);
        this.cache.get(entryCache.getKey()).addValueDB(value);
    }
    private void addFSTValueBD(Map<String,List<Value>> valores, String type)
    {
        this.addValue(valores.get(type).get(0));
    }
    private void addAllValueBD(Map<String,List<Value>> valores, String type)
    {
        for(Value value : valores.get(type))
            this.addValue(value);
    }
    private void addCnameBD(Map<String,List<Value>> valores, List<String> warnings)
    {
        List<Value> values = valores.get("CNAME");
        for(Value value : values)
            if(valores.get("A").stream().anyMatch( e -> e.getDominio().equals(value.getValue())))
                this.addValue(value);
            else
                warnings.add("CNAME " + value.getDominio() + " não guardado pois " +value.getValue() + " não existe");

    }
    private void converteBD(Map<String,List<Value>> valores, List<String> warnings)
    {
        addFSTValueBD(valores,"SOASP");
        addFSTValueBD(valores,"SOAADMIN");
        addFSTValueBD(valores,"SOASERIAL");
        addFSTValueBD(valores,"SOAREFRESH");
        addFSTValueBD(valores,"SOARETRY");
        addFSTValueBD(valores,"SOAEXPIRE");
        addAllValueBD(valores,"NS");
        addAllValueBD(valores,"MX");
        addAllValueBD(valores,"A");
        addAllValueBD(valores,"PTR");
        addCnameBD(valores,warnings);
    }
    private void addValor(Map<String,List<Value>> valores,String dom, String type, String name, Integer TTL) throws Exception
    {
        if(valores.get(type).size() > 0)
            throw new Exception("Valor de " + type + " repetido.");
        else
        {
            Value value = new Value(dom, aux.get(type), name, TTL);
            valores.get(type).add(value);
        }
    }
    private void addValores(Map<String,List<Value>> valores, String dom,String type, String name, Value value) throws Exception
    {
        List<Value> values = valores.get(type);
        if (values.stream().anyMatch(v -> v.getDominio().equals(dom) && (type.equals("A") || v.getValue().equals(name))))
            throw new Exception("Valor do " + type + " repetido para o mesmo domínio");
        else
            valores.get(type).add(value);
    }

    private void addValoresCNAME(Map<String,List<Value>> valores, String dom,String type, String name, Value value) throws Exception
    {
        List<Value> values = valores.get(type);
        if (values.stream().anyMatch(v -> v.getDominio().equals(dom)))
            throw new Exception("Valor do " + type + " repetido para o mesmo domínio");
        if (values.stream().anyMatch(v -> v.getDominio().equals(name)))
            throw new Exception("Valor do " + type + " a apontar para outro CNAME");
        valores.get(type).add(value);

    }

    private void addValoresPri(Map<String,List<Value>> valores, String[]words, String dom, String type,String name, Integer TTL, Map<String,String> macro) throws Exception
    {
        Value value;
        if(words.length > 4)
        {
            int prioridade = converteInt(words, macro, 4, "'Prioridade'");
            value = new Value(dom, aux.get(type), name, TTL, prioridade);
        }
        else
            value = new Value(dom, aux.get(words[1]), name, TTL);
        addValores(valores,dom,type,name,value);

    }

    /**
     * Método que faz o parsing de um ficheiro para um BD.
     * @param lines Linhas de um ficheiro.
     * @return Base de Dados.
     */
    public void createBD(String[] lines)
    {
        Map<String, String> macro = new HashMap<>();
        Map<String, List<Value>> valores = new HashMap<>();
        List<String> warnings = new ArrayList<>();
        int l = 1;
        try
        {
            valores.put("SOASP",new ArrayList<>());
            valores.put("SOAADMIN",new ArrayList<>());
            valores.put("SOASERIAL",new ArrayList<>());
            valores.put("SOAREFRESH",new ArrayList<>());
            valores.put("SOARETRY",new ArrayList<>());
            valores.put("SOAEXPIRE",new ArrayList<>());
            valores.put("NS",new ArrayList<>());
            valores.put("CNAME",new ArrayList<>());
            valores.put("MX",new ArrayList<>());
            valores.put("A",new ArrayList<>());
            valores.put("PTR",new ArrayList<>());
            aux.put("SOASP", DNSPacket.typeOfValueConvert("SOASP"));
            aux.put("SOAADMIN", DNSPacket.typeOfValueConvert("SOAADMIN"));
            aux.put("SOASERIAL", DNSPacket.typeOfValueConvert("SOASERIAL"));
            aux.put("SOAREFRESH", DNSPacket.typeOfValueConvert("SOAREFRESH"));
            aux.put("SOARETRY", DNSPacket.typeOfValueConvert("SOARETRY"));
            aux.put("SOAEXPIRE", DNSPacket.typeOfValueConvert("SOAEXPIRE"));
            aux.put("NS", DNSPacket.typeOfValueConvert("NS"));
            aux.put("CNAME", DNSPacket.typeOfValueConvert("CNAME"));
            aux.put("MX", DNSPacket.typeOfValueConvert("MX"));
            aux.put("A", DNSPacket.typeOfValueConvert("A"));
            aux.put("PTR", DNSPacket.typeOfValueConvert("PTR"));
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        for (String str : lines)
        {
            String[] words = str.split(" ");
            if (str.length() > 0 && str.charAt(0) != '#' && words.length > 2) {
                if (words[1].equals("DEFAULT"))
                    macro.put(words[0], words[2]);
                else if (words.length > 3) {
                    try
                    {
                        String dom = converteDom(words[0], macro);
                        int TTL = converteInt(words, macro, 3, "'TTL'");
                        switch (words[1]) {
                            case "SOASP":
                            case "SOAADMIN":
                                String name = converteDom(words[2], macro);
                                addValor(valores,dom,words[1],name,TTL);
                                break;
                            case "SOASERIAL":
                            case "SOAREFRESH":
                            case "SOARETRY":
                            case "SOAEXPIRE":
                                addValor(valores,dom,words[1],words[2],TTL);
                                break;
                            case "PTR":
                                Value value = new Value(dom, aux.get(words[1]), words[2], TTL);
                                addValores(valores,dom,words[1],words[2],value);
                                break;
                            case "CNAME":
                                name = converteDom(words[2], macro);
                                value = new Value(dom, aux.get(words[1]), name, TTL);
                                addValoresCNAME(valores,dom,words[1],name,value);
                                break;
                            case "NS":
                            case "MX":
                                name = converteDom(words[2], macro);
                                addValoresPri(valores,words,dom,words[1],name,TTL,macro);
                                break;
                            case "A":
                                addValoresPri(valores,words,dom,words[1],words[2],TTL,macro);
                                break;
                            default:
                                warnings.add("Erro linha " + l + ": Tipo de valor não identificado.");
                                break;
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
        converteBD(valores,warnings);
        System.out.println("Warnings criação BD");
        for (String warning : warnings)
        {
            System.out.println("- " + warning);
        }
    }

    /**
     * Método que faz o parsing de um ficheiro para um BD
     * @param filename Nome do ficheiro.
     * @return Base de Dados.
     */
    public void createBD(String filename) throws IOException
    {
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        this.createBD(lines.toArray(new String[1]));
        EntryCache entryCache = new EntryCacheDBF("",filename);
        this.cache.put(entryCache.getKey(),entryCache);
    }
    public void createBD(String filename,String dom) throws IOException
    {
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        this.createBD(lines.toArray(new String[1]));
        EntryCache entryCache = new EntryCacheDBF(dom,filename);
        this.cache.put(entryCache.getKey(),entryCache);
    }
    @Override
    public String toString()
    {
        StringBuilder res = new StringBuilder();
        for(EntryCache entryCache : this.cache.values())
        {
            res.append(entryCache.toString()).append("\n");
        }
        return res.toString();
    }
}

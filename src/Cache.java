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
 * Data última atualização: 10/11/2022
 */
public class Cache
{
    private static final Tuple<Integer,Integer> pri = new Tuple<>(0,255);
    private static final Tuple<Integer,Integer> tem = new Tuple<>(0,Integer.MAX_VALUE);
    private final Map<String,EntryCache> cache;
    private int espaco;
    private int answers;
    public Cache(int espaco)
    {
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
    public void addData(String dom, byte type, Value valor, EntryCache.Origin origin)
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
        EntryCache entryCache = new EntryCache(mensagem, EntryCache.Origin.SP);
        Data res = null;
        if(this.cache.containsKey(entryCache.getKey()))
            res = this.cache.get(entryCache.getKey()).getData();
        return res;
    }

    public Data findAnswer(String dom, byte type)
    {
        EntryCache entryCache = new EntryCache(dom,type, EntryCache.Origin.SP);
        Data res = null;
        if(this.cache.containsKey(entryCache.getKey()))
            res = this.cache.get(entryCache.getKey()).getData();
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
        Map<Byte,Boolean> map = new HashMap<>();
        try
        {
            map.put(DNSPacket.typeOfValueConvert("SOASP"),false);
            map.put(DNSPacket.typeOfValueConvert("SOAADMIN"),false);
            map.put(DNSPacket.typeOfValueConvert("SOASERIAL"),false);
            map.put(DNSPacket.typeOfValueConvert("SOAREFRESH"),false);
            map.put(DNSPacket.typeOfValueConvert("SOARETRY"),false);
            map.put(DNSPacket.typeOfValueConvert("SOAEXPIRE"),false);
            map.put(DNSPacket.typeOfValueConvert("NS"),false);
            map.put(DNSPacket.typeOfValueConvert("CNAME"),false);
            map.put(DNSPacket.typeOfValueConvert("MX"),false);
            map.put(DNSPacket.typeOfValueConvert("A"),false);
            map.put(DNSPacket.typeOfValueConvert("PTR"),false);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        for(EntryCache entryCache : this.cache.values())
        {
            if(entryCache.getOrigem() == EntryCache.Origin.FILE)
            {
                map.put(entryCache.getTypeofValue(),true);
            }
        }
        return map.values().stream().allMatch(b -> b);
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
    /**
     * Método que faz o parsing de um ficheiro para um BD
     * @param filename Nome do ficheiro.
     * @return Base de Dados.
     */
    public void createBD(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        Map<String,String> macro = new HashMap<>();
        List<String> warnings = new ArrayList<>();
        Map<String,Byte> aux = new HashMap<>();
        EntryCache entryCache = new EntryCacheDBF("",filename);
        this.cache.put(entryCache.getKey(),entryCache);
        int l = 1;
        try
        {
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
        System.out.println(aux);
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
                        int TTL = converteInt(words, macro,3, "'TTL'");
                        switch (words[1])
                        {
                            case "SOASP"      :
                            case "SOAADMIN"   :
                            case "SOASERIAL"  :
                            case "SOAREFRESH" :
                            case "SOARETRY"   :
                            case "SOAEXPIRE"  :
                            case "PTR"        :
                                this.addData(dom, aux.get(words[1]), new Value(dom,aux.get(words[1]),words[2],TTL), EntryCache.Origin.FILE); break;
                            case "CNAME"      :
                                String name = converteDom(words[2], macro);
                                this.addData(dom, aux.get(words[1]), new Value(dom,aux.get(words[1]),name,TTL), EntryCache.Origin.FILE); break;
                            case "NS"         :
                            case "MX"         :
                            case "A"          :
                                int prioridade = converteInt(words,macro,4,"'Prioridade'");
                                this.addData(dom, aux.get(words[1]), new Value(dom,aux.get(words[1]),words[2],TTL,prioridade), EntryCache.Origin.FILE); break;
                            default           : warnings.add("Erro linha " + l + ": Tipo de valor não identificado."); break;
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
        if(!this.checkBD())
        {
            warnings.add("BD não criada");
        }
        // campos em falta
        System.out.println("Warnings no ficheiro '" + filename + "':");
        for(String warning : warnings)
        {
            System.out.println("- " + warning);
        }
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

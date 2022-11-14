import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author José Carvalho
 * Classe que representa a estrutura de uma cache dos servidores
 * Algoritmo usado: Least Recently Used (LRU)
 * Data criação: 29/10/2022
 * Data última atualização: 12/11/2022
 */
public class Cache
{
    private static final Tuple<Integer,Integer> pri = new Tuple<>(0,255);
    private static final Tuple<Integer,Integer> tem = new Tuple<>(0,Integer.MAX_VALUE);
    private final Map<String,EntryCache> cache;
    private final Map<String, Byte> aux;
    private String dominio = ""; // SÓ PARA SP
    public Cache()
    {
        this.aux = new HashMap<>();
        this.cache = new HashMap<>();
    }

    /**
     * Adiciona uma entrada à cache.
     * @param entryCache Entrada a adicionar.
     */
    private void addDataCache(EntryCache entryCache)
    {
        if(!this.cache.containsKey(entryCache.getKey()))
        {
            this.cache.put(entryCache.getKey(),entryCache);
        }
        else if(entryCache.getOrigem() == EntryCache.Origin.OTHERS)
            this.cache.get(entryCache.getKey()).setTempoEntrada(LocalDateTime.now());
    }

    /**
     * Adicionar um determinado valor à cache.
     * @param value Valor a adicionar à cache.
     * @param origin Origem da mensagem, SP se for transferência de zona, OTHERs no resto.
     */
    public void addData(Value value, EntryCache.Origin origin)
    {
        this.removeExpireInfo();
        EntryCache entryCache = new EntryCache(value.getDominio(),value.getType(),origin);
        this.addDataCache(entryCache);
    }
    /**
     * Adicionar dados de um pacote recebido. Usado para transferência de zona e para receção
     * de respostas a queries.
     * @param resposta Resposta à query.
     * @param origin Origem da mensagem, SP se for transferência de zona, OTHERs no resto.
     */
    public void addData(DNSPacket resposta, EntryCache.Origin origin)
    {
        this.removeExpireInfo();
        EntryCache entryCache = new EntryCache(resposta,origin);
        this.addDataCache(entryCache);
    }

    /**
     * Remove informação que já está expirada da cache.
     */
    public void removeExpireInfo()
    {
        this.cache.values().forEach(EntryCache::removeExpireInfo);
        for(EntryCache entryCache : this.cache.values())
        {
            if(entryCache.isEmpty())
                this.cache.remove(entryCache.getKey());
        }
    }

    /**
     * Procura resposta a uma dada query, dando o domínio da query e o tipo.
     * @param dom Domínio da pergunta.
     * @param type Tipo da pergunta.
     * @return Null se resposta não for encontrada, ou Data se for encontrada.
     */
    private Data getAnswer(String dom, byte type)
    {
        EntryCache entryCache = new EntryCache(dom,type, EntryCache.Origin.SP);
        Data res = null;
        if(this.cache.containsKey(entryCache.getKey()))
        {
            EntryCache entryCache1 = this.cache.get(entryCache.getKey());
            res = entryCache1.getData();
            if(entryCache1.getOrigem() == EntryCache.Origin.FILE)
            {
                List<Value> av = new ArrayList<>();
                List<Value> ev = new ArrayList<>();
                for(EntryCache entryCache2 : this.cache.values())
                {
                    if(entryCache2.getTypeofValue().equals(aux.get("NS")) && entryCache2.getDominio().matches("(.*)" + this.dominio))
                    {
                        Data data = entryCache2.getData();
                        av.addAll(List.of(data.getResponseValues()));
                    }
                    else if(entryCache2.getTypeofValue().equals(aux.get("A")) && entryCache2.getDominio().matches("(.*)" + this.dominio))
                    {
                        Data data = entryCache2.getData();
                        ev.addAll(List.of(data.getResponseValues()));
                    }
                }
                if(!av.isEmpty())
                    res.setAuthoriteValues(av.toArray(new Value[1]));
                if(!ev.isEmpty())
                    res.setExtraValues(ev.toArray(new Value[1]));
            }

        }
        return res;
    }

    /**
     * Procura resposta a uma dada query, dando o domínio da query e o tipo.
     * @param dom Domínio da pergunta.
     * @param type Tipo da pergunta.
     * @return Null se resposta não for encontrada, ou Data se for encontrada.
     */
    public Data findAnswer(String dom, byte type)
    {
        Data res = getAnswer(dom,type);
        if(res == null)
        {
            // Caso de ser SP -> vai ve correspondência no CNAME.
            for(EntryCache entryCache1 : this.cache.values())
            {
                String str = entryCache1.getNameCNAME(dom,aux.get("CNAME"));
                if(str.length() > 0)
                    res = getAnswer(str,type);
            }
        }
        return res;
    }

    /**
     * Procura resposta quando é recebida um pacote DNS.
     * @param mensagem Query dns.
     * @return Null se resposta não for encontrada, ou Data se for encontrada.
     */
    public Data findAnswer(DNSPacket mensagem)
    {
        String name = mensagem.getData().getName();
        byte b = mensagem.getData().getTypeOfValue();
        return this.findAnswer(name,b);
    }

    /**
     * Método exclusivo dos SS.
     * @param name Domínio que queremos remover da cache.
     */
    public void removeByName(String name)
    {
        for(EntryCache val : this.cache.values())
            if(val.getDominio().equals(name))
                this.cache.remove(val.getKey());
    }

    /**
     * Verifica se todos os campos da cache estão completos.
     * @param type Tipo do servidor (SP, ST, REVERSE)
     * @return true se não faltar campos, false caso contrário.
     */
    public boolean checkBD(String type)
    {
        try
        {
            Map<Byte,Integer> counter = new HashMap<>();
            counter.put(Data.typeOfValueConvert("SOASP"),0);
            counter.put(Data.typeOfValueConvert("SOAADMIN"),0);
            counter.put(Data.typeOfValueConvert("SOASERIAL"),0);
            counter.put(Data.typeOfValueConvert("SOAREFRESH"),0);
            counter.put(Data.typeOfValueConvert("SOARETRY"),0);
            counter.put(Data.typeOfValueConvert("SOAEXPIRE"),0);
            counter.put(Data.typeOfValueConvert("NS"),0);
            counter.put(Data.typeOfValueConvert("CNAME"),0);
            counter.put(Data.typeOfValueConvert("MX"),0);
            counter.put(Data.typeOfValueConvert("A"),0);
            counter.put(Data.typeOfValueConvert("PTR"),0);
            for(EntryCache entryCache : this.cache.values())
            {
                if(entryCache.getOrigem() == EntryCache.Origin.FILE && entryCache.getTypeofValue() != -1)
                {
                    counter.put(entryCache.getTypeofValue(), counter.get(entryCache.getTypeofValue())+1);
                }
            }
            boolean res = false;
            switch (type)
            {
                case "SP":
                    res = counter.keySet().stream().allMatch(b -> b.equals(aux.get("PTR")) || b.equals(aux.get("CNAME")) || counter.get(b) > 0);
                    break;
                case "ST":
                    res =  counter.keySet().stream().allMatch(b -> (!b.equals(aux.get("NS")) && !b.equals(aux.get("A"))) || counter.get(b) > 0);
                    break;
                case "REVERSE":
                    res =  counter.keySet().stream().allMatch(b -> (!b.equals(aux.get("NS")) && !b.equals(aux.get("PTR"))) || counter.get(b) > 0);
                    break;
            }
            return res;
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
            boolean entrou = false;
            for(String key : macro.keySet())
            {
                if(res.contains(key))
                {
                    res = res.replaceAll(key,macro.get(key));
                    entrou = true;
                    break;
                }
            }
            if(!entrou)
            {
                res += "." + macro.get("@");
            }
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
     * Adiciona um valor da Base de dados à cache.
     * @param value Valor a adicionar.
     */
    private void addValueDB(Value value)
    {
        EntryCache entryCache = new EntryCache(value.getDominio(),value.getType(), EntryCache.Origin.FILE);
        if(!this.cache.containsKey(entryCache.getKey()))
            this.cache.put(entryCache.getKey(), entryCache);
        this.cache.get(entryCache.getKey()).addValue(value);
    }

    /**
     * Adiciona valor de um tipo há cache. Só é usado para tipos elementares, isto é, SOADMIN, SOASP (...).
     * @param valores Map de todos os valores lidos do ficheiro.
     * @param type Tipo a acrescentar na cache
     */
    private void addFSTValueBD(Map<String,List<Value>> valores, String type)
    {
        if(!valores.get(type).isEmpty())
            this.addValueDB(valores.get(type).get(0));
    }

    /**
     * Adiciona valor de um tipo há cache. Só é usado para tipos multiplos, isto é, A, NS (...).
     * @param valores Map de todos os valores lidos do ficheiro.
     * @param type Tipo a acrescentar na cache
     */
    private void addAllValueBD(Map<String,List<Value>> valores, String type)
    {
        for(Value value : valores.get(type))
            this.addValueDB(value);
    }

    /**
     * Adiciona um CNAME há cache.
     * @param valores Map de todos os valores lidos do ficheiro.
     * @param warnings Lista de avisos, para verificar se os cnames não são canónicos de um servidor
     *                 inexistente.
     */
    private void addCnameBD(Map<String,List<Value>> valores, List<String> warnings)
    {
        List<Value> values = valores.get("CNAME");
        for(Value value : values)
            if(valores.get("A").stream().anyMatch( e -> e.getDominio().equals(value.getValue())))
                this.addValueDB(value);
            else
                warnings.add("CNAME " + value.getDominio() + " não guardado pois " +value.getValue() + " não existe");

    }

    /**
     * Passa os valores lidos do ficheiro da base de dados para a cache.
     * @param valores Valores lidos.
     * @param warnings Lista com os avisos de erros do ficheiro. Apenas usado no tipo CNAME.
     */
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

    /**
     * Adiciona um valor lido do ficheiro aos valores lidos até ao momento. Funciona para tipos
     * elementares.
     * @param valores Valores lidos até ao momento.
     * @param type Tipo do valor a adicionar.
     * @param value Valor lido da linha.
     * @throws Exception Caso o valor já tenha sido lido.
     */
    private void addValor(Map<String,List<Value>> valores,String type, Value value) throws Exception
    {
        if(valores.get(type).size() > 0)
            throw new Exception("Valor de " + type + " repetido.");
        else
        {
            valores.get(type).add(value);
        }
    }

    /**
     * Adiciona um valor lido do ficheiro aos valores lidos até ao momento. Funciona para tipos
     * multiplos.
     * @param valores Valores lidos até ao momento.
     * @param type Tipo do valor a adicionar sub o formato de String.
     * @param value Valor a adicionar
     * @throws Exception Caso o valor já tenha sido lido.
     */
    private void addValores(Map<String,List<Value>> valores,String type, Value value) throws Exception
    {
        String dom = value.getDominio();
        String val = value.getValue();
        List<Value> values = valores.get(type);
        if (values.stream().anyMatch(v -> v.getDominio().equals(dom) && (type.equals("A") || v.getValue().equals(val))))
            throw new Exception("Valor do " + type + " repetido para o mesmo domínio");
        else
            valores.get(type).add(value);
    }

    /**
     * Adicionar um CNAME aos valores lidos até ao momento.
     * @param valores Valores lidos até ao momento.
     * @param value Valor do CNAME a adicioanr
     * @throws Exception Caso o valor seja repetido ou esteja a apontar para outro CNAME.
     */
    private void addValoresCNAME(Map<String,List<Value>> valores, Value value) throws Exception
    {
        String can = value.getDominio();
        String val = value.getValue();
        List<Value> values = valores.get("CNAME");
        if (values.stream().anyMatch(v -> v.getDominio().equals(can)))
            throw new Exception("Valor do CNAME repetido para o mesmo domínio");
        if (values.stream().anyMatch(v -> v.getDominio().equals(val)))
            throw new Exception("Valor do CNAME a não pode ser um canónico de um canónico");
        valores.get("CNAME").add(value);

    }

    /**
     * Adiciona os valores aos valores lidos até ao momento com prioridade.
     * @param valores Todos os valores lidos até ao momento.
     * @param words lista de palavras de uma linha.
     * @param value Valor lido sem a prioridade.
     * @param macro Macros de valores default.
     * @throws Exception método addValores.
     */
    private void addValoresPri(Map<String,List<Value>> valores,String[] words, String type, Value value, Map<String,String> macro) throws Exception
    {
        if(words.length > 4)
        {
            String dom = value.getDominio();
            String val = value.getValue();
            int TTL = value.getTTL();
            int prioridade = converteInt(words, macro, 4, "'Prioridade'");
            value = new Value(dom, aux.get(type), val, TTL, prioridade);
        }
        addValores(valores, type,value);
    }

    /**
     * Método que faz o parsing de um ficheiro para um BD.
     * @param lines Linhas de um ficheiro.
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
            aux.put("SOASP", Data.typeOfValueConvert("SOASP"));
            aux.put("SOAADMIN", Data.typeOfValueConvert("SOAADMIN"));
            aux.put("SOASERIAL", Data.typeOfValueConvert("SOASERIAL"));
            aux.put("SOAREFRESH", Data.typeOfValueConvert("SOAREFRESH"));
            aux.put("SOARETRY", Data.typeOfValueConvert("SOARETRY"));
            aux.put("SOAEXPIRE", Data.typeOfValueConvert("SOAEXPIRE"));
            aux.put("NS", Data.typeOfValueConvert("NS"));
            aux.put("CNAME", Data.typeOfValueConvert("CNAME"));
            aux.put("MX", Data.typeOfValueConvert("MX"));
            aux.put("A", Data.typeOfValueConvert("A"));
            aux.put("PTR", Data.typeOfValueConvert("PTR"));
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
                        String dom = words[0];
                        if(!words[1].equals("PTR"))
                            dom = converteDom(words[0], macro);
                        int TTL = converteInt(words, macro, 3, "'TTL'");
                        switch (words[1]) {
                            case "SOASP":
                            case "SOAADMIN":
                                String name = converteDom(words[2], macro);
                                addValor(valores,words[1], new Value(dom,aux.get(words[1]),name,TTL));
                                break;
                            case "SOASERIAL":
                            case "SOAREFRESH":
                            case "SOARETRY":
                            case "SOAEXPIRE":
                                addValor(valores,words[1], new Value(dom,aux.get(words[1]),words[2],TTL));
                                break;
                            case "PTR":
                                if(!dom.contains(":"))
                                    dom += ":5353";
                                Value value = new Value(dom, aux.get(words[1]), words[2], TTL);
                                addValores(valores,words[1],value);
                                break;
                            case "CNAME":
                                name = converteDom(words[2], macro);
                                value = new Value(dom, aux.get(words[1]), name, TTL);
                                addValoresCNAME(valores,value);
                                break;
                            case "NS":
                            case "MX":
                                name = converteDom(words[2], macro);
                                value = new Value(dom, aux.get(words[1]), name, TTL);
                                addValoresPri(valores,words,words[1],value,macro);
                                break;
                            case "A":
                                String en = words[2];
                                if(!en.contains(":"))
                                    en += ":5353";
                                value = new Value(dom, aux.get(words[1]), en, TTL);
                                addValoresPri(valores,words,words[1],value,macro);
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
     */
    public void createBD(String filename,String dom) throws IOException
    {
        EntryCache entryCache = new EntryCacheDBF(dom,filename);
        this.cache.put(entryCache.getKey(),entryCache);
        this.dominio = dom;
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        this.createBD(lines.toArray(new String[1]));
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

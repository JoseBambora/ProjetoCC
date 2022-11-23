package Cache;

import DNSPacket.*;
import Log.*;
import ObjectServer.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author José Carvalho
 * Classe que representa a estrutura de uma cache dos servidores
 * Algoritmo usado: Least Recently Used (LRU)
 * DNSPacket.Data criação: 29/10/2022
 * DNSPacket.Data última atualização: 19/11/2022
 */
public class Cache
{
    private static final Tuple<Integer,Integer> pri = new Tuple<>(0,255);
    private static final Tuple<Integer,Integer> tem = new Tuple<>(0,Integer.MAX_VALUE);
    private final List<EntryCache> cache;
    private final Map<String, Byte> aux;
    private final Map<String, String> macro;
    private final List<String> tipos;
    private String dominio = ""; // SÓ PARA SP
    private final ReentrantReadWriteLock readWriteLock;
    public Cache()
    {
        this.readWriteLock = new ReentrantReadWriteLock();
        this.macro = new HashMap<>();
        this.aux = new HashMap<>();
        this.cache = new ArrayList<>();
        this.tipos = new ArrayList<>();
        tipos.add("SOASP");
        tipos.add("SOAADMIN");
        tipos.add("SOASERIAL");
        tipos.add("SOAREFRESH");
        tipos.add("SOARETRY");
        tipos.add("SOAEXPIRE");
        tipos.add("NS");
        tipos.add("CNAME");
        tipos.add("MX");
        tipos.add("A");
        tipos.add("PTR");
        tipos.forEach(str -> {try {aux.put(str , Data.typeOfValueConvert(str));} catch (Exception e){}});
    }

    /**
     * Adiciona uma entrada à cache.
     * @param entryCache Entrada a adicionar.
     */
    private boolean addDataCache(EntryCache entryCache)
    {
        this.readWriteLock.writeLock().lock();
        this.cache.forEach(EntryCache::removeExpireInfo);
        int ind = this.cache.indexOf(entryCache);
        if(ind == -1)
            this.cache.add(entryCache);
        else if(entryCache.getOrigem() == EntryCache.Origin.OTHERS)
            this.cache.get(ind).updateTempoEntrada();
        this.readWriteLock.writeLock().unlock();
        return ind == -1;
    }

    /**
     * Adicionar um determinado valor à cache.
     * @param value Valor a adicionar à cache.
     * @param origin Origem da mensagem, SP se for transferência de zona, OTHERs no resto.
     */
    public boolean addData(Value value, EntryCache.Origin origin)
    {
        EntryCache entryCache = new EntryCache(value,origin);
        return this.addDataCache(entryCache);
    }
    /**
     * Adicionar dados de um pacote recebido. Usado para transferência de zona e para receção
     * de respostas a queries.
     * @param resposta Resposta à query.
     * @param origin Origem da mensagem, SP se for transferência de zona, OTHERs no resto.
     */
    public void addData(DNSPacket resposta, EntryCache.Origin origin)
    {
        List<Value> rv = Arrays.stream(resposta.getData().getResponseValues()).toList();
        List<Value> av = Arrays.stream(resposta.getData().getAuthoriteValues()).toList();
        List<Value> ev = Arrays.stream(resposta.getData().getExtraValues()).toList();
        rv.forEach(v -> this.addDataCache(new EntryCache(v,origin)));
        av.forEach(v -> this.addDataCache(new EntryCache(v,origin)));
        ev.forEach(v -> this.addDataCache(new EntryCache(v,origin)));
    }

    /**
     * Adiciona uma entrada de uma linha há cache. Usado para a transferência de zona
     * @param line Linha a adicionar
     * @throws Exception A linha não estiver certa.
     */
    public boolean addData(String line, EntryCache.Origin origin)
    {
        Value value = this.converteLinha(line);
        return value != null && !this.addData(value, origin);
    }

    /**
     * Método que devolve os Authority Values
     * @return Lista com os Authority Values
     */
    private List<Value> getAVBD()
    {
        this.readWriteLock.readLock().lock();
        List<Value> values = new ArrayList<>();
        byte ns = aux.get("NS");
        this.cache.stream().filter(e -> e.getType() == ns)
                           .filter(e -> e.getDominio().matches("(.*)" + this.dominio))
                           .forEach(e -> values.add(e.getData()));
        this.readWriteLock.readLock().unlock();
        return values;
    }

    /**
     * Método que devolve os Extra Values
     * @param RV Response Values
     * @param AV Authority Values
     * @return Lista com os Extra Values
     */
    private List<Value> getEVBD(List<Value> RV, List<Value> AV)
    {
        this.readWriteLock.readLock().lock();
        List<Value> values = new ArrayList<>();
        byte a = aux.get("A");
        this.cache.stream()
                  .filter(e -> e.getType() == a)
                  .filter(e -> RV.stream().noneMatch(value -> value.getType() == a && value.getDominio().equals(e.getDominio())))
                  .filter(e -> RV.stream().anyMatch(value -> value.getValue().equals(e.getDominio())) ||
                               AV.stream().anyMatch(value -> value.getValue().equals(e.getDominio())))
                  .forEach(e -> values.add(e.getData()));

        this.readWriteLock.readLock().unlock();
        return values;
    }
    /**
     * Procura resposta a uma dada query, dando o domínio da query e o tipo.
     * @param dom Domínio da pergunta.
     * @param type Tipo da pergunta.
     * @return Null se resposta não for encontrada, ou DNSPacket.Data se for encontrada.
     */
    private Tuple<Integer, Data> getAnswer(String dom, byte type)
    {
        int cod = 0;
        this.readWriteLock.readLock().lock();
        Data res = new Data(dom,type);
        List<Value> rv = this.cache.stream()
                                   .filter(EntryCache::isValid)
                                   .filter(e -> e.getType() == type)
                                   .filter(e -> e.getDominio().equals(dom))
                                   .map(EntryCache::getData).toList();
        if(!rv.isEmpty())
        {
            res.setResponseValues(rv.toArray(new Value[0]));
        }
        else
        {
            if(this.cache.stream().anyMatch(e -> e.getDominio().equals(dom)))
                cod = 1;
            else
                cod = 2;
        }
        List<Value> av = this.getAVBD();
        List<Value> ev = this.getEVBD(Arrays.asList(res.getResponseValues() == null ? new Value[0] : res.getResponseValues()), av);
        if(cod == 1)
        {
            av = av.stream().filter(v -> v.getDominio().equals(dom)).toList();
            ev = ev.stream().filter(v -> v.getDominio().equals(dom)).toList();
        }
        if(!av.isEmpty())
            res.setAuthoriteValues(av.toArray(new Value[1]));
        if(!ev.isEmpty())
            res.setExtraValues(ev.toArray(new Value[1]));
        this.readWriteLock.readLock().unlock();
        return new Tuple<>(cod,res);
    }

    /**
     * Procura resposta a uma dada query, dando o domínio da query e o tipo.
     * @param dom Domínio da pergunta.
     * @param type Tipo da pergunta.
     * @return Null se resposta não for encontrada, ou DNSPacket.Data se for encontrada.
     */
    public Tuple<Integer, Data> findAnswer(String dom, byte type)
    {
        byte cname = aux.get("CNAME");
        if(type != cname)
        {
            this.readWriteLock.readLock().lock();
            String finalDom = dom;
            List<String> aux = this.cache.stream()
                                         .map(e -> e.getNameCNAME(finalDom,cname))
                                         .filter(s -> s.length() > 0)
                                         .toList();
            if(!aux.isEmpty())
                dom = aux.get(0);
            this.readWriteLock.readLock().unlock();
        }
        return getAnswer(dom,type);
    }

    /**
     * Procura resposta quando é recebida um pacote DNS.
     * @param mensagem Query dns.
     * @return Null se resposta não for encontrada, ou DNSPacket.Data se for encontrada.
     */
    public Tuple<Integer, Data> findAnswer(DNSPacket mensagem)
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
        this.readWriteLock.writeLock().lock();
        this.cache.stream()
                  .filter(e -> e.getOrigem() == EntryCache.Origin.SP)
                  .filter(e -> e.getDominio().equals(name))
                  .forEach(this.cache::remove);
        this.readWriteLock.writeLock().unlock();
    }

    /**
     * Verifica se todos os campos da cache estão completos.
     * @param type Tipo do servidor (SP, ST, REVERSE)
     * @return true se não faltar campos, false caso contrário.
     */
    public boolean checkBD(String type)
    {
        this.readWriteLock.readLock().lock();
        try
        {
            Map<Byte,Integer> counter = new HashMap<>();
            tipos.forEach(str -> {try {counter.put(Data.typeOfValueConvert(str),0);} catch (Exception e){}});
            this.cache.stream()
                      .filter(e -> e.getOrigem() == EntryCache.Origin.FILE)
                      .forEach(e -> counter.put(e.getType(),counter.get(e.getType())+1));
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
        finally
        {
            this.readWriteLock.readLock().unlock();
        }
        return true;
    }


    /**
     * Método auxiliar ao parsing, de forma a retornar o valor correto do endereço URL.
     * Quando o endereço já termina com o '.' este método não faz nada.
     * @param str Endereço a converter.
     * @return Endereço URL final.
     */
    private String converteDom(String str)
    {
        String res = str;
        if(str.charAt(str.length()-1) != '.')
        {
            String finalRes = res;
            Optional<String> op = this.macro.keySet().stream().filter(finalRes::contains).findFirst();
            if(op.isPresent())
                res = res.replaceAll(op.get(),this.macro.get(op.get()));
            else
                res += "." + this.macro.get("@");
        }
        return res;
    }

    /**
     * Método que converte uma string em inteiro tendo recurso à macro em caso
     * de a string não ser um número
     * @param words Palavras para converter
     * @param index Indice da string no array words que pretendemos converter para inteiro.
     * @param campo Campo que queremos ir buscar o inteiro. Esta string serve para saber quais os limites.
     * @return Inteiro Convertido
     */
    private int converteInt(String[] words, int index, String campo)
    {
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
                String str = this.macro.get(word);
                if(str != null)
                    try
                    {
                        num = Integer.parseInt(str);
                    }
                    catch (NumberFormatException exp)
                    {
                        // throw new Exception("Valor não inteiro para a macro " + word);
                        num = -1;
                    }
                else
                    num = -2;
                    //throw new Exception("Valor não é inteiro e não está definido nas macros");
            }
            if(num < min || num > max)
                num = -3;
                //throw new Exception("Número que excede o intervalo estabelecidos para o campo " + campo + ". O intervalo é [" + min + "," + max + "]");

        }
        else
            num = -4;
            //throw new Exception("Não respeita a sintaxe.");
        return num;
    }

    /**
     * Converte uma linha num DNSPacket.Value
     * @param line linha a converter
     * @return Valor convertido.
     */
    private Value converteLinha(String line)
    {
        Value res = null;
        String[] words = line.split(" ");
        if (line.length() > 0 && line.charAt(0) != '#' && words.length > 2)
        {
            if (words[1].equals("DEFAULT"))
            {
                this.macro.put(words[0], words[2]);
                return res;
            }
            else if (words.length > 3)
            {
                String dom = words[0];
                if (!words[1].equals("PTR"))
                    dom = converteDom(words[0]);
                int TTL = converteInt(words, 3, "'TTL'");
                if(TTL < 0)
                    return res;
                switch (words[1])
                {
                    case "SOASP":
                    case "SOAADMIN":
                        String name = converteDom(words[2]);
                        res = new Value(dom, aux.get(words[1]), name, TTL);
                        break;
                    case "SOASERIAL":
                    case "SOAREFRESH":
                    case "SOARETRY":
                    case "SOAEXPIRE":
                        res = new Value(dom, aux.get(words[1]), words[2], TTL);
                        break;
                    case "PTR":
                        if (!dom.contains(":"))
                            dom += ":5353";
                        res = new Value(dom, aux.get(words[1]), words[2], TTL);
                        break;
                    case "CNAME":
                        name = converteDom(words[2]);
                        res = new Value(dom, aux.get(words[1]), name, TTL);
                        break;
                    case "NS":
                    case "MX":
                        name = converteDom(words[2]);
                        if(words.length > 4)
                        {
                            int pri = converteInt(words,4,"'Prioridade'");
                            if(pri < 0)
                                return res;
                            res = new Value(dom, aux.get(words[1]), name, TTL,pri);
                        }
                        else
                            res = new Value(dom, aux.get(words[1]), name, TTL);
                        break;
                    case "A":
                        String en = words[2];
                        if (!en.contains(":"))
                            en += ":5353";
                        if(words.length > 4)
                        {
                            int pri = converteInt(words,4,"'Prioridade'");
                            if(pri < 0)
                                return res;
                            res = new Value(dom, aux.get(words[1]), en, TTL,pri);
                        }
                        else
                            res = new Value(dom, aux.get(words[1]), en, TTL);
                        break;
                    default:
                        break;
                }
            }
        }
        return res;
    }
    /**
     * Método que faz o parsing de um ficheiro para um BD.
     * @param lines Linhas de um ficheiro.
     */
    public void createBD(List<String> lines, String logFile) throws IOException
    {
        List<String> warnings = new ArrayList<>();
        AtomicInteger l = new AtomicInteger(1);
        lines.forEach(str ->
                            { l.getAndIncrement();
                              if(this.addData(str, EntryCache.Origin.FILE))
                                  warnings.add("Erro ficheiro BD, linha " + l + " não adicionada");}
                            );
        List<String> writeLogs = new ArrayList<>();
        warnings.forEach(w -> {writeLogs.add(new Log(Date.from(Instant.now()), Log.EntryType.SP,"",w).toString()); System.out.println(w);});
        LogFileWriter.writeInLogFile(logFile,writeLogs);
    }

    /**
     * Método que faz o parsing de um ficheiro para um BD
     * @param filename Nome do ficheiro.
     */
    public void createBD(String filename,String dom, String logFile) throws IOException
    {
        this.dominio = dom;
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        this.createBD(lines,logFile);
    }
    @Override
    public String toString()
    {
        StringBuilder res = new StringBuilder();
        this.cache.forEach(e -> res.append(e.toString()).append('\n'));
        return res.toString();
    }
    public int size()
    {
        return this.cache.size();
    }
}

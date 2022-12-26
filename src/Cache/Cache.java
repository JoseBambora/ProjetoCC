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
import java.util.function.Consumer;

/**
 * @author José Carvalho
 * Classe que representa a estrutura de uma cache dos servidores
 * Algoritmo usado: Least Recently Used (LRU)
 * DNSPacket.Data criação: 29/10/2022
 * DNSPacket.Data última atualização: 20/12/2022
 */
public class Cache
{
    private static final Tuple<Integer,Integer> pri = new Tuple<>(0,255);
    private static final Tuple<Integer,Integer> tem = new Tuple<>(0,Integer.MAX_VALUE);
    private final List<EntryCache> cache;
    private final Map<Tuple<String,Byte>,List<NegativeEntryCache>> cacheNegativa;
    private final Map<String, Byte> aux;
    private final Map<String, String> macro;
    private final List<String> tipos;
    private String dominio = ""; // SÓ PARA SP
    private final ReentrantReadWriteLock readWriteLock;
    private final ReentrantReadWriteLock readWriteLockNC;
    public Cache()
    {
        this.readWriteLock = new ReentrantReadWriteLock();
        this.readWriteLockNC = new ReentrantReadWriteLock();
        this.macro = new HashMap<>();
        this.aux = new HashMap<>();
        this.cache = new ArrayList<>();
        this.cacheNegativa = new HashMap<>();
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
        tipos.forEach(str -> {try {aux.put(str , Data.typeOfValueConvert(str));} catch (Exception ignored){}});
    }

    public void setDominio(String dominio) {
        this.dominio = dominio;
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
     * Adiciona uma entrada à cache negativa.
     */
    private boolean addDataNegativeCache(String dom, byte type, NegativeEntryCache entryCache)
    {
        this.readWriteLockNC.writeLock().lock();
        this.cacheNegativa.values().forEach(e -> e.forEach(EntryCache::removeExpireInfo));
        Tuple<String,Byte> t = new Tuple<>(dom,type);
        boolean in = this.cacheNegativa.containsKey(t);
        if(!in)
        {
            this.cacheNegativa.put(t,new ArrayList<>());
            this.cacheNegativa.get(t).add(entryCache);
        }
        else
        {
            if(this.cacheNegativa.get(t).contains(entryCache))
                this.cacheNegativa.get(t).forEach(EntryCache::updateTempoEntrada);
            else
                this.cacheNegativa.get(t).add(entryCache);
        }
        this.readWriteLockNC.writeLock().unlock();
        return in;
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
        Consumer<Value> consumer1;
        Consumer<Value> consumer2;
        Consumer<Value> consumer3;
        byte error = resposta.getHeader().getResponseCode();
        String dom = resposta.getData().getName();
        byte type = resposta.getData().getTypeOfValue();
        if(error == 0)
        {
            consumer1 = v -> this.addDataCache(new EntryCache(v,origin));
            consumer2 = consumer1;
            consumer3 = consumer1;
        }
        else
        {
            consumer1 = v -> this.addDataNegativeCache(dom,type,new NegativeEntryCache(v,origin,error, NegativeEntryCache.tipo.RV));
            consumer2 = v -> this.addDataNegativeCache(dom,type,new NegativeEntryCache(v,origin,error, NegativeEntryCache.tipo.AV));
            consumer3 = v -> this.addDataNegativeCache(dom,type,new NegativeEntryCache(v,origin,error, NegativeEntryCache.tipo.EV));
        }
        if (resposta.getHeader().getNumberOfValues() != 0) {
            List<Value> rv = Arrays.stream(resposta.getData().getResponseValues()).toList();
            rv.forEach(consumer1);
        }
        if (resposta.getHeader().getNumberOfAuthorites() != 0) {
            List<Value> av = Arrays.stream(resposta.getData().getAuthoriteValues()).toList();
            av.forEach(consumer2);
        }
        if (resposta.getHeader().getNumberOfExtraValues() != 0) {
            List<Value> ev = Arrays.stream(resposta.getData().getExtraValues()).toList();
            ev.forEach(consumer3);
        }
    }

    /**
     * Adiciona uma entrada de uma linha há cache. Usado para a transferência de zona.
     * @param line Linha a adicionar.
     * @param origin Origem da linha.
     */
    public boolean addData(String line, EntryCache.Origin origin)
    {
        Value value = this.converteLinha(line);
        return value != null && !this.addData(value, origin);
    }

    /**
     * Método que devolve os Authority Values.
     * @param dominio Dominio da query.
     * @return Lista com os Authority Values.
     */
    private List<Value> getAVBD(String dominio)
    {
        List<Value> values = new ArrayList<>();
        byte ns = aux.get("NS");
        this.cache.stream().filter(e -> e.getType() == ns)
                           .filter(e -> e.getOrigem() != EntryCache.Origin.OTHERS ?
                                   e.getDominio().matches("(.*)" + this.dominio) :
                                   e.getDominio().matches(dominio))
                           .forEach(e -> values.add(e.getData()));
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
        List<Value> values = new ArrayList<>();
        byte a = aux.get("A");
        this.cache.stream()
                  .filter(e -> e.getType() == a)
                  .filter(e -> RV.stream().noneMatch(value -> value.getType() == a && value.getDominio().equals(e.getDominio())))
                  .filter(e -> RV.stream().anyMatch(value -> value.getValue().equals(e.getDominio())) ||
                               AV.stream().anyMatch(value -> value.getValue().equals(e.getDominio())))
                  .forEach(e -> values.add(e.getData()));
        return values;
    }

    /**
     * Constroi um pacote de resposta
     * @param header Header da query recebida
     * @param data Dadas obtidos
     * @param cod codigo de erro
     * @param rv Lista dos response values
     * @param av Lista dos authority values
     * @param ev Lista dos extra values
     * @return Pacote construido.
     */
    private DNSPacket buildPacket(Header header, Data data, byte cod, List<Value> rv, List<Value> av, List<Value> ev)
    {
        byte rvs = (byte) rv.size();
        byte avs = (byte) av.size();
        byte evs = (byte) ev.size();
        Header h;
        if(header != null)
        {
            String flags = header.flagsToString();
            flags = flags.replaceAll("Q+", "");
            List<String> ns = this.cache.stream()
                    .filter(EntryCache::isValid)
                    .filter(e -> e.getType() == aux.get("NS"))
                    .filter(e -> e.getOrigem() == EntryCache.Origin.FILE)
                    .map(EntryCache::getDominio).toList();
            if (av.stream().allMatch(v -> ns.contains(v.getDominio())))
            {
                if(flags.isEmpty())
                    flags = "A";
                else
                    flags += "+A";
            }
            h = new Header(header.getMessageID(), Header.flagsStrToByte(flags), cod, rvs, avs, evs);
        }
        else
        {
            byte zero = (byte) 0;
            h = new Header((short) 0, zero,cod,zero,zero,zero);
        }
        return new DNSPacket(h,data);
    }
    /**
     * Procura resposta a uma dada query, dando o domínio da query e o tipo.
     * @param dom Domínio da pergunta.
     * @param type Tipo da pergunta.
     * @return Par entre inteiro e data. Interio corresponde ao código de erro.
     */
    private DNSPacket getAnswer(Header header,String dom, byte type)
    {
        DNSPacket result = null;
        Data res = new Data(dom,type);
        Tuple<String,Byte> t = new Tuple<>(dom,type);
        byte cod = 0;
        List<Value> rv = new ArrayList<>();
        List<Value> av = new ArrayList<>();
        List<Value> ev = new ArrayList<>();
        if(this.cacheNegativa.containsKey(t))
        {
            List<NegativeEntryCache> l = this.cacheNegativa.get(t);
            for(NegativeEntryCache e : l)
                e.addPacket(rv,av,ev);
            cod = l.get(0).getErrorCode();
        }
        else
        {
            if(type == aux.get("PTR"))
            {
                String []domA = dom.split("\\.");
                // 13.9.0.10.IN-ADDR.REVERSE.G706.
                StringBuilder domain = new StringBuilder();
                for (int i = 3; i < domA.length; i++)
                    domain.append(domA[i]).append('.');
                if(this.dominio.equals(domain.toString()))
                {
                    StringBuilder domBuilder = new StringBuilder();
                    for(int i = 3; i > 0; i--)
                    {
                        domBuilder.append(domA[i]).append('.');
                    }
                    dom = domBuilder.append(domA[0]).toString();
                }
            }
            String domaux = dom;
            rv = this.cache.stream()
                    .filter(EntryCache::isValid)
                    .filter(e -> e.getType() == type)
                    .filter(e -> e.getDominio().equals(domaux))
                    .map(EntryCache::getData).toList();
            if(rv.isEmpty())
            {
                if(this.cache.stream().anyMatch(e -> e.domainExist(domaux,this.dominio)))
                    cod = 1;
                else
                    cod = 2;
            }
            if(cod == 1)
            {
                av = this.cache.stream().filter(EntryCache::isValid)
                        .filter(e -> e.getType() == aux.get("NS"))
                        .filter(e -> e.domainExist(domaux,this.dominio))
                        .map(e -> e.getData())
                        .toList();
            }
            else
                av = this.getAVBD(dom);
            ev = this.getEVBD(rv, av);
        }
        if(!rv.isEmpty())
            res.setResponseValues(rv.toArray(new Value[1]));
        if(!av.isEmpty())
            res.setAuthoriteValues(av.toArray(new Value[1]));
        if(!ev.isEmpty())
            res.setExtraValues(ev.toArray(new Value[1]));
        result = this.buildPacket(header,res,cod,rv,av,ev);
        return result;
    }

    /**
     * Vai buscar o CNAME de um determinado domínio
     * @param dom Domínio da query
     * @param type Tipo da query
     * @return Cname correspondente.
     */
    private String getCName(String dom, byte type)
    {
        byte cname = aux.get("CNAME");
        if(type != cname)
        {
            String finalDom = dom;
            List<String> aux = this.cache.stream()
                    .map(e -> e.getNameCNAME(finalDom, cname))
                    .filter(s -> s.length() > 0)
                    .toList();
            if (!aux.isEmpty())
                dom = aux.get(0);
        }
        return dom;
    }
    /**
     * Procura resposta a uma dada query, dando o domínio da query e o tipo.
     * @param dom Domínio da pergunta.
     * @param type Tipo da pergunta.
     * @return Par entre inteiro e data. Interio corresponde ao código de erro.
     */
    public Tuple<Byte,Data> findAnswer(String dom, byte type)
    {
        this.readWriteLock.readLock().lock();
        dom = this.getCName(dom,type);
        DNSPacket aux = this.getAnswer(null,dom,type);
        this.readWriteLock.readLock().unlock();
        return new Tuple<>(aux.getHeader().getResponseCode(),aux.getData());
    }

    /**
     * Procura resposta quando é recebida um pacote DNS.
     * @param mensagem Query dns.
     * @return Par entre inteiro e data. Interio corresponde ao código de erro.
     */
    public DNSPacket findAnswer(DNSPacket mensagem)
    {
        Header h = mensagem.getHeader();
        Data d = mensagem.getData();
        String name = d.getName();
        byte b = d.getTypeOfValue();
        this.readWriteLock.readLock().lock();
        name = this.getCName(name,b);
        DNSPacket res = this.getAnswer(h,name,b);
        this.readWriteLock.readLock().unlock();
        return res;
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
            tipos.forEach(str -> {try {counter.put(Data.typeOfValueConvert(str),0);} catch (Exception ignored){}});
            this.cache.stream()
                      .filter(e -> e.getOrigem() == EntryCache.Origin.FILE)
                      .forEach(e -> counter.put(e.getType(),counter.get(e.getType())+1));
            boolean res = false;
            switch (type)
            {
                case "SP" -> res = counter.keySet().stream().allMatch(b -> b.equals(aux.get("PTR")) || b.equals(aux.get("CNAME")) || counter.get(b) > 0);
                case "ST"-> res =  counter.keySet().stream().allMatch(b -> (!b.equals(aux.get("NS")) && !b.equals(aux.get("A"))) || counter.get(b) > 0);
                case "REVERSE" -> res =  counter.keySet().stream().allMatch(b -> (!b.equals(aux.get("NS")) && !b.equals(aux.get("A")) && !b.equals(aux.get("PTR"))) || counter.get(b) > 0);
                case "REVERSET" -> res =  counter.keySet().stream().allMatch(b -> b.equals(aux.get("MX")) || b.equals(aux.get("PTR")) || b.equals(aux.get("CNAME")) || counter.get(b) > 0);
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
                this.macro.put(words[0], words[2]);
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
     * @param logFile Ficheiro para escrever os warnigns do ficheiro de configuração da base de dados.
     */
    public void createBD(List<String> lines, String logFile, boolean debug) throws IOException
    {
        List<String> warnings = new ArrayList<>();
        AtomicInteger l = new AtomicInteger(1);
        lines.forEach(str ->
                            {
                                l.getAndIncrement();
                                if(this.addData(str, EntryCache.Origin.FILE))
                                    warnings.add("Erro ficheiro BD, linha " + l + " não adicionada");}
                            );
        List<String> writeLogs = new ArrayList<>();
        String nameSP = this.cache.stream().filter(entryCache -> entryCache.getType() == aux.get("SOASP"))
                                           .findFirst().map(entryCache -> entryCache.getData().getValue())
                                           .orElse("");
        String endereco = this.cache.stream().filter(entryCache -> entryCache.getType() == aux.get("A"))
                                             .filter(entryCache ->  entryCache.getDominio().equals(nameSP))
                                             .findFirst().map(entryCache -> entryCache.getData().getValue())
                                             .orElse("");
        if (warnings.isEmpty())
        {
            String str = "Base de dados criada com sucesso";
            writeLogs.add(new Log(Date.from(Instant.now()), Log.EntryType.SP,endereco,str).toString());
            if(debug)
                System.out.println(str);
        }
        else
        {
            warnings.forEach(w -> writeLogs.add(new Log(Date.from(Instant.now()), Log.EntryType.SP,endereco,w).toString()));
            if(debug)
                writeLogs.forEach(System.out::println);
        }
        LogFileWriter.writeInLogFile(logFile,writeLogs);
    }

    /**
     * Método que faz o parsing de um ficheiro para um BD.
     * @param filename Nome do ficheiro.
     * @param dom Domínio do servidor.
     * @param logFile Ficheiro para escrever os warnings.
     */
    public void createBD(String filename,String dom, String logFile, boolean debug) throws IOException
    {
        this.dominio = dom;
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        this.createBD(lines,logFile, debug);
    }
    @Override
    public String toString()
    {
        StringBuilder res = new StringBuilder();
        this.cache.forEach(e -> res.append(e.toString()).append('\n'));
        return res.toString();
    }

    /**
     * Procura o IP para o modo iterativo.
     * @param dominio Domínio da query.
     * @return Endereço IP, mais a porta do servidor a contactar.
     */
    public String findIP(String dominio)
    {
        this.readWriteLock.readLock().lock();
        String [] dominios = dominio.split("\\.");
        byte ns = aux.get("NS");
        Map<String, List<String>> dominioServer = new HashMap<>();
        this.cache.stream().filter(EntryCache::isValid).filter(e -> e.getType() == ns)
                .forEach(e ->
                        {
                            if (! dominioServer.containsKey('.' + e.getDominio()))
                                dominioServer.put('.' + e.getDominio(),new ArrayList<>());
                            dominioServer.get('.' + e.getDominio()).add(e.getData().getValue());});
        Map<String,Integer> map = new HashMap<>();
        for(String dom : dominioServer.keySet())
        {
            map.put(dom,0);
            int i = dominios.length-1;
            StringBuilder dom2 = new StringBuilder('.' + dominios[i] + '.');
            while(i > 0 && dom.matches("(.*)" + dom2))
            {
                i--;
                dom2.insert(0, '.' + dominios[i]);
                map.put(dom,map.get(dom)+1);
            }
            if(dom.matches("(.*)" + dom2))
                map.put(dom,map.get(dom)+1);

        }
        Random random = new Random();
        int max = map.entrySet().stream().max(Comparator.comparingInt(Map.Entry::getValue)).map(Map.Entry::getValue).orElse(0);
        List<String> doms = map.entrySet().stream().filter(e -> e.getValue().equals(max)).map(Map.Entry::getKey).toList();
        List<String> servers = dominioServer.get(doms.get(0));
        int r = random.nextInt(0,servers.size());
        String server = servers.get(r);
        String res = this.cache.stream().filter(EntryCache :: isValid)
                .filter(e -> e.getType() == aux.get("A"))
                .filter(e -> e.getDominio().equals(server))
                .findFirst().map(e -> e.getData().getValue())
                .orElse("");
        this.readWriteLock.readLock().unlock();
        return res;
    }

    /**
     * Método para testes
     * @return Devolve o número de entradas na cache.
     */
    public int size()
    {
        return this.cache.size();
    }
}

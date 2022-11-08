import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @author José Carvalho
 * Classe que representa a estrutura de uma cache dos servidores
 * Algoritmo usado: Least Recently Used (LRU)
 * Data criação: 29/10/2022
 * Data última atualização: 7/11/2022
 */
public class Cache
{
    private final Map<Tuple<String,Byte>,EntryCache> cache;
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
        Tuple<String,Byte> t = new Tuple<>(data.getName(), data.getTypeOfValue());
        if(this.answers == this.espaco)
            this.removeData();
        if(!this.cache.containsKey(t))
        {
            EntryCache entryCache = new EntryCache(resposta,origin);
            this.answers++;
            this.cache.put(entryCache.getKey(),entryCache);
        }
        else if(origin == EntryCache.Origin.OTHERS)
            this.cache.get(t).setTempoEntrada(LocalDateTime.now());
    }
    public void addData(String dom, byte type, Value valor, EntryCache.Origin origin)
    {
        Tuple<String,Byte> t = new Tuple<>(dom, type);
        if(!this.cache.containsKey(t))
            this.cache.put(t,new EntryCache(t, origin));
        this.cache.get(t).addValueDB(valor);
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
        Tuple<String,Byte> t = new Tuple<>(name,b);
        Data res = null;
        if(this.cache.containsKey(t))
            res = this.cache.get(t).getData();
        return res;
    }

    public Data findAnswer(String dom, byte type)
    {
        Tuple<String,Byte> t = new Tuple<>(dom,type);
        Data res = null;
        if(this.cache.containsKey(t))
            res = this.cache.get(t).getData();
        return res;
    }

    // exclusiva dos SS
    public void removeByName(String name)
    {
        for(Tuple<String,Byte> elem : this.cache.keySet())
            if(elem.getValue1().equals(name))
                this.cache.remove(elem);
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

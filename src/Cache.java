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
    private final Map<Tuple<String,Byte>,EntryCache> cacheRV;
    private final Map<Tuple<String,Byte>,EntryCache> cacheAV;
    private final Map<Tuple<String,Byte>,EntryCache> cacheEV;
    private final Map<Tuple<String,Byte>,LocalDateTime> times;
    private int espaco;
    private int answers;
    public Cache(int espaco)
    {
        this.cacheRV = new HashMap<>();
        this.cacheAV = new HashMap<>();
        this.cacheEV = new HashMap<>();
        this.times = new HashMap<>();
        this.espaco = espaco;
        this.answers = 0;
    }
    public void setEspaco(int espaco)
    {
        this.espaco = espaco;
    }

    public void addData(DNSPacket resposta, EntryCache.Origin origin)
    {
        Data data = resposta.getData();
        Tuple<String,Byte> t = new Tuple<>(data.getName(), data.getTypeOfValue());
        if(this.answers == this.espaco)
            this.removeData();
        if(!this.cacheRV.containsKey(t))
        {
            this.answers++;
            this.cacheRV.put(t,new EntryCache(data.getResponseValues(),origin));
            this.cacheAV.put(t,new EntryCache(data.getResponseValues(),origin));
            this.cacheEV.put(t,new EntryCache(data.getResponseValues(),origin));
            this.times.put(t,LocalDateTime.now());
        }
        else if(origin == EntryCache.Origin.OTHERS)
            this.times.put(t,LocalDateTime.now());
    }
    public void removeExpireInfo()
    {
        for(Tuple<String,Byte> key : this.cacheRV.keySet())
        {
            EntryCache rv = this.cacheRV.get(key);
            EntryCache av = this.cacheAV.get(key);
            EntryCache ev = this.cacheEV.get(key);
            LocalDateTime localDateTime = this.times.get(key);
            int num = rv.removeExpired(localDateTime);
            av.removeExpired(localDateTime);
            ev.removeExpired(localDateTime);
            if(num == 0)
            {
                this.cacheRV.remove(key);
                this.cacheAV.remove(key);
                this.cacheEV.remove(key);
            }
        }
    }
    public void removeData()
    {
        this.removeExpireInfo();
        if(this.answers == this.espaco)
        {
            List<Tuple<String,Byte>> remove = new ArrayList<>(this.cacheRV.keySet());
            remove.sort((t1,t2) -> (int) ChronoUnit.NANOS.between(this.times.get(t2),this.times.get(t1)));
            for(int i = 0; i < this.espaco/2; i++)
            {
                this.cacheRV.remove(remove.get(i));
                this.cacheAV.remove(remove.get(i));
                this.cacheEV.remove(remove.get(i));
            }
            this.answers -= (this.espaco/2);
        }
    }
    public Data findAnswer(DNSPacket mensagem)
    {
        String name = mensagem.getData().getName();
        byte b = mensagem.getData().getTypeOfValue();
        Tuple<String,Byte> t = new Tuple<>(name,b);
        Data res = null;
        if(this.cacheRV.containsKey(t))
        {
            res =  new Data(name,b);
            res.setResponseValues(this.cacheRV.get(t).getDados());
            res.setAuthoriteValues(this.cacheAV.get(t).getDados());
            res.setExtraValues(this.cacheEV.get(t).getDados());
        }
        return res;
    }

    // exclusiva dos SS
    public void removeByName(String name)
    {
        for(Tuple<String,Byte> elem : this.cacheRV.keySet())
        {
            if(elem.getValue1().equals(name))
            {
                this.cacheRV.remove(elem);
                this.cacheAV.remove(elem);
                this.cacheEV.remove(elem);
            }
        }
    }

    @Override
    public String toString()
    {
        StringBuilder res = new StringBuilder();
        for(EntryCache entryCache : this.cacheRV.values())
        {
            res.append(entryCache.toString()).append("\n");
        }
        return res.toString();
    }
}

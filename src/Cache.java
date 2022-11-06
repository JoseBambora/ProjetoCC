import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author José Carvalho
 * Classe que representa a estrutura de uma cache dos servidores
 * Algoritmo usado: Least Recently Used (LRU)
 * Data criação: 29/10/2022
 * Data última atualização: 6/11/2022
 */
public class Cache
{
    // Pergunta, Resposta, Tempo
    private final Map<Tuple<String,Byte>,EntryCache> cache;
    private int espaco;
    public Cache(int espaco)
    {
        this.cache = new HashMap<>();
        this.espaco = espaco;
    }
    public void setEspaco(int espaco)
    {
        this.espaco = espaco;
    }
    public void addLog(DNSPacket resposta)
    {
        // Adicionar uma entrada para cada type of value
        if(this.cache.size() == this.espaco)
            this.removeData();
        Data data = resposta.getData();
        Tuple<String,Byte> t = new Tuple<>(data.getName(), data.getTypeOfValue());
        this.cache.put(t,new EntryCache(data, EntryCache.Origin.SP,LocalDateTime.now()));
    }

    public void addData(DNSPacket resposta, EntryCache.Origin origin)
    {
        Data data = resposta.getData();
        Tuple<String,Byte> t = new Tuple<>(data.getName(), data.getTypeOfValue());
        if(!this.cache.containsKey(t))
        {
            if(this.cache.size() == this.espaco)
                this.removeData();
            this.cache.put(t,new EntryCache(data, origin,LocalDateTime.now()));
        }
        else if(EntryCache.Origin.OTHERS == origin)
        {
            this.cache.get(t).setTempo(LocalDateTime.now());
        }
    }

    public void removeData()
    {
        // Comparar TTL, não remover entradas do SP / SS que TTL esteja válido
        List<Tuple<String,Byte>> remove = new ArrayList<>(this.cache.keySet());
        remove.sort((t1,t2) -> this.cache.get(t1).compareTo(this.cache.get(t2)));
        for(int i = 0; i < this.cache.size()/2; i++)
            this.cache.remove(remove.get(i));
    }
    public Tuple<Boolean,Data> findAnswer(DNSPacket mensagem)
    {
        Tuple<String,Byte> t = new Tuple<>(mensagem.getData().getName(),mensagem.getData().getTypeOfValue());
        Data resData = this.cache.get(t).getDados();
        boolean resBool = true;
        if (resData.getResponseValues() == null)
        {
            resBool = false;
            resData = null;
        }
        return new Tuple<>(resBool,resData);
    }

    // exclusiva dos SS
    public void removeByName(String name)
    {
        for(Tuple<String,Byte> elem : this.cache.keySet())
        {
            if(elem.getValue1().equals(name))
            {
                this.cache.remove(elem);
            }
        }
    }

    @Override
    public String toString()
    {
        StringBuilder res = new StringBuilder();
        for(EntryCache entryCache : this.cache.values())
            res.append(entryCache.toString()).append("\n");
        return res.toString();
    }
}

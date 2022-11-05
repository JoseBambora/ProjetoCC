import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * @author José Carvalho
 * Classe que representa a estrutura de uma cache dos servidores
 * Algoritmo usado: Least Recently Used (LRU)
 * Data criação: 29/10/2022
 * Data última atualização: 5/11/2022
 */
public class Cache
{
    // Pergunta, Resposta, Tempo
    private final Map<Tuple<String,Byte>,Data> cache;
    private final Map<Tuple<String,Byte>,LocalDateTime> time;
    private int espaco;
    public Cache(int espaco)
    {
        this.time = new HashMap<>();
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
            this.removeLog();
        Data data = resposta.getData();
        Tuple<String,Byte> t = new Tuple<>(data.getName(), data.getTypeOfValue());
        this.cache.put(t,data);
        this.time.put(t,LocalDateTime.now());
    }
    public void removeLog()
    {
        Tuple<String,Byte> tuple = new Tuple<>("",(byte) 0);
        LocalDateTime date = LocalDateTime.now();
        for(Tuple<String,Byte> elem : this.time.keySet())
        {
            LocalDateTime dateelem = this.time.get(elem);
            long num = ChronoUnit.NANOS.between(dateelem,date);
            if(num > 0)
            {
                tuple = elem;
                date = this.time.get(elem);
            }
        }
        this.cache.remove(tuple);
    }
    public Data findAnswer(DNSPacket mensagem)
    {
        Data data = mensagem.getData();
        String name = data.getName();
        Byte type = data.getTypeOfValue();
        Data res = new Data(name,type);
        for(Tuple<String,Byte> elem : this.cache.keySet())
        {
            if(elem.getValue1().equals(name) && elem.getValue2().equals(type))
            {
                // Set -> addicionar resposta
                res.setResponseValues(this.cache.get(elem).getResponseValues());
                res.setAuthoriteValues(this.cache.get(elem).getAuthoriteValues());
                res.setExtraValues(this.cache.get(elem).getResponseValues());
                this.time.put(elem,LocalDateTime.now());
            }
        }
        return res;
    }
    @Override
    public String toString()
    {
        StringBuilder res = new StringBuilder();
        for(Data data : this.cache.values())
            res.append(data.toString()).append("\n");
        return res.toString();
    }
}

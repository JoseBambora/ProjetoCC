import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author José Carvalho
 * Classe que representa a estrutura de uma cache dos servidores
 * Algoritmo usado: Least Recently Used (LRU)
 * Data criação: 29/10/2022
 * Data última atualização: 29/10/2022
 */
public class Cache
{
    // Pergunta, Resposta, Tempo
    private List<Triple<Logs,Logs, LocalDateTime>> cache;
    private int espaco;
    public Cache(int espaco)
    {
        this.cache = new ArrayList<>();
        this.espaco = espaco;
    }
    public void addLog(Logs pergunta, Logs resposta)
    {
        if(this.cache.size() == this.espaco)
            this.removeLog();
        this.cache.add(new Triple<>(pergunta, resposta, LocalDateTime.now()));
    }
    public void removeLog()
    {
        Tuple<Integer,LocalDateTime> indexLRU = new Tuple<>(0,this.cache.get(0).getValue3());
        int i = 0;
        for(Triple<Logs,Logs,LocalDateTime> triple : this.cache)
        {
            if(triple.getValue3().isBefore(indexLRU.getValue2()))
            {
                indexLRU.setValue1(i);
                indexLRU.setValue2(triple.getValue3());
            }
            i++;
        }
        this.cache.remove((int) indexLRU.getValue1());
    }
    public Logs findAnswer(Logs mensagem)
    {
        Logs res = null;
        int i = 0;
        for(Triple<Logs,Logs,LocalDateTime> triple : this.cache)
        {
            if(mensagem.equals(triple.getValue1()))
            {
                res = this.cache.get(i).getValue2();
                this.cache.get(i).setValue3(LocalDateTime.now());
            }
            i++;
        }
        return res;
    }
    @Override
    public String toString() {
        return this.cache.toString();
    }
}

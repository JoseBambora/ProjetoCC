import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * @author José Carvalho
 * Classe que define uma entrada na cache
 * Data criação: 7/11/2022
 * Data última atualização: 7/11/2022
 */
public class EntryCache
{

    public enum Origin {FILE, SP, OTHERS }
    private List<Value> dados;
    private Origin origem;

    public EntryCache(Value[] dados, Origin origem)
    {
        this.dados = new ArrayList<>(List.of(dados));
        this.origem = origem;
    }

    public Value[] getDados() {
        return dados.toArray(new Value[1]);
    }

    public Origin getOrigem() {
        return origem;
    }

    public int removeExpired(LocalDateTime localDateTimeAdded)
    {
        List<Integer> index = new ArrayList<>();
        int i = 0;
        for(Value value : dados)
        {
            if(value.getTTL() < ChronoUnit.SECONDS.between(localDateTimeAdded,LocalDateTime.now()))
            {
                index.add(i);
            }
            i++;
        }
        index.sort((n1,n2) -> n2 - n1);
        for(int in : index)
        {
            this.dados.remove(in);
        }
        return this.dados.size();
    }

    @Override
    public String toString() {
        return this.dados.toString();
    }
}
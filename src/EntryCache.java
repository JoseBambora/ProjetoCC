import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class EntryCache implements Comparable<EntryCache>
{

    public enum Origin {FILE, SP, OTHERS }
    private Data dados;
    private Origin origem;


    private LocalDateTime tempo;

    public EntryCache(Data dados, Origin origem, LocalDateTime tempo)
    {
        this.dados = dados;
        this.origem = origem;
        this.tempo = tempo;
    }

    public void setTempo(LocalDateTime tempoEntrada) {
        this.tempo = tempoEntrada;
    }
    public Value[] getResponseValues() {
        return this.dados.getResponseValues();
    }

    public Value[] getAuthoriteValues()
    {
        return this.dados.getAuthoriteValues();
    }

    public Value[] getExtraValues()
    {
        return this.dados.getExtraValues();
    }

    public Data getDados() {
        return dados;
    }

    @Override
    public int compareTo(EntryCache o) {
        return (int) ChronoUnit.NANOS.between(this.tempo,o.tempo);
    }

    @Override
    public String toString() {
        return this.dados.toString();
    }
}

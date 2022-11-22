import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * @author José Carvalho
 * Classe que define uma entrada na cache
 * Data criação: 7/11/2022
 * Data última atualização: 12/11/2022
 */
public class EntryCache
{
    public enum State {FREE, VALID }
    public enum Origin {FILE, SP, OTHERS }
    private final Value dados;
    private final Origin origem;
    private State estado;
    private LocalDateTime tempoEntrada;
    public EntryCache(Value value,Origin origem)
    {
        this.dados = value;
        this.origem = origem;
        this.estado = State.VALID;
        this.tempoEntrada = LocalDateTime.now();
    }

    /**
     * Remove informação que já esteja expirada.
     */
    public void removeExpireInfo()
    {
        if(this.origem != Origin.FILE && ChronoUnit.SECONDS.between(tempoEntrada, LocalDateTime.now()) > dados.getTTL())
            this.estado = State.FREE;
    }

    public void updateTempoEntrada()
    {
        this.tempoEntrada = LocalDateTime.now();
    }

    public String getDominio()
    {
        return this.dados.getDominio();
    }
    public byte getType()
    {
        return this.dados.getType();
    }

    /**
     * Buscar dados de uma posição da cache.
     * @return Dados.
     */
    public Value getData()
    {
        return this.dados;
    }

    public Origin getOrigem()
    {
        return this.origem;
    }

    @Override
    public String toString() {
        return this.getData().toString();
    }

    /**
     * Método usado apenas pelos SPs, de forma a ir buscar os nomes correspondentes a nomes canónicos.
     * @param can Nome canónico
     * @param cname CNAME sub a forma de byte.
     * @return Nome se existir correspondência ou string vazia caso contrário.
     */
    public String getNameCNAME(String can, byte cname)
    {
        if(this.dados.getType() == cname && this.dados.getDominio().equals(can))
            return this.dados.getValue();
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntryCache that = (EntryCache) o;
        return  origem == that.origem &&
                dados.getType() == that.getType() &&
                (dados.getType() > 5 ?
                dados.equals(that.dados) :
                dados.getDominio().equals(that.dados.getDominio()));
    }
    public boolean isValid()
    {
        return this.estado == State.VALID;
    }
}
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
    private Tuple<String,Byte> key;
    private List<Value> responseValues;
    private List<Value> authorityValues;
    private List<Value> extraValues;
    private Origin origem;
    private LocalDateTime tempoEntrada;

    public EntryCache(Tuple<String,Byte> key,Origin origem)
    {
        this.key = key;
        this.responseValues = new ArrayList<>();
        this.authorityValues = new ArrayList<>();
        this.extraValues = new ArrayList<>();
        this.origem = origem;
        this.tempoEntrada = LocalDateTime.now();
    }

    public EntryCache(DNSPacket dnsPacket, Origin origem)
    {
        this.key = new Tuple<>(dnsPacket.getData().getName(),dnsPacket.getData().getTypeOfValue());
        this.responseValues = new ArrayList<>(List.of(dnsPacket.getData().getResponseValues()));
        this.authorityValues = new ArrayList<>(List.of(dnsPacket.getData().getAuthoriteValues()));
        this.extraValues = new ArrayList<>(List.of(dnsPacket.getData().getExtraValues()));
        this.origem = origem;
        this.tempoEntrada = LocalDateTime.now();
    }

    public LocalDateTime getTempoEntrada() {
        return tempoEntrada;
    }

    public void removeExpireInfo()
    {
        if(this.origem != Origin.FILE)
        {
            this.responseValues  = this.responseValues.stream().filter(v -> v.getTTL() < ChronoUnit.SECONDS.between(LocalDateTime.now(), tempoEntrada)).toList();
            this.authorityValues = this.authorityValues.stream().filter(v -> v.getTTL() < ChronoUnit.SECONDS.between(LocalDateTime.now(), tempoEntrada)).toList();
            this.extraValues     = this.extraValues.stream().filter(v -> v.getTTL() < ChronoUnit.SECONDS.between(LocalDateTime.now(), tempoEntrada)).toList();
        }
    }

    public Tuple<String, Byte> getKey() {
        return key;
    }

    public void setTempoEntrada(LocalDateTime tempoEntrada) {
        this.tempoEntrada = tempoEntrada;
    }

    public Data getData()
    {
        Value[] res = null;
        if(!responseValues.isEmpty()) res = responseValues.toArray(new Value[1]);
        Value[] aut = null;
        if(!authorityValues.isEmpty()) aut = authorityValues.toArray(new Value[1]);
        Value[] ext = null;
        if(!extraValues.isEmpty()) ext = extraValues.toArray(new Value[1]);
        return new Data(key.getValue1(), key.getValue2(),res,aut,ext);
    }

    public void addValueDB(Value value)
    {
        this.responseValues.add(value);
    }

    @Override
    public String toString() {
        return this.getData().toString();
    }
}
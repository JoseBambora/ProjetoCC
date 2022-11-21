import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
/**
 * Dados de uma entrada na cache de respostas a queries.
 * @author José Carvalho
 * Data criação: 12/11/2022
 * Data última atualização: 12/11/2022
 */
public class DadosCacheAnswer implements DadosCache
{
    private List<Value> responseValues;
    private List<Value> authorityValues;
    private List<Value> extraValues;
    public DadosCacheAnswer()
    {
        this.responseValues = new ArrayList<>();
        this.authorityValues = new ArrayList<>();
        this.extraValues = new ArrayList<>();
    }
    public DadosCacheAnswer(DNSPacket dnsPacket)
    {
        if(dnsPacket.getData().getResponseValues()!= null)
            this.responseValues = new ArrayList<>(List.of(dnsPacket.getData().getResponseValues()));
        else
            this.responseValues = new ArrayList<>();
        if(dnsPacket.getData().getAuthoriteValues()!= null)
            this.authorityValues = new ArrayList<>(List.of(dnsPacket.getData().getAuthoriteValues()));
        else
            this.authorityValues = new ArrayList<>();
        if(dnsPacket.getData().getExtraValues()!= null)
            this.extraValues = new ArrayList<>(List.of(dnsPacket.getData().getExtraValues()));
        else
            this.extraValues = new ArrayList<>();
    }
    public void removeExpiredInfo(LocalDateTime tempoEntrada)
    {
        this.responseValues  = this.responseValues.stream().filter(v -> v.getTTL() > ChronoUnit.SECONDS.between(tempoEntrada, LocalDateTime.now())).toList();
        this.authorityValues = this.authorityValues.stream().filter(v -> v.getTTL() > ChronoUnit.SECONDS.between(tempoEntrada, LocalDateTime.now())).toList();
        this.extraValues     = this.extraValues.stream().filter(v -> v.getTTL() > ChronoUnit.SECONDS.between(tempoEntrada, LocalDateTime.now())).toList();
    }
    public Data getData(String name, byte type)
    {
        Data res = new Data(name,type);
        if(!responseValues.isEmpty())
            res.setResponseValues(responseValues.toArray(new Value[0]));
        if(!authorityValues.isEmpty())
            res.setAuthoriteValues(authorityValues.toArray(new Value[0]));
        if(!extraValues.isEmpty())
            res.setExtraValues(extraValues.toArray(new Value[0]));
        return res;
    }

    public String getNameCNAME(String dominio)
    {
        List<Value> val = this.responseValues.stream().filter(v -> v.getDominio().equals(dominio)).toList();
        return val.isEmpty() ? "" : val.get(0).getValue();
    }

    @Override
    public void addData(Value value) {
        this.responseValues.add(value);
    }

    @Override
    public boolean isEmpty() {
        return this.responseValues.isEmpty();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DadosCacheAnswer that = (DadosCacheAnswer) o;
        return this.responseValues.size() == that.responseValues.size() &&
                this.authorityValues.size() == that.authorityValues.size() &&
                this.extraValues.size() == that.extraValues.size() &&
                this.responseValues.stream().allMatch(v -> that.responseValues.stream().anyMatch(va -> va.equals(v)))&&
                this.authorityValues.stream().allMatch(v -> that.authorityValues.stream().anyMatch(va -> va.equals(v)))&&
                this.extraValues.stream().allMatch(v -> that.extraValues.stream().anyMatch(va -> va.equals(v)));
    }
}

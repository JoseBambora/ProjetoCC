import java.time.LocalDateTime;

/**
 * @author José Carvalho
 * Classe que define uma entrada na cache
 * Data criação: 7/11/2022
 * Data última atualização: 12/11/2022
 */
public class EntryCache
{
    public enum Origin {FILE, SP, OTHERS }
    private final String key;
    private final String dominio;
    private final Byte typeofValue;
    private final DadosCache dadosCache;
    private final Origin origem;
    private LocalDateTime tempoEntrada;
    public EntryCache(String dominio, Byte typeofValue,Origin origem)
    {
        this.key = new Tuple<>(dominio,typeofValue).toString();
        this.dominio = dominio;
        this.typeofValue = typeofValue;
        if(origem == Origin.FILE)
            this.dadosCache = new DadosCacheDB();
        else
            this.dadosCache = new DadosCacheAnswer();
        this.origem = origem;
        this.tempoEntrada = LocalDateTime.now();
    }

    public EntryCache(DNSPacket dnsPacket, Origin origem)
    {
        this.dominio = dnsPacket.getData().getName();
        this.typeofValue = dnsPacket.getData().getTypeOfValue();
        this.key = new Tuple<>(this.dominio,this.typeofValue).toString();
        this.dadosCache = new DadosCacheAnswer(dnsPacket);
        this.origem = origem;
        this.tempoEntrada = LocalDateTime.now();
    }


    public String getDominio() {
        return dominio;
    }

    public Byte getTypeofValue() {
        return typeofValue;
    }

    /**
     * Remove informação que já esteja expirada.
     */
    public void removeExpireInfo()
    {
        if(this.origem != Origin.FILE)
        {
            DadosCacheAnswer data = (DadosCacheAnswer) this.dadosCache;
            data.removeExpiredInfo(this.tempoEntrada);
        }
    }

    public String getKey() {
        return key;
    }

    public void setTempoEntrada(LocalDateTime tempoEntrada) {
        this.tempoEntrada = tempoEntrada;
    }

    /**
     * Buscar dados de uma posição da cache.
     * @return Dados.
     */
    public Data getData()
    {
        return this.dadosCache.getData(this.dominio,typeofValue);
    }

    public Origin getOrigem()
    {
        return this.origem;
    }

    /**
     * Adiciona um valor há cache.
     * @param value Valor a adicionar.
     */
    public void addValue(Value value)
    {
        this.dadosCache.addData(value);
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
        if(this.getOrigem() == EntryCache.Origin.FILE)
        {
            if(this.getTypeofValue() == cname && this.getDominio().equals(can))
            {
                DadosCacheDB dadosCacheAnswer = (DadosCacheDB) this.dadosCache;
                return dadosCacheAnswer.getNameCNAME(can);
            }
        }
        return "";
    }

    /**
     * Verifica se os dados da estão vazios
     * @return true caso sim, falso caso não.
     */
    public boolean isEmpty()
    {
        return this.dadosCache.isEmpty();
    }
}
package Cache;

import DNSPacket.Value;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author José Carvalho
 * Classe que define uma entrada na cache
 * DNSPacket.Data criação: 7/11/2022
 * DNSPacket.Data última atualização: 23/11/2022
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

    /**
     * Atualiza tempo de entrada
     */
    public void updateTempoEntrada()
    {
        this.estado = State.VALID;
        this.tempoEntrada = LocalDateTime.now();
    }

    /**
     * Devolve o domínio da entrada.
     * @return Domínio da entrada.
     */
    public String getDominio()
    {
        return this.dados.getDominio();
    }

    /**
     * Devolve o tipo da entrada
     * @return Tipo da entrada.
     */
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

    /**
     * Devolve a origem da entrada.
     * @return Origem da entrada.
     */
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

    /**
     * Verifica se uma entrada é válida.
     * @return true caso afirmativo, false caso contrário.
     */
    public boolean isValid()
    {
        return this.estado == State.VALID;
    }

    /**
     * Código de erro 1. Verificar entradas com servidores a contactar.
     * @param domain Domínio da query.
     * @param domainServer Domínio do servidor.
     * @return True se houver entradas (codigo de erro = 1), false caso contrário (código de erro = 2)
     */
    public boolean domainExist(String domain, String domainServer)
    {
        String []sections = this.getDominio().split("\\.");
        boolean found = true;
        int i = sections.length-1;
        while(found && i >= 0)
        {
            if (!domainServer.contains(sections[i]))
            {
                found = false;
            }
            else
                i--;
        }
        boolean res = false;
        if(!found)
        {
            res = domain.contains(sections[i]);
        }
        return res;

    }
}
/**
 * Interface para os dados de uma entrada da cache
 * @author José Carvalho
 * Data criação: 12/11/2022
 * Data última atualização: 12/11/2022
 */
public interface DadosCache
{
    public Data getData(String name, byte type);
    public void addData(Value value);
    public boolean isEmpty();
}

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Dados de uma posição da cache que corresponde a base de dados.
 * @author José Carvalho
 * Data criação: 12/11/2022
 * Data última atualização: 12/11/2022
 */
public class DadosCacheDB implements DadosCache
{
    private List<Value> campo;
    public DadosCacheDB()
    {
        this.campo = new ArrayList<>();
    }
    public Data getData(String name, byte type)
    {
        Data res = new Data(name,type);
        res.setResponseValues(campo.toArray(new Value[0]));
        return res;
    }

    @Override
    public void addData(Value value) {
        this.campo.add(value);
    }

    public String getNameCNAME(String dominio)
    {
        List<Value> val = this.campo.stream().filter(v -> v.getDominio().equals(dominio)).toList();
        return val.isEmpty() ? "" : val.get(0).getValue();

    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DadosCacheDB that = (DadosCacheDB) o;
        return this.campo.size() == that.campo.size() &&
               this.campo.stream().allMatch(v -> that.campo.stream().anyMatch(va -> va.equals(v)));
    }
}

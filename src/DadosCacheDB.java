import java.util.ArrayList;
import java.util.List;

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
        for(Value value : this.campo)
        {
            if(value.getDominio().equals(dominio))
                return value.getValue();
        }
        return "";
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}

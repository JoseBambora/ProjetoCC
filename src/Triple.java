import java.util.Objects;

/**
 * Classe auxiliar para criação de tuplos com 3 elementos
 * @author José Carvalho
 * Data criação: 23/10/2022
 * Data última atualização: 24/10/2022
 */
public class Triple <V1,V2,V3>
{
    private V1 value1;
    private V2 value2;
    private V3 value3;

    public Triple(V1 value1, V2 value2, V3 value3) {
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
    }

    public V1 getValue1() {
        return value1;
    }

    public V2 getValue2() {
        return value2;
    }

    public V3 getValue3() {
        return value3;
    }

    public V1 setValue1(V1 value) {
        V1 old = this.value1;
        this.value1 = value;
        return old;
    }

    public V2 setValue2(V2 value) {
        V2 old = this.value2;
        this.value2 = value;
        return old;
    }

    public V3 setValue3(V3 value3) {
        V3 old = this.value3;
        this.value3 = value3;
        return old;
    }

    @Override
    public String toString() {
        return "(" + value1 + "," +  value2 + "," + value3 + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
        return value1.equals(triple.value1);
    }
}

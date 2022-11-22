import java.util.Objects;

public class Value {
    private String dominio;
    private byte type;
    private String value;
    private int TTL;
    private int prioridade;

    public Value(String dominio, byte type, String value, int ttl, int prioridade) {
        this.dominio = dominio;
        this.type = type;
        this.value = value;
        this.TTL = ttl;
        this.prioridade = prioridade;
    }

    public Value(String dominio, byte type, String value, int ttl) {
        this.dominio = dominio;
        this.type = type;
        this.value = value;
        this.TTL = ttl;
        this.prioridade = -1;
    }

    public String getDominio() {
        return dominio;
    }

    public void setDominio(String dominio) {
        this.dominio = dominio;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(int prioridade) {
        this.prioridade = prioridade;
    }

    public int getTTL() {
        return TTL;
    }

    public void setTTL(int TTL) {
        this.TTL = TTL;
    }

    public static Value stringToValue (String value) throws TypeOfValueException {
        String[] fields = value.split(" ");
        int prioridade = -1;
        if (fields.length == 5) {
            prioridade = Integer.parseInt(fields[4]);
        }
        return new Value(fields[0],(byte) Data.typeOfValueConvert(fields[1]),fields[2],Integer.parseInt(fields[3]),prioridade);
    }

    @Override
    public String toString() {
        String out;
        if (this.prioridade != -1) {
            out = this.dominio + " " + Data.typeOfValueConvertSring(this.type) + " " + this.value + " " + this.TTL + " " + this.prioridade;
        }
        else {
            out = this.dominio + " " + Data.typeOfValueConvertSring(this.type) + " " + this.value + " " + this.TTL;
        }
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value value1 = (Value) o;
        return type == value1.type && TTL == value1.TTL && prioridade == value1.prioridade && Objects.equals(dominio, value1.dominio) && Objects.equals(value, value1.value);
    }
}

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

    @Override
    public String toString() {
        String prioridade = "";
        if(this.prioridade != -1)
            prioridade = Integer.toString(this.prioridade);
        return this.dominio + " " + this.type + " " + this.value + " " + this.TTL + " " + prioridade;
    }
}

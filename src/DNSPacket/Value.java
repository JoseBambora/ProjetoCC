package DNSPacket;

import Exceptions.TypeOfValueException;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * @Author João Martins
 * @Class Value
 * Created date: 22/10/2022
 * Last Update: 07/11/2022
 */
public class Value {
    private String dominio;
    private byte type;
    private String value;
    private int TTL;
    private int prioridade;

    /**
     * Construtor da classe Value dado o dominio, type, value, ttl e prioridade.
     */
    public Value(String dominio, byte type, String value, int ttl, int prioridade) {
        this.dominio = dominio;
        this.type = type;
        this.value = value;
        this.TTL = ttl;
        this.prioridade = prioridade;
    }

    /**
     * Construtor da classe Value dado o dominio, type, value e ttl.
     */
    public Value(String dominio, byte type, String value, int ttl) {
        this.dominio = dominio;
        this.type = type;
        this.value = value;
        this.TTL = ttl;
        this.prioridade = -1;
    }

    /**
     * Getters e setters.
     */
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

    /**
     * Parsing de uma string para obter um novo objeto Value.
     */
    public static Value stringToValue (String value) throws TypeOfValueException {
        String[] fields = value.split(" ");
        int prioridade = -1;
        if (fields.length == 5) {
            prioridade = Integer.parseInt(fields[4]);
        }
        return new Value(fields[0], Data.typeOfValueConvert(fields[1]),fields[2],Integer.parseInt(fields[3]),prioridade);
    }

    /**
     * Representação de um objeto value.
     */
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
        return type == value1.type && Objects.equals(dominio, value1.dominio) && Objects.equals(value, value1.value);
    }

    public int numberBytes()
    {
        return this.dominio.getBytes().length + 1 + this.value.getBytes().length + 8;
    }

    public static Value bytesToValues(byte [] bytes)
    {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        int len1 = byteBuffer.getInt();
        byte []dom = new byte[len1];
        byteBuffer.get(dom);
        String domi = new String(dom);
        byte t = byteBuffer.get();
        int len2 = byteBuffer.getInt();
        byte []val = new byte[len2];
        byteBuffer.get(val);
        String valu = new String(val);
        int ttl = byteBuffer.getInt();
        int pri = byteBuffer.getInt();
        return new Value(domi,t,valu,ttl,pri);
    }

    public byte[] valuesToBytes()
    {
        byte []dom = this.dominio.getBytes();
        byte []val = this.value.getBytes();
        int size = (Integer.SIZE / 8) * 4 + dom.length + 1 + val.length;
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        byteBuffer.putInt(dom.length);
        byteBuffer.put(dom);
        byteBuffer.put(this.type);
        byteBuffer.putInt(val.length);
        byteBuffer.put(val);
        byteBuffer.putInt(this.TTL);
        byteBuffer.putInt(this.prioridade);
        return byteBuffer.array();
    }
}

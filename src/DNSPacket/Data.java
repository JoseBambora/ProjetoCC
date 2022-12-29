package DNSPacket;

import Exceptions.TypeOfValueException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @Author João Martins
 * @Class Data
 * Created date: 22/10/2022
 * Last Update: 5/11/2022
 */
public class Data {
    private String name;
    private byte typeOfValue;
    private Value[] responseValues;
    private Value[] authoriteValues;
    private Value[] extraValues;


    /**
     * Construtor de classe Data dado o name e o typeOfValue.
     */
    public Data(String name, byte typeOfValue) {
        this.name = name;
        this.typeOfValue = typeOfValue;
        this.responseValues = null;
        this.authoriteValues = null;
        this.extraValues = null;
    }

    /**
     * Construtor da classe Data dados o name, typeOfValue, responseValues, authoritiesValues e extraValues.
     */
    public Data(String name, byte typeOfValue, Value[] responseValues, Value[] authoriteValues, Value[] extraValues) {
        this.name = name;
        this.typeOfValue = typeOfValue;
        this.responseValues = responseValues;
        this.authoriteValues = authoriteValues;
        this.extraValues = extraValues;
    }

    /**
     * Converto uma string com um type para o inteiro respetivo.
     * @throws TypeOfValueException tipo inválido.
     */
    public static byte typeOfValueConvert (String type) throws TypeOfValueException {
        byte ret;
        switch (type) {
            case "SOASP":
                ret = 0; break;
            case "SOAADMIN":
                ret = 1; break;
            case "SOASERIAL":
                ret = 2; break;
            case "SOAREFRESH":
                ret = 3; break;
            case "SOARETRY":
                ret = 4; break;
            case "SOAEXPIRE":
                ret = 5; break;
            case "NS":
                ret = 6; break;
            case "A":
                ret = 7; break;
            case "CNAME":
                ret = 8; break;
            case "MX":
                ret = 9; break;
            case "PTR":
                ret = 10; break;
            default:
                throw new TypeOfValueException("The type of value passed as argument does not exist.");
        };
        return ret;
    }

    /**
     * Converte o tipo na sua representação em string.
     */
    public static String typeOfValueConvertSring (byte type) {
        String ret = null;
        switch (type) {
            case 0:
                ret = "SOASP"; break;
            case 1:
                ret = "SOAADMIN"; break;
            case 2:
                ret = "SOASERIAL"; break;
            case 3:
                ret = "SOAREFRESH"; break;
            case 4:
                ret = "SOARETRY"; break;
            case 5:
                ret = "SOAEXPIRE"; break;
            case 6:
                ret = "NS"; break;
            case 7:
                ret = "A"; break;
            case 8:
                ret = "CNAME"; break;
            case 9:
                ret = "MX"; break;
            case 10:
                ret = "PTR"; break;
            default:
                break;
        };
        return ret;
    }

    /**
     * Getters e setters.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getTypeOfValue() {
        return typeOfValue;
    }

    public void setTypeOfValue(byte typeOfValue) {
        this.typeOfValue = typeOfValue;
    }

    public Value[] getResponseValues() {
        return responseValues;
    }

    public void setResponseValues(Value[] responseValues) {
        this.responseValues = responseValues;
    }

    public Value[] getAuthoriteValues() {
        return authoriteValues;
    }

    public void setAuthoriteValues(Value[] authoriteValues) {
        this.authoriteValues = authoriteValues;
    }

    public Value[] getExtraValues() {
        return extraValues;
    }

    public void setExtraValues(Value[] extraValues) {
        this.extraValues = extraValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Data data = (Data) o;
        return typeOfValue == data.typeOfValue &&
                name.equals(data.name) &&
                Arrays.equals(responseValues, data.responseValues) &&
                Arrays.equals(authoriteValues, data.authoriteValues) &&
                Arrays.equals(extraValues, data.extraValues);
    }

    /**
     * Conversão de um array de values para a sua representação textual.
     */
    public static String valuesToString (Value[] values) {
        StringBuilder out = new StringBuilder();
        out.append("\n");
        for (int i = 0, tam = values.length; i < tam; i++) {
            out.append(values[i]);
            if (i == tam - 1) out.append(";");
            else out.append(",\n");
        }
        return out.toString();
    }

    /**
     * Converte a parte da Data num pacote DNS para o formato conciso.
     */
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(name);
        out.append(",");
        out.append(Data.typeOfValueConvertSring(typeOfValue));
        out.append(";");
        if (responseValues!=null) out.append(Data.valuesToString(responseValues));
        if (authoriteValues!=null) out.append(Data.valuesToString(authoriteValues));
        if (extraValues!=null) out.append(Data.valuesToString(extraValues));
        return out.toString();
    }

    /**
     * Converte a parte da Data num pacote DNS para um formato mais amigo do utilizadorl.
     * @return
     */
    public String showData() {
        StringBuilder out = new StringBuilder();
        out.append("# Data: Query-info\nQUERY-INFO.NAME = ");
        out.append(name);
        out.append(", QUERY-INFO.TYPE = ");
        out.append(Data.typeOfValueConvertSring(typeOfValue));
        out.append(";\n# Data: List of Response, Authorities and Extra Values\n");
        int i, tam;
        if (responseValues!=null) {
            for (i = 0, tam = responseValues.length; i < tam; i++) {
                out.append("RESPONSE-VALUES = ");
                out.append(responseValues[i]);
                if (i == tam - 1) out.append(";\n");
                else out.append(",\n");
            }
        }
        else out.append("RESPONSE-VALUES = (Null)\n");

        if (authoriteValues!=null) {
            for (i = 0, tam = authoriteValues.length; i < tam; i++) {
                out.append("AUTHORITIES-VALUES = ");
                out.append(authoriteValues[i]);
                if (i == tam - 1) out.append(";\n");
                else out.append(",\n");
            }
        }
        else out.append("AUTHORITIES-VALUES = (Null)\n");

        if (extraValues!=null) {
            for (i = 0, tam = extraValues.length; i < tam; i++) {
                out.append("EXTRA-VALUES = ");
                out.append(extraValues[i]);
                if (i == tam - 1) out.append(";\n");
                else out.append(",\n");
            }
        }
        else out.append("EXTRA-VALUES = (Null)");
        return out.toString();
    }

    public static Data bytesToData(byte [] bytes,byte rv, byte av, byte ev)
    {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        int len = byteBuffer.getInt();
        byte []aux1 = new byte[len];
        byteBuffer.get(aux1);
        String name = new String(aux1);
        byte t = byteBuffer.get();
        int len1 = byteBuffer.getInt();
        int len2 = byteBuffer.getInt();
        int len3 = byteBuffer.getInt();
        byte []aux2 = new byte[len1];
        byte []aux3 = new byte[len2];
        byte []aux4 = new byte[len3];
        byteBuffer.get(aux2);
        byteBuffer.get(aux3);
        byteBuffer.get(aux4);
        List<Value> rvv = new ArrayList<>();
        List<Value> avv = new ArrayList<>();
        List<Value> evv = new ArrayList<>();
        for(int i = 0; i < rv; i++)
            rvv.add(Value.bytesToValues(aux2));
        for(int i = 0; i < av; i++)
            avv.add(Value.bytesToValues(aux3));
        for(int i = 0; i < ev; i++)
            evv.add(Value.bytesToValues(aux4));
        return new Data(name,t,rvv.toArray(new Value[1]),avv.toArray(new Value[1]),evv.toArray(new Value[1]));
    }

    public byte[] dataToBytes()
    {
        byte[] n = this.name.getBytes();
        List<byte[]> values = new ArrayList<>();
        int len1 = 0, len2 = 0, len3 = 0;
        if(this.responseValues != null)
        {
            for (Value value : this.responseValues) {
                byte[] b = value.valuesToBytes();
                values.add(b);
                len1 += b.length;
            }
        }
        if(this.authoriteValues != null)
        {
            for (Value value : this.authoriteValues) {
                byte[] b = value.valuesToBytes();
                values.add(b);
                len2 += b.length;
            }
        }
        if(this.extraValues != null)
        {
            for (Value value : this.extraValues) {
                byte[] b = value.valuesToBytes();
                values.add(b);
                len3 += b.length;
            }
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + n.length + 1 + 12 + len1 + len2 + len3);
        byteBuffer.putInt(this.name.length());
        byteBuffer.put(n);
        byteBuffer.put(this.typeOfValue);
        byteBuffer.putInt(len1);
        byteBuffer.putInt(len2);
        byteBuffer.putInt(len3);
        values.forEach(byteBuffer::put);
        return byteBuffer.array();
    }

}


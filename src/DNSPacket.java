import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class DNSPacket implements Serializable {
    private Header header;
    private Data data;



    public DNSPacket(short messageID, boolean flagQ, boolean flagR, boolean flagA, String name, byte typeOfValue) {
        this.header = new Header(messageID,flagQ,flagR,flagA);
        this.data = new Data(name,typeOfValue);
    }

    public DNSPacket(short messageID, boolean flagQ, boolean flagR, boolean flagA, byte responseCode, byte numberOfValues, byte numberOfAuthorites, byte numberOfExtraValues, String name, byte typeOfValue, Value[] responseValues, Value[] authoriteValues, Value[] extraValues) {
        this.header = new Header(messageID,flagQ,flagR,flagA,responseCode,numberOfValues,numberOfAuthorites,numberOfExtraValues);
        this.data = new Data(name,typeOfValue,responseValues,authoriteValues,extraValues);
    }

    public DNSPacket(Header header, Data data) {
        this.header = header;
        this.data = data;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return header.toString() + data.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DNSPacket dnsPacket = (DNSPacket) o;
        return header.equals(dnsPacket.header) &&
               data.equals(dnsPacket.data);
    }

    public static byte typeOfValueConvert (String type) throws TypeOfValueException {
        byte ret;
        switch (type) {
            case "SOASP":
                ret = (byte) 0;
                break;
            case "SOAADMIN":
                ret = (byte) 1;
                break;
            case "SOASERIAL":
                ret = (byte) 2;
                break;
            case "SOAREFRESH":
                ret = (byte) 3;
                break;
            case "SOARETRY":
                ret = (byte) 4;
                break;
            case "SOAEXPIRE":
                ret = (byte) 5;
                break;
            case "NS":
                ret = (byte) 6;
                break;
            case "A":
                ret = (byte) 7;
                break;
            case "CNAME":
                ret = (byte) 8;
                break;
            case "MX":
                ret = (byte) 9;
                break;
            case "PTR":
                ret = (byte) 10;
            default:
                throw new TypeOfValueException("");
        };
        return ret;
    }


    public byte[] dnsPacketToBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(this);
        oos.flush();
        return bos.toByteArray();
    }

    public static DNSPacket bytesToDnsPacket(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
        ObjectInputStream oi = new ObjectInputStream(bi);
        DNSPacket r = (DNSPacket) oi.readObject();
        bi.close();
        oi.close();
        return r;
    }

}

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

    public DNSPacket(short messageID, boolean flagQ, boolean flagR, boolean flagA, byte responseCode, byte numberOfValues, byte numberOfAuthorites, byte numberOfExtraValues, String name, byte typeOfValue, String[] responseValues, String[] authoriteValues, String[] extraValues) {
        this.header = new Header(messageID,flagQ,flagR,flagA,responseCode,numberOfValues,numberOfAuthorites,numberOfExtraValues);
        this.data = new Data(name,typeOfValue,responseValues,authoriteValues,extraValues);
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

    public static byte typeOfValueConvert (String type) throws Exception {
        return switch (type) {
            case "SOASP" -> (byte) 0;
            case "SOAADMIN" -> (byte) 1;
            case "SOASERIAL" -> (byte) 2;
            case "SOAREFRESH" -> (byte) 3;
            case "SOARETRY" -> (byte) 4;
            case "SOAEXPIRE" -> (byte) 5;
            case "NS" -> (byte) 6;
            case "A" -> (byte) 7;
            case "CNAME" -> (byte) 8;
            case "MX" -> (byte) 9;
            case "PTR" -> (byte) 10;
            default -> throw new Exception();
        };
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

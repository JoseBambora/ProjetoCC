package DNSPacket;

import Exceptions.TypeOfValueException;

public class DNSPacket {
    private Header header;
    private Data data;

    public DNSPacket(short messageID, byte flags, String name, byte typeOfValue) {
        this.header = new Header(messageID,flags);
        this.data = new Data(name,typeOfValue);
    }

    public DNSPacket(short messageID, byte flags, byte responseCode, byte numberOfValues, byte numberOfAuthorites, byte numberOfExtraValues, String name, byte typeOfValue, Value[] responseValues, Value[] authoriteValues, Value[] extraValues) {
        this.header = new Header(messageID,flags,responseCode,numberOfValues,numberOfAuthorites,numberOfExtraValues);
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

    public String showDNSPacket() {
        return header.showHeader() + data.showData();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DNSPacket dnsPacket = (DNSPacket) o;
        return header.equals(dnsPacket.header) &&
                data.equals(dnsPacket.data);
    }

    public byte[] dnsPacketToBytes() {
        return this.toString().getBytes();
    }

    public static DNSPacket bytesToDnsPacket(byte[] bytes) throws TypeOfValueException {
        String packet = new String(bytes);
        String[] fields = packet.split(";");
        Header h = Header.stringToHeader(fields[0]);
        String[] qi = fields[1].split(",");
        String name = qi[0];
        byte tv = Data.typeOfValueConvert(qi[1]);

        int i = 0;
        Value[] rv = null;
        if (h.getNumberOfValues()>0) {
            rv = new Value[h.getNumberOfValues()];
            String[] rvAux = fields[2].split(",");
            for (String str : rvAux) {
                rv[i++] = Value.stringToValue(str.substring(1));
            }
        }
        i = 0;
        Value[] av = null;
        if (h.getNumberOfAuthorites()>0) {
            av = new Value[h.getNumberOfAuthorites()];
            String[] avAux = fields[3].split(",");
            for (String str : avAux) {
                av[i++] = Value.stringToValue(str.substring(1));
            }
        }
        i = 0;
        Value[] ev = null;
        if (h.getNumberOfExtraValues()>0) {
            ev = new Value[h.getNumberOfExtraValues()];
            String[] evAux = fields[4].split(",");
            for (String str : evAux) {
                ev[i++] = Value.stringToValue(str.substring(1));
            }
        }
        return new DNSPacket(h,new Data(name,tv,rv,av,ev));
    }


}

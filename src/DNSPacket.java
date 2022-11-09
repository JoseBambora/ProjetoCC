public class DNSPacket {
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

    public static String typeOfValueConvertSring (byte type) {
        String ret;
        switch (type) {
            case 0:
                ret = "SOASP";
                break;
            case 1:
                ret = "SOAADMIN";
                break;
            case 2:
                ret = "SOASERIAL";
                break;
            case 3:
                ret = "SOAREFRESH";
                break;
            case 4:
                ret = "SOARETRY";
                break;
            case 5:
                ret = "SOAEXPIRE";
                break;
            case 6:
                ret = "NS";
                break;
            case 7:
                ret = "A";
                break;
            case 8:
                ret = "CNAME";
                break;
            case 9:
                ret = "MX";
                break;
            case 10:
                ret = "PTR";
            default:
                ret = "";
        };
        return ret;
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
        byte tv = DNSPacket.typeOfValueConvert(qi[1]);

        int i = 0;
        Value[] rv = new Value[h.getNumberOfValues()];
        String[] rvAux = fields[2].split(",");
        for (String str : rvAux) {
            rv[i++] = Value.stringToValue(str.substring(1));
        }
        i = 0;
        Value[] av = new Value[h.getNumberOfAuthorites()];
        String[] avAux = fields[3].split(",");
        for (String str : avAux) {
            av[i++] = Value.stringToValue(str.substring(1));
        }
        i = 0;
        Value[] ev = new Value[h.getNumberOfExtraValues()];
        String[] evAux = fields[4].split(",");
        for (String str : evAux) {
            ev[i++] = Value.stringToValue(str.substring(1));
        }
        return new DNSPacket(h,new Data(name,tv,rv,av,ev));
    }


}

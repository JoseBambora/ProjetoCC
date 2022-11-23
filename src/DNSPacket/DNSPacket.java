package DNSPacket;

import Exceptions.TypeOfValueException;

/**
 * @Author João Martins
 * @Class DNSPacket
 * Created date: 03/11/2022
 * Last update: 17/11/2022
 */
public class DNSPacket {
    private Header header;
    private Data data;

    /**
     * Construtor da classe DNSPacket dados o messageID, flags, name e typeofValue.
     */
    public DNSPacket(short messageID, byte flags, String name, byte typeOfValue) {
        this.header = new Header(messageID,flags);
        this.data = new Data(name,typeOfValue);
    }

    /**
     * Construtor da classe DNSPacket dados o header e a data.
     */
    public DNSPacket(Header header, Data data) {
        this.header = header;
        this.data = data;
    }

    /**
     * Getters and setters
     */
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

    /**
     * Representação em formato conciso.
     * @return string representativa do pacote dns.
     */
    @Override
    public String toString() {
        return header.toString() + data.toString();
    }

    /**
     * Representação em formato de apresentação para o cliente.
     * @return string a ser apresentada ao cliente.
     */
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

    /**
     * Controi o array de bytes para ser enviado através do socket udp.
     */
    public byte[] dnsPacketToBytes() {
        return this.toString().getBytes();
    }

    /**
     * Constroi a partir de um array de bytes vindo de um socket UDP o pacote DNS correspondente.
     */
    public static DNSPacket bytesToDnsPacket(byte[] bytes) throws TypeOfValueException {
        String packet = new String(bytes);
        String[] fields = packet.split(";");
        Header h = Header.stringToHeader(fields[0]);
        String[] qi = fields[1].split(",");
        String name = qi[0];
        byte tv = Data.typeOfValueConvert(qi[1]);

        int i = 0;
        int ifields = 2;
        Value[] rv = null;
        if (h.getNumberOfValues()>0) {
            rv = new Value[h.getNumberOfValues()];
            String[] rvAux = fields[ifields++].split(",");
            for (String str : rvAux) {
                rv[i++] = Value.stringToValue(str.substring(1));
            }
        }
        i = 0;
        Value[] av = null;
        if (h.getNumberOfAuthorites()>0) {
            av = new Value[h.getNumberOfAuthorites()];
            String[] avAux = fields[ifields++].split(",");
            for (String str : avAux) {
                av[i++] = Value.stringToValue(str.substring(1));
            }
        }
        i = 0;
        Value[] ev = null;
        if (h.getNumberOfExtraValues()>0) {
            ev = new Value[h.getNumberOfExtraValues()];
            String[] evAux = fields[ifields].split(",");
            for (String str : evAux) {
                ev[i++] = Value.stringToValue(str.substring(1));
            }
        }
        return new DNSPacket(h,new Data(name,tv,rv,av,ev));
    }


}

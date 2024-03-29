package DNSPacket;


import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * @Author João Martins
 * @Class Header
 * Created date: 22/10/2022
 * Last Update: 07/11/2022
 */
public class Header {
    private short messageID;
    private byte flags;
    private byte responseCode;
    private byte numberOfValues;
    private byte numberOfAuthorites;
    private byte numberOfExtraValues;

    /**
     * Construtor da classe header dados os campos: messageID, flags,responseCode, numberOfValues, numberOfAuthorites, numberOfExtraValues.
     */
    public Header(short messageID, byte flags, byte responseCode, byte numberOfValues, byte numberOfAuthorites, byte numberOfExtraValues) {
        this.messageID = messageID;
        this.flags = flags;
        this.responseCode = responseCode;
        this.numberOfValues = numberOfValues;
        this.numberOfAuthorites = numberOfAuthorites;
        this.numberOfExtraValues = numberOfExtraValues;
    }

    /**
     * Construtor da classe header dados os campos: messageID, flags.
     */
    public Header(short messageID, byte flags) {
        this.messageID = messageID;
        this.flags = flags;
        this.responseCode = 0;
        this.numberOfValues = 0;
        this.numberOfAuthorites = 0;
        this.numberOfExtraValues = 0;
    }

    /**
     * Getters e setters.
     */
    public short getMessageID() {
        return messageID;
    }

    public void setMessageID(short messageID) {
        this.messageID = messageID;
    }

    public byte getFlags() {
        return flags;
    }

    public void setFlags(byte flags) {
        this.flags = flags;
    }

    public byte getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(byte responseCode) {
        this.responseCode = responseCode;
    }

    public byte getNumberOfValues() {
        return numberOfValues;
    }

    public void setNumberOfValues(byte numberOfValues) {
        this.numberOfValues = numberOfValues;
    }

    public byte getNumberOfAuthorites() {
        return numberOfAuthorites;
    }

    public void setNumberOfAuthorites(byte numberOfAuthorites) {
        this.numberOfAuthorites = numberOfAuthorites;
    }

    public byte getNumberOfExtraValues() {
        return numberOfExtraValues;
    }

    public void setNumberOfExtraValues(byte numberOfExtraValues) {
        this.numberOfExtraValues = numberOfExtraValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Header header = (Header) o;
        return messageID == header.messageID &&
                flags == header.flags &&
                responseCode == header.responseCode &&
                numberOfValues == header.numberOfValues &&
                numberOfAuthorites == header.numberOfAuthorites &&
                numberOfExtraValues == header.numberOfExtraValues;
    }

    /**
     * Converte o valor das flags para a sua representação em string.
     */
    public String flagsToString () {
        String out;
        switch (this.flags) {
            case 1: //100
                out = "Q"; break;
            case 2: //010
                out = "R"; break;
            case 3: //110
                out = "Q+R"; break;
            case 4: //001
                out = "A"; break;
            case 5: //101
                out = "Q+A"; break;
            case 6: //011
                out = "R+A"; break;
            case 7: //111
                out = "Q+R+A"; break;
            default: //000
                out = ""; break;
        }
        return out;
    }

    /**
     * Dado a string com a representação das flags, obtemos o valor correspondente.
     */
    public static byte flagsStrToByte (String flags) {
        byte out;
        switch (flags) {
            case "Q": //100
                out = 1; break;
            case "R": //010
                out = 2; break;
            case "Q+R": //110
                out = 3; break;
            case "A": //001
                out = 4; break;
            case "Q+A": //101
                out = 5; break;
            case "R+A": //011
                out = 6; break;
            case "Q+R+A": //111
                out = 7; break;
            default: //000
                out = 0; break;
        }
        return out;
    }

    /**
     * Parsing de uma string com o header.
     */
    public static Header stringToHeader (String header) {
        String[] headerFields = header.split(",");
        short mId = (short) Integer.parseInt(headerFields[0]);
        byte fs = Header.flagsStrToByte(headerFields[1]);
        byte rc = (byte) Integer.parseInt(headerFields[2]);
        byte nv = (byte) Integer.parseInt(headerFields[3]);
        byte na = (byte) Integer.parseInt(headerFields[4]);
        byte ne = (byte) Integer.parseInt(headerFields[5]);
        return new Header(mId,fs,rc,nv,na,ne);
    }

    /**
     * Representação do header em formato conciso.
     */
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(Short.toUnsignedInt(messageID));
        out.append(",");
        out.append(flagsToString());
        out.append(",");
        out.append(responseCode);
        out.append(",");
        out.append(Byte.toUnsignedInt(numberOfValues));
        out.append(",");
        out.append(Byte.toUnsignedInt(numberOfAuthorites));
        out.append(",");
        out.append(Byte.toUnsignedInt(numberOfExtraValues));
        out.append(";");
        return out.toString();
    }

    /**
     * Representação do header para apresentação ao cliente.
     */
    public String showHeader() {
        StringBuilder out = new StringBuilder();
        out.append("# Header\n");
        out.append("MESSAGE-ID = ");
        out.append(Short.toUnsignedInt(messageID));
        out.append(", FLAGS = ");
        out.append(flagsToString());
        out.append(", RESPONSE CODE = ");
        out.append(responseCode);
        out.append(",\nN-VALUES = ");
        out.append(Byte.toUnsignedInt(numberOfValues));
        out.append(", N-AUTHORITIES = ");
        out.append(Byte.toUnsignedInt(numberOfAuthorites));
        out.append(", N-EXTRA-VALUES = ");
        out.append(Byte.toUnsignedInt(numberOfExtraValues));
        out.append(";\n");
        return out.toString();
    }

    public static Header bytesToHeader(byte [] bytes)
    {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        short mi = byteBuffer.getShort();
        byte f = byteBuffer.get();
        byte rc = byteBuffer.get();
        byte nv = byteBuffer.get();
        byte av = byteBuffer.get();
        byte ev = byteBuffer.get();
        return new Header(mi,f,rc,nv,av,ev);
    }

    public byte[] headerToBytes()
    {
        ByteBuffer byteBuffer = ByteBuffer.allocate(7);
        byteBuffer.putShort(this.messageID);
        byteBuffer.put(this.flags);
        byteBuffer.put(this.responseCode);
        byteBuffer.put(this.numberOfValues);
        byteBuffer.put(this.numberOfAuthorites);
        byteBuffer.put(this.numberOfExtraValues);
        return byteBuffer.array();
    }

}
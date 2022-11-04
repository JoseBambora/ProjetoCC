import java.io.*;
import java.util.Arrays;
import java.util.Objects;

public class DNSPacket implements Serializable {
    private short messageID;
    private boolean flagQ;
    private boolean flagR;
    private boolean flagA;
    private byte responseCode; // 0 1 2 3 ver situações
    private byte numberOfValues;
    private byte numberOfAuthorites;
    private byte numberOfExtraValues;
    private String name;
    private byte typeOfValue; // 11 tipos
    private String[] responseValues;
    private String[] authoriteValues;
    private String[] extraValues;

    public DNSPacket(short messageID, boolean flagQ, boolean flagR, boolean flagA, String name, byte typeOfValue) {
        this.messageID = messageID;
        this.flagQ = flagQ;
        this.flagR = flagR;
        this.flagA = flagA;
        this.responseCode = 0;
        this.numberOfValues = 0;
        this.numberOfAuthorites = 0;
        this.numberOfExtraValues = 0;
        this.name = name;
        this.typeOfValue = typeOfValue;
        this.responseValues = null;
        this.authoriteValues = null;
        this.extraValues = null;
    }

    public DNSPacket(short messageID, boolean flagQ, boolean flagR, boolean flagA, byte responseCode, byte numberOfValues, byte numberOfAuthorites, byte numberOfExtraValues, String name, byte typeOfValue, String[] responseValues, String[] authoriteValues, String[] extraValues) {
        this.messageID = messageID;
        this.flagQ = flagQ;
        this.flagR = flagR;
        this.flagA = flagA;
        this.responseCode = responseCode;
        this.numberOfValues = numberOfValues;
        this.numberOfAuthorites = numberOfAuthorites;
        this.numberOfExtraValues = numberOfExtraValues;
        this.name = name;
        this.typeOfValue = typeOfValue;
        this.responseValues = responseValues;
        this.authoriteValues = authoriteValues;
        this.extraValues = extraValues;
    }

    public short getMessageID() {
        return messageID;
    }

    public void setMessageID(short messageID) {
        this.messageID = messageID;
    }

    public boolean isFlagQ() {
        return flagQ;
    }

    public void setFlagQ(boolean flagQ) {
        this.flagQ = flagQ;
    }

    public boolean isFlagR() {
        return flagR;
    }

    public void setFlagR(boolean flagR) {
        this.flagR = flagR;
    }

    public boolean isFlagA() {
        return flagA;
    }

    public void setFlagA(boolean flagA) {
        this.flagA = flagA;
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

    public String[] getResponseValues() {
        return responseValues;
    }

    public void setResponseValues(String[] responseValues) {
        this.responseValues = responseValues;
    }

    public String[] getAuthoriteValues() {
        return authoriteValues;
    }

    public void setAuthoriteValues(String[] authoriteValues) {
        this.authoriteValues = authoriteValues;
    }

    public String[] getExtraValues() {
        return extraValues;
    }

    public void setExtraValues(String[] extraValues) {
        this.extraValues = extraValues;
    }

    public String flagsToString () {
        String out;
        if (this.flagQ && this.flagR && this.flagA) out = "Q+R+A";      // 1 1 1
        else if (this.flagQ && this.flagR) out = "Q+R";                 // 1 1 0
        else if (this.flagQ && this.flagA) out = "Q+A";                 // 1 0 1
        else if (this.flagQ) out = "Q";                                 // 1 0 0
        else if (this.flagR && this.flagA) out = "R+A";                 // 0 1 1
        else if (this.flagR) out = "R";                                 // 0 1 0
        else if (this.flagA) out = "A";                                 // 0 0 1
        else out = "";                                                  // 0 0 0
        return out;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(messageID);
        out.append(",");
        out.append(flagsToString());
        out.append(",");
        out.append(responseCode);
        out.append(",");
        out.append(numberOfValues);
        out.append(",");
        out.append(numberOfAuthorites);
        out.append(",");
        out.append(numberOfExtraValues);
        out.append(";");
        out.append(name);
        out.append(",");
        out.append(typeOfValue);
        out.append(";\n");
        int i, tam;
        if (responseValues!=null) {
            for (i = 0, tam = responseValues.length; i < tam; i++) {
                out.append(responseValues[i]);
                if (i == tam - 1) out.append(";\n");
                else out.append(",\n");
            }
        }
        if (authoriteValues!=null) {
            for (i = 0, tam = authoriteValues.length; i < tam; i++) {
                out.append(authoriteValues[i]);
                if (i == tam - 1) out.append(";\n");
                else out.append(",\n");
            }
        }
        if (extraValues!=null) {
            for (i = 0, tam = extraValues.length; i < tam; i++) {
                out.append(extraValues[i]);
                if (i == tam - 1) out.append(";\n");
                else out.append(",\n");
            }
        }
        return out.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DNSPacket dnsPacket = (DNSPacket) o;
        return messageID == dnsPacket.messageID &&
               flagQ == dnsPacket.flagQ &&
               flagR == dnsPacket.flagR &&
               flagA == dnsPacket.flagA &&
               responseCode == dnsPacket.responseCode &&
               numberOfValues == dnsPacket.numberOfValues &&
               numberOfAuthorites == dnsPacket.numberOfAuthorites &&
               numberOfExtraValues == dnsPacket.numberOfExtraValues &&
               typeOfValue == dnsPacket.typeOfValue &&
               name.equals(dnsPacket.name) &&
               Arrays.equals(responseValues, dnsPacket.responseValues) &&
               Arrays.equals(authoriteValues, dnsPacket.authoriteValues) &&
               Arrays.equals(extraValues, dnsPacket.extraValues);
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

public class Header {
    private short messageID;
    private boolean flagQ;
    private boolean flagR;
    private boolean flagA;
    private byte responseCode;
    private byte numberOfValues;
    private byte numberOfAuthorites;
    private byte numberOfExtraValues;

    public Header(short messageID, boolean flagQ, boolean flagR, boolean flagA, byte responseCode, byte numberOfValues, byte numberOfAuthorites, byte numberOfExtraValues) {
        this.messageID = messageID;
        this.flagQ = flagQ;
        this.flagR = flagR;
        this.flagA = flagA;
        this.responseCode = responseCode;
        this.numberOfValues = numberOfValues;
        this.numberOfAuthorites = numberOfAuthorites;
        this.numberOfExtraValues = numberOfExtraValues;
    }


    public Header(short messageID, boolean flagQ, boolean flagR, boolean flagA) {
        this.messageID = messageID;
        this.flagQ = flagQ;
        this.flagR = flagR;
        this.flagA = flagA;
        this.responseCode = 0;
        this.numberOfValues = 0;
        this.numberOfAuthorites = 0;
        this.numberOfExtraValues = 0;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Header header = (Header) o;
        return messageID == header.messageID &&
                flagQ == header.flagQ &&
                flagR == header.flagR &&
                flagA == header.flagA &&
                responseCode == header.responseCode &&
                numberOfValues == header.numberOfValues &&
                numberOfAuthorites == header.numberOfAuthorites &&
                numberOfExtraValues == header.numberOfExtraValues;
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

    public static Header stringToHeader (String header) {
        String[] headerFields = header.split(",");
        short mId = (short) Integer.parseInt(headerFields[0]);
        boolean q = false;
        boolean r = false;
        boolean a = false;
        switch (headerFields[1]) {
            case "Q+R+A":
                q = true;
                r = true;
                a = true;
                break;
            case "Q+R":
                q = true;
                r = true;
                break;
            case "Q+A":
                q = true;
                a = true;
                break;
            case "Q":
                q = true;
                break;
            case "R+A":
                r = true;
                a = true;
                break;
            case "R":
                r = true;
                break;
            case "A":
                a = true;
                break;
            default:
                break;
        }
        byte rc = (byte) Integer.parseInt(headerFields[2]);
        byte nv = (byte) Integer.parseInt(headerFields[3]);
        byte na = (byte) Integer.parseInt(headerFields[4]);
        byte ne = (byte) Integer.parseInt(headerFields[5]);
        return new Header(mId,q,r,a,rc,nv,na,ne);
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
        return out.toString();
    }
}
import java.util.Arrays;

public class Data {
    private String name;
    private byte typeOfValue;
    private Value[] responseValues;
    private Value[] authoriteValues;
    private Value[] extraValues;


    public Data(String name, byte typeOfValue) {
        this.name = name;
        this.typeOfValue = typeOfValue;
        this.responseValues = null;
        this.authoriteValues = null;
        this.extraValues = null;
    }

    public Data(String name, byte typeOfValue, Value[] responseValues, Value[] authoriteValues, Value[] extraValues) {
        this.name = name;
        this.typeOfValue = typeOfValue;
        this.responseValues = responseValues;
        this.authoriteValues = authoriteValues;
        this.extraValues = extraValues;
    }

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
                throw new TypeOfValueException("Type not exist");
        };
        return ret;
    }

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

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(name);
        out.append(",");
        out.append(Data.typeOfValueConvertSring(typeOfValue));
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
}


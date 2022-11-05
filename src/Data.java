import java.util.Arrays;
import java.util.Objects;

public class Data {
    private String name;
    private byte typeOfValue; // 11 tipos
    private String[] responseValues;
    private String[] authoriteValues;
    private String[] extraValues;


    public Data(String name, byte typeOfValue) {
        this.name = name;
        this.typeOfValue = typeOfValue;
        this.responseValues = null;
        this.authoriteValues = null;
        this.extraValues = null;
    }

    public Data(String name, byte typeOfValue, String[] responseValues, String[] authoriteValues, String[] extraValues) {
        this.name = name;
        this.typeOfValue = typeOfValue;
        this.responseValues = responseValues;
        this.authoriteValues = authoriteValues;
        this.extraValues = extraValues;
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
}


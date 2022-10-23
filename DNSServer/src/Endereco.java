import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author José Carvalho
 * Classe para guardar os endereços IPV4.
 * Não usamos o InetAddress como base por questões relacionadas com o método toString.
 */
public class Endereco {
    InetAddress enderecoIP;
    Endereco(byte[] endereco) throws UnknownHostException {
        this.enderecoIP = InetAddress.getByAddress(endereco);
    }
    @Override
    public String toString() {
        return this.enderecoIP.toString().substring(1);
    }

    @Override
    public boolean equals(Object o) {
        return this.enderecoIP.equals(o);
    }

    @Override
    public int hashCode() {
        return this.enderecoIP.hashCode();
    }

    /**
     * Método que converte uma string num endereço.
     * @param str String para converter
     * @return Endereço após a conversão
     */
    public static Endereco stringToIP(String str) throws UnknownHostException {
        String[] l = str.split("\\.");
        byte[] endereco = new byte[4];
        endereco[0] = (byte) (Integer.parseInt(l[0]));
        endereco[1] = (byte) (Integer.parseInt(l[1]));
        endereco[2] = (byte) (Integer.parseInt(l[2]));
        endereco[3] = (byte) (Integer.parseInt(l[3]));
        return new Endereco(endereco);
    }
}

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * @author José Carvalho
 * Classe para guardar os endereços IPV4.
 * Não usamos o InetAddress como base por questões relacionadas com o método toString.
 */
public class Endereco {
    private final InetAddress enderecoIP;
    private final short porta;
    Endereco(byte[] endereco, short porta) throws UnknownHostException {
        this.enderecoIP = InetAddress.getByAddress(endereco);
        this.porta = porta;
    }
    @Override
    public String toString()
    {
        String res = this.enderecoIP.toString().substring(1);
        return this.porta != 0 ? res + ":" + this.porta : res;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Endereco endereco = (Endereco) o;
        return enderecoIP.toString().substring(1).equals(endereco.enderecoIP.toString().substring(1));
    }
    /**
     * Método que converte uma string num endereço.
     * @param str String para converter
     * @return Endereço após a conversão
     */
    public static Endereco stringToIP(String str) throws UnknownHostException {
        String[] end = str.split(":");
        str = end[0];
        short porta = 0;
        if(end.length > 1)
            porta = Short.parseShort(end[1]);
        String[] l = str.split("\\.");
        byte[] endereco = new byte[4];
        endereco[0] = (byte) (Integer.parseInt(l[0]));
        endereco[1] = (byte) (Integer.parseInt(l[1]));
        endereco[2] = (byte) (Integer.parseInt(l[2]));
        endereco[3] = (byte) (Integer.parseInt(l[3]));
        return new Endereco(endereco,porta);
    }
}

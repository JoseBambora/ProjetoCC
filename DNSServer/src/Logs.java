/********************************************************
 * Author: João Martins                                 *
 * Created date: 22/10/2022                             *
 * Last Update: 22/10/2022                              *
 ********************************************************/
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;


public class Logs {
    public enum EntryType {
        QR, QE, RP, RR, ZT, EV, ER, EZ, FL, TO, SP, ST
    }

    final static DateFormat dateFormat = new SimpleDateFormat("dd:MM:yyyy.HH:mm:ss:mmm");  // .format

    Date date; /* Etiqueta temporal */
    EntryType type; /* Tipo de entrada */
    Endereco addr; /* Endereço IP */
    int port; /* Porta */
    Byte[] data; /* Dados de entrada */

    public Logs (Date date, EntryType type, Endereco addr, int port, Byte[] data) {
        this.date = date;
        this.type = type;
        this.addr = addr;
        this.port = port;
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(dateFormat.format(this.date));
        sb.append(" ");
        sb.append(type);
        sb.append(addr);
        if (port!=-1) {       /* Ainda vou mudar isto */
            sb.append(":");
            sb.append(port);
        }
        sb.append(" ");
        sb.append(Arrays.toString(data));
        sb.append("\n");
        return sb.toString();
    }

}
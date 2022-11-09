/**
 * @Author João Martins
 * @Class Client
 * Created date: 22/10/2022
 * Last Update: 07/11/2022
 */
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;


public class Log {
    public enum EntryType {
        QR, QE, RP, RR, ZT, EV, ER, EZ, FL, TO, SP, ST
    }

    final static DateFormat dateFormat = new SimpleDateFormat("dd:MM:yyyy.HH:mm:ss:mmm");  // .format

    Date date; /* Etiqueta temporal */
    EntryType type; /* Tipo de entrada */
    String addr; /* Endereço IP */
    int port; /* Porta */
    String data; /* Dados de entrada */

    public Log(Date date, EntryType type, String addr, int port, String data) {
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
        sb.append(" ");
        sb.append(addr);
        if (port!=53) {
            sb.append(":");
            sb.append(port);
        }
        sb.append(" ");
        sb.append(data);
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Log l = (Log) obj;
        return this.date.equals(l.date) &&
               this.type == l.type &&
               this.port == l.port &&
               this.addr.equals(l.addr) &&
               this.data.equals(l.data);
    }
}
package Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @Author João Martins
 * @Class Log
 * Created date: 22/10/2022
 * Last Update: 5/11/2022
 */
public class Log {
    /**
     * Tipo de entradas de um ficheiro de log.
     */
    public enum EntryType {
        QR, QE, RP, RR, ZT, EV, ER, EZ, FL, TO, SP, ST
    }

    /**
     * Formato usado para representação temporal de um log.
     */
    final static DateFormat dateFormat = new SimpleDateFormat("dd:MM:yyyy.HH:mm:ss:mmm");  // .format

    private Date date; /* Etiqueta temporal */
    private EntryType type; /* Tipo de entrada */
    private String addr; /* Endereço IP */
    private int port; /* Porta */
    private String data; /* Dados de entrada */

    /**
     * Construtor da classe Log.
     * @param date Etiqueta temporal
     * @param type Tipo de entrada
     * @param addr Endereço IP
     * @param port Porta
     * @param data Dados de entrada
     */
    public Log(Date date, EntryType type, String addr, int port, String data) {
        this.date = date;
        this.type = type;
        this.addr = addr;
        this.port = port;
        this.data = data;
    }

    /**
     * Construtor da classe Log.
     * @param date Etiqueta temporal
     * @param type Tipo de entrada
     * @param addr Endereço IP
     * @param data Dados de entrada
     */
    public Log(Date date, EntryType type, String addr, String data) {
        this.date = date;
        this.type = type;
        this.addr = addr;
        this.port = 5353;
        this.data = data;
    }

    /**
     * Gera uma string com a representação de um log.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(dateFormat.format(this.date));
        sb.append(" ");
        sb.append(type);
        sb.append(" ");
        sb.append(addr);
        if (port!=5353) {
            sb.append(":");
            sb.append(port);
        }
        sb.append(" ");
        sb.append(data);
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
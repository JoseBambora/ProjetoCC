/**
 * @author Miguel Cidade Silva
 * Classe cuja função é auxiliar no processo de transferência de zona
 * Data de criação 11/12/2022
 * Data de edição 14/12/2022
 */

import Cache.Tuple;
import ObjectServer.ObjectSP;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ZoneTransferManager implements Runnable{

    private ObjectSP objsp;
    private Socket socket;

    /**
     * Construtor da classe ZoneTransferManager
     * @param objsp Servidor a partir do qual é feita a transferência de zona
     * @param socket Socket a partir do qual é feita a comunicação
     */
    public ZoneTransferManager(ObjectSP objsp, Socket socket) {
        this.objsp = objsp;
        this.socket = socket;
    }

    /**
     * Método que conta o número de entradas válidas com as linhas obtidos do ficheiro de base de dados.
     * @param lines linhas a contar as entradas
     * @return o número de entradas válidas nas linhas enviadas como parâmetro
     */
    public int countEntrys (List<String> lines) {
        int i = 0;
        for (String l : lines) {
            if (l.length()>0 && l.charAt(0) != '#' && !l.equals("\n")) {
                i++;
            }
        }
        return i;
    }

    /**
     * Método que verifica se o SS tem autorização para realizar uma transferência de zona.
     * @param ss - Endereço IP do SS
     * @return true caso o ss se encontre na lista de SS do SP para o domínio em questão, false caso contrário.
     */
    public boolean allowSS (InetAddress ss) {
        Iterator<InetSocketAddress> it = objsp.getSS().iterator();
        boolean f = false;
        while (it.hasNext() && !f) {
            InetSocketAddress sa = it.next();
            if (sa.getAddress().equals(ss)) {
                f = true;
            }
        }
        return f;
    }

    /**
     * Método run que realiza a transferência de zona
     */
    @Override
    public void run() {
        try {
            DataOutputStream toClient = new DataOutputStream(this.socket.getOutputStream());
            DataInputStream fromClient = new DataInputStream(this.socket.getInputStream());

            /* Recebe dominio e valida */
            String domain = fromClient.readUTF();
            boolean autoriza = allowSS(this.socket.getInetAddress());

            if (this.objsp.getDominio().equals(domain) && autoriza) {
                /* Envia número de entradas */
                List<String> lines = Files.readAllLines(Paths.get(this.objsp.getBD()));
                AtomicInteger num = new AtomicInteger(1);
                List<Tuple<Integer,String>> aux = new ArrayList<>();
                lines.forEach(s -> aux.add(new Tuple<>(num.getAndIncrement(),s)));
                List<Tuple<Integer,String>> aux2 = aux.stream().filter(l -> l.getValue2().length() > 0
                                                    && l.getValue2().charAt(0) != '#'
                                                    && !l.getValue2().equals("\n")).toList();
                toClient.write(aux2.size());
                toClient.flush();

                /* Recebe número de entradas */
                int ne = fromClient.read();

                /* Envia entradas do ficheiro de base de dados */
                for(Tuple<Integer,String> l : aux2)
                {
                    toClient.writeUTF(l.getValue1() + ":" + l.getValue2());
                    toClient.flush();
                }
            }
            fromClient.close();
            toClient.close();
            this.socket.close();
            } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}

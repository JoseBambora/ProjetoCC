/**
 * @author Miguel Cidade Silva
 * Classe cuja função é auxiliar no processo de transferência de zona
 * Data de criação 23/10/2022
 * Data de edição 22/11/2022
 */

import ObjectServer.ObjectSP;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

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
     * @param lines
     * @return
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
     * @param ss
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

    @Override
    public void run() {
        try {
            ServerSocket socketTcp = new ServerSocket(5353);
            DataOutputStream toClient = new DataOutputStream(this.socket.getOutputStream());
            DataInputStream fromClient = new DataInputStream(this.socket.getInputStream());

            /* Recebe dominio e valida */
            String domain = fromClient.readUTF();
            boolean autoriza = allowSS(this.socket.getInetAddress());

            if (this.objsp.getDominio().equals(domain) && autoriza) {
                /* Envia número de entradas */
                List<String> lines = Files.readAllLines(Paths.get(this.objsp.getBD()));
                int ce = countEntrys(lines);
                toClient.write(ce);
                toClient.flush();

                /* Recebe número de entradas */
                int ne = fromClient.read();

                /* Envia entradas do ficheiro de base de dados */
                int i = 1;
                for (String l : lines) {
                    if (l.length() > 0 && l.charAt(0) != '#' && !l.equals("\n")) {
                        toClient.writeUTF(i + ":" + l);
                        toClient.flush();
                        i++;
                    }
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

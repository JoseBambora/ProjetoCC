/**
 * @Author João Martins
 * @Class ZoneTransfer
 * Created date: 03/11/2022
 * Last update: 23/11/2022
 */
import ObjectServer.ObjectSP;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

public class ZoneTransfer implements Runnable {
        private ObjectSP objsp;

        /**
         * Contrutor da classe ZoneTransfer.
         * @param objsp
         */
        public ZoneTransfer(ObjectSP objsp) {
                this.objsp = objsp;
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

        /**
         * Processo que vai correr em um segundo plano no servidor principal para realizar operações de transferência de zona.
         */
        @Override
        public void run() {
                try {
                        ServerSocket socketTcp = new ServerSocket(5353);

                        while (true) {
                                Socket c = socketTcp.accept();
                                DataOutputStream toClient = new DataOutputStream(c.getOutputStream());
                                DataInputStream fromClient = new DataInputStream(c.getInputStream());

                                /* Recebe dominio e valida */
                                String domain = fromClient.readUTF();
                                boolean autoriza = allowSS(c.getInetAddress());

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
                                                if (l.length()>0 && l.charAt(0) != '#' && !l.equals("\n")) {
                                                        toClient.writeUTF(i + ":" + l);
                                                        toClient.flush();
                                                        i++;
                                                }
                                        }
                                }

                                fromClient.close();
                                toClient.close();
                                c.close();
                        }

                } catch (IOException e) {
                        throw new RuntimeException(e);
                }


        }
}

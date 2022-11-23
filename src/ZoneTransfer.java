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

        public ZoneTransfer(ObjectSP objsp) {
                this.objsp = objsp;
        }

        public int countEntrys (List<String> lines) {
                int i = 0;
                for (String l : lines) {
                        if (l.length()>0 && l.charAt(0) != '#' && !l.equals("\n")) {
                                i++;
                        }
                }
                return i;
        }

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

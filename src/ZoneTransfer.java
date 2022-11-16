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
                        ServerSocket socketTcp = new ServerSocket(6363);

                        while (true) {
                                Socket c = socketTcp.accept();
                                PrintWriter toClient = new PrintWriter(c.getOutputStream());
                                BufferedReader fromClient = new BufferedReader(new InputStreamReader(c.getInputStream()));

                                /* Receive query */
                                String line = fromClient.readLine();

                                /* Envia versão */
                                DNSPacket qr = DNSPacket.bytesToDnsPacket(line.getBytes());
                                if (Data.typeOfValueConvertSring(qr.getData().getTypeOfValue()).equals("SOASERIAL")) {
                                        /* procurar versão na cache fase posterior e envia resposta, por enquanto envio a mesma querie*/
                                        String aux = qr.toString();
                                        toClient.println(aux.substring(0,aux.length()-1));
                                        toClient.flush();

                                        /* Recebe dominio e valida */
                                        String domain = fromClient.readLine();
                                        boolean autoriza = true; // allowSS(c.getInetAddress())

                                        if (this.objsp.getDominio().equals(domain) && autoriza) {
                                                /* Envia número de entradas */
                                                List<String> lines = Files.readAllLines(Paths.get(this.objsp.getBD()));
                                                int ce = countEntrys(lines);
                                                toClient.println(ce);
                                                toClient.flush();

                                                /* Recebe número de entradas */
                                                int ne = Integer.parseInt(fromClient.readLine());
                                                if (ne == ce) {
                                                        /* Envia entradas do ficheiro de base de dados */
                                                        int i = 1;
                                                        for (String l : lines) {
                                                                if (l.length()>0 && l.charAt(0) != '#' && !l.equals("\n")) {
                                                                        toClient.println(i + "-" + l);
                                                                        toClient.flush();
                                                                        i++;
                                                                }
                                                        }
                                                }
                                        }
                                }

                                c.close();
                        }

                } catch (IOException | TypeOfValueException e) {
                        throw new RuntimeException(e);
                }


        }
}

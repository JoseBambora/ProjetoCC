import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ZoneTransfer implements Runnable{
        String filename;
        List<InetAddress> ss;
        String domain;
        int numEntrys;

        public ZoneTransfer(String filename, List<InetAddress> ss, String domain, int numEntrys) {
                this.filename = filename;
                this.ss = ss;
                this.domain = domain;
                this.numEntrys = numEntrys;
        }

        @Override
        public void run() {

                try {
                        ServerSocket socketTcp = new ServerSocket();

                        while (true) {
                                Socket c = socketTcp.accept();

                                /* Recebe query */
                                PrintWriter toClient = new PrintWriter(c.getOutputStream(),true);
                                BufferedReader fromClient = new BufferedReader(new InputStreamReader(c.getInputStream()));
                                String line = fromClient.readLine();

                                /* Envia versão */
                                DNSPacket qr = DNSPacket.bytesToDnsPacket(line.getBytes());
                                if (DNSPacket.typeOfValueConvertSring(qr.getData().getTypeOfValue()).equals("SOASERIAL")) {
                                        /* ler versão do filename e envia resposta*/
                                        toClient.println(qr.toString());
                                }

                                /* Recebe dominio */
                                String domain = fromClient.readLine();
                                if (this.domain.equals(domain)) {
                                        /* Envia número de entradas (obter a partir do file?) */
                                        toClient.println(numEntrys);

                                        /* Recebe número de entradas */
                                        String ne = fromClient.readLine();

                                        /* Envia entradas do file */
                                        List<String> lines = Files.readAllLines(Paths.get(this.filename));

                                        for (String l : lines) {
                                                if (l.charAt(0) != '#' && !l.equals("\n")) {
                                                        toClient.println(l);
                                                }
                                        }
                                }

                                toClient.flush();
                                toClient.close();
                                fromClient.close();

                                c.close();

                        }

                } catch (IOException | TypeOfValueException e) {
                        throw new RuntimeException(e);
                }


        }
}

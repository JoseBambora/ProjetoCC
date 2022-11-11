/**
 * @Author João Martins
 * @Class Server
 * Created date: 03/11/2022
 * Last update: 07/11/2022
 */

import java.net.*;
import java.util.Date;


public class Server {
    private String configFile;  /* 1º arg: config file */
    private int timeout;        /* 2º arg: timeout */
    private int port;           /* 3º arg: porta de funcionameto (optional) */
    private boolean debug;      /* 4º arg: funcionar em debug (optional) */

    public Server() {
        this.port = 53;
        this.debug = false;
    }

    public static void main(String[] args) {
        Server s = new Server();

        try {
            /* Arguments Parsing */
            int i = 0;
            s.configFile = args[i++];
            s.timeout = Integer.parseInt(args[i++]);
            if (args.length == 3 && args[i].compareTo("D")==0) { s.debug = true; }
            else if (args.length == 3) { s.port = Integer.parseInt(args[i]); }
            else if (args.length == 4) {
                s.port = Integer.parseInt(args[i++]);
                s.debug = args[i].compareTo("D")==0;
            }

            /* Configurate server */
            ObjectServer sc = ObjectServer.parseServer(s.configFile);

            /* Identificate the type of server */
            boolean sp = sc instanceof ObjectSP;
            boolean ss = sc instanceof ObjectSS;

            /* Create thread for tcp socket */
            if (sp) {
                
            }



            /* Zone transfer if is SS */
            if (ss) {
                ObjectSS sec = (ObjectSS) sc;



            }

            /* Create server socket */
            DatagramSocket socket = new DatagramSocket(s.port);
            while (true) {
                /* Receive packet */
                byte[] receiveBytes = new byte[1000];
                DatagramPacket request = new DatagramPacket(receiveBytes, receiveBytes.length);
                socket.receive(request);

                /* Extract client address and port */
                InetAddress clientAddress = request.getAddress();
                int clientPort = request.getPort();

                /* Build packet */
                DNSPacket receivePacket = DNSPacket.bytesToDnsPacket(receiveBytes);

                /*  Create Log and write in the output */
                Log qr = new Log(new Date(), Log.EntryType.QR, clientAddress.getHostAddress(), clientPort, "");
                // Log no ficheiro respetivo
                if (s.debug) System.out.println(qr);

                /* Find answer */
                Data resp = sc.getCache().findAnswer(receivePacket);

                if (resp!=null) {
                    /* Build Packet */
                    Header header = new Header(receivePacket.getHeader().getMessageID(), false, receivePacket.getHeader().isFlagA(), false);
                    DNSPacket sendPacket = new DNSPacket(header, resp);

                    /* Create Datagram */
                    byte[] sendBytes = sendPacket.dnsPacketToBytes();
                    DatagramPacket response = new DatagramPacket(sendBytes, sendBytes.length, clientAddress, clientPort);

                    /* Create new Datagram Socket */
                    DatagramSocket socket1 = new DatagramSocket();

                    /* Send answer */
                    socket1.send(response);
                    Log re = new Log(new Date(), Log.EntryType.QE, clientAddress.getHostAddress(), clientPort, "");
                    if (s.debug) System.out.println(re);

                    /* Close new socket */
                    socket1.close();
                }
            }

        } catch (Exception e) {
            if (s.debug) {
                Log fl = new Log(new Date(), Log.EntryType.FL,"127.0.0.1",53,null);
                System.out.println(fl);
            }
        }

    }

}

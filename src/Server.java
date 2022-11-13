/**
 * @Author João Martins
 * @Class Server
 * Created date: 03/11/2022
 * Last update: 07/11/2022
 */

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;


public class Server {
    private String configFile;  /* 1º arg: Configuration file */
    private int timeout;        /* 2º arg: Timeout */
    private int port;           /* 3º arg: Port (optional) */
    private boolean debug;      /* 4º arg: Debug mode (optional) */

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

            Thread transfer;
            /* Create thread for tcp socket */
            if (sp) {
                ObjectSP pri = (ObjectSP) sc;
                ZoneTransfer zt = new ZoneTransfer("",null, pri.getDominio(), 0);
                transfer = new Thread(zt);
                transfer.start();

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

                /* Find answer */
                Data resp = sc.getCache().findAnswer(receivePacket);

                if (resp!=null) {
                    /* Build Packet */
                    byte flags = 4;
                    Header header = new Header(receivePacket.getHeader().getMessageID(), flags);
                    DNSPacket sendPacket = new DNSPacket(header, resp);

                    /* Create Datagram */
                    byte[] sendBytes = sendPacket.dnsPacketToBytes();
                    DatagramPacket response = new DatagramPacket(sendBytes, sendBytes.length, clientAddress, clientPort);

                    /* Create new Datagram Socket */
                    DatagramSocket socket1 = new DatagramSocket();

                    /* Send answer */
                    socket1.send(response);

                    /* Close new socket */
                    socket1.close();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

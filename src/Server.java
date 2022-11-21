/**
 * @Author João Martins
 * @Class Server
 * Created date: 03/11/2022
 * Last update: 15/11/2022
 */

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Date;


public class Server {
    private String configFile;  /* 1º arg: Configuration file */
    private int timeout;        /* 2º arg: Timeout */
    private int port;           /* 3º arg: Port (optional) */
    private boolean debug;      /* 4º arg: Debug mode (optional) */

    public Server() {
        this.port = 5353;
        this.debug = false;
    }

    public static void main(String[] args) {
        Server s = new Server();

        try {
            /* Arguments Parsing */
            s.configFile = args[0];
            s.timeout = Integer.parseInt(args[1]);
            if (args.length == 3 && args[2].compareTo("D")==0) {
                s.debug = true;
            }
            else if (args.length == 3) {
                s.port = Integer.parseInt(args[2]);
            }
            else if (args.length == 4) {
                s.port = Integer.parseInt(args[2]);
                s.debug = args[3].compareTo("D")==0;
            }

            /* Configurate server */
            ObjectServer sc = ObjectServer.parseServer(s.configFile);

            /* Identificate the type of server */
            boolean sp = sc instanceof ObjectSP;
            boolean ss = sc instanceof ObjectSS;

            /* Create thread for tcp server socket if is primary server */
            Thread transfersp;
            if (sp) {
                ObjectSP pri = (ObjectSP) sc;
                transfersp = new Thread(new ZoneTransfer(pri));
                transfersp.start();
            }

            /* Create thread for SS ask the database version */
            Thread transferss;
            if (ss) {
                ObjectSS sec = (ObjectSS) sc;
                transferss = new Thread(new AskVersion(sec));
                transferss.start();
            }

            /* Create the udp socket for receving queries */
            DatagramSocket socket = new DatagramSocket(s.port);

            while (true) {
                /* Receive packet */
                byte[] receiveBytes = new byte[1000];
                DatagramPacket request = new DatagramPacket(receiveBytes, receiveBytes.length);
                socket.receive(request);

                /* Extract client address and port */
                InetAddress clientAddress = request.getAddress();
                int clientPort = request.getPort();

                /* Build received packet */
                DNSPacket receivePacket = DNSPacket.bytesToDnsPacket(receiveBytes);

                if (s.debug) {
                    Log qr = new Log(new Date(), Log.EntryType.QR,clientAddress.getHostAddress(),clientPort,receivePacket.toString());
                    System.out.println(qr);
                }

                /* Find answer */
                Data resp = sc.getCache().findAnswer(receivePacket).getValue2();

                if (resp!=null) {
                    /* Build Packet */
                    byte flags = 4;
                    int nrv = resp.getResponseValues().length;
                    int nav = resp.getAuthoriteValues().length;
                    int nev = resp.getExtraValues().length;

                    Header header = new Header(receivePacket.getHeader().getMessageID(), flags, (byte) 0, (byte) nrv, (byte)  nav, (byte)  nev);
                    DNSPacket sendPacket = new DNSPacket(header, resp);

                    /* Create Datagram */
                    byte[] sendBytes = sendPacket.dnsPacketToBytes();
                    DatagramPacket response = new DatagramPacket(sendBytes, sendBytes.length, clientAddress, clientPort);

                    /* Create new Datagram Socket */
                    DatagramSocket socket1 = new DatagramSocket();

                    /* Send answer */
                    socket1.send(response);
                    if (s.debug) {
                        Log re = new Log(new Date(), Log.EntryType.RP,clientAddress.getHostAddress(),clientPort,sendPacket.toString());
                        System.out.println(re);
                    }

                    /* Close new socket */
                    socket1.close();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

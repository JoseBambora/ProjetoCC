/**
 * @Author João Martins
 * @Class Server
 * Created date: 03/11/2022
 * Last update: 07/11/2022
 */

import java.net.*;
import java.util.Date;


/*
 Tipo do valor | DB   SP   SS   ST
 SP            |  t   nt    t    t
 SS            |  nt  t    nt    t
 SR            |  nt  nt   nt    t
 ST            |  t   nt   nt   nt
 SDT           funciona como sp
 */
public class Server {
    private String configFile;  /* 1º arg: config file */
    private int timeout;        /* 2º arg: timeout */
    private int port;           /* 3º arg: porta de funcionameto */
    private boolean debug;      /* 4º arg: funcionar em debug */

    public Server() {
        this.configFile = "";
        this.timeout = 0;
        this.port = 53;
        this.debug = false;
    }

    public static void main(String[] args) {
        Server s = new Server();

        try {
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
            ServidorConfiguracao sc = ServidorConfiguracao.parseServer(s.configFile);

            /* Identificate the type of server */
            boolean sp = sc instanceof ServidorSP;
            boolean ss = sc instanceof ServidorSS;

            /* Zone transfer if is SS */
            if (ss) {

            }


            DatagramSocket socket = new DatagramSocket(s.port);

            while (true) {

                /* Receive packet */
                byte[] receiveBytes = new byte[1000];
                DatagramPacket request = new DatagramPacket(receiveBytes, receiveBytes.length);
                socket.receive(request);
                DNSPacket receivePacket = DNSPacket.bytesToDnsPacket(receiveBytes);

                /* Create Log and write in the output */
                InetAddress clientAddress = request.getAddress();
                int clientPort = request.getPort();
                Log qr = new Log(new Date(), Log.EntryType.QR,clientAddress.getHostAddress(),clientPort,receiveBytes);
                // Log no ficheiro respetivo
                if (s.debug) System.out.println(qr);

                /* Find answer */
                DNSPacket sendPacket = null;
                Header header = new Header(receivePacket.getHeader().getMessageID(), false, receivePacket.getHeader().isFlagA(),false);
                Data resp = sc.getCache().findAnswer(receivePacket);
                sendPacket = new DNSPacket(header,resp);

                /* Send answer */
                byte[] sendBytes = sendPacket.dnsPacketToBytes();
                DatagramPacket response = new DatagramPacket(sendBytes, sendBytes.length, clientAddress, clientPort);
                socket.send(response);
                Log qe = new Log(new Date(), Log.EntryType.QE,clientAddress.getHostAddress(),clientPort,sendBytes);
                if (s.debug) System.out.println(qe);

            }

        } catch (Exception e) {
            if (s.debug) {
                Log fl = new Log(new Date(), Log.EntryType.FL,"127.0.0.1",53,null);
                System.out.println(fl);
            }
        }

    }

}

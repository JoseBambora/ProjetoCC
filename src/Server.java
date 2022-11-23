/**
 * @Author João Martins
 * @Class Server
 * Created date: 03/11/2022
 * Last update: 23/11/2022
 */

import DNSPacket.*;
import Exceptions.InvalidArgumentException;
import Exceptions.TypeOfValueException;
import Cache.*;
import Log.Log;
import ObjectServer.*;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;


public class Server {
    private String configFile;  /* 1º arg: Configuration file */
    private int timeout;        /* 2º arg: Timeout */
    private int port;           /* 3º arg: Port (optional) */
    private boolean debug;      /* 4º arg: Operation mode (optional) */


    /**
     * Construtor da classe Server.
     * @param configFile Caminho para o ficheiro de configuração.
     * @param timeout Valor de timeout.
     * @param port Porta de funcionamento do servidor.
     * @param debug Modo normal
     * @throws InvalidArgumentException Argumento do modo inválido.
     */
    public Server(String configFile, String timeout, String port, String debug) throws InvalidArgumentException {
        this.configFile = configFile;
        this.timeout = Integer.parseInt(timeout);
        this.port = Integer.parseInt(port);
        if (debug.compareTo("N")==0) this.debug = false;
        else throw new InvalidArgumentException("Invalid last argument");
    }

    /**
     * Construtor da classe Server.
     * @param configFile Caminho para o ficheiro de configuração.
     * @param timeout Valor de timeout.
     * @param optional Porta de funcionamento do servidor ou modo normal
     */
    public Server(String configFile, String timeout, String optional) {
        this.configFile = configFile;
        this.timeout = Integer.parseInt(timeout);
        if (optional.compareTo("N")==0) {
            this.debug = false;
            this.port = 5353;
        } else {
            this.debug = true;
            this.port = Integer.parseInt(optional);
        }
    }

    /**
     * Construtor da classe Server.
     * @param configFile Caminho para o ficheiro de configuração.
     * @param timeout Valor de timeout.
     */
    public Server(String configFile, String timeout) throws InvalidArgumentException {
        this.configFile = configFile;
        this.timeout = Integer.parseInt(timeout);
        this.debug = true;
        this.port = 5353;
    }


    /**
     * Server main.
     * @param args argumentos passados por linha de comando.
     */
    public static void main(String[] args) {
        Server s = null;

        try {
            /* Arguments Parsing */
            switch (args.length) {
                case 2 -> s = new Server(args[0], args[1]);
                case 3 -> s = new Server(args[0], args[1], args[2]);
                case 4 -> s = new Server(args[0], args[1], args[2], args[3]);
                default -> throw new InvalidArgumentException("Invalid number of arguments");

            }

            /* Configurate server */
            ObjectServer sc = ObjectServer.parseServer(s.configFile);
            // ev ficheiro de config lido, ficheiro de db e st

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
                try {
                    /* Receive packet */
                    byte[] receiveBytes = new byte[1000];
                    DatagramPacket request = new DatagramPacket(receiveBytes, receiveBytes.length);
                    socket.receive(request);

                    /* Extract client address and port */
                    InetAddress clientAddress = request.getAddress();
                    int clientPort = request.getPort();

                    try {
                        /* Build received packet */
                        DNSPacket receivePacket = DNSPacket.bytesToDnsPacket(receiveBytes);
                        sc.writeAnswerInLog(receivePacket.getData().getName(), Log.EntryType.QR, clientAddress.getHostAddress(), clientPort, receivePacket.toString());
                        Log qr = new Log(new Date(), Log.EntryType.QR, clientAddress.getHostAddress(), clientPort, receivePacket.toString());
                        System.out.println(qr);

                        /* Find answer */
                        Tuple<Integer, Data> anwser = sc.getCache().findAnswer(receivePacket);

                        int respCode = anwser.getValue1();
                        Data resp = anwser.getValue2();
                        /* Build Packet */
                        byte flags = 4;
                        int nrv = 0, nav = 0, nev = 0;
                        if (resp.getResponseValues()!=null) nrv = resp.getResponseValues().length;
                        if (resp.getAuthoriteValues()!=null) nav = resp.getAuthoriteValues().length;
                        if (resp.getExtraValues()!=null) nev = resp.getExtraValues().length;

                        Header header = new Header(receivePacket.getHeader().getMessageID(), flags,(byte) respCode, (byte) nrv, (byte) nav, (byte) nev);
                        DNSPacket sendPacket = new DNSPacket(header, resp);

                        /* Create Datagram */
                        byte[] sendBytes = sendPacket.dnsPacketToBytes();
                        DatagramPacket response = new DatagramPacket(sendBytes, sendBytes.length, clientAddress, clientPort);

                        /* Create new Datagram Socket */
                        DatagramSocket socket1 = new DatagramSocket();

                        /* Send answer */
                        socket1.send(response);
                        sc.writeAnswerInLog(receivePacket.getData().getName(), Log.EntryType.RP, clientAddress.getHostAddress(), clientPort, receivePacket.toString());
                        Log re = new Log(new Date(), Log.EntryType.RP, clientAddress.getHostAddress(), clientPort, sendPacket.toString());
                        System.out.println(re);

                        /* Close new socket */
                        socket1.close();


                    } catch (TypeOfValueException e) {
                        sc.writeAnswerInLog(sc.getDominio(), Log.EntryType.ER, clientAddress.getHostAddress(), clientPort, e.getMessage());
                        Log er = new Log(new Date(), Log.EntryType.ER, clientAddress.getHostAddress(), clientPort, e.getMessage());
                        System.out.println(er);
                    } catch (Exception e) {
                        sc.writeAnswerInLog(sc.getDominio(), Log.EntryType.FL, "127.0.0.1", s.port, e.getMessage());
                        Log fl = new Log(new Date(), Log.EntryType.FL, "127.0.0.1", e.getMessage());
                        System.out.println(fl);
                    }
                } catch (IOException e) {
                    sc.writeAnswerInLog(sc.getDominio(), Log.EntryType.FL, "127.0.0.1", s.port, e.getMessage());
                    Log fl = new Log(new Date(), Log.EntryType.FL, "127.0.0.1", "Error sending/receiving the response/query");
                    System.out.println(fl);
                }
            }

        } catch (InvalidArgumentException | SocketException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("Error parsing the configuration file");
        }

    }

}

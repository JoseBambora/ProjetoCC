/**
 * @Author João Martins
 * @Class Client
 * Created date: 03/11/2022
 * Last update: 22/11/2022
 */

import DNSPacket.*;
import Exceptions.InvalidArgumentException;
import Exceptions.TypeOfValueException;
import Log.Log;

import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.Random;

public class Client {
    private InetAddress serverAddress;  /* 1º arg: IP address of the destination server */
    private int serverPort;             /* 1º arg: Port of the destination server */
    private int timeout;                /* 2º arg: Timeout */
    private String name;                /* 3º arg: Name */
    private byte type;                  /* 4º arg: Type of value */
    boolean recursive;                  /* 5º arg: Try recursive mode (optional) */
    boolean debug;                      /* 6º arg: Operation mode (optional) */


    /**
     * Constructors of client.
     */
    public Client(String serverAddr, String timeout, String name, String type) throws UnknownHostException, TypeOfValueException {
        String[] words = serverAddr.split(":");
        this.serverAddress = InetAddress.getByName(words[0]);
        if (words.length==2) this.serverPort = Integer.parseInt(words[1]);
        else this.serverPort = 5353;
        this.timeout = Integer.parseInt(timeout);
        this.name = name;
        this.type = Data.typeOfValueConvert(type);
        this.recursive = false;
        this.debug = true;
    }

    public Client(String serverAddr, String timeout, String name, String type, String option) throws UnknownHostException, TypeOfValueException, InvalidArgumentException {
        String[] words = serverAddr.split(":");
        this.serverAddress = InetAddress.getByName(words[0]);
        if (words.length==2) this.serverPort = Integer.parseInt(words[1]);
        else this.serverPort = 5353;
        this.timeout = Integer.parseInt(timeout);
        this.name = name;
        this.type = Data.typeOfValueConvert(type);
        switch (option) {
            case "N" -> {
                this.debug = false;
                this.recursive = false;
            }
            case "R" -> {
                this.debug = true;
                this.recursive = true;
            }
            default -> throw new InvalidArgumentException("Invalid optional argument");
        }
    }

    public Client(String serverAddr, String timeout, String name, String type, String recursive, String debug) throws UnknownHostException, TypeOfValueException, InvalidArgumentException {
        String[] words = serverAddr.split(":");
        this.serverAddress = InetAddress.getByName(words[0]);
        if (words.length==2) this.serverPort = Integer.parseInt(words[1]);
        else this.serverPort = 5353;
        this.timeout = Integer.parseInt(timeout);
        this.name = name;
        this.type = Data.typeOfValueConvert(type);
        if (recursive.equals("R")) this.recursive = true;
        else throw new InvalidArgumentException("Invalid optional recursive argument");
        if (debug.equals("N")) this.debug = false;
        else throw new InvalidArgumentException("Invalid optional debug argument");
    }

    /**
     * Create the DNSPacket for client query.
     */
    public DNSPacket createDNSPacket() {
        byte flags = 1;
        if (this.recursive) flags = 3;
        return new DNSPacket((short) (new Random()).nextInt(1,65535), flags, this.name, this.type);
    }

    public static void main(String[] args) {
        Client cl = null;

        try {
            /* Arguments parsing */
            switch (args.length) {
                case 4 -> cl = new Client(args[0], args[1], args[2], args[3]);
                case 5 -> cl = new Client(args[0], args[1], args[2], args[3], args[4]);
                case 6 -> cl = new Client(args[0], args[1], args[2], args[3], args[4], args[5]);
                default -> throw new InvalidArgumentException("Invalid number of arguments");
            };

            /* Create the packet */
            DNSPacket sendPacket = cl.createDNSPacket();

            /* Create udp datagram */
            byte[] sendBytes = sendPacket.dnsPacketToBytes();
            DatagramPacket request = new DatagramPacket(sendBytes, sendBytes.length, cl.serverAddress, cl.serverPort);

            /* Create the client udp socket with the preset timeout */
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(cl.timeout);

            /* Send the packet */
            socket.send(request);
            Log qe = new Log(new Date(), Log.EntryType.QE,cl.serverAddress.getHostAddress(), cl.serverPort, sendPacket.toString());
            System.out.println(qe);

            /* Get the query response */
            byte[] receiveBytes = new byte[1000];
            DatagramPacket response = new DatagramPacket(receiveBytes, receiveBytes.length);
            socket.receive(response);

            /* Close the socket */
            socket.close();

            /* Build the response message */
            DNSPacket resPacket = DNSPacket.bytesToDnsPacket(receiveBytes);
            Log rr = new Log(new Date(), Log.EntryType.RR, cl.serverAddress.getHostAddress(), cl.serverPort, resPacket.toString());
            System.out.println(rr);

            /* Print the response */
            System.out.println(resPacket.showDNSPacket());

        } catch (InvalidArgumentException | TypeOfValueException e) {
            Log fl = new Log(new Date(), Log.EntryType.FL, "127.0.0.1", e.toString());
            System.out.println(fl);
        } catch (UnknownHostException e) {
            Log fl = new Log(new Date(), Log.EntryType.FL, "127.0.0.1", "Address passed as argument does not exist");
            System.out.println(fl);
        } catch (SocketException e) {
            Log fl = new Log(new Date(), Log.EntryType.FL, "127.0.0.1", "Error opening the socket");
            System.out.println(fl);
        } catch (SocketTimeoutException e) {
            Log to = new Log(new Date(), Log.EntryType.TO, cl.serverAddress.getHostAddress(), cl.serverPort, "Query response");
            System.out.println(to);
        } catch (IOException e) {
            Log fl = new Log(new Date(), Log.EntryType.FL, "127.0.0.1", "Error sending/receiving the query/response");
            System.out.println(fl);
        }
    }
}
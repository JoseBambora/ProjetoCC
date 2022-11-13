/**
 * @Author João Martins
 * @Class Client
 * Created date: 03/11/2022
 * Last update: 07/11/2022
 */

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

public class Client {
    private InetAddress serverAddress;  /* 1º arg: IP address of the destination server */
    private int serverPort;             /* 1º arg: Port of the destination server */
    private int timeout;                /* 2º arg: Timeout */
    private String name;                /* 3º arg: Name */
    private byte type;                  /* 4º arg: Type of value */
    boolean recursive;                  /* 5º arg: Try recursive mode (optional) */
    boolean debug;                      /* 6º arg: Debug mode (optional) */

    public Client() {
        this.serverPort = 53;
        this.recursive = false;
        this.debug = false;
    }

    public static void main(String[] args) {
        Client cl = new Client();

        try {
            /* Arguments parsing */
            String[] words = args[0].split(":");
            cl.serverAddress = InetAddress.getByName(words[0]);
            if (words.length == 2) { cl.serverPort = Integer.parseInt(words[1]); }
            cl.timeout = Integer.parseInt(args[1]);
            cl.name = args[2];
            cl.type = Data.typeOfValueConvert(args[3]);
            cl.recursive = args.length == 5 && args[4].compareTo("R") == 0;
            cl.debug = (args.length == 5 && args[4].compareTo("D") == 0) || (args.length == 6 && args[4].compareTo("R") == 0 && args[5].compareTo("D") == 0);

            /* Create the packet */
            byte flags = 1;
            if (cl.recursive) flags = 3;
            DNSPacket sendPacket = new DNSPacket((short) (new Random()).nextInt(0,65534), flags, cl.name, cl.type);

            /* Create the client udp socket with the preset timeout */
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(cl.timeout);

            /* Send the packet */
            byte[] sendBytes = sendPacket.dnsPacketToBytes();
            DatagramPacket request = new DatagramPacket(sendBytes, sendBytes.length, cl.serverAddress, cl.serverPort);
            socket.send(request);

            /* Get the query response */
            byte[] receiveBytes = new byte[1000];
            DatagramPacket response = new DatagramPacket(receiveBytes, receiveBytes.length);
            socket.receive(response);

            /* Close the socket */
            socket.close();

            /* Build the response message */
            DNSPacket resPacket = DNSPacket.bytesToDnsPacket(receiveBytes);

            /* Print the response */
            System.out.println(resPacket);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
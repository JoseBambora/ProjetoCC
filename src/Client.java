/**
 * @Author João Martins
 * @Class Client
 * Created date: 03/11/2022
 * Last update: 07/11/2022
 */

import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.Random;

public class Client {
    private InetAddress serverAddress;  /* 1º arg: IP address of the destination server */
    private int serverPort;             /* 1º arg: Port */
    private int timeout;                /* 2º arg: Timeout */
    private String name;                /* 3º arg: Name */
    private byte type;                  /* 4º arg: Type of value */
    boolean recursive;                  /* 5º arg: want the query to be Recursive (optional) */
    boolean debug;                      /* 6º arg: debug mode (optional) */

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
            cl.type = DNSPacket.typeOfValueConvert(args[3]);
            cl.recursive = args.length == 5 && args[4].compareTo("R") == 0;
            cl.debug = (args.length == 5 && args[4].compareTo("D") == 0) || (args.length == 6 && args[4].compareTo("R") == 0 && args[5].compareTo("D") == 0);

            /* Create the packet */
            DNSPacket sendPacket = new DNSPacket((short) (new Random()).nextInt(1,65535), true, cl.recursive, false, cl.name, cl.type);

            /* Create the client udp socket with the preset timeout */
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(cl.timeout);

            /* Send the packet */
            byte[] sendBytes = sendPacket.dnsPacketToBytes();
            DatagramPacket request = new DatagramPacket(sendBytes, sendBytes.length, cl.serverAddress, cl.serverPort);
            socket.send(request);
            if (cl.debug) {
                Log qe = new Log(new Date(), Log.EntryType.QE,cl.serverAddress.getHostAddress(),cl.serverPort,"");
                System.out.println(qe);
            }

            /* Get the query response */
            byte[] receiveBytes = new byte[1000];
            DatagramPacket response = new DatagramPacket(receiveBytes, receiveBytes.length);
            socket.receive(response);
            if (cl.debug) {
                Log rr = new Log(new Date(), Log.EntryType.RR,response.getAddress().getHostAddress(), response.getPort(),"");
                System.out.println(rr);
            }

            /* Close the socket */
            socket.close();

            /* Build the response message */
            DNSPacket resPacket = DNSPacket.bytesToDnsPacket(receiveBytes);

            /* Print the response */
            System.out.println(resPacket);

        } catch (UnknownHostException | TypeOfValueException | SocketException e) {
            if (cl.debug) {
                Log fl = new Log(new Date(), Log.EntryType.FL,"127.0.0.1",53,"");
                System.out.println(fl);
            }
        } catch (SocketTimeoutException e) {
            if (cl.debug) {
                Log to = new Log(new Date(), Log.EntryType.TO,cl.serverAddress.getHostAddress(),cl.serverPort,"");
                System.out.println(to);
            }
        } catch (IOException e) {
            if (cl.debug) {
                Log er = new Log(new Date(), Log.EntryType.RR,cl.serverAddress.getHostAddress(),cl.serverPort,"");
                System.out.println(er);
            }
        } catch (Exception e) {
            if (cl.debug) {
                Log fl = new Log(new Date(), Log.EntryType.FL,"127.0.0.1",53,"");
                System.out.println(fl);
            }
        }

    }
}
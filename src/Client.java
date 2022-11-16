/**
 * @Author João Martins
 * @Class Client
 * Created date: 03/11/2022
 * Last update: 15/11/2022
 */

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
    boolean debug;                      /* 6º arg: Debug mode (optional) */

    public Client() {
        this.serverPort = 5353;
        this.recursive = false;
        this.debug = false;
    }

    public static void main(String[] args) {
        Client cl = new Client();

        try {
            /* Arguments parsing */
            if (args.length == 5) {
                cl.recursive = args[4].compareTo("R") == 0;
                cl.debug = args[4].compareTo("D") == 0;
            } else if (args.length == 6) {
                cl.recursive = args[4].compareTo("R") == 0;
                cl.debug = args[5].compareTo("D") == 0;
            }
            String[] words = args[0].split(":");
            cl.serverAddress = InetAddress.getByName(words[0]);
            if (words.length == 2) { cl.serverPort = Integer.parseInt(words[1]); }
            cl.timeout = Integer.parseInt(args[1]);
            cl.name = args[2];
            cl.type = Data.typeOfValueConvert(args[3]);


            /* Create the packet */
            byte flags = 1;
            if (cl.recursive) flags = 3;
            int mid = (new Random()).nextInt(1,65535);
            DNSPacket sendPacket = new DNSPacket((short) mid, flags, cl.name, cl.type);

            /* Create the client udp socket with the preset timeout */
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(cl.timeout);

            /* Send the packet */
            byte[] sendBytes = sendPacket.dnsPacketToBytes();
            DatagramPacket request = new DatagramPacket(sendBytes, sendBytes.length, cl.serverAddress, cl.serverPort);
            socket.send(request);
            if (cl.debug) {
                Log qe = new Log(new Date(), Log.EntryType.QE,cl.serverAddress.getHostAddress(), cl.serverPort, sendPacket.toString());
                System.out.println(qe);
            }

            /* Get the query response */
            byte[] receiveBytes = new byte[1000];
            DatagramPacket response = new DatagramPacket(receiveBytes, receiveBytes.length);
            socket.receive(response);

            /* Build the response message */
            DNSPacket resPacket = DNSPacket.bytesToDnsPacket(receiveBytes);
            if (cl.debug) {
                Log rr = new Log(new Date(), Log.EntryType.RR,cl.serverAddress.getHostAddress(), cl.serverPort, resPacket.toString());
                System.out.println(rr);
            }

            /* Close the socket */
            socket.close();


            /* Print the response */
            System.out.println(resPacket);

        } catch (UnknownHostException e) {
            if (cl.debug) {
                Log fl = new Log(new Date(), Log.EntryType.FL,"127.0.0.1","Unknown server address");
                System.out.println(fl);
            }
        } catch (TypeOfValueException e) {
            if (cl.debug) {
                Log fl = new Log(new Date(), Log.EntryType.FL, "127.0.0.1", "Unknown type of value");
                System.out.println(fl);
            }
        } catch (SocketException e) {
            if (cl.debug) {
                Log fl = new Log(new Date(), Log.EntryType.FL, "127.0.0.1", "Error opening the socket");
                System.out.println(fl);
            }
        } catch (SocketTimeoutException e) {
            if (cl.debug) {
                Log to = new Log(new Date(), Log.EntryType.TO, "127.0.0.1", "Query response");
                System.out.println(to);
            }
        } catch (IOException e) {
            if (cl.debug) {
                Log fl = new Log(new Date(), Log.EntryType.FL, "127.0.0.1", "Error sending/receiving the datagram");
                System.out.println(fl);
            }
        } catch (Exception e) {
            if (cl.debug) {
                Log fl = new Log(new Date(), Log.EntryType.FL, "127.0.0.1", "Invalid Arguments");
                System.out.println(fl);
            }
        }

    }
}
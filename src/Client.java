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
    private InetAddress address;    /* 1º arg: IP address of the destination server */
    private int port;               /* 1º arg: Port */
    private int timeout;            /* 2º arg: Timeout */
    private String name;            /* 3º arg: Name */
    private String type;            /* 4º arg: Type of value */
    boolean isRecursive;            /* 5º arg: want the query to be Recursive (optional) */
    boolean debug;                  /* 6º arg: debug mode (optional) */

    public Client() {
        this.address = null;
        this.port = 53;
        this.timeout = 0;
        this.name = "";
        this.type = "";
        this.isRecursive = false;
        this.debug = false;
    }

    public static void main(String[] args) {
        Client cl = new Client();

        try {
            /* Arguments parsing */
            int i = 0;
            String[] words = args[i].split(":");
            cl.address = InetAddress.getByName(words[i++]);
            if (words.length == 2) { cl.port = Integer.parseInt(words[i]); }
            cl.timeout = Integer.parseInt(args[i++]);
            cl.name = args[i++];
            cl.type = args[i++];
            cl.isRecursive = args.length == 5 && args[i].compareTo("R") == 0;
            cl.debug = (args.length == 5 && args[i].compareTo("D") == 0) || (args.length == 6 && args[i++].compareTo("R") == 0 && args[i].compareTo("D") == 0);

            /* Create the client udp socket with the preset timeout */
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(cl.timeout);

            /* Create and send the query */
            DNSPacket sendPacket = new DNSPacket((short) (new Random()).nextInt(1,65535), true, cl.isRecursive, false, cl.name, DNSPacket.typeOfValueConvert(cl.type));
            byte[] sendBytes = sendPacket.dnsPacketToBytes();
            DatagramPacket request = new DatagramPacket(sendBytes, sendBytes.length, cl.address, cl.port);
            socket.send(request);
            if (cl.debug) {
                Log qe = new Log(new Date(), Log.EntryType.QE,cl.address.getHostAddress(),cl.port,sendBytes);
                System.out.println(qe);
            }

            /* Get the query response */
            byte[] receiveBytes = new byte[1000];
            DatagramPacket response = new DatagramPacket(receiveBytes, receiveBytes.length);
            socket.receive(response);
            if (cl.debug) {
                Log rr = new Log(new Date(), Log.EntryType.RR,response.getAddress().getHostAddress(), response.getPort(),sendBytes);
                System.out.println(rr);
            }

            System.out.println(DNSPacket.bytesToDnsPacket(receiveBytes).toString());

        } catch (UnknownHostException | TypeOfValueException | SocketException e) {
            if (cl.debug) {
                Log fl = new Log(new Date(), Log.EntryType.FL,"127.0.0.1",53,null);
                System.out.println(fl);
            }
        } catch (SocketTimeoutException e) {
            if (cl.debug) {
                Log to = new Log(new Date(), Log.EntryType.TO,cl.address.getHostAddress(),cl.port,null);
                System.out.println(to);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (cl.debug) {
                Log er = new Log(new Date(), Log.EntryType.RR,cl.address.getHostAddress(),cl.port,null);
                System.out.println(er);
            }
        } catch (Exception e) {
            if (cl.debug) {
                Log fl = new Log(new Date(), Log.EntryType.FL,"127.0.0.1",53,null);
                System.out.println(fl);
            }
        }

    }
}
import Cache.*;
import DNSPacket.*;
import Exceptions.TypeOfValueException;
import Log.Log;
import ObjectServer.ObjectServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;

public class SolveQueries implements Runnable{
    private int serverPort;
    private byte[] data;
    private InetAddress clientAddress;
    private int clientPort;
    private ObjectServer objectServer;

    public SolveQueries(int serverPort, byte[] data, InetAddress clientAddress, int clientPort, ObjectServer objectServer) {
        this.serverPort = serverPort;
        this.data = data;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.objectServer = objectServer;
    }

    @Override
    public void run() {
        try {
            /* Build received packet */
            DNSPacket receivePacket = DNSPacket.bytesToDnsPacket(data);
            objectServer.writeAnswerInLog(receivePacket.getData().getName(), Log.EntryType.QR, clientAddress.getHostAddress(), clientPort, receivePacket.toString());
            Log qr = new Log(new Date(), Log.EntryType.QR, clientAddress.getHostAddress(), clientPort, receivePacket.toString());
            System.out.println(qr);

            /* Find answer */
            Tuple<Integer, Data> anwser = objectServer.getCache().findAnswer(receivePacket);

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
            objectServer.writeAnswerInLog(receivePacket.getData().getName(), Log.EntryType.RP, clientAddress.getHostAddress(), clientPort, sendPacket.toString());
            Log re = new Log(new Date(), Log.EntryType.RP, clientAddress.getHostAddress(), clientPort, sendPacket.toString());
            System.out.println(re);

            /* Close new socket */
            socket1.close();


        } catch (TypeOfValueException e) {
            try {
                objectServer.writeAnswerInLog(objectServer.getDominio(), Log.EntryType.ER, clientAddress.getHostAddress(), clientPort, e.getMessage());
                Log er = new Log(new Date(), Log.EntryType.ER, clientAddress.getHostAddress(), clientPort, e.getMessage());
                System.out.println(er);
            } catch (IOException ex) {
                Log fl = new Log(new Date(), Log.EntryType.FL, "127.0.0.1", ex.getMessage());
                System.out.println(fl);
            }
        } catch (Exception e) {
            try {
                objectServer.writeAnswerInLog(objectServer.getDominio(), Log.EntryType.FL, "127.0.0.1", serverPort, e.getMessage());
                Log fl = new Log(new Date(), Log.EntryType.FL, "127.0.0.1", e.getMessage());
                System.out.println(fl);
            } catch (Exception ex) {
                Log fl = new Log(new Date(), Log.EntryType.FL, "127.0.0.1", ex.getMessage());
                System.out.println(fl);
            }
        }
    }
}

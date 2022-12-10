import Cache.*;
import DNSPacket.*;
import Exceptions.TypeOfValueException;
import Log.Log;
import ObjectServer.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Iterator;

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

            boolean answerQuery = true;
            boolean isNs = objectServer instanceof ObjectSP ||  objectServer instanceof ObjectSS;

            if (isNs) {
                Iterator<String> it = objectServer.getDD().keySet().iterator();
                boolean found = false;
                String key;
                while (it.hasNext() && !found) {
                    key = it.next();
                    if (receivePacket.getData().getName().contains(key)) {
                        found = true;
                    }
                }

                answerQuery = found;
            }

            if (answerQuery) {

                /* Find answer */
                DNSPacket anwser = objectServer.getCache().findAnswer(receivePacket);
                int respCode = anwser.getHeader().getResponseCode();

                if (respCode == 1 || respCode == 2) {
                    boolean found = false;
                    InetSocketAddress socketAddress;

                    if (!isNs) {
                        Iterator<String> itdd = objectServer.getDD().keySet().iterator();
                        String key = null;
                        while (itdd.hasNext() && !found) {
                            key = itdd.next();
                            if (receivePacket.getData().getName().contains(key)) {
                                found = true;
                            }
                        }

                        if (found) {
                            socketAddress = objectServer.getDD().get(key).get(0);
                        }

                    }

                    if (found) {
                        // envia para dd
                        // espera pela resposta

                    }
                    else if (objectServer.getST()!=null){
                        socketAddress = objectServer.getST().get(0);
                        // envia para st

                        // recebe resposta

                        // add cache

                        // find ip

                        // while nao for resp code

                    }

                    //flags = 0;
                }


                /* Build Packet */

                /* Create Datagram */
                byte[] sendBytes = anwser.dnsPacketToBytes();
                DatagramPacket response = new DatagramPacket(sendBytes, sendBytes.length, clientAddress, clientPort);

                /* Create new Datagram Socket */
                DatagramSocket socket1 = new DatagramSocket();

                /* Send answer */
                socket1.send(response);
                objectServer.writeAnswerInLog(receivePacket.getData().getName(), Log.EntryType.RP, clientAddress.getHostAddress(), clientPort, anwser.toString());
                Log re = new Log(new Date(), Log.EntryType.RP, clientAddress.getHostAddress(), clientPort, anwser.toString());
                System.out.println(re);

                /* Close new socket */
                socket1.close();

            }


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

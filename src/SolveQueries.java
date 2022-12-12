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
                DNSPacket answer = objectServer.getCache().findAnswer(receivePacket);
                int respCode = answer.getHeader().getResponseCode();

                if (respCode == 1 || respCode == 2) {
                    boolean found = false;

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
                            InetSocketAddress socketAddress = objectServer.getDD().get(key).get(0);
                            DatagramPacket r = new DatagramPacket(data,data.length,socketAddress.getAddress(),socketAddress.getPort());

                            DatagramSocket s = new DatagramSocket();
                            s.send(r);

                            byte[] buf = new byte[1000];
                            DatagramPacket res = new DatagramPacket(buf, buf.length);
                            s.receive(res);
                            answer = DNSPacket.bytesToDnsPacket(buf);
                            objectServer.getCache().addData(answer, EntryCache.Origin.OTHERS);
                        }
                    }

                    if (!found && objectServer.getST()!=null){
                        InetSocketAddress socketAddress = objectServer.getST().get(0);

                        // envia para st
                        DatagramPacket r = new DatagramPacket(data, data.length,socketAddress.getAddress(),socketAddress.getPort());
                        DatagramSocket s = new DatagramSocket();
                        s.send(r);

                        // recebe resposta
                        byte[] buf = new byte[1000];
                        DatagramPacket res = new DatagramPacket(buf, buf.length);
                        s.receive(res);
                        DNSPacket np = DNSPacket.bytesToDnsPacket(buf);
                        // add cache
                        objectServer.getCache().addData(np, EntryCache.Origin.OTHERS);

                        int code = np.getHeader().getResponseCode();

                        while (code != 0) {
                            // find ip
                            String ip = objectServer.getCache().findIP(receivePacket.getData().getName());
                            r.setAddress(InetAddress.getByName(ip));
                            r.setPort(5353);
                            s.send(r);

                            byte[] arr = new byte[1000];
                            DatagramPacket rec = new DatagramPacket(arr, arr.length);
                            s.receive(rec);
                            np = DNSPacket.bytesToDnsPacket(arr);
                            answer = np;
                            objectServer.getCache().addData(np, EntryCache.Origin.OTHERS);
                            code = np.getHeader().getResponseCode();
                        }

                    }
                    //flags = 0;
                }


                /* Build Packet */

                /* Create Datagram */
                byte[] sendBytes = answer.dnsPacketToBytes();
                DatagramPacket response = new DatagramPacket(sendBytes, sendBytes.length, clientAddress, clientPort);

                /* Create new Datagram Socket */
                DatagramSocket socket1 = new DatagramSocket();

                /* Send answer */
                socket1.send(response);
                objectServer.writeAnswerInLog(receivePacket.getData().getName(), Log.EntryType.RP, clientAddress.getHostAddress(), clientPort, answer.toString());
                Log re = new Log(new Date(), Log.EntryType.RP, clientAddress.getHostAddress(), clientPort, answer.toString());
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

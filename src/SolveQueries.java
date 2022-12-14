import Cache.*;
import DNSPacket.*;
import Exceptions.TypeOfValueException;
import Log.Log;
import ObjectServer.*;

import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.Iterator;

public class SolveQueries implements Runnable{
    private final Server server;
    private final byte[] data;
    private final InetAddress clientAddress;
    private final int clientPort;
    private final ObjectServer objectServer;

    public SolveQueries(Server server, byte[] data, InetAddress clientAddress, int clientPort, ObjectServer objectServer) {
        this.server = server;
        this.data = data;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.objectServer = objectServer;
    }

    private DNSPacket send_to_server(DatagramSocket s, String key) throws IOException, TypeOfValueException {
        InetSocketAddress socketAddress = objectServer.getDD().get(key).get(0);
        DatagramPacket r = new DatagramPacket(data, data.length, socketAddress.getAddress(), socketAddress.getPort());
        s.send(r);
        byte[] buf = new byte[1000];
        DatagramPacket res = new DatagramPacket(buf, buf.length);
        s.receive(res);
        DNSPacket answer = DNSPacket.bytesToDnsPacket(buf);
        objectServer.getCache().addData(answer, EntryCache.Origin.OTHERS);
        return answer;
    }

    @Override
    public void run() {
        try {
            /* Build received packet */
            DNSPacket receivePacket = DNSPacket.bytesToDnsPacket(data);
            objectServer.writeAnswerInLog(receivePacket.getData().getName(), Log.EntryType.QR, clientAddress.getHostAddress(), clientPort, receivePacket.toString());
            Log qr = new Log(new Date(), Log.EntryType.QR, clientAddress.getHostAddress(), clientPort, receivePacket.toString());
            System.out.println(qr);

            boolean isNs = objectServer instanceof ObjectSP ||  objectServer instanceof ObjectSS;
            DNSPacket answer = null;
            DatagramSocket socket1 = new DatagramSocket();

            if (isNs && !objectServer.getDD().isEmpty()) {
                Iterator<String> it = objectServer.getDD().keySet().iterator();
                boolean found = false;
                String key = null;
                while (it.hasNext() && !found) {
                    key = it.next();
                    if (receivePacket.getData().getName().contains(key)) found = true;
                }
                if (found) answer = objectServer.getCache().findAnswer(receivePacket);

            } else if (isNs && objectServer.getST().isEmpty()) {
                answer = objectServer.getCache().findAnswer(receivePacket);

            } else {
                boolean found = false;
                String key = null;
                if (!isNs) {
                    Iterator<String> it = objectServer.getDD().keySet().iterator();
                    while (it.hasNext() && !found) {
                        key = it.next();
                        if (receivePacket.getData().getName().contains(key)) found = true;
                    }
                }

                if (found) answer = send_to_server(socket1, key);
                else {
                    // iterative mode
                    InetSocketAddress socketAddress = objectServer.getST().get(0);

                    // envia para st
                    DatagramPacket r = new DatagramPacket(data, data.length, socketAddress.getAddress(), socketAddress.getPort());
                    socket1.send(r);
                    Log qe = new Log(new Date(), Log.EntryType.QE, socketAddress.getAddress().getHostAddress(), socketAddress.getPort(), receivePacket.toString());
                    System.out.println(qe);

                    // recebe resposta
                    byte[] buf = new byte[1000];
                    DatagramPacket res = new DatagramPacket(buf, buf.length);
                    socket1.receive(res);
                    Log rr = new Log(new Date(), Log.EntryType.RR, res.getAddress().getHostAddress(), res.getPort(), receivePacket.toString());
                    System.out.println(rr);

                    DNSPacket np = DNSPacket.bytesToDnsPacket(buf);

                    // add cache
                    objectServer.getCache().addData(np, EntryCache.Origin.OTHERS);

                    int code = np.getHeader().getResponseCode();

                    while (code == 1) {
                        System.out.println(code); // para efeitos de debug
                        // find ip
                        String ip = objectServer.getCache().findIP(receivePacket.getData().getName());
                        String[] wd = ip.split(":");
                        System.out.println(ip); // para efeitos de debug
                        r.setAddress(InetAddress.getByName(wd[0]));
                        r.setPort(Integer.parseInt(wd[1]));
                        socket1.send(r);

                        byte[] arr = new byte[1000];
                        DatagramPacket rec = new DatagramPacket(arr, arr.length);
                        socket1.receive(rec);
                        np = DNSPacket.bytesToDnsPacket(arr);
                        answer = np;
                        objectServer.getCache().addData(np, EntryCache.Origin.OTHERS);
                        code = np.getHeader().getResponseCode();
                    }
                }

            }

            // Create Datagram
            byte[] sendBytes = answer.dnsPacketToBytes();
            DatagramPacket response = new DatagramPacket(sendBytes, sendBytes.length, clientAddress, clientPort);

            // Send answer
            socket1.send(response);
            objectServer.writeAnswerInLog(receivePacket.getData().getName(), Log.EntryType.RP, clientAddress.getHostAddress(), clientPort, answer.toString());
            Log re = new Log(new Date(), Log.EntryType.RP, clientAddress.getHostAddress(), clientPort, answer.toString());
            System.out.println(re);

        } catch (TypeOfValueException e) {
            try {
                objectServer.writeAnswerInLog(objectServer.getDominio(), Log.EntryType.ER, clientAddress.getHostAddress(), clientPort, e.getMessage());
                Log er = new Log(new Date(), Log.EntryType.ER, clientAddress.getHostAddress(), clientPort, e.getMessage());
                System.out.println(er);
            } catch (IOException ex) {
                Log fl = new Log(new Date(), Log.EntryType.FL, "127.0.0.1", ex.getMessage());
                System.out.println(fl);
            }
            e.printStackTrace(); // debug
        } catch (Exception e) {
            try {
                objectServer.writeAnswerInLog(objectServer.getDominio(), Log.EntryType.FL, "127.0.0.1", server.getPort(), e.getMessage());
                Log fl = new Log(new Date(), Log.EntryType.FL, "127.0.0.1", e.getMessage());
                System.out.println(fl);
            } catch (Exception ex) {
                Log fl = new Log(new Date(), Log.EntryType.FL, "127.0.0.1", ex.getMessage());
                System.out.println(fl);
            }
            e.printStackTrace(); // debug
        }
    }
}

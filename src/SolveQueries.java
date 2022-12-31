import Cache.*;
import DNSPacket.*;
import Exceptions.TypeOfValueException;
import Log.Log;
import ObjectServer.*;

import java.io.IOException;
import java.net.*;
import java.util.*;

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

    private DNSPacket send_to_server(DatagramSocket s, String key) {
        boolean rr = false;
        DNSPacket answer = null;
        List<InetSocketAddress> l = objectServer.getDD().get(key);
        for(int i=0; i< l.size() && !rr;i++) {
            try {
                InetSocketAddress socketAddress = l.get(i);
                DatagramPacket r = new DatagramPacket(data, data.length, socketAddress.getAddress(), socketAddress.getPort());
                s.send(r);
                objectServer.writeAnswerInLog(DNSPacket.bytesToDnsPacket(data).getData().getName(), Log.EntryType.QE, r.getAddress().getHostAddress(), r.getPort(), DNSPacket.bytesToDnsPacket(data).toString());
                if (server.isDebug()) {
                    Log qe = new Log(new Date(), Log.EntryType.QE, r.getAddress().getHostAddress(), r.getPort(), DNSPacket.bytesToDnsPacket(data).toString());
                    System.out.println(qe);
                }

                byte[] buf = new byte[1000];
                DatagramPacket res = new DatagramPacket(buf, buf.length);
                s.receive(res);
                answer = DNSPacket.bytesToDnsPacket(buf);
                objectServer.writeAnswerInLog(answer.getData().getName(), Log.EntryType.RR,  r.getAddress().getHostAddress(), r.getPort(), answer.toString());
                if (server.isDebug()) {
                    Log resp = new Log(new Date(), Log.EntryType.RR, r.getAddress().getHostAddress(), r.getPort(), answer.toString());
                    System.out.println(resp);
                }
                rr = true;
                objectServer.getCache().addData(answer, EntryCache.Origin.OTHERS);
            } catch (TypeOfValueException | IOException ignored) {}

        }
        return answer;

    }

    private DNSPacket contact_st(DatagramSocket socket, DNSPacket receivePacket) throws IOException {
        Iterator<InetSocketAddress> it = objectServer.getST().iterator();
        InetSocketAddress aux;
        boolean received = false;
        DNSPacket ret = null;
        while (it.hasNext() && !received) {
            aux = it.next();
            // envia para st
            try {
                DatagramPacket r = new DatagramPacket(data, data.length, aux.getAddress(), aux.getPort());
                socket.send(r);
                objectServer.writeAnswerInLog(receivePacket.getData().getName(), Log.EntryType.QE,  r.getAddress().getHostAddress(), r.getPort(), receivePacket.toString());
                if (server.isDebug()) {
                    Log qe = new Log(new Date(), Log.EntryType.QE, r.getAddress().getHostAddress(), r.getPort(), receivePacket.toString());
                    System.out.println(qe);
                }

                // recebe resposta
                byte[] buf = new byte[1000];
                DatagramPacket res = new DatagramPacket(buf, buf.length);
                socket.receive(res);
                received = true;
                ret = DNSPacket.bytesToDnsPacket(buf);
                objectServer.writeAnswerInLog(ret.getData().getName(), Log.EntryType.RR,  r.getAddress().getHostAddress(), r.getPort(), ret.toString());
                if (server.isDebug()) {
                    Log rr = new Log(new Date(), Log.EntryType.RR, res.getAddress().getHostAddress(), res.getPort(), ret.toString());
                    System.out.println(rr);
                }
                break;
            } catch (TypeOfValueException | SocketTimeoutException ignore) {

            }
        }
        return ret;
    }

    private List<InetSocketAddress> getNSSocketAddresses(DNSPacket rp) {
        List<InetSocketAddress> list = new ArrayList<>();

        Value[] av = rp.getData().getAuthoriteValues();
        Value[] ev = rp.getData().getExtraValues();
        String val;

        Arrays.sort(av, (value, t1) -> value.getPrioridade()- t1.getPrioridade());

        for (int i = 0; i<av.length; i++) {
            val = av[i].getValue();
            for (int j = 0; j< ev.length; j++) {
                if (val.equals(ev[j].getDominio())) {
                    String[] wd = ev[j].getValue().split(":");
                    int port = 5353;
                    if (wd.length>1) port = Integer.parseInt(wd[1]);
                    list.add(new InetSocketAddress(wd[0],port));
                }
            }
        }

        return list;
    }

    @Override
    public void run() {
        try {
            /* Build received packet */
            DNSPacket receivePacket = DNSPacket.bytesToDnsPacket(data);
            objectServer.writeAnswerInLog(receivePacket.getData().getName(), Log.EntryType.QR, clientAddress.getHostAddress(), clientPort, receivePacket.toString());
            if (this.server.isDebug()) {
                Log qr = new Log(new Date(), Log.EntryType.QR, clientAddress.getHostAddress(), clientPort, receivePacket.toString());
                System.out.println(qr);
            }

            boolean isNs = objectServer instanceof ObjectSP ||  objectServer instanceof ObjectSS;
            DNSPacket answer = null;
            DatagramSocket socket1 = new DatagramSocket();
            socket1.setSoTimeout(server.getTimeout());

            if (isNs && !objectServer.getDD().isEmpty()) {
                Iterator<String> it = objectServer.getDD().keySet().iterator();
                boolean found = false;
                String key = null;
                while (it.hasNext() && !found) {
                    key = it.next();
                    if (receivePacket.getData().getName().contains(key)) found = true;
                }
                if (found) answer = objectServer.getCache().findAnswer(receivePacket);

                if (receivePacket.getHeader().getFlags() == 3 && found && answer.getHeader().getResponseCode() == 1) {
                    DatagramPacket dp = new DatagramPacket(data, data.length);
                    List<InetSocketAddress> lsa = getNSSocketAddresses(answer);
                    Iterator<InetSocketAddress> itlsa = lsa.iterator();
                    boolean received = false;
                    while (itlsa.hasNext() && !received) {
                        InetSocketAddress sa = itlsa.next();
                        try {
                            dp.setAddress(sa.getAddress());
                            dp.setPort(sa.getPort());
                            socket1.send(dp);
                            objectServer.writeAnswerInLog(receivePacket.getData().getName(), Log.EntryType.QE,  dp.getAddress().getHostAddress(), dp.getPort(), receivePacket.toString());
                            if (server.isDebug()) {
                                Log qe = new Log(new Date(), Log.EntryType.QE, dp.getAddress().getHostAddress(), dp.getPort(), receivePacket.toString());
                                System.out.println(qe);
                            }

                            byte[] arr = new byte[1000];
                            DatagramPacket rec = new DatagramPacket(arr, arr.length);
                            socket1.receive(rec);
                            answer = DNSPacket.bytesToDnsPacket(arr);
                            objectServer.writeAnswerInLog(answer.getData().getName(), Log.EntryType.RR,  dp.getAddress().getHostAddress(), dp.getPort(), answer.toString());
                            if (server.isDebug()) {
                                Log rr = new Log(new Date(), Log.EntryType.RR, dp.getAddress().getHostAddress(), dp.getPort(), answer.toString());
                                System.out.println(rr);
                            }
                            received = true;
                            objectServer.getCache().addData(answer, EntryCache.Origin.OTHERS);
                        } catch (TypeOfValueException | SocketTimeoutException ignored) {}
                    }
                    if (answer.getHeader().getFlags() >= (byte) 4) answer.getHeader().setFlags((byte) ((int) answer.getHeader().getFlags() - 4));

                }

            } else if (isNs && objectServer.getST().isEmpty()) {
                answer = objectServer.getCache().findAnswer(receivePacket);

                if (receivePacket.getHeader().getFlags() == 3 && answer.getHeader().getResponseCode() == 1) {
                    DatagramPacket dp = new DatagramPacket(data, data.length);
                    List<InetSocketAddress> lsa = getNSSocketAddresses(answer);
                    Iterator<InetSocketAddress> itlsa = lsa.iterator();
                    boolean received = false;
                    while (itlsa.hasNext() && !received) {
                        InetSocketAddress sa = itlsa.next();
                        try {
                            dp.setAddress(sa.getAddress());
                            dp.setPort(sa.getPort());
                            socket1.send(dp);
                            objectServer.writeAnswerInLog(receivePacket.getData().getName(), Log.EntryType.QE,  dp.getAddress().getHostAddress(), dp.getPort(), receivePacket.toString());
                            if (server.isDebug()) {
                                Log qe = new Log(new Date(), Log.EntryType.QE, dp.getAddress().getHostAddress(), dp.getPort(), receivePacket.toString());
                                System.out.println(qe);
                            }

                            byte[] arr = new byte[1000];
                            DatagramPacket rec = new DatagramPacket(arr, arr.length);
                            socket1.receive(rec);
                            answer = DNSPacket.bytesToDnsPacket(arr);
                            objectServer.writeAnswerInLog(answer.getData().getName(), Log.EntryType.RR,  dp.getAddress().getHostAddress(), dp.getPort(), answer.toString());
                            if (server.isDebug()) {
                                Log rr = new Log(new Date(), Log.EntryType.RR, dp.getAddress().getHostAddress(), dp.getPort(), answer.toString());
                                System.out.println(rr);
                            }
                            received = true;
                            objectServer.getCache().addData(answer, EntryCache.Origin.OTHERS);
                        } catch (TypeOfValueException | SocketTimeoutException ignored) {}
                    }
                    if (answer.getHeader().getFlags() >= (byte) 4) answer.getHeader().setFlags((byte) ((int) answer.getHeader().getFlags() - 4));

                }

            } else if (!isNs) {
                answer = objectServer.getCache().findAnswer(receivePacket);
		        if(answer.isEmpty()) {
                    String name = receivePacket.getData().getName();
                    String domain = null;
                    for (String s : objectServer.getDD().keySet()) {
                        if (s.contains(name)) {
                            domain = s;
                            break;
                        }
                    }

                    if (domain != null) {
                        DNSPacket paux = send_to_server(socket1,domain);
                        if (paux!=null) {
                            if (paux.getHeader().getFlags() >= (byte) 4) paux.getHeader().setFlags((byte) ((int) answer.getHeader().getFlags() - 4));
                            answer = paux;
                        }
                    }
                    else {
                        // iterative mode
                        DNSPacket np = contact_st(socket1,receivePacket);

                        if (np!=null) {
                            // add cache
                            objectServer.getCache().addData(np, EntryCache.Origin.OTHERS);
                            int code = np.getHeader().getResponseCode();
                            DatagramPacket dp = new DatagramPacket(data, data.length);

                            while (code == 1) {
                                List<InetSocketAddress> lsa = getNSSocketAddresses(np);
                                Iterator<InetSocketAddress> itlsa = lsa.iterator();
                                boolean received = false;
                                while (itlsa.hasNext() && !received) {
                                    InetSocketAddress sa = itlsa.next();
                                    try {
                                        dp.setAddress(sa.getAddress());
                                        dp.setPort(sa.getPort());
                                        socket1.send(dp);
                                        objectServer.writeAnswerInLog(receivePacket.getData().getName(), Log.EntryType.QE,  dp.getAddress().getHostAddress(), dp.getPort(), receivePacket.toString());
                                        if (server.isDebug()) {
                                            Log qe = new Log(new Date(), Log.EntryType.QE, dp.getAddress().getHostAddress(), dp.getPort(), receivePacket.toString());
                                            System.out.println(qe);
                                        }

                                        byte[] arr = new byte[1000];
                                        DatagramPacket rec = new DatagramPacket(arr, arr.length);
                                        socket1.receive(rec);
                                        np = DNSPacket.bytesToDnsPacket(arr);
                                        objectServer.writeAnswerInLog(np.getData().getName(), Log.EntryType.RR,  dp.getAddress().getHostAddress(), dp.getPort(), np.toString());
                                        if (server.isDebug()) {
                                            Log rr = new Log(new Date(), Log.EntryType.RR, dp.getAddress().getHostAddress(), dp.getPort(), np.toString());
                                            System.out.println(rr);
                                        }
                                        received = true;
                                        answer = np;
                                        objectServer.getCache().addData(np, EntryCache.Origin.OTHERS);
                                        code = np.getHeader().getResponseCode();
                                    } catch (TypeOfValueException | SocketTimeoutException ignored) {}
                                }
                            }
                            if (np.getHeader().getFlags() >= (byte) 4) np.getHeader().setFlags((byte) ((int) np.getHeader().getFlags() - 4));
                            if (answer.isEmpty()) answer = np;
                        }
                    }

                }

            }

            // Create Datagram
            byte[] sendBytes = answer.dnsPacketToBytes(server.isDebug());
            DatagramPacket response = new DatagramPacket(sendBytes, sendBytes.length, clientAddress, clientPort);

            // Send answer
            socket1.send(response);
            objectServer.writeAnswerInLog(receivePacket.getData().getName(), Log.EntryType.RP, clientAddress.getHostAddress(), clientPort, answer.toString());
            if (this.server.isDebug()) {
                Log re = new Log(new Date(), Log.EntryType.RP, clientAddress.getHostAddress(), clientPort, answer.toString());
                System.out.println(re);
            }

            socket1.close();

        } catch (TypeOfValueException e) {
            try {
                objectServer.writeAnswerInLog(objectServer.getDominio(), Log.EntryType.ER, clientAddress.getHostAddress(), clientPort, e.getMessage());
                if (this.server.isDebug()) {
                    Log er = new Log(new Date(), Log.EntryType.ER, clientAddress.getHostAddress(), clientPort, e.getMessage());
                    System.out.println(er);
                }
            } catch (IOException ex) {
                Log fl = new Log(new Date(), Log.EntryType.FL, "127.0.0.1", ex.getMessage());
                System.out.println(fl);
            }
        } catch (IOException e) {
            try {
                objectServer.writeAnswerInLog(objectServer.getDominio(), Log.EntryType.FL, "127.0.0.1", server.getPort(), e.getMessage());
                if (this.server.isDebug()) {
                    Log fl = new Log(new Date(), Log.EntryType.FL, "127.0.0.1", e.getMessage());
                    System.out.println(fl);
                }
            } catch (IOException ex) {
                Log fl = new Log(new Date(), Log.EntryType.FL, "127.0.0.1", ex.getMessage());
                System.out.println(fl);
            }
        }
    }
}

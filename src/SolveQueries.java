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

    private DNSPacket contact_st(DatagramSocket socket, DNSPacket receivePacket) throws IOException {
        Iterator<InetSocketAddress> it = objectServer.getST().iterator();
        InetSocketAddress aux;
        boolean received = false;
        DNSPacket ret = null;
        while (it.hasNext() && !received) {
            aux = it.next();
            // envia para st
            DatagramPacket r = new DatagramPacket(data, data.length, aux.getAddress(), aux.getPort());
            socket.send(r);
            Log qe = new Log(new Date(), Log.EntryType.QE, aux.getAddress().getHostAddress(), aux.getPort(), receivePacket.toString());
            System.out.println(qe);

            try {
                // recebe resposta
                byte[] buf = new byte[1000];
                DatagramPacket res = new DatagramPacket(buf, buf.length);
                socket.receive(res);
                received = true;
                ret = DNSPacket.bytesToDnsPacket(buf);
                Log rr = new Log(new Date(), Log.EntryType.RR, res.getAddress().getHostAddress(), res.getPort(), receivePacket.toString());
                System.out.println(rr);
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
            Log qr = new Log(new Date(), Log.EntryType.QR, clientAddress.getHostAddress(), clientPort, receivePacket.toString());
            System.out.println(qr);

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
                        dp.setAddress(sa.getAddress());
                        dp.setPort(sa.getPort());
                        socket1.send(dp);

                        try {
                            byte[] arr = new byte[1000];
                            DatagramPacket rec = new DatagramPacket(arr, arr.length);
                            socket1.receive(rec);
                            answer = DNSPacket.bytesToDnsPacket(arr);
                            received = true;
                            objectServer.getCache().addData(answer, EntryCache.Origin.OTHERS);
                        } catch (TypeOfValueException | SocketTimeoutException ignored) {}
                    }

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
                        dp.setAddress(sa.getAddress());
                        dp.setPort(sa.getPort());
                        socket1.send(dp);

                        try {
                            byte[] arr = new byte[1000];
                            DatagramPacket rec = new DatagramPacket(arr, arr.length);
                            socket1.receive(rec);
                            answer = DNSPacket.bytesToDnsPacket(arr);
                            received = true;
                            objectServer.getCache().addData(answer, EntryCache.Origin.OTHERS);
                        } catch (TypeOfValueException | SocketTimeoutException ignored) {}
                    }

                }

            } else if (!isNs) {
                answer = objectServer.getCache().findAnswer(receivePacket);
		if(answer.isEmpty())
		{
		boolean found = false;
                String key = null;

                Iterator<String> it = objectServer.getDD().keySet().iterator();
                while (it.hasNext() && !found) {
                    key = it.next();
                    if (receivePacket.getData().getName().contains(key)) found = true;
                }

                if (found) {
                    answer = send_to_server(socket1, key); // fazer isto para a lista ???
                    answer.getHeader().setFlags((byte) ((int) answer.getHeader().getFlags() - 4));
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
                                dp.setAddress(sa.getAddress());
                                dp.setPort(sa.getPort());
                                socket1.send(dp);
				Log qe = new Log(new Date(), Log.EntryType.QE, dp.getAddress().getHostAddress(), dp.getPort(), receivePacket.toString());
				System.out.println(qe);
                                try {
                                    byte[] arr = new byte[1000];
                                    DatagramPacket rec = new DatagramPacket(arr, arr.length);
                                    socket1.receive(rec);

				    Log rr = new Log(new Date(), Log.EntryType.RR, rec.getAddress().getHostAddress(), rec.getPort(), receivePacket.toString());
			            System.out.println(rr);
                                    np = DNSPacket.bytesToDnsPacket(arr);
                                    received = true;
                                    answer = np;
				    objectServer.getCache().addData(np, EntryCache.Origin.OTHERS);
                                    code = np.getHeader().getResponseCode();
                                } catch (TypeOfValueException | SocketTimeoutException ignored) {}
                            }
                        }
                        np.getHeader().setFlags((byte) ((int) np.getHeader().getFlags() - 4));
                        if (answer == null) answer = np;
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
            Log re = new Log(new Date(), Log.EntryType.RP, clientAddress.getHostAddress(), clientPort, answer.toString());
            System.out.println(re);

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
            e.printStackTrace(); // debug
        } catch (IOException e) {
            try {
                objectServer.writeAnswerInLog(objectServer.getDominio(), Log.EntryType.FL, "127.0.0.1", server.getPort(), e.getMessage());
                Log fl = new Log(new Date(), Log.EntryType.FL, "127.0.0.1", e.getMessage());
                System.out.println(fl);
            } catch (IOException ex) {
                Log fl = new Log(new Date(), Log.EntryType.FL, "127.0.0.1", ex.getMessage());
                System.out.println(fl);
            }
            e.printStackTrace(); // debug
        }
    }
}

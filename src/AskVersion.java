/**
 * @Author João Martins
 * @Class AskVersion
 * Created date: 03/11/2022
 * Last update: 23/11/2022
 */
import Cache.*;
import Cache.EntryCache;
import DNSPacket.DNSPacket;
import DNSPacket.Data;
import Exceptions.TypeOfValueException;
import Log.Log;
import ObjectServer.ObjectSS;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.Random;


public class AskVersion implements Runnable {
    private ObjectSS objss;
    private Server server;

    /**
     * Construtor da classe AskVersion.
     * @param objss
     */
    public AskVersion(ObjectSS objss, Server server) {
        this.objss = objss;
        this.server = server;
    }

    /**
     * Processo que ocorre em segundo plano no servidor secundário para perguntar a versão ao servidor principal e realizar transferência de zona.
     */
    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(server.getTimeout());
            DatagramPacket dp = null;
            int wait = server.getTimeout();
            while (true) {
                try {
                    DNSPacket qv = new DNSPacket((short) (new Random()).nextInt(1, 65535), (byte) 1, objss.getDominio(), (byte) 2);
                    byte[] sendBytes = qv.dnsPacketToBytes(server.isDebug());
                    dp = new DatagramPacket(sendBytes, sendBytes.length,objss.getSP().getAddress(),objss.getSP().getPort());
                    socket.send(dp);
                    if (this.server.isDebug()) {
                        Log qe = new Log(new Date(), Log.EntryType.QE, objss.getSP().getAddress().getHostAddress(), objss.getSP().getPort(), qv.toString());
                        System.out.println(qe);
                    }

                    byte[] receiveBytes = new byte[1000];
                    DatagramPacket response = new DatagramPacket(receiveBytes, receiveBytes.length);
                    socket.receive(response);
                    DNSPacket resp = DNSPacket.bytesToDnsPacket(receiveBytes);
                    if (this.server.isDebug()) {
                        Log rr = new Log(new Date(), Log.EntryType.RR, response.getAddress().getHostAddress(), response.getPort(), resp.toString());
                        System.out.println(rr);
                    }

                    /* Verifica versão  */
                    Tuple<Byte, Data> respc = objss.getCache().findAnswer(objss.getDominio(), (byte) 2);
                    boolean exexTZ = false;
                    if (respc.getValue1() == 0 && resp.getHeader().getResponseCode() == 0) {
                        String version = respc.getValue2().getResponseValues()[0].getValue();
                        exexTZ = !(version.equals(resp.getData().getResponseValues()[0].getValue()));
                    } else if (respc.getValue1() != 0) {
                        exexTZ = true;
                    }

                    if (exexTZ) {
                        Socket s = new Socket(objss.getSP().getAddress(), objss.getSP().getPort());
                        DataOutputStream toClient = new DataOutputStream(s.getOutputStream());
                        DataInputStream fromClient = new DataInputStream(s.getInputStream());

                        /* Send domain */
                        toClient.writeUTF(objss.getDominio());
                        toClient.flush();

                        /* Receive number of entrys */
                        int ne = fromClient.read();

                        /* Accept the number of entrys */
                        toClient.write(ne);
                        toClient.flush();

                        String line;
                        int nerec = 0;
                        while (nerec < ne) {
                            line = fromClient.readUTF();
                            String[] w = line.split(":");
                            nerec++;
                            try {
                                objss.getCache().addData(w[1], EntryCache.Origin.SP);
                            } catch (Exception e) {
                                System.out.println("Linha " + nerec + " errada");
                            }
                        }

                        toClient.close();
                        fromClient.close();
                        s.close();

                        Cache cache = objss.getCache();
                        if (cache != null) {
                            String soar = cache.findAnswer(objss.getDominio(), (byte) 3).getValue2().getResponseValues()[0].getValue();
                            wait = Integer.parseInt(soar);
                        }

                        if (this.server.isDebug()) {
                            Log zt = new Log(new Date(), Log.EntryType.ZT, objss.getSP().getAddress().getHostAddress(), objss.getSP().getPort(), "SS");
                            System.out.println(zt);
                        }
                    }

                } catch (SocketTimeoutException e) {
                    if (this.server.isDebug()) {
                        Log to = new Log(new Date(), Log.EntryType.TO, "127.0.0.1", "Zone Transfer");
                        System.out.println(to);
                    }
                    Cache cache = objss.getCache();
                    if (cache != null) {
                        Tuple<Byte, Data> fa = cache.findAnswer(objss.getDominio(), (byte) 4);
                        if (fa.getValue1() != 0) {
                            wait = this.server.getTimeout();
                        }
                        else {
                            wait = Integer.parseInt(fa.getValue2().getResponseValues()[0].getValue());
                        }
                    }
                } catch (IOException | TypeOfValueException e) {
                    if (this.server.isDebug()) {
                        Log to = new Log(new Date(), Log.EntryType.EZ, objss.getSP().getAddress().getHostAddress(),objss.getSP().getPort(), "SS");
                        System.out.println(to);
                    }

                    Cache cache = objss.getCache();
                    if (cache != null) {
                        Tuple<Byte, Data> fa = cache.findAnswer(objss.getDominio(), (byte) 3);
                        if (fa.getValue1() != 0) {
                            wait = this.server.getTimeout();
                        }
                        else {
                            wait = Integer.parseInt(fa.getValue2().getResponseValues()[0].getValue());
                        }
                    }

                }

                Thread.sleep(wait);

            }
        } catch (InterruptedException | SocketException e) {
            Log to = new Log(new Date(), Log.EntryType.FL, "127.0.0.1", "Error in zone tranfer thread");
            System.out.println(to);
        }


    }
}

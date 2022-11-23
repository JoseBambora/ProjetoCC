import Cache.*;
import Cache.EntryCache;
import DNSPacket.Data;
import ObjectServer.ObjectSS;

import java.io.*;
import java.net.Socket;


public class AskVersion implements Runnable {
    private ObjectSS objss;

    public AskVersion(ObjectSS objss) {
        this.objss = objss;
    }

    @Override
    public void run() {
        try {
            while (true) {

                /* Verifica versão  */
                Tuple<Integer, Data> respc = objss.getCache().findAnswer(objss.getDominio(), (byte) 2);
                boolean execTZ = false;
                if (respc.getValue1() != 3) {
                    execTZ = true;
                }

                // Com timeout -> o valor de wait é o soaretry
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

                int wait = 0;
                Cache cache = objss.getCache();
                if (cache != null) {
                    String soar = cache.findAnswer(objss.getDominio(), (byte) 3).getValue2().getResponseValues()[0].getValue();
                    wait = Integer.parseInt(soar);
                }

                Thread.sleep(wait);
            }
        } catch (InterruptedException |IOException e) {

        }



    }
}

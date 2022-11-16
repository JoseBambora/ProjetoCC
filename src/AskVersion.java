import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

public class AskVersion implements Runnable{
    private ObjectSS objss;

    public AskVersion(ObjectSS objss) {
        this.objss = objss;
    }

    @Override
    public void run() {
        try {

            while (true) {

                InetAddress sp = objss.getSP().getAddress();
                Socket s = new Socket(InetAddress.getByName("127.0.0.1"),6363);
                PrintWriter toClient = new PrintWriter(s.getOutputStream());
                BufferedReader fromClient = new BufferedReader(new InputStreamReader(s.getInputStream()));

                DNSPacket qe = new DNSPacket((short) (new Random().nextInt(1,65535)), (byte) 1, objss.getDominio(),Data.typeOfValueConvert("SOASERIAL"));
                String aux = qe.toString();
                toClient.println(aux.substring(0,aux.length()-1));
                toClient.flush();

                String rr = fromClient.readLine();

                /* Verify version */
                if (rr != null) {
                    /* Send domain */
                    toClient.println(objss.getDominio());
                    toClient.flush();

                    /* Receive number of entrys */
                    int ne = Integer.parseInt(fromClient.readLine());
                    boolean accept = true; /* Accept the number of entrys */
                    if (accept) {
                        toClient.println(ne);
                        toClient.flush();

                        String line;
                        int nerec = 0;
                        while ((line = fromClient.readLine()) != null) {
                            String[] w = line.split("-");
                            nerec++;
                            System.out.println(w[1]);
                        }

                    }
                }

                s.close();

                //String soar = objss.getCache().findAnswer(objss.getDominio(),(byte) 3).getResponseValues()[0].getValue();

                Thread.sleep(5000);

            }

        } catch (TypeOfValueException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }


    }
}

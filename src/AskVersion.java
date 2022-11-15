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
            int soarefresh = 1000;

            while (true) {

                Socket s = new Socket(InetAddress.getByName("127.0.0.1"),6363);
                PrintWriter toClient = new PrintWriter(s.getOutputStream(),true);
                BufferedReader fromClient = new BufferedReader(new InputStreamReader(s.getInputStream()));

                DNSPacket qe = new DNSPacket((short) (new Random().nextInt(1,65535)), (byte) 1, objss.getDominio(),Data.typeOfValueConvert("SOASERIAL"));

                toClient.println(qe.toString());
                toClient.flush();

                String rr = fromClient.readLine();
                // verificar versao
                System.out.println(rr);

                // envia dominio
                toClient.println(objss.getDominio());

                // recebe entradas
                int ne = Integer.parseInt(fromClient.readLine());
                boolean accept = true;
                if (accept) {
                    toClient.println(ne);

                    boolean lst = false;
                    String str;
                    while (!lst) {
                        str = fromClient.readLine();
                        String[] w = str.split("-");
                        if (Integer.parseInt(w[0])==ne) lst = true;
                        // adiciona à cache
                        System.out.println(w[1]);
                    }

                }
                else {
                    toClient.println("end");
                }


                s.close();

                Thread.sleep(soarefresh);
            }

        } catch (TypeOfValueException | InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}

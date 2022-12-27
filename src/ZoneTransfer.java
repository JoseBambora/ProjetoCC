/**
 * @Author João Martins
 * @Class ZoneTransfer
 * Created date: 03/11/2022
 * Last update: 14/12/2022
 */
import ObjectServer.ObjectSP;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class ZoneTransfer implements Runnable {
        private ObjectSP objsp;
        private boolean debug;

        /**
         * Contrutor da classe ZoneTransfer.
         * @param objsp
         */
        public ZoneTransfer(ObjectSP objsp, boolean debug) {
                this.objsp = objsp;
                this.debug = debug;
        }

        /**
         * Processo que vai correr em um segundo plano no servidor principal para realizar operações de transferência de zona.
         */
        @Override
        public void run() {
                try (ServerSocket socketTcp = new ServerSocket(5353)) {

                        while (true) {
                                Socket c = socketTcp.accept();
                                Thread ZT = new Thread(new ZoneTransferManager(this.objsp,c,debug));
                                ZT.start();
                        }

                } catch (IOException e) {
                        throw new RuntimeException(e);
                }


        }
}

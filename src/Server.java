/**
 * @author João Martins
 * Classe Servidor
 * Data de criação 03/11/2022
 * Data de edição 04/11/2022
 */


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Server {

    public static void main(String[] args) {
        try {
            int i = 0;
            int porta = 53;
            if (args.length == 3) {
                porta = Integer.parseInt(args[0]);
                i += 1;
            }
            String timeout = args[i++];
            String modo = args[i];   // debug ou não, especificar isto

            DatagramSocket socket = new DatagramSocket(porta);

            while (true) {

                // receber pacote
                byte[] receiveBytes = new byte[1000];
                DatagramPacket request = new DatagramPacket(receiveBytes, receiveBytes.length);
                socket.receive(request);

                DNSPacket receivePacket = DNSPacket.bytesToDnsPacket(receiveBytes);

                //criar log e escrever no output

                InetAddress clientAddress = request.getAddress();
                int clientPort = request.getPort();

                // criar resposta
                // A se for autoritativo - 3 flag
                DNSPacket sendPacket= new DNSPacket(receivePacket.getMessageID(),false,false,true,receivePacket.getName(),receivePacket.getTypeOfValue());
                byte[] sendBytes = sendPacket.dnsPacketToBytes();
                // enviar resposta
                DatagramPacket response = new DatagramPacket(sendBytes, sendBytes.length, clientAddress, clientPort);
                socket.send(response);
            }

        }
        catch (Exception e) {
            System.out.println("Invalid Arguments.");
        }

    }

}

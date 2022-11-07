/**
 * @author João Martins
 * Classe Cliente
 * Data de criação 03/11/2022
 * Data de edição 04/11/2022
 */


/*
 * 1 argumento - IP[:Porta]
 * 2 argumento - timeout *MUDAR NO RELATORIO
 * 3 argummento - Name
 * 4 argumento - Type os value
 * 5 (Opcional) argumento - R , query recursiva ou não
 */

import java.io.IOException;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        try {
            int i = 0;
            String[] words = args[i].split(":");
            String ipn = words[i++];
            InetAddress address = InetAddress.getByName(ipn);
            int porta = 53;
            if (words.length == 2) {
                porta = Integer.parseInt(words[i]);
            }
            int timeout = Integer.parseInt(args[i++]);
            String name = args[i++];
            String type = args[i++];
            boolean isRecursive = args.length == 5 && args[i].compareTo("R") == 0;

            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(timeout);

            // ver message id
            DNSPacket sendPacket = new DNSPacket((short) 0,true,isRecursive,false,name,DNSPacket.typeOfValueConvert(type));
            byte[] sendBytes = sendPacket.dnsPacketToBytes();
            DatagramPacket request = new DatagramPacket(sendBytes, sendBytes.length, address, porta);
            socket.send(request);

            byte[] receiveBytes = new byte[1000];
            DatagramPacket response = new DatagramPacket(receiveBytes, receiveBytes.length);
            socket.receive(response);

            System.out.println(DNSPacket.bytesToDnsPacket(receiveBytes).toString());

        } catch (SocketException | UnknownHostException exception) {
            throw new RuntimeException(exception);
        } catch (SocketTimeoutException ex) {
            System.out.println("Timeout error: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception e) {
            System.out.println("Invalid Arguments.");
        }


    }
}
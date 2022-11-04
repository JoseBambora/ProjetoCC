/**
 * @author João Martins
 * Classe Cliente
 * Data de criação 03/11/2022
 * Data de edição 04/11/2022
 */


/*
 * Servidor de destino das suas queries - 1 argumento - IP[:Porta]
 * 2 argummento - Name
 * 3 argumento - Type os value
 * 4 (Opcional) argumento - R , query recursiva ou não
 */

import java.io.IOException;
import java.net.*;
import java.util.Random;


public class Client {
    public static void main(String[] args) {
        try {
            String[] words = args[0].split(":");
            String ipn = words[0];
            int porta = 53;
            if (words.length == 2) {
                porta = Integer.parseInt(words[1]);
            }
            String name = args[1];
            String type = args[2];
            boolean isRecursive = args.length == 4 && args[3].compareTo("R") == 0;

            InetAddress address = InetAddress.getByName(ipn);
            DatagramSocket socket = new DatagramSocket();

            // ver message id
            DNSPacket sendPacket = new DNSPacket((short) 0,true,false,isRecursive,name,DNSPacket.typeOfValueConvert(type));
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
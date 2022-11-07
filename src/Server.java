/**
 * @author João Martins
 * Classe Servidor
 * Data de criação 03/11/2022
 * Data de edição 04/11/2022
 */


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;

/*
    Argumento 1: Ficheiro de configuração
    Argumento 2: Valor de timeout à espera de um query
    Argumento 3: (opcional) porta de funcionameto
    Argumento 4: (opcional) funcionar em Debug (usar um 'D')
 */

// Tipo do valor | DB   SP   SS   ST
// SP            |  t   nt    t    t
// SS            |  nt  t    nt    t
// SR            |  nt  nt   nt    t
// ST            |  t   nt   nt   nt
// SDT           funciona como sp

public class Server {

    public static void main(String[] args) {
        try {
            int i = 0;
            int porta = 53;
            String configFile = args[i++];
            String timeout = args[i++];
            boolean debug = false;

            if (args.length == 3 && args[i].compareTo("D")==0) {
                debug = true;
            }
            else if (args.length == 3) {
                porta = Integer.parseInt(args[i]);
            }
            else if (args.length == 4){
                porta = Integer.parseInt(args[i++]);
                debug = args[i].compareTo("D")==0;
            }

            // configurar servidor
            ServidorConfiguracao sc = ServidorConfiguracao.parseServer(configFile);

            // flag para identificar o tipo do servidor
            boolean sp = sc instanceof ServidorSP;
            boolean ss = sc instanceof ServidorSS;

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
                Log qr = new Log(new Date(), Log.EntryType.QR,Endereco.stringToIP(clientAddress.toString()),clientPort,receiveBytes);
                if (debug) System.out.println(qr);

                DNSPacket sendPacket = null;
                Header header = new Header(receivePacket.getHeader().getMessageID(), false, receivePacket.getHeader().isFlagA(),false);
                // todos acedem à cache
                Data resp = sc.getCache().findAnswer(receivePacket).getValue2();
                if (resp != null) {
                    sendPacket = new DNSPacket(header,resp);
                }
                else if (sp) {
                    ServidorSP spServer = (ServidorSP) sc;
                    /* Value[] responseValues = spServer.getDB().getInfo(receivePacket.getData().getName(),receivePacket.getData().getTypeOfValue()).getValue2();

                    if (responseValues!=null) {
                        header.setFlagA(true);
                        sendPacket = new DNSPacket(header,new Data(receivePacket.getData().getName(),receivePacket.getData().getTypeOfValue(),responseValues,null,null));
                    }*/
                }
                else if (ss) {
                    ServidorSS ssServer = (ServidorSS) sc;
                    /*String[] responseValues = ssServer.getDB().getInfo(receivePacket.getData().getName(),receivePacket.getData().getTypeOfValue()).getValue2();

                    if (responseValues!=null) {
                        header.setFlagA(true);
                        sendPacket = new DNSPacket(header,new Data(receivePacket.getData().getName(),receivePacket.getData().getTypeOfValue(),responseValues,null,null));
                    }*/
                }

                // campo dd

                if (sendPacket==null && !sc.getST().isEmpty()) {
                    // perguntar aos st
                }

                if (sendPacket!= null) {
                    byte[] sendBytes = sendPacket.dnsPacketToBytes();
                    // enviar resposta
                    DatagramPacket response = new DatagramPacket(sendBytes, sendBytes.length, clientAddress, clientPort);
                    socket.send(response);
                    Log qe = new Log(new Date(), Log.EntryType.QE,Endereco.stringToIP(clientAddress.toString()),clientPort,sendBytes);
                    if (debug) System.out.println(qe);
                }
            }

        }
        catch (Exception e) {
            System.out.println("Invalid Arguments.");
        }

    }

}

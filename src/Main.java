import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Cache bd = CacheSP.createBD("DatabasesFiles/Example.db");
        //System.out.println(bd);
        System.out.println(bd.findAnswer("example.com.",(byte) 0));
        System.out.println(bd.findAnswer("example.com.",(byte) 1));
        System.out.println(bd.findAnswer("example.com.",(byte) 2));
        System.out.println(bd.findAnswer("example.com.",(byte) 3));
        System.out.println(bd.findAnswer("example.com.",(byte) 4));
        System.out.println(bd.findAnswer("example.com.",(byte) 5));
        System.out.println(bd.findAnswer("example.com.",(byte) 6));
        System.out.println(bd.findAnswer("ns1.example.com.",(byte) 7));
        System.out.println(bd.findAnswer("mail1.example.com.",(byte) 8));
        System.out.println(bd.findAnswer("example.com.",(byte) 9));
        //Cache cache = new Cache(10);
        //Value []aux = new Value[1];
        //Value []aux2 = new Value[1];
        //byte b = (byte) 1;
        //aux[0] = new Value("Teste",b,"valor",1);
        //aux2[0] = new Value("Teste1",b,"valor",1);
        //DNSPacket query = new DNSPacket((short) 0,true,true,true,"Teste",b);
        //DNSPacket answer = new DNSPacket((short) 0,true,true,true,b,b,b,b,"Teste",b,aux,aux,aux);
        //DNSPacket answer2 = new DNSPacket((short) 0,true,true,true,b,b,b,b,"Teste2",b,aux2,aux2,aux2);
        //cache.addData(new DNSPacket((short) 0,true,true,true,b,b,b,b,"Teste",b,aux,aux,aux), EntryCache.Origin.SP);
        //System.out.println("Find Answer:\n" + cache.findAnswer(query));
        //System.out.println("Cache:\n" + cache);
        //Thread.sleep(2000);
        //cache.removeData();
        //System.out.println("Pós-remoção:\n" + cache);
        //cache.addData(answer, EntryCache.Origin.SP);
        //cache.removeByName("Teste");
        //System.out.println("Pós-remoção by name:\n" + cache);
        //cache.addData(answer, EntryCache.Origin.SP);
        //cache.addData(answer, EntryCache.Origin.SP);
        //cache.addData(answer2, EntryCache.Origin.SP);
        //System.out.println("Inserção repetidos:\n" + cache);

        /**
        ServidorConfiguracao SP = ServidorConfiguracao.parseServer("ConfigurationFiles/config");
        System.out.println(SP);
        **/
     }
}
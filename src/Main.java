import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Database bd = Database.createBD("DatabasesFiles/Example.db");
        System.out.println(bd);
        System.out.println(Arrays.toString(bd.getInfo("example.com.", (byte) 0).getValue2()));
        System.out.println(Arrays.toString(bd.getInfo("example.com.",(byte) 1).getValue2()));
        System.out.println(Arrays.toString(bd.getInfo("example.com.",(byte) 2).getValue2()));
        System.out.println(Arrays.toString(bd.getInfo("example.com.",(byte) 3).getValue2()));
        System.out.println(Arrays.toString(bd.getInfo("example.com.",(byte) 4).getValue2()));
        System.out.println(Arrays.toString(bd.getInfo("example.com.",(byte) 5).getValue2()));
        System.out.println(Arrays.toString(bd.getInfo("example.com.",(byte) 6).getValue2()));
        System.out.println(Arrays.toString(bd.getInfo("example.com.",(byte) 7).getValue2()));
        System.out.println(Arrays.toString(bd.getInfo("mail1.example.com.",(byte) 8).getValue2()));
        System.out.println(Arrays.toString(bd.getInfo("example.com.",(byte) 9).getValue2()));
        Cache cache = new Cache(10);
        String aux[] = new String[1];
        aux[0] = "Teste";
        byte b = (byte) 1;
        cache.addLog(new DNSPacket((short) 0,true,true,true,b,b,b,b,"Teste",b,aux,aux,aux));
        System.out.println(cache);
        cache.removeLog();
        System.out.println(cache);
        /**
        ServidorConfiguracao SP = ServidorConfiguracao.parseServer("ConfigurationFiles/config");
        System.out.println(SP);
        **/
     }
}
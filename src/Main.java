import java.io.IOException;

public class Main {
    private static void find(Cache bd, String domQ)
    {
        System.out.println("==========================================================");
        System.out.println(bd.findAnswer(domQ,(byte) 0));
        System.out.println(bd.findAnswer(domQ,(byte) 1));
        System.out.println(bd.findAnswer(domQ,(byte) 2));
        System.out.println(bd.findAnswer(domQ,(byte) 3));
        System.out.println(bd.findAnswer(domQ,(byte) 4));
        System.out.println(bd.findAnswer(domQ,(byte) 5));
        System.out.println(bd.findAnswer(domQ,(byte) 6));
        System.out.println(bd.findAnswer("sp." + domQ,(byte) 7));
        System.out.println(bd.findAnswer("ss1." + domQ,(byte) 7));
        System.out.println(bd.findAnswer("mail2." + domQ,(byte) 7));
        System.out.println(bd.findAnswer(domQ,(byte) 9));
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        Cache bd = new Cache();
        bd.createBD("DatabasesFiles/Braga.db");
        Cache bd1 = new Cache();
        bd1.createBD("DatabasesFiles/Cancelo.db");
        String domQ = "CR7.CMS.G706.";
        find(bd,domQ);
        domQ = "M10.JJM.G706.";
        find(bd1,domQ);
        Cache cache = new Cache();
        Value []aux = new Value[1];
        Value []aux2 = new Value[1];
        byte b = (byte) 1;
        aux[0] = new Value("Teste",b,"valor",1);
        aux2[0] = new Value("Teste2",b,"valor",1);
        DNSPacket query = new DNSPacket((short) 0, (byte) 1,"Teste",b);
        DNSPacket answer = new DNSPacket((short) 0, (byte) 1,b,b,b,b,"Teste",b,aux,null,null);
        DNSPacket answer2 = new DNSPacket((short) 0, (byte) 1,b,b,b,b,"Teste2",b,aux2,null,null);
        cache.addData(new DNSPacket((short) 0, (byte) 1,b,b,b,b,"Teste",b,aux,null,null), EntryCache.Origin.SP);
        System.out.println("Find Answer SOAADMIN:\n" + cache.findAnswer(query));
        System.out.println("Cache:\n" + cache);
        Thread.sleep(2000);
        cache.removeExpireInfo();
        System.out.println("Pós-remoção:\n" + cache);
        cache.addData(answer, EntryCache.Origin.SP);
        cache.removeByName("Teste");
        System.out.println("Pós-remoção by name:\n" + cache);
        cache.addData(answer, EntryCache.Origin.OTHERS);
        cache.addData(answer, EntryCache.Origin.OTHERS);
        cache.addData(answer2, EntryCache.Origin.OTHERS);
        System.out.println("Inserção repetidos:\n" + cache);
        ObjectServer SP= ObjectServer.parseServer("ConfigurationFiles/configPepe");
        System.out.println(SP);

     }

}
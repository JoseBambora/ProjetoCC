import Cache.*;
import DNSPacket.Data;
import DNSPacket.Value;
import ObjectServer.ObjectServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static void find(Cache bd, String domQ) throws IOException {
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
    public static void main(String[] args) throws Exception {
        Cache bd1 = new Cache();
        bd1.createBD("../DatabasesFiles/Braga.db", "CR7.CMS.G706.","../LogsFiles/CR7.CMS.G706.log");
        Cache bd2 = new Cache();
        bd2.createBD("../DatabasesFiles/Topo.db","G706.","../LogsFiles/all.log");
        Cache bd3 = new Cache();
        bd3.createBD("../DatabasesFiles/Cancelo.db", "M10.JJM.G706.","../LogsFiles/all.log");
        Cache bd4 = new Cache();
        bd4.createBD("../DatabasesFiles/Rui.db","REVERSE.G706.","../LogsFiles/all.log");
        Cache bd5 = new Cache();
        bd5.addData(new Value("braga", (byte) 8,"alan",1000), EntryCache.Origin.OTHERS);
        bd5.addData(new Value("alan", (byte) 9,"alan.email.com.",1000), EntryCache.Origin.OTHERS);
        ObjectServer SP= ObjectServer.parseServer("../ConfigurationFiles/configPepe");
        System.out.println(SP.getCache().toString().equals("""
                G706. NS pepe.G706. 90000
                G706. NS palhinha.G706. 90000
                CMS.G706. NS william.CMS.G706. 90000
                CMS.G706. NS mario.CMS.G706. 90000
                CMS.G706. NS dalot.CMS.G706. 90000
                JJM.G706. NS felix.JJM.G706. 90000
                JJM.G706. NS otavio.JJM.G706. 90000
                JJM.G706. NS ramos.JJM.G706. 90000
                pepe.G706. A 10.0.14.11:5353 90000
                palhinha.G706. A 10.0.13.11:5353 90000
                william.CMS.G706. A 10.0.8.12:5353 90000
                mario.CMS.G706. A 10.0.16.13:5353 90000
                dalot.CMS.G706. A 10.0.14.12:5353 90000
                felix.JJM.G706. A 10.0.13.12:5353 90000
                otavio.JJM.G706. A 10.0.15.13:5353 90000
                ramos.JJM.G706. A 10.0.10.13:5353 90000
                st1.G706. CNAME pepe.G706. 90000
                st2.G706. CNAME palhinha.G706. 90000
                ss1sd1.JJM.G706. CNAME otavio.JJM.G706. 90000
                ss2sd1.JJM.G706. CNAME ramos.JJM.G706. 90000
                spsd2.CMS.G706. CNAME william.CMS.G706. 90000
                ss1sd2.CMS.G706. CNAME mario.CMS.G706. 90000
                ss2sd2.CMS.G706. CNAME dalot.CMS.G706. 90000
                sd3.REVERSE.G706. CNAME rui.REVERSE.G706. 90000
                """));
        Tuple<Integer, Data> data5 = bd1.findAnswer("CR7.CMS.G706.",Data.typeOfValueConvert("SOASP"));
        Tuple<Integer,Data> data6 = bd1.findAnswer("renato.CR7.CMS.G706.",Data.typeOfValueConvert("A"));
        Tuple<Integer,Data> data7 = bd1.findAnswer("mail1.CR7.CMS.G706.",Data.typeOfValueConvert("A"));
        Tuple<Integer,Data> data8 = bd2.findAnswer("G706.",Data.typeOfValueConvert("NS"));
        Tuple<Integer,Data> data9 = bd3.findAnswer("M10.JJM.G706.",Data.typeOfValueConvert("MX"));
        Tuple<Integer,Data> data10 = bd5.findAnswer("braga", (byte) 9);
        Tuple<Integer,Data> data11 = bd5.findAnswer("braga", (byte) 8);
        Tuple<Integer,Data> data12 = bd1.findAnswer("mail1.CR7.CMS.G706.",Data.typeOfValueConvert("NS"));
        Tuple<Integer,Data> data13 = bd1.findAnswer("mail3.CR7.CMS.G706.",Data.typeOfValueConvert("MX"));
        Tuple<Integer,Data> data14 = bd1.findAnswer("CR7.CMS.G706.",Data.typeOfValueConvert("A"));
        System.out.println(data5.getValue2().toString().equals("""
                CR7.CMS.G706.,SOASP;
                CR7.CMS.G706. SOASP braga.CR7.CMS.G706. 90000;
                CR7.CMS.G706. NS braga.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS renato.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS mendes.CR7.CMS.G706. 90000;
                braga.CR7.CMS.G706. A 10.0.15.10:5353 90000,
                renato.CR7.CMS.G706. A 10.0.16.10:5353 90000,
                mendes.CR7.CMS.G706. A 10.0.13.10:5353 90000;"""));
        System.out.println(data6.getValue2().toString().equals("""
                renato.CR7.CMS.G706.,A;
                renato.CR7.CMS.G706. A 10.0.16.10:5353 90000;
                CR7.CMS.G706. NS braga.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS renato.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS mendes.CR7.CMS.G706. 90000;
                braga.CR7.CMS.G706. A 10.0.15.10:5353 90000,
                mendes.CR7.CMS.G706. A 10.0.13.10:5353 90000;"""));
        System.out.println(data7.getValue2().toString().equals("""
                fernando1.CR7.CMS.G706.,A;
                fernando1.CR7.CMS.G706. A 10.0.8.11:5353 90000;
                CR7.CMS.G706. NS braga.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS renato.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS mendes.CR7.CMS.G706. 90000;
                braga.CR7.CMS.G706. A 10.0.15.10:5353 90000,
                renato.CR7.CMS.G706. A 10.0.16.10:5353 90000,
                mendes.CR7.CMS.G706. A 10.0.13.10:5353 90000;"""));
        System.out.println(data8.getValue2().toString().equals("""
                G706.,NS;
                G706. NS pepe.G706. 90000,
                G706. NS palhinha.G706. 90000;
                G706. NS pepe.G706. 90000,
                G706. NS palhinha.G706. 90000,
                CMS.G706. NS william.CMS.G706. 90000,
                CMS.G706. NS mario.CMS.G706. 90000,
                CMS.G706. NS dalot.CMS.G706. 90000,
                JJM.G706. NS felix.JJM.G706. 90000,
                JJM.G706. NS otavio.JJM.G706. 90000,
                JJM.G706. NS ramos.JJM.G706. 90000;
                pepe.G706. A 10.0.14.11:5353 90000,
                palhinha.G706. A 10.0.13.11:5353 90000,
                william.CMS.G706. A 10.0.8.12:5353 90000,
                mario.CMS.G706. A 10.0.16.13:5353 90000,
                dalot.CMS.G706. A 10.0.14.12:5353 90000,
                felix.JJM.G706. A 10.0.13.12:5353 90000,
                otavio.JJM.G706. A 10.0.15.13:5353 90000,
                ramos.JJM.G706. A 10.0.10.13:5353 90000;"""));
        System.out.println(data9.getValue2().toString().equals("""
                M10.JJM.G706.,MX;
                M10.JJM.G706. MX scaloni1.M10.JJM.G706. 90000 10,
                M10.JJM.G706. MX scaloni2.M10.JJM.G706. 90000 20;
                M10.JJM.G706. NS cancelo.M10.JJM.G706. 90000,
                M10.JJM.G706. NS rafael.M10.JJM.G706. 90000,
                M10.JJM.G706. NS bruno.M10.JJM.G706. 90000;
                cancelo.M10.JJM.G706. A 10.0.9.11:5353 90000,
                rafael.M10.JJM.G706. A 10.0.15.11:5353 90000,
                bruno.M10.JJM.G706. A 10.0.14.10:5353 90000,
                scaloni1.M10.JJM.G706. A 10.0.10.11:5353 90000,
                scaloni2.M10.JJM.G706. A 10.0.16.12:5353 90000;"""));
        System.out.println(data10.getValue2().toString().equals("alan,MX;\nalan MX alan.email.com. 1000;"));
        System.out.println(data11.getValue2().toString().equals("braga,CNAME;\nbraga CNAME alan 1000;"));
        System.out.println(data12.getValue1() == 1);
        System.out.println(data12.getValue2().toString().equals("fernando1.CR7.CMS.G706.,NS;"));
        System.out.println(data13.getValue1() == 2);
        System.out.println(data13.getValue2().toString().equals("""
                mail3.CR7.CMS.G706.,MX;
                CR7.CMS.G706. NS braga.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS renato.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS mendes.CR7.CMS.G706. 90000;
                braga.CR7.CMS.G706. A 10.0.15.10:5353 90000,
                renato.CR7.CMS.G706. A 10.0.16.10:5353 90000,
                mendes.CR7.CMS.G706. A 10.0.13.10:5353 90000;"""));
        System.out.println(data14.getValue2().toString().equals("""
                CR7.CMS.G706.,A;
                CR7.CMS.G706. NS braga.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS renato.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS mendes.CR7.CMS.G706. 90000;
                braga.CR7.CMS.G706. A 10.0.15.10:5353 90000,
                renato.CR7.CMS.G706. A 10.0.16.10:5353 90000,
                mendes.CR7.CMS.G706. A 10.0.13.10:5353 90000;"""));
        /*
        int num = 75;
        System.out.println("COMEÇA TESTE CONCORRÊNCIA");
        Cache cacheTestThreads = new Cache();
        Thread[] threads = new Thread[num];
        for(int i = 0; i < num; i++)
            threads[i] = new Thread(new TesteCacheThreads(Integer.toString(i),cacheTestThreads,(byte) 0,num));
        for(int i = 0; i < num; i++)
            threads[i].start();
        for(int i = 0; i < num; i++)
            threads[i].join();
        System.out.println("ACABOU");
        System.out.println(cacheTestThreads.size() == num * num);
         */
        Cache bd7 = new Cache();
        bd7.setDominio("CR7.CMS.G706.");
        List<String> lines = Files.readAllLines(Paths.get("../DatabasesFiles/Braga.db"), StandardCharsets.UTF_8);
        lines.forEach(s -> bd7.addData(s, EntryCache.Origin.SP));
        System.out.println(bd7.findAnswer("braga.CR7.CMS.G706.", (byte) 7).getValue2().toString().equals("""
                braga.CR7.CMS.G706.,A;
                braga.CR7.CMS.G706. A 10.0.15.10:5353 90000;
                CR7.CMS.G706. NS braga.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS renato.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS mendes.CR7.CMS.G706. 90000;
                renato.CR7.CMS.G706. A 10.0.16.10:5353 90000,
                mendes.CR7.CMS.G706. A 10.0.13.10:5353 90000;"""));
    }
}
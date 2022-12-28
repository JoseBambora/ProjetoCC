import Cache.*;
import DNSPacket.DNSPacket;
import DNSPacket.Data;
import DNSPacket.Header;
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
        Cache cacheBraga = new Cache();
        cacheBraga.createBD("../DatabasesFiles/Braga.db", "CR7.CMS.G706.","../LogsFiles/CR7.CMS.G706.log",false);
        Cache cacheTopo = new Cache();
        cacheTopo.createBD("../DatabasesFiles/Topo.db","G706.","../LogsFiles/all.log",false);
        Cache cacheCancelo = new Cache();
        cacheCancelo.createBD("../DatabasesFiles/Cancelo.db", "M10.JJM.G706.","../LogsFiles/all.log",false);
        Cache cacheRui = new Cache();
        cacheRui.createBD("../DatabasesFiles/Rui.db","REVERSE.G706.","../LogsFiles/all.log",false);
        Cache bd5 = new Cache();
        bd5.addData(new Value("braga", (byte) 8,"alan",1000), EntryCache.Origin.OTHERS);
        bd5.addData(new Value("alan", (byte) 9,"alan.email.com.",1000), EntryCache.Origin.OTHERS);
        ObjectServer SP= ObjectServer.parseServer("../ConfigurationFiles/configPepe",false);
        List<Boolean> list = new ArrayList<>();
        list.add(SP.getCache().toString().equals("""
                G706. NS pepe.G706. 90000
                G706. NS palhinha.G706. 90000
                CMS.G706. NS william.CMS.G706. 90000
                CMS.G706. NS mario.CMS.G706. 90000
                CMS.G706. NS dalot.CMS.G706. 90000
                JJM.G706. NS felix.JJM.G706. 90000
                JJM.G706. NS otavio.JJM.G706. 90000
                JJM.G706. NS ramos.JJM.G706. 90000
                REVERSE.G706. NS rui.REVERSE.G706. 90000
                pepe.G706. A 10.0.14.11:5353 90000
                palhinha.G706. A 10.0.13.11:5353 90000
                william.CMS.G706. A 10.0.8.12:5353 90000
                mario.CMS.G706. A 10.0.16.13:5353 90000
                dalot.CMS.G706. A 10.0.14.12:5353 90000
                felix.JJM.G706. A 10.0.13.12:5353 90000
                otavio.JJM.G706. A 10.0.15.13:5353 90000
                ramos.JJM.G706. A 10.0.10.13:5353 90000
                rui.REVERSE.G706. A 10.0.10.10:5353 90000
                st1.G706. CNAME pepe.G706. 90000
                st2.G706. CNAME palhinha.G706. 90000
                ss1sd1.JJM.G706. CNAME otavio.JJM.G706. 90000
                ss2sd1.JJM.G706. CNAME ramos.JJM.G706. 90000
                spsd2.CMS.G706. CNAME william.CMS.G706. 90000
                ss1sd2.CMS.G706. CNAME mario.CMS.G706. 90000
                ss2sd2.CMS.G706. CNAME dalot.CMS.G706. 90000
                spsd3.REVERSE.G706. CNAME rui.REVERSE.G706. 90000
                """));
        Tuple<Byte,Data> data5 = cacheBraga.findAnswer("CR7.CMS.G706.",Data.typeOfValueConvert("SOASP"));
        Tuple<Byte,Data> data6 = cacheBraga.findAnswer("renato.CR7.CMS.G706.",Data.typeOfValueConvert("A"));
        Tuple<Byte,Data> data7 = cacheBraga.findAnswer("mail1.CR7.CMS.G706.",Data.typeOfValueConvert("A"));
        Tuple<Byte,Data> data8 = cacheTopo.findAnswer("G706.",Data.typeOfValueConvert("NS"));
        Tuple<Byte,Data> data9 = cacheCancelo.findAnswer("M10.JJM.G706.",Data.typeOfValueConvert("MX"));
        Tuple<Byte,Data> data10 = bd5.findAnswer("braga", (byte) 9);
        Tuple<Byte,Data> data11 = bd5.findAnswer("braga", (byte) 8);
        Tuple<Byte,Data> data12 = cacheBraga.findAnswer("mail1.CR7.CMS.G706.",Data.typeOfValueConvert("NS"));
        Tuple<Byte,Data> data13 = cacheBraga.findAnswer("mail3.CR7.CMS.G706.",Data.typeOfValueConvert("MX"));
        Tuple<Byte,Data> data14 = cacheBraga.findAnswer("CR7.CMS.G706.",Data.typeOfValueConvert("A"));
        list.add(data5.getValue2().toString().equals("""
                CR7.CMS.G706.,SOASP;
                CR7.CMS.G706. SOASP braga.CR7.CMS.G706. 90000;
                CR7.CMS.G706. NS braga.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS renato.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS mendes.CR7.CMS.G706. 90000;
                braga.CR7.CMS.G706. A 10.0.15.10:5353 90000,
                renato.CR7.CMS.G706. A 10.0.16.10:5353 90000,
                mendes.CR7.CMS.G706. A 10.0.13.10:5353 90000;"""));
        list.add(data6.getValue2().toString().equals("""
                renato.CR7.CMS.G706.,A;
                renato.CR7.CMS.G706. A 10.0.16.10:5353 90000;
                CR7.CMS.G706. NS braga.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS renato.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS mendes.CR7.CMS.G706. 90000;
                braga.CR7.CMS.G706. A 10.0.15.10:5353 90000,
                mendes.CR7.CMS.G706. A 10.0.13.10:5353 90000;"""));
        list.add(data7.getValue2().toString().equals("""
                fernando1.CR7.CMS.G706.,A;
                fernando1.CR7.CMS.G706. A 10.0.8.11:5353 90000;
                CR7.CMS.G706. NS braga.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS renato.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS mendes.CR7.CMS.G706. 90000;
                braga.CR7.CMS.G706. A 10.0.15.10:5353 90000,
                renato.CR7.CMS.G706. A 10.0.16.10:5353 90000,
                mendes.CR7.CMS.G706. A 10.0.13.10:5353 90000;"""));
        list.add(data8.getValue2().toString().equals("""
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
                JJM.G706. NS ramos.JJM.G706. 90000,
                REVERSE.G706. NS rui.REVERSE.G706. 90000;
                pepe.G706. A 10.0.14.11:5353 90000,
                palhinha.G706. A 10.0.13.11:5353 90000,
                william.CMS.G706. A 10.0.8.12:5353 90000,
                mario.CMS.G706. A 10.0.16.13:5353 90000,
                dalot.CMS.G706. A 10.0.14.12:5353 90000,
                felix.JJM.G706. A 10.0.13.12:5353 90000,
                otavio.JJM.G706. A 10.0.15.13:5353 90000,
                ramos.JJM.G706. A 10.0.10.13:5353 90000,
                rui.REVERSE.G706. A 10.0.10.10:5353 90000;"""));
        list.add(data9.getValue2().toString().equals("""
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
        list.add(data10.getValue2().toString().equals("alan,MX;\nalan MX alan.email.com. 1000;"));
        list.add(data11.getValue2().toString().equals("braga,CNAME;\nbraga CNAME alan 1000;"));
        list.add(data12.getValue1() == 1);
        list.add(data12.getValue2().toString().equals("fernando1.CR7.CMS.G706.,NS;"));
        list.add(data13.getValue1() == 2);
        list.add(data13.getValue2().toString().equals("""
                mail3.CR7.CMS.G706.,MX;
                CR7.CMS.G706. NS braga.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS renato.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS mendes.CR7.CMS.G706. 90000;
                braga.CR7.CMS.G706. A 10.0.15.10:5353 90000,
                renato.CR7.CMS.G706. A 10.0.16.10:5353 90000,
                mendes.CR7.CMS.G706. A 10.0.13.10:5353 90000;"""));
        list.add(data14.getValue2().toString().equals("""
                CR7.CMS.G706.,A;
                CR7.CMS.G706. NS braga.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS renato.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS mendes.CR7.CMS.G706. 90000;
                braga.CR7.CMS.G706. A 10.0.15.10:5353 90000,
                renato.CR7.CMS.G706. A 10.0.16.10:5353 90000,
                mendes.CR7.CMS.G706. A 10.0.13.10:5353 90000;"""));
        int num = 50;
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
        list.add(cacheTestThreads.size() == num * num);
        Cache bd7 = new Cache();
        bd7.setDominio("CR7.CMS.G706.");
        List<String> lines = Files.readAllLines(Paths.get("../DatabasesFiles/Braga.db"), StandardCharsets.UTF_8);
        lines.forEach(s -> bd7.addData(s, EntryCache.Origin.SP));
        list.add(bd7.findAnswer("braga.CR7.CMS.G706.", (byte) 7).getValue2().toString().equals("""
                braga.CR7.CMS.G706.,A;
                braga.CR7.CMS.G706. A 10.0.15.10:5353 90000;
                CR7.CMS.G706. NS braga.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS renato.CR7.CMS.G706. 90000,
                CR7.CMS.G706. NS mendes.CR7.CMS.G706. 90000;
                renato.CR7.CMS.G706. A 10.0.16.10:5353 90000,
                mendes.CR7.CMS.G706. A 10.0.13.10:5353 90000;"""));
        Value value = new Value("123",(byte) 0, "abc",3,4);
        byte[] b = value.valuesToBytes();
        Value value1 = Value.bytesToValues(b);
        list.add(value1.equals(value));
        Header h = new Header((short) 3, (byte) 7,(byte) 0, (byte) 4,(byte) 4,(byte)4);
        b = h.headerToBytes();
        Header h2 = Header.bytesToHeader(b);
        list.add(h.equals(h2));
        Value[] values = {value,value1,value1,value1};
        Data d = new Data("abc",(byte) 0, values, values, values);
        b = d.dataToBytes();
        Data d1 = Data.bytesToData(b,(byte) 4,(byte) 4,(byte)4);
        list.add(d.equals(d1));
        DNSPacket dnsPacket = new DNSPacket(h,d);
        b = dnsPacket.dnsPacketToBytes(false);
        DNSPacket dnsPacket1 = DNSPacket.bytesToDnsPacket(b);
        b = dnsPacket.dnsPacketToBytes(true);
        DNSPacket dnsPacket2 = DNSPacket.bytesToDnsPacket(b);
        byte[] b2 = dnsPacket2.dnsPacketToBytes();
        DNSPacket dnsPacket3 = DNSPacket.bytesToDnsPacket(b2);
        list.add(dnsPacket1.equals(dnsPacket));
        list.add(dnsPacket2.equals(dnsPacket));
        list.add(dnsPacket3.equals(dnsPacket));
        List<String> l1 = new ArrayList<>();
        l1.add("10.0.8.12:5353");
        l1.add("10.0.16.13:5353");
        l1.add("10.0.14.12:5353");
        List<String> l2 = new ArrayList<>();
        l2.add("10.0.13.12:5353");
        l2.add("10.0.15.13:5353");
        l2.add("10.0.10.13:5353");
        list.add(l1.contains(cacheTopo.findIP("CMS.G706.")));
        list.add(l1.contains(cacheTopo.findIP("CMS.G706.")));
        list.add(l1.contains(cacheTopo.findIP("CMS.G706.")));
        list.add(l1.contains(cacheTopo.findIP("M10.CMS.G706.")));
        list.add(l1.contains(cacheTopo.findIP("M10.CMS.G706.")));
        list.add(l1.contains(cacheTopo.findIP("M10.CMS.G706.")));
        list.add(l2.contains(cacheTopo.findIP("JJM.G706.")));
        list.add(l2.contains(cacheTopo.findIP("JJM.G706.")));
        list.add(l2.contains(cacheTopo.findIP("JJM.G706.")));
        list.add(l2.contains(cacheTopo.findIP("CR7.JJM.G706.")));
        list.add(l2.contains(cacheTopo.findIP("CR7.JJM.G706.")));
        list.add(l2.contains(cacheTopo.findIP("CR7.JJM.G706.")));
        Cache cacheWilliam = new Cache();
        Cache cacheFelix = new Cache();
        Cache cacheSa = new Cache();
        Cache cacheAntonio = new Cache();
        cacheWilliam.createBD("../DatabasesFiles/William.db","CMS.G706.","../LogsFiles/all.log",false);
        cacheFelix.createBD("../DatabasesFiles/Felix.db","CMS.G706.","../LogsFiles/all.log",false);
        cacheSa.createBD("../DatabasesFiles/Sa.db","IN-ADDR.REVERSE.G706.","../LogsFiles/all.log",false);
        cacheAntonio.createBD("../DatabasesFiles/Antonio.db","10.IN-ADDR.REVERSE.G706.","../LogsFiles/all.log",false);
        list.add(cacheWilliam.checkBD("SP"));
        list.add(cacheFelix.checkBD("SP"));
        list.add(cacheSa.checkBD("REVERSET"));
        list.add(cacheRui.checkBD("REVERSET"));
        list.add(cacheAntonio.checkBD("REVERSE"));
        Cache cacheResolver = new Cache();
        DNSPacket query = new DNSPacket((short)0,Header.flagsStrToByte("Q"),"CR7.CMS.G706.",(byte) 6);
        DNSPacket response1 = cacheTopo.findAnswer(query);
        DNSPacket response2 = cacheWilliam.findAnswer(query);
        DNSPacket response3 = cacheBraga.findAnswer(query);
        cacheResolver.addData(response1, EntryCache.Origin.OTHERS);
        cacheResolver.addData(response2, EntryCache.Origin.OTHERS);
        cacheResolver.addData(response3, EntryCache.Origin.OTHERS);
        list.add(cacheTopo.findAnswer("braga.CR7.CMS.G706",(byte) 0).getValue2().toString().equals("""
                braga.CR7.CMS.G706,SOASP;
                CMS.G706. NS william.CMS.G706. 90000,
                CMS.G706. NS mario.CMS.G706. 90000,
                CMS.G706. NS dalot.CMS.G706. 90000;
                william.CMS.G706. A 10.0.8.12:5353 90000,
                mario.CMS.G706. A 10.0.16.13:5353 90000,
                dalot.CMS.G706. A 10.0.14.12:5353 90000;"""));
        list.add(cacheTopo.findAnswer( new DNSPacket((short) 2,Header.flagsStrToByte("Q"),"braga.CR7.CMS.G706",(byte) 0)).toString().equals("""
                2,A,1,0,3,3;braga.CR7.CMS.G706,SOASP;
                CMS.G706. NS william.CMS.G706. 90000,
                CMS.G706. NS mario.CMS.G706. 90000,
                CMS.G706. NS dalot.CMS.G706. 90000;
                william.CMS.G706. A 10.0.8.12:5353 90000,
                mario.CMS.G706. A 10.0.16.13:5353 90000,
                dalot.CMS.G706. A 10.0.14.12:5353 90000;"""));
        list.add(cacheRui.findAnswer(new DNSPacket((short) 2,Header.flagsStrToByte("Q"),"IN-ADDR.REVERSE.G706.",(byte) 6)).toString().equals("""
                2,A,0,1,2,2;IN-ADDR.REVERSE.G706.,NS;
                IN-ADDR.REVERSE.G706. NS sa.IN-ADDR.REVERSE.G706. 90000;
                REVERSE.G706. NS rui.REVERSE.G706. 90000,
                IN-ADDR.REVERSE.G706. NS sa.IN-ADDR.REVERSE.G706. 90000;
                rui.REVERSE.G706. A 10.0.10.10:5353 90000,
                sa.IN-ADDR.REVERSE.G706. A 10.0.13.13:5353 90000;"""));
        list.add(cacheAntonio.findAnswer(new DNSPacket((short) 2,Header.flagsStrToByte("Q"),"10.10.0.10.IN-ADDR.REVERSE.G706.",(byte) 10)).toString().equals("""
                2,A,0,1,1,1;10.10.0.10.IN-ADDR.REVERSE.G706.,PTR;
                10.0.10.10 PTR rui.10.IN-ADDR.REVERSE.G706. 90000;
                10.IN-ADDR.REVERSE.G706. NS antonio.10.IN-ADDR.REVERSE.G706. 90000;
                antonio.10.IN-ADDR.REVERSE.G706. A 10.0.14.13:5353 90000;"""));
        cacheResolver.addData(response3, EntryCache.Origin.OTHERS);
        query = new DNSPacket((short)0,Header.flagsStrToByte("Q"),"CMS.G706.",(byte) 6);
        list.add(cacheResolver.findAnswer(query).toString().equals("""
                0,,0,3,3,3;CMS.G706.,NS;
                CMS.G706. NS william.CMS.G706. 90000,
                CMS.G706. NS mario.CMS.G706. 90000,
                CMS.G706. NS dalot.CMS.G706. 90000;
                CMS.G706. NS william.CMS.G706. 90000,
                CMS.G706. NS mario.CMS.G706. 90000,
                CMS.G706. NS dalot.CMS.G706. 90000;
                william.CMS.G706. A 10.0.8.12:5353 90000,
                mario.CMS.G706. A 10.0.16.13:5353 90000,
                dalot.CMS.G706. A 10.0.14.12:5353 90000;"""));
        list.add(cacheAntonio.findAnswer("13.9.0.10.IN-ADDR.REVERSE.G706.",(byte) 10).toString().equals("""
                (0,13.9.0.10.IN-ADDR.REVERSE.G706.,PTR;
                10.0.9.13 PTR bernardo.M10.JJM.G706. 90000;
                10.IN-ADDR.REVERSE.G706. NS antonio.10.IN-ADDR.REVERSE.G706. 90000;
                antonio.10.IN-ADDR.REVERSE.G706. A 10.0.14.13:5353 90000;)"""));
        list.add(cacheSa.findAnswer("13.9.0.10.IN-ADDR.REVERSE.G706.",(byte) 10).getValue2().toString().equals("""
                13.9.0.10.IN-ADDR.REVERSE.G706.,PTR;
                10.IN-ADDR.REVERSE.G706. NS antonio.10.IN-ADDR.REVERSE.G706. 90000;
                antonio.10.IN-ADDR.REVERSE.G706. A 10.0.14.13:5353 90000;"""));
        list.add(cacheRui.findAnswer("13.9.0.10.IN-ADDR.REVERSE.G706.",(byte) 10).getValue2().toString().equals("""
                13.9.0.10.IN-ADDR.REVERSE.G706.,PTR;
                IN-ADDR.REVERSE.G706. NS sa.IN-ADDR.REVERSE.G706. 90000;
                sa.IN-ADDR.REVERSE.G706. A 10.0.13.13:5353 90000;"""));
        list.add(cacheTopo.findAnswer("13.9.0.10.IN-ADDR.REVERSE.G706.",(byte) 10).getValue2().toString().equals("""
                13.9.0.10.IN-ADDR.REVERSE.G706.,PTR;
                REVERSE.G706. NS rui.REVERSE.G706. 90000;
                rui.REVERSE.G706. A 10.0.10.10:5353 90000;"""));
        list.add(cacheAntonio.findAnswer("10.15.0.10.IN-ADDR.REVERSE.G706.",(byte) 10).toString().equals("""
                (0,10.15.0.10.IN-ADDR.REVERSE.G706.,PTR;
                10.0.15.10 PTR braga.CR7.CMS.G706. 90000;
                10.IN-ADDR.REVERSE.G706. NS antonio.10.IN-ADDR.REVERSE.G706. 90000;
                antonio.10.IN-ADDR.REVERSE.G706. A 10.0.14.13:5353 90000;)"""));
        DNSPacket dnsPacket4 = new DNSPacket((short) 123,Header.flagsStrToByte("Q+R"),"M10.JJM.G706.",(byte) 9);
        dnsPacket4 = cacheCancelo.findAnswer(dnsPacket4);
        cacheResolver.addData(dnsPacket4, EntryCache.Origin.OTHERS);
        list.add(cacheResolver.findAnswer(new DNSPacket((short) 123,Header.flagsStrToByte("Q+R"),"ramos.JJM.G706.", (byte) 6)).isEmpty());
        boolean bool = list.stream().allMatch(bo -> bo);
        if(bool)
            System.out.println("Tudo Certo");
        else
        {
            for(int i = 0; i < list.size();i++)
                if(!list.get(i))
                    System.out.println("Teste " + i + " errado");
        }
    }
}
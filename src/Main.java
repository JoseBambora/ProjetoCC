import java.io.IOException;
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
        List<String> seila = new ArrayList<>();
        seila.add("LogsFiles/Braga.log");
        bd1.createBD("DatabasesFiles/Braga.db", "CR7.CMS.G706.","LogsFiles/CR7.CMS.G706.log");
        Cache bd2 = new Cache();
        bd2.createBD("DatabasesFiles/Topo.db","G706.","LogsFiles/all.log");
        Cache bd3 = new Cache();
        bd3.createBD("DatabasesFiles/Cancelo.db", "M10.JJM.G706.","LogsFiles/all.log");
        Cache bd4 = new Cache();
        bd4.createBD("DatabasesFiles/Rui.db","KB9.REVERSE.G706.","LogsFiles/all.log");
        Cache bd5 = new Cache();
        bd5.addData(new Value("braga", (byte) 8,"alan",1000), EntryCache.Origin.OTHERS);
        bd5.addData(new Value("alan", (byte) 9,"alan.email.com.",1000), EntryCache.Origin.OTHERS);
        ObjectServer SP= ObjectServer.parseServer("ConfigurationFiles/configPepe");
        System.out.println(SP.getCache().toString().equals("G706. NS pepe.G706. 90000\n" +
                "G706. NS palhinha.G706. 90000\n" +
                "CR7.CMS.G706. NS braga.CR7.CMS.G706. 90000\n" +
                "M10.JJM.G706. NS cancelo.M10.JJM.G706. 90000\n" +
                "REVERSE.G706. NS rui.REVERSE.G706. 90000\n" +
                "pepe.G706. A 10.0.14.11:5353 90000\n" +
                "palhinha.G706. A 10.0.13.11:5353 90000\n" +
                "braga.CR7.CMS.G706. A 10.0.15.10:5353 90000\n" +
                "cancelo.M10.JJM.G706. A 10.0.9.11:5353 90000\n" +
                "rui.REVERSE.G706. A 10.0.10.10:5353 90000\n" +
                "st1.G706. CNAME pepe.G706. 90000\n" +
                "st2.G706. CNAME palhinha.G706. 90000\n" +
                "sd1.CR7.CMS.G706. CNAME braga.CR7.CMS.G706. 90000\n" +
                "sd2.M10.JJM.G706. CNAME cancelo.M10.JJM.G706. 90000\n" +
                "sd3.REVERSE.G706. CNAME rui.REVERSE.G706. 90000\n"));
        Tuple<Integer,Data> data5 = bd1.findAnswer("CR7.CMS.G706.",Data.typeOfValueConvert("SOASP"));
        Tuple<Integer,Data> data6 = bd1.findAnswer("renato.CR7.CMS.G706.",Data.typeOfValueConvert("A"));
        Tuple<Integer,Data> data7 = bd1.findAnswer("mail1.CR7.CMS.G706.",Data.typeOfValueConvert("A"));
        Tuple<Integer,Data> data8 = bd2.findAnswer("G706.",Data.typeOfValueConvert("NS"));
        Tuple<Integer,Data> data9 = bd3.findAnswer("M10.JJM.G706.",Data.typeOfValueConvert("MX"));
        Tuple<Integer,Data> data10 = bd5.findAnswer("braga", (byte) 9);
        Tuple<Integer,Data> data11 = bd5.findAnswer("braga", (byte) 8);
        Tuple<Integer,Data> data12 = bd1.findAnswer("mail1.CR7.CMS.G706.",Data.typeOfValueConvert("NS"));
        Tuple<Integer,Data> data13 = bd1.findAnswer("mail3.CR7.CMS.G706.",Data.typeOfValueConvert("MX"));
        System.out.println(data5.getValue2().toString().equals("CR7.CMS.G706.,SOASP;\n" +
                "CR7.CMS.G706. SOASP braga.CR7.CMS.G706. 90000;\n" +
                "CR7.CMS.G706. NS braga.CR7.CMS.G706. 90000,\n" +
                "CR7.CMS.G706. NS renato.CR7.CMS.G706. 90000,\n" +
                "CR7.CMS.G706. NS mendes.CR7.CMS.G706. 90000;\n"  +
                "braga.CR7.CMS.G706. A 10.0.15.10:5353 90000,\n" +
                "renato.CR7.CMS.G706. A 10.0.16.10:5353 90000,\n"+
                "mendes.CR7.CMS.G706. A 10.0.13.10:5353 90000;\n"));
        System.out.println(data6.getValue2().toString().equals("renato.CR7.CMS.G706.,A;\n" +
                "renato.CR7.CMS.G706. A 10.0.16.10:5353 90000;\n" +
                "CR7.CMS.G706. NS braga.CR7.CMS.G706. 90000,\n" +
                "CR7.CMS.G706. NS renato.CR7.CMS.G706. 90000,\n" +
                "CR7.CMS.G706. NS mendes.CR7.CMS.G706. 90000;\n" +
                "braga.CR7.CMS.G706. A 10.0.15.10:5353 90000,\n"+
                "mendes.CR7.CMS.G706. A 10.0.13.10:5353 90000;\n" ));
        System.out.println(data7.getValue2().toString().equals("fernando1.CR7.CMS.G706.,A;\n" +
                "fernando1.CR7.CMS.G706. A 10.0.8.11:5353 90000;\n" +
                "CR7.CMS.G706. NS braga.CR7.CMS.G706. 90000,\n" +
                "CR7.CMS.G706. NS renato.CR7.CMS.G706. 90000,\n" +
                "CR7.CMS.G706. NS mendes.CR7.CMS.G706. 90000;\n"  +
                "braga.CR7.CMS.G706. A 10.0.15.10:5353 90000,\n" +
                "renato.CR7.CMS.G706. A 10.0.16.10:5353 90000,\n"+
                "mendes.CR7.CMS.G706. A 10.0.13.10:5353 90000;\n"));
        System.out.println(data8.getValue2().toString().equals("G706.,NS;\n" +
                "G706. NS pepe.G706. 90000,\n" +
                "G706. NS palhinha.G706. 90000;\n" +
                "G706. NS pepe.G706. 90000,\n" +
                "G706. NS palhinha.G706. 90000,\n" +
                "CR7.CMS.G706. NS braga.CR7.CMS.G706. 90000,\n" +
                "M10.JJM.G706. NS cancelo.M10.JJM.G706. 90000,\n" +
                "REVERSE.G706. NS rui.REVERSE.G706. 90000;\n" +
                "pepe.G706. A 10.0.14.11:5353 90000,\n" +
                "palhinha.G706. A 10.0.13.11:5353 90000,\n" +
                "braga.CR7.CMS.G706. A 10.0.15.10:5353 90000,\n" +
                "cancelo.M10.JJM.G706. A 10.0.9.11:5353 90000,\n" +
                "rui.REVERSE.G706. A 10.0.10.10:5353 90000;\n"));
        System.out.println(data9.getValue2().toString().equals(
                "M10.JJM.G706.,MX;\n" +
                "M10.JJM.G706. MX scaloni1.M10.JJM.G706. 90000,\n" +
                "M10.JJM.G706. MX scaloni2.M10.JJM.G706. 90000;\n" +
                "M10.JJM.G706. NS cancelo.M10.JJM.G706. 90000,\n" +
                "M10.JJM.G706. NS rafael.M10.JJM.G706. 90000,\n" +
                "M10.JJM.G706. NS bruno.M10.JJM.G706. 90000;\n" +
                "cancelo.M10.JJM.G706. A 10.0.9.11:5353 90000,\n" +
                "rafael.M10.JJM.G706. A 10.0.15.11:5353 90000,\n" +
                "bruno.M10.JJM.G706. A 10.0.14.10:5353 90000,\n" +
                "scaloni1.M10.JJM.G706. A 10.0.10.11:5353 90000,\n" +
                "scaloni2.M10.JJM.G706. A 10.0.16.12:5353 90000;\n"));
        System.out.println(data10.getValue2().toString().equals("alan,MX;\nalan MX alan.email.com. 1000;\n"));
        System.out.println(data11.getValue2().toString().equals("braga,CNAME;\nbraga CNAME alan 1000;\n"));
        System.out.println(data12.getValue1() == 1);
        System.out.println(data12.getValue2().toString().equals("fernando1.CR7.CMS.G706.,NS;\n"));
        System.out.println(data13.getValue1() == 2);
        System.out.println(data13.getValue2().toString().equals("mail3.CR7.CMS.G706.,MX;\n" +
                "CR7.CMS.G706. NS braga.CR7.CMS.G706. 90000,\n" +
                "CR7.CMS.G706. NS renato.CR7.CMS.G706. 90000,\n" +
                "CR7.CMS.G706. NS mendes.CR7.CMS.G706. 90000;\n"  +
                "braga.CR7.CMS.G706. A 10.0.15.10:5353 90000,\n" +
                "renato.CR7.CMS.G706. A 10.0.16.10:5353 90000,\n"+
                "mendes.CR7.CMS.G706. A 10.0.13.10:5353 90000;\n"));
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
    }
}
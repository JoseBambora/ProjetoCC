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
    public static void main(String[] args) throws Exception {
        Cache bd = new Cache();
        bd.createBD("DatabasesFiles/Braga.db","CR7.CMS.G706.");
        Cache bd1 = new Cache();
        bd1.createBD("DatabasesFiles/Cancelo.db","M10.JJM.G706.");
        String domQ = "CR7.CMS.G706.";
        find(bd,domQ);
        domQ = "M10.JJM.G706.";
        find(bd1,domQ);
        ObjectServer SP= ObjectServer.parseServer("ConfigurationFiles/configPepe");
        System.out.println(SP.getCache());
        Cache bd5 = new Cache();
        bd5.createBD("DatabasesFiles/Braga.db", "CR7.CMS.G706.");
        System.out.println(bd5.findAnswer("CR7.CMS.G706.",Data.typeOfValueConvert("SOASP")));
        Cache bd2 = new Cache();
        bd2.createBD("DatabasesFiles/Topo.db","G706.");
        System.out.println(bd2.findAnswer("G706.",Data.typeOfValueConvert("NS")));
        Cache bd3 = new Cache();
        bd3.createBD("DatabasesFiles/Cancelo.db", "M10.JJM.G706.");
        System.out.println(bd3.findAnswer("M10.JJM.G706.",Data.typeOfValueConvert("MX")));
        Cache bd4 = new Cache();
        bd4.createBD("DatabasesFiles/Rui.db","KB9.REVERSE.G706.");

     }

}
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Database bd = Database.createBD("DatabasesFiles/Example.db");
        System.out.println(bd);
        System.out.println(bd.getInfo("example.com.",(byte) 0).getValue2());
        System.out.println(bd.getInfo("example.com.",(byte) 1).getValue2());
        System.out.println(bd.getInfo("example.com.",(byte) 2).getValue2());
        System.out.println(bd.getInfo("example.com.",(byte) 3).getValue2());
        System.out.println(bd.getInfo("example.com.",(byte) 4).getValue2());
        System.out.println(bd.getInfo("example.com.",(byte) 5).getValue2());
        System.out.println(bd.getInfo("example.com.",(byte) 6).getValue2());
        System.out.println(bd.getInfo("example.com.",(byte) 7).getValue2());
        System.out.println(bd.getInfo("mail1.example.com.",(byte) 8).getValue2());
        System.out.println(bd.getInfo("example.com.",(byte) 9).getValue2());
    }
}
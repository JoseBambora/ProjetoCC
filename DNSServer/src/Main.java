import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        ServidorBD bd = ServidorBD.createBD("Example.db");
        //System.out.println(bd);
        System.out.println(bd.getInfo("example.com.",(byte) 0));
        System.out.println(bd.getInfo("example.com.",(byte) 1));
        System.out.println(bd.getInfo("example.com.",(byte) 2));
        System.out.println(bd.getInfo("example.com.",(byte) 3));
        System.out.println(bd.getInfo("example.com.",(byte) 4));
        System.out.println(bd.getInfo("example.com.",(byte) 5));
        System.out.println(bd.getInfo("example.com.",(byte) 6));
        System.out.println(bd.getInfo("example.com.",(byte) 7));
        System.out.println(bd.getInfo("mail1.example.com.",(byte) 8));
        System.out.println(bd.getInfo("example.com.",(byte) 9));
    }
}
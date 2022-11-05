import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author José Carvalho
 * Classe que representa a estrutura de um servidor reverse
 * Data criação: 5/11/2022
 * Data última atualização: 5/11/2022
 */
public class DatabaseReverse
{
    /**
     * Indica o nome dum servidor/host que usa o endereço IPv4 indicado no parâmetro
     */
    private final Map<Endereco,Tuple<String,Integer>> PTR;
    DatabaseReverse()
    {
        this.PTR = new HashMap<>();
    }
    /**
     * Adiciona uma associação entre endereço IP e endereço URL.
     * @param endereco Endereço IP
     * @param str Endereço URL.
     */
    public void addPTR(Endereco endereco, String str, Integer TTL) throws Exception
    {
        if(!this.PTR.containsKey(endereco))
            this.PTR.put(endereco,new Tuple<>(str,TTL));
        else
            throw new Exception("Endereço já definido");
    }
    public String getURL(Endereco endereco)
    {
        return this.PTR.get(endereco).getValue1();
    }

    public static DatabaseReverse createReverseDB(String filename) throws IOException
    {
        DatabaseReverse databaseReverse = new DatabaseReverse();
        int l = 0;
        List<String> warnings = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        for(String line : lines)
        {
            String[]words = line.split(" ");
            if(line.length() > 0 && line.charAt(0) != '#'&& words.length > 3)
            {
                if(words[1].equals("PTR"))
                {
                    try
                    {
                        Integer TTL = Integer.parseInt(words[3]);
                        databaseReverse.addPTR(Endereco.stringToIP(words[0]),words[2],TTL);
                    }
                    catch (Exception e)
                    {
                        warnings.add("Erro linha " + l + ": " + e.getMessage());
                    }
                }
                else
                {
                    warnings.add("Erro linha " + l + ": com campo não identificável.");
                }
            }
            else if(words.length < 4)
            {
                warnings.add("Erro linha " + l + ": com campos incompletos.");
            }
            l++;
        }
        System.out.println("Warnings no ficheiro '" + filename + "':");
        for(String warning : warnings)
        {
            System.out.println("- " + warning);
        }
        return databaseReverse;
    }
}
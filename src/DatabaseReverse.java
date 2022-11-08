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
public class DatabaseReverse extends Database
{
    /**
     * Indica o nome dum servidor/host que usa o endereço IPv4 indicado no parâmetro
     */
    private final Map<Endereco,Tuple<String,Integer>> PTR;
    DatabaseReverse()
    {
        super();
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

    /**
     * Método que faz o parsing de um ficheiro para um BD
     * @param filename Nome do ficheiro.
     * @return Base de Dados.
     */
    public static Database createBD(String filename) throws IOException
    {
        DatabaseReverse databaseReverse = new DatabaseReverse();
        int l = 0;
        List<String> warnings = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        Map<String,String> macro = new HashMap<>();
        for(String line : lines)
        {
            String[]words = line.split(" ");
            if(line.length() > 0 && line.charAt(0) != '#'&& words.length > 3)
            {
                if(words[1].equals("DEFAULT"))
                    macro.put(words[0], words[2]);
                else
                {
                    try
                    {
                        String dom = Database.converteDom(words[0], macro);
                        Integer TTL = Database.converteInt(words, macro,3, "'TTL'");
                        switch (words[1])
                        {
                            case "NS"  : databaseReverse.addNS(dom, words[2], Database.converteInt(words,macro,4,"'Prioridade'"), TTL); break;
                            case "PTR" : databaseReverse.addPTR(Endereco.stringToIP(words[0]),words[2],TTL); break;
                            default    : warnings.add("Erro linha " + l + ": Tipo de valor não identificado."); break;
                        }
                    }
                    catch (Exception e)
                    {
                        warnings.add("Erro linha " + l + ": " + e.getMessage());
                    }
                }
            }
            else if(words.length < 4)
            {
                warnings.add("Erro linha " + l + ": Não respeita a sintaxe.");
            }
            l++;
        }
        if(databaseReverse.PTR.isEmpty() || databaseReverse.emptyNS())
        {
            databaseReverse = null;
            warnings.add("Campos em falta. BD reverse não criada");
        }
        System.out.println("Warnings no ficheiro '" + filename + "':");
        for(String warning : warnings)
        {
            System.out.println("- " + warning);
        }
        return databaseReverse;
    }
}

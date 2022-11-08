import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheSP extends Cache
{
    private static final Tuple<Integer,Integer> pri = new Tuple<>(0,255);
    private static final Tuple<Integer,Integer> tem = new Tuple<>(0,Integer.MAX_VALUE);
    private final Map<String,String> filesDataBase;
    public CacheSP(int espaco)
    {
        super(espaco);
        this.filesDataBase = new HashMap<>();
    }

    public void addFile(String dom, String name)
    {
        if(!this.filesDataBase.containsKey(dom))
            this.filesDataBase.put(dom,name);
    }


    /**
     * Método auxiliar ao parsing, de forma a retornar o valor correto do endereço URL.
     * Quando o endereço já termina com o '.' este método não faz nada.
     * @param str Endereço a converter.
     * @param macro Mapeamento que contém as macros guardados. É util para ir buscar a marco "@".
     * @return Endereço URL final.
     */
    private static String converteDom(String str, Map<String,String> macro)
    {
        String res = str;
        if(str.charAt(str.length()-1) != '.')
        {
            if(res.contains("@"))
                res = res.replaceAll("@",macro.get("@"));
            else
                res += "." + macro.get("@");
        }
        return res;
    }

    /**
     * Método que converte uma string em inteiro tendo recurso à macro em caso
     * de a string não ser um número
     * @param words Palavras para converter
     * @param macro Macro em caso de a palavra correspondente não ser um número
     * @param index Indice da string no array words que pretendemos converter para inteiro.
     * @param campo Campo que queremos ir buscar o inteiro. Esta string serve para saber quais os limites.
     * @return Inteiro Convertido
     */
    private static int converteInt(String[] words, Map<String,String> macro, int index, String campo) throws Exception {
        Tuple<Integer,Integer> tuple;
        if(campo.equals("'Prioridade'"))
            tuple = pri;
        else
            tuple = tem;
        int min = tuple.getValue1();
        int max = tuple.getValue2();
        int num;
        if(index < words.length)
        {
            String word = words[index];
            try
            {
                num = Integer.parseInt(word);
            }
            catch (NumberFormatException e)
            {
                String str = macro.get(word);
                if(str != null)
                {
                    try {
                        num = Integer.parseInt(str);
                    }
                    catch (NumberFormatException exp)
                    {
                        throw new Exception("Valor não inteiro para a macro " + word);
                    }
                }
                else
                {
                    throw new Exception("Valor não é inteiro e não está definido nas macros");
                }
            }
            if(num < min || num > max)
                throw new Exception("Número que excede o intervalo estabelecidos para o campo " + campo + ". O intervalo é [" + min + "," + max + "]");

        }
        else
            throw new Exception("Não respeita a sintaxe.");
        return num;
    }
    /**
     * Método que faz o parsing de um ficheiro para um BD
     * @param filename Nome do ficheiro.
     * @return Base de Dados.
     */
    public static Cache createBD(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        Map<String,String> macro = new HashMap<>();
        Cache cacheRes = new CacheSP(1000);
        List<String> warnings = new ArrayList<>();
        Map<String,Byte> aux = new HashMap<>();
        int l = 1;
        try
        {
            aux.put("SOASP", DNSPacket.typeOfValueConvert("SOASP"));
            aux.put("SOAADMIN", DNSPacket.typeOfValueConvert("SOAADMIN"));
            aux.put("SOASERIAL", DNSPacket.typeOfValueConvert("SOASERIAL"));
            aux.put("SOAREFRESH", DNSPacket.typeOfValueConvert("SOAREFRESH"));
            aux.put("SOARETRY", DNSPacket.typeOfValueConvert("SOARETRY"));
            aux.put("SOAEXPIRE", DNSPacket.typeOfValueConvert("SOAEXPIRE"));
            aux.put("NS", DNSPacket.typeOfValueConvert("NS"));
            aux.put("CNAME", DNSPacket.typeOfValueConvert("CNAME"));
            aux.put("MX", DNSPacket.typeOfValueConvert("MX"));
            aux.put("A", DNSPacket.typeOfValueConvert("A"));
            aux.put("PTR", DNSPacket.typeOfValueConvert("PTR"));
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        System.out.println(aux);
        for(String str : lines)
        {
            String[] words = str.split(" ");
            if(str.length() > 0 && str.charAt(0) != '#'&& words.length > 2)
            {
                if(words[1].equals("DEFAULT"))
                    macro.put(words[0], words[2]);
                else if(words.length > 3)
                {
                    try
                    {
                        String dom = converteDom(words[0], macro);
                        int TTL = converteInt(words, macro,3, "'TTL'");
                        switch (words[1])
                        {
                            case "SOASP"      :
                            case "SOAADMIN"   :
                            case "SOASERIAL"  :
                            case "SOAREFRESH" :
                            case "SOARETRY"   :
                            case "SOAEXPIRE"  :
                            case "PTR"        :
                                cacheRes.addData(dom, aux.get(words[1]), new Value(dom,aux.get(words[1]),words[2],TTL), EntryCache.Origin.FILE); break;
                            case "CNAME"      :
                                String name = converteDom(words[2], macro);
                                cacheRes.addData(dom, aux.get(words[1]), new Value(dom,aux.get(words[1]),name,TTL), EntryCache.Origin.FILE); break;
                            case "NS"         :
                            case "MX"         :
                            case "A"          :
                                int prioridade = converteInt(words,macro,4,"'Prioridade'");
                                cacheRes.addData(dom, aux.get(words[1]), new Value(dom,aux.get(words[1]),words[2],TTL,prioridade), EntryCache.Origin.FILE); break;
                            default           : warnings.add("Erro linha " + l + ": Tipo de valor não identificado."); break;
                        }
                    }
                    catch (Exception e)
                    {
                        warnings.add("Erro linha " + l + ": " + e.getMessage());
                    }
                }
                else
                    warnings.add("Erro linha " + l + ": Não respeita a sintaxe.");
            }
            l++;
        }
        // campos em falta
        System.out.println("Warnings no ficheiro '" + filename + "':");
        for(String warning : warnings)
        {
            System.out.println("- " + warning);
        }

        return cacheRes;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

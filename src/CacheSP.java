import java.util.HashMap;
import java.util.Map;

public class CacheSP extends Cache
{
    private Map<String,String> filesDataBase;
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
}

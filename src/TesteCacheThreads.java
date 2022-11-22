import Cache.*;
import DNSPacket.Value;

import java.util.ArrayList;
import java.util.List;

public class TesteCacheThreads  implements Runnable
{
    private Cache cache;
    private String id;
    private byte b;
    private int num;
    TesteCacheThreads(String id,Cache cache,byte b, int num)
    {
        this.id = id;
        this.cache = cache;
        this.b = b;
        this.num = num;
    }

    @Override
    public void run()
    {
        List<String> list = new ArrayList<>();
        for(int i = 0; i < num; i++)
            list.add(id + " " + i);
        for(String str : list)
            this.cache.addData(new Value(str,b,"value: " + str,1000000000), EntryCache.Origin.SP);
        for(String str : list)
            if(this.cache.findAnswer(str,b) == null)
                System.out.println("ERRO A ADICIONAR");
    }
}

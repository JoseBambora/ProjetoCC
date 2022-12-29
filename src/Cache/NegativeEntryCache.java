package Cache;

import DNSPacket.DNSPacket;
import DNSPacket.Value;

import java.util.List;

public class NegativeEntryCache extends EntryCache
{
    private final tipo type;
    public enum tipo { RV, AV, EV }
    public NegativeEntryCache(Value value, Origin origem, tipo tipo)
    {
        super(value, origem);
        this.type = tipo;
    }

    public void addPacket(List<Value> rv, List<Value> av, List<Value> ev)
    {
        switch (type)
        {
            case RV -> rv.add(this.getData());
            case AV -> av.add(this.getData());
            case EV -> ev.add(this.getData());
        }
    }
    public String toString()
    {
        return super.toString() + " " +  this.type;
    }
}

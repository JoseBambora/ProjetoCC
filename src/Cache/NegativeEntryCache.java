package Cache;

import DNSPacket.DNSPacket;
import DNSPacket.Value;

import java.util.List;

public class NegativeEntryCache extends EntryCache
{
    private final byte errorCode;
    private final tipo type;
    public enum tipo { RV, AV, EV }
    public NegativeEntryCache(Value value, Origin origem, byte errorCode, tipo tipo)
    {
        super(value, origem);
        this.type = tipo;
        this.errorCode = errorCode;
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

    public byte getErrorCode() {
        return errorCode;
    }

    public String toString()
    {
        return super.toString() + " " +  this.type;
    }
}

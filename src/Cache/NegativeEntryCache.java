package Cache;

import DNSPacket.Value;

public class NegativeEntryCache extends EntryCache
{
    private final byte errorCode;
    public NegativeEntryCache(Value value, Origin origem, byte errorCode)
    {
        super(value, origem);
        this.errorCode = errorCode;
    }

    public byte getErrorCode() {
        return errorCode;
    }
}

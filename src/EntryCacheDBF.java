public class EntryCacheDBF extends EntryCache
{
    private String dom;
    private String nameFile;
    public EntryCacheDBF(String dom, String nameFile)
    {
        super(dom,(byte)-1,Origin.FILE);
        this.dom = dom;
        this.nameFile = nameFile;
    }

    @Override
    public String toString() {
        return dom + " " + nameFile;
    }
}

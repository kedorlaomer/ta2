import java.util.*;
import java.io.*;

public class HopscotchPersistent implements Map<String, PointerPair>, Constants
{
    private RandomAccessFile file;
    private int length;

    public HopscotchPersistent(RandomAccessFile file) throws IOException
    {
        this.file = file;
        file.seek(0);
        this.length = file.readInt();
    }

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();	
	}

	@Override
	public boolean containsKey(Object key)
	{
		throw new UnsupportedOperationException();	
	}
	@Override
	public boolean containsValue(Object key)
	{
		throw new UnsupportedOperationException();	
	}
	@Override
	public Set<Map.Entry<String,PointerPair>> entrySet ()
	{
		throw new UnsupportedOperationException();	
	}
	@Override
	public boolean equals(Object o)
	{
		throw new UnsupportedOperationException();	
	}
	@Override
	public int hashCode()
	{
		throw new UnsupportedOperationException();	
	}
	@Override	
	public boolean isEmpty()
	{
		throw new UnsupportedOperationException();	
	}
	@Override
	public Set<String> keySet()
	{
		throw new UnsupportedOperationException();	
	}
	@Override
	public PointerPair remove(Object o)
	{
		throw new UnsupportedOperationException();	
	}

	@Override
	public void putAll(Map<? extends String,? extends PointerPair> map)
	{
		throw new UnsupportedOperationException();	
	}

	@Override
	public int size()
	{
		throw new UnsupportedOperationException();	
	}

	@Override
	public Collection<PointerPair> values()
	{
		throw new UnsupportedOperationException();	
	}

    /*
     * Persistente Hopscotch-Tabellen können nicht geändert
     * werden.
     */

    @Override
    public PointerPair put(String key, PointerPair value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public PointerPair get(Object key)
    {
        int hashCode = Math.abs(key.hashCode() % length);

        try
        {
            int pos = (1+hashCode)*(STRING_LENGTH+8);
            file.seek(pos);
            StringBuilder sb = new StringBuilder(STRING_LENGTH);

            for (int i = 0; i < BUCKET_LENGTH; i++)
            {
                /* lies einen STRING_LENGTH langen String */
                sb.delete(0, STRING_LENGTH);
                for (int j = 0; j < STRING_LENGTH; j++)
                {
                    byte b = file.readByte();

                    if (b != 0)
                        sb.append((char) b);
                }

                int p1 = file.readInt();
                int p2 = file.readInt();

                if (sb.toString().equals(key))
                    return new PointerPair(p1, p2);
            }
        }

        catch (IOException exc)
        {
            throw new RuntimeException(exc);
        }

        return null;
    }

    /* triviale Tests */
    public static void main(String[] argv)
    {
        try
        {
            RandomAccessFile file = new RandomAccessFile("test.dat", "r");
            HopscotchPersistent table = new HopscotchPersistent(file);
            PointerPair result = table.get("er");
            System.out.println(result.a + "; " + result.b);
        }

        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }
}

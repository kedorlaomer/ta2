import java.util.*;
import java.io.*;

public class HopscotchResident<K,V> implements Map<K,V>, Constants
{
	private int length;
	private K[] keys;
	private V[] values;

	public HopscotchResident(int length)
	{
		this.length = length;
		keys = (K[])new Object[BUCKET_LENGTH + length];
		values = (V[])new Object[BUCKET_LENGTH + length];
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
	public Set<Map.Entry<K,V>> entrySet ()
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
	public Set<K> keySet()
	{
		throw new UnsupportedOperationException();	
	}
	@Override
	public V remove(Object o)
	{
		throw new UnsupportedOperationException();	
	}

	@Override
	public void putAll(Map<?extends K,?extends V> map)
	{
		throw new UnsupportedOperationException();	
	}

	@Override
	public int size()
	{
		throw new UnsupportedOperationException();	
	}

	@Override
	public Collection<V> values()
	{
		throw new UnsupportedOperationException();	
	}


	/*
	*Die Methoden interessieren uns
	*/
	@Override
	public V put(K key, V value)
    {
        int keyValue = Math.abs(key.hashCode()%length);
        int emptyBucket = -1;

        for (int i = keyValue; i<keyValue+BUCKET_LENGTH; i++)
        {
            if (keys[i] != null && keys[i].equals(key))
            {
                V oldValue = values[i];				
                values[i] = value;	
                return oldValue;
            }

            if (keys[i]==null && emptyBucket < 0 )
                emptyBucket=i;
        }

        if(emptyBucket>=0){
            keys[emptyBucket]=key;
            values[emptyBucket]=value;
            return null;		
        }else{
            throw new RuntimeException();
        }

    }

	@Override
	public V get(Object key)
	{
		int keyValue = Math.abs(((K)key).hashCode()%length);
		for(int i = keyValue; i < keyValue+BUCKET_LENGTH; i++){	
			if(keys[i] != null && keys[i].equals((K)key)){return values[i];}
		}
		return null;
	}

    private static PointerPair pair(int a, int b)
    {
        return new PointerPair(a, b);
    }

    /* trivialer Test; vielleicht ist der Code korrekt */
    public static void main(String[] argv)
    {
        HopscotchResident table = new HopscotchResident<String, PointerPair>(64);

        table.put("ich", pair(12, 16));
        table.put("du", pair(18, 12));
        table.put("er", pair(13451345, 65535));
        table.put("fürchterlich", pair(255, 16711680));

        /*
        table.keys[0] = "ich";
        table.keys[1] = "du";
        table.keys[2] = "er";
        table.keys[3] = "sehr lange Begriffe sind möglich";

        table.values[0] = "i";
        table.values[1] = "you";
        table.values[2] = "he";
        table.values[3] = "very long terms are possible";
        */

        try
        {
            DataOutputStream out = new DataOutputStream (new
                        FileOutputStream(new File("test.dat")));
            table.serialize(out);
            out.close();
        }

        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }

    /*
     * Dateiformat:
     * - Integer length
     * - 28 Nullen (um einen 32-Byte-Record aufzufüllen)
     * - length Records, wobei jeder Record folgendes enthält:
     *   - String s aus STRING_LENGTH Bytes
     *   - Integer p1, Integer p2
     * Der String s wird durch seine Bytes kodiert, gefolgt von
     * Nullen, um die Länge STRING_LENGTH zu erreichen.
     *
     * Dieser Kode funktioniert nur, falls K = java.lang.String
     * und V = ??? ist.
     */

    public void serialize(DataOutputStream out) throws IOException
    {
        out.writeInt(length);

        /* 28 Nullen */
        for (int i = 0; i < 28; i++)
            out.writeByte(0);

        for (int i = 0; i < length+BUCKET_LENGTH; i++)
        {
            String key = (String) keys[i];
            if (key == null) // kein Inhalt → nur Nullen
            {
                /* 
                 * STRING_LENGTH Nullen; dann noch 2⋅4 Nullen
                 * für p1 und p2 
                 */
                for (int j = 0; j < STRING_LENGTH+8; j++)
                    out.writeByte(0);
            }
            else
            {
                /* String s */
                for (int j = 0; j < STRING_LENGTH; j++)
                {
                    char ch = j < key.length()? key.charAt(j) : 0;

                    if (ch > 0x7f)
                        throw new RuntimeException("Non-ASCII character encountered: " + (int) ch);

                    out.writeByte((byte) ch);
                }

                PointerPair p = (PointerPair) values[i];
                int p1 = p.a; int p2 = p.b;
                out.writeInt(p1); out.writeInt(p2);
            }
        }
    }
}

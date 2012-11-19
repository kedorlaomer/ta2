interface Constants
{
	final int BUCKET_LENGTH = 256;
    final int STRING_LENGTH = 120;

    /*
     * Token-Indizes i, für welche i & ABSTRACT_MASK != 0,
     * bedeuten: i taucht im Abstract auf; sonst im Titel
     */

    final int ABSTRACT_MASK = 0x8000000;
}

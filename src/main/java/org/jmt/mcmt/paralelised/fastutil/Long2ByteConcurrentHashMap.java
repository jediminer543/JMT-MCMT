package org.jmt.mcmt.paralelised.fastutil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public class Long2ByteConcurrentHashMap implements Long2ByteMap {

	Map<Long, Byte> backing;
	byte defaultReturn = 0;
	byte nullKey = 0;
	
	public Long2ByteConcurrentHashMap() {
		backing = new ConcurrentHashMap<Long, Byte>();
	}
	
	@Override
	public byte get(long key) {
		Byte out = backing.get(key);
		return (out == null && !backing.containsKey(key)) ? defaultReturn : out;
	}

	@Override
	public boolean isEmpty() {
		return backing.isEmpty();
	}

	@Override
	public boolean containsValue(byte value) {
		return backing.containsValue(value);
	}

	@Override
	public void putAll(Map<? extends Long, ? extends Byte> m) {
		backing.putAll(m);
	}

	@Override
	public int size() {
		return backing.size();
	}
	
	@Override
	public void defaultReturnValue(byte rv) {
		defaultReturn = rv;
	}

	@Override
	public byte defaultReturnValue() {
		return defaultReturn;
	}
	
	@Override
	public ObjectSet<Entry> long2ByteEntrySet() {
		return FastUtilHackUtil.entrySetLongByteWrap(backing);
	}
		
	@Override
	public LongSet keySet() {
		return FastUtilHackUtil.wrapLongSet(backing.keySet());
	}

	@Override
	public ByteCollection values() {
		return FastUtilHackUtil.wrapBytes(backing.values());
	}

	@Override
	public boolean containsKey(long key) {
		return backing.containsKey(key);
	}

	@Override
	public byte put(long key, byte value) {
		return put((Long)key, (Byte)value);
	}
	
	@Override
	public Byte put(Long key, Byte value) {
		Byte out = backing.put(key, value);
		return (out == null && !backing.containsKey(key)) ? defaultReturn : backing.put(key, value);
	}
	
	@Override
	public byte remove(long key) {
		Byte out = backing.remove(key);
		return (out == null && !backing.containsKey(key)) ? defaultReturn : out;
	}

	
}

package net.jadoth.collections;

import static net.jadoth.Jadoth.coalesce;

import java.util.function.Consumer;
import java.util.function.Function;

import net.jadoth.collections.types.X2DMap;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.hash.HashEqualator;
import net.jadoth.hash.JadothHash;
import net.jadoth.util.Composition;
import net.jadoth.util.KeyValue;

public final class EqHash2DMap<K1, K2, V> implements X2DMap<K1, K2, V>, Composition
{
	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	public static final <K1, K2, V> EqHash2DMap<K1, K2, V> New()
	{
		return new EqHash2DMap<>(
			JadothHash.<K1>hashEqualityValue(),
			JadothHash.<K2>hashEqualityValue()
		);
	}

	public static final <K1, K2, V> EqHash2DMap<K1, K2, V> New(
		final HashEqualator<K1> k1HashEqualator,
		final HashEqualator<K2> k2HashEqualator
	)
	{
		return new EqHash2DMap<>(k1HashEqualator, k2HashEqualator);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	final HashEqualator<K1>                      k1HashEqualator;
	final HashEqualator<K2>                      k2HashEqualator;

	final EqHashTable<K1, EqHashTable<K2, V>> tree1;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	private EqHash2DMap(
		final HashEqualator<K1> k1HashEqualator,
		final HashEqualator<K2> k2HashEqualator
	)
	{
		super();
		this.k1HashEqualator = coalesce(k1HashEqualator, JadothHash.<K1>hashEqualityValue());
		this.k2HashEqualator = coalesce(k2HashEqualator, JadothHash.<K2>hashEqualityValue());
		this.tree1           = EqHashTable.<K1, EqHashTable<K2, V>>New(this.k1HashEqualator);
	}

	final EqHashTable<K2, V> createTree2()
	{
		return EqHashTable.<K2, V>New(this.k2HashEqualator);
	}

	final EqHashTable<K2, V> ensureTree2(final K1 key1)
	{
		EqHashTable<K2, V> tree2 = this.tree1.get(key1);
		if(tree2 == null)
		{
			this.tree1.add(key1, tree2 = this.createTree2());
		}
		return tree2;
	}


	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final <P extends Consumer<? super KeyValue<K1, ? extends XGettingMap<K2, V >>>> P iterate(final P procedure)
	{
		return this.tree1.iterate(procedure);
	}

	@Override
	public final EqHashTable<K1, EqHashTable<K2, V>> get()
	{
		return this.tree1;
	}

	@Override
	public final EqHashTable<K2, V> get(final K1 key1)
	{
		return this.tree1.get(key1);
	}

	@Override
	public final V get(final K1 key1, final K2 key2)
	{
		final EqHashTable<K2, V> tree2 = this.tree1.get(key1);
		if(tree2 == null)
		{
			return null;
		}
		return tree2.get(key2);
	}

	@Override
	public final boolean add(final K1 key1, final K2 key2, final V value)
	{
		return this.ensureTree2(key1).add(key2, value);
	}

	@Override
	public final boolean put(final K1 key1, final K2 key2, final V value)
	{
		return this.ensureTree2(key1).put(key2, value);
	}

	@Override
	public final V ensure(final K1 key1, final K2 key2, final Function<? super K2, V> valueSupplier)
	{
		return this.ensureTree2(key1).ensure(key2, valueSupplier);
	}

	@Override
	public final <PK1 extends Consumer<? super K1>> PK1 iterateKeys1(final PK1 procedure)
	{
		return this.tree1.keys().iterate(procedure);
	}

	@Override
	public final <PK2 extends Consumer<? super K2>> PK2 iterateKeys2(final PK2 procedure)
	{
		this.tree1.values().iterate(new Consumer<EqHashTable<K2, V>>()
		{
			@Override
			public void accept(final EqHashTable<K2, V> tree2)
			{
				tree2.keys().iterate(procedure);
			}
		});
		return procedure;
	}

	@Override
	public final <PV extends Consumer<? super V>> PV iterateValues(final PV procedure)
	{
		this.tree1.values().iterate(new Consumer<EqHashTable<K2, V>>()
		{
			@Override
			public void accept(final EqHashTable<K2, V> tree2)
			{
				tree2.values().iterate(procedure);
			}
		});
		return procedure;
	}

	@Override
	public <PIE extends Consumer<? super KeyValue<K2, V>>> PIE iterateInnerEntries(final PIE procedure)
	{
		this.tree1.values().iterate(new Consumer<EqHashTable<K2, V>>()
		{
			@Override
			public void accept(final EqHashTable<K2, V> tree2)
			{
				tree2.iterate(procedure);
			}
		});
		return procedure;
	}

}

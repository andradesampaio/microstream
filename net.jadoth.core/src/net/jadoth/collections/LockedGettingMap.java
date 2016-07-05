package net.jadoth.collections;

import static net.jadoth.Jadoth.notNull;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XImmutableMap;
import net.jadoth.concurrent.Synchronized;
import net.jadoth.functional.BiProcedure;
import net.jadoth.util.Equalator;
import net.jadoth.util.KeyValue;

public final class LockedGettingMap<K, V> implements XGettingMap<K, V>, Synchronized
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static <K, V> LockedGettingMap<K, V> New(final XGettingMap<K, V> subject)
	{
		return New(subject, subject);
	}

	public static <K, V> LockedGettingMap<K, V> New(final XGettingMap<K, V> subject, final Object lock)
	{
		return new LockedGettingMap<>(
			notNull(subject),
			notNull(lock)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final XGettingMap<K, V> subject;
	final Object            lock   ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	LockedGettingMap(final XGettingMap<K, V> subject, final Object lock)
	{
		super();
		this.subject = subject;
		this.lock    = lock   ;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final long maximumCapacity()
	{
		synchronized(this.lock)
		{
			return this.subject.maximumCapacity();
		}
	}

	@Override
	public final <A> A join(final BiProcedure<? super KeyValue<K, V>, ? super A> joiner, final A aggregate)
	{
		synchronized(this.lock)
		{
			return this.subject.join(joiner, aggregate);
		}
	}

	@Override
	public final boolean nullAllowed()
	{
		synchronized(this.lock)
		{
			return this.subject.nullAllowed();
		}
	}

	@Override
	public final boolean hasVolatileElements()
	{
		synchronized(this.lock)
		{
			return this.subject.hasVolatileElements();
		}
	}

	@Override
	public final XGettingMap.Keys<K, V> keys()
	{
		synchronized(this.lock)
		{
			return this.subject.keys();
		}
	}

	@Override
	public final <P extends Consumer<? super KeyValue<K, V>>> P iterate(final P procedure)
	{
		synchronized(this.lock)
		{
			return this.subject.iterate(procedure);
		}
	}

	@Override
	public final XGettingMap.Values<K, V> values()
	{
		synchronized(this.lock)
		{
			return this.subject.values();
		}
	}

	@Override
	public final long remainingCapacity()
	{
		synchronized(this.lock)
		{
			return this.subject.remainingCapacity();
		}
	}

	@Override
	public final XGettingMap.EntriesBridge<K, V> old()
	{
		synchronized(this.lock)
		{
			return this.subject.old();
		}
	}

	@Override
	public final XGettingMap.Bridge<K, V> oldMap()
	{
		synchronized(this.lock)
		{
			return this.subject.oldMap();
		}
	}

	@Override
	public final XGettingMap<K, V> copy()
	{
		synchronized(this.lock)
		{
			return this.subject.copy();
		}
	}

	@Override
	public final <C extends Consumer<? super V>> C query(final XIterable<? extends K> keys, final C collector)
	{
		synchronized(this.lock)
		{
			return this.subject.query(keys, collector);
		}
	}

	@Override
	public final boolean nullKeyAllowed()
	{
		synchronized(this.lock)
		{
			return this.subject.nullKeyAllowed();
		}
	}

	@Override
	public final boolean isFull()
	{
		synchronized(this.lock)
		{
			return this.subject.isFull();
		}
	}

	@Override
	public final boolean nullValuesAllowed()
	{
		synchronized(this.lock)
		{
			return this.subject.nullValuesAllowed();
		}
	}

	@Override
	public final KeyValue<K, V> get()
	{
		synchronized(this.lock)
		{
			return this.subject.get();
		}
	}

	@Override
	public final XGettingMap<K, V> view()
	{
		synchronized(this.lock)
		{
			return this.subject.view();
		}
	}

	@Override
	public final void forEach(final Consumer<? super KeyValue<K, V>> action)
	{
		synchronized(this.lock)
		{
			this.subject.forEach(action);
		}
	}

	@Override
	public final Iterator<KeyValue<K, V>> iterator()
	{
		synchronized(this.lock)
		{
			return this.subject.iterator();
		}
	}

	@Override
	public final Object[] toArray()
	{
		synchronized(this.lock)
		{
			return this.subject.toArray();
		}
	}

	@Override
	public final KeyValue<K, V>[] toArray(final Class<KeyValue<K, V>> type)
	{
		synchronized(this.lock)
		{
			return this.subject.toArray(type);
		}
	}

	@Override
	public final Spliterator<KeyValue<K, V>> spliterator()
	{
		synchronized(this.lock)
		{
			return this.subject.spliterator();
		}
	}

	@Override
	public final long size()
	{
		synchronized(this.lock)
		{
			return this.subject.size();
		}
	}

	@Override
	public final boolean isEmpty()
	{
		synchronized(this.lock)
		{
			return this.subject.isEmpty();
		}
	}

	@Override
	public final Equalator<? super KeyValue<K, V>> equality()
	{
		synchronized(this.lock)
		{
			return this.subject.equality();
		}
	}

	@Override
	public final boolean equals(
		final XGettingCollection<? extends KeyValue<K, V>> samples,
		final Equalator<? super KeyValue<K, V>> equalator
	)
	{
		synchronized(this.lock)
		{
			return this.subject.equals(samples, equalator);
		}
	}

	@Override
	public final boolean equalsContent(
		final XGettingCollection<? extends KeyValue<K, V>> samples,
		final Equalator<? super KeyValue<K, V>> equalator
	)
	{
		synchronized(this.lock)
		{
			return this.subject.equalsContent(samples, equalator);
		}
	}


	@Override
	public final V get(final K key)
	{
		synchronized(this.lock)
		{
			return this.subject.get(key);
		}
	}

	@Override
	public final V searchValue(final Predicate<? super K> keyPredicate)
	{
		synchronized(this.lock)
		{
			return this.subject.searchValue(keyPredicate);
		}
	}

	@Override
	public final XImmutableMap<K, V> immure()
	{
		synchronized(this.lock)
		{
			return this.subject.immure();
		}
	}

	@Override
	public final boolean nullContained()
	{
		synchronized(this.lock)
		{
			return this.subject.nullContained();
		}
	}

	@Override
	public final boolean containsId(final KeyValue<K, V> element)
	{
		synchronized(this.lock)
		{
			return this.subject.containsId(element);
		}
	}

	@Override
	public final boolean contains(final KeyValue<K, V> element)
	{
		synchronized(this.lock)
		{
			return this.subject.contains(element);
		}
	}

	@Override
	public final boolean containsSearched(final Predicate<? super KeyValue<K, V>> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.containsSearched(predicate);
		}
	}

	@Override
	public final boolean containsAll(final XGettingCollection<? extends KeyValue<K, V>> elements)
	{
		synchronized(this.lock)
		{
			return this.subject.containsAll(elements);
		}
	}

	@Override
	public final boolean applies(final Predicate<? super KeyValue<K, V>> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.applies(predicate);
		}
	}

	@Override
	public final long count(final KeyValue<K, V> element)
	{
		synchronized(this.lock)
		{
			return this.subject.count(element);
		}
	}

	@Override
	public final long countBy(final Predicate<? super KeyValue<K, V>> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.countBy(predicate);
		}
	}

	@Override
	public final boolean hasDistinctValues()
	{
		synchronized(this.lock)
		{
			return this.subject.hasDistinctValues();
		}
	}

	@Override
	public final boolean hasDistinctValues(final Equalator<? super KeyValue<K, V>> equalator)
	{
		synchronized(this.lock)
		{
			return this.subject.hasDistinctValues(equalator);
		}
	}

	@Override
	public final KeyValue<K, V> search(final Predicate<? super KeyValue<K, V>> predicate)
	{
		synchronized(this.lock)
		{
			return this.subject.search(predicate);
		}
	}

	@Override
	public final KeyValue<K, V> seek(final KeyValue<K, V> sample)
	{
		synchronized(this.lock)
		{
			return this.subject.seek(sample);
		}
	}

	@Override
	public final KeyValue<K, V> max(final Comparator<? super KeyValue<K, V>> comparator)
	{
		synchronized(this.lock)
		{
			return this.subject.max(comparator);
		}
	}

	@Override
	public final KeyValue<K, V> min(final Comparator<? super KeyValue<K, V>> comparator)
	{
		synchronized(this.lock)
		{
			return this.subject.min(comparator);
		}
	}

	@Override
	public final <T extends Consumer<? super KeyValue<K, V>>> T distinct(final T target)
	{
		synchronized(this.lock)
		{
			return this.subject.distinct(target);
		}
	}

	@Override
	public final <T extends Consumer<? super KeyValue<K, V>>> T distinct(
		final T target,
		final Equalator<? super KeyValue<K, V>> equalator
	)
	{
		synchronized(this.lock)
		{
			return this.subject.distinct(target, equalator);
		}
	}

	@Override
	public final <T extends Consumer<? super KeyValue<K, V>>> T copyTo(final T target)
	{
		synchronized(this.lock)
		{
			return this.subject.copyTo(target);
		}
	}

	@Override
	public final <T extends Consumer<? super KeyValue<K, V>>> T filterTo(
		final T target,
		final Predicate<? super KeyValue<K, V>> predicate
	)
	{
		synchronized(this.lock)
		{
			return this.subject.filterTo(target, predicate);
		}
	}

	@Override
	public final <T> T[] copyTo(final T[] target, final int targetOffset)
	{
		synchronized(this.lock)
		{
			return this.subject.copyTo(target, targetOffset);
		}
	}

	@Override
	public final <T extends Consumer<? super KeyValue<K, V>>> T union(
		final XGettingCollection<? extends KeyValue<K, V>> other,
		final Equalator<? super KeyValue<K, V>> equalator,
		final T target)
	{
		synchronized(this.lock)
		{
			return this.subject.union(other, equalator, target);
		}
	}

	@Override
	public final <T extends Consumer<? super KeyValue<K, V>>> T intersect(
		final XGettingCollection<? extends KeyValue<K, V>> other,
		final Equalator<? super KeyValue<K, V>> equalator,
		final T target
	)
	{
		synchronized(this.lock)
		{
			return this.subject.intersect(other, equalator, target);
		}
	}

	@Override
	public final <T extends Consumer<? super KeyValue<K, V>>> T except(
		final XGettingCollection<? extends KeyValue<K, V>> other,
		final Equalator<? super KeyValue<K, V>> equalator,
		final T target
	)
	{
		synchronized(this.lock)
		{
			return this.subject.except(other, equalator, target);
		}
	}

	@Deprecated
	@Override
	public final boolean equals(final Object o)
	{
		synchronized(this.lock)
		{
			return this.subject.equals(o);
		}
	}

	@Deprecated
	@Override
	public final int hashCode()
	{
		synchronized(this.lock)
		{
			return this.subject.hashCode();
		}
	}

}

package net.jadoth.collections;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.Jadoth;
import net.jadoth.collections.old.AbstractBridgeXSet;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XImmutableSet;
import net.jadoth.collections.types.XSet;
import net.jadoth.concurrent.Synchronized;
import net.jadoth.functional.BiProcedure;
import net.jadoth.util.Equalator;
import net.jadoth.util.iterables.SynchronizedIterator;


public final class MutexSet<E> implements XSet<E>, Synchronized
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final XSet<E> subject;
	private final Object mutex;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public MutexSet(final XSet<E> set)
	{
		super();
		this.subject = set;
		this.mutex = set;
	}

	public MutexSet(final XSet<E> set, final Object mutex)
	{
		super();
		this.subject = set;
		this.mutex = mutex;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public E get()
	{
		return this.subject.get();
	}

	@Override
	public Equalator<? super E> equality()
	{
		return this.subject.equality();
	}



	///////////////////////////////////////////////////////////////////////////
	//   add methods    //
	/////////////////////

	@Override
	public void accept(final E e)
	{
		synchronized(this.mutex)
		{
			this.subject.accept(e);
		}
	}

	@Override
	public boolean add(final E e)
	{
		synchronized(this.mutex)
		{
			return this.subject.add(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public MutexSet<E> addAll(final E... elements)
	{
		synchronized(this.mutex)
		{
			this.subject.addAll(elements);
			return this;
		}
	}

	@Override
	public MutexSet<E> addAll(final E[] elements, final int offset, final int length)
	{
		synchronized(this.mutex)
		{
			this.subject.addAll(elements, offset, length);
			return this;
		}
	}

	@Override
	public MutexSet<E> addAll(final XGettingCollection<? extends E> elements)
	{
		synchronized(this.mutex)
		{
			this.subject.addAll(elements);
			return this;
		}
	}

	@Override
	public boolean nullAdd()
	{
		synchronized(this.mutex)
		{
			return this.subject.nullAdd();
		}
	}



	///////////////////////////////////////////////////////////////////////////
	//   put methods    //
	/////////////////////

	@Override
	public boolean put(final E e)
	{
		synchronized(this.mutex)
		{
			return this.subject.put(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public MutexSet<E> putAll(final E... elements)
	{
		synchronized(this.mutex)
		{
			this.subject.putAll(elements);
			return this;
		}
	}

	@Override
	public MutexSet<E> putAll(final E[] elements, final int offset, final int length)
	{
		synchronized(this.mutex)
		{
			this.subject.putAll(elements, offset, length);
			return this;
		}
	}

	@Override
	public MutexSet<E> putAll(final XGettingCollection<? extends E> elements)
	{
		synchronized(this.mutex)
		{
			this.subject.putAll(elements);
			return this;
		}
	}

	@Override
	public boolean nullPut()
	{
		synchronized(this.mutex)
		{
			return this.subject.nullPut();
		}
	}

	@Override
	public E putGet(final E e)
	{
		synchronized(this.mutex)
		{
			return this.subject.putGet(e);
		}
	}

	@Override
	public E addGet(final E e)
	{
		synchronized(this.mutex)
		{
			return this.subject.addGet(e);
		}
	}

	@Override
	public E replace(final E e)
	{
		synchronized(this.mutex)
		{
			return this.subject.replace(e);
		}
	}

	@Override
	public boolean containsSearched(final Predicate<? super E> predicate)
	{
		synchronized(this.mutex)
		{
			return this.subject.containsSearched(predicate);
		}
	}

	@Override
	public boolean applies(final Predicate<? super E> predicate)
	{
		synchronized(this.mutex)
		{
			return this.subject.applies(predicate);
		}
	}

	@Override
	public void clear()
	{
		synchronized(this.mutex)
		{
			this.subject.clear();
		}
	}

	@Override
	public long consolidate()
	{
		synchronized(this.mutex)
		{
			return this.subject.consolidate();
		}
	}

	@Override
	public boolean contains(final E element)
	{
		synchronized(this.mutex)
		{
			return this.subject.contains(element);
		}
	}

	@Override
	public boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		synchronized(this.mutex)
		{
			return this.subject.containsAll(elements);
		}
	}

	@Override
	public boolean containsId(final E element)
	{
		synchronized(this.mutex)
		{
			return this.subject.containsId(element);
		}
	}

	@Override
	public boolean nullAllowed()
	{
		synchronized(this.mutex)
		{
			return this.subject.nullAllowed();
		}
	}

	@Override
	public boolean nullContained()
	{
		synchronized(this.mutex)
		{
			return this.subject.nullContained();
		}
	}

	@Override
	public <C extends Consumer<? super E>> C filterTo(final C target, final Predicate<? super E> predicate)
	{
		synchronized(this.mutex)
		{
			return this.subject.filterTo(target, predicate);
		}
	}

	@Override
	public <T> T[] copyTo(final T[] target, final int offset)
	{
		synchronized(this.mutex)
		{
			return this.subject.copyTo(target, offset);
		}
	}

	@Override
	public <C extends Consumer<? super E>> C copyTo(final C target)
	{
		synchronized(this.mutex)
		{
			return this.subject.copyTo(target);
		}
	}

	@Override
	public long count(final E element)
	{
		synchronized(this.mutex)
		{
			return this.subject.count(element);
		}
	}

	@Override
	public long countBy(final Predicate<? super E> predicate)
	{
		synchronized(this.mutex)
		{
			return this.subject.countBy(predicate);
		}
	}

	@Override
	public <C extends Consumer<? super E>> C distinct(final C target, final Equalator<? super E> equalator)
	{
		synchronized(this.mutex)
		{
			return this.subject.distinct(target, equalator);
		}
	}

	@Override
	public <C extends Consumer<? super E>> C distinct(final C target)
	{
		synchronized(this.mutex)
		{
			return this.subject.distinct(target);
		}
	}

	@Override
	public MutexSet<E> ensureFreeCapacity(final long minimalFreeCapacity)
	{
		synchronized(this.mutex)
		{
			this.subject.ensureFreeCapacity(minimalFreeCapacity);
			return this;
		}
	}

	@Override
	public MutexSet<E> ensureCapacity(final long minimalCapacity)
	{
		synchronized(this.mutex)
		{
			this.subject.ensureCapacity(minimalCapacity);
			return this;
		}
	}

	@Deprecated
	@Override
	public boolean equals(final Object o)
	{
		synchronized(this.mutex)
		{
			return this.subject.equals(o);
		}
	}

	@Override
	public boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		synchronized(this.mutex)
		{
			return this.subject.equals(samples, equalator);
		}
	}

	@Override
	public boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		synchronized(this.mutex)
		{
			return this.subject.equalsContent(samples, equalator);
		}
	}

	@Override
	public <C extends Consumer<? super E>> C except(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		synchronized(this.mutex)
		{
			return this.subject.except(other, equalator, target);
		}
	}

	@Override
	public final <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		synchronized(this.mutex)
		{
			return this.subject.iterate(procedure);
		}
	}

	@Override
	public final <A> A join(final BiProcedure<? super E, ? super A> joiner, final A aggregate)
	{
		synchronized(this.mutex)
		{
			return this.subject.join(joiner, aggregate);
		}
	}

	@Deprecated
	@Override
	public int hashCode()
	{
		synchronized(this.mutex)
		{
			return this.subject.hashCode();
		}
	}

	@Override
	public boolean hasDistinctValues(final Equalator<? super E> equalator)
	{
		synchronized(this.mutex)
		{
			return this.subject.hasDistinctValues(equalator);
		}
	}

	@Override
	public boolean hasDistinctValues()
	{
		synchronized(this.mutex)
		{
			return this.subject.hasDistinctValues();
		}
	}

	@Override
	public boolean hasVolatileElements()
	{
		synchronized(this.mutex)
		{
			return this.subject.hasVolatileElements();
		}
	}

	@Override
	public <C extends Consumer<? super E>> C intersect(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		synchronized(this.mutex)
		{
			return this.subject.intersect(other, equalator, target);
		}
	}

	@Override
	public boolean isEmpty()
	{
		synchronized(this.mutex)
		{
			return this.subject.isEmpty();
		}
	}

	@Override
	public Iterator<E> iterator()
	{
		synchronized(this.mutex)
		{
			return new SynchronizedIterator<>(this.subject.iterator());
		}
	}

	@Override
	public E max(final Comparator<? super E> comparator)
	{
		synchronized(this.mutex)
		{
			return this.subject.max(comparator);
		}
	}

	@Override
	public E min(final Comparator<? super E> comparator)
	{
		synchronized(this.mutex)
		{
			return this.subject.min(comparator);
		}
	}

	@Override
	public <C extends Consumer<? super E>> C moveTo(final C target, final Predicate<? super E> predicate)
	{
		synchronized(this.mutex)
		{
			return this.subject.moveTo(target, predicate);
		}
	}

	@Override
	public final <P extends Consumer<? super E>> P process(final P procedure)
	{
		synchronized(this.mutex)
		{
			return this.subject.process(procedure);
		}
	}

	@Override
	public E fetch()
	{
		synchronized(this.mutex)
		{
			return this.subject.fetch();
		}
	}

	@Override
	public E pinch()
	{
		synchronized(this.mutex)
		{
			return this.subject.pinch();
		}
	}

	@Override
	public long removeBy(final Predicate<? super E> predicate)
	{
		synchronized(this.mutex)
		{
			return this.subject.removeBy(predicate);
		}
	}

	@Override
	public E retrieve(final E element)
	{
		synchronized(this.mutex)
		{
			return this.subject.retrieve(element);
		}
	}

	@Override
	public E retrieveBy(final Predicate<? super E> predicate)
	{
		synchronized(this.mutex)
		{
			return this.subject.retrieveBy(predicate);
		}
	}

	@Override
	public boolean removeOne(final E element)
	{
		synchronized(this.mutex)
		{
			return this.subject.removeOne(element);
		}
	}

	@Override
	public long remove(final E element)
	{
		synchronized(this.mutex)
		{
			return this.subject.remove(element);
		}
	}

	@Override
	public long removeAll(final XGettingCollection<? extends E> elements)
	{
		synchronized(this.mutex)
		{
			return this.subject.removeAll(elements);
		}
	}

	@Override
	public long removeDuplicates(final Equalator<? super E> equalator)
	{
		synchronized(this.mutex)
		{
			return this.subject.removeDuplicates(equalator);
		}
	}

	@Override
	public long removeDuplicates()
	{
		synchronized(this.mutex)
		{
			return this.subject.removeDuplicates();
		}
	}

	@Override
	public long nullRemove()
	{
		synchronized(this.mutex)
		{
			return this.subject.nullRemove();
		}
	}

	@Override
	public long retainAll(final XGettingCollection<? extends E> elements)
	{
		synchronized(this.mutex)
		{
			return this.subject.retainAll(elements);
		}
	}

	@Override
	public E seek(final E sample)
	{
		synchronized(this.mutex)
		{
			return this.subject.seek(sample);
		}
	}

	@Override
	public E search(final Predicate<? super E> predicate)
	{
		synchronized(this.mutex)
		{
			return this.subject.search(predicate);
		}
	}

	@Override
	public long optimize()
	{
		synchronized(this.mutex)
		{
			return this.subject.optimize();
		}
	}

	@Override
	public long size()
	{
		synchronized(this.mutex)
		{
			return Jadoth.to_int(this.subject.size());
		}
	}

	@Override
	public Object[] toArray()
	{
		synchronized(this.mutex)
		{
			return this.subject.toArray();
		}
	}

	@Override
	public E[] toArray(final Class<E> type)
	{
		synchronized(this.mutex)
		{
			return this.subject.toArray(type);
		}
	}

	@Override
	public void truncate()
	{
		synchronized(this.mutex)
		{
			this.subject.truncate();
		}
	}

	@Override
	public <C extends Consumer<? super E>> C union(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		synchronized(this.mutex)
		{
			return this.subject.union(other, equalator, target);
		}
	}

	@Override
	public long currentCapacity()
	{
		synchronized(this.mutex)
		{
			return this.subject.currentCapacity();
		}
	}

	@Override
	public long maximumCapacity()
	{
		synchronized(this.mutex)
		{
			return this.subject.maximumCapacity();
		}
	}

	@Override
	public boolean isFull()
	{
		synchronized(this.mutex)
		{
			return Jadoth.to_int(this.subject.size()) >= this.subject.maximumCapacity();
		}
	}

	@Override
	public long remainingCapacity()
	{
		synchronized(this.mutex)
		{
			return this.subject.remainingCapacity();
		}
	}

	@Override
	public MutexSet<E> copy()
	{
		synchronized(this.mutex)
		{
			return new MutexSet<>(this.subject.copy(), new Object());
		}
	}

	@Override
	public XImmutableSet<E> immure()
	{
		synchronized(this.mutex)
		{
			return this.subject.immure();
		}
	}

	@Override
	public SetView<E> view()
	{
		return new SetView<>(this);
	}

	@Override
	public OldMutexSet<E> old()
	{
		return new OldMutexSet<>(this);
	}

	public static final class OldMutexSet<E> extends AbstractBridgeXSet<E>
	{
		OldMutexSet(final MutexSet<E> set)
		{
			super(set);
		}

		@Override
		public MutexSet<E> parent()
		{
			return (MutexSet<E>)super.parent();
		}

	}

}

package net.jadoth.collections;

import static net.jadoth.Jadoth.notNull;

import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import net.jadoth.Jadoth;
import net.jadoth.collections.old.OldList;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingList;
import net.jadoth.collections.types.XImmutableList;
import net.jadoth.collections.types.XList;
import net.jadoth.functional.BiProcedure;
import net.jadoth.functional.IndexProcedure;
import net.jadoth.functional.JadothEqualators;
import net.jadoth.util.Equalator;

public class MappedList<E, S> implements XGettingList<E>
{
	/* (12.07.2012 TM)FIXME: complete MappedList implementation
	 * See all "FIX-ME"s
	 */

	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	final XGettingList<S> subject;
	final Function<S, E> mapper;
	final Equalator<? super E> equality;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public MappedList(final XGettingList<S> subject, final Function<S, E> mapper, final Equalator<? super E> equality)
	{
		super();
		this.subject  = notNull(subject);
		this.mapper   = notNull(mapper);
		this.equality = notNull(equality);
	}

	public MappedList(final XList<S> subject, final Function<S, E> mapper)
	{
		this(subject, mapper, JadothEqualators.identity());
	}




	private Comparator<S> mapComparator(final Comparator<? super E> comparator)
	{
		return new Comparator<S>()
		{
			@Override
			public int compare(final S o1, final S o2)
			{
				return comparator.compare(MappedList.this.mapper.apply(o1), MappedList.this.mapper.apply(o2));
			}
		};
	}

	private Predicate<S> mapPredicate(final Predicate<? super E> predicate)
	{
		return new Predicate<S>()
		{
			@Override
			public boolean test(final S e)
			{
				return predicate.test(MappedList.this.mapper.apply(e));
			}
		};
	}

	private Predicate<S> mapIsEqual(final E element)
	{
		return new Predicate<S>()
		{
			@Override
			public boolean test(final S e)
			{
				return MappedList.this.equality.equal(element, MappedList.this.mapper.apply(e));
			}
		};
	}




	@Override
	public E at(final long index)
	{
		return this.mapper.apply(this.subject.at(index));
	}

	@Override
	public E get()
	{
		return this.mapper.apply(this.subject.get());
	}

	@Override
	public E first()
	{
		return this.mapper.apply(this.subject.first());
	}

	@Override
	public E last()
	{
		return this.mapper.apply(this.subject.last());
	}

	@Override
	public E poll()
	{
		return this.mapper.apply(this.subject.poll());
	}

	@Override
	public E peek()
	{
		return this.mapper.apply(this.subject.peek());
	}

	@Override
	public long maxIndex(final Comparator<? super E> comparator)
	{
		return this.subject.maxIndex(this.mapComparator(comparator));
	}

	@Override
	public long minIndex(final Comparator<? super E> comparator)
	{
		return this.subject.minIndex(this.mapComparator(comparator));
	}

	@Override
	public long indexOf(final E element)
	{
		return this.subject.indexBy(this.mapIsEqual(element));
	}

	@Override
	public long indexBy(final Predicate<? super E> predicate)
	{
		return this.subject.indexBy(this.mapPredicate(predicate));
	}

	@Override
	public long lastIndexOf(final E element)
	{
		return this.subject.lastIndexBy(this.mapIsEqual(element));
	}

	@Override
	public long lastIndexBy(final Predicate<? super E> predicate)
	{
		return this.subject.lastIndexBy(this.mapPredicate(predicate));
	}

	@Override
	public long scan(final Predicate<? super E> predicate)
	{
		return this.subject.scan(this.mapPredicate(predicate));
	}

	@Override
	public boolean isSorted(final Comparator<? super E> comparator)
	{
		return this.subject.isSorted(this.mapComparator(comparator));
	}

	@Override
	public <T extends Consumer<? super E>> T copySelection(final T target, final long... indices)
	{
		final int length = indices.length;
		final int size = Jadoth.to_int(this.subject.size());

		// validate all indices before copying the first element
		for(int i = 0; i < length; i++)
		{
			if(indices[i] < 0 || indices[i] >= size)
			{
				throw new IndexExceededException(size, indices[i]);
			}
		}

		// actual copying
		final XGettingList<S> subject = this.subject;
		final Function<S, E>  mapper  = this.mapper;
		for(int i = 0; i < length; i++)
		{
			// (19.11.2011)NOTE: single element access can get pretty inefficent, but well...
			target.accept(mapper.apply(subject.at(indices[i])));
		}

		return target;
	}

	@Override
	public <T> T[] copyTo(final T[] target, final int targetOffset, final long offset, final int length)
	{
		// (19.11.2011)NOTE: range validation for subject is done in ranged execution method
		AbstractArrayStorage.validateRange0toUpperBound(target.length, targetOffset, length);

		XUtilsCollection.rngIterate(this.subject, offset, length, new Consumer<S>()
		{
			int toff = targetOffset;
			@SuppressWarnings("unchecked")
			@Override
			public void accept(final S e)
			{
				target[this.toff++] = (T)MappedList.this.mapper.apply(e);
			}
		});
		return target;
	}

	@Override
	public Iterator<E> iterator()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#iterator
	}

	@Override
	public Object[] toArray()
	{
		return this.copyTo(new Object[Jadoth.to_int(this.subject.size())], 0);
	}

	@Override
	public boolean hasVolatileElements()
	{
		return this.subject.hasVolatileElements();
	}

	@Override
	public long size()
	{
		return Jadoth.to_int(this.subject.size());
	}

	@Override
	public boolean isEmpty()
	{
		return this.subject.isEmpty();
	}

	@Override
	public Equalator<? super E> equality()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#equality
	}

	@Override
	public E[] toArray(final Class<E> type)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#toArray
	}

	@Override
	public boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#equals
	}

	@Override
	public boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#equalsContent
	}

	@Override
	public boolean nullContained()
	{
		// (19.11.2011)NOTE: would actually have to iterative over every element and see if function returns null o_0
		return this.subject.nullContained();
	}

	@Override
	public boolean containsId(final E element)
	{
		return this.subject.containsSearched(new Predicate<S>()
		{
			@Override
			public boolean test(final S e)
			{
				return MappedList.this.mapper.apply(e) == element;
			}
		});
	}

	@Override
	public boolean contains(final E element)
	{
		return this.subject.containsSearched(this.mapIsEqual(element));
	}

	@Override
	public boolean containsSearched(final Predicate<? super E> predicate)
	{
		return this.subject.containsSearched(this.mapPredicate(predicate));
	}

	@Override
	public boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#containsAll
	}

//	@Override
//	public boolean containsAll(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
//	{
//		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#containsAll
//	}

	@Override
	public boolean applies(final Predicate<? super E> predicate)
	{
		return this.subject.applies(this.mapPredicate(predicate));
	}

	@Override
	public long count(final E element)
	{
		return this.subject.countBy(this.mapIsEqual(element));
	}

	@Override
	public long countBy(final Predicate<? super E> predicate)
	{
		return this.subject.countBy(this.mapPredicate(predicate));
	}

	@Override
	public boolean hasDistinctValues()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#hasDistinctValues
	}

	@Override
	public boolean hasDistinctValues(final Equalator<? super E> equalator)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#hasDistinctValues
	}

	@Override
	public E search(final Predicate<? super E> predicate)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#search
	}

	@Override
	public E seek(final E sample)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#seek
	}

	@Override
	public E max(final Comparator<? super E> comparator)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#max
	}

	@Override
	public E min(final Comparator<? super E> comparator)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#min
	}

	@Override
	public <T extends Consumer<? super E>> T distinct(final T target)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#distinct
	}

	@Override
	public <T extends Consumer<? super E>> T distinct(final T target, final Equalator<? super E> equalator)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#distinct
	}

	@Override
	public <T extends Consumer<? super E>> T copyTo(final T target)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#copyTo
	}

	@Override
	public <T extends Consumer<? super E>> T filterTo(final T target, final Predicate<? super E> predicate)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#copyTo
	}

	@Override
	public <T> T[] copyTo(final T[] target, final int targetOffset)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#copyTo
	}

	@Override
	public <T extends Consumer<? super E>> T union(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#union
	}

	@Override
	public <T extends Consumer<? super E>> T intersect(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#intersect
	}

	@Override
	public <T extends Consumer<? super E>> T except(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#except
	}

	@Override
	public boolean nullAllowed()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME ExtendedCollection<E>#nullAllowed
	}

	@Override
	public long maximumCapacity()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME CapacityCarrying#maximumCapacity
	}

	@Override
	public long remainingCapacity()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME CapacityCarrying#freeCapacity
	}

	@Override
	public boolean isFull()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME CapacityCarrying#isFull
	}

	@Override
	public XImmutableList<E> immure()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingList<E>#immure
	}

	@Override
	public ListIterator<E> listIterator()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingList<E>#listIterator
	}

	@Override
	public ListIterator<E> listIterator(final long index)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingList<E>#listIterator
	}

	@Override
	public OldList<E> old()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingList<E>#old
	}

	@Override
	public XGettingList<E> copy()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingList<E>#copy
	}

	@Override
	public XGettingList<E> toReversed()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingList<E>#toReversed
	}

	@Override
	public final <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingList<E>#iterate
	}

	@Override
	public final <A> A join(final BiProcedure<? super E, ? super A> joiner, final A aggregate)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingList<E>#join
	}

	@Override
	public final <P extends IndexProcedure<? super E>> P iterate(final P procedure)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingList<E>#iterate
	}

	@Override
	public XGettingList<E> view()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingList<E>#view
	}

	@Override
	public XGettingList<E> view(final long lowIndex, final long highIndex)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingList<E>#view
	}

	@Override
	public XGettingList<E> range(final long fromIndex, final long toIndex)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME XGettingList<E>#range
	}

}

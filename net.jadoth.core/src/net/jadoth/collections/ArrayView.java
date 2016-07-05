package net.jadoth.collections;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.Jadoth;
import net.jadoth.collections.old.AbstractOldGettingList;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingList;
import net.jadoth.collections.types.XImmutableList;
import net.jadoth.collections.types.XList;
import net.jadoth.collections.types.XSettingList;
import net.jadoth.exceptions.IndexBoundsException;
import net.jadoth.functional.BiProcedure;
import net.jadoth.functional.IndexProcedure;
import net.jadoth.functional.JadothEqualators;
import net.jadoth.util.Equalator;
import net.jadoth.util.iterables.ReadOnlyListIterator;


/**
 * Immutable implementation of extended collection type {@link XGettingList}.
 * <p>
 * For mutable extended lists (implementors of {@link XSettingList}, {@link XList}), see {@link FixedList},
 * {@link LimitList}, {@link BulkList}.
 * <p>
 * As instances of this class are completely immutable after creation, this list is automatically thread-safe.
 * <p>
 * Also note that by being an extended collection, this implementation offers various functional and batch procedures
 * to maximize internal iteration potential, eliminating the need to use the ill-conceived external iteration
 * {@link Iterator} paradigm.
 *
 * @author Thomas Muenz
 * @version 0.91, 2011-02-28
 */
public final class ArrayView<E> extends AbstractSimpleArrayCollection<E> implements XGettingList<E>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private static final Object[] DUMMY = new Object[0];



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	E[] data; // the storage array containing the elements
	int size; // the current element count (logical size)



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	@SuppressWarnings("unchecked")
	public ArrayView()
	{
		super();
		this.data = (E[])DUMMY;
		this.size = 0;
	}

	public ArrayView(final ArrayView<? extends E> original) throws NullPointerException
	{
		super();
		this.data = original.data;
		this.size = original.size;
	}

	@SuppressWarnings("unchecked")
	public ArrayView(final E... elements) throws NullPointerException
	{
		super();
		this.size = (this.data = elements != null ? elements : (E[])DUMMY).length;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters          //
	/////////////////////

	public E[] getArray()
	{
		return this.data == DUMMY ? null : (E[])this.data;
	}

	@Override
	public Equalator<? super E> equality()
	{
		return JadothEqualators.identity();
	}

	@Override
	protected int internalCountingAddAll(final E[] elements) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}

	@Override
	protected int internalCountingAddAll(final E[] elements, final int offset, final int length)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}

	@Override
	protected int internalCountingAddAll(final XGettingCollection<? extends E> elements)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}

	@Override
	protected int internalCountingPutAll(final E[] elements) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}

	@Override
	protected int internalCountingPutAll(final E[] elements, final int offset, final int length)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}

	@Override
	protected int internalCountingPutAll(final XGettingCollection<? extends E> elements)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}



	///////////////////////////////////////////////////////////////////////////
	// setters          //
	/////////////////////

	@SuppressWarnings("unchecked")
	public ArrayView<E> setArray(final E[] array)
	{
		if(array == null)
		{
			this.data = (E[])DUMMY;
			this.size = 0;
			return this;
		}

		if(this.size < 0 || this.size > array.length)
		{
			throw new ArrayIndexOutOfBoundsException(this.size);
		}
		this.data = array;
		this.size = array.length;
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	protected E[] internalGetStorageArray()
	{
		return this.data;
	}

	@Override
	protected int internalSize()
	{
		return this.size;
	}

	@Override
	protected int[] internalGetSectionIndices()
	{
		return new int[]{0, this.size}; // trivial section
	}



	///////////////////////////////////////////////////////////////////////////
	// getting methods  //
	/////////////////////

	@Override
	public ArrayView<E> copy()
	{
		return new ArrayView<>(this);
	}

	@Override
	public XImmutableList<E> immure()
	{
		return new ConstList<>(this);
	}

	@Override
	public ArrayView<E> toReversed()
	{
		final E[] rData = JadothArrays.newArrayBySample(this.data, this.size);
		final E[] data = this.data;
		for(int i = this.size, r = 0; i-- > 0;)
		{
			rData[r++] = data[i];
		}
		return new ArrayView<>(rData);
	}

	@Override
	public E[] toArray(final Class<E> type)
	{
		final E[] array = JadothArrays.newArray(type, this.size);
		System.arraycopy(this.data, 0, array, 0, this.size);
		return array;
	}

	// executing //

	@Override
	public <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		AbstractArrayStorage.iterate(this.data, this.size, procedure);
		return procedure;
	}

	@Override
	public final <A> A join(final BiProcedure<? super E, ? super A> joiner, final A aggregate)
	{
		AbstractArrayStorage.join(this.data, this.size, joiner, aggregate);
		return aggregate;
	}

	@Override
	public final <P extends IndexProcedure<? super E>> P iterate(final P procedure)
	{
		AbstractArrayStorage.iterate(this.data, this.size, procedure);
		return procedure;
	}

	// aggregating //

	// count querying //

	@Override
	public long count(final E element)
	{
		return AbstractArrayStorage.count(this.data, this.size, element);
	}

//	@Override
//	public int count(final E sample, final Equalator<? super E> equalator)
//	{
//		return AbstractArrayStorage.count(this.data, this.size, sample, equalator);
//	}

	@Override
	public long countBy(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.conditionalCount(this.data, this.size, predicate);
	}

	// index querying //

	@Override
	public long indexOf(final E element)
	{
		return AbstractArrayStorage.indexOf(this.data, this.size, element);
	}

//	@Override
//	public int indexOf(final E sample, final Equalator<? super E> equalator)
//	{
//		return AbstractArrayStorage.indexOf(this.data, this.size, sample, equalator);
//	}

	@Override
	public long indexBy(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.conditionalIndexOf(this.data, this.size, predicate);
	}

	@Override
	public long lastIndexOf(final E element)
	{
		return AbstractArrayStorage.rngIndexOF(this.data, this.size, this.size - 1, -this.size, element);
	}

	@Override
	public long lastIndexBy(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.lastIndexOf(this.data, this.size, predicate);
	}

	@Override
	public long maxIndex(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.maxIndex(this.data, this.size, comparator);
	}

	@Override
	public long minIndex(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.minIndex(this.data, this.size, comparator);
	}

	@Override
	public long scan(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.scan(this.data, this.size, predicate);
	}

	// element querying //

	@Override
	public E get()
	{
		return this.data[0];
	}

	@Override
	public E first()
	{
		return this.data[0];
	}

	@Override
	public E last()
	{
		return this.data[this.size - 1];
	}

	@Override
	public E poll()
	{
		return this.size == 0 ? null : (E)this.data[0];
	}

	@Override
	public E peek()
	{
		return this.size == 0 ? null : (E)this.data[this.size - 1];
	}

//	@Override
//	public E search(final E sample, final Equalator<? super E> equalator)
//	{
//		return AbstractArrayStorage.find(this.data, this.size, sample, equalator);
//	}

	@Override
	public E seek(final E sample)
	{
		return AbstractArrayStorage.containsSame(this.data, this.size, sample) ? sample : null;
	}

	@Override
	public E search(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.queryElement(this.data, this.size, predicate, null);
	}

	@Override
	public E max(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.max(this.data, this.size, comparator);
	}

	@Override
	public E min(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.min(this.data, this.size, comparator);
	}

	// boolean querying //

	@Override
	public boolean hasVolatileElements()
	{
		return false;
	}

	@Override
	public boolean nullAllowed()
	{
		return true;
	}

	@Override
	public boolean isSorted(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.isSorted(this.data, this.size, comparator);
	}

	@Override
	public boolean hasDistinctValues()
	{
		return AbstractArrayStorage.hasDistinctValues(this.data, this.size);
	}

	@Override
	public boolean hasDistinctValues(final Equalator<? super E> equalator)
	{
		return AbstractArrayStorage.hasDistinctValues(this.data, this.size, equalator);
	}

	// boolean querying - applies //

	@Override
	public boolean containsSearched(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.contains(this.data, this.size, predicate);
	}

	@Override
	public boolean applies(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.applies(this.data, this.size, predicate);
	}

	// boolean querying - contains //

	@Override
	public boolean nullContained()
	{
		return AbstractArrayStorage.nullContained(this.data, this.size);
	}

	@Override
	public boolean containsId(final E element)
	{
		return AbstractArrayStorage.containsSame(this.data, this.size, element);
	}

	@Override
	public boolean contains(final E element)
	{
		return AbstractArrayStorage.containsSame(this.data, this.size, element);
	}

	@Override
	public boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		return AbstractArrayStorage.containsAll(this.data, this.size, elements);
	}

//	@Override
//	public boolean containsAll(final XGettingCollection<? extends E> elements, final Equalator<? super E> equalator)
//	{
//		return AbstractArrayStorage.containsAll(this.data, this.size, elements, equalator);
//	}

	// boolean querying - equality //

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		if(samples == null || !(samples instanceof ArrayView<?>) || Jadoth.to_int(samples.size()) != this.size)
		{
			return false;
		}
		if(samples == this)
		{
			return true;
		}

		// equivalent to equalsContent()
		return JadothArrays.equals(
			this.data,
			0,
			((ArrayView<?>)samples).data,
			0,
			this.size,
			(Equalator<Object>)equalator
		);
	}

	@Override
	public boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		if(samples == null || Jadoth.to_int(samples.size()) != this.size)
		{
			return false;
		}
		if(samples == this)
		{
			return true;
		}
		return AbstractArrayStorage.equalsContent(this.data, this.size, samples, equalator);
	}

	// data set procedures //

	@Override
	public <C extends Consumer<? super E>> C intersect(
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		return AbstractArrayStorage.intersect(this.data, this.size, samples, equalator, target);
	}

	@Override
	public <C extends Consumer<? super E>> C except(
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		return AbstractArrayStorage.except(this.data, this.size, samples, equalator, target);
	}

	@Override
	public <C extends Consumer<? super E>> C union(
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		return AbstractArrayStorage.union(this.data, this.size, samples, equalator, target);
	}

	@Override
	public <C extends Consumer<? super E>> C copyTo(final C target)
	{
		return AbstractArrayStorage.copyTo(this.data, this.size, target);
	}

	@Override
	public <C extends Consumer<? super E>> C filterTo(final C target, final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.copyTo(this.data, this.size, target, predicate);
	}

	@Override
	public <T> T[] copyTo(final T[] target, final int offset)
	{
		System.arraycopy(this.data, 0, target, offset, this.size);
		return target;
	}

	@Override
	public <T> T[] copyTo(final T[] target, final int targetOffset, final long offset, final int length)
	{
		return AbstractArrayStorage.rngCopyTo(
			this.data                     ,
			this.size                     ,
			Jadoth.checkArrayRange(offset),
			length                        ,
			target                        ,
			targetOffset
		);
	}

	public <T> T[] rngCopyTo(final int startIndex, final int length, final T[] target, final int offset)
	{
		return AbstractArrayStorage.rngCopyTo(this.data, this.size, startIndex, length,  target, offset);
	}

	@Override
	public <C extends Consumer<? super E>> C distinct(final C target)
	{
		return AbstractArrayStorage.distinct(this.data, this.size, target);
	}

	@Override
	public <C extends Consumer<? super E>> C distinct(final C target, final Equalator<? super E> equalator)
	{
		return AbstractArrayStorage.distinct(this.data, this.size, target, equalator);
	}

	@Override
	public <C extends Consumer<? super E>> C copySelection(final C target, final long... indices)
	{
		return AbstractArrayStorage.copySelection(this.data, this.size, indices, target);
	}



	///////////////////////////////////////////////////////////////////////////
	// java.util.list and derivatives  //
	////////////////////////////////////

	@Override
	public boolean isEmpty()
	{
		return this.size == 0;
	}

	@Override
	public Iterator<E> iterator()
	{
		return new ReadOnlyListIterator<>(this);
	}

	@Override
	public ListIterator<E> listIterator()
	{
		return new ReadOnlyListIterator<>(this);
	}

	@Override
	public ListIterator<E> listIterator(final long index)
	{
		validateIndex(this.size, index);
		return new ReadOnlyListIterator<>(this, (int)index);
	}

	@Override
	public long size()
	{
		return this.size;
	}

	@Override
	public long maximumCapacity()
	{
		return this.size; // size is always array length
	}

	@Override
	public boolean isFull()
	{
		return true; // array is always "full"
	}

	@Override
	public long remainingCapacity()
	{
		return 0;
	}

	@Override
	public ArrayView<E> view()
	{
		return this;
	}

	@Override
	public SubListView<E> view(final long fromIndex, final long toIndex)
	{
		return new SubListView<>(this, fromIndex, toIndex); // range check is done in constructor
	}

	@Override
	public SubListView<E> range(final long fromIndex, final long toIndex)
	{
		// range check is done in constructor
		return new SubListView<>(this, fromIndex, toIndex);
	}

	@Override
	public String toString()
	{
		return AbstractArrayStorage.toString(this.data, this.size);
	}

	@Override
	public Object[] toArray()
	{
		final Object[] array = new Object[this.size];
		System.arraycopy(this.data, 0, array, 0, this.size);
		return array;
	}

	@Override
	public E at(final long index) throws ArrayIndexOutOfBoundsException
	{
		if(index >= this.size)
		{
			throw new IndexBoundsException(this.size, index);
		}
		return this.data[(int)index];
	}

	@Deprecated
	@Override
	public boolean equals(final Object o)
	{
		//trivial escape conditions
		if(o == this)
		{
			return true;
		}
		if(o == null || !(o instanceof List<?>))
		{
			return false;
		}

		final List<?> list = (List<?>)o;
		if(this.size != list.size())
		{
			return false; //lists can only be equal if they have the same length
		}

		final Object[] data = this.data;
		int i = 0;
		for(final Object e2 : list)
		{
			// use iterator for passed list as it could be a non-random-access list
			final Object e1 = data[i++];
			if(e1 == null)
			{
				// null-handling escape conditions
				if(e2 != null)
				{
					return false;
				}
				continue;
			}
			if(!e1.equals(e2))
			{
				return false;
			}
		}
		return true; //no un-equal element found, so lists must be equal
	}

	@Deprecated
	@Override
	public int hashCode()
	{
		return JadothArrays.arrayHashCode(this.data, this.size);
	}

	@Override
	public OldArrayView<E> old()
	{
		return new OldArrayView<>(this);
	}

	public static final class OldArrayView<E> extends AbstractOldGettingList<E>
	{
		OldArrayView(final ArrayView<E> list)
		{
			super(list);
		}

		@Override
		public ArrayView<E> parent()
		{
			return (ArrayView<E>)super.parent();
		}

	}

}

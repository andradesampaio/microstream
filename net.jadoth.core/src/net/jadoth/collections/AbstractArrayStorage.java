package net.jadoth.collections;

import static net.jadoth.collections.JadothArrays.removeAllFromArray;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.RandomAccess;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import net.jadoth.Jadoth;
import net.jadoth.collections.functions.AggregateMax;
import net.jadoth.collections.functions.AggregateMin;
import net.jadoth.collections.functions.IsCustomEqual;
import net.jadoth.collections.functions.IsGreater;
import net.jadoth.collections.functions.IsNull;
import net.jadoth.collections.functions.IsSame;
import net.jadoth.collections.functions.IsSmaller;
import net.jadoth.collections.interfaces.ChainStorage;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XMap;
import net.jadoth.functional.Aggregator;
import net.jadoth.functional.BiProcedure;
import net.jadoth.functional.IndexProcedure;
import net.jadoth.functional.JadothPredicates;
import net.jadoth.functional.JadothProcedures;
import net.jadoth.math.FastRandom;
import net.jadoth.math.JadothMath;
import net.jadoth.util.Equalator;
import net.jadoth.util.branching.ThrowBreak;
import net.jadoth.util.chars.VarString;


/**
 * Abstract class with static delegate array logic methods to be used as pseudo-array-inherent methods.
 * <p>
 * The passed array and size are handled as pseudo implementation details and thus are never sanity-checked.
 * <p>
 * See {@link ChainStorage} and for example {@link ChainStrongLogic} as a comparable actual logic implementation without
 * delegate-pseudo-character.
 *
 * @author Thomas Muenz
 */
public abstract class AbstractArrayStorage
{
	// CHECKSTYLE.OFF: FinalParameter: A LOT of methods use that pattern in this class

	///////////////////////////////////////////////////////////////////////////
	// utility methods  //
	/////////////////////

	public static final void validateRange0toUpperBound(final int upperBound, final int offset, final int length)
	{
		if(offset < 0 || offset >= upperBound)
		{
			throw new IndexExceededException(upperBound, offset);
		}

		if(length > 0 && offset + length > upperBound)
		{
			throw new IndexExceededException(upperBound, offset + length);
		}
		else if(length < 0 && offset + length < -1)
		{
			throw new IndexExceededException(-1, offset + length);
		}
	}

	private static final void clearRest(final Object[] data, final int offset)
	{
		for(int i = offset; i < data.length; i++)
		{
			data[i] = null;
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// index scanning   //
	/////////////////////

	public static final <E> int indexOf(
		final E[] data,
		final int size,
		final E   element
	)
	{
		return forwardIndexOf(data, 0, size, element);
	}

	public static final <E> int rngIndexOF(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final E   element
	)
	{
		validateRange0toUpperBound(size, offset, length);
		return length >= 0
			? forwardIndexOf(data, offset, offset + length    , element)
			: reverseIndexOf(data, offset, offset + length + 1, element)
		;
	}

	public static final <E> int forwardIndexOf(
		final E[] data,
		final int lowOffset,
		final int highBound,
		final E   element
	)
	{
		return reverseIndexOf(data, lowOffset, highBound, new IsSame<>(element));
	}

	public static final <E> int reverseIndexOf(
		final E[] data,
		final int highOffset,
		final int lowEnd,
		final E   element
	)
	{
		return forwardConditionalIndexOf(data, highOffset, lowEnd, new IsSame<>(element));
	}

	public static final <E> int indexOf0(
		final E[] data,
		final int size,
		final E   element
	)
	{
		return forwardConditionalIndexOf(data, 0, size, new IsSame<>(element));
	}

	public static final <E> int indexOf1(
		final E[] data,
		final int size,
		final E   element
	)
	{
		return forwardConditionalIndexOf(data, 0, size, new IsSame<>(element));
	}

	public static final <E> int conditionalIndexOf(
		final E[] data,
		final int size,
		final Predicate<? super E> predicate
	)
	{
		return forwardConditionalIndexOf(data, 0, size, predicate);
	}

	public static final <E> int rngConditionalIndexOf(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Predicate<? super E> predicate
	)
	{
		validateRange0toUpperBound(size, offset, length);
		return length >= 0
			? forwardConditionalIndexOf(data, offset, offset + length    , predicate)
			: reverseConditionalIndexOf(data, offset, offset + length + 1, predicate)
		;
	}

	public static final <E> int forwardConditionalIndexOf(
		final E[] data,
		final int lowOffset,
		final int highBound,
		final Predicate<? super E> predicate
	)
	{
		try
		{
			for(int i = lowOffset; i < highBound; i++)
			{
				if(predicate.test(data[i]))
				{
					return i;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return -1;
	}

	public static final <E> int reverseConditionalIndexOf(
		final E[] data,
		final int highOffset,
		final int lowEnd,
		final Predicate<? super E> predicate
	)
	{
		try
		{
			for(int i = highOffset; i >= lowEnd; i--)
			{
				if(predicate.test(data[i]))
				{
					return i;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return -1;
	}

	// -- //

	public static final <E> int scan(final E[] data, final int size, final Predicate<? super E> predicate)
	{
		return forwardScan(data, 0, size, predicate);
	}

	public static final <E> int rngScan(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Predicate<? super E> predicate
	)
	{
		validateRange0toUpperBound(size, offset, length);
		return length >= 0
			? forwardScan(data, offset, offset + length    , predicate)
			: reverseScan(data, offset, offset + length + 1, predicate)
		;
	}

	public static final <E> int forwardScan(
		final E[] data,
		final int lowOffset,
		final int highBound,
		final Predicate<? super E> predicate
	)
	{
		int index = -1;
		try
		{
			for(int i = lowOffset; i < highBound; i++)
			{
				if(predicate.test(data[i]))
				{
					index = i;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return index;
	}

	public static final <E> int reverseScan(
		final E[] data,
		final int highOffset,
		final int lowEnd,
		final Predicate<? super E> predicate
	)
	{
		int index = -1;
		try
		{
			for(int i = highOffset; i >= lowEnd; i--)
			{
				if(predicate.test(data[i]))
				{
					index = i;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return index;
	}



	///////////////////////////////////////////////////////////////////////////
	// condition applying //
	///////////////////////

	public static final <E> boolean contains(
		final E[] data,
		final int size,
		final Predicate<? super E> predicate
	)
	{
		return forwardContains(data, 0, size, predicate);
	}

	public static final <E> boolean rngContains(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Predicate<? super E> predicate
	)
	{
		validateRange0toUpperBound(size, offset, length);
		return length >= 0
			? forwardContains(data, offset, offset + length    , predicate)
			: reverseContains(data, offset, offset + length + 1, predicate)
		;
	}

	public static final <E> boolean forwardContains(
		final E[] data,
		final int lowOffset,
		final int highBound,
		final Predicate<? super E> predicate
	)
	{
		try
		{
			for(int i = lowOffset; i < highBound; i++)
			{
				if(predicate.test(data[i]))
				{
					return true;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return false;
	}

	public static final <E> boolean reverseContains(
		final E[] data,
		final int highOffset,
		final int lowEnd,
		final Predicate<? super E> predicate
	)
	{
		try
		{
			for(int i = highOffset; i >= lowEnd; i--)
			{
				if(predicate.test(data[i]))
				{
					return true;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return false;
	}

	// -- //

	public static final <E> boolean applies(
		final E[] data,
		final int size,
		final Predicate<? super E> predicate
	)
	{
		return forwardApplies(data, 0, size, predicate);
	}

	public static final <E> boolean rngApplies(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Predicate<? super E> predicate
	)
	{
		validateRange0toUpperBound(size, offset, length);
		return length >= 0
			? forwardApplies(data, offset, offset + length    , predicate)
			: reverseApplies(data, offset, offset + length + 1, predicate)
		;
	}

	public static final <E> boolean forwardApplies(
		final E[] data,
		final int lowOffset,
		final int highBound,
		final Predicate<? super E> predicate
	)
	{
		try
		{
			for(int i = lowOffset; i < highBound; i++)
			{
				if(!predicate.test(data[i]))
				{
					return false;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return true;
	}

	public static final <E> boolean reverseApplies(
		final E[] data,
		final int highOffset,
		final int lowEnd,
		final Predicate<? super E> predicate
	)
	{
		try
		{
			for(int i = highOffset; i >= lowEnd; i--)
			{
				if(!predicate.test(data[i]))
				{
					return false;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return true;
	}



	///////////////////////////////////////////////////////////////////////////
	// conditional counting //
	/////////////////////////

	public static final <E> int conditionalCount(
		final E[] data,
		final int size,
		final Predicate<? super E> predicate
	)
	{
		return forwardConditionalCount(data, 0, size, predicate);
	}

	public static final <E> int rngConditionalCount(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Predicate<? super E> predicate
	)
	{
		return length >= 0
			? forwardConditionalCount(data, offset, offset + length    , predicate)
			: reverseConditionalCount(data, offset, offset + length + 1, predicate)
		;
	}

	public static final <E> int forwardConditionalCount(
		final E[] data,
		final int lowOffset,
		final int highBound,
		final Predicate<? super E> predicate
	)
	{
		int count = 0;
		try
		{
			for(int i = lowOffset; i < highBound; i++)
			{
				if(predicate.test(data[i]))
				{
					count++;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return count;
	}

	public static final <E> int reverseConditionalCount(
		final E[] data,
		final int highOffset,
		final int lowEnd,
		final Predicate<? super E> predicate
	)
	{
		int count = 0;
		try
		{
			for(int i = highOffset; i >= lowEnd; i--)
			{
				if(predicate.test(data[i]))
				{
					count++;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return count;
	}



	///////////////////////////////////////////////////////////////////////////
	// element querying  //
	//////////////////////

	public static final <E> E searchElement(
		final E[] data,
		final int size,
		final Predicate<? super E> predicate,
		final E notFoundMarker
	)
	{
		return forwardSearchElement(data, 0, size, predicate, notFoundMarker);
	}

	public static final <E> E forwardSearchElement(
		final E[] data,
		final int lowOffset,
		final int highBound,
		final Predicate<? super E> predicate,
		final E notFoundMarker
	)
	{
		try
		{
			for(int i = lowOffset; i < highBound; i++)
			{
				if(predicate.test(data[i]))
				{
					return data[i];
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return notFoundMarker;
	}

	public static final <E> E reverseSearchElement(
		final E[] data,
		final int highOffset,
		final int lowEnd,
		final Predicate<? super E> predicate,
		final E notFoundMarker
	)
	{
		try
		{
			for(int i = highOffset; i >= lowEnd; i--)
			{
				if(predicate.test(data[i]))
				{
					return data[i];
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return notFoundMarker;
	}

	// -- //

	public static final <E> E queryElement(
		final E[]                  data          ,
		final int                  size          ,
		final Predicate<? super E> predicate     ,
		final E                    notFoundMarker
	)
	{
		return forwardQueryElement(data, 0, size, predicate, notFoundMarker);
	}

	public static final <E> E rngQueryElement(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Predicate<? super E> predicate,
		final E notFoundMarker
	)
	{
		validateRange0toUpperBound(size, offset, length);
		return length >= 0
			? forwardQueryElement(data, offset, offset + length    , predicate, notFoundMarker)
			: reverseQueryElement(data, offset, offset + length + 1, predicate, notFoundMarker)
		;
	}

	public static final <E> E forwardQueryElement(
		final E[] data,
		final int lowOffset,
		final int highBound,
		final Predicate<? super E> predicate,
		final E notFoundMarker
	)
	{
		E match = notFoundMarker;
		try
		{
			for(int i = lowOffset; i < highBound; i++)
			{
				if(predicate.test(data[i]))
				{
					match = data[i];
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return match;
	}

	public static final <E> E reverseQueryElement(
		final E[] data,
		final int highOffset,
		final int lowEnd,
		final Predicate<? super E> predicate,
		final E notFoundMarker
	)
	{
		E match = notFoundMarker;
		try
		{
			for(int i = highOffset; i >= lowEnd; i--)
			{
				if(predicate.test(data[i]))
				{
					match = data[i];
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return match;
	}



	///////////////////////////////////////////////////////////////////////////
	// iteration        //
	/////////////////////

	public static final <E> void iterate(
		final E[] data,
		final int size,
		final Consumer<? super E> procedure
	)
	{
		try
		{
			for(int i = 0; i < size; i++)
			{
				procedure.accept(data[i]);
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	public static final <E, A> void join(
		final E[]                          data     ,
		final int                          size     ,
		final BiProcedure<? super E, ? super A> joiner   ,
		final A                            aggregate
	)
	{
		try
		{
			for(int i = 0; i < size; i++)
			{
				joiner.accept(data[i], aggregate);
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	public static final <E> void rngIterate(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Consumer<? super E> procedure
	)
	{
		validateRange0toUpperBound(size, offset, length);
		if(length >= 0)
		{
			forwardIterate(data, offset, offset + length    , procedure);
		}
		else
		{
			reverseIterate(data, offset, offset + length + 1, procedure);
		}
	}

	public static final <E, A> void rngJoin(
		final E[]                          data  ,
		final int                          size  ,
		final int                          offset,
		final int                          length,
		final BiProcedure<? super E, ? super A> joiner,
		final A                            aggregate
	)
	{
		validateRange0toUpperBound(size, offset, length);
		if(length >= 0)
		{
			forwardJoin(data, offset, offset + length    , joiner, aggregate);
		}
		else
		{
			reverseJoin(data, offset, offset + length + 1, joiner, aggregate);
		}
	}

	public static final <E> void forwardIterate(
		final E[] data,
		final int lowOffset,
		final int highBound,
		final Consumer<? super E> procedure
	)
	{
		try
		{
			for(int i = lowOffset; i < highBound; i++)
			{
				procedure.accept(data[i]);
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	public static final <E> void reverseIterate(
		final E[] data,
		final int highOffset,
		final int lowEnd,
		final Consumer<? super E> procedure
	)
	{
		try
		{
			for(int i = highOffset; i >= lowEnd; i--)
			{
				procedure.accept(data[i]);
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	public static final <E, A> void forwardJoin(
		final E[]                          data     ,
		final int                          lowOffset,
		final int                          highBound,
		final BiProcedure<? super E, ? super A> joiner   ,
		final A                            aggregate
	)
	{
		try
		{
			for(int i = lowOffset; i < highBound; i++)
			{
				joiner.accept(data[i], aggregate);
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	public static final <E, A> void reverseJoin(
		final E[]                          data      ,
		final int                          highOffset,
		final int                          lowEnd    ,
		final BiProcedure<? super E, ? super A> joiner    ,
		final A                            aggregate
	)
	{
		try
		{
			for(int i = highOffset; i >= lowEnd; i--)
			{
				joiner.accept(data[i], aggregate);
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	// -- //

	public static final <E> void conditionalIterate(
		final E[] data,
		final int size,
		final Predicate<? super E> predicate,
		final Consumer<? super E> procedure
	)
	{
		forwardConditionalIterate(data, 0, size, predicate, procedure);
	}

	public static final <E> void rngConditionalIterate(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Predicate<? super E> predicate,
		final Consumer<? super E> procedure
	)
	{
		validateRange0toUpperBound(size, offset, length);
		if(length >= 0)
		{
			forwardConditionalIterate(data, offset, offset + length    , predicate, procedure);
		}
		else
		{
			reverseConditionalIterate(data, offset, offset + length + 1, predicate, procedure);
		}
	}

	public static final <E> void forwardConditionalIterate(
		final E[] data,
		final int lowOffset,
		final int highBound,
		final Predicate<? super E> predicate,
		final Consumer<? super E> procedure
	)
	{
		forwardIterate(data, lowOffset, highBound, JadothProcedures.wrapWithPredicate(procedure, predicate));
	}

	public static final <E> void reverseConditionalIterate(
		final E[] data,
		final int highOffset,
		final int lowEnd,
		final Predicate<? super E> predicate,
		final Consumer<? super E> procedure
	)
	{
		reverseIterate(data, highOffset, lowEnd, JadothProcedures.wrapWithPredicate(procedure, predicate));
	}



	///////////////////////////////////////////////////////////////////////////
	// aggregation      //
	/////////////////////

	public static final <E, R> R aggregate(
		final E[] data,
		final int size,
		final Aggregator<? super E, R> aggregator
	)
	{
		return forwardAggregate(data, 0, size, aggregator);
	}

	public static final <E, R> R rngAggregate(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Aggregator<? super E, R> aggregator
	)
	{
		validateRange0toUpperBound(size, offset, length);
		return length >= 0
			? forwardAggregate(data, offset, offset + length    , aggregator)
			: reverseAggregate(data, offset, offset + length + 1, aggregator)
		;
	}

	public static final <E, R> R forwardAggregate(
		final E[] data,
		final int lowOffset,
		final int highBound,
		final Aggregator<? super E, R> aggregator
	)
	{
		forwardIterate(data, lowOffset, highBound, aggregator);
		return aggregator.yield();
	}

	public static final <E, R> R reverseAggregate(
		final E[] data,
		final int highOffset,
		final int lowEnd,
		final Aggregator<? super E, R> aggregator
	)
	{
		reverseIterate(data, highOffset, lowEnd, aggregator);
		return aggregator.yield();
	}

	// -- //

	public static final <E, R> R conditionalAggregate(
		final E[] data,
		final int size,
		final Predicate<? super E> predicate,
		final Aggregator<? super E, R> aggregator
	)
	{
		return forwardConditionalAggregate(data, 0, size, predicate, aggregator);
	}

	public static final <E, R> R forwardConditionalAggregate(
		final E[] data,
		final int lowOffset,
		final int highBound,
		final Predicate<? super E> predicate,
		final Aggregator<? super E, R> aggregator
	)
	{
		forwardConditionalIterate(data, lowOffset, highBound, predicate, aggregator);
		return aggregator.yield();
	}

	public static final <E, R> R reverseConditionalAggregate(
		final E[] data,
		final int highOffset,
		final int lowEnd,
		final Predicate<? super E> predicate,
		final Aggregator<? super E, R> aggregator
	)
	{
		reverseConditionalIterate(data, highOffset, lowEnd, predicate, aggregator);
		return aggregator.yield();
	}



	///////////////////////////////////////////////////////////////////////////
	//     adding       //
	/////////////////////

	public static final <E> int addAll(
		final E[] data,
		final int size,
		final E[] src,
		final int srcIndex,
		final int srcLength,
		final Predicate<? super E> predicate
	)
	{
		validateRange0toUpperBound(src.length, srcIndex, srcLength);
		try
		{
			return srcLength >= 0
				? forwardAddAll(data, size, src, srcIndex, srcLength, predicate)
				: reverseAddAll(data, size, src, srcIndex, srcLength, predicate)
			;
		}
		catch(final ArrayIndexOutOfBoundsException e)
		{
			clearRest(data, size); // rollback (can't check length beforehand because of predicate)
			throw e; // note that collection's actual size value remains on its old value
		}
	}

	private static final <E> int forwardAddAll(
		final E[] data,
		final int size,
		final E[] src,
		final int srcIndex,
		final int srcLength,
		final Predicate<? super E> predicate
	)
	{
		int s = size;
		try
		{
			final int srcBound = srcIndex + srcLength;
			for(int i = srcIndex; i < srcBound; i++)
			{
				if(predicate.test(src[i]))
				{
					data[s++] = src[i];
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return s;
	}

	private static final <E> int reverseAddAll(
		final E[] data,
		final int size,
		final E[] src,
		final int srcIndex,
		final int srcLength,
		final Predicate<? super E> predicate
	)
	{
		int s = size;
		try
		{
			final int srcBound = srcIndex + srcLength;
			for(int i = srcIndex; i > srcBound; i--)
			{
				if(predicate.test(src[i]))
				{
					data[s++] = src[i];
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return s;
	}



	///////////////////////////////////////////////////////////////////////////
	// containing       //
	/////////////////////

	public static final <E> boolean nullContained(final E[] data, final int size)
	{
		return forwardNullContained(data, 0, size);
	}

	public static final <E> boolean forwardNullContained(
		final E[] data,
		final int lowOffset,
		final int highBound
	)
	{
		return forwardContains(data, lowOffset, highBound, new IsNull<>()); // should get stack-allocated
	}

	public static final <E> boolean reverseNullContained(
		final E[] data,
		final int highOffset,
		final int lowEnd
	)
	{
		return reverseContains(data, highOffset, lowEnd, new IsNull<>()); // should get stack-allocated
	}

	public static final <E> boolean rngContainsNull(
		final E[] data,
		final int size,
		final int offset,
		final int length
	)
	{
		validateRange0toUpperBound(size, offset, length);
		return length >= 0
			? forwardNullContained(data, offset, offset + length    )
			: reverseNullContained(data, offset, offset + length + 1)
		;
	}

	// -- //

	public static final <E> boolean containsSame(final E[] data, final int size, final E element)
	{
		return forwardContainsSame(data, 0, size, element);
	}

	public static final <E> boolean rngContainsSame(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final E element
	)
	{
		validateRange0toUpperBound(size, offset, length);
		return length >= 0
			? forwardContainsSame(data, offset, offset + length    , element)
			: reverseContainsSame(data, offset, offset + length + 1, element)
		;
	}

	public static final <E> boolean forwardContainsSame(
		final E[] data,
		final int lowOffset,
		final int highBound,
		final E   element
	)
	{
		return forwardContains(data, lowOffset, highBound, new IsSame<>(element));
	}

	public static final <E> boolean reverseContainsSame(
		final E[] data,
		final int lowOffset,
		final int highBound,
		final E   element
	)
	{
		return reverseContains(data, lowOffset, highBound, new IsSame<>(element));
	}

	// -- //

	public static final <E> boolean containsAll(
		final E[] data,
		final int size,
		final XGettingCollection<? extends E> elements
	)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			// directly check array against array without predicate function or method calls
			return JadothArrays.uncheckedContainsAll(
				data,
				0,
				size,
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements),
				0,
				Jadoth.to_int(elements.size())
			);
		}

		// iterate by predicate function
		return elements.applies(e -> containsSame(data, size, e));
	}

	public static final <E> boolean rngContainsAll(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final XGettingCollection<? extends E> elements
	)
	{
		validateRange0toUpperBound(size, offset, length);

		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			// directly check array against array without predicate function or method calls
			return JadothArrays.uncheckedContainsAll(
				data,
				length >= 0 ? offset          : offset + length + 1,
				length >= 0 ? offset + length : offset + 1,
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements),
				0,
				Jadoth.to_int(elements.size())
			);
		}

		// collection is not array-backed (e.g. hash set), so iterate by predicate function
		if(length < 0)
		{
			return elements.applies(e ->
				reverseContainsSame(data, offset, offset + length + 1, e)
			);
		}
		return elements.applies(e ->
			forwardContainsSame(data, offset, offset + length, e)
		);
	}

	public static final <E> boolean containsAll(
		final E[] data,
		final int size,
		final XGettingCollection<? extends E> elements,
		final Equalator<? super E> equalator
	)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			// directly check array against array without predicate function or method calls
			return JadothArrays.uncheckedContainsAll(
				data,
				0,
				size,
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements),
				0,
				Jadoth.to_int(elements.size()),
				equalator
			);
		}

		// collection is not array-backed (e.g. hashset), so iterate by predicate function
		return elements.applies(e ->
		{
			return contains(data, size, new IsCustomEqual<>(equalator, e));
		});
	}



	///////////////////////////////////////////////////////////////////////////
	//     counting     //
	/////////////////////

	public static final <E> int count(
		final E[] data,
		final int size,
		final E element
	)
	{
		return forwardCount(data, 0, size, element);
	}

	public static final <E> int rngCount(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final E element
	)
	{
		validateRange0toUpperBound(size, offset, length);
		return length >= 0
			? forwardCount(data, offset, offset + length    , element)
			: reverseCount(data, offset, offset + length + 1, element)
		;
	}

	public static final <E> int forwardCount(
		final E[] data,
		final int lowOffset,
		final int highBound,
		final E   element
	)
	{
		return forwardConditionalCount(data, lowOffset, highBound, new IsSame<>(element)); // should get stack-allocated
	}

	public static final <E> int reverseCount(
		final E[] data,
		final int highOffset,
		final int lowEnd,
		final E   element
	)
	{
		return reverseConditionalCount(data, highOffset, lowEnd, new IsSame<>(element)); // should get stack-allocated
	}

	public static final <E> E max(
		final E[] data,
		final int size,
		final Comparator<? super E> comparator
	)
	{
		return forwardAggregate(data, 0, size, new AggregateMax<>(comparator));
	}

	public static final <E> E min(
		final E[] data,
		final int size,
		final Comparator<? super E> comparator
	)
	{
		return forwardAggregate(data, 0, size, new AggregateMin<>(comparator));
	}

	public static final <E> int lastIndexOf(final E[] data, final int size, final Predicate<? super E> predicate)
	{
		return reverseConditionalIndexOf(data, size - 1, 0, predicate);
	}

	public static final <E> int maxIndex(final E[] data, final int size, final Comparator<? super E> comparator)
	{
		return forwardScan(data, 0, size, new IsGreater<>(comparator));
	}

	public static final <E> int minIndex(
		final E[] data,
		final int size,
		final Comparator<? super E> comparator
	)
	{
		return forwardScan(data, 0, size, new IsSmaller<>(comparator));
	}

	public static final <E, C extends Consumer<? super E>> C copyTo(
		final E[] data,
		final int size,
		final C target
	)
	{
		// note: even if target is an adding collection, the private storage array may not be passed to it directly
		return forwardCopyTo(data, 0, size, target);
	}

	public static final <E, C extends Consumer<? super E>> C rngCopyTo(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final C target
	)
	{
		// note: even if target is an adding collection, the private storage array may not be passed to it directly
		validateRange0toUpperBound(size, offset, length);
		return length >= 0
			? forwardCopyTo(data, offset, offset + length    , target)
			: reverseCopyTo(data, offset, offset + length + 1, target)
		;
	}

	public static final <E, C extends Consumer<? super E>> C forwardCopyTo(
		final E[] data,
		final int lowOffset,
		final int highBound,
		final C target
	)
	{
		forwardIterate(data, lowOffset, highBound, target);
		return target;
	}

	public static final <E, C extends Consumer<? super E>> C reverseCopyTo(
		final E[] data,
		final int highOffset,
		final int lowEnd,
		final C target
	)
	{
		reverseIterate(data, highOffset, lowEnd, target);
		return target;
	}

	public static final <E, C extends Consumer<? super E>> C copyTo(
		final E[] data,
		final int size,
		final C target,
		final Predicate<? super E> predicate
	)
	{
		return forwardCopyTo(data, 0, size, target, predicate);
	}

	public static final <E, C extends Consumer<? super E>> C rngCopyTo(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final C target,
		final Predicate<? super E> predicate
	)
	{
		validateRange0toUpperBound(size, offset, length);
		return length >= 0
			? forwardCopyTo(data, offset, offset + length    , target, predicate)
			: reverseCopyTo(data, offset, offset + length + 1, target, predicate)
		;
	}

	public static final <E, C extends Consumer<? super E>> C forwardCopyTo(
		final E[] data,
		final int lowOffset,
		final int highBound,
		final C target,
		final Predicate<? super E> predicate
	)
	{
		forwardIterate(data, lowOffset, highBound, JadothProcedures.wrapWithPredicate(target, predicate));
		return target;
	}

	public static final <E, C extends Consumer<? super E>> C reverseCopyTo(
		final E[] data,
		final int highOffset,
		final int lowEnd,
		final C target,
		final Predicate<? super E> predicate
	)
	{
		reverseIterate(data, highOffset, lowEnd, JadothProcedures.wrapWithPredicate(target, predicate));
		return target;
	}

	@SuppressWarnings("unchecked")
	public static final <E, T> T[] rngCopyTo(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final T[] target,
		final int targetOffset
	)
	{
		validateRange0toUpperBound(size, offset, length);
		validateRange0toUpperBound(target.length, targetOffset, length);

		if(length >= 0)
		{
			if(length != 0)
			{
				System.arraycopy(data, offset, target, offset, length);
			}
			return target;
		}

		final int lowEnd = offset + length + 1;
		for(int i = offset, t = targetOffset; i >= lowEnd; i--, t++)
		{
			target[t] = (T)data[i];
		}
		return target;
	}

	public static final <E, C extends Consumer<? super E>> C copySelection(
		final E[] data,
		final int size,
		final long[] indices,
		final C target
	)
	{
		final int length = indices.length;

		// validate all indices before copying the first element
		for(int i = 0; i < length; i++)
		{
			if(indices[i] < 0 || indices[i] >= size)
			{
				throw new IndexExceededException(size, indices[i]);
			}
		}

		// actual copying
		for(int i = 0; i < length; i++)
		{
			target.accept(data[(int)indices[i]]); // manual loop to spare temporary variable
		}

		return target;
	}

	public static final <E> int binarySearch(
		final E[] data,
		final int size,
		final E element,
		final Comparator<? super E> comparator
	)
	{
		return internalBinarySearch(data, 0, size - 1, element, comparator);
	}

	public static final <E> int rngBinarySearch(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final E element,
		final Comparator<? super E> comparator
	)
	{
		validateRange0toUpperBound(size, offset, length);
		return length >= 0
			? internalBinarySearch(data, offset, offset + length - 1, element, comparator)
			: internalBinarySearch(data, offset + length - 1, offset, element, comparator)
		;
	}

	static final <E> int internalBinarySearch(
		final E[] data,
		      int low,
		      int high,
		final E element,
		final Comparator<? super E> comparator
	)
	{
		try
		{
			while(low <= high)
			{
				final int order, mid;
				if((order = comparator.compare(data[mid = low + high >>> 1], element)) < 0)
				{
					low = mid + 1;
				}
				else if(order > 0)
				{
					high = mid - 1;
				}
				else
				{
					return mid; // key found
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return -(low + 1);  // key not found
	}






	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////


























	///////////////////////////////////////////////////////////////////////////
	// data arithmetic  //
	/////////////////////

	public static final <E, C extends Consumer<? super E>> C intersect(
		final E[] data,
		final int size,
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			final E[] array = AbstractSimpleArrayCollection.internalGetStorageArray(
				(AbstractSimpleArrayCollection<?>)samples
			);
			final int arrayLength = Jadoth.to_int(samples.size());
			da:
			for(int di = 0; di < size; di++)
			{
				final E element = data[di];
				for(int i = 0; i < arrayLength; i++)
				{
					if(equalator.equal(element, array[i]))
					{
						target.accept(element);
						continue da;
					}
				}
			}
			return target;
		}

		/* has to be the long way around because:
		 * - can't directly pass an instance of type E to a collection of type ? extends E.
		 * - data's equal element instances must be added, not samples'.
		 */
		final CachedSampleEquality<E> equalCurrentElement = new CachedSampleEquality<>(equalator);
		for(int di = 0; di < size; di++)
		{
			equalCurrentElement.sample = data[di];
			if(samples.containsSearched(equalCurrentElement))
			{
				target.accept(equalCurrentElement.sample);
			}
		}
		return target;
	}

	public static final <E, C extends Consumer<? super E>> C except(
		final E[]                             data     ,
		final int                             size     ,
		final XGettingCollection<? extends E> samples  ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			final E[] array = AbstractSimpleArrayCollection.internalGetStorageArray(
				(AbstractSimpleArrayCollection<?>)samples
			);
			final int arrayLength = Jadoth.to_int(samples.size());
			da:
			for(int di = 0; di < size; di++)
			{
				final E element = data[di];
				for(int i = 0; i < arrayLength; i++)
				{
					if(equalator.equal(element, array[i]))
					{
						continue da;
					}
				}
				target.accept(element);
			}
			return target;
		}

		/* has to be the long way around because:
		 * - can't directly pass an instance of type E to a collection of type ? extends E.
		 * - data's equal element instances must be added, not samples'.
		 */
		final CachedSampleEquality<E> equalCurrentElement = new CachedSampleEquality<>(equalator);
		ch:
		for(int di = 0; di < size; di++)
		{
			equalCurrentElement.sample = data[di];
			if(samples.containsSearched(equalCurrentElement))
			{
				continue ch;
			}
			target.accept(equalCurrentElement.sample);
		}
		return target;
	}

	public static final <E, C extends Consumer<? super E>> C union(
		final E[]                             data     ,
		final int                             size     ,
		final XGettingCollection<? extends E> samples  ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		copyTo(data, size, target);
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			final E[] array = AbstractSimpleArrayCollection.internalGetStorageArray(
				(AbstractSimpleArrayCollection<?>)samples
			);
			final int arrayLength = Jadoth.to_int(samples.size());
			ch:
			for(int i = 0; i < size; i++)
			{
				final E sample = array[i];
				for(int di = 0; di < arrayLength; di++)
				{
					if(equalator.equal(data[di], sample))
					{
						continue ch;
					}
				}
				target.accept(sample);
			}
			return target;
		}

		/* has to be the long way around because:
		 * - can't directly pass an instance of type E to a collection of type ? extends E.
		 * - data's equal element instances must be added, not samples'.
		 */
		samples.iterate(e ->
		{
			// local references to AIC fields
			final E[] data2 = data;
			final int size2 = size;
			final Equalator<? super E> equalator2 = equalator;

			for(int di = 0; di < size2; di++)
			{
				if(equalator2.equal(e, data2[di]))
				{
					return;
				}
			}
			target.accept(e);
		});
		return target;
	}

	public static final <E, C extends Consumer<? super E>> C rngIntersect(
		final E[]                             data     ,
		final int                             size     ,
		final int                             offset   ,
		final int                             length   ,
		final XGettingCollection<? extends E> samples  ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		final int d;
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return target;
		}
		final int bound = offset + length;

		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			final E[] array = AbstractSimpleArrayCollection.internalGetStorageArray(
				(AbstractSimpleArrayCollection<?>)samples
			);
			final int arrayLength = Jadoth.to_int(samples.size());
			da:
			for(int di = offset; di != bound; di += d)
			{
				final E element = data[di];
				for(int i = 0; i < arrayLength; i++)
				{
					if(equalator.equal(element, array[i]))
					{
						target.accept(element);
						continue da;
					}
				}
			}
			return target;
		}

		/* has to be the long way around because:
		 * - can't directly pass an instance of type E to a collection of type ? extends E.
		 * - data's equal element instances must be added, not samples'.
		 */
		final CachedSampleEquality<E> equalCurrentElement = new CachedSampleEquality<>(equalator);
		for(int di = offset; di != bound; di += d)
		{
			equalCurrentElement.sample = data[di];
			if(samples.containsSearched(equalCurrentElement))
			{
				target.accept(equalCurrentElement.sample);
			}
		}
		return target;
	}

	public static final <E, C extends Consumer<? super E>> C rngExcept(
		final E[]                             data     ,
		final int                             size     ,
		final int                             offset   ,
		final int                             length   ,
		final XGettingCollection<? extends E> samples  ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		final int d;
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return target;
		}
		final int bound = offset + length;

		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			final E[] array = AbstractSimpleArrayCollection.internalGetStorageArray(
				(AbstractSimpleArrayCollection<?>)samples
			);
			final int arrayLength = Jadoth.to_int(samples.size());
			da:
			for(int di = offset; di != bound; di += d)
			{
				final E element = data[di];
				for(int i = 0; i < arrayLength; i++)
				{
					if(equalator.equal(element, array[i]))
					{
						continue da;
					}
				}
				target.accept(element);
			}
			return target;
		}

		/* has to be the long way around because:
		 * - can't directly pass an instance of type E to a collection of type ? extends E.
		 * - data's equal element instances must be added, not samples'.
		 */
		final CachedSampleEquality<E> equalCurrentElement = new CachedSampleEquality<>(equalator);
		da:
		for(int di = offset; di != bound; di += d)
		{
			equalCurrentElement.sample = data[di];
			if(samples.containsSearched(equalCurrentElement))
			{
				continue da;
			}
			target.accept(equalCurrentElement.sample);
		}
		return target;
	}

	public static final <E, C extends Consumer<? super E>> C rngUnion(
		final E[]                             data     ,
		final int                             size     ,
		final int                             offset   ,
		final int                             length   ,
		final XGettingCollection<? extends E> samples  ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		final int d;
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return target;
		}
		final int bound = offset + length;

		rngCopyTo(data, size, offset, length, target);
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			final E[] array = AbstractSimpleArrayCollection.internalGetStorageArray(
				(AbstractSimpleArrayCollection<?>)samples
			);
			final int arrayLength = Jadoth.to_int(samples.size());
			ar:
			for(int i = 0; i < arrayLength; i++)
			{
				final E sample = array[i];
				for(int di = offset; di != bound; di += d)
				{
					if(equalator.equal(data[di], sample))
					{
						continue ar;
					}
				}
				target.accept(sample);
			}
			return target;
		}

		samples.iterate(e ->
		{
			// local references to AIC fields
			final E[] data2 = data;
			final int bound2 = bound;
			final Equalator<? super E> equalator2 = equalator;

			for(int di = offset; di != bound2; di += d)
			{
				if(equalator2.equal(e, data2[di]))
				{
					return;
				}
			}
			target.accept(e);
		});
		return target;
	}


	public static final <E, C extends Consumer<? super E>> C distinct(
		final E[] data,
		final int size,
		final C target
	)
	{
		final HashEnum<E> distincts = HashEnum.New();
		E element;
		for(int i = 0; i < size; i++)
		{
			if(distincts.containsId(element = data[i]))
			{
				continue;
			}
			distincts.add(element);
			target.accept(element);
		}
		return target;
	}

	public static final <E, C extends Consumer<? super E>> C rngDistinct(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final C target
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return target;
		}
		final int endIndex = offset + length - d;

		final HashEnum<E> distincts = HashEnum.New();
		E element;
		for(int i = offset - d; i != endIndex;)
		{
			if(distincts.containsId(element = data[i += d]))
			{
				continue;
			}
			distincts.add(element);
			target.accept(element);
		}
		return target;
	}

	@SuppressWarnings("unchecked")
	public static final <E, C extends Consumer<? super E>> C distinct(
		final E[] data,
		final int size,
		final C target,
		final Equalator<? super E> equalator
	)
	{
		final Object[] distincts = new Object[size]; // a set would have to fully iterate as well for a custom equalator
		int k = 0;

		mainLoop:
		for(int i = 0; i < size; i++)
		{
			final E element = data[i];
			for(int j = 0; j < k; j++)
			{
				if(equalator.equal((E)distincts[j], element))
				{
					continue mainLoop;
				}
			}
			distincts[k++] = element;
			target.accept(element);
		}

		return target;
	}

	@SuppressWarnings("unchecked")
	public static final <E, C extends Consumer<? super E>> C rngDistinct(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final C target,
		final Equalator<? super E> equalator
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return target;
		}
		final int endIndex = offset + length - d;

		final Object[] distincts = new Object[size]; // a set would have to fully iterate as well for a custom equalator
		int k = 0;

		mainLoop:
		for(int i = offset - d; i != endIndex;)
		{
			final E element = data[i += d];
			for(int j = 0; j < k; j++)
			{
				if(equalator.equal((E)distincts[j], element))
				{
					continue mainLoop;
				}
			}
			distincts[k++] = element;
			target.accept(element);
		}

		return target;
	}



	///////////////////////////////////////////////////////////////////////////
	//    executing     //
	/////////////////////

	public static final <E> void iterate(final E[] data, final int size, final IndexProcedure<? super E> procedure)
	{
		try
		{
			for(int i = 0; i < size; i++)
			{
				procedure.accept(data[i], i);
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	// (20.01.2013)FIXME: replace ALL " + = d" constructions with case distinction and constant iteration

	public static final <E> void rngIterate(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final IndexProcedure<? super E> procedure
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return;
		}
		try
		{
			final int endIndex = offset + length - d;
			// start i one before offset as (i += d) only works as pre~crement
			for(int i = offset - d; i != endIndex;)
			{
				procedure.accept(data[i += d], i);
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}




	///////////////////////////////////////////////////////////////////////////
	//    removing      //
	/////////////////////

	public static final <E> int rngRemove(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final E element
	)
	{
		final int start, bound;
		if(length >= 0)
		{
			if(length == 0)
			{
				return 0;
			}
			start = offset;
			bound = offset + length;
		}
		else if(length < 0)
		{
			bound = offset + 1;
			start = offset + length + 1;
		}
		else if(offset < 0 || offset >= size)
		{
			throw new IndexExceededException(size, offset);
		}
		else
		{
			return 0;
		}
		if(start < 0 || bound > size)
		{
			throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
		}
		return removeAllFromArray(data, start, bound, element);
	}

	public static final <E> int rngRemove(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final E sample,
		final Equalator<? super E> equalator
	)
	{
		final int start, bound;
		if(length >= 0)
		{
			if(length == 0)
			{
				return 0;
			}
			start = offset;
			bound = offset + length;
		}
		else if(length < 0)
		{
			bound = offset + 1;
			start = offset + length + 1;
		}
		else if(offset < 0 || offset >= size)
		{
			throw new IndexExceededException(size, offset);
		}
		else
		{
			return 0;
		}
		if(start < 0 || bound > size)
		{
			throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
		}
		return removeAllFromArray(data, start, bound, sample, equalator);
	}

	public static final <E> int rngRemoveNull(
		final E[] data,
		final int size,
		final int offset,
		final int length
	)
	{
		final int start, bound;
		if(length >= 0)
		{
			if(length == 0)
			{
				return 0;
			}
			start = offset;
			bound = offset + length;
		}
		else if(length < 0)
		{
			bound = offset + 1;
			start = offset + length + 1;
		}
		else if(offset < 0 || offset >= size)
		{
			throw new IndexExceededException(size, offset);
		}
		else
		{
			return 0;
		}
		if(start < 0 || bound > size)
		{
			throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
		}

		// shut up FindBugs ..
		return JadothArrays.removeAllFromArray(null, data, start, bound);
	}

	// reducing //

	public static final <E> int reduce(
		final E[]                  data        ,
		final int                  size        ,
		final Predicate<? super E> predicate   ,
		final E                    removeMarker
	)
	{
		if(size == 0)
		{
			return 0;
		}

		final int removeCount;
		int i = 0;
		try
		{
			while(i < size)
			{
				if(predicate.test(data[i]))
				{
					data[i] = removeMarker;
				}
				i++;
			}
		}
		finally
		{
			//even if predicate throws an execption, the remove markers have to be cleared
			removeCount = JadothArrays.removeAllFromArray(data, 0, size, removeMarker);
		}
		return removeCount;
	}

	public static final <E> int rngReduce(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Predicate<? super E> predicate,
		final E removeMarker
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		final int removeCount, removeStartIndex;
		int i = offset - d;
		try
		{
			while(i != endIndex)
			{
				if(predicate.test(data[i += d]))
				{
					data[i] = removeMarker;
				}
			}
		}
		finally
		{
			if(d < 0)
			{
				removeStartIndex = i;
				i = offset;
			}
			else
			{
				removeStartIndex = offset;
			}
			removeCount = removeAllFromArray(data, removeStartIndex, ++i, removeMarker);
		}
		return removeCount;
	}

	// retaining //

	public static final <E> int retainAll(
		final E[] data,
		final int size,
		final XGettingCollection<E> elements,
		final E removeMarker
	)
	{
		if(Jadoth.to_int(elements.size()) == 0)
		{
			// effectively clear the array, return size as remove count.
			for(int i = size; i-- > 0;)
			{
				data[i] = null;
			}
			return size;
		}

		final int removeCount;
		final int lastIndex = size - 1;
		int i = -1;
		try
		{
			while(i < lastIndex)
			{
				if(!elements.containsId(data[++i]))
				{
					data[i] = removeMarker;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		finally
		{
			removeCount = JadothArrays.removeAllFromArray(data, 0, ++i, removeMarker);
		}
		return removeCount;
	}

	public static final <E> int rngRetainAll(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final XGettingCollection<E> elements,
		final E removeMarker
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		final int removeCount, removeStartIndex;
		final boolean removeNulls = !elements.nullContained();
		int i = offset - d;
		try
		{
			while(i != endIndex)
			{
				final E e;
				if((e = data[i += d]) == null)
				{
					if(removeNulls)
					{
						data[i] = removeMarker;
					}
					continue;
				}
				if(!elements.containsId(e))
				{
					data[i] = removeMarker;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		finally
		{
			if(d < 0)
			{
				removeStartIndex = i;
				i = offset;
			}
			else
			{
				removeStartIndex = offset;
			}
			removeCount = removeAllFromArray(data, removeStartIndex, ++i, removeMarker);
		}
		return removeCount;
	}

	public static final <E> int retainAll(
		final E[] data,
		final int size,
		final XGettingCollection<E> samples,
		final Equalator<? super E> equalator,
		final E removeMarker
	)
	{
		if(Jadoth.to_int(samples.size()) == 0)
		{
			// effectively clear the array, return size as remove count.
			for(int i = size; i-- > 0;)
			{
				data[i] = null;
			}
			return size;
		}

		final int lastIndex = size - 1;
		final int removeCount;
		int i = -1;
		try
		{
			while(i < lastIndex)
			{
				if(!samples.containsSearched(JadothPredicates.predicate(data[++i], equalator)))
				{
					data[i] = removeMarker;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		finally
		{
			removeCount = JadothArrays.removeAllFromArray(data, 0, ++i, removeMarker);
		}
		return removeCount;
	}

	public static final <E> int rngRetainAll(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final XGettingCollection<E> samples,
		final Equalator<? super E> equalator,
		final E removeMarker
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		final int removeCount, removeStartIndex;
		int i = offset - d;
		try
		{
			while(i != endIndex)
			{
				// let equalator decide on every element (even multiple nulls)
				if(!samples.containsSearched(JadothPredicates.predicate(data[i += d], equalator)))
				{
					data[i] = removeMarker;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		finally
		{
			if(d < 0)
			{
				removeStartIndex = i;
				i = offset;
			}
			else
			{
				removeStartIndex = offset;
			}
			removeCount = removeAllFromArray(data, removeStartIndex, ++i, removeMarker);
		}
		return removeCount;
	}

	// processing //

	public static final <E> int process(
		final E[] data,
		final int size,
		final Consumer<? super E> procedure,
		final E removeMarker
	)
	{
		final int lastIndex = size - 1;
		int removeCount = 0;
		int i = -1;
		try
		{
			while(i != lastIndex)
			{
				procedure.accept(data[++i]);
				data[i] = removeMarker;
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		finally
		{
			removeCount = removeAllFromArray(data, 0, ++i, removeMarker);
		}
		return removeCount;
	}

	public static final <E> int rngProcess(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Consumer<? super E> procedure,
		final E removeMarker
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int i = offset - d; //start i one before offset as (i += d) only works as pre~crement
		final int removeCount;
		try
		{
			while(i != endIndex)
			{
				procedure.accept(data[i += d]);
				data[i] = removeMarker;
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		finally
		{
			final int removeStartIndex;
			if(d < 0)
			{
				removeStartIndex = i;
				i = offset;
			}
			else
			{
				removeStartIndex = offset;
			}
			removeCount = removeAllFromArray(data, removeStartIndex, ++i, removeMarker);
		}
		return removeCount;
	}

	// moving //

	public static final <E> int moveTo(
		final E[] data,
		final int size,
		final Consumer<? super E> target,
		final Predicate<? super E> predicate,
		final E removeMarker
	)
	{
		final int lastIndex = size - 1;
		final int removeCount;
		int i = -1;
		try
		{
			while(i < lastIndex)
			{
				if(predicate.test(data[++i]))
				{
					target.accept(data[i]);
					data[i] = removeMarker;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		finally
		{
			//can't return until remove markers are cleared, so do this in any case
			removeCount = JadothArrays.removeAllFromArray(data, 0, ++i, removeMarker);
		}
		return removeCount;
	}

	public static final <E> int rngMoveTo(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Consumer<? super E> target,
		final Predicate<? super E> predicate,
		final E removeMarker
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int i = offset - d;
		final int removeCount;
		try
		{
			while(i != endIndex)
			{
				if(predicate.test(data[i += d]))
				{
					target.accept(data[i]);
					data[i] = removeMarker;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		finally
		{
			final int removeStartIndex;
			if(d < 0)
			{
				removeStartIndex = i;
				i = offset;
			}
			else
			{
				removeStartIndex = offset;
			}
			removeCount = removeAllFromArray(data, removeStartIndex, ++i, removeMarker);
		}
		return removeCount;
	}

	public static final <E> int moveSelection(
		final E[] data,
		final int size,
		final long[] indices,
		final Consumer<? super E> target,
		final E removeMarker
	)
	{
		final int length;
		if((length = indices.length) == 0)
		{
			return 0;
		}

		// validate all indices before moving the first element
		long min, max = min = indices[0];
		long idx;
		for(int i = 1; i < length; i++)
		{
			if((idx = indices[i]) < min)
			{
				min = idx;
			}
			else if(idx > max)
			{
				max = idx;
			}
		}
		if(min < 0)
		{
			throw new IndexExceededException(size, min);
		}
		if(max >= size)
		{
			throw new IndexExceededException(size, max);
		}

		final int removeCount;
		// actual moving
		try
		{
			for(int i = 0, j; i < length; i++)
			{
				target.accept(data[j = (int)indices[i]]); // manual loop to spare temporary variable
				data[j] = removeMarker;
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		finally
		{
			removeCount = JadothArrays.removeAllFromArray(data, (int)min, (int)++max, removeMarker);
		}
		return removeCount;
	}

	// removing - multiple all //

	public static final <E> int rngRemoveAll(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final XGettingCollection<? extends E> elements
	)
	{
		final int start, bound;
		if(length >= 0)
		{
			if(length == 0)
			{
				return 0;
			}
			start = offset;
			bound = offset + length;
		}
		else if(length < 0)
		{
			bound = offset + 1;
			start = offset + length + 1;
		}
		else if(offset < 0 || offset >= size)
		{
			throw new IndexExceededException(size, offset);
		}
		else
		{
			return 0;
		}
		if(start < 0 || bound > size)
		{
			throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
		}
		return removeAllFromArray(elements, data, start, bound);
	}

	public static final <E> int rngRemoveAll(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final E removeMarker
	)
	{
		final int start, bound;
		if(length >= 0)
		{
			if(length == 0)
			{
				return 0;
			}
			start = offset;
			bound = offset + length;
		}
		else if(length < 0)
		{
			bound = offset + 1;
			start = offset + length + 1;
		}
		else if(offset < 0 || offset >= size)
		{
			throw new IndexExceededException(size, offset);
		}
		else
		{
			return 0;
		}
		if(start < 0 || bound > size)
		{
			throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
		}

		if(Jadoth.to_int(samples.size()) == 0)
		{
			return 0;
		}
		return removeAllFromArray(data, start, bound, samples, equalator);
	}


	// removing - duplicates //

	public static final <E> int removeDuplicates(
		final E[] data,
		final int size,
		final Equalator<? super E> equalator,
		final E removeMarker
	)
	{
		return rngRemoveDuplicates(data, size, 0, size, equalator, removeMarker);
	}

	public static final <E> int rngRemoveDuplicates(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Equalator<? super E> equalator,
		final E removeMarker
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		final int removeCount;
		int i = offset - d;
		int removeStartIndex = i;
		try
		{
			while(i != endIndex)
			{
				final E ei = data[i += d];
				for(int j = i + d; j != endIndex;)
				{
					final E ej = data[j += d];
					if(ej == removeMarker)
					{
						continue;
					}
					if(equalator.equal(ei, ej))
					{
						data[j] = removeMarker;
					}
				}
			}
		}
		finally
		{
			if(d < 0)
			{
				final int temp = removeStartIndex;
				removeStartIndex = i;
				i = temp;
			}
			removeCount = removeAllFromArray(data, removeStartIndex, ++i, removeMarker);
		}
		return removeCount;
	}

	public static final <E> int removeDuplicates(
		final E[] data,
		final int size,
		final E removeMarker
	)
	{
		return rngRemoveDuplicates(data, size, 0, size, removeMarker);
	}

	public static final <E> int rngRemoveDuplicates(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final E removeMarker
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		final int removeCount;
		int i = offset - d;
		int removeStartIndex = i;
		try
		{
			while(i != endIndex)
			{
				final Object ei = data[i += d];
				if(ei == removeMarker)
				{
					continue;
				}
				for(int j = i; j != endIndex;)
				{
					final Object ej;
					if((ej = data[j += d]) == removeMarker)
					{
						continue;
					}
					if(ei == ej)
					{
						data[j] = removeMarker;
					}
				}
			}
		}
		finally
		{
			if(d < 0)
			{
				final int temp = removeStartIndex;
				removeStartIndex = i;
				i = temp;
			}
			removeCount = removeAllFromArray(data, removeStartIndex, ++i, removeMarker);
		}
		return removeCount;
	}

	// removing - indexed //

	public static final <E> int removeSelection(
		final E[] data,
		final int size,
		final long[] indices,
		final E removeMarker
	)
	{
		final int length;
		if((length = indices.length) == 0)
		{
			return 0;
		}

		// validate all indices before moving the first element
		long idx, min, max = min = indices[0];
		for(int i = 1; i < length; i++)
		{
			if((idx = indices[i]) < min)
			{
				min = idx;
			}
			else if(idx > max)
			{
				max = idx;
			}
		}
		if(min < 0)
		{
			throw new IndexExceededException(size, min);
		}
		if(max >= size)
		{
			throw new IndexExceededException(size, max);
		}

		for(int i = 0; i < length; i++)
		{
			data[(int)indices[i]] = removeMarker;
		}
		
		// actual moving
		final int removeCount = JadothArrays.removeAllFromArray(data, (int)min, (int)++max, removeMarker);
		
		return removeCount;
	}

	public static final <E> int removeRange(
		final E[] data,
		final int size,
		final int offset,
		final int length
	)
	{
		final int start, bound;
		if(length >= 0)
		{
			if(length == 0)
			{
				return 0;
			}
			start = offset;
			bound = offset + length;
		}
		else if(length < 0)
		{
			bound = offset + 1;
			start = offset + length + 1;
		}
		else if(offset < 0 || offset >= size)
		{
			throw new IndexExceededException(size, offset);
		}
		else
		{
			return 0;
		}
		if(start < 0 || bound > size)
		{
			throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
		}

		System.arraycopy(data, bound, data, start, size - bound);

		// free old array buckets
		for(int i = size - length; i < size; i++)
		{
			data[i] = null;
		}
		return length;
	}

	public static final <E> int retainRange(
		final E[] data,
		final int size,
		final int offset,
		final int length
	)
	{
		if(offset < 0 || offset >= size)
		{
			throw new IndexExceededException(size, offset);
		}
		if(length < 0)
		{
			throw new IllegalArgumentException();
		}
		if(offset + length > size)
		{
			throw new IndexExceededException(size, offset + length + 1);
		}
		if(offset == 0 && length == size)
		{
			return 0;
		}

		System.arraycopy(data, offset, data, 0, length);

		// free old array buckets
		for(int i = length; i < size; i++)
		{
			data[i] = null;
		}
		return length;
	}

	public static final <E> E retrieve(
		final E[] data,
		      int size,
		final E element,
		final E notFoundMarker
	)
	{
		for(int i = 0; i < size; i++)
		{
			if(data[i] == element)
			{
				if(i < --size)
				{
					System.arraycopy(data, i + 1, data, i, size - i);
				}
				data[size] = null;
				return element;
			}
		}
		return notFoundMarker;
	}

	public static final <E> E retrieve(
		final E[] data,
		      int size,
		final E sample,
		final Equalator<? super E> equalator,
		final E notFoundMarker
	)
	{
		for(int i = 0; i < size; i++)
		{
			if(equalator.equal(data[i], sample))
			{
				final E oldElement = data[i];
				if(i < --size)
				{
					System.arraycopy(data, i + 1, data, i, size - i);
				}
				data[size] = null;
				return oldElement;
			}
		}
		return notFoundMarker;
	}

	public static final <E> E retrieve(
		final E[] data,
		      int size,
		final Predicate<? super E> predicate,
		final E notFoundMarker
	)
	{
		for(int i = 0; i < size; i++)
		{
			if(predicate.test(data[i]))
			{
				final E oldElement = data[i];
				if(i < --size)
				{
					System.arraycopy(data, i + 1, data, i, size - i);
				}
				data[size] = null;
				return oldElement;
			}
		}
		return notFoundMarker;
	}

	public static final <E> boolean removeOne(final E[] data, int size, final E element)
	{
		for(int i = 0; i < size; i++)
		{
			if(data[i] == element)
			{
				if(i < --size)
				{
					System.arraycopy(data, i + 1, data, i, size - i);
				}
				data[size] = null;
				return true;
			}
		}
		return false;
	}

	public static final <E> boolean removeOne(
		final E[]                  data     ,
		      int                  size     ,
		final E                    sample   ,
		final Equalator<? super E> equalator
	)
	{
		for(int i = 0; i < size; i++)
		{
			if(equalator.equal(data[i], sample))
			{
				if(i < --size)
				{
					System.arraycopy(data, i + 1, data, i, size - i);
				}
				data[size] = null;
				return true;
			}
		}
		return false;
	}

	public static final <E> E rngRetrieve(
		final E[] data,
		      int size,
		final int offset,
		final int length,
		final E element,
		final E notFoundMarker
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return null;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			if(element == data[i += d])
			{
				if(i < --size)
				{
					System.arraycopy(data, i + 1, data, i, size - i);
				}
				data[size] = null;
				return element;
			}
		}
		return notFoundMarker;
	}

	public static final <E> E rngRetrieve(
		final E[] data,
		      int size,
		final int offset,
		final int length,
		final E sample,
		final Equalator<? super E> equalator,
		final E notFoundMarker
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return null;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			if(equalator.equal(data[i += d], sample))
			{
				final E oldElement = data[i];
				if(i < --size)
				{
					System.arraycopy(data, i + 1, data, i, size - i);
				}
				data[size] = null;
				return oldElement;
			}
		}
		return notFoundMarker;
	}

	public static final <E> E rngRetrieve(
		final E[] data,
		      int size,
		final int offset,
		final int length,
		final Predicate<? super E> predicate,
		final E notFoundMarker
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return null;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			if(predicate.test(data[i += d]))
			{
				final E oldElement = data[i];
				if(i < --size)
				{
					System.arraycopy(data, i + 1, data, i, size - i);
				}
				data[size] = null;
				return oldElement;
			}
		}
		return notFoundMarker;
	}

	public static final <E> boolean rngRemoveOne(
		final E[] data,
		      int size,
		final int offset,
		final int length,
		final E element
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return false;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			if(element == data[i += d])
			{
				if(i < --size)
				{
					System.arraycopy(data, i + 1, data, i, size - i);
				}
				data[size] = null;
				return true;
			}
		}
		return false;
	}

	public static final <E> boolean rngRemoveOne(
		final E[] data,
		      int size,
		final int offset,
		final int length,
		final E sample,
		final Equalator<? super E> equalator
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return false;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			if(equalator.equal(data[i += d], sample))
			{
				if(i < --size)
				{
					System.arraycopy(data, i + 1, data, i, size - i);
				}
				data[size] = null;
				return true;
			}
		}
		return false;
	}



	///////////////////////////////////////////////////////////////////////////
	//    replacing     //
	/////////////////////

	// replacing - single //

	public static final <E> boolean replaceOne(
		final E[] data,
		final int size,
		final E oldElement,
		final E newElement
	)
	{
		for(int i = 0; i < size; i++)
		{
			if(data[i] == oldElement)
			{
				data[i] = newElement;
				return true;
			}
		}
		return false;
	}

	public static final <E> int rngReplaceOne(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final E oldElement,
		final E newElement
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return -1;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			if(oldElement == data[i += d])
			{
				data[i] = newElement;
				return i;
			}
		}

		return -1;
	}

	public static final <E> boolean replaceOne(
		final E[] data,
		final int size,
		final E sample,
		final E newElement,
		final Equalator<? super E> equalator
	)
	{
		for(int i = 0; i < size; i++)
		{
			if(equalator.equal(data[i], sample))
			{
				data[i] = newElement;
				return true;
			}
		}
		return false;
	}

	public static final <E> int rngReplaceOne(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final E sample,
		final E newElement,
		final Equalator<? super E> equalator
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return -1;
		}
		try
		{
			final int endIndex = offset + length - d;
			for(int i = offset - d; i != endIndex;)
			{
				if(equalator.equal(
					data[i += d],
					sample))
				{
					data[i] = newElement;
					return i;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return -1;
	}

	public static final <E> boolean substituteOne(
		final E[] data,
		final int size,
		final Predicate<? super E> predicate,
		final E replacement
	)
	{
		try
		{
			for(int i = 0; i < size; i++)
			{
				if(predicate.test(data[i]))
				{
					data[i] = replacement;
					return true;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return false;
	}

	public static final <E> int rngReplaceOne(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Predicate<? super E> predicate,
		final E newElement
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return -1;
		}
		try
		{
			final int endIndex = offset + length - d;
			for(int i = offset - d; i != endIndex;)
			{
				if(predicate.test(data[i += d]))
				{
					data[i] = newElement;
					return i;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return -1;
	}

	// replacing - multiple //

	public static final <E> int replace(
		final E[] data,
		final int size,
		final E oldElement,
		final E newElement
	)
	{
		int replaceCount = 0;
		for(int i = 0; i < size; i++)
		{
			if(data[i] == oldElement)
			{
				data[i] = newElement;
				replaceCount++;
			}
		}
		return replaceCount;
	}

	public static final <E> int rngReplace(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final E oldElement,
		final E newElement
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int replaceCount = 0;
		for(int i = offset - d; i != endIndex;)
		{
			if(data[i += d] == oldElement)
			{
				data[i] = newElement;
				replaceCount++;
			}
		}
		return replaceCount;
	}

	public static final <E> int replace(
		final E[] data,
		final int size,
		final E sample,
		final E newElement,
		final Equalator<? super E> equalator
	)
	{
		int replaceCount = 0;
		try
		{
			for(int i = 0; i < size; i++)
			{
				if(equalator.equal(
					data[i],
					sample))
				{
					data[i] = newElement;
					replaceCount++;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return replaceCount;
	}

	public static final <E> int rngReplace(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final E sample,
		final E newElement,
		final Equalator<? super E> equalator
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int replaceCount = 0;
		for(int i = offset - d; i != endIndex;)
		{
			if(equalator.equal(data[i += d], sample))
			{
				data[i] = newElement;
				replaceCount++;
			}
		}
		return replaceCount;
	}

	public static final <E> int substitute(
		final E[] data,
		final int size,
		final Predicate<? super E> predicate,
		final E newElement
	)
	{
		int replaceCount = 0;
		try
		{
			for(int i = 0; i < size; i++)
			{
				if(predicate.test(data[i]))
				{
					data[i] = newElement;
					replaceCount++;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return replaceCount;
	}

	public static final <E> int rngReplace(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Predicate<? super E> predicate,
		final E newElement
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		int replaceCount = 0;
		try
		{
			final int endIndex = offset + length - d;
			for(int i = offset - d; i != endIndex;)
			{
				if(predicate.test(data[i += d]))
				{
					data[i] = newElement;
					replaceCount++;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return replaceCount;
	}

	// replacing - multiple all //

	public static final <E, U extends E> int replaceAll(
		final E[] data,
		final int size,
		final XGettingCollection<U> oldElements,
		final E newElement,
		final E marker
	)
	{
		final int replaceCount;
		try
		{
			oldElements.iterate(e ->
			{
				final E[] data1 = data; // data is actually a field here
				for(int i = 0, len = size; i < len; i++)
				{
					// as is size
					if(data1[i] == e)
					{
						data1[i] = marker;
						break;
					}
				}
			});
		}
		finally
		{
			replaceCount = JadothArrays.replaceAllInArray(data, 0, size, marker, newElement);
		}
		return replaceCount;
	}

	public static final <E> int rngReplaceAll(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final XGettingCollection<E> oldElements,
		final E newElement
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int replaceCount = 0;
		for(int i = offset - d; i != endIndex;)
		{
			if(oldElements.containsId(data[i += d]))
			{
				data[i] = newElement;
				replaceCount++;
			}
		}
		return replaceCount;
	}

	public static final <E, U extends E> int replaceAll(
		final E[] data,
		final int size,
		final XGettingCollection<U> samples,
		final E newElement,
		final Equalator<? super E> equalator,
		final E marker
	)
	{
		final int replaceCount;
		try
		{
			samples.iterate(e ->
			{
				final E[] data1 = data; // data is actually a field here
				for(int i = 0, len = size; i < len; i++)
				{
					// as is size
					if(equalator.equal(data1[i], e))
					{
						data1[i] = marker;
						break;
					}
				}
			});
		}
		finally
		{
			replaceCount = JadothArrays.replaceAllInArray(data, 0, size, marker, newElement);
		}
		return replaceCount;
	}

	public static final <E> int rngReplaceAll(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final XGettingCollection<E> samples,
		final E newElement,
		final Equalator<? super E> equalator
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int replaceCount = 0;
		for(int i = offset - d; i != endIndex;)
		{
			if(samples.containsSearched(JadothPredicates.predicate(data[i += d], equalator)))
			{
				data[i] = newElement;
				replaceCount++;
			}
		}
		return replaceCount;
	}

	// replacing - mapped //

	public static final <E> int replaceAll(final E[] data, final int size, final XMap<E, E> replacementMapping)
	{
		int replaceCount = 0;
		for(int i = 0; i < size; i++)
		{
			final E replacement;
			if((replacement = replacementMapping.get(data[i])) != null)
			{
				data[i] = replacement;
				replaceCount++;
			}
		}
		return replaceCount;
	}

	public static final <E> int modify(final E[] data, final int size, final Function<E, E> mapper)
	{
		int replaceCount = 0;
		for(int i = 0; i < size; i++)
		{
			if(data[i] != (data[i] = mapper.apply(data[i])))
			{
				// tricky :D
				replaceCount++;
			}
		}
		return replaceCount;
	}

	public static final <E> int modify(
		final E[] data,
		final int size,
		final Predicate<? super E> predicate,
		final Function<E, E> mapper
	)
	{
		int replaceCount = 0;
		try
		{
			for(int i = 0; i < size; i++)
			{
				if(predicate.test(data[i]))
				{
					data[i] = mapper.apply(data[i]);
					replaceCount++;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return replaceCount;
	}

	public static final <E> int rngReplaceAll(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final XMap<E, E> replacementMapping
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int replaceCount = 0;
		for(int i = offset - d; i != endIndex;)
		{
			final E replacement;
			if((replacement = replacementMapping.get(data[i += d])) != null)
			{
				data[i] = replacement;
				replaceCount++;
			}
		}
		return replaceCount;
	}

	public static final <E> int rngModify(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Function<E, E> mapper
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return 0;
		}
		final int endIndex = offset + length - d;

		int replaceCount = 0;
		for(int i = offset - d; i != endIndex;)
		{
			if(data[i += d] != (data[i] = mapper.apply(data[i])))
			{
				// setting array right away is faster than buffer var.
				replaceCount++;
			}
		}
		return replaceCount;
	}



	///////////////////////////////////////////////////////////////////////////
	//     setting      //
	/////////////////////

	public static final <E> void swap(
		final E[] data,
		final int size,
		final int indexA,
		final int indexB
	)
		throws IndexOutOfBoundsException, ArrayIndexOutOfBoundsException
	{
		if(indexA >= size)
		{
			throw new IndexExceededException(size, indexA);
		}
		if(indexB >= size)
		{
			throw new IndexExceededException(size, indexB);
		}
		final E t = data[indexA];
		data[indexA] = data[indexB];
		data[indexB] = t;
	}

	public static final <E> void swap(
		final E[] data,
		final int size,
		      int indexA,
		      int indexB,
		final int length
	)
	{
		if(length == 0 || indexA == indexB)
		{
			return;
		}
		else if(indexA > indexB)
		{
			final int t = indexA;
			indexA = indexB;
			indexB = t;
		}

		final int bound;
		if(indexA < 0 || length < 0 || (bound = indexA + length) >= indexB || indexB + length >= size)
		{
			throw new IndexOutOfBoundsException(exceptionIllegalSwapBounds(size, indexA, indexB, length));
		}

		while(indexA < bound)
		{
			final E t = data[indexA];
			data[indexA++] = data[indexB];
			data[indexB++] = t;
		}
	}

	public static final <E> void reverse(
		final E[] data,
		final int size
	)
	{
		final int halfSize = size >> 1;
		for(int i = 0, j = size - 1; i < halfSize; i++, j--)
		{
			final E element = data[i];
			data[i] = data[j];
			data[j] = element;
		}
	}

	public static final <E> void rngReverse(
		final E[] data,
		final int size,
		final int offset,
		final int length
	)
	{
		int low, high;
		if(length >= 0)
		{
			if((low = offset) < 0 || (high = offset + length - 1) >= size)
			{
				throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
			}
			if(length == 0)
			{
				return;
			}
		}
		else if(length < 0)
		{
			if((low = offset + length + 1) < 0 || (high = offset) >= size)
			{
				throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
			}
		}
		else if(offset < 0 || offset >= size)
		{
			throw new IndexExceededException(size, offset);
		}
		else
		{
			return; // handle length 0 special case not as escape condition but as last case to ensure index checking
		}

		while(low < high)
		{
			final E element = data[low];
			data[low++] = data[high];
			data[high--] = element;
		}
	}

	// direct setting //

	public static final <E> void set(
		final E[] data,
		final int size,
		final int offset,
		final E[] elements
	)
	{
		if(offset < 0 || offset + elements.length > size)
		{
			throw new IndexOutOfBoundsException(exceptionRange(size, offset, offset + elements.length - 1));
		}
		System.arraycopy(elements, 0, data, offset, elements.length);
	}
	
	public static final <E> void set(
		final E[]                           data          ,
		final int                           size          ,
		final int                           offset        ,
		final XGettingSequence<? extends E> elements      ,
		final long                          elementsOffset,
		final long                          elementsLength
	)
	{
		if(offset < 0 || offset + elements.size() > size)
		{
			throw new IndexOutOfBoundsException(exceptionRange(size, offset, offset + elements.size() - 1));
		}
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME AbstractArrayStorage#set()
	}

	public static final <E> void set(
		final E[] data,
		final int size,
		final int offset,
		final E[] src,
		final int srcIndex,
		final int srcLength
	)
	{
		if(srcLength < 0)
		{
			if(offset < 0 || offset - srcLength > size)
			{
				throw new IndexOutOfBoundsException(exceptionRange(size, offset, offset - srcLength + 1));
			}
			if(srcIndex >= src.length)
			{
				throw new ArrayIndexOutOfBoundsException(srcIndex);
			}
			final int bound;
			if((bound = offset + srcLength) < -1)
			{
				throw new ArrayIndexOutOfBoundsException(bound + 1);
			}
			for(int s = srcIndex, i = offset; s > bound; s--)
			{
				data[i++] = src[s];
			}
			return;
		}

		if(offset < 0 || offset + srcLength > size)
		{
			throw new IndexOutOfBoundsException(exceptionRange(size, offset, offset + src.length - 1));
		}
		System.arraycopy(src, srcIndex, data, offset, srcLength);
	}

	public static final <E> void fill(
		final E[] data   ,
		final int size   ,
		final int offset ,
		final int length ,
		final E   element
	)
	{
		validateRange0toUpperBound(size, offset, length);
		if(length >= 0)
		{
			final int bound = offset + length; // because of faster "<" operator
			for(int i = offset; i < bound; i++)
			{
				data[i] = element;
			}
		}
		else
		{
			final int lastIndex = offset + length + 1; // because of faster " >= " operator
			for(int i = offset; i >= lastIndex; i--)
			{
				data[i] = element;
			}
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// VarString appending //
	//////////////////////

	public static final String toString(final Object[] data, final int size)
	{
		if(size == 0)
		{
			return "[]";
		}
		final VarString vc = VarString.New((int)(size * 2.0f)).append('[');
		for(int i = 0; i < size; i++)
		{
			vc.add(data[i]).add(',', ' ');
		}
		vc.deleteLast().setLast(']');
		return vc.toString();
	}

	public static final <E> VarString appendTo(final E[] data, final int size, final VarString vc)
	{
		for(int i = 0; i < size; i++)
		{
			vc.add(data[i]);
		}
		return vc;
	}

	public static final <E> VarString appendTo(
		final E[] data,
		final int size,
		final VarString vc,
		final char separator
	)
	{
		if(size == 0)
		{
			return vc;
		}
		for(int i = 0; i < size; i++)
		{
			vc.add(data[i]).append(separator);
		}
		vc.deleteLast();
		return vc;
	}

	public static final <E> VarString appendTo(
		final E[] data,
		final int size,
		final VarString vc,
		final String separator
	)
	{
		if(size == 0)
		{
			return vc;
		}
		else if(separator == null || separator.isEmpty())
		{
			for(int i = 0; i < size; i++)
			{
				vc.add(data[i]);
			}
		}
		else
		{
			final char[] sepp = separator.toCharArray();
			for(int i = 0; i < size; i++)
			{
				vc.add(data[i]).add(sepp);
			}
			vc.deleteLast(sepp.length);
		}
		return vc;
	}

	public static final <E> VarString appendTo(
		final E[] data,
		final int size,
		final VarString vc,
		final BiProcedure<VarString, ? super E> appender
	)
	{
		if(size == 0)
		{
			return vc;
		}
		for(int i = 0; i < size; i++)
		{
			appender.accept(vc, data[i]);
		}
		return vc;
	}

	public static final <E> VarString appendTo(
		final E[] data,
		final int size,
		final VarString vc,
		final BiProcedure<VarString, ? super E> appender,
		final char separator
	)
	{
		if(size == 0)
		{
			return vc;
		}
		for(int i = 0; i < size; i++)
		{
			appender.accept(vc, data[i]);
			vc.append(separator);
		}
		vc.deleteLast();
		return vc;
	}

	public static final <E> VarString appendTo(
		final E[] data,
		final int size,
		final VarString vc,
		final BiProcedure<VarString, ? super E> appender,
		final String separator
	)
	{
		if(size == 0)
		{
			return vc;
		}
		else if(separator == null || separator.isEmpty())
		{
			for(int i = 0; i < size; i++)
			{
				appender.accept(vc, data[i]);
			}
		}
		else
		{
			final char[] sepp = separator.toCharArray();
			for(int i = 0; i < size; i++)
			{
				appender.accept(vc, data[i]);
				vc.add(sepp);
			}
			vc.deleteLast(sepp.length);
		}
		return vc;
	}

	public static final <E> VarString rngAppendTo(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final VarString vc
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return vc;
		}
		final int endIndex = offset + length - d;
		for(int i = offset - d; i != endIndex;)
		{
			vc.add(data[i += d]);
		}
		return vc;
	}

	public static final <E> VarString rngAppendTo(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final VarString vc,
		final char separator)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return vc;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			vc.add(data[i += d]).append(separator);
		}
		vc.deleteLast();
		return vc;
	}

	public static final <E> VarString rngAppendTo(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final VarString vc,
		final String separator
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return vc;
		}
		final int endIndex = offset + length - d;

		if(separator == null || separator.isEmpty())
		{
			for(int i = offset - d; i != endIndex;)
			{
				vc.add(data[i += d]);
			}
		}
		else
		{
			final char[] sepp = separator.toCharArray();
			for(int i = offset - d; i != endIndex;)
			{
				vc.add(data[i += d]).add(sepp);
			}
			vc.deleteLast(sepp.length);
		}
		return vc;
	}

	public static final <E> VarString rngAppendTo(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final VarString vc,
		final BiProcedure<VarString, ? super E> appender
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return vc;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			appender.accept(vc, data[i += d]);
		}
		return vc;
	}

	public static final <E> VarString rngAppendTo(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final VarString vc,
		final BiProcedure<VarString, ? super E> appender,
		final char separator
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return vc;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			appender.accept(vc, data[i += d]);
			vc.append(separator);
		}
		vc.deleteLast();
		return vc;
	}

	public static final <E> VarString rngAppendTo(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final VarString vc,
		final BiProcedure<VarString, ? super E> appender,
		final String separator
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return vc;
		}
		final int endIndex = offset + length - d;

		if(separator == null || separator.isEmpty())
		{
			for(int i = offset - d; i != endIndex;)
			{
				appender.accept(vc, data[i += d]);
			}
		}
		else
		{
			final char[] sepp = separator.toCharArray();
			for(int i = offset - d; i != endIndex;)
			{
				appender.accept(vc, data[i += d]);
				vc.add(sepp);
			}
			vc.deleteLast(sepp.length);
		}
		return vc;
	}



	///////////////////////////////////////////////////////////////////////////
	//     sorting       //
	//////////////////////

	public static final <E> boolean isSorted(
		final E[] data,
		final int size,
		final Comparator<? super E> comparator
	)
	{
		if(size <= 1)
		{
			return true;
		}
		E loopLastElement = data[0];
		for(int i = 1; i < size; i++)
		{
			final E element;
			if(comparator.compare(loopLastElement, element = data[i]) > 0)
			{
				return false;
			}
			loopLastElement = element;
		}
		return true;
	}

	public static final <E> boolean rngIsSorted(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Comparator<? super E> comparator
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return true;
		}
		final int endIndex = offset + length - d;

		E loopLastElement = data[offset];
		for(int i = offset - d; i != endIndex;)
		{
			final E element;
			if(comparator.compare(loopLastElement, element = data[i += d]) > 0)
			{
				return false;
			}
			loopLastElement = element;
		}
		return true;
	}

	public static final <E> void sortInsertion(
		final E[] data,
		final int size,
		final Comparator<? super E> comparator
	)
	{
		for(int i = 0; i < size; i++)
		{
			for(int j = i; j != 0 && comparator.compare(data[j - 1], data[j]) > 0; j--)
			{
				final E t = data[j];
				data[j] = data[j - 1];
				data[j - 1] = t;
			}
		}
	}

	public static final <E> void rngSortInsertion(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Comparator<? super E> comparator
	)
	{
		validateRange0toUpperBound(size, offset, length);
		if(length >= 0)
		{
			internalSortInsertion(data, offset, offset + length, comparator);
		}
		else
		{
			internalSortInsertion(data, offset + length + 1, offset + 1, comparator);
		}
	}

	static final <E> void internalSortInsertion(
		final E[] data,
		final int low,
		final int bound,
		final Comparator<? super E> comparator
	)
	{
		for(int i = low; i < bound; i++)
		{
			for(int j = i; j != 0 && comparator.compare(data[j - 1], data[j]) > 0; j--)
			{
				final E t = data[j];
				data[j] = data[j - 1];
				data[j - 1] = t;
			}
		}
	}

	public static final <E> void rngSortQuick(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Comparator<? super E> comparator
	)
	{
		validateRange0toUpperBound(size, offset, length);
		if(length >= 0)
		{
			JadothSort.quicksort(data, offset, offset + length, comparator);
		}
		else
		{
			JadothSort.quicksort(data, offset + length + 1, offset + 1, comparator);
		}
	}

	public static final <E> void rngSortMerge(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Comparator<? super E> comparator
	)
	{
		final int endIndex; // bi-directional index movement
		if(length > 0)
		{
			if(offset < 0 || (endIndex = offset + length - 1) >= size)
			{
				throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
			}
			JadothSort.mergesort(data, offset, endIndex + 1, comparator);
		}
		else if(length < 0)
		{
			if((endIndex = offset + length + 1) < 0 || offset >= size)
			{
				throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
			}
			JadothSort.mergesort(data, endIndex, offset + 1, comparator);
		}
		else if(offset < 0 || offset >= size)
		{
			throw new IndexExceededException(size, offset);
		}
	}

	public static final <E> void rngSort(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Comparator<? super E> comparator
	)
	{
		// copy of #rngSortMerge()
		final int endIndex; // bi-directional index movement
		if(length > 0)
		{
			if(offset < 0 || (endIndex = offset + length - 1) >= size)
			{
				throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
			}
			JadothSort.mergesort(data, offset, endIndex + 1, comparator);
		}
		else if(length < 0)
		{
			if((endIndex = offset + length + 1) < 0 || offset >= size)
			{
				throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
			}
			JadothSort.mergesort(data, endIndex, offset + 1, comparator);
		}
		else if(offset < 0 || offset >= size)
		{
			throw new IndexExceededException(size, offset);
		}
	}

	public static final <E> void shuffle(final E[] data, final int size)
	{
		final FastRandom random = new FastRandom();
		for(int i = size, j; i > 1; i--)
		{
			final E t = data[i - 1];
			data[i - 1] = data[j = random.nextInt(i)];
			data[j] = t;
		}
	}

	public static final <E> void rngShuffle(
		final E[] data,
		final int size,
		final int offset, final int length
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return;
		}
		final int endIndex = offset + length - d;

		final Random r = JadothMath.random();
		for(int i = offset - d, j; i != endIndex;)
		{
			final E t = data[i += d];
			data[i] = data[j = r.nextInt(i)];
			data[j] = t;
		}
	}

	public static final <T> T[] toReversed(final T[] array, final int size)
	{
		final T[] rArray = JadothArrays.newArrayBySample(array, size);
		for(int i = 0, r = size; i < size; i++)
		{
			rArray[--r] = array[i];
		}
		return rArray;
	}


	public static final <E> boolean hasDistinctValues(final E[] data, final int size)
	{
		final HashEnum<E> uniques = HashEnum.NewCustom(size);
		for(int i = 0; i < size; i++)
		{
			final E element = data[i];
			if(uniques.containsId(element))
			{
				return false;
			}
			uniques.add(element);
		}

		return true;
	}

	public static final <E> boolean rngHasUniqueValues(
		final E[] data,
		final int size,
		final int offset,
		final int length
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return true;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			final Object element = data[i += d];
			for(int j = i; j != endIndex;)
			{
				if(data[j += d] == element)
				{
					return false;
				}
			}
		}
		return true;
	}

	public static final <E> boolean hasDistinctValues(
		final E[]                  data     ,
		final int                  size     ,
		final Equalator<? super E> equalator
	)
	{
		for(int i = 0; i < size; i++)
		{
			final E element = data[i];
			for(int j = i + 1; j < size; j++)
			{
				if(equalator.equal(element, data[j]))
				{
					return false;
				}
			}
		}
		return true;
	}

	public static final <E> boolean rngHasUniqueValues(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Equalator<? super E> equalator
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return true;
		}
		final int endIndex = offset + length - d;

		for(int i = offset - d; i != endIndex;)
		{
			final E element = data[i += d];
			for(int j = i; j != endIndex;)
			{
				if(equalator.equal(element, data[j += d]))
				{
					return false;
				}
			}
		}
		return true;
	}



	@SuppressWarnings("unchecked")
	public static final <E> boolean equalsContent(
		final E[] data,
		final int size,
		final Collection<? extends E> collection,
		final Equalator<? super E> equalator
	)
	{
		if(collection instanceof AbstractSimpleArrayCollection<?>)
		{
			return JadothArrays.equals(
				data,
				0,
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)collection),
				0,
				size,
				(Equalator<Object>)equalator
			);
		}

		int i = 0;
		for(final Iterator<? extends E> itr = collection.iterator(); itr.hasNext();)
		{
			if(!equalator.equal(data[i++], itr.next()))
			{
				return false;
			}
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	public static final <E> boolean equalsContent(
		final E[]                             data     ,
		final int                             size     ,
		final XGettingCollection<? extends E> samples  ,
		final Equalator<? super E>            equalator
	)
	{
		if(size != Jadoth.to_int(samples.size()))
		{
			return false; // content can only be equal if sizes are equal
		}

		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			return JadothArrays.equals(
				data,
				0,
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples),
				0,
				size,
				(Equalator<Object>)equalator
			);
		}

		return samples.applies(new Predicate<E>()
		{
			private int i;

			@Override
			public boolean test(final E e)
			{
				return equalator.equal(data[this.i++], e); // note that both sizes are equal, so this is safe
			}
		});
	}

	@SuppressWarnings("unchecked")
	public static final <E> boolean rngEqualsContent(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final List<? extends E> list,
		final Equalator<? super E> equalator
	)
	{
		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return list.size() == 0;
		}
		final int endIndex = offset + length - d;

		if(list == null || list.size() != (length < 0 ? -length : length))
		{
			return false;
		}

		if(list instanceof RandomAccess)
		{
			for(int i = offset - d, j = 0; i != endIndex;)
			{
				if(!equalator.equal(data[i += d], list.get(j++)))
				{
					return false;
				}
			}
		}
		else
		{
			int i = offset - d;
			for(final ListIterator<E> itr = ((List<E>)list).listIterator(); itr.hasNext();)
			{
				if(!equalator.equal(data[i += d], itr.next()))
				{
					return false;
				}
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public static final <E> boolean rngEqualsContent(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final XGettingSequence<? extends E> sequence,
		final Equalator<? super E> equalator
	)
	{
		if(size != Jadoth.to_int(sequence.size()))
		{
			return false; // content can only be equal if sizes are equal
		}

		if(sequence instanceof AbstractSimpleArrayCollection<?>)
		{
			return JadothArrays.equals(
				data,
				offset,
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)sequence),
				0,
				length,
				(Equalator<Object>)equalator
			);
		}

		final int d; // bi-directional index movement
		if((d = checkIterationDirection(size, offset, length)) == 0)
		{
			return Jadoth.to_int(sequence.size()) == 0;
		}

		if(d < 0)
		{
			return sequence.applies(new Predicate<E>()
			{
				private int i = offset;
				@Override
				public boolean test(final E e)
				{
					return equalator.equal(data[this.i--], e); // note that both sizes are equal, so this is safe
				}
			});
		}
		return sequence.applies(new Predicate<E>()
		{
			private int i = offset;
			@Override
			public boolean test(final E e)
			{
				return equalator.equal(data[this.i++], e); // note that both sizes are equal, so this is safe
			}
		});
	}

	public static final Object[] rngToArray(
		final Object[] data,
		final int size,
		final int offset,
		final int length
	)
	{
		if(offset < 0 || offset >= size)
		{
			throw new IndexExceededException(size, offset);
		}
		if(length == 0)
		{
			return new Object[0];
		}
		else if(length > 0)
		{
			final Object[] array = new Object[length];
			System.arraycopy(data, offset, array, 0, length);
			return array;
		}

		// reverse iteration direction
		final int boundIndex;
		if((boundIndex = offset + length) < -1)
		{
			throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
		}

		final Object[] array = new Object[-length];
		for(int i = offset, j = 0; i > boundIndex; i--)
		{
			array[j++] = data[i];
		}
		return array;
	}

	@SuppressWarnings("unchecked")
	public static final <T> T[] rngToArray(
		final Object[] data,
		final int size,
		final int offset,
		final int length,
		      T[] a
	)
	{
		if(offset < 0 || offset >= size)
		{
			throw new IndexExceededException(size, offset);
		}

		// length 0 special case with marker element null at index 0 ("after" the last element)
		if(length == 0)
		{
			if(a.length == 0)
			{
				return (T[])Array.newInstance(a.getClass().getComponentType(), 1); // length-one array with null
			}
			return a;
		}

		if(length > 0)
		{
			if(a.length < length)
			{
				a = (T[])Array.newInstance(a.getClass().getComponentType(), length);
			}
			// convenient and more performant case: increasing iteration direction, arraycopy can be used
			System.arraycopy(data, offset, a, 0, length);
		}
		else
		{
			// reverse iteration direction
			final int boundIndex;
			if((boundIndex = offset + length) < -1)
			{
				throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
			}
			if(a.length < -length)
			{
				a = (T[])Array.newInstance(a.getClass().getComponentType(), -length);
			}

			for(int i = offset, j = 0; i > boundIndex; i--)
			{
				a[j++] = (T)data[i];
			}
		}

		// marker element null
		if(a.length > size)
		{
			a[size] = null;
		}
		return a;
	}

	public static <E> E[] rngToArray(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Class<E> type
	)
	{
		// checks can probably be optimized
		if(offset < 0 || offset >= size)
		{
			if(offset == 0 && size == 0 && length == 0)
			{
				return JadothArrays.newArray(type, 0);
			}
			throw new IndexExceededException(size, offset);
		}
		else if(length == 0)
		{
			return JadothArrays.newArray(type, 0);
		}
		else if(length > 0)
		{
			final E[] array = JadothArrays.newArray(type, length);
			System.arraycopy(data, offset, array, 0, length);
			return array;
		}
		return internalReverseToArray(data, size, offset, length, type);
	}

	private static <E> E[] internalReverseToArray(
		final E[] data,
		final int size,
		final int offset,
		final int length,
		final Class<E> type
	)
	{
		// reverse iteration direction
		final int boundIndex;
		if((boundIndex = offset + length) < -1)
		{
			throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
		}
		final E[] array = JadothArrays.newArray(type, -length);
		for(int i = offset, j = 0; i > boundIndex; i--)
		{
			array[j++] = data[i];
		}
		return array;
	}


	// (21.01.2013 TM)EXCP: proper exceptions instead of Strings
	@Deprecated
	static final String exceptionRange(final int size, final int offset, final long length)
	{
		return "Range ["
			+ (length < 0 ? offset + length + 1 + ";" + offset
			: length > 0 ? offset + ";" + (offset + length - 1)
			: offset + ";" + offset
			) + "] not in [0;" + (size - 1) + "]";
	}

	@Deprecated
	private static String exceptionIllegalSwapBounds(
		final int size,
		final int indexA,
		final int indexB,
		final int length
	)
	{
		return "Illegal swap bounds: (" + indexA + " [" + length + "] -> "
			+ indexB + " [" + length + "]) in range [0;" + (size - 1) + "]"
		;
	}

	@Deprecated
	public static final int checkIterationDirection(final int size, final int offset, final int length)
	{
		if(length > 0)
		{
			if(offset < 0 || offset + length > size)
			{
				throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
			}
			return 1;
		}
		else if(length < 0)
		{
			if(offset >= size || offset + length < -1)
			{
				throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
			}
			return -1;
		}
		else if(offset < 0 || offset >= size)
		{
			throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
		}
		return 0;
	}

	// CHECKSTYLE.ON: FinalParameter
}

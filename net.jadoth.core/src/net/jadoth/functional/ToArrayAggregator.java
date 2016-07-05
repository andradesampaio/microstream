package net.jadoth.functional;

import static net.jadoth.Jadoth.notNull;

public final class ToArrayAggregator<E> implements Aggregator<E, E[]>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	final E[] array;

	int i = -1;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public ToArrayAggregator(final E[] array)
	{
		super();
		this.array = notNull(array);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void accept(final E element)
	{
		this.array[++this.i] = element;
	}

	@Override
	public final E[] yield()
	{
		return this.array;
	}

}

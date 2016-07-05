package net.jadoth.memory;

import net.jadoth.exceptions.InstantiationRuntimeException;

public final class SystemInstantiator<T> implements Instantiator<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Class<T> type;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public SystemInstantiator(final Class<T> type)
	{
		super();
		this.type = type;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters          //
	/////////////////////

	public Class<T> getType()
	{
		return this.type;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public T newInstance()
	{
		try
		{
			return Memory.instantiate(this.type);
		}
		catch(final InstantiationException e)
		{
			// (10.02.2012)NOTE: As far as I understand unsafe javadoc, this should hardly be possible in this scenario
			throw new InstantiationRuntimeException(e);
		}
	}

}

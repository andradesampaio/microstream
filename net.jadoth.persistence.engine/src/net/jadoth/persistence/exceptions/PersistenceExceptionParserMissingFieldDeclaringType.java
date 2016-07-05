package net.jadoth.persistence.exceptions;

public class PersistenceExceptionParserMissingFieldDeclaringType extends PersistenceExceptionParser
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public PersistenceExceptionParserMissingFieldDeclaringType(
		final int index
	)
	{
		this(index, null, null);
	}

	public PersistenceExceptionParserMissingFieldDeclaringType(
		final int index,
		final String message
	)
	{
		this(index, message, null);
	}

	public PersistenceExceptionParserMissingFieldDeclaringType(
		final int index,
		final Throwable cause
	)
	{
		this(index, null, cause);
	}

	public PersistenceExceptionParserMissingFieldDeclaringType(
		final int index,
		final String message, final Throwable cause
	)
	{
		this(index, message, cause, true, true);
	}

	public PersistenceExceptionParserMissingFieldDeclaringType(
		final int index,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(index, message, cause, enableSuppression, writableStackTrace);
	}



	///////////////////////////////////////////////////////////////////////////
	// getters          //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Missing field declaring type at index " + this.getIndex() + "."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}

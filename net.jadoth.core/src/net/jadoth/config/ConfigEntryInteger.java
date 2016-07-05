package net.jadoth.config;

final class ConfigEntryInteger extends ConfigEntry.AbstractImplementation<Integer>
{
	ConfigEntryInteger(final String key)
	{
		super(key);
	}

	@Override
	public final Integer parse(final String value)
	{
		return Integer.valueOf(value);
	}

}

package net.jadoth.config;

import java.util.function.Consumer;

import net.jadoth.collections.EqConstHashTable;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XGettingTable;


public class RootConfig extends AbstractConfig
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final EqHashTable<String, SubConfig>   children     = EqHashTable.New();
	private final XGettingTable<String, SubConfig> viewChildren = this.children.view();
	private       EqConstHashTable<String, String> configTable ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RootConfig(final String identifier)
	{
		super(identifier);
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	final void register(final SubConfig child)
	{
		this.children.add(child.identifier(), child);
	}

	public final RootConfig updateDefaults(final EqHashTable<String, ConfigFile> configFiles)
	{
		this.updateFiles(configFiles);
		this.configTable = this.compileEntries();

		// update all children (coalesce defaults with local overrides)
		this.children.values().iterate(new Consumer<SubConfig>()
		{
			@Override
			public void accept(final SubConfig e)
			{
				e.updateFromParent();
			}
		});
		return this;
	}

	public final XGettingTable<String, SubConfig> children()
	{
		return this.viewChildren;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final String get(final String key)
	{
		return this.table().get(key);
	}

	@Override
	public final XGettingTable<String, String> table()
	{
		if(this.configTable == null)
		{
			// (15.07.2013 TM)EXCP: proper exception
			throw new RuntimeException("Default config not initialized");
		}
		return this.configTable;
	}

}

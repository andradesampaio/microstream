package one.microstream.java.util;

import java.util.IdentityHashMap;

import one.microstream.X;
import one.microstream.chars.XChars;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerIdentityHashMap extends AbstractBinaryHandlerCustomCollection<IdentityHashMap<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_ELEMENTS = 0;
	// to prevent recurring confusion: IdentityHashMap really has no loadFactor. It uses an open adressing hash array.



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<IdentityHashMap<?, ?>> handledType()
	{
		return (Class)IdentityHashMap.class; // no idea how to get ".class" to work otherwise
	}

	static final int getElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCountKeyValue(BINARY_OFFSET_ELEMENTS));
	}
	
	public static BinaryHandlerIdentityHashMap New()
	{
		return new BinaryHandlerIdentityHashMap();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerIdentityHashMap()
	{
		super(
			handledType(),
			keyValuesFields()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final IdentityHashMap<?, ?>   instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		// store elements simply as array binary form
		bytes.storeMapEntrySet(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance.entrySet()   ,
			handler
		);
	}
	
	@Override
	public final IdentityHashMap<?, ?> create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return new IdentityHashMap<>(
			getElementCount(bytes)
		);
	}

	@Override
	public final void update(
		final Binary                 bytes   ,
		final IdentityHashMap<?, ?>  instance,
		final PersistenceObjectIdResolver idResolver
	)
	{
		instance.clear();
		
		@SuppressWarnings("unchecked")
		final IdentityHashMap<Object, Object> castedInstance = (IdentityHashMap<Object, Object>)instance;
		
		// IdentityHashMap does not need the elementsHelper detour as identity hashing does not depend on contained data
		bytes.collectKeyValueReferences(
			BINARY_OFFSET_ELEMENTS,
			getElementCount(bytes),
			idResolver,
			(k, v) ->
			{
				if(castedInstance.putIfAbsent(k, v) != null)
				{
					// (22.04.2016 TM)EXCP: proper exception
					throw new RuntimeException(
						"Duplicate key reference in " + IdentityHashMap.class.getSimpleName()
						+ " " + XChars.systemString(instance)
					);
				}
			}
		);
	}
	
	@Override
	public final void iterateInstanceReferences(final IdentityHashMap<?, ?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferencesMap(iterator, instance);
	}

	@Override
	public final void iterateLoadableReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateKeyValueEntriesReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}

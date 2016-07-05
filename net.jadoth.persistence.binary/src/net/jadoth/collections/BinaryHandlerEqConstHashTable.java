package net.jadoth.collections;

import java.lang.reflect.Field;
import java.util.Iterator;

import net.jadoth.Jadoth;
import net.jadoth.functional.BiProcedure;
import net.jadoth.functional._longProcedure;
import net.jadoth.hash.HashEqualator;
import net.jadoth.memory.Memory;
import net.jadoth.memory.objectstate.ObjectState;
import net.jadoth.memory.objectstate.ObjectStateHandlerLookup;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNative;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNativeCustom;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryCollectionHandling;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.reflect.JadothReflect;
import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;
import net.jadoth.swizzling.types.SwizzleStoreLinker;
import net.jadoth.util.KeyValue;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerEqConstHashTable extends AbstractBinaryHandlerNativeCustom<EqConstHashTable<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long
		BINARY_OFFSET_EQUALATOR    =                                                       0,
		BINARY_OFFSET_KEYS         = BINARY_OFFSET_EQUALATOR + BinaryPersistence.oidLength(),
		BINARY_OFFSET_VALUES       = BINARY_OFFSET_KEYS      + BinaryPersistence.oidLength(),
		BINARY_OFFSET_HASH_DENSITY = BINARY_OFFSET_VALUES    + BinaryPersistence.oidLength(),
		BINARY_OFFSET_ELEMENTS     = BINARY_OFFSET_HASH_DENSITY + Memory.byteSize_float()
	;

	// field type detour because there are sadly no field literals in Java (yet?).
	static final Field
		FIELD_EQUALATOR = JadothReflect.getInstanceFieldOfType(EqConstHashTable.class, HashEqualator.class)          ,
		FIELD_KEYS      = JadothReflect.getInstanceFieldOfType(EqConstHashTable.class, EqConstHashTable.Keys.class)  ,
		FIELD_VALUES    = JadothReflect.getInstanceFieldOfType(EqConstHashTable.class, EqConstHashTable.Values.class)
	;



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<EqConstHashTable<?, ?>> typeWorkaround()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)EqConstHashTable.class;
	}

	private static int getBuildItemElementCount(final Binary bytes)
	{
		return Jadoth.checkArrayRange(BinaryPersistence.getListElementCount(bytes, BINARY_OFFSET_ELEMENTS));
	}

	private static float getBuildItemHashDensity(final Binary bytes)
	{
		return BinaryPersistence.get_float(bytes, BINARY_OFFSET_HASH_DENSITY);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerEqConstHashTable(final long typeId)
	{
		// binary layout definition
		super(
			typeId,
			typeWorkaround(),
			AbstractBinaryHandlerNative.pseudoFields(
				pseudoField(HashEqualator.class, "hashEqualator"),
				pseudoField(EqConstHashTable.Keys.class, "keys"),
				pseudoField(EqConstHashTable.Values.class, "values"),
				pseudoField(float.class, "hashDensity"),
				AbstractBinaryHandlerNative.complex("entries",
					AbstractBinaryHandlerNative.pseudoField(Object.class, "key"),
					AbstractBinaryHandlerNative.pseudoField(Object.class, "value")
				)
			)

		);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void store(
		final Binary                 bytes    ,
		final EqConstHashTable<?, ?> instance ,
		final long                   oid      ,
		final SwizzleStoreLinker        linker
	)
	{
		// store elements simply as array binary form
		final long contentAddress = BinaryCollectionHandling.storeSizedKeyValuesAsEntries(
			bytes                 ,
			this.typeId()         ,
			oid                   ,
			BINARY_OFFSET_ELEMENTS,
			instance              ,
			instance.size()       ,
			linker
		);
		Memory.set_long(
			contentAddress + BINARY_OFFSET_EQUALATOR,
			linker.apply(instance.hashEqualator)
		);
		Memory.set_long(
			contentAddress + BINARY_OFFSET_KEYS,
			linker.apply(instance.keys)
		);
		Memory.set_long(
			contentAddress + BINARY_OFFSET_VALUES,
			linker.apply(instance.values)
		);
		Memory.set_float(
			contentAddress + BINARY_OFFSET_HASH_DENSITY,
			instance.hashDensity
		);
	}

	@Override
	public final EqConstHashTable<?, ?> create(final Binary bytes)
	{
		return EqConstHashTable.NewCustom(
			getBuildItemElementCount(bytes),
			getBuildItemHashDensity(bytes)
		);
	}

	@Override
	public final void update(
		final Binary                 bytes   ,
		final EqConstHashTable<?, ?> instance,
		final SwizzleBuildLinker     builder
	)
	{
		@SuppressWarnings("unchecked") // necessary because this handler operates on a generic technical level
		final EqConstHashTable<Object, Object> collectingInstance = (EqConstHashTable<Object, Object>)instance;

		// set single instances (must be done on memory-level due to final modifier. Little hacky, but okay)
		Memory.setObject(
			instance,
			Memory.objectFieldOffset(FIELD_EQUALATOR),
			builder.lookupObject(BinaryPersistence.get_long(bytes, BINARY_OFFSET_EQUALATOR))
		);
		Memory.setObject(
			instance,
			Memory.objectFieldOffset(FIELD_KEYS),
			builder.lookupObject(BinaryPersistence.get_long(bytes, BINARY_OFFSET_KEYS))
		);
		Memory.setObject(
			instance,
			Memory.objectFieldOffset(FIELD_VALUES),
			builder.lookupObject(BinaryPersistence.get_long(bytes, BINARY_OFFSET_VALUES))
		);
		instance.size = BinaryPersistence.collectKeyValueReferences(
			bytes,
			BINARY_OFFSET_ELEMENTS,
			getBuildItemElementCount(bytes),
			builder,
			new BiProcedure<Object, Object>()
			{
				@Override
				public void accept(final Object key, final Object value)
				{
					// unhashed because element instances are potentially not populated with data yet. see complete()
					collectingInstance.internalCollectUnhashed(key, value);
				}
			}
		);
		// note: hashDensity has already been set at creation time (shallow primitive value)
	}

	@Override
	public final void complete(final Binary medium, final EqConstHashTable<?, ?> instance, final SwizzleBuildLinker builder)
	{
		// rehash all previously unhashed collected elements
		instance.internalRehash();
	}

	@Override
	public final void iterateInstanceReferences(final EqConstHashTable<?, ?> instance, final SwizzleFunction iterator)
	{
		iterator.apply(instance.hashEqualator);
		iterator.apply(instance.keys);
		iterator.apply(instance.values);
		Swizzle.iterateReferences(iterator, instance);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final _longProcedure iterator)
	{
		iterator.accept(BinaryPersistence.get_long(bytes, BINARY_OFFSET_EQUALATOR));
		iterator.accept(BinaryPersistence.get_long(bytes, BINARY_OFFSET_KEYS));
		iterator.accept(BinaryPersistence.get_long(bytes, BINARY_OFFSET_VALUES));
		BinaryCollectionHandling.iterateKeyValueEntriesReferences(bytes, BINARY_OFFSET_ELEMENTS, iterator);
	}

	@SuppressWarnings("unchecked") // type erasure hassle :(
	@Override
	public final boolean isEqual(
		final EqConstHashTable<?, ?>        source            ,
		final EqConstHashTable<?, ?>        target            ,
		final ObjectStateHandlerLookup stateHandlerLookup
	)
	{
		// not sure if equalator should be checked here

		// one must be iterated with a stateful iterator while the other one is iterated directly
		final Iterator<KeyValue<Object, Object>> srcIterator = ((EqConstHashTable<Object, Object>)source).iterator();
		return source.size == target.size
			&& ObjectState.isEqual(source.hashEqualator, target.hashEqualator, stateHandlerLookup)
			&& ((EqConstHashTable<Object, Object>)target).applies(
				e -> srcIterator.hasNext() && isEqualEntry(e, srcIterator.next(), stateHandlerLookup)
			)
		;
	}

	static final boolean isEqualEntry(
		final KeyValue<Object, Object> e1,
		final KeyValue<Object, Object> e2,
		final ObjectStateHandlerLookup stateHandlerLookup
	)
	{
		return ObjectState.isEqual(e1.key(), e2.key(), stateHandlerLookup)
			&& ObjectState.isEqual(e1.value(), e2.value(), stateHandlerLookup)
		;
	}

	@Override
	public final boolean hasInstanceReferences()
	{
		return true;
	}

	@Override
	public final boolean isVariableBinaryLengthType()
	{
		return true;
	}

	@Override
	public final boolean hasVariableBinaryLengthInstances()
	{
		return true;
	}

//	@Override
//	public final void copy(final EqConstHashTable<?, ?> source, final EqConstHashTable<?, ?> target)
//	{
//		// due to type erasure, there is no way to determine if target is valid.
//		// this also proces that such a totaly generic copy functionality is not viable here
//		throw new UnsupportedOperationException();
//	}

}

package net.jadoth.swizzling.types;

import net.jadoth.collections.HashTable;
import net.jadoth.collections.XIterable;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistencyInvalidObjectId;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistencyInvalidTypeId;

public class Swizzle
{
	public static enum IdType
	{
		NULL
		{
			@Override
			public boolean isInRange(final long id)
			{
				return id == Swizzle.OID_NULL;
			}
		},
		TID
		{
			@Override
			public boolean isInRange(final long id)
			{
				return id >= Swizzle.FIRST_TID && id < BOUND_TID;
			}
		},
		OID
		{
			@Override
			public boolean isInRange(final long id)
			{
				return id >= Swizzle.FIRST_OID && id < BOUND_OID;
			}
		},
		CID
		{
			@Override
			public boolean isInRange(final long id)
			{
				return id >= Swizzle.FIRST_CID && id < BOUND_CID;
			}
		},
		UNDEFINED
		{
			@Override
			public boolean isInRange(final long id)
			{
				return id < START_TID_BASE || id >= BOUND_CID;
			}
		};


		public boolean isInRange(final long id)
		{
			return true;
		}

		public static IdType determineFromValue(final long id)
		{
			// order of checks is designed according to probability of type (OID having the highest etc.)
			return id >= Swizzle.FIRST_OID
				? id >= Swizzle.FIRST_CID
					? id >= Swizzle.BOUND_CID
						? UNDEFINED
						: CID
					: OID
				: id >= Swizzle.FIRST_TID
					? TID
					: id == Swizzle.OID_NULL
						? NULL
						: UNDEFINED
			;
		}

	}


	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long START_CID_BASE =  9_000_000_000_000_000_000L; // first assigned CID is 9...1
	static final long START_OID_BASE =  1_000_000_000_000_000_000L; // first assigned OID is 1...1
	static final long START_TID_BASE =                          0L; // first assigned TID is 1 (see below)

	static final long FIRST_CID      =  START_CID_BASE + 1;
	static final long FIRST_OID      =  START_OID_BASE + 1;
	static final long FIRST_TID      =  START_TID_BASE + 1;

	static final long BOUND_CID      =  9_100_000_000_000_000_000L;
	static final long BOUND_OID      =  START_CID_BASE;
	static final long BOUND_TID      =  START_OID_BASE;

	static final long START_CID_BYTE      = START_CID_BASE + 1_000;
	static final long START_CID_BOOLEAN   = START_CID_BASE + 2_000;
	static final long START_CID_SHORT     = START_CID_BASE + 3_000;
	static final long START_CID_CHARACTER = START_CID_BASE + 4_000;
	static final long START_CID_INTEGER   = START_CID_BASE + 5_000;
	static final long START_CID_LONG      = START_CID_BASE + 6_000;

	static final int JSL_CACHE_INTEGER_MIN = -128;
	static final int JSL_CACHE_INTEGER_MAX = +128;
	static final int JSL_CACHE_CHARACTER_MAX = +128;

	static final long START_CID_REAL = START_CID_BASE +    10_000L; // first 10K reserved for JLS constants
	static final long START_TID_REAL = START_TID_BASE + 1_000_000L; // first new type gets 1M1 assigned.

	static final long OID_NULL =  0L;
	static final long TID_NULL = OID_NULL; // same as OID null because TIDs are actually OIDs.

	// CHECKSTYLE.OFF: ConstantName: type names are intentionally unchanged

	static final long TID_PRIMITIVE_byte               =  1L;
	static final long TID_PRIMITIVE_boolean            =  2L;
	static final long TID_PRIMITIVE_short              =  3L;
	static final long TID_PRIMITIVE_char               =  4L;
	static final long TID_PRIMITIVE_int                =  5L;
	static final long TID_PRIMITIVE_float              =  6L;
	static final long TID_PRIMITIVE_long               =  7L;
	static final long TID_PRIMITIVE_double             =  8L;
	static final long TID_PRIMITIVE_void               =  9L; // "kind of" primitive. Or whatever.
	static final long TID_Object                       = 10L;
	static final long TID_Byte                         = 11L;
	static final long TID_Boolean                      = 12L;
	static final long TID_Short                        = 13L;
	static final long TID_Character                    = 14L;
	static final long TID_Integer                      = 15L;
	static final long TID_Float                        = 16L;
	static final long TID_Long                         = 17L;
	static final long TID_Double                       = 18L;
	static final long TID_Void                         = 19L;

	static final long TID_Class                        = 20L;
	static final long TID_Enum                         = 21L;

	static final long TID_String                       = 30L;
	static final long TID_AbstractStringBuilder        = 31L;
	static final long TID_StringBuffer                 = 32L;
	static final long TID_StringBuilder                = 33L;

	static final long TID_java_io_File                 = 34L;
	static final long TID_java_util_Date               = 35L;

	static final long TID_Number                       = 36L;
	static final long TID_java_math_BigInteger         = 37L;
	static final long TID_java_math_BigDecimal         = 38L;

	static final long TID_java_util_AbstractCollection = 40L;
	static final long TID_java_util_AbstractList       = 41L;
	static final long TID_java_util_AbstractSet        = 42L;
	static final long TID_java_util_ArrayList          = 43L;
	static final long TID_java_util_HashSet            = 44L;

	static final long TID_ARRAY_byte           = 100L + TID_PRIMITIVE_byte   ;
	static final long TID_ARRAY_boolean        = 100L + TID_PRIMITIVE_boolean;
	static final long TID_ARRAY_short          = 100L + TID_PRIMITIVE_short  ;
	static final long TID_ARRAY_char           = 100L + TID_PRIMITIVE_char   ;
	static final long TID_ARRAY_int            = 100L + TID_PRIMITIVE_int    ;
	static final long TID_ARRAY_float          = 100L + TID_PRIMITIVE_float  ;
	static final long TID_ARRAY_long           = 100L + TID_PRIMITIVE_long   ;
	static final long TID_ARRAY_double         = 100L + TID_PRIMITIVE_double ;

	static final long TID_ARRAY_Object         = 100L + TID_Object   ;
	static final long TID_ARRAY_Byte           = 100L + TID_Byte     ;
	static final long TID_ARRAY_Boolean        = 100L + TID_Boolean  ;
	static final long TID_ARRAY_Short          = 100L + TID_Short    ;
	static final long TID_ARRAY_Character      = 100L + TID_Character;
	static final long TID_ARRAY_Integer        = 100L + TID_Integer  ;
	static final long TID_ARRAY_Float          = 100L + TID_Float    ;
	static final long TID_ARRAY_Long           = 100L + TID_Long     ;
	static final long TID_ARRAY_Double         = 100L + TID_Double   ;
	static final long TID_ARRAY_Void           = 100L + TID_Void     ;

	static final long TID_ARRAY_Class          = 100L + TID_Class;
	static final long TID_ARRAY_Enum           = 100L + TID_Enum;

	static final long TID_ARRAY_String         = 100L + TID_String;
	static final long TID_ARRAY_AbsStringBuffr = 100L + TID_AbstractStringBuilder;
	static final long TID_ARRAY_StringBuffer   = 100L + TID_StringBuffer         ;
	static final long TID_ARRAY_StringBuilder  = 100L + TID_StringBuilder        ;

	static final long TID_net_jadoth_swizzling_types_Swizzling = 10000L;
	static final long TID_net_jadoth_collections_VarList       = 10005L;

	// CHECKSTYLE.ON: ConstantName

	static final String OBJECT_ID_NAME       = "ObjectId";
	static final String OBJECT_ID_NAME_SHORT = "OID";



	public static final String objectIdName()
	{
		return OBJECT_ID_NAME;
	}

	public static final String objectIdShortName()
	{
		return OBJECT_ID_NAME_SHORT;
	}

	/**
	 * Central architectural information method that always returns {@code long.class}.
	 *
	 * @return {@code long.class}
	 */
	public static final Class<?> objectIdType()
	{
		return long.class;
	}

	public static final long nullId()
	{
		return OID_NULL;
	}

//	public static final SwizzleTypeIdLookup createDefaultTypeLookup()
//	{
//		return new SwizzleTypeIdLookup()
//		{
//			@Override
//			public long lookupTypeId(final Class<?> type)
//			{
//				final Long nativeTypeId = mapping.get(type);
//				return nativeTypeId == null ? Swizzle.nullId() : nativeTypeId.longValue();
//			}
//		};
//	}


	public static final long defaultStartTypeId()
	{
		return START_TID_REAL;
	}

	public static final long defaultStartConstantId()
	{
		return START_CID_REAL;
	}

	public static final long defaultStartObjectId()
	{
		return START_OID_BASE;
	}


	/* (22.03.2013 TM)XXX: remove or optionally replace with SwizzleTypeDictionary lookup
	 * here and in persistence
	 */

	public static final HashTable<Class<?>, Long> defaultTypeMapping()
	{
		final HashTable<Class<?>, Long> mapping = HashTable.New();
		
		// note: correct order is important for recursive super type registration
		mapping.add(byte           .class, TID_PRIMITIVE_byte   );
		mapping.add(boolean        .class, TID_PRIMITIVE_boolean);
		mapping.add(short          .class, TID_PRIMITIVE_short  );
		mapping.add(char           .class, TID_PRIMITIVE_char   );
		mapping.add(int            .class, TID_PRIMITIVE_int    );
		mapping.add(float          .class, TID_PRIMITIVE_float  );
		mapping.add(long           .class, TID_PRIMITIVE_long   );
		mapping.add(double         .class, TID_PRIMITIVE_double );
		mapping.add(void           .class, TID_PRIMITIVE_void   );
		mapping.add(Object         .class, TID_Object           );
		mapping.add(Number         .class, TID_Number           );
		mapping.add(Byte           .class, TID_Byte             );
		mapping.add(Boolean        .class, TID_Boolean          );
		mapping.add(Short          .class, TID_Short            );
		mapping.add(Character      .class, TID_Character        );
		mapping.add(Integer        .class, TID_Integer          );
		mapping.add(Float          .class, TID_Float            );
		mapping.add(Long           .class, TID_Long             );
		mapping.add(Double         .class, TID_Double           );
		mapping.add(Void           .class, TID_Void             );

		mapping.add(Class          .class, TID_Class            );
		mapping.add(Enum           .class, TID_Enum             );

		mapping.add(String         .class, TID_String           );
		// stupid default visibility on such a common type. Idiots ^^.
		mapping.add(StringBuffer.class.getSuperclass(), TID_AbstractStringBuilder);
		mapping.add(StringBuffer   .class, TID_StringBuffer     );
		mapping.add(StringBuilder  .class, TID_StringBuilder    );


		mapping.add(java.io  .File      .class, TID_java_io_File        );
		mapping.add(java.util.Date      .class, TID_java_util_Date      );

		mapping.add(Number              .class, TID_Number              );
		mapping.add(java.math.BigInteger.class, TID_java_math_BigInteger);
		mapping.add(java.math.BigDecimal.class, TID_java_math_BigDecimal);

		// so stupid java.util collections, can't even tell x_x
		mapping.add(java.util.ArrayList.class.getSuperclass().getSuperclass(), TID_java_util_AbstractCollection );
		mapping.add(java.util.ArrayList.class.getSuperclass(), TID_java_util_AbstractList );
		mapping.add(java.util.HashSet  .class.getSuperclass(), TID_java_util_AbstractSet );
		mapping.add(java.util.ArrayList.class, TID_java_util_ArrayList );
		mapping.add(java.util.HashSet  .class, TID_java_util_HashSet   );
		/* (27.03.2012)FIXME more native types
		 *
		 * And apropriate type handlers in persistence, of course
		 *
		 * more jdk collections
		 * XCollections here as well?
		 * what about Thread? Is it persistable? Hardly ^^
		 * java.nio.Path
		 *
		 * How to handle "definitely not persistable" native types
		 * and native types with stuff like unshared objects?
		 *
		 */

		// basic array types (arrays of java.lang. types)
		mapping.add(byte[]         .class, TID_ARRAY_byte   );
		mapping.add(boolean[]      .class, TID_ARRAY_boolean);
		mapping.add(short[]        .class, TID_ARRAY_short  );
		mapping.add(char[]         .class, TID_ARRAY_char   );
		mapping.add(int[]          .class, TID_ARRAY_int    );
		mapping.add(float[]        .class, TID_ARRAY_float  );
		mapping.add(long[]         .class, TID_ARRAY_long   );
		mapping.add(double[]       .class, TID_ARRAY_double );
		// invalid: void[].class
		mapping.add(Class[]        .class, TID_ARRAY_Class        );
		mapping.add(Byte[]         .class, TID_ARRAY_Byte         );
		mapping.add(Boolean[]      .class, TID_ARRAY_Boolean      );
		mapping.add(Short[]        .class, TID_ARRAY_Short        );
		mapping.add(Character[]    .class, TID_ARRAY_Character    );
		mapping.add(Integer[]      .class, TID_ARRAY_Integer      );
		mapping.add(Float[]        .class, TID_ARRAY_Float        );
		mapping.add(Long[]         .class, TID_ARRAY_Long         );
		mapping.add(Double[]       .class, TID_ARRAY_Double       );
		mapping.add(Void[]         .class, TID_ARRAY_Void         );
		mapping.add(Object[]       .class, TID_ARRAY_Object       );
		mapping.add(String[]       .class, TID_ARRAY_String       );
		mapping.add(StringBuffer[] .class, TID_ARRAY_StringBuffer );
		mapping.add(StringBuilder[].class, TID_ARRAY_StringBuilder);
		mapping.add(Enum[]         .class, TID_ARRAY_Enum         );

		// jadoth types //

		mapping.add(net.jadoth.swizzling.types.Lazy.class, TID_net_jadoth_swizzling_types_Swizzling);

//		mapping.add(net.jadoth.collections.VarList.class, TID_net_jadoth_collections_VarList);
		
		return mapping;
	}
	
	public static final <R extends SwizzleTypeRegistry> R registerDefaultTypeMappings(final R registry)
	{
		registry.registerTypeToIdMappings(defaultTypeMapping());
		return registry;
	}

	public static final long classTypeId()
	{
		return TID_Class;
	}

	public static final <R extends SwizzleRegistry> R registerJavaConstants(final R registry)
	{
		synchronized(registry)
		{
			long
				oidByte      = START_CID_BYTE     ,
				oidBoolean   = START_CID_BOOLEAN  ,
				oidShort     = START_CID_SHORT    ,
				oidCharacter = START_CID_CHARACTER,
				oidInteger   = START_CID_INTEGER  ,
				oidLong      = START_CID_LONG
			;
			/* Booleans */
			{
				registry.registerObject(oidBoolean++, Boolean.FALSE);
				registry.registerObject(oidBoolean++, Boolean.TRUE );
			}
			for(int i = JSL_CACHE_INTEGER_MIN; i < JSL_CACHE_INTEGER_MAX; i++)
			{
				registry.registerObject(oidByte++   , Byte.valueOf((byte)i)  );
				registry.registerObject(oidShort++  , Short.valueOf((short)i));
				registry.registerObject(oidInteger++, Integer.valueOf(i)     );
				registry.registerObject(oidLong++   , Long.valueOf(i)        );
			}
			for(int i = 0; i < JSL_CACHE_CHARACTER_MAX; i++)
			{
				registry.registerObject(oidCharacter++, Character.valueOf((char)i));
			}
		}
		
		return registry;
	}

	public static final boolean getCached(
		final SwizzleObjectIdResolving oidResolver,
		final Object[] target,
		final int targetOffset,
		final long[] oids
	)
	{
		for(int i = 0; i < oids.length; i++)
		{
			final Object cachedInstance;
			if((cachedInstance = oidResolver.lookupObject(oids[i])) != null)
			{
				target[targetOffset + i] = cachedInstance;
				oids[i] = 0L;
			}
		}
		for(int i = targetOffset; i < target.length; i++)
		{
			if(target[i] == null)
			{
				return false;
			}
		}
		return true;
	}




	public static long validateObjectId(final long id) throws SwizzleExceptionConsistencyInvalidObjectId
	{
		if(id < START_OID_BASE)
		{
			throw new SwizzleExceptionConsistencyInvalidObjectId(id);
		}
		return id;
	}

	public static long validateTypeId(final long id) throws SwizzleExceptionConsistencyInvalidTypeId
	{
		if(id < START_TID_BASE)
		{
			throw new SwizzleExceptionConsistencyInvalidTypeId(id);
		}
		return id;
	}

	public static final void iterateReferences(
		final SwizzleFunction iterator,
		final Object[]        array   ,
		final int             offset  ,
		final int             length
	)
	{
		final int bound = offset + length;
		for(int i = offset; offset < bound; i++)
		{
			iterator.apply(array[i]);
		}
	}

	public static final void iterateReferences(final SwizzleFunction iterator, final XIterable<?> elements)
	{
		elements.iterate(iterator::apply);
	}



	protected Swizzle()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}

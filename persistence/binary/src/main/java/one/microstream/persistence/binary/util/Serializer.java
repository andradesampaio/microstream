package one.microstream.persistence.binary.util;

import static java.lang.System.identityHashCode;
import static one.microstream.X.notNull;

import java.nio.ByteBuffer;
import java.util.function.Function;

import one.microstream.X;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.hashing.XHashing;
import one.microstream.math.XMath;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryStorer;
import one.microstream.persistence.binary.types.ChunksBuffer;
import one.microstream.persistence.binary.types.ChunksBufferByteReversing;
import one.microstream.persistence.binary.types.ChunksWrapper;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.PersistenceAcceptor;
import one.microstream.persistence.types.PersistenceIdSet;
import one.microstream.persistence.types.PersistenceLocalObjectIdRegistry;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.persistence.types.PersistenceObjectIdRequestor;
import one.microstream.persistence.types.PersistenceObjectManager;
import one.microstream.persistence.types.PersistenceSource;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceStorer;
import one.microstream.persistence.types.PersistenceTarget;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.persistence.types.Storer;
import one.microstream.reference.ObjectSwizzling;
import one.microstream.reference.Swizzling;
import one.microstream.util.BufferSizeProviderIncremental;

/**
 * Convenient API layer to use the binary persistence functionality for a simple serializer.
 * <p>
 * It is based on a {@link SerializerFoundation}, which can be configured to various needs.
 * <p>
 * Per default {@link Binary} and <code>byte[]</code> are supported as medium types.
 *
 * @param <M> the medium type
 */
public interface Serializer<M> extends AutoCloseable
{
	/**
	 * Serializes the given object graph into the medium type.
	 * @param object the graph's root
	 * @return the binary format
	 */
	public M serialize(Object object);
	
	/**
	 * Recreates an object graph based on the given data.
	 * @param <T> the object's type
	 * @param medium the medium to read from
	 * @return the deserialized object graph
	 */
	public <T> T deserialize(M medium);
	
	
	public static Serializer<Binary> Binary()
	{
		return Binary(SerializerFoundation.New());
	}
	
	public static Serializer<Binary> Binary(final SerializerFoundation<?> foundation)
	{
		return New(
			foundation         ,
			Function.identity(),
			Function.identity()
		);
	}
	
	public static Serializer<byte[]> Bytes()
	{
		return Bytes(SerializerFoundation.New());
	}
	
	public static Serializer<byte[]> Bytes(final SerializerFoundation<?> foundation)
	{
		return New(
			foundation      ,
			Static::toBytes ,
			Static::toBinary
		);
	}
	
	public static <M> Serializer<M> New(
		final Function<Binary, M> toMedium,
		final Function<M, Binary> toBinary
	)
	{
		return New(
			SerializerFoundation.New(),
			toMedium                  ,
			toBinary
		);
	}
		
	public static <M> Serializer<M> New(
		final SerializerFoundation<?> foundation,
		final Function<Binary, M>     toMedium  ,
		final Function<M, Binary>     toBinary
	)
	{
		return new Serializer.Default<>(
			notNull(foundation),
			notNull(toMedium  ),
			notNull(toBinary  )
		);
	}
	
	
	public final static class Static
	{
		public static byte[] toBytes(final Binary binary)
		{
			return XMemory.toArray(binary.buffers());
		}
		
		public static Binary toBinary(final byte[] bytes)
		{
			final ByteBuffer buffer = XMemory.allocateDirectNative(bytes.length);
			buffer.put(bytes);
			return ChunksWrapper.New(buffer);
		}
		
		/**
		 * Dummy constructor to prevent instantiation of this static-only utility class.
		 *
		 * @throws UnsupportedOperationException when called
		 */
		private Static()
		{
			// static only
			throw new UnsupportedOperationException();
		}
	}
	
	
	public static interface Source extends PersistenceSource<Binary>
	{
		@Override
		default XGettingCollection<? extends Binary> readByObjectIds(final PersistenceIdSet[] oids)
			throws PersistenceExceptionTransfer
		{
			return null;
		}
	}
	
	
	public static interface Target extends PersistenceTarget<Binary>
	{
		@Override
		default boolean isWritable()
		{
			return true;
		}
	}
	
	
	public static class Default<M> implements Serializer<M>
	{
		private final SerializerFoundation<?> foundation        ;
		private final Function<Binary, M>     toMedium          ;
		private final Function<M, Binary>     toBinary          ;
		private PersistenceManager<Binary>    persistenceManager;
		private Storer                        storer            ;
		private Binary                        input             ;
		private Binary                        output            ;
				
		Default(
			final SerializerFoundation<?> foundation,
			final Function<Binary, M>     toMedium  ,
			final Function<M, Binary>     toBinary
		)
		{
			super();
			this.foundation = foundation;
			this.toMedium   = toMedium  ;
			this.toBinary   = toBinary  ;
		}
		
		@Override
		public synchronized M serialize(final Object object)
		{
			this.lazyInit();
			this.storer.store(object);
			this.storer.commit();
			return this.toMedium.apply(this.output);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public synchronized <T> T deserialize(final M data)
		{
			this.lazyInit();
			this.input = this.toBinary.apply(data);
			return (T)this.persistenceManager.get();
		}
		
		@Override
		public synchronized void close()
		{
			if(this.persistenceManager != null)
			{
				this.persistenceManager.objectRegistry().truncateAll();
				this.persistenceManager.close();
				this.persistenceManager = null;
				this.input              = null;
				this.output             = null;
			}
		}
		
		private void lazyInit()
		{
			if(this.persistenceManager == null)
			{
				final Source source = ()   -> X.Constant(this.input);
				final Target target = data -> this.output = data    ;
								
				this.persistenceManager = this.foundation.createPersistenceManager(source, target);
				this.storer             = this.persistenceManager.createStorer(
					new SerializerStorer.Creator(this.foundation.isByteOrderMismatch())
				);
			}
			else
			{
				this.persistenceManager.objectRegistry().truncateAll();
			}
		}
		
		
		static class SerializerStorer
		implements BinaryStorer, PersistenceStoreHandler<Binary>, PersistenceLocalObjectIdRegistry<Binary>
		{
			static class Creator implements PersistenceStorer.Creator<Binary>
			{
				private final boolean switchByteOrder;
				
				Creator(final boolean switchByteOrder)
				{
					super();
					this.switchByteOrder = switchByteOrder;
				}

				@Override
				public PersistenceStorer createLazyStorer(
					final PersistenceTypeHandlerManager<Binary> typeManager       ,
					final PersistenceObjectManager<Binary>      objectManager     ,
					final ObjectSwizzling                       objectRetriever   ,
					final PersistenceTarget<Binary>             target            ,
					final BufferSizeProviderIncremental         bufferSizeProvider
				)
				{
					return this.createEagerStorer(
						typeManager       ,
						objectManager     ,
						objectRetriever   ,
						target            ,
						bufferSizeProvider
					);
				}

				@Override
				public PersistenceStorer createEagerStorer(
					final PersistenceTypeHandlerManager<Binary> typeManager       ,
					final PersistenceObjectManager<Binary>      objectManager     ,
					final ObjectSwizzling                       objectRetriever   ,
					final PersistenceTarget<Binary>             target            ,
					final BufferSizeProviderIncremental         bufferSizeProvider
				)
				{
					final SerializerStorer storer = new SerializerStorer(
						objectManager       ,
						objectRetriever     ,
						typeManager         ,
						target              ,
						bufferSizeProvider  ,
						this.switchByteOrder
					);
					objectManager.registerLocalRegistry(storer);
					return storer;
				}
				
			}
			
			
			
			protected static int defaultSlotSize()
			{
				return 1024;
			}


			private final boolean                               switchByteOrder;
			private final PersistenceObjectManager<Binary>      objectManager  ;
			private final ObjectSwizzling                       objectRetriever;
			private final PersistenceTypeHandlerManager<Binary> typeManager    ;
			private final PersistenceTarget<Binary>             target         ;
			
			private final BufferSizeProviderIncremental bufferSizeProvider;
			
			private ChunksBuffer[] chunks;

			final   Item   head = new Item(null, 0L, null, null);
			private Item   tail;
			private Item[] hashSlots;
			private int    hashRange;
			private long   itemCount;


			public SerializerStorer(
				final PersistenceObjectManager<Binary>      objectManager     ,
				final ObjectSwizzling                       objectRetriever   ,
				final PersistenceTypeHandlerManager<Binary> typeManager       ,
				final PersistenceTarget<Binary>             target            ,
				final BufferSizeProviderIncremental         bufferSizeProvider,
				final boolean                               switchByteOrder
			)
			{
				super();
				this.objectManager      = notNull(objectManager)     ;
				this.objectRetriever    = notNull(objectRetriever)   ;
				this.typeManager        = notNull(typeManager)       ;
				this.target             = notNull(target)            ;
				this.bufferSizeProvider = notNull(bufferSizeProvider);
				this.switchByteOrder    =         switchByteOrder    ;
				
				this.defaultInitialize();
			}



			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public final PersistenceObjectManager<Binary> parentObjectManager()
			{
				return this.objectManager;
			}

			@Override
			public final ObjectSwizzling getObjectRetriever()
			{
				return this.objectRetriever;
			}

			@Override
			public final long maximumCapacity()
			{
				return Long.MAX_VALUE;
			}

			@Override
			public final long currentCapacity()
			{
				return this.hashSlots.length;
			}

			@Override
			public final long size()
			{
				return this.itemCount;
			}

			@Override
			public PersistenceStorer reinitialize()
			{
				// does locking internally
				this.defaultInitialize();
				
				return this;
			}

			@Override
			public PersistenceStorer reinitialize(final long initialCapacity)
			{
				// does locking internally
				this.internalInitialize(XHashing.padHashLength(initialCapacity));
				
				return this;
			}

			@Override
			public void clear()
			{
				// clearing means just to reinitialize (with default values).
				this.reinitialize();
			}

			private void defaultInitialize()
			{
				// does locking internally
				this.internalInitialize(defaultSlotSize());
			}

			protected void internalInitialize(final int hashLength)
			{
				this.hashSlots = new Item[hashLength];
				this.hashRange = hashLength - 1;
				
				// initializing/clearing item chain
				(this.tail = this.head).next = null;

				final ChunksBuffer[] chunks = this.chunks = new ChunksBuffer[1];
				chunks[0] = this.switchByteOrder
					? ChunksBufferByteReversing.New(chunks, this.bufferSizeProvider)
					: ChunksBuffer.New(chunks, this.bufferSizeProvider)
				;
			}

			@Override
			public PersistenceStorer ensureCapacity(final long desiredCapacity)
			{
				if(this.currentCapacity() >= desiredCapacity)
				{
					return this;
				}
				this.rebuildStoreItems(XHashing.padHashLength(desiredCapacity));
				
				return this;
			}
			
			@Override
			public <T> long apply(final T instance)
			{
				return this.applyEager(instance);
			}
			
			@Override
			public <T> long apply(final T instance, final PersistenceTypeHandler<Binary, T> localTypeHandler)
			{
				// concurrency: lookupOid() and ensureObjectId() lock internally, the rest is thread-local
				
				if(instance == null)
				{
					return Swizzling.nullId();
				}
				
				final long objectIdLocal;
				if(Swizzling.isFoundId(objectIdLocal = this.lookupOid(instance)))
				{
					// returning 0 is a valid case: an instance registered to be skipped by using the null-OID.
					return objectIdLocal;
				}
				
				return this.objectManager.ensureObjectId(instance, this, localTypeHandler);
			}
			
			@Override
			public final <T> long applyEager(final T instance)
			{
				if(instance == null)
				{
					return Swizzling.nullId();
				}
				
				/*
				 * "Eager" must still mean that if this storer has already stored the passed instance,
				 * it may not store it again. That would not only be data-wise redundant and unnecessary,
				 * but would also create infinite storing loops and overflows.
				 * So "eager" can only mean to not check the global registry, but it must still mean to check
				 * the local registry.
				 */
				final long objectIdLocal;
				if(Swizzling.isFoundId(objectIdLocal = this.lookupOid(instance)))
				{
					// returning 0 is a valid case: an instance registered to be skipped by using the null-OID.
					return objectIdLocal;
				}
				
				return this.registerGuaranteed(instance);
			}
			
			@Override
			public <T> long applyEager(final T instance, final PersistenceTypeHandler<Binary, T> localTypeHandler)
			{
				if(instance == null)
				{
					return Swizzling.nullId();
				}
				
				/*
				 * "Eager" must still mean that if this storer has already stored the passed instance,
				 * it may not store it again. That would not only be data-wise redundant and unnecessary,
				 * but would also create infinite storing loops and overflows.
				 * So "eager" can only mean to not check the global registry, but it must still mean to check
				 * the local registry.
				 */
				final long objectIdLocal;
				if(Swizzling.isFoundId(objectIdLocal = this.lookupOid(instance)))
				{
					// returning 0 is a valid case: an instance registered to be skipped by using the null-OID.
					return objectIdLocal;
				}
				
				return this.objectManager.ensureObjectIdGuaranteedRegister(instance, this, localTypeHandler);
			}
			
			/**
			 * Stores the passed instance (always) and interprets it as the root of a graph to be traversed and
			 * have its instances stored recursively if deemed necessary by the logic until all instance
			 * that can be reached by that logic have been handled.
			 * 
			 * @param root the root object of the graph
			 * @return the root's object id
			 */
			protected final long storeGraph(final Object root)
			{
				// initial registration. After that, storing adds via recursing the graph and processing items iteratively.
				final long rootOid = this.registerGuaranteed(notNull(root));

				// process and collect required instances uniquely in item chain (graph recursion transformed to iteration)
				for(Item item = this.tail; item != null; item = item.next)
				{
					item.typeHandler.store(this.chunks[0], item.instance, item.oid, this);
				}

				return rootOid;
			}

			@Override
			public final long store(final Object root)
			{
				return this.storeGraph(root);
			}

			@Override
			public final long[] storeAll(final Object... instances)
			{
				final long[] oids = new long[instances.length];
				for(int i = 0; i < instances.length; i++)
				{
					oids[i] = this.storeGraph(instances[i]);
				}
				return oids;
			}
			
			@Override
			public void storeAll(final Iterable<?> instances)
			{
				for(final Object instance : instances)
				{
					this.storeGraph(instance);
				}
			}
			
			@Override
			public void iterateMergeableEntries(final PersistenceAcceptor iterator)
			{
				for(Item e = this.head; (e = e.next) != null;)
				{
					// skip items are local only and not valid for being visible to (i.e. merged into) global context
					if(isSkipItem(e))
					{
						continue;
					}
					
					// mergeable entry
					iterator.accept(e.oid, e.instance);
				}
			}

			@Override
			public final Object commit()
			{
				// isEmpty locks internally
				if(!this.isEmpty())
				{
					// must validate here, too, in case the WriteController disabled writing during the storer's existence.
					this.target.validateIsStoringEnabled();
					
					this.typeManager.checkForPendingRootInstances();
					this.typeManager.checkForPendingRootsStoring(this);
					
					this.target.write(this.chunks[0].complete());
					
					this.typeManager.clearStorePendingRoots();
					this.objectManager.mergeEntries(this);
				}
				
				this.clear();
				
				// not used (yet?)
				return null;
			}
			
			public final long lookupOid(final Object object)
			{
				for(Item e = this.hashSlots[identityHashCode(object) & this.hashRange]; e != null; e = e.link)
				{
					if(e.instance == object)
					{
						return e.oid;
					}
				}

				// returning 0 is a valid case: an instance registered to be skipped by using the null-OID.
				return Swizzling.notFoundId();
			}
			
			private static boolean isSkipItem(final Item item)
			{
				return item.typeHandler == null;
			}
			
			@Override
			public final <T> long lookupObjectId(
				final T                                    object           ,
				final PersistenceObjectIdRequestor<Binary> objectIdRequestor,
				final PersistenceTypeHandler<Binary, T>    optionalHandler
			)
			{
				for(Item e = this.hashSlots[identityHashCode(object) & this.hashRange]; e != null; e = e.link)
				{
					if(e.instance == object)
					{
						if(isSkipItem(e))
						{
							// skip-entry for this storer, so it can offer nothing to the receiver.
							break;
						}
						
						// found a local entry in the current storer, transfer object<->id association to the receiver.
						objectIdRequestor.registerGuaranteed(e.oid, object, optionalHandler);
						return e.oid;
					}
				}
				
				return Swizzling.notFoundId();
			}
			

			@Override
			public final <T> void registerGuaranteed(
				final long                              objectId       ,
				final T                                 instance       ,
				final PersistenceTypeHandler<Binary, T> optionalHandler
			)
			{
				// ensure handler (or fail if type is not persistable) before ensuring an OID.
				final PersistenceTypeHandler<Binary, ? super T> typeHandler = optionalHandler != null
					? optionalHandler
					: this.typeManager.ensureTypeHandler(instance)
				;
				this.tail = this.tail.next = this.registerObjectId(instance, typeHandler, objectId);
			}
			
			@Override
			public <T> void registerLazyOptional(
				final long                              objectId       ,
				final T                                 instance       ,
				final PersistenceTypeHandler<Binary, T> optionalHandler
			)
			{
				// default is eager logic, so no-op
			}
			
			@Override
			public <T> void registerEagerOptional(
				final long                              objectId       ,
				final T                                 instance       ,
				final PersistenceTypeHandler<Binary, T> optionalHandler
			)
			{
				// default is eager logic.
				this.registerGuaranteed(objectId, instance, optionalHandler);
			}
			
			protected final long register(final Object instance)
			{
				/* Note:
				 * - ensureObjectId may never be called under a storer lock or a deadlock might happen!
				 * - depending on implementation lazy or eager callback, the other variant is a no-op respectively
				 */
				return this.objectManager.ensureObjectId(instance, this, null);
			}
			
			protected final long registerGuaranteed(final Object instance)
			{
				/* Note:
				 * - ensureObjectId may never be called under a storer lock or a deadlock might happen!
				 * - calls back to #register(long, Object), guaranteeing the registration
				 */
				return this.objectManager.ensureObjectIdGuaranteedRegister(instance, this, null);
			}
			
			@Override
			public final boolean skipMapped(final Object instance, final long objectId)
			{
				return this.internalSkip(instance, objectId);
			}

			@Override
			public final boolean skip(final Object instance)
			{
				final long foundObjectId = this.objectManager.lookupObjectId(instance);
				
				// not found means store as null. Lookup will never return 0
				if(Swizzling.isNotFoundId(foundObjectId))
				{
					return this.skipNulled(instance);
				}
				
				return this.internalSkip(instance, foundObjectId);
			}
			
			@Override
			public final boolean skipNulled(final Object instance)
			{
				return this.internalSkip(instance, Swizzling.nullId());
			}
			
			final boolean internalSkip(final Object instance, final long objectId)
			{
				// lookup returns -1 on failure, so 0 is a valid lookup result. Main reason for -1 vs. 0 distinction!
				if(Swizzling.isNotFoundId(this.lookupOid(instance)))
				{
					// only register if not found locally, of course
					this.registerObjectId(instance, null, objectId);
					return true;
				}
				
				// already locally present (found), do nothing.
				return false;
			}
			
			@SuppressWarnings("unchecked")
			public final <T> Item registerObjectId(
				final T                                         instance   ,
				final PersistenceTypeHandler<Binary, ? super T> typeHandler,
				final long                                      objectId
			)
			{
				if(++this.itemCount >= this.hashRange)
				{
					this.rebuildStoreItems();
				}

				return this.hashSlots[identityHashCode(instance) & this.hashRange] =
					new Item(
						instance,
						objectId,
						(PersistenceTypeHandler<Binary, Object>)typeHandler,
						this.hashSlots[identityHashCode(instance) & this.hashRange]
					)
				;
			}

			public final void rebuildStoreItems()
			{
				this.rebuildStoreItems(this.hashSlots.length * 2);
			}

			public final void rebuildStoreItems(final int newLength)
			{
				// moreless academic check for more than 1 billion entries
				if(this.hashSlots.length >= XMath.highestPowerOf2_int())
				{
					return; // note that aborting rebuild does not ruin anything.
				}

				final int newRange;
				final Item[] newSlots = new Item[(newRange = newLength - 1) + 1];
				for(Item entry : this.hashSlots)
				{
					for(Item next; entry != null; entry = next)
					{
						next = entry.link;
						entry.link = newSlots[identityHashCode(entry.instance) & newRange];
						newSlots[identityHashCode(entry.instance) & newRange] = entry;
					}
				}
				this.hashSlots = newSlots;
				this.hashRange = newRange;
			}
			
			
			static final class Item
			{
				final PersistenceTypeHandler<Binary, Object> typeHandler;
				final Object                                 instance   ;
				final long                                   oid        ;
				      Item                                   link, next ;

				Item(
					final Object                                 instance   ,
					final long                                   oid        ,
					final PersistenceTypeHandler<Binary, Object> typeHandler,
					final Item                                   link
				)
				{
					super();
					this.instance    = instance   ;
					this.oid         = oid        ;
					this.typeHandler = typeHandler;
					this.link        = link       ;
				}

			}
			
		}
		
	}
	
}

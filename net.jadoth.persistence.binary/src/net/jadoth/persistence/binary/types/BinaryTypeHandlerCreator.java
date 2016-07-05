package net.jadoth.persistence.binary.types;

import static net.jadoth.Jadoth.notNull;

import java.lang.reflect.Field;

import net.jadoth.collections.HashEnum;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeArrayObject;
import net.jadoth.persistence.binary.internal.BinaryHandlerPrimitive;
import net.jadoth.persistence.binary.internal.BinaryHandlerStateless;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.persistence.types.PersistenceFieldLengthResolver;
import net.jadoth.persistence.types.PersistenceTypeAnalyzer;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberField;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.persistence.types.PersistenceTypeHandlerCreator;
import net.jadoth.swizzling.types.SwizzleTypeManager;

public interface BinaryTypeHandlerCreator extends PersistenceTypeHandlerCreator<Binary>
{
	@Override
	public <T> PersistenceTypeHandler<Binary, T> createTypeHandler(
		Class<T>           type       ,
		long               typeId     ,
		SwizzleTypeManager typeManager
	) throws PersistenceExceptionTypeNotPersistable;



	public class Implementation implements BinaryTypeHandlerCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final PersistenceTypeAnalyzer        typeAnalyzer  ;
		private final PersistenceFieldLengthResolver lengthResolver;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final PersistenceTypeAnalyzer        typeAnalyzer  ,
			final PersistenceFieldLengthResolver lengthResolver
		)
		{
			super();
			this.typeAnalyzer   = notNull(typeAnalyzer);
			this.lengthResolver = notNull(lengthResolver); // must be provided, may not be null
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		@Override
		public <T> PersistenceTypeHandler<Binary, T> createTypeHandler(
			final Class<T>                      type              ,
			final long                          typeId            ,
			final SwizzleTypeManager            typeManager
		)
			throws PersistenceExceptionTypeNotPersistable
		{
			if(type.isPrimitive())
			{
				// primitive special cases
				return new BinaryHandlerPrimitive<>(type, typeId);
			}
			if(type.isArray())
			{
				// array special cases
				if(type.getComponentType().isPrimitive())
				{
					throw new RuntimeException(); // (01.04.2013)EXCP: proper exception
				}
				return new BinaryHandlerNativeArrayObject<>(type, typeId);
			}

			final HashEnum<PersistenceTypeDescriptionMemberField> fieldDescriptions = HashEnum.New();

			final XGettingEnum<Field> persistableFields =
				this.typeAnalyzer.collectPersistableFields(type, typeManager, fieldDescriptions)
			;

			if(persistableFields.isEmpty())
			{
				// required for a) sparing unnecessary overhead and b) validation reasons
				return new BinaryHandlerStateless<>(type, typeId);
			}

			// default implementation simply always uses a blank memory instantiator
			return new BinaryHandlerGeneric<>(
				type,
				typeId,
				BinaryPersistence.blankMemoryInstantiator(type),
				persistableFields,
				this.lengthResolver
			);
		}

	}

}

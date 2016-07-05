package net.jadoth.storage.types;

import static net.jadoth.Jadoth.notNull;

import java.io.File;

import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.memory.Chunks;
import net.jadoth.swizzling.types.SwizzleIdSet;

public interface StorageRequestTaskCreator
{
	public StorageChannelTaskInitialize createInitializationTask(
		int                         channelCount                    ,
		StorageChannelController    channelController               ,
		StorageEntityCacheEvaluator entityInitializingCacheEvaluator,
		StorageTypeDictionary       oldTypes
	);

	public StorageRequestTaskSaveEntities createSaveTask(Chunks[] medium);

	public StorageRequestTaskLoadByOids createLoadTaskByOids(SwizzleIdSet[] loadOids);

	public StorageRequestTaskLoadRoots createRootsLoadTask(int channelCount);
	
	public StorageRequestTaskLoadByTids createLoadTaskByTids(SwizzleIdSet loadTids, int channelCount);

	public StorageRequestTaskExportEntitiesByType createExportTypesTask(
		int                                 channelCount      ,
		StorageEntityTypeExportFileProvider exportFileProvider
	);

	public StorageRequestTaskExportChannels createTaskExportChannels(
		int                channelCount,
		StorageIoHandler fileHandler
	);

	public StorageRequestTaskCreateStatistics createCreateRawFileStatisticsTask(int channelCount);

	public StorageRequestTaskFileCheck createFullFileCheckTask(
		int                                channelCount  ,
		long                               nanoTimeBudget,
		StorageDataFileDissolvingEvaluator fileDissolver
	);

	public StorageRequestTaskCacheCheck createFullCacheCheckTask(
		int                         channelCount   ,
		long                        nanoTimeBudget ,
		StorageEntityCacheEvaluator entityEvaluator
	);

	public StorageRequestTaskImportData createImportFromFilesTask(
		int                           channelCount          ,
		StorageDataFileEvaluator      fileEvaluator         ,
		StorageObjectIdRangeEvaluator objectIdRangeEvaluator,
		XGettingEnum<File>            importFiles
	);

	public StorageChannelTaskShutdown createShutdownTask(
		int                      channelCount     ,
		StorageChannelController channelController
	);

	public StorageChannelTaskTruncateData createTruncateTask(
		int                      channelCount     ,
		StorageChannelController channelController
	);



	public final class Implementation implements StorageRequestTaskCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final StorageTimestampProvider timestampProvider;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(final StorageTimestampProvider timestampProvider)
		{
			super();
			this.timestampProvider = notNull(timestampProvider);
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public StorageChannelTaskInitialize createInitializationTask(
			final int                         channelCount                    ,
			final StorageChannelController    channelController               ,
			final StorageEntityCacheEvaluator entityInitializingCacheEvaluator,
			final StorageTypeDictionary       oldTypes
		)
		{
			return new StorageChannelTaskInitialize.Implementation(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount                                 ,
				channelController                            ,
				entityInitializingCacheEvaluator             ,
				oldTypes
			);
		}

		@Override
		public StorageChannelTaskShutdown createShutdownTask(
			final int channelCount,
			final StorageChannelController channelController
		)
		{
			return new StorageChannelTaskShutdown.Implementation(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount,
				channelController
			);
		}

		@Override
		public StorageChannelTaskTruncateData createTruncateTask(
			final int                      channelCount     ,
			final StorageChannelController channelController
		)
		{
			return new StorageChannelTaskTruncateData.Implementation(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount,
				channelController
			);
		}

		@Override
		public StorageRequestTaskSaveEntities createSaveTask(final Chunks[] medium)
		{
			return new StorageRequestTaskSaveEntities.Implementation(
				this.timestampProvider.currentNanoTimestamp(),
				medium
			);
		}

		@Override
		public StorageRequestTaskLoadByOids createLoadTaskByOids(final SwizzleIdSet[] loadOids)
		{
			return new StorageRequestTaskLoadByOids.Implementation(
				this.timestampProvider.currentNanoTimestamp(),
				loadOids
			);
		}

		@Override
		public StorageRequestTaskLoadRoots createRootsLoadTask(final int channelCount)
		{
			return new StorageRequestTaskLoadRoots.Implementation(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount
			);
		}
		
		@Override
		public StorageRequestTaskLoadByTids createLoadTaskByTids(final SwizzleIdSet loadTids, final int channelCount)
		{
			return new StorageRequestTaskLoadByTids.Implementation(
				this.timestampProvider.currentNanoTimestamp(),
				loadTids,
				channelCount
			);
		}

		@Override
		public StorageRequestTaskExportEntitiesByType createExportTypesTask(
			final int                                 channelCount      ,
			final StorageEntityTypeExportFileProvider exportFileProvider
		)
		{
			return new StorageRequestTaskExportEntitiesByType.Implementation(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount                                 ,
				exportFileProvider
			);
		}

		@Override
		public StorageRequestTaskExportChannels createTaskExportChannels(
			final int                channelCount,
			final StorageIoHandler fileHandler
		)
		{
			return new StorageRequestTaskExportChannels.Implementation(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount,
				fileHandler
			);
		}

		@Override
		public StorageRequestTaskCreateStatistics createCreateRawFileStatisticsTask(final int channelCount)
		{
			return new StorageRequestTaskCreateStatistics.Implementation(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount
			);
		}

		@Override
		public StorageRequestTaskFileCheck createFullFileCheckTask(
			final int                                channelCount       ,
			final long                               nanoTimeBudgetBound,
			final StorageDataFileDissolvingEvaluator fileDissolver
		)
		{
			return new StorageRequestTaskFileCheck.Implementation(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount,
				nanoTimeBudgetBound,
				fileDissolver
			);
		}

		@Override
		public StorageRequestTaskCacheCheck createFullCacheCheckTask(
			final int                         channelCount       ,
			final long                        nanoTimeBudgetBound,
			final StorageEntityCacheEvaluator entityEvaluator
		)
		{
			return new StorageRequestTaskCacheCheck.Implementation(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount,
				nanoTimeBudgetBound,
				entityEvaluator
			);
		}

		@Override
		public StorageRequestTaskImportData createImportFromFilesTask(
			final int                           channelCount          ,
			final StorageDataFileEvaluator          fileEvaluator         ,
			final StorageObjectIdRangeEvaluator objectIdRangeEvaluator,
			final XGettingEnum<File>            importFiles
		)
		{
			return new StorageRequestTaskImportData.Implementation(
				this.timestampProvider.currentNanoTimestamp(),
				channelCount,
				objectIdRangeEvaluator,
				importFiles
			);
		}

	}

}

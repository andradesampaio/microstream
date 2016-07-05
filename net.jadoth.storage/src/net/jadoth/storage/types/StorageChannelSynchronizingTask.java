package net.jadoth.storage.types;



public interface StorageChannelSynchronizingTask extends StorageChannelTask
{
	public boolean isProcessed();

	public void incrementProcessingProgress();

	public void waitOnProcessing() throws InterruptedException;



	public abstract class AbstractCompletingTask<R>
	extends StorageChannelTask.AbstractImplementation<R>
	implements StorageChannelSynchronizingTask
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public AbstractCompletingTask(final long timestamp, final int channelCount)
		{
			super(timestamp, channelCount);
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		protected void succeed(final StorageChannel channel, final R result)
		{
			// no-op by default. To be overridden only when needed.
		}

		protected void fail(final StorageChannel channel, final R result)
		{
			// no-op by default. To be overridden only when needed.
		}

		protected void postCompletionSuccess(final StorageChannel channel, final R result) throws InterruptedException
		{
			// no-op by default. To be overridden only when needed.
		}

		private void synchronizedComplete(final StorageChannel channel, final R result)
		{
			try
			{
				// handle success or failure
				if(this.hasProblems())
				{
					// any other thread's storing failed, so rollback own storing
					this.fail(channel, result);
				}
				else
				{
					this.succeed(channel, result);
				}
			}
			catch(final Throwable t)
			{
				this.addProblem(channel.channelIndex(), t);
			}
			finally
			{
				// must complete the task (signal calling thread) no matter the result (success or problem)
				this.incrementCompletionProgress();
			}
		}





		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		protected R internalProcessBy(final StorageChannel channel)
		{
			// no-op by default. To be overridden only when needed.
			return null;
		}

		@Override
		protected final void complete(final StorageChannel channel, final R result) throws InterruptedException
		{
			try
			{
				// wait for all other processing threads to report in before completing (e.g. committing a write)
				this.waitOnProcessing();
			}
			catch(final InterruptedException e)
			{
				/* Thread interrupted. Register problem, pass exception along
				 * but still care for consistency via finally block before leaving
				 */
				this.addProblem(channel.channelIndex(), e);
				throw e; // passing the interruption basically means terminating the channel work loop
			}
			finally
			{
				// actual completion logic after (timely) synchronizing with other threads (or after interruption)
				this.synchronizedComplete(channel, result);

				// post-completion logic that may not be subject to completion try-catch-finally
				if(!this.hasProblems())
				{
					this.postCompletionSuccess(channel, result);
				}
			}
		}


		public static final class Dummy extends AbstractCompletingTask<Void> implements StorageRequestTask
		{

			public Dummy(final int channelCount)
			{
				super(0, channelCount);
			}

		}

	}

}

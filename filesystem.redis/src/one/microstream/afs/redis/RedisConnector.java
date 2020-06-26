package one.microstream.afs.redis;

import static one.microstream.X.checkArrayRange;
import static one.microstream.X.notNull;
import static one.microstream.chars.XChars.notEmpty;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import one.microstream.afs.blobstore.BlobStoreConnector;
import one.microstream.afs.blobstore.BlobStorePath;


public interface RedisConnector extends BlobStoreConnector
{

	public static RedisConnector New(
		final String redisUri
	)
	{
		return New(
			RedisClient.create(
				notEmpty(redisUri)
			)
		);
	}

	public static RedisConnector New(
		final RedisClient client
	)
	{
		return new RedisConnector.Default(
			notNull(client)
		);
	}


	public static class Default
	extends    BlobStoreConnector.Abstract<BlobMetadata>
	implements RedisConnector
	{
		private final RedisClient                                 client    ;
		private       StatefulRedisConnection<String, ByteBuffer> connection;
		private       RedisCommands          <String, ByteBuffer> commands  ;

		Default(
			final RedisClient client
		)
		{
			super(
				BlobMetadata::key,
				BlobMetadata::size
			);
			this.client = client;
		}

		private RedisCommands<String, ByteBuffer> commands()
		{
			if(this.commands == null)
			{
				synchronized(this)
				{
					if(this.commands == null)
					{
						this.commands = (
							this.connection = this.client.connect(
								StringByteBufferCodec.New()
							)
						)
						.sync();

						this.commands.setTimeout(Duration.ofMinutes(1L));
					}
				}
			}

			return this.commands;
		}

		@Override
		protected Stream<BlobMetadata> blobs(
			final BlobStorePath file
		)
		{
			final RedisCommands<String, ByteBuffer> commands = this.commands();
			final String                            prefix   = toBlobKeyPrefixWithContainer(file);
			final Pattern                           pattern  = Pattern.compile(blobKeyRegex(prefix));
			return commands.keys(prefix.concat("*"))
				.stream()
				.filter(key -> pattern.matcher(key).matches())
				.map(key ->
					BlobMetadata.New(
						key,
						commands.strlen(key)
					)
				)
				.sorted(this.blobComparator())
			;
		}

		@Override
		protected void internalReadBlobData(
			final BlobStorePath   file        ,
			final BlobMetadata    blob        ,
			final ByteBuffer      targetBuffer,
			final long            offset      ,
			final long            length
		)
		{
			targetBuffer.put(
				this.commands().getrange(
					blob.key(),
					offset,
					offset + length - 1L
				)
			);
		}

		@Override
		protected boolean internalDeleteBlobs(
			final BlobStorePath                file ,
			final List<? extends BlobMetadata> blobs
		)
		{
			final String[] keys   = blobs.stream()
				.map(BlobMetadata::key)
				.toArray(String[]::new)
			;
			final Long     result = this.commands.del(keys);
			return result != null
				&& result.intValue() == blobs.size()
			;
		}

		@Override
		protected long internalWriteData(
			final BlobStorePath                  file         ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			final long nextBlobNumber = this.nextBlobNumber(file);
			final long totalSize      = this.totalSize(sourceBuffers);

			final ByteBuffer buffer = ByteBuffer.allocateDirect(checkArrayRange(totalSize));
			sourceBuffers.forEach(sourceBuffer -> buffer.put(sourceBuffer));
			buffer.flip();

			this.commands().set(
				toBlobKeyWithContainer(file, nextBlobNumber),
				buffer
			);

			return totalSize;
		}

		@Override
		protected long internalCopyFile(
			final BlobStorePath sourceFile,
			final BlobStorePath targetFile
		)
		{
			final long[] amount = new long[1];
			this.blobs(sourceFile).forEach(blob ->
			{
				final long       size   = blob.size();
				final ByteBuffer buffer = ByteBuffer.allocateDirect(checkArrayRange(size));
				this.internalReadBlobData(
					sourceFile,
					blob,
					buffer,
					0L,
					size
				);
				buffer.flip();
				this.commands().set(
					toBlobKeyWithContainer(
						 targetFile,
						 this.blobNumber(blob)
					),
					buffer
				);
				amount[0] += size;
			});

			return amount[0];
		}

		@Override
		protected synchronized void internalClose()
		{
			if(this.connection != null)
			{
				this.connection.close();
				this.connection = null;
			}
			this.commands = null;
		}

	}

}

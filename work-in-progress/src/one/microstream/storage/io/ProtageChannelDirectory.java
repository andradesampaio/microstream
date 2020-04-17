package one.microstream.storage.io;

import one.microstream.afs.ProtageWritableDirectory;
import one.microstream.storage.types.StorageHashChannelPart;

public interface ProtageChannelDirectory extends ProtageWritableDirectory, StorageHashChannelPart
{
	@Override
	public ProtageChannelFile createFile(String fileName);

	public ProtageChannelDataFile createNextDataFile(String fileName);
}

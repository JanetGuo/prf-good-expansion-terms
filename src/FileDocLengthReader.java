package src;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

public class FileDocLengthReader implements DocLengthReader {
	
	public static final String DEFAULT_PREFIX = "dl.";
	public static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;
	
	protected FileChannel fch;
	protected IntBuffer buffer;
	
	protected int start;
	protected int bufferSize;
	
	public static File getDocLengthFile( File dirIndex, String field ) {
		return new File( dirIndex, DEFAULT_PREFIX + field );
	}
	
	public FileDocLengthReader( File dirIndex, String field ) throws IOException {
		this( dirIndex, field, DEFAULT_BUFFER_SIZE );
	}
	
	public FileDocLengthReader( File dirIndex, String field, int bufferSize ) throws IOException {
		Path path = getDocLengthFile( dirIndex, field ).toPath();
		this.fch = FileChannel.open( path );
		this.start = 0;
		this.bufferSize = bufferSize;
		mapBuffer();
	}
	
	private void mapBuffer() throws IOException {
		long size = bufferSize * 4;
		if ( fch.size() < start * 4 + size ) {
			size = fch.size() - start * 4;
		}
		this.buffer = fch.map( FileChannel.MapMode.READ_ONLY, start * 4, size ).asIntBuffer();
	}
	
	public int getLength( int docid ) throws IOException {
		if ( docid - start >= bufferSize || docid < start ) {
			start = docid;
			mapBuffer();
		}
		return this.buffer.get( docid - start );
	}
	
	public void close() throws IOException {
		fch.close();
	}
	
}

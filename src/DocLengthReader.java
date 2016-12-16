package src;

import java.io.IOException;

public interface DocLengthReader {
	
	int getLength( int docid ) throws IOException;
	
	void close() throws IOException;
	
}

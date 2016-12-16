package src;

import java.io.IOException;

public interface PostingList {
	
	boolean end() throws IOException;
	
	void next() throws IOException;
	
	int doc() throws IOException;
	
	int freq() throws IOException;
	
	int N();
	
	int df();
	
	long totalFreq();
	
	long corpusLength();
	
	boolean supportN();
	
	boolean supportDf();
	
	boolean supportTotalFreq();
	
	boolean supportCorpusLength();
	
}

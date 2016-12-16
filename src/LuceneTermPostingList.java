package src;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

public class LuceneTermPostingList implements PostingList {
	
	protected PostingsEnum posting;
	protected int cursor;
	
	protected int df;
	protected int N;
	protected long tfc;
	protected long cl;
	
	public LuceneTermPostingList( IndexReader index, String field, String term ) throws IOException {
		this( index, MultiFields.getTermDocsEnum( index, field, new BytesRef( term ), PostingsEnum.FREQS ), field, term );
	}
	
	protected LuceneTermPostingList( IndexReader index, PostingsEnum posting, String field, String term ) throws IOException {
		this.posting = posting;
		cursor = posting.nextDoc();
		Term tm = new Term( field, term );
		this.df = index.docFreq( tm );
		this.N = index.numDocs();
		this.tfc = index.totalTermFreq( tm );
		this.cl = index.getSumTotalTermFreq( field );
	}
	
	public boolean supportDf() {
		return true;
	}
	
	public boolean supportTotalFreq() {
		return true;
	}
	
	public boolean supportCorpusLength() {
		return true;
	}
	
	public int df() {
		return df;
	}
	
	public long totalFreq() {
		return tfc;
	}
	
	public long corpusLength() {
		return cl;
	}
	
	public boolean supportN() {
		return true;
	}
	
	public boolean end() throws IOException {
		return cursor == PostingsEnum.NO_MORE_DOCS;
	}
	
	public void next() throws IOException {
		cursor = posting.nextDoc();
	}
	
	public int doc() throws IOException {
		return cursor;
	}
	
	public int freq() throws IOException {
		return posting.freq();
	}
	
	public int N() {
		return 0;
	}
	
}

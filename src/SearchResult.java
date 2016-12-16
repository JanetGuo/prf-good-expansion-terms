package src;

import org.apache.lucene.index.IndexReader;

import java.io.*;
import java.util.*;

/**
 * SearchResult stores a search result, including its internal ID (docid), external ID (docno),
 * and its relevance score computed by the retrieval system.
 */
public class SearchResult implements Comparable<SearchResult> {
	
	protected int docid;
	protected String docno;
	protected double score;
	
	public SearchResult( int docid, String docno, double score ) {
		this.docid = docid;
		this.docno = docno;
		this.score = score;
	}
	
	public int getDocid() {
		return docid;
	}
	
	public String getDocno() {
		return docno;
	}
	
	public SearchResult setDocno( String docno ) {
		this.docno = docno;
		return this;
	}
	
	public Double getScore() {
		return score;
	}
	
	public SearchResult setScore( double score ) {
		this.score = score;
		return this;
	}
	
	/**
	 * Writer a list of search result using the TREC standard result format.
	 *
	 * @param writer  Output.
	 * @param queryid A query id.
	 * @param runname The runname (such as VSM).
	 * @param results A ranked list of results.
	 * @param n       Only output the top-n results.
	 */
	public static void writeTRECFormat( PrintStream writer, String queryid, String runname, List<SearchResult> results, int n ) {
		for ( int ix = 0; ix < results.size() && ix < n; ix++ ) {
			int rank = ix + 1;
			writer.printf( "%s 0 %s %d %.8f %s\n", queryid, results.get( ix ).docno, rank, results.get( ix ).score, runname );
		}
	}
	
	/**
	 * Read a standard TREC format result file.
	 *
	 * @param file A TREC-format result file.
	 * @return A mapping of queryid to search result list.
	 * @throws IOException
	 */
	public static Map<String, List<SearchResult>> readTRECFormat( File file ) throws IOException {
		Map<String, List<SearchResult>> results = new TreeMap<>();
		BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( file ), "UTF-8" ) );
		String line;
		while ( ( line = reader.readLine() ) != null ) {
			String[] splits = line.split( "\\s+" );
			String qid = splits[0];
			String docno = splits[2];
			double score = Double.parseDouble( splits[4] );
			results.putIfAbsent( qid, new ArrayList<>() );
			results.get( qid ).add( new SearchResult( -1, docno, score ) );
		}
		reader.close();
		return results;
	}
	
	public static void dumpDocno( IndexReader index, String field_docno, Collection<SearchResult> results ) throws IOException {
		Set<String> fields = new HashSet<>();
		fields.add( field_docno );
		for ( SearchResult result : results ) {
			result.setDocno( index.document( result.docid, fields ).get( field_docno ) );
		}
	}
	
	public int compareTo( SearchResult r ) {
		return r.getScore().compareTo( getScore() );
	}
}

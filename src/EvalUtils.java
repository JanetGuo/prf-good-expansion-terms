package src;

import java.io.*;
import java.util.*;

public class EvalUtils {
	
	/**
	 * Evaluate the precision of the ranked list until some cutoff rank n.
	 *
	 * @param results   A list of search results.
	 * @param relDocnos The set of judged relevant documents.
	 * @param n         The cutoff rank n (results at lower ranks will not be evaluated).
	 * @return The precision of the ranked list until some cutoff rank n (P@n).
	 */
	public static double precision( Collection<SearchResult> results, Set<String> relDocnos, int n ) {
		double numrel = 0;
		int count = 0;
		for ( SearchResult result : results ) {
			if ( relDocnos.contains( result.getDocno() ) ) {
				numrel++;
			}
			count++;
			if ( count >= n ) {
				break;
			}
		}
		return numrel / n;
	}
	
	/**
	 * Evaluate the recall of the ranked list until some cutoff rank n.
	 *
	 * @param results   A list of search results.
	 * @param relDocnos The set of judged relevant documents.
	 * @param n         The cutoff rank n (results at lower ranks will not be evaluated).
	 * @return The recall of the ranked list until some cutoff rank n.
	 */
	public static double recall( Collection<SearchResult> results, Set<String> relDocnos, int n ) {
		double numrel = 0;
		int count = 0;
		for ( SearchResult result : results ) {
			if ( relDocnos.contains( result.getDocno() ) ) {
				numrel++;
			}
			count++;
			if ( count >= n ) {
				break;
			}
		}
		return numrel / relDocnos.size();
	}
	
	/**
	 * Evaluate the average precision of the ranked list until some cutoff rank n.
	 *
	 * @param results   A list of search results.
	 * @param relDocnos The set of judged relevant documents.
	 * @param n         The cutoff rank n (results at lower ranks will not be evaluated).
	 * @return The average precision (AP) of the ranked list until some cutoff rank n.
	 */
	public static double avgPrec( Collection<SearchResult> results, Set<String> relDocnos, int n ) {
		double numrel = 0;
		double sumprec = 0;
		int count = 0;
		for ( SearchResult result : results ) {
			if ( relDocnos.contains( result.getDocno() ) ) {
				numrel++;
				sumprec += ( numrel / ( count + 1 ) );
			}
			count++;
			if ( count >= n ) {
				break;
			}
		}
		return sumprec / relDocnos.size();
	}
	
	/**
	 * Load a TREC-format relevance judgment (qrels) file (such as "qrels_robust04" in HW2).
	 *
	 * @param f A qrels file.
	 * @return A map storing the set of relevant documents for each qid.
	 * @throws IOException
	 */
	public static Map<String, Set<String>> loadQrels( String f ) throws IOException {
		return loadQrels( new File( f ) );
	}
	
	/**
	 * Load a TREC-format relevance judgment (qrels) file (such as "qrels_robust04" in HW2).
	 *
	 * @param f A qrels file.
	 * @return A map storing the set of relevant documents for each qid.
	 * @throws IOException
	 */
	public static Map<String, Set<String>> loadQrels( File f ) throws IOException {
		Map<String, Set<String>> qrels = new TreeMap<>();
		BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( f ), "UTF-8" ) );
		String line;
		while ( ( line = reader.readLine() ) != null ) {
			String[] splits = line.split( "\\s+" );
			String qid = splits[0];
			String docno = splits[2];
			qrels.putIfAbsent( qid, new TreeSet<>() );
			if ( Integer.parseInt( splits[3] ) > 0 ) {
				qrels.get( qid ).add( docno );
			}
		}
		reader.close();
		return qrels;
	}
	
	/**
	 * Load a query file (such as "queries_robust04" in HW2).
	 *
	 * @param f A query file.
	 * @return A map storing the text query for each qid.
	 * @throws IOException
	 */
	public static Map<String, String> loadQueries( String f ) throws IOException {
		return loadQueries( new File( f ) );
	}
	
	/**
	 * Load a query file (such as "queries_robust04" in HW2).
	 *
	 * @param f A query file.
	 * @return A map storing the text query for each qid.
	 * @throws IOException
	 */
	public static Map<String, String> loadQueries( File f ) throws IOException {
		Map<String, String> queries = new TreeMap<>();
		BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( f ), "UTF-8" ) );
		String line;
		while ( ( line = reader.readLine() ) != null ) {
			String[] splits = line.split( "\t" );
			String qid = splits[0];
			String query = splits[1];
			queries.put( qid, query );
		}
		reader.close();
		return queries;
	}
	
}

package src;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An implementation of QL with Dirichlet smoothing.
 */
public abstract class AbstractQLSearcher {
	
	protected HashSet<String> stopwords;
	
	public abstract PostingList getPosting( String field, String term ) throws IOException;
	
	public abstract DocLengthReader getDocLengthReader( String field ) throws IOException;
	
	public abstract void close() throws IOException;
	
	protected AbstractQLSearcher() {
		this.stopwords = new HashSet<>();
	}
	
	public void setStopwords( Collection<String> stopwords ) {
		this.stopwords.addAll( stopwords );
	}
	
	public void setStopwords( String stopwordsPath ) throws IOException {
		setStopwords( new File( stopwordsPath ) );
	}
	
	public void setStopwords( File stopwordsFile ) throws IOException {
		BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( stopwordsFile ), "UTF-8" ) );
		String line;
		while ( ( line = reader.readLine() ) != null ) {
			line = line.trim();
			if ( line.length() > 0 ) {
				this.stopwords.add( line );
			}
		}
		reader.close();
	}
	
	public boolean isStopwords( String w ) {
		return stopwords.contains( w );
	}
	
	public List<SearchResult> search( String field, List<String> terms, double mu, int top ) throws IOException {
		Map<String, Double> tfreqs = new HashMap<>();
		for ( String term : terms ) {
			tfreqs.put( term, tfreqs.getOrDefault( term, 0.0 ) + 1.0 );
		}
		return search( field, tfreqs, mu, top );
	}
	
	public List<SearchResult> search( String field, Map<String, Double> termWeights, double mu, int top ) throws IOException {
		Map<PostingList, Double> postingWeights = new HashMap<>();
		List<PostingList> postings = new ArrayList<>();
		List<Double> weights = new ArrayList<>();
		for ( String term : termWeights.keySet() ) {
			PostingList list = getPosting( field, term );
			if ( !list.end() && !isStopwords( term ) ) {
				postings.add( list );
				postingWeights.put( list, termWeights.get( term ) );
			}
		}
		Collections.sort( postings, ( p1, p2 ) -> postingWeights.get( p1 ).compareTo( postingWeights.get( p2 ) ) );
		weights.addAll( postings.stream().map( postingWeights::get ).collect( Collectors.toList() ) );
		return search( postings, weights, getDocLengthReader( field ), mu, top );
	}
	
	private List<SearchResult> search( List<PostingList> postings, List<Double> weights, DocLengthReader doclen, double mu, int top ) throws IOException {
		
		int[] docs = new int[0];
		float[] scores = new float[0];
		int length = 0;
		
		double smooth_unseen_sum = 0;
		double smooth_unseen = 0;
		
		for ( int ix = 0; ix < postings.size(); ix++ ) {
			
			PostingList posting = postings.get( ix );
			if ( !posting.end() ) {
				
				int df = posting.df();
				double weight = weights.get( ix );
				double tf_smooth = mu * posting.totalFreq() / posting.corpusLength();
				
				smooth_unseen = weight * Math.log( tf_smooth );
				
				int[] docs_old = docs;
				float[] scores_old = scores;
				int length_old = length;
				docs = new int[df + length_old];
				scores = new float[df + length_old];
				length = merge( posting, weight, tf_smooth, smooth_unseen_sum, smooth_unseen,
						docs_old, scores_old, length_old, docs, scores );
				smooth_unseen_sum += smooth_unseen;
			}
		}
		
		double sumpwq = 0;
		for ( double pwq : weights ) {
			sumpwq += pwq;
		}
		
		for ( int ix = 0; ix < length; ix++ ) {
			scores[ix] -= sumpwq * Math.log( doclen.getLength( docs[ix] ) + mu );
		}
		
		PriorityQueue<SearchResult> pq = new PriorityQueue<>( ( r1, r2 ) -> {
			int cp = r1.getScore().compareTo( r2.getScore() );
			if ( cp == 0 ) {
				cp = r1.getDocid() - r2.getDocid();
			}
			return cp;
		} );
		for ( int ix = 0; ix < length; ix++ ) {
			if ( pq.size() < top ) {
				pq.add( new SearchResult( docs[ix], null, scores[ix] ) );
			} else {
				SearchResult result = pq.peek();
				if ( scores[ix] > result.getScore() ) {
					pq.poll();
					pq.add( new SearchResult( docs[ix], null, scores[ix] ) );
				}
			}
		}
		
		List<SearchResult> results = new ArrayList<>( pq.size() );
		results.addAll( pq );
		Collections.sort( results, ( o1, o2 ) -> o2.getScore().compareTo( o1.getScore() ) );
		return results;
	}
	
	private int merge( PostingList posting, double pwq, double tf_smooth, double smooth_unseen_sum, double smooth_unseen,
					   int[] docs_old, float[] scores_old, int length_old,
					   int[] docs, float[] scores ) throws IOException {
		int ix = 0;
		int ix_old = 0;
		while ( !posting.end() && ix_old < length_old ) {
			int doc1 = posting.doc();
			int doc2 = docs_old[ix_old];
			if ( doc1 < doc2 ) {
				docs[ix] = doc1;
				scores[ix] += smooth_unseen_sum + pwq * Math.log( posting.freq() + tf_smooth );
				ix++;
				posting.next();
			} else if ( doc1 > doc2 ) {
				docs[ix] = doc2;
				scores[ix] += scores_old[ix_old] + smooth_unseen;
				ix++;
				ix_old++;
			} else {
				docs[ix] = doc1;
				scores[ix] += scores_old[ix_old] + pwq * Math.log( posting.freq() + tf_smooth );
				ix++;
				posting.next();
				ix_old++;
			}
		}
		while ( !posting.end() ) {
			docs[ix] = posting.doc();
			scores[ix] += smooth_unseen_sum + pwq * Math.log( posting.freq() + tf_smooth );
			ix++;
			posting.next();
		}
		while ( ix_old < length_old ) {
			docs[ix] = docs_old[ix_old];
			scores[ix] += scores_old[ix_old] + smooth_unseen;
			ix++;
			ix_old++;
		}
		return ix;
	}
	
}

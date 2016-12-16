package src;

import java.util.*;

public class Utils {
	
	/**
	 * Norm the weights such that they sum up to 1.
	 *
	 * @param weights
	 */
	public static Map<String, Double> norm( Map<String, Double> weights ) {
		double sum = 0;
		for ( double weight : weights.values() ) {
			sum += weight;
		}
		HashMap<String, Double> normWeights = new HashMap<>();
		for ( String key : weights.keySet() ) {
			normWeights.put( key, weights.get( key ) / sum );
		}
		return normWeights;
	}
	
	/**
	 * Norm the weights such that they sum up to 1.
	 *
	 * @param weights
	 */
	public static Map<String, Double> getTop( Map<String, Double> weights, int top ) {
		List<String> entries = new ArrayList<>( weights.keySet() );
		Collections.sort( entries, ( w1, w2 ) -> weights.get( w2 ).compareTo( weights.get( w1 ) ) );
		Map<String, Double> topEntries = new HashMap<>();
		for ( int ix = 0; ix < top && ix < entries.size(); ix++ ) {
			topEntries.put( entries.get( ix ), weights.get( entries.get( ix ) ) );
		}
		return topEntries;
	}
	
	/**
	 * Norm the weights such that they sum up to 1.
	 *
	 * @param weights
	 */
	public static void printTop( Map<String, Double> weights, int top ) {
		List<String> entries = new ArrayList<>( weights.keySet() );
		Collections.sort( entries, ( w1, w2 ) -> weights.get( w2 ).compareTo( weights.get( w1 ) ) );
		for ( int ix = 0; ix < top && ix < entries.size(); ix++ ) {
			System.out.printf( "%-30s%10.8f\n", entries.get( ix ), weights.get( entries.get( ix ) ) );
		}
	}
	
}

package src;

import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LuceneQLSearcher extends AbstractQLSearcher {
	
	protected File dirBase;
	protected Directory dirLucene;
	protected IndexReader index;
	protected Map<String, DocLengthReader> doclens;
	
	public LuceneQLSearcher( String dirPath ) throws IOException {
		this( new File( dirPath ) );
	}
	
	public LuceneQLSearcher( File dirBase ) throws IOException {
		this.dirBase = dirBase;
		this.dirLucene = FSDirectory.open( this.dirBase.toPath() );
		this.index = DirectoryReader.open( dirLucene );
		this.doclens = new HashMap<>();
	}
	
	public IndexReader getIndex() {
		return this.index;
	}
	
	public PostingList getPosting( String field, String term ) throws IOException {
		return new LuceneTermPostingList( index, field, term );
	}
	
	public DocLengthReader getDocLengthReader( String field ) throws IOException {
		DocLengthReader doclen = doclens.get( field );
		if ( doclen == null ) {
			doclen = new FileDocLengthReader( this.dirBase, field );
			doclens.put( field, doclen );
		}
		return doclen;
	}
	
	public void close() throws IOException {
		index.close();
		dirLucene.close();
		for ( DocLengthReader doclen : doclens.values() ) {
			doclen.close();
		}
	}

    Map<String, Integer[]> wordFreqInDoc;
    // Janet's method
    public void getWordFrequencies(String field, List<String> terms) throws IOException{
        // Stores each term and an array indexed by docid that contains the frequency of that term in that doc
        wordFreqInDoc = new HashMap<>();
        PostingsEnum posting;
        for(String term: terms){
            posting = MultiFields.getTermDocsEnum( index, field, new BytesRef( term ), PostingsEnum.FREQS );
            Integer[] term_posting = new Integer[index.maxDoc()];
            if ( posting != null ) { // if the term does not appear in any document, the posting object may be null
                int docid;
                // Each time you call posting.nextDoc(), it moves the cursor of the posting list to the next position
                // and returns the docid of the current entry (document). Note that this is an internal Lucene docid.
                // It returns PostingsEnum.NO_MORE_DOCS if you have reached the end of the posting list.
                while ( ( docid = posting.nextDoc() ) != PostingsEnum.NO_MORE_DOCS ) {
					// get the frequency of the term in the current document
                    // and associate it with its docid
					term_posting[docid] = posting.freq();
                }
            }
            wordFreqInDoc.put(term, term_posting);
        }

    }

    public double calc_expansion(List<String> query_terms, Set<String> expansion_terms){
        if(wordFreqInDoc==null){
            System.out.println("ERROR: Word frequencies are null. Please run getWordFrequencies first.");
            return -1.0;
        }

        
        return 0.0;
    }

    // Janet's method
	public Map<String, Double> estimateMixtureModel( String field, List<String> terms, double lambda, int numfbdocs, int numfbterms ) throws IOException {
        // iterate through
        for(int id = 0; id < this.index.maxDoc(); id++){
//            double cnt_wordInDoc
        }

        return null;
    }

    private double t_n(int n, double lambda){
        return 0.0;
    }

    private double query_topic_model(int nPlusOne, String term, double lambda){
        int n = nPlusOne - 1;

        if(wordFreqInDoc==null){
            System.out.println("ERROR: Word frequencies are null. Please run getWordFrequencies first.");
            return -1.0;
        }

        if(n < 0) return 1; // stopping condition

        // order doesn't matter, so n will simply be the docid we retrieve
//        for(int docid = 0; docid < n; docid++) {
//            query_postings.get(term)[n]
//        }
        return 0.0;
    }

    // my method
    public Set<String> getVocab(String field, List<SearchResult> results) throws IOException{
        Set<String> voc = new HashSet<>();
        for ( SearchResult result : results ) {
            TermsEnum iterator = index.getTermVector( result.getDocid(), field ).iterator();
            BytesRef br;
            while ( ( br = iterator.next() ) != null ) {
                if ( !isStopwords( br.utf8ToString() ) ) {
                    voc.add( br.utf8ToString() );
                }
            }
        }

        return voc;
    }

	public Map<String, Double> estimateQueryModelRM1( String field, List<String> terms, double mu, double mufb, int numfbdocs, int numfbterms ) throws IOException {
		
		List<SearchResult> results = search( field, terms, mu, numfbdocs );
		Set<String> voc = getVocab(field, results);
		
		Map<String, Double> collector = new HashMap<>();
		for ( SearchResult result : results ) {
			double ql = result.getScore();
			double dw = Math.exp( ql );
			TermsEnum iterator = index.getTermVector( result.getDocid(), field ).iterator();
			Map<String, Integer> tfs = new HashMap<>();
			int len = 0;
			BytesRef br;
			while ( ( br = iterator.next() ) != null ) {
				tfs.put( br.utf8ToString(), (int) iterator.totalTermFreq() );
				len += iterator.totalTermFreq();
			}
			for ( String w : voc ) {
				int tf = tfs.getOrDefault( w, 0 );
				double pw = ( tf + mufb * index.totalTermFreq( new Term( field, w ) ) / index.getSumTotalTermFreq( field ) ) / ( len + mufb );
				collector.put( w, collector.getOrDefault( w, 0.0 ) + pw * dw );
			}
		}
		return Utils.getTop( Utils.norm( collector ), numfbterms );
	}
	
	public Map<String, Double> estimateQueryModelRM3( List<String> terms, Map<String, Double> rm1, double weight_org ) throws IOException {
		
		Map<String, Double> mle = new HashMap<>();
		for ( String term : terms ) {
			mle.put( term, mle.getOrDefault( term, 0.0 ) + 1.0 );
		}
		for ( String w : mle.keySet() ) {
			mle.put( w, mle.get( w ) / terms.size() );
		}
		
		Set<String> v = new TreeSet<>();
		v.addAll( terms );
		v.addAll( rm1.keySet() );
		
		Map<String, Double> rm3 = new HashMap<>();
		for ( String w : v ) {
			rm3.put( w, weight_org * mle.getOrDefault( w, 0.0 ) + ( 1 - weight_org ) * rm1.getOrDefault( w, 0.0 ) );
		}
		
		return rm3;
	}
	
}

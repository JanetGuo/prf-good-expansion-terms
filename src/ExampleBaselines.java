package src;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExampleBaselines {
	
	public static void main( String[] args ) throws Exception {
		
		String pathIndex = "/home/janet/Documents/Courses/InfoRetr_Jiang/final_project/index_trec123";
		Analyzer analyzer = LuceneUtils.getAnalyzer( LuceneUtils.Stemming.Krovetz );
		
		String pathStopwords = "/home/janet/Documents/Courses/InfoRetr_Jiang/final_project/stopwords_inquery"; // change to your stop words path
		
		String fieldDocno = "docno";
		String fieldSearch = "content";
		
		String query = "president clinton";
		
		LuceneQLSearcher searcher = new LuceneQLSearcher( pathIndex );
		searcher.setStopwords( pathStopwords );
		
		// retrieve the top 10 results
		int top = 10;
		
		// QL dirichlet smoothing settings
		double mu = 1000;
		
		// RM1 settings
		double mufb = 0;
		int numfbdoc = 10;
		int numfbterms = 100;
		
		// RM3 settings
		double weightOriginalQuery = 0.5;

		String run = "defaults";

        if(run.contains("defaults")) {
            // Get QL search results
            List<SearchResult> QLresults = searcher.search(fieldSearch, LuceneUtils.tokenize(query, analyzer), mu, top);
            for (int ix = 0; ix < QLresults.size(); ix++) {
                QLresults.get(ix).setDocno(LuceneUtils.getDocno(searcher.getIndex(), fieldDocno, QLresults.get(ix).getDocid()));
                System.out.printf("%-10s%-6d%-15d%-25s%10.4f\n", "QL", (ix + 1), QLresults.get(ix).getDocid(), QLresults.get(ix).getDocno(), QLresults.get(ix).getScore());
            }

            // Get RM1 search results
            Map<String, Double> rm1 = searcher.estimateQueryModelRM1(fieldSearch, LuceneUtils.tokenize(query, analyzer), mu, mufb, numfbdoc, numfbterms);
            List<SearchResult> RM1results = searcher.search(fieldSearch, rm1, mu, top);
            for (int ix = 0; ix < RM1results.size(); ix++) {
                RM1results.get(ix).setDocno(LuceneUtils.getDocno(searcher.getIndex(), fieldDocno, RM1results.get(ix).getDocid()));
                System.out.printf("%-10s%-6d%-15d%-25s%10.4f\n", "RM1", (ix + 1), RM1results.get(ix).getDocid(), RM1results.get(ix).getDocno(), RM1results.get(ix).getScore());
            }

            // Get RM3 search results
            Map<String, Double> rm3 = searcher.estimateQueryModelRM3(LuceneUtils.tokenize(query, analyzer), rm1, weightOriginalQuery);
            List<SearchResult> RM3results = searcher.search(fieldSearch, rm3, mu, top);
            for (int ix = 0; ix < RM3results.size(); ix++) {
                RM3results.get(ix).setDocno(LuceneUtils.getDocno(searcher.getIndex(), fieldDocno, RM3results.get(ix).getDocid()));
                System.out.printf("%-10s%-6d%-15d%-25s%10.4f\n", "RM3", (ix + 1), RM3results.get(ix).getDocid(), RM3results.get(ix).getDocno(), RM3results.get(ix).getScore());
            }
        }
        if(run.contains("testing")){
            System.out.println(searcher.getIndex().maxDoc());
            Document test = searcher.getIndex().document(0);
            System.out.println(test.getField(fieldSearch));
            System.out.println(searcher.getIndex().getTermVector(0, fieldSearch));
        }
        if(run.contains("testRM1")){
            // Get QL search results
            List<SearchResult> QLresults = searcher.search(fieldSearch, LuceneUtils.tokenize(query, analyzer), mu, top);
            for (int ix = 0; ix < QLresults.size(); ix++) {
                QLresults.get(ix).setDocno(LuceneUtils.getDocno(searcher.getIndex(), fieldDocno, QLresults.get(ix).getDocid()));
                System.out.printf("%-10s%-6d%-15d%-25s%10.4f\n", "QL", (ix + 1), QLresults.get(ix).getDocid(), QLresults.get(ix).getDocno(), QLresults.get(ix).getScore());
            }

            Set<String> voc = searcher.getVocab(fieldSearch, QLresults);
            for(String term : voc){

            }

        }
		searcher.close();
		
	}
	
}

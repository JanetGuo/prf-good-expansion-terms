package src;

public class ExampleGetContent {
	
	public static void main( String[] args ) throws Exception {
		
		String pathIndex = "/home/janet/Documents/Courses/InfoRetr_Jiang/final_project/index_trec123";
		
		LuceneQLSearcher searcher = new LuceneQLSearcher( pathIndex );
		
		String content = searcher.index.document( 1129024 ).get( "content" );
		String html = searcher.index.document( 1129024 ).get( "html" );
		
		System.out.println( content );
		System.out.println( "--------------------------------------------------------------" );
		System.out.println( html );
		
		searcher.close();
		
	}
	
}

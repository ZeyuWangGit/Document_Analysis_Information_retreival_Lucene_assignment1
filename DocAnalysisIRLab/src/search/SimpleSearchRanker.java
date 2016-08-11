/** Exhibits standard Lucene searches for ranking documents.
 * 
 * @author Scott Sanner, Paul Thomas
 */

package search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

public class SimpleSearchRanker {

	String        _indexPath;
	StandardQueryParser   _parser;
	IndexReader   _reader;
	IndexSearcher _searcher;
	DecimalFormat _df = new DecimalFormat("#.####");
	
	public SimpleSearchRanker(String index_path, String default_field, Analyzer a) 
		throws IOException {
		_indexPath = index_path;
		Directory d = new SimpleFSDirectory(Paths.get(_indexPath));
		DirectoryReader dr = DirectoryReader.open(d);
		_searcher  = new IndexSearcher(dr);
		_parser    = new StandardQueryParser(a);
	}
	
	public void doSearch(String query, int num_hits, PrintStream ps, String num) 
		throws Exception {
		
		Query q = _parser.parse(query, "CONTENT");
		TopScoreDocCollector collector = TopScoreDocCollector.create(num_hits);
		_searcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		//ps.println("Found " + hits.length + " hits " + " for query " + query + ":");
		
		for (int i = 0; i < hits.length; i++) {
		    int docId = hits[i].doc;
		    Document d = _searcher.doc(docId);
		    //ps.println((i + 1) + ". (" + _df.format(hits[i].score) + ") " + d.get("PATH"));
		    ps.print(num + " Q0 "+ d.get("PATH").substring(14, 21) + " ");
		    ps.print(i+1);
		    ps.print(" " + _df.format(hits[i].score) + " myname");
		    ps.println();
		    
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		String index_path = "src/search/lucene.index";
		String default_field = "CONTENT";
		String topic_path = "src/topics/air.topics";
		String output_path = "src/retrieved.txt";
		
		FileIndexBuilder b = new FileIndexBuilder(index_path);
		SimpleSearchRanker r = new SimpleSearchRanker(b._indexPath, default_field, b._analyzer);
		
		System.out.println("###########");
		File f=new File(output_path);  
        f.createNewFile();  
        FileOutputStream fileOutputStream = new FileOutputStream(f);  
        PrintStream printStream = new PrintStream(fileOutputStream);
		
		// See the following for query parser syntax
		//   https://lucene.apache.org/core/5_2_0/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#package_description
		//
		// IN SHORT: the default scoring function for OR terms is a variant of TF-IDF
		//           where one can individually boost the importance of query terms with
		//           a multipler using ^
		
		// Standard single term
		r.doSearch("Obama", 5, printStream, "01");

		// Multiple term (implicit OR)
		r.doSearch("Obama Hillary", 5, System.out, "01");

		// Wild card
		r.doSearch("Ob*ma", 5, System.out, "01");
		
		// Edit distance
		r.doSearch("Obama~.4", 5, System.out, "01");
		
		// Fielded search (FIELD:...), boolean (AND OR NOT)
		r.doSearch("FIRST_LINE:Obama AND Hillary", 5, System.out, "01");
		r.doSearch("FIRST_LINE:Obama AND NOT Hillary", 5, System.out, "01");

		// Phrase search (slop factor ~k allows words to be within k distance)
		r.doSearch("\"Barack Obama\"", 5, System.out, "01");
		r.doSearch("\"Barack Obama\"~5", 5, System.out, "01");
		
		// Note: can boost terms or subqueries using ^ (e.g., ^10 or ^.1) -- default is 1 
		r.doSearch("Obama^10 Hillary^0.1", 5, System.out, "01");
		r.doSearch("(FIRST_LINE:\"Barack Obama\")^10 OR Hillary^0.1", 5, System.out, "01");

		// Reversing boost... see change in ranking 
		r.doSearch("Obama^0.1 Hillary^10", 5, System.out, "01");
		r.doSearch("(FIRST_LINE:\"Barack Obama\")^0.1 OR Hillary^10", 5, System.out, "01");

		// Complex query
		r.doSearch("(FIRST_LINE:\"Barack Obama\"~5^10 AND Obama~.4) OR Hillary", 5, System.out, "01");
		
		
        
		System.out.println("###########");
		BufferedReader buffread = new BufferedReader (new InputStreamReader(new FileInputStream(topic_path)));
		String read = "";
		String search_phase = "";
		String numb;
		while((read = buffread.readLine())!=null){
			StringTokenizer st = new StringTokenizer(read);
			numb = st.nextToken() ;
			
			search_phase = st.nextToken(); 
			while(st.hasMoreTokens()){
				search_phase = search_phase + " " + st.nextToken();
			}
			r.doSearch(search_phase, 5, printStream, numb);
		}	
	}
}

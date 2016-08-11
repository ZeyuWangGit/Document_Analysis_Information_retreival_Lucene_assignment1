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
import java.io.StringReader;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
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
		    //ps.print(num + " Q0 "+ d.get("PATH").substring(14, 21) + " ");
		    ps.print(num + " Q0 "+ d.get("PATH").substring(17, 31) + " ");
		    ps.print(i+1);
		    ps.print(" " + _df.format(hits[i].score) + " myname");
		    ps.println();
		    
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		String index_path = "src/search/lucene.index";
		String default_field = "CONTENT";
		String topic_path = "src/topics/gov.topics";
		String output_path = "src/retrieved.txt";
		
		FileIndexBuilder b = new FileIndexBuilder(index_path);
		SimpleSearchRanker r = new SimpleSearchRanker(b._indexPath, default_field, b._analyzer);
		
		System.out.println("###########");
		File f=new File(output_path);  
        f.createNewFile();  
        FileOutputStream fileOutputStream = new FileOutputStream(f);  
        PrintStream printStream = new PrintStream(fileOutputStream);
        
        //Read and token part for lucene
		BufferedReader buffread = new BufferedReader (new InputStreamReader(new FileInputStream(topic_path)));
		String read = "";
		String search_phase = "";
		String stop_phase = "";
		String numb;
		
		while((read = buffread.readLine())!=null){
			StringTokenizer st = new StringTokenizer(read);
			numb = st.nextToken() ;
			search_phase = read.substring(2);
			
			//Whitespacetokenizer
	        Tokenizer search_phase_Token = new WhitespaceTokenizer();
	        search_phase_Token.setReader(new StringReader(search_phase));
	        
	        //Stopwords Filter
	        TokenStream tokenStream = new StopFilter(search_phase_Token, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
	        
	        //Stem Filter
	        tokenStream = new PorterStemFilter(tokenStream);
	        
	        //LowerCase filter
	        tokenStream = new LowerCaseFilter(tokenStream);
	        
	        //Standard Filter
	        tokenStream = new StandardFilter(tokenStream);
	        
	        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
	        tokenStream.reset();

	        stop_phase = charTermAttribute.toString();
	        while (tokenStream.incrementToken()) {
	            stop_phase = stop_phase + " " + charTermAttribute.toString();
	        }

	        tokenStream.end();
	        tokenStream.close();
			System.out.println(stop_phase);
			r.doSearch(stop_phase, 50, printStream, numb);
		}
		
		
		
	}

}

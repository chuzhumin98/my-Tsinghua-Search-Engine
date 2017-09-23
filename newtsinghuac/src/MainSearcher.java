import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class MainSearcher {
	private IndexReader reader;
	public IndexSearcher searcher;
	public Analyzer analyzer;
	private float avgLength=1.0f;
	private int featurenum = 10;
	private String field[] = {"href", "title", "h12", "h36", "strong", "content", "titleattr", "alt", "description", "keywords"};
	private float boostvalue[] = {5, 10, 9, 6, 8, 9, 1, 1, 8, 8};
	private Map<String , Float> boosts;
	public Map<Integer, Float> sortlist;
	
	public MainSearcher(String indexdir)
	throws Exception{
		reader = IndexReader.open(FSDirectory.open(new File(indexdir)));
		searcher = new IndexSearcher(reader);
		searcher.setSimilarity(new BM25Similarity());
		analyzer = new IKAnalyzer(true);
		boosts = new HashMap<String, Float>();
		for (int i = 0;i < featurenum;i ++)
			boosts.put(field[i], boostvalue[i]);
		sortlist = new HashMap<Integer, Float>();
	}
	
	public Query searchQuery(String queryString){
		MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_40, field, analyzer, boosts);
		parser.setDefaultOperator(QueryParser.AND_OPERATOR);
		Query query = null;
		try {
			query = parser.parse(queryString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return query;
	}
	
	public Document getDoc(int docID){
		try{
			return searcher.doc(docID);
		}catch(IOException e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args)
	throws Exception{
		MainSearcher main = new MainSearcher("forindex/index");
		String keyword = "Ñ§ÌÃÂ·";
		Query query = main.searchQuery(keyword);
		TopDocs results = main.searcher.search(query, 1000);
		ScoreDoc[] hits = results.scoreDocs;
		System.out.println(hits.length);
		for (int i = 0;i < hits.length;i ++){
			Document doc = main.getDoc(hits[i].doc);
			float point = Float.parseFloat(doc.get("pagerank")) * hits[i].score;
			main.sortlist.put(i, point);
		}
		List<Entry<Integer, Float>> list =
			    new ArrayList<Entry<Integer, Float>>(main.sortlist.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Integer, Float>>() {
		    public int compare(Map.Entry<Integer, Float> o1,
		            Map.Entry<Integer, Float> o2) {
		    	if (o2.getValue() - o1.getValue() > 0)
		    		return 1;
		    	else if (o2.getValue() - o1.getValue() < 0)
		    		return -1;
		    	else 
		    		return 0;
		    }
		});
		for (int i = 0;i < 100;i++) { // output raw format
			int index = list.get(i).getKey();
			System.out.println("original rank is: " + index + " now rank is: " + i);
			Document doc = main.getDoc(hits[index].doc);
			System.out.println("doc=" + hits[index].doc + " score=" + hits[index].score+ " pagerank=" +
					doc.get("pagerank") + " fin:" + list.get(i).getValue());
			System.out.println("id= "+doc.get("id") + " filepath= " +doc.get("filepath"));
			String content = doc.get("content") + " " + doc.get("title") + " "
			+ " " + doc.get("h12") + " " + doc.get("h36") + doc.get("href");
			SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color='red'>","</font></b>");
			Highlighter highlighter = new Highlighter(simpleHTMLFormatter,new QueryScorer(query));
			TokenStream tokenstream=main.analyzer.tokenStream(keyword, new StringReader(content));
			String highLightText = highlighter.getBestFragment(tokenstream, content);
			System.out.println("highlight: " + highLightText);
		}
	}
}

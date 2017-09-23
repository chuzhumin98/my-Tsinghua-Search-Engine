package pagerank;

import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;   
import org.wltea.analyzer.lucene.IKAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class XmlParseLucene {
	HashMap<String,Integer> source = new HashMap<String,Integer>(); 
	public void indexSpecialFile(String filename){
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();   
			DocumentBuilder db = dbf.newDocumentBuilder();    
			org.w3c.dom.Document doc = db.parse(new File(filename));
			System.out.println(db);
			NodeList nodeList = doc.getElementsByTagName("doc");
			for(int i=0;i<nodeList.getLength();i++){
				Node node=nodeList.item(i);
				NamedNodeMap map=node.getAttributes();
				Node locate=map.getNamedItem("locate");
				Node id=map.getNamedItem("id");
				source.put(locate.getNodeValue(), new Integer(id.getNodeValue()));
				String absString=id.getNodeValue()+id.getNodeValue();
				if(i%10000==0){
					System.out.println("process "+i);
				}
				//TODO: add other fields such as html title or html content 				
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println(source.size());
	}
	public static void main(String[] args) {
		XmlParseLucene indexer=new XmlParseLucene();
		indexer.indexSpecialFile("D:/workspace/pagerank/src/news.xml");
		RegTest r1 = new RegTest(indexer);				
		System.out.println(indexer.source.get("thunewsen/9707/20160107/9181452153213459.pdf"));
		int count = 0;
		for (String filepath : indexer.source.keySet()) {
			int n = indexer.source.get(filepath);
			r1.getURLs(r1.path+filepath, n);
			count++;
			if (count % 1000 == 0) {
				System.out.println(count);
			}
		}
		System.out.println("OK");
	}
}

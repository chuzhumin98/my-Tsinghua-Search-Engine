import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleDocValuesField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.htmlparser.Node;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.htmlparser.Parser;
import org.htmlparser.filters.*;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.*;
import org.htmlparser.NodeFilter;
import org.wltea.analyzer.lucene.IKAnalyzer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

import org.apache.lucene.document.Field;
public class MainIndexer {
	private Analyzer analyzer; 
    private IndexWriter indexWriter;
    private float averageLength=1.0f;
    private String mainPath = "D:/workspace/mys/jobs/news.tsinghua.edu.cnV8-20170513001935154/mirror/news.tsinghua.edu.cn/publish/";
    //now split: hrefname, title, h1/2, h3-6, strong/b/i, p, title(attr), alt, meata(description), meta(keyword)
    private int featureNum = 10;
    private String tagOrAttr[] = {"t", "t", "t", "t", "t", "t", "a", "a", "v", "v"};
    private String fieldName[] = {"href", "title", "h12", "h36", "strong", "content", "titleattr", "alt", "description", "keywords"};
    private String keyName[]   = {"a", "title", "h1 h2", "h3 h4 h5 h6", "strong b i em", "p", "title", "alt", "description", "keywords"};
    //private int boost[] = {5, 10, 9, 6, 2, 7, 1, 1, 8, 8};
    
	public MainIndexer(String indexDir)
	throws Exception{
//		Directory dir=FSDirectory.open(Paths.get(indexDir));
//		IndexWriterConfig iwc = new IndexWriterConfig(new StandardAnalyzer());
		Directory dir=FSDirectory.open(new File(indexDir));
	    IndexWriterConfig iwc=new IndexWriterConfig(Version.LUCENE_40, new IKAnalyzer(true));
		iwc.setSimilarity(new BM25Similarity());
		iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		indexWriter = new IndexWriter(dir, iwc);
	}
	
	private String getTagText(String tag, String filecontent, String charset)
	throws Exception{
		Parser parser = MyParser.createParser(filecontent, charset);
		String splitResult[] = tag.split(" ");
		NodeFilter filter = new TagNameFilter(splitResult[0]);
		for (int i = 1;i < splitResult.length;i ++)
			filter = new OrFilter(filter, new TagNameFilter(splitResult[i]));
		NodeList nodes = parser.extractAllNodesThatMatch(filter);
		String totalStr = "";
		for (int index = 0;index < nodes.size();index ++){
			Node n = (Node)nodes.elementAt(index);
			totalStr += (n.toPlainTextString() + " ");
		}
		return totalStr;
	}
	
	private String getAttrText(String attr, String filecontent, String charset)
	throws Exception{
		Parser parser = MyParser.createParser(filecontent, charset);
		NodeFilter filter = new HasAttributeFilter(attr);
		NodeList nodes = parser.extractAllNodesThatMatch(filter);
		String totalStr = "";
		for (int i = 0;i < nodes.size();i ++){
			String text = ((TagNode)nodes.elementAt(i)).getAttribute(attr);
			if (text != null)
				totalStr += (text + " ");
		}
		return totalStr;
	}
	
	private String getValueText(String attrvalue, String filecontent, String charset)
	throws Exception{
		Parser parser = MyParser.createParser(filecontent, charset);
		NodeFilter filter = new HasAttributeFilter("name", attrvalue);
		NodeList nodes = parser.extractAllNodesThatMatch(filter);
		String totalStr = "";
		for (int i = 0;i < nodes.size();i ++){
			String text = ((TagNode)nodes.elementAt(i)).getAttribute("content");
			totalStr += (text + " ");
		}
		return totalStr;
	}
	
	private String getTime(String filepath, String filecontent, String charset)
	throws Exception{
		Parser parser = MyParser.createParser(filecontent, charset);
		NodeFilter filter = new HasAttributeFilter("class", "articletime");
		NodeList nodes = parser.extractAllNodesThatMatch(filter);
		String totalStr = "";
		for (int i = 0;i < nodes.size();i ++){
			Node n = (Node)nodes.elementAt(i);
			String time = n.toPlainTextString().split(" ")[0].replaceAll("[ÄêÔÂÈÕ]", "-");
			totalStr += (time.substring(0, time.length() - 1) + " ");
		}
		if (totalStr.equalsIgnoreCase("")){
			try{
				Parser newparser = new Parser("http://news.tsinghua.edu.cn/publish/" + filepath);
				URLConnection urs = newparser.getConnection();
				String time = urs.getHeaderField("Last-modified");
				Date date = new Date(time);
		        totalStr += (dateToString(date).split(" ")[0] + " ");
			}
			catch (Exception e){
				totalStr = "Date not available";
			}
		}
		return totalStr;
	}
	
	private void readXML(String filePath)
	throws Exception{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		org.w3c.dom.Document doc;
		org.w3c.dom.NodeList nodeList;
		
		String tagList[] = {"pdf", "word"};
		for (int k = 0;k < tagList.length;k ++){
			doc = db.parse(new File(filePath + tagList[k] + ".xml"));
			nodeList = doc.getElementsByTagName(tagList[k]);
			System.out.println(tagList[k] + ":" + nodeList.getLength());
			for (int i = 0;i < nodeList.getLength();i ++){
				org.w3c.dom.Node node=nodeList.item(i);
				NamedNodeMap map=node.getAttributes();
				org.w3c.dom.Node id=map.getNamedItem("id");
				org.w3c.dom.Node locate=map.getNamedItem("locate");
				org.w3c.dom.Node pagerank=map.getNamedItem("pagerank");
				String nowPath = mainPath + locate.getNodeValue();
				Document document = null;
				if (k == 0)
					document = readPDF(nowPath);
				else
					document = readWord(nowPath);
				Field idfield = new Field("id", id.getNodeValue(), Field.Store.YES, Field.Index.NO);
				Field pathfield = new Field("filepath", locate.getNodeValue(), Field.Store.YES, Field.Index.NO);
				Field pagerankfield = new Field("pagerank", pagerank.getNodeValue(), Field.Store.YES, Field.Index.NO);
				document.add(idfield);
				document.add(pathfield);
				document.add(pagerankfield);
				averageLength += locate.getNodeValue().length();
				indexWriter.addDocument(document);
			}
		}
		
		doc = db.parse(new File(filePath + "html.xml"));
		nodeList = doc.getElementsByTagName("doc");
		System.out.println("html:" + nodeList.getLength());
		for(int i=0;i<nodeList.getLength();i++){
			org.w3c.dom.Node node=nodeList.item(i);
			NamedNodeMap map=node.getAttributes();
			org.w3c.dom.Node id=map.getNamedItem("id");
			org.w3c.dom.Node locate=map.getNamedItem("locate");
			org.w3c.dom.Node pagerank=map.getNamedItem("pagerank");
			String nowPath = mainPath + locate.getNodeValue();
			String filecontent = new String(Files.readAllBytes(Paths.get(nowPath)));
			filecontent = filecontent.replaceAll("<script[\\s\\S]*?</script>", "");
			String encoding = "utf-8";
			Pattern pp = Pattern.compile("charset\\s*=\"*\\s*([0-9a-zA-Z-_]+)\"*");
			Matcher m = pp.matcher(filecontent);
			if (m.find())
				encoding = m.group(1);
			Document document = new Document();
			Field idfield = new Field("id", id.getNodeValue(), Field.Store.YES, Field.Index.NO);
			Field pathfield = new Field("filepath", locate.getNodeValue(), Field.Store.YES, Field.Index.NO);
			Field pagerankfield = new Field("pagerank", pagerank.getNodeValue(), Field.Store.YES, Field.Index.NO);
			Field postTime = new Field("posttime", getTime(locate.getNodeValue() ,filecontent, encoding), Field.Store.YES, Field.Index.NO);
			document.add(idfield);
			document.add(pathfield);
			document.add(pagerankfield);
			document.add(postTime);
			averageLength += locate.getNodeValue().length();
			for (int j = 0;j < featureNum;j ++){
				String resultText = "";
				if (tagOrAttr[j].equalsIgnoreCase("t"))
					resultText = getTagText(keyName[j], filecontent, encoding);
				else if (tagOrAttr[j].equalsIgnoreCase("a"))
					resultText = getAttrText(keyName[j], filecontent, encoding);
				else
					resultText = getValueText(keyName[j], filecontent, encoding);
				Field field = new Field(fieldName[j], resultText, Field.Store.YES, Field.Index.ANALYZED);
				document.add(field);
				averageLength += resultText.length();
			}
			indexWriter.addDocument(document);
			if(i%1000==0){
				System.out.println("process "+i);
			}
		}
		
		
		averageLength /= indexWriter.numDocs();
		indexWriter.close();
	}
	
	public void saveGlobals(String filename){
    	try{
    		PrintWriter pw=new PrintWriter(new File(filename));
    		pw.println(averageLength);
    		pw.close();
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }
	
	public String splitWord(String keyword)
	throws Exception{
		IKAnalyzer ikanalyzer = new IKAnalyzer(true);
		StringReader reader = new StringReader(keyword);
		TokenStream ts = ikanalyzer.tokenStream("", reader);
		CharTermAttribute term=ts.getAttribute(CharTermAttribute.class);
		String splitStr = "";
		while(ts.incrementToken()){
			splitStr += term.toString() + " ";
        }
		ikanalyzer.close();
		return splitStr;
	}
	
	public String dateFormat(Calendar calendar){  
        if( null == calendar )  
            return null;  
        String date = null;    
        String pattern = "yyyy-MM-dd HH:mm:ss";  
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        date = format.format(calendar.getTime());  
        return date == null ? "" : date;  
    }
	
	public String dateToString(Date time){ 
	    SimpleDateFormat formatter;
	    formatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
	    String ctime = formatter.format(time);
	    return ctime;
	} 

	
	public Document readPDF(String filepath){
		File pdfFile = new File(filepath);
		PDDocument document = null;
		Document result = new Document();
		try {
			document = PDDocument.load(pdfFile);
			int pageNum = document.getNumberOfPages();
			PDDocumentInformation info = document.getDocumentInformation();
			String postTime = dateFormat(info.getModificationDate()).split(" ")[0];
			PDFTextStripper stripper=new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setStartPage(1);
            stripper.setEndPage(pageNum);
            String content = stripper.getText(document);
            String title = info.getTitle();
            if (title == null)
            	title = content.split("[\n\r]",2)[0];
            Field titlefield = new Field("title", title, Field.Store.YES, Field.Index.ANALYZED);
            Field contentfield = new Field("content", content, Field.Store.YES, Field.Index.ANALYZED);
            Field timefield = new Field("posttime", postTime, Field.Store.YES, Field.Index.NO);
            result.add(titlefield);
            result.add(contentfield);
            result.add(timefield);
            averageLength += title.length() + content.length();
            document.close();
		} catch (InvalidPasswordException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public Document readWord(String filepath){
		Document result = new Document();
		try {
			InputStream is = new FileInputStream(new File(filepath));
			WordExtractor ex = new WordExtractor(is);
			String title = ex.getSummaryInformation().getTitle();
			Date date = ex.getSummaryInformation().getLastSaveDateTime();
			String postTime = dateToString(date).split(" ")[0];
			String content = ex.getText();
			if (title == null)
				title = content.split("[\n\r]",2)[0];
			Field titlefield = new Field("title", title, Field.Store.YES, Field.Index.ANALYZED);
            Field contentfield = new Field("content", content, Field.Store.YES, Field.Index.ANALYZED);
            Field timefield = new Field("posttime", postTime, Field.Store.YES, Field.Index.NO);
            result.add(titlefield);
            result.add(contentfield);
            result.add(timefield);
            averageLength += title.length() + content.length();
            ex.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static void main(String []args)
	throws Exception{
		MainIndexer mainindexer = new MainIndexer("forindex/index");
		mainindexer.readXML("");//path of folder where xml(html.xml, pdf.xml, word.xml) stored.
		mainindexer.saveGlobals("forIndex/global.txt");
//		MainIndexer mainindexer = new MainIndexer("forindex/index");
//		mainindexer.readPDF("1.pdf");
//		mainindexer.readWord("1.doc");
//		MainIndexer mainindexer = new MainIndexer("forindex/index");
//		String filepath = "/Users/huanghuirong/news.tsinghua.edu.cn/publish/thunews/9760/2011/20110225231438312330396/20110225231438312330396_.html";
//		String filecontent = new String(Files.readAllBytes(Paths.get(filepath)));
//		System.out.println(mainindexer.getTime(filecontent, "utf-8"));
//		MainIndexer mainindexer = new MainIndexer("forindex/index");
//		 Parser parser = new Parser("http://news.tsinghua.edu.cn/publish/thunews/10303/2017/20170531141100031923814/20170531141100031923814_.html");
//	     URLConnection urs = parser.getConnection();
//	     String time = urs.getHeaderField("Last-modified");
//	     Date date = new Date(time);
//	     System.out.println("time==============="+time);
//	     System.out.println("time==============="+mainindexer.dateToString(date).split(" ")[0]);
	}
}

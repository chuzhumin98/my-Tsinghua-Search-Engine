


import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;

public class MainServer extends HttpServlet{
	public static final int PAGE_RESULT=10;
	public static final String picDir="";
	private MainSearcher mainsearcher = null;
	private int maxDoc = 1000;
	private String[] titleList;
	private String[] abstractList;
	private String pagelink;
	private String[] linkList;
	private String[] postTimeList;
	String path = "D:/workspace/newtsinghuac/";
	public MainServer()
	throws Exception{
		super();
		mainsearcher = new MainSearcher(path+"forIndex/index");
		titleList = null;
		abstractList = null;
		pagelink = null;
		System.out.println("in MainServer");
	}
	
	public int doQuery(String keyword, int page, String rankString)
	throws Exception{
		Query query = mainsearcher.searchQuery(keyword);
		TopDocs results = mainsearcher.searcher.search(query, maxDoc);
		ScoreDoc[] hits = results.scoreDocs;
		for (int i = 0;i < hits.length;i ++){
			Document doc = mainsearcher.getDoc(hits[i].doc);
			float point = Float.parseFloat(doc.get("pagerank")) * hits[i].score;
			mainsearcher.sortlist.put(i, point);
		}
		List<Entry<Integer, Float>> list =
			    new ArrayList<Entry<Integer, Float>>(mainsearcher.sortlist.entrySet());
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
		titleList = null;
		abstractList = null;
		pagelink = null;
		linkList = null;
		postTimeList = null;
		System.out.println("hitslength:"+hits.length);
		if (hits.length > (page-1)*PAGE_RESULT){
			int start=Math.max((page-1)*PAGE_RESULT, 0);
			int docnum=Math.min(hits.length-start,PAGE_RESULT);
			if (hits.length-start >= 0 && hits.length-start < docnum) { //去除无用结果
				docnum = hits.length-start;
			} else {
				if (hits.length-start < 0) {
					docnum = 0;
				}
			}
			System.out.println("docnum:"+docnum);
			titleList = new String[docnum];
			abstractList = new String[docnum];
			linkList = new String[docnum];
			postTimeList = new String[docnum];
			for (int i = start;i < start + docnum;i ++){
				try{
					int index = list.get(i).getKey();
					Document doc = mainsearcher.getDoc(hits[index].doc);
					titleList[i-start] = doc.get("title").replaceAll(" Untitled Document", "");
					linkList[i-start] = doc.get("filepath");
					postTimeList[i - start] = doc.get("posttime");
					System.out.println("doc\nid:" + doc.get("id") + "locate:" + doc.get("locate"));
					String content = doc.get("content") + " " + doc.get("title") + " "
							+ " " + doc.get("h12") + " " + doc.get("h36") + doc.get("href");
					SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color='red'>","</font></b>");
					Highlighter highlighter = new Highlighter(simpleHTMLFormatter,new QueryScorer(query));
					TokenStream tokenstream = mainsearcher.analyzer.tokenStream(keyword, new StringReader(content));
					try {
						abstractList[i-start] = highlighter.getBestFragment(tokenstream, content);
					} catch (InvalidTokenOffsetsException e) {
						e.printStackTrace();
					}
				}catch (Exception e) {
					System.out.println("java.lang.ArrayIndexOutOfBoundsException");
				}
				
			}
			if (rankString != null){
				int rank = Integer.parseInt(rankString);// 0~n-1
				int index = list.get(start + rank).getKey();
				Document doc = mainsearcher.getDoc(hits[index].doc);
				pagelink = "news.tsinghua.edu.cn/publish/" + doc.get("filepath");
				System.out.println(pagelink);
			}
		} 
		return hits.length;
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException{
		try {
			mainsearcher = new MainSearcher(path+"forIndex/index");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		String keyword=request.getParameter("query");
		String pageString=request.getParameter("page");
		String rankString=request.getParameter("rank");
		int page=1;
		int hitslength = 0;
		if(pageString!=null){
			page=Integer.parseInt(pageString);
		}
		System.out.println("page:"+page);
		if(keyword==null){
			System.out.println("null query");
		}else{
			try {
				hitslength = doQuery(keyword, page, rankString);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (rankString == null){
				request.setAttribute("currentQuery", keyword);
				request.setAttribute("currentPage", page);
				request.setAttribute("title", titleList);
				request.setAttribute("abstract", abstractList);
				request.setAttribute("link", linkList);
				request.setAttribute("posttime", postTimeList);
				request.setAttribute("hitslength", hitslength);
				request.getRequestDispatcher("/resultShow.jsp").forward(request,
						response);
			}
			else{
				request.setAttribute("pageLink", pagelink);
				request.getRequestDispatcher("/pageShow.jsp").forward(request,
						response);
			}
		}
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		this.doGet(request, response);
	}
}



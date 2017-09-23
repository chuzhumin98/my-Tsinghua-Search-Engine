package pagerank;

  

import java.io.File;  
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;  
import java.util.List;  
  
import org.dom4j.Document;  
import org.dom4j.DocumentException;  
import org.dom4j.Element;  
import org.dom4j.io.SAXReader;  
  
public class ParseXml {  
    public void read() throws Exception {  
        SAXReader reader = new SAXReader();  
        Document document = reader.read(new File("D:/workspace/pagerank/src/news.xml"));  
        Element root = document.getRootElement(); 
        System.out.println(root);
        //将解析出来的allresource下的resourceitem放在list中  
        List list  = root.elements("doc");  
        System.out.println(list);
        //创建source存放每一个resourceitem中资源  
        HashMap<String,Integer> source = new HashMap<String,Integer>();  
        System.out.println("OK");
        //将resourceitem中的各项解析出来，通过XmlBean存放到source中  
        for(Iterator i = list.iterator();i.hasNext();) {  
        	System.out.println("OK");
            Element pic = (Element) i.next();  
            String locate = pic.element("locate").getText();  
            String id = pic.element("id").getText();   
            System.out.println(locate+id);
            source.put(locate, new Integer(id));
        }      
    }
    public static void main(String[] args) throws Exception {
    	ParseXml p1 = new ParseXml();
    	p1.read();
    }
  
}  

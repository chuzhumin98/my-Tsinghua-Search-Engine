package pagerank;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;
import java.io.*;
import java.nio.CharBuffer;
public class RegTest {
	File f = new File("output.txt");
	PrintStream out; //Ҳ���ļ���
	XmlParseLucene x1;
	public static String path = "D://workspace/mys/jobs/news.tsinghua.edu.cnV8-20170513001935154/mirror/news.tsinghua.edu.cn/publish/";
	RegTest(XmlParseLucene x) {
		this.x1 = x;
		try {
			out = new PrintStream(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //Ҳ���ļ���
	}
	public void getURLs(String filepath, int n) {
		out.print(n+":");
		
        //����һ�������б��ࡣ�������µ���ַ�����±���
        class ArticleList {
            String URLs;

            public ArticleList(){}
            public ArticleList(String u)
            {
                URLs=u;
                 
            }
                    
            public String toString()
            {
                return (" ��ַ��"+URLs+" ");
                
            }
        }
        //----------start---��ȡHTML�ļ���buff--------------------------------------------------------
        File file = new File(filepath);
        BufferedReader reader = null;
        StringBuffer buff = new StringBuffer(); 
        try {
     //   System.out.println("����Ϊ��λ��ȡ�ļ����ݣ�һ�ζ�һ���У�");
        reader = new BufferedReader(new FileReader(file));
        String tempString = null;
         
        int line = 1;
        //һ�ζ���һ�У�ֱ������nullΪ�ļ�����
        while ((tempString = reader.readLine()) != null){
        //��ʾ�к�
         buff.append(tempString+"/r/n");
        //System.out.println("line " + line + ": " + tempString);
        line++;
        }
     // System.out.println(buff.toString());
        reader.close();
        } catch (IOException e) {
        e.printStackTrace();
        } finally {
        if (reader != null){
        try {
        reader.close();
        } catch (IOException e1) {
        }
        }
        }

        //------------end--��ȡ---------------------------------------------------------------------------        
        ArrayList <ArticleList>  al=new ArrayList<ArticleList>();
        String s=buff.toString();
        String regex="<a.*?/a>";    
        Pattern pt=Pattern.compile(regex);
        //System.out.println(regex);
        Matcher mt=pt.matcher(s);
       // String includeString=".*baidu.com.*";//������� �ַ���"baidu.com" 
        String[] fs = filepath.split("/");
        int start = 0; //��ʼ��λ��
        for (; start < fs.length; start++) {
        	if (fs[start].equals("publish")) {
        		break;
        	}
        }
        start++; //startλ�ÿ�ȡ
        int end = fs.length - 1; //endλ�ò���ȡ
    //    for (int i = start; i < end; i++) {
   //     	System.out.println(fs[i]);
    //    }
        while(mt.find()){ 
            if(true){                  
                 //System.out.println(mt.group());               
                 String s3="href=\".*?\"";                 
                  Pattern pt3=Pattern.compile(s3);
                  Matcher mt3=pt3.matcher(mt.group());
                  while(mt3.find()){                     
                      String u=mt3.group().replaceAll("href=|>","");
                      int curlen = u.length();
                      
                      if (curlen >= 6 && (u.substring(u.length()-5,u.length()-1).equals("html") || u.substring(u.length()-4,u.length()-1).equals("pdf") || u.substring(u.length()-4,u.length()-1).equals("doc"))) {                    	  
                    	  u = u.substring(1, curlen-1); //ȥ��˫����                    	
                    	  String total = new String("");
                    	  String[] us= u.split("/");
                    	  boolean isOK = false; //���
                    	  boolean isusefs = false; //�Ƿ���Ҫ��fs
                    	  int usstart = 0;
                    	  for (int i = 0; i < us.length; i++) {
                    		  if (us[i].equals("publish")) {
                    			  usstart = i+1;
                    			  isOK = true;
                    			  break;
                    		  }
                    	  }
                    	  if (isOK) {
                    		  for (int i = usstart; i < us.length-1; i++) {
                    			  total += us[i];
                    			  total += "/";
                    		  }
                    		  total += us[us.length-1];
                    	  } else { //��Ҫ������Ŀ¼ȷ��
                    		  if (us[0].equals("")) {
                    			  usstart = 1;
                    		  } else {
                    			  usstart = 0;
                    		  }
                    		  for (int i = end - 1; i >= start; i--) {
                    			  
                    			  if (fs[i].equals(us[usstart])) {
                    				  end = i;
                    				  isusefs = true;
                    				  break;
                    			  }
                    		  }
                    		  if (isusefs) {
                    			  for (int i = start; i < end; i++) {
                    				  total += fs[i];
                    				  total += "/";
                    			  }
                    		  }
                    		  for (int i = usstart; i < us.length-1; i++) {
                    			  total += us[i];
                    			  total += "/";
                    		  }
                    		  total += us[us.length - 1];
                    	  }
                    	  if (this.x1.source.get(total)!=null) {
                    		  out.print(this.x1.source.get(total)+" ");
                    	  }                   	  
                    	  //System.out.println(us[1]);
                    	 // out.println(total);
                    	//  System.out.println(total);
                    	  al.add(new ArticleList(u));
                     }                
                    	  
                  }                
            } 
            
        }//end while      
  //      for(int i=0;i<al.size();i++)
  //      System.out.println(al.get(i));    
        out.println("");
      //  System.out.println("����"+al.size()+"�����"); 
    }
}
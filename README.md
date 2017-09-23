# my-Tsinghua-Search-Engine
my tsinghua search engine using Java, coorparate with Huang Huirong

配置方法：

项目原始运行环境：Windows8.1
项目原始运行IDE：除那个python小程序之外，均为Eclipse
项目说明：
mys项目: 运行org.archive.crawler包下的heritrix.java源文件，然后按照报告中所说的方法配置，将抓取的结果所存储的路径自作crawlpath

pagerank项目：更改一些文件路径后，直接运行pagerank包下的XmlParseLucene.java源文件，即可得到邻接表output.txt,然后运行PageRankmain.cpp，可以得到pagerank.xml文件

newtsinghuac项目：首先运行src下的源文件MainIndexer.java，从而在forindex/index下建立索引，然后将该项目配置到Tomcat中，打开Tomcat，在浏览器中输入网址http://localhost:8080/newtsinghuac/schoolSearch.jsp，就可以看到搜索界面了
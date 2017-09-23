<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
request.setCharacterEncoding("utf-8");
response.setCharacterEncoding("utf-8");
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
String htmlPath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/";
System.out.println(path);
System.out.println(basePath);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>校园搜索结果展示</title>
<link rel="stylesheet" type="text/css" href=<%=path+"/bootstrap/css/bootstrap.min.css" %> />
<script type="text/javascript" src=<%=path+"/bootstrap/js/bootstrap.min.js" %>></script>
<script src="http://cdn.bootcss.com/jquery/1.11.1/jquery.min.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<style type="text/css">
<!--
#Layer1 {
	position:absolute;
	left:0px;
	top:31px;
	width:1400px;
	height:100px;
	z-index:1;
}
#Layer2 {
	position:absolute;
	left:29px;
	top:82px;
	width:648px;
	height:602px;
	z-index:2;
}
#Layer3 {
	position:absolute;
	left:28px;
	top:697px;
	width:652px;
	height:67px;
	z-index:3;
}
-->
</style>
</head>

<body>
<%
	String currentQuery=(String) request.getAttribute("currentQuery");
	int currentPage=(Integer) request.getAttribute("currentPage");
%>
<nav class="navbar navbar-default" role="navigation" style="height:55px;border-style:none"></nav>
<div id="Layer1">
<form class="form-inline" id="form1" name="form1" method="get" action="MainServer">
<nav class="navbar navbar-default" role="navigation" style="height:81px;border-style:none">
    <div class="container-fluid"> 
        <form class="navbar-form navbar-left" role="search">
        	<img src=<%=path+"/images/tsinghuas7.png" %>  alt="Tsinghua Search" width="165" height="55" class="img-rounded"/>
            <input style="width:570px;height:30px" type="text" class="form-control" id="searchInput" placeholder="input something..." name="query" value="<%=currentQuery %>">
            <button style="height:30px;width:60px" class="btn btn-success" type="submit">搜索</button>
        </form>
    </div>
</nav>
</form>
</div>

<%  	String[] titles=(String[]) request.getAttribute("title");
		String[] abstracts=(String[]) request.getAttribute("abstract");
		String[] links = (String[]) request.getAttribute("link");
		String[] posttime = (String[]) request.getAttribute("posttime");
		int hitslength = (Integer) request.getAttribute("hitslength");
%>
<div id="Layer2" style="top: 120px; height: 900px; left:185px">
  <div id="imagediv">共搜到<%= hitslength %>条结果：
  <br>
  <Table style="left: 0px; width: 594px;">
  <% 
  	if (links != null) {
  		for (int i = 0; i < links.length; i++) {
  	  		links[i] = "http://news.tsinghua.edu.cn/publish/"+links[i];
  	  	}
  	}
  	if (posttime != null){
  		for (int i = 0;i < posttime.length;i ++){
  			abstracts[i] = "<font color=\"#C2C2C2\">" + posttime[i] + "</font>" + " - " + abstracts[i];
  		}
  	}
  	if(titles!=null && titles.length>0){
  		for(int i=0;i<titles.length;i++){%>
  		<%String sublink = links[i].substring(7); 
		if (sublink.length() >= 50) {
			sublink = sublink.substring(0, 50);
			sublink = sublink+"...";
		}
  		%>
  		<p>
  		<tr><h3><%=(currentPage-1)*10+i+1%>. <a href=<%=links[i]%> target="_blank"><%=titles[i] %></a></h3></tr>
  		<tr><%= abstracts[i] %></tr><br/>
  		<font color="#32CD32"><tr><%=sublink %></tr></font>
  		</p>
  		<br/>
  		<%}; %>
  	<%}else{ %>
  		<p><tr><h3>no such result</h3></tr></p>
  	<%}; %>
  </Table>
  </div>
  <div>
  <br/>
  <div align="center"> <img src=<%=path+"/images/tsiiinghua.png" %> alt="Tsiiiiiiiinghua" width="360" height="60"/></div>
  	<p align="center"><font size="3">
  		<%if(currentPage!=1){ %><a href="MainServer?query=<%=currentQuery%>&page=<%=1%>"> 首页</a>
  		<%}else{ %> 首页<%} %>
		<%if(currentPage>1){ %>
			<a href="MainServer?query=<%=currentQuery%>&page=<%=currentPage-1%>"> 上一页</a>
		<%}else{ %>
		 上一页<%}; %>
		<%if(titles!=null && titles.length!=0){ %>
		
		<%for (int i=Math.max(1,currentPage-5);i<currentPage;i++){%>
			<a href="MainServer?query=<%=currentQuery%>&page=<%=i%>"><%=i%></a>
		<%}; %>
		<strong> <%=currentPage%></strong>
		<%for (int i=currentPage+1;i<=Math.min(currentPage+5,(hitslength+9)/10);i++){ %>
			<a href="MainServer?query=<%=currentQuery%>&page=<%=i%>"><%=i%></a>
		<%}; %>
		<%if(currentPage*10 >= hitslength){%> 下一页<%}else { %><a href="MainServer?query=<%=currentQuery%>&page=<%=currentPage+1%>"> 下一页</a>
		<%}} %>
		</font>
	</p>
  </div>
</div>
<div id="Layer3" style="top: 1000px; left: 27px;">
	
</div>
<div>
</div>
</body>

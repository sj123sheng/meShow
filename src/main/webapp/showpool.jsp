<%@ page language="java" contentType="text/html;charset=UTF-8"%>
<%@page import="org.apache.log4j.Logger" %>
<%@page import="org.apache.struts2.*" %>
<%@page import="com.opensymphony.xwork2.*" %>
<%@page import="org.apache.struts2.components.*" %>
<%@page import="com.opensymphony.xwork2.interceptor.*" %>
<%@ page import="com.opensymphony.xwork2.interceptor.ExceptionHolder" %>

<%@ page import="javax.management.*" %>
<%@ page import="java.lang.management.ManagementFactory" %>
<%@ page import="java.util.Set" %>
<%@ page import="javax.management.openmbean.CompositeDataSupport" %>
<%@ page import="javax.management.openmbean.CompositeData" %>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
<title>Insert title here</title>
</head>
<body>

<%
//import java.util.Set;
	javax.management.MBeanServer server = ManagementFactory.getPlatformMBeanServer();
	Set<ObjectInstance> beans = server.queryMBeans(null, null);
	for(ObjectInstance oi : beans){
		//response.getWriter().write(oi.getClassName()+"<BR>");
		if(oi.getClassName().equals("com.mongodb.MongoConnectionPool")){
			ObjectName objName = oi.getObjectName();
			String poolName = (String)server.getAttribute(objName, "Name");
			int poolMaxSize = (Integer)server.getAttribute(objName, "MaxSize");
			String mongoSvr = (String)server.getAttribute(objName, "Host");
			int mongoPort = (Integer)server.getAttribute(objName, "Port");
			CompositeDataSupport cds = (CompositeDataSupport)server.getAttribute(objName, "Statistics");

	        int stsTotal = (Integer)cds.get("total");
	        int stsInUse = (Integer)cds.get("inUse");
	        Object[] conns = (Object[])cds.get("inUseConnections");//javax.management.openmbean.CompositeData[]
		
			response.getWriter().write("<BR><BR>poolName: "+poolName+"<BR>"+
				"poolMaxSize: "+poolMaxSize + "<BR>" +
				"mongoSvr: "+mongoSvr + "<BR>" +	
				"mongoPort: "+mongoPort + "<BR>" + 
				"statusTotal: " + stsTotal + "<BR>" +
				"statusInUse: " + stsInUse + "<BR>" +
				"statusUsingConnections: " + conns.length + "<BR>");
	        if(conns.length > 0){
		        response.getWriter().write("==============================================================<BR>");
		        for(int i=0; i<conns.length; i++){
		        	CompositeData cd = (CompositeData)conns[i];
		        	long durationMs = (Long)cd.get("DurationMS");
		        	String threadName = (String)cd.get("ThreadName");
		        	int localPort = (Integer)cd.get("LocalPort");
		        	String query = (String)cd.get("Query");
		        	int newDocs = (Integer)cd.get("NumDocuments");
		        	response.getWriter().write("--" + i + " ThreadName: "+threadName+"<BR>"+
		    				"LocalPort: "+localPort + "<BR>" +
		    				"Query: "+query + "<BR>" +
		    				"NumDocumentsCount: "+newDocs + "<BR>");
		        }
		        response.getWriter().write("==============================================================<BR>");
	        }
		}
	}
	
	
%>
	
</body>
</html>

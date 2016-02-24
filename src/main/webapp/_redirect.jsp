<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" session="false"
%><%
    String id = request.getPathInfo();
    if (id != null) {
        id = id.substring(1);
    }
    if ((id == null) || id.isEmpty()) {
        id = "[N/A]";
    }
    final String korapURL = config.getInitParameter("korapWebUrl");
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
                      "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <title>KorAP SRU to KorAP bridge</title>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <meta http-equiv="refresh" content="30; URL=<%= korapURL %>" />
</head>
<body>
<h1>This resource is only available within KorAP</h1>
<p>
  Sorry, but the resource <em><%= id %></em> is not directly accessible for
  browsing or download. <br />
  Please use
  <a href="<%= korapURL %>">KorAP</a> for
  further exploration or more advanced research in the IDS corpora.
</p>
</body>
</html>

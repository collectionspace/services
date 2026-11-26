<%@ page language="java" session="false" %>
<%! String stableUrl = "@PUBLIC_BROWSER_STABLE_URL@"; %>
<%
	// Redirects to the public browser script that is compatible with this backend. The redirect
	// must not be cached, since its target changes when the backend is upgraded.
	response.setHeader("Cache-Control", "no-cache, max-age=0");
	response.sendRedirect(stableUrl);
%>

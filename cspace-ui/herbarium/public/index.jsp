<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%! String configuredGatewayUrl = "@GATEWAY_URL@"; %>
<html>
	<head>
		<meta charset="UTF-8" />
	</head>
	<body>
		<div id="cspace-browser"></div>
    <script src="/cspace-ui/@PUBLIC_BROWSER_FILENAME@"></script>
    <script>
      cspacePublicBrowser({
        basename: '@BASENAME@/public',
        baseConfig: '@TENANT_SHORTNAME@',
        gatewayUrl: '<%=
          configuredGatewayUrl.length() > 0
            ? configuredGatewayUrl
            : request.getRequestURL().substring(0, request.getRequestURL().length() - request.getRequestURI().length()) + "/gateway/@TENANT_SHORTNAME@"
        %>',
      });
    </script>
	</body>
</html>

<#--
	This FreeMarker template is used to generate response bodies of service layer endpoints that
	return HTML, e.g. login, logout, requestpasswordreset, processpasswordreset.
-->
<html>
	<head>
		<meta charset="UTF-8" />
	</head>
	<body>
		<div id="cspace"></div>
		<script src="/cspace-ui/@SERVICE_UI_FILENAME@"></script>
		<script>
			cspaceUI(
<#outputformat "JavaScript">
				${uiConfig}
</#outputformat>
			);
		</script>
	</body>
</html>

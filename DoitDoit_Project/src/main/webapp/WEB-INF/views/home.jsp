<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>홈 화면</title>
</head>
<%@include file="./comm/setting.jsp" %>
<script type="text/javascript" src="./js/cald/calendarList.js"></script>
<body>
	<div id="container">
        <%@include file="./comm/nav.jsp" %>
        <main>
            <%@include file="./comm/header.jsp" %>
            <div id="content">
            <sec:authorize access="hasRole('ROLE_USER')">
                <div id="rContent">
                    <div id="calendar" class="rContent-half"></div>
                    <div id="resent" class="rContent-half">
                   	 <div id="resentBoard">
                    <!-- home.js에서 $(document).ready(function(){ eboardResent() }); -->
                    	</div>
<!-- 					<div class="rContent-full"></div> -->
<!-- 					<div class="rContent-normal-top"></div> -->
<!-- 					<div class="rContent-normal-bottom"></div> -->
					</div>
                </div>
            <%@include file="./comm/aside.jsp" %>    
            </sec:authorize>
            </div>
        </main>
    </div>
</body>
</html>
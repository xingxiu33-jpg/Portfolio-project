<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
<%
    // 检查 Session 是否存在
    HttpSession sess = request.getSession(false); 
    String userType = null;
    
    if (sess != null) {
        userType = (String) sess.getAttribute("user_type");
    }

    // 权限检查：如果 Session 不存在，或者 user_type 不是 'user'
    if (sess == null || !"user".equals(userType)) {
        // 重定向到登录页
        response.sendRedirect("login.jsp?error=AccessDenied");
        return; // 必须 return，停止执行此页面
    }
%>

<h1>欢迎, 普通用户!</h1>
<p>这是您的个人中心。</p>
<a href="login.jsp">退出登录</a>
</body>
</html>
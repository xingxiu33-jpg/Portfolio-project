<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
   
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>商家页面</title>

</head>
<body>
<%
    HttpSession sess = request.getSession(false);
    String userType = null;
    
    if (sess != null) {
        userType = (String) sess.getAttribute("user_type");
    }

    // 权限检查：如果 Session 不存在，或者 user_type 不是 'merchant'
    if (sess == null || !"merchant".equals(userType)) {
        response.sendRedirect("login.jsp?error=AccessDenied");
        return; 
    }
%>
<a href="MerchantProfileServlet">编辑我的资料</a>
<a href="LogoutServlet">退出登录</a>

</body>
</html>
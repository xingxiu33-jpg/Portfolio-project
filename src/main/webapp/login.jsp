<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%-- 引入国际化配置文件 --%>
<%@ include file="/WEB-INF/common/i18n_setup.jspf"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title><fmt:message key="page.title.login" /></title>
<style>
/* (可选) 一些简单的样式 */
body {
	font-family: sans-serif;
	display: grid;
	place-items: center;
	min-height: 80vh;
}

.container {
	border: 1px solid #ccc;
	padding: 20px;
	border-radius: 8px;
}

.form-group {
	margin-bottom: 10px;
}

.form-group label {
	display: inline-block;
	width: 80px;
}

.message-error {
	color: red;
}

.message-success {
	color: green;
}

.register-link {
	margin-top: 20px;
	text-align: center;
}

.show-password-group {
	font-size: 0.9em;
	text-align: right;
	margin-top: -5px;
	margin-bottom: 15px;
}
</style>
</head>
<body>
	<div style="text-align: right; width: 100%; ">
		<a href="login.jsp?lang=zh">中文</a> | <a href="login.jsp?lang=en">English</a>
		| <a href="login.jsp?lang=ja">日本語</a>
	</div>
	<div class="container">
		<h2>
			<fmt:message key="page.title.login" />
		</h2>

		<%-- 处理 URL 中的提示信息 --%>
		<%
		String error = request.getParameter("error");
		if (error != null) {
			if (error.equals("InvalidCredentials")) {
				out.println("<p class='message-error'>用户名或密码错误。</p>");
			} else if (error.equals("AccessDenied")) {
				out.println("<p class='message-error'>您没有权限访问，请先登录。</p>");
			}
		}

		String message = request.getParameter("message");
		if (message != null && message.equals("RegistrationSuccessful")) {
			out.println("<p class='message-success'>注册成功！请登录。</p>");
		}
		%>

		<form action="./LoginServlet" method="post">
			<div class="form-group">
				<label for="username">用户名:</label> <input type="text" id="username"
					name="username" required>
			</div>
			<div class="form-group">
				<label for="password">密码:</label> <input type="password"
					id="password" name="password" required>
			</div>
			<div class="show-password-group">
				<input type="checkbox" id="showPassword"> <label
					for="showPassword">显示密码</label>
			</div>
			<div class="form-group">
				<input type="submit" value="登录">
			</div>
		</form>

		<hr>

		<div class="register-link">
			<p>
				还没有账户？ <a href="register.jsp">点击这里注册</a>
			</p>
		</div>

	</div>
	<script>
		// 等待整个 HTML 页面加载完毕后
		document.addEventListener("DOMContentLoaded", function() {

			// 1. 通过 ID 获取密码输入框
			const passwordInput = document.getElementById("password");

			// 2. 通过 ID 获取“显示密码”复选框
			const showPasswordCheckbox = document
					.getElementById("showPassword");

			// 3. 为复选框添加一个 "click" 事件监听器
			showPasswordCheckbox.addEventListener("click", function() {
				// 检查复选框当前是否被选中
				if (this.checked) {
					// 如果选中了，就把密码框的 type 变成 "text"
					passwordInput.type = "text";
				} else {
					// 如果没选中，就变回 "password"
					passwordInput.type = "password";
				}
			});
		});
	</script>
</body>
</html>
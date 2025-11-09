<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%-- 引入国际化配置文件 --%>
<%@ include file="/WEB-INF/common/i18n_setup.jspf" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<style>
    /* (可选) 样式 */
    body { font-family: sans-serif; display: grid; place-items: center; min-height: 90vh; }
    .container { border: 1px solid #ccc; padding: 20px; border-radius: 8px; width: 350px; }
    .form-group { margin-bottom: 12px; }
    .form-group label { display: inline-block; width: 100px; }
    .message-error { color: red; }
    .register-link { margin-top: 20px; text-align: center; }
    
    /* (新样式) 和 login.jsp 一样 */
    .show-password-group {
        font-size: 0.9em;
        text-align: right;
        margin-top: 5px;
        margin-bottom: 15px;
    }
</style>
</head>
<body>

<h2>注册</h2>
<form id="registerForm" action="./Register" method="post" >
		<p><fmt:message key="label.username" /><input type="text" name="username"></p>
		<div class="form-group">
                <label for="reg_password">密码:</label>
                <input type="password" id="reg_password" name="password" required>
            </div>

            <div class="form-group">
                <label for="reg_confirm_password">确认密码:</label>
                <input type="password" id="reg_confirm_password" name="confirm_password" required>
            </div>
            <div class="show-password-group">
                <input type="checkbox" id="reg_showPassword">
                <label for="reg_showPassword">显示密码</label>
            </div>
		<p>email:<input type="text" name="email"></p>	
		<p>姓名 ： <input type="text" id="name" name="name" maxlength="50">	</p>
		<p>phonenumber<input type="tel" id="phone_number" name="phone_number" maxlength="20"></p>
		<p>性別<input type="radio" name="gender" value="Male" checked>男性
        <input type="radio" name="gender" value="Female">女性</p>
         <p>Birthday: <input type="date" name="bday"></p>
         <label>账户类型:</label>
    <input type="radio" name="user_type" value="user" checked> 普通用户
    <input type="radio" name="user_type" value="merchant"> 商家
		<input type="submit" value="送信">
		
    <br>
	</form>
	//检查是否有错误
	<%
        String error = (String) request.getAttribute("error");
        if (error != null) {
            out.println("<p style='color:red;'>" + error + "</p>");
        }
    %>
    <script>
        // 等待整个 HTML 页面加载完毕后
        document.addEventListener("DOMContentLoaded", function() {
            
            // --- 1. 实现“显示密码”功能 ---
            
            // 通过 ID 获取三个元素
            const passwordInput = document.getElementById("reg_password");
            const confirmPasswordInput = document.getElementById("reg_confirm_password");
            const showPasswordCheckbox = document.getElementById("reg_showPassword");

            // 为复选框添加 "click" 事件监听器
            if (showPasswordCheckbox) {
                showPasswordCheckbox.addEventListener("click", function() {
                    // 根据复选框状态，决定 input 的 type
                    const newType = this.checked ? "text" : "password";
                    
                    // 将新 type 应用到 *两个* 密码框
                    if (passwordInput) {
                        passwordInput.type = newType;
                    }
                    if (confirmPasswordInput) {
                        confirmPasswordInput.type = newType;
                    }
                });
            }

            
            // --- 2. (额外功能) 验证两次密码是否一致 ---
            
            const form = document.getElementById("registerForm");
            
            if (form) {
                // 监听表单的 "submit" (提交) 事件
                form.addEventListener("submit", function(event) {
                    
                    // 检查两个密码框的值
                    if (passwordInput.value !== confirmPasswordInput.value) {
                        
                        // 如果不一致
                        alert("错误：密码和确认密码不一致！");
                        
                        // 阻止表单提交 (非常重要)
                        event.preventDefault(); 
                    }
                });
            }
        });
    </script>
</body>
</html>
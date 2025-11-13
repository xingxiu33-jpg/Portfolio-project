<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.util.Set"%>
<%@ page import="java.util.Map" %>
<%
        // --- 准备工作 (修正版): 从 Servlet 获取所有数据 ---

        // 1. 获取基本资料 (从 Map 中读取)
        Map<String, Object> profileData = (Map<String, Object>) request.getAttribute("profileData");
        
        String avatarPath = "/uploads/avatars/default.png"; // 默认头像
        String specialties = "";
        String bio = "";

        if (profileData != null && !profileData.isEmpty()) {
            
            // (关键) 从 Map 中获取数据
            String dbAvatar = (String) profileData.get("profile_avatar_path");
            
            if (dbAvatar != null && !dbAvatar.isEmpty()) {
                avatarPath = "/uploads/avatars/" + dbAvatar;
            }
            
            specialties = (String) profileData.get("specialties");
            bio = (String) profileData.get("bio");

            // (防止 null 导致 value="null")
            if (specialties == null) specialties = "";
            if (bio == null) bio = "";
        }
    %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>编辑资料</title>
<style>
body {
	font-family: sans-serif;
	margin: 20px;
}

.form-section {
	border: 1px solid #ccc;
	padding: 15px;
	margin-bottom: 20px;
	border-radius: 8px;
}

.form-group {
	margin-bottom: 10px;
}

label {
	display: block;
	font-weight: bold;
}

input[type="text"], textarea {
	width: 90%;
	max-width: 500px;
}

textarea {
	height: 100px;
}

.checkbox-grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
	gap: 5px;
}

.portfolio-gallery {
	display: flex;
	flex-wrap: wrap;
	gap: 10px;
}

.portfolio-item {
	border: 1px solid #ddd;
	padding: 5px;
	text-align: center;
}

.portfolio-item img {
	max-width: 150px;
	max-height: 150px;
	object-fit: cover;
}

.avatar-preview {
	max-width: 100px;
	max-height: 100px;
	border-radius: 50%;
	border: 2px solid #eee;
}
</style>
</head>
<body>
	<h1>编辑您的商家资料</h1>

	<p>
		欢迎,
		<%=session.getAttribute("username")%>!
	</p>



	<div class="form-section">
		<h3>基本资料</h3>

		<form action="MerchantProfileServlet" method="post"
			enctype="multipart/form-data">
			<input type="hidden" name="action" value="update_profile">

			<div class="form-group">
			<h3 style="color:red;">DEBUG: <%= avatarPath %></h3>
				<label>当前头像:</label> <img src="<%=avatarPath%>" alt="Avatar"
					class="avatar-preview">
			</div>
			<div class="form-group">
				<label for="avatar">更换头像:</label> <input type="file" id="avatar"
					name="avatar_file">
			</div>
			<div class="form-group">
				<label for="specialties">您的“专长” (展示给用户):</label> <input type="text"
					id="specialties" name="specialties" value="<%=specialties%>">
			</div>
			<div class="form-group">
				<label for="bio">个人简介:</label>
				<textarea id="bio" name="bio"><%=bio%></textarea>
			</div>
			<input type="submit" value="保存基本资料">
		</form>
	</div>

	<div class="form-section">
		<h3>筛选标签 (用户将通过这些找到您)</h3>

		<form action="./MerchantProfileServlet" method="post"enctype="multipart/form-data">
			<input type="hidden" name="action" value="update_tags">

			<%
			// --- 准备工作 2: 获取所有筛选列表和已选集合 ---
			ResultSet allServices = (ResultSet) request.getAttribute("allServices");
			Set<Integer> myServiceIds = (Set<Integer>) request.getAttribute("myServiceIds");

			ResultSet allRegions = (ResultSet) request.getAttribute("allRegions");
			Set<Integer> myRegionIds = (Set<Integer>) request.getAttribute("myRegionIds");

			ResultSet allLanguages = (ResultSet) request.getAttribute("allLanguages");
			Set<Integer> myLangIds = (Set<Integer>) request.getAttribute("myLangIds");
			%>

			<h4>可提供的服务 (勾选)</h4>
			<div class="checkbox-grid">
				<%
				// --- 循环 1: 服务 ---
				if (allServices != null) {
					while (allServices.next()) {
						int id = allServices.getInt("service_id");
						String name = allServices.getString("service_name");
						String checked = (myServiceIds != null && myServiceIds.contains(id)) ? "checked" : "";
				%>
				<label> <input type="checkbox" name="service_ids"
					value="<%=id%>" <%=checked%>> <%=name%>
				</label>
				<%
				} // 结束 while
				} // 结束 if
				%>
			</div>

			<h4>服务地区 (勾选)</h4>
			<div class="checkbox-grid">
				<%
				// --- 循环 2: 地区 ---
				if (allRegions != null) {
					while (allRegions.next()) {
						int id = allRegions.getInt("region_id");
						String name = allRegions.getString("region_name");
						String checked = (myRegionIds != null && myRegionIds.contains(id)) ? "checked" : "";
				%>
				<label> <input type="checkbox" name="region_ids"
					value="<%=id%>" <%=checked%>> <%=name%>
				</label>
				<%
				} // 结束 while
				} // 结束 if
				%>
			</div>

			<h4>可用语言 (勾选)</h4>
			<div class="checkbox-grid">
				<%
				// --- 循环 3: 语言 ---
				if (allLanguages != null) {
					while (allLanguages.next()) {
						int id = allLanguages.getInt("lang_id");
						String name = allLanguages.getString("lang_name");
						String checked = (myLangIds != null && myLangIds.contains(id)) ? "checked" : "";
				%>
				<label> <input type="checkbox" name="lang_ids"
					value="<%=id%>" <%=checked%>> <%=name%>
				</label>
				<%
				} // 结束 while
				} // 结束 if
				%>
			</div>
			<br> <input type="submit" value="保存筛选标签">
		</form>
	</div>

	<div class="form-section">
		<h3>我的作品集</h3>
		<h4>上传新作品</h4>

		<form action="portfolioUpload" method="post" enctype="multipart/form-data">
            <div class="form-group">
                <label for="port_image">作品图片 (可多选):</label>
                <input type="file" id="port_image" name="portfolio_images" required multiple>
            </div>
            <div class="form-group">
                <label for="port_title">作品标题 (将应用于本次上传的所有图片):</label>
                <input type="text" id="port_title" name="title">
            </div>
            <div class="form-group">
                <label for="port_desc">作品描述 (同上):</label>
                <textarea id="port_desc" name="description"></textarea>
            </div>
            <input type="submit" value="上传作品">
        </form>

		<h4>已上传的作品</h4>
		<div class="portfolio-gallery">
			<%
			// --- 循环 4: 作品集 ---
			ResultSet portfolioItems = (ResultSet) request.getAttribute("portfolioItems");

			// 检查 ResultSet 是否为空
			if (portfolioItems != null && portfolioItems.isBeforeFirst()) {

				while (portfolioItems.next()) {
					int portfolioId = portfolioItems.getInt("portfolio_id");
					String imagePath = portfolioItems.getString("image_path");
					String title = portfolioItems.getString("title");
			%>
			<div class="portfolio-item">
				<img src="/uploads/portfolio/<%=imagePath%>" alt="<%=title%>">
				<p>
					<b><%=title%></b>
				</p>

				<form action="portfolioDelete" method="post"
					onsubmit="return confirm('确定删除吗?');">
					<input type="hidden" name="portfolio_id" value="<%=portfolioId%>">
					<input type="submit" value="删除">
				</form>
			</div>
			<%
			} // 结束 while
			} else {
			%>
			<p>您还没有上传任何作品。</p>
			<%
			} // 结束 if-else
			%>
		</div>
	</div>
	<script>
        document.addEventListener("DOMContentLoaded", function() {
            
            // 1. 找到“更换头像”的 <input type="file"> 元素
            const avatarInput = document.getElementById("avatar"); // 对应 <input id="avatar" ...>
            
            // 2. 找到用于显示“当前头像”的 <img> 元素
            // (假设您给它一个 id="avatarPreview")
            // <img src="<%= avatarPath %>" class="avatar-preview" id="avatarPreview">
            const avatarPreview = document.querySelector(".avatar-preview"); 

            if (avatarInput && avatarPreview) {
                // 3. 当用户选择了新文件时
                avatarInput.addEventListener("change", function(event) {
                    
                    const file = event.target.files[0];
                    
                    if (file) {
                        // 4. 使用 FileReader 在浏览器中读取该文件
                        const reader = new FileReader();
                        
                        reader.onload = function(e) {
                            // 5. 将 <img> 标签的 src 替换为
                            // 刚刚读取到的文件的 Base64 数据
                            avatarPreview.src = e.target.result;
                        }
                        
                        // 6. 开始读取
                        reader.readAsDataURL(file);
                    }
                });
            }
        });
    </script>

</body>
</html>
</body>
</html>
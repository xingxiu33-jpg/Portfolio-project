<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
     <%@ page import="java.sql.ResultSet" %>
<%@ page import="java.util.Set" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>编辑资料</title>
<style>
    body { font-family: sans-serif; margin: 20px; }
    .form-section { border: 1px solid #ccc; padding: 15px; margin-bottom: 20px; border-radius: 8px; }
    .form-group { margin-bottom: 10px; }
    label { display: block; font-weight: bold; }
    input[type="text"], textarea { width: 90%; max-width: 500px; }
    textarea { height: 100px; }
    .checkbox-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 5px; }
    .portfolio-gallery { display: flex; flex-wrap: wrap; gap: 10px; }
    .portfolio-item { border: 1px solid #ddd; padding: 5px; text-align: center; }
    .portfolio-item img { max-width: 150px; max-height: 150px; object-fit: cover; }
    .avatar-preview { max-width: 100px; max-height: 100px; border-radius: 50%; border: 2px solid #eee; }
</style>
</head>
<body>
<h1>编辑您的商家资料</h1>
    
    <p>欢迎, <%= session.getAttribute("username") %>!</p>

    <%
        // --- 准备工作: 从 Servlet 获取所有数据 ---

        // 1. 获取基本资料 (ResultSet)
        ResultSet profileRS = (ResultSet) request.getAttribute("profile");
        
        // 2. 初始化资料变量 (防止 profileRS 为 null 或为空)
        String avatarPath = "uploads/avatars/default.png"; // 默认头像
        String specialties = "";
        String bio = "";

        if (profileRS != null && profileRS.next()) {
            String dbAvatar = profileRS.getString("profile_avatar_path");
            if (dbAvatar != null && !dbAvatar.isEmpty()) {
                avatarPath = "uploads/avatars/" + dbAvatar;
            }
            specialties = profileRS.getString("specialties") != null ? profileRS.getString("specialties") : "";
            bio = profileRS.getString("bio") != null ? profileRS.getString("bio") : "";
        }
    %>

    <div class="form-section">
        <h3>基本资料</h3>
        
        <form action="merchantProfile" method="post" enctype="multipart/form-data">
            <input type="hidden" name="action" value="update_profile">
            
            <div class="form-group">
                <label>当前头像:</label>
                <img src="<%= avatarPath %>" alt="Avatar" class="avatar-preview">
            </div>
            <div class="form-group">
                <label for="avatar">更换头像:</label>
                <input type="file" id="avatar" name="avatar_file">
            </div>
            <div class="form-group">
                <label for="specialties">您的“专长” (展示给用户):</label>
                <input type="text" id="specialties" name="specialties" value="<%= specialties %>">
            </div>
            <div class="form-group">
                <label for="bio">个人简介:</label>
                <textarea id="bio" name="bio"><%= bio %></textarea>
            </div>
            <input type="submit" value="保存基本资料">
        </form>
    </div>

    <div class="form-section">
        <h3>筛选标签 (用户将通过这些找到您)</h3>
        
        <form action="./merchantProfile" method="post">
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
                        <label>
                            <input type="checkbox" name="service_ids" value="<%= id %>" <%= checked %> > 
                            <%= name %>
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
                        <label>
                            <input type="checkbox" name="region_ids" value="<%= id %>" <%= checked %> > 
                            <%= name %>
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
                        <label>
                            <input type="checkbox" name="lang_ids" value="<%= id %>" <%= checked %> > 
                            <%= name %>
                        </label>
                <%
                        } // 结束 while
                    } // 结束 if
                %>
            </div>
            <br>
            <input type="submit" value="保存筛选标签">
        </form>
    </div>

    <div class="form-section">
        <h3>我的作品集</h3>
        <h4>上传新作品</h4>
        
        <form action="portfolioUpload" method="post" enctype="multipart/form-data">
            <div class="form-group">
                <label for="port_image">作品图片:</label>
                <input type="file" id="port_image" name="portfolio_image" required>
            </div>
            <div class="form-group">
                <label for="port_title">作品标题:</label>
                <input type="text" id="port_title" name="title">
            </div>
            <div class="form-group">
                <label for="port_desc">作品描述:</label>
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
                            <img src="uploads/portfolio/<%= imagePath %>" alt="<%= title %>">
                            <p><b><%= title %></b></p>
                            
                            <form action="portfolioDelete" method="post" onsubmit="return confirm('确定删除吗?');">
                                <input type="hidden" name="portfolio_id" value="<%= portfolioId %>">
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
</body>
</html>
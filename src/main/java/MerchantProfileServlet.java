import java.io.IOException;
import java.io.InputStream; // 新增
import java.nio.file.Files; // 新增
import java.nio.file.Path;   // 新增
import java.nio.file.Paths; // 新增
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID; // 新增

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig; // 新增
import javax.servlet.annotation.WebServlet;
// ... (导入其他必要的 servlet 和 sql 包)
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part; // 新增
@WebServlet("/MerchantProfileServlet")
@MultipartConfig(
	    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
	    maxFileSize = 1024 * 1024 * 10, // 10MB
	    maxRequestSize = 1024 * 1024 * 50 // 50MB
	)
public class MerchantProfileServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
                
                HttpSession session = request.getSession(false);
                
                if (session == null || !"merchant".equals(session.getAttribute("user_type"))) {
                    response.sendRedirect("login.jsp?error=AccessDenied");
                    return;
                }

                Integer accountId = (Integer) session.getAttribute("account_id");
                Connection conn = null;

                try {
                    try {
						conn = DBUtil.getConnection();
					} catch (ClassNotFoundException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					} 
                    
                    // --- 任务 1: 获取商家基本资料 ---
                    // (我们不再传递 ResultSet, 而是传递一个 Map)
                    Map<String, Object> profileData = new HashMap<>();
                    
                    String sqlProfile = "SELECT * FROM merchant_profiles WHERE account_id = ?";
                    try (PreparedStatement psProfile = conn.prepareStatement(sqlProfile)) {
                        psProfile.setInt(1, accountId);
                        try (ResultSet rsProfile = psProfile.executeQuery()) {
                            if (rsProfile.next()) {
                                // *** (关键) 在 Servlet 中读取数据 ***
                                profileData.put("profile_avatar_path", rsProfile.getString("profile_avatar_path"));
                                profileData.put("specialties", rsProfile.getString("specialties"));
                                profileData.put("bio", rsProfile.getString("bio"));
                            }
                        }
                    }
                    // *** (关键) 将 Map 传递给 JSP ***
                    request.setAttribute("profileData", profileData); 

                    // --- 任务 2 & 3: (保持不变, 但最好也改成 List) ---
                    // (为了简单, 我们暂时保留这些查询)
                    request.setAttribute("allRegions", conn.prepareStatement("SELECT * FROM regions").executeQuery());
                    request.setAttribute("myRegionIds", getSelectedIds(conn, "SELECT region_id FROM merchant_service_areas WHERE account_id = ?", accountId));
                    
                    request.setAttribute("allLanguages", conn.prepareStatement("SELECT * FROM languages").executeQuery());
                    request.setAttribute("myLangIds", getSelectedIds(conn, "SELECT lang_id FROM merchant_languages WHERE account_id = ?", accountId));
                    
                    request.setAttribute("allServices", conn.prepareStatement("SELECT * FROM services").executeQuery());
                    request.setAttribute("myServiceIds", getSelectedIds(conn, "SELECT service_id FROM merchant_services WHERE account_id = ?", accountId));

                    // --- 任务 4: (保持不变) ---
                    String sqlPortfolio = "SELECT * FROM merchant_portfolio WHERE account_id = ? ORDER BY uploaded_at DESC";
                    PreparedStatement psPortfolio = conn.prepareStatement(sqlPortfolio);
                    psPortfolio.setInt(1, accountId);
                    request.setAttribute("portfolioItems", psPortfolio.executeQuery());

                    // 5. 转发
                    request.getRequestDispatcher("/merchant_profile.jsp").forward(request, response);

                } catch (SQLException e) {
                    throw new ServletException("数据库查询出错", e);
                } finally {
                    // (现在, conn 在这里关闭是 100% 安全的)
                    if (conn != null) try { conn.close(); } catch (SQLException e) {}
                }
            }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8"); // 处理中文乱码
        
        HttpSession session = request.getSession(false);
        
        // 权限检查
        if (session == null || !"merchant".equals(session.getAttribute("user_type"))) {
            response.sendRedirect("login.jsp?error=AccessDenied");
            return;
        }

        Integer accountId = (Integer) session.getAttribute("account_id");
        String action = partToString(request.getPart("action")); // (使用辅助方法)

        // 我们使用 "action" 参数来区分是哪个表单提交的
        if ("update_profile".equals(action)) {
            // 这是表单 A (基本资料)
            handleUpdateProfile(request, response, accountId);
        } else if ("update_tags".equals(action)) {
            // 这是表单 B (筛选标签)
            handleUpdateTags(request, response, accountId);
        } else {
            // 未知操作
            response.sendRedirect("MerchantProfileServlet?error=UnknownAction");
        }
    }

    /**
     * 处理表单 A: 保存基本资料 (头像, 专长, 简介)
     */
    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response, int accountId) 
            throws ServletException, IOException {
        
        Connection conn = null;
        PreparedStatement ps = null;
        String newAvatarFilename = null;

        try {
            // --- 1. 处理文件上传 (头像) ---
            Part filePart = request.getPart("avatar_file"); // 对应 <input name="avatar_file">
            String originalFileName = filePart.getSubmittedFileName();

            if (originalFileName != null && !originalFileName.isEmpty()) {
                // 用户确实上传了新文件
                String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                newAvatarFilename = UUID.randomUUID().toString() + fileExtension; // 创建唯一文件名
                
                // *** 定义您的图片保存路径 ***
                // (重要!) 这个路径必须是服务器上的一个 *绝对* 路径
                // (示例: "C:/tomcat/webapps/uploads/avatars")
                // (示例: "/var/www/uploads/avatars")
                String savePath = "D:/my_project_uploads/avatars"; // <-- *** (请根据您的服务器修改此路径!) ***
                
                Path savePathDir = Paths.get(savePath);
                if (!Files.exists(savePathDir)) {
                    Files.createDirectories(savePathDir); // 如果目录不存在，则创建它
                }

                filePart.write(savePath + "/" + newAvatarFilename); // 保存文件
            }

            // --- 2. 获取文本字段 ---
            // (因为是 multipart, 必须用 partToString 辅助方法)
            String specialties = partToString(request.getPart("specialties"));
            String bio = partToString(request.getPart("bio"));

            // --- 3. 更新数据库 (使用 UPSERT 逻辑) ---
            try {
				conn = DBUtil.getConnection();
			} catch (ClassNotFoundException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
            String sql;

            if (newAvatarFilename != null) {
                // 如果上传了新头像，则更新所有字段
                sql = "INSERT INTO merchant_profiles (account_id, specialties, bio, profile_avatar_path) " +
                      "VALUES (?, ?, ?, ?) " +
                      "ON DUPLICATE KEY UPDATE " +
                      "specialties = ?, bio = ?, profile_avatar_path = ?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, accountId);
                ps.setString(2, specialties);
                ps.setString(3, bio);
                ps.setString(4, newAvatarFilename);
                // (用于 ON DUPLICATE UPDATE 部分)
                ps.setString(5, specialties);
                ps.setString(6, bio);
                ps.setString(7, newAvatarFilename);
            } else {
                // 如果没有上传新头像，则 *不* 更新头像字段
                sql = "INSERT INTO merchant_profiles (account_id, specialties, bio) " +
                      "VALUES (?, ?, ?) " +
                      "ON DUPLICATE KEY UPDATE " +
                      "specialties = ?, bio = ?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, accountId);
                ps.setString(2, specialties);
                ps.setString(3, bio);
                // (用于 ON DUPLICATE UPDATE 部分)
                ps.setString(4, specialties);
                ps.setString(5, bio);
            }

            ps.executeUpdate();

            // --- 4. 重定向 ---
            response.sendRedirect("MerchantProfileServlet?success=profileUpdated");

        } catch (SQLException e) {
            throw new ServletException("数据库错误", e);
        } catch (IOException e) {
            throw new ServletException("文件上传失败", e);
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }
    
    /**
     * 处理表单 B: 保存筛选标签 (我们下一步再实现这个)
     */
    private void handleUpdateTags(HttpServletRequest request, HttpServletResponse response, int accountId) 
            throws ServletException, IOException {
       
    	Connection conn = null;
        try {
            try {
				conn = DBUtil.getConnection();
			} catch (ClassNotFoundException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
            conn.setAutoCommit(false); // *** 开启事务！***

            // --- 1. 更新服务地区 (Regions) ---
            String[] regionIds = request.getParameterValues("region_ids");
            updateJunctionTable(conn, 
                                "DELETE FROM merchant_service_areas WHERE account_id = ?", 
                                "INSERT INTO merchant_service_areas (account_id, region_id) VALUES (?, ?)", 
                                accountId, 
                                regionIds);

            // --- 2. 更新可用语言 (Languages) ---
            String[] langIds = request.getParameterValues("lang_ids");
            updateJunctionTable(conn, 
                                "DELETE FROM merchant_languages WHERE account_id = ?", 
                                "INSERT INTO merchant_languages (account_id, lang_id) VALUES (?, ?)", 
                                accountId, 
                                langIds);
            
            // --- 3. 更新可提供服务 (Services) ---
            String[] serviceIds = request.getParameterValues("service_ids");
            updateJunctionTable(conn, 
                                "DELETE FROM merchant_services WHERE account_id = ?", 
                                "INSERT INTO merchant_services (account_id, service_id) VALUES (?, ?)", 
                                accountId, 
                                serviceIds);

            // --- 4. 提交事务 ---
            conn.commit(); // 只有当所有更新都成功时，才保存到数据库
        // 暂时先重定向
        response.sendRedirect("MerchantProfileServlet?success=tagsUpdated");} catch (SQLException e) {
            // 如果出错，回滚所有更改
            if (conn != null) try { conn.rollback(); } catch (SQLException se) {}
            throw new ServletException("数据库事务错误", e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) {} // 恢复默认
                try { conn.close(); } catch (SQLException e) {}
            }
        }
    
    }

    /**
     * [新增辅助方法]
     * 一个通用的方法，用于处理“先删后插”的连接表 (Junction Table) 逻辑
     * * @param conn 数据库连接
     * @param deleteSql "DELETE FROM ... WHERE account_id = ?"
     * @param insertSql "INSERT INTO ... (account_id, item_id) VALUES (?, ?)"
     * @param accountId 商家的 ID
     * @param itemIds   从复选框中获取的 ID 数组
     * @throws SQLException
     */
    private void updateJunctionTable(Connection conn, String deleteSql, String insertSql, int accountId, String[] itemIds) throws SQLException {
        
        // 1. 先删除该商家的所有旧记录
        try (PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
            psDelete.setInt(1, accountId);
            psDelete.executeUpdate();
        }

        // 2. 如果用户没有勾选任何项 (itemIds 为 null)，则到此为止
        if (itemIds == null || itemIds.length == 0) {
            return;
        }

        // 3. 插入所有新勾选的记录
        try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
            // (使用批量处理提高效率)
            for (String idStr : itemIds) {
                psInsert.setInt(1, accountId);
                psInsert.setInt(2, Integer.parseInt(idStr)); // (假设 ID 都是数字)
                psInsert.addBatch(); // 添加到批处理
            }
            psInsert.executeBatch(); // 一次性执行所有 INSERT
        }
    }
    /*
     * ==========================================================
     * (3) 这是 multipart 表单必需的辅助方法
     * (用于从 Part 中读取 String)
     * ==========================================================
     */
    private String partToString(Part part) throws IOException {
        if (part == null) {
            return null;
        }
        try (InputStream inputStream = part.getInputStream()) {
            // (简单的 byte[] -> String 转换)
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            return new String(bytes, "UTF-8");
        }
    }

    // 辅助方法：查询商家已选的 ID，并存入一个 Set 中以便快速查找
    private Set<Integer> getSelectedIds(Connection conn, String sql, int accountId) throws SQLException {
        Set<Integer> idSet = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    idSet.add(rs.getInt(1)); // 获取第一列的 ID
                }
            }
        }
        return idSet;
    }
}
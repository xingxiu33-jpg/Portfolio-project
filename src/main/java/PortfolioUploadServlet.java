import java.io.IOException;
import java.io.InputStream; // 需要
import java.nio.file.Files; // 需要
import java.nio.file.Path;   // 需要
import java.nio.file.Paths; // 需要
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID; // 需要

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig; // 需要
import javax.servlet.annotation.WebServlet; // 需要
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part; // 需要

/*
 * ==========================================================
 * (1) 确保 @WebServlet 和 @MultipartConfig 都在这里
 * @WebServlet 必须匹配表单 C 的 action="portfolioUpload"
 * ==========================================================
 */
@WebServlet("/portfolioUpload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10, // 10MB
    maxRequestSize = 1024 * 1024 * 50 // 50MB
)
public class PortfolioUploadServlet extends HttpServlet {

	/**
     * 处理表单 C: 上传 *多个* 新作品
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);

        // 1. 权限检查
        if (session == null || !"merchant".equals(session.getAttribute("user_type"))) {
            response.sendRedirect("login.jsp?error=AccessDenied");
            return;
        }

        Integer accountId = (Integer) session.getAttribute("account_id");
        
        Connection conn = null;
        String sql = "INSERT INTO merchant_portfolio (account_id, image_path, title, description) " +
                     "VALUES (?, ?, ?, ?)";
        
        try {
            // 2. (关键) 先获取共享的文本信息
            String title = partToString(request.getPart("title"));
            String description = partToString(request.getPart("description"));

            // 3. 准备文件保存路径
            String savePath = "D:/my_project_uploads/portfolio"; // (确保路径正确)
            Path savePathDir = Paths.get(savePath);
            if (!Files.exists(savePathDir)) {
                Files.createDirectories(savePathDir);
            }

            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // *** 开启事务！***

            // 4. 准备批量插入
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                
                // 5. (关键) 循环遍历所有提交的 parts
                for (Part filePart : request.getParts()) {
                    
                    // 6. 只处理 "portfolio_images" 并且有文件
                    if ("portfolio_images".equals(filePart.getName()) && filePart.getSize() > 0) {
                        
                        String originalFileName = filePart.getSubmittedFileName();
                        if (originalFileName == null || originalFileName.isEmpty()) {
                            continue; // 跳过空的 part
                        }

                        // --- A. 保存文件到硬盘 ---
                        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                        String newPortfolioFilename = UUID.randomUUID().toString() + fileExtension;
                        filePart.write(savePath + "/" + newPortfolioFilename); // 保存文件

                        // --- B. 将 INSERT 操作添加到批处理 ---
                        ps.setInt(1, accountId);
                        ps.setString(2, newPortfolioFilename); // 只保存文件名
                        ps.setString(3, title);                // 使用共享的标题
                        ps.setString(4, description);          // 使用共享的描述
                        
                        ps.addBatch(); // 添加到批处理
                    }
                }
                
                // 7. (关键) 一次性执行所有 INSERT
                ps.executeBatch();
            }

            // 8. 提交事务
            conn.commit();

            // 9. 重定向回商家资料页
            response.sendRedirect("MerchantProfileServlet?success=portfolioUploaded");

        } catch (Exception e) {
            // 10. 如果出错，回滚所有更改
            if (conn != null) try { conn.rollback(); } catch (SQLException se) {}
            throw new ServletException("文件上传或数据库批量处理失败", e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) {} // 恢复默认
                try { conn.close(); } catch (SQLException e) {}
            }
        }
    }

   
    /*
     * ==========================================================
     * (2) 您需要将 partToString 辅助方法也复制到这个文件中
     * ==========================================================
     */
    private String partToString(Part part) throws IOException {
        if (part == null) {
            return null;
        }
        try (InputStream inputStream = part.getInputStream()) {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            return new String(bytes, "UTF-8");
        }
    }
}
import java.io.IOException;
import java.nio.file.Files; // 需要
import java.nio.file.Path;   // 需要
import java.nio.file.Paths; // 需要
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet; // 需要
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/*
 * ==========================================================
 * (1) @WebServlet 必须匹配表单 C 的 action="portfolioDelete"
 * ==========================================================
 */
@WebServlet("/portfolioDelete")
public class PortfolioDeleteServlet extends HttpServlet {

    /**
     * 处理删除作品集的请求
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);

        // 1. 权限检查：必须是登录的商家
        if (session == null || !"merchant".equals(session.getAttribute("user_type"))) {
            response.sendRedirect("login.jsp?error=AccessDenied");
            return;
        }

        Integer accountId = (Integer) session.getAttribute("account_id");
        
        // 2. 从隐藏表单 <input type="hidden"> 中获取要删除的 ID
        int portfolioId = Integer.parseInt(request.getParameter("portfolio_id"));

        Connection conn = null;
        String filenameToDelete = null; // 用于存储要从 D 盘删除的文件名

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // *** 开启事务！***

            // --- 步骤 A: (重要!) 先 *查询* 该商家是否有权删除，并获取文件名 ---
            
            // (这个查询既是安全检查，也是为了获取文件名)
            String sqlSelect = "SELECT image_path FROM merchant_portfolio WHERE " +
                               "portfolio_id = ? AND account_id = ?";
            
            try (PreparedStatement psSelect = conn.prepareStatement(sqlSelect)) {
                psSelect.setInt(1, portfolioId);
                psSelect.setInt(2, accountId); // (确保商家只能删除 *自己* 的照片)
                
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        filenameToDelete = rs.getString("image_path");
                    } else {
                        // 如果 rs.next() 为 false，意味着这个商家无权删除该 ID (可能在恶意操作)
                        throw new SecurityException("无权删除该作品或作品不存在。");
                    }
                }
            }
            
            // --- 步骤 B: 从数据库中删除记录 ---
            String sqlDelete = "DELETE FROM merchant_portfolio WHERE portfolio_id = ?";
            try (PreparedStatement psDelete = conn.prepareStatement(sqlDelete)) {
                psDelete.setInt(1, portfolioId);
                psDelete.executeUpdate();
            }

            // --- 步骤 C: 从硬盘中删除物理文件 ---
            if (filenameToDelete != null && !filenameToDelete.isEmpty()) {
                String savePath = "D:/my_project_uploads/portfolio"; // (必须与上传路径一致)
                Path filePath = Paths.get(savePath + "/" + filenameToDelete);
                
                try {
                    Files.deleteIfExists(filePath); // (安全删除，如果文件存在)
                } catch (IOException e) {
                    System.err.println("文件删除失败: " + filePath + " " + e.getMessage());
                    // (注意: 即使文件删除失败，我们通常也允许数据库事务继续，
                    // 因为记录被删除更重要)
                }
            }

            // --- 步骤 D: 提交事务 ---
            conn.commit(); // 只有当数据库删除成功时，才提交

            // --- 步骤 E: 重定向 ---
            response.sendRedirect("MerchantProfileServlet?success=portfolioDeleted");

        } catch (Exception e) {
            // 如果出错 (例如安全检查失败)，回滚所有更改
            if (conn != null) try { conn.rollback(); } catch (SQLException se) {}
            throw new ServletException("删除操作失败", e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) {} // 恢复默认
                try { conn.close(); } catch (SQLException e) {}
            }
        }
    }
}
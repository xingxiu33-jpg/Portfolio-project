import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
// ... (导入其他必要的 servlet 和 sql 包)
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/merchantProfile")
public class MerchantProfileServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        // 1. 权限检查：必须是登录的商家
        if (session == null || !"merchant".equals(session.getAttribute("user_type"))) {
            response.sendRedirect("login.jsp?error=AccessDenied");
            return;
        }

        Integer accountId = (Integer) session.getAttribute("account_id");
        Connection conn = null;

        try {
        	conn = DBUtil.getConnection(); // 假设的数据库连接方法
            
            // --- 任务 1: 获取商家的基本资料 (头像, 专长, 简介) ---
            // (使用 LEFT JOIN，因为商家可能是第一次编辑，profile 表中还没有记录)
            String sqlProfile = "SELECT * FROM merchant_profiles WHERE account_id = ?";
            PreparedStatement psProfile = conn.prepareStatement(sqlProfile);
            psProfile.setInt(1, accountId);
            ResultSet rsProfile = psProfile.executeQuery();
            if (rsProfile.next()) {
                request.setAttribute("profile", rsProfile);
            }

            // --- 任务 2: 加载所有筛选选项 (地区, 语言, 服务) ---
            request.setAttribute("allRegions", 
                conn.prepareStatement("SELECT * FROM regions").executeQuery());
            request.setAttribute("allLanguages", 
                conn.prepareStatement("SELECT * FROM languages").executeQuery());
            request.setAttribute("allServices", 
                conn.prepareStatement("SELECT * FROM services").executeQuery());

            // --- 任务 3: 加载商家 *已选择* 的选项 ID ---
            // 为了在 JSP 中预先勾选复选框
            request.setAttribute("myRegionIds", 
                getSelectedIds(conn, "SELECT region_id FROM merchant_service_areas WHERE account_id = ?", accountId));
            request.setAttribute("myLangIds", 
                getSelectedIds(conn, "SELECT lang_id FROM merchant_languages WHERE account_id = ?", accountId));
            request.setAttribute("myServiceIds", 
                getSelectedIds(conn, "SELECT service_id FROM merchant_services WHERE account_id = ?", accountId));

            // --- 任务 4: 加载商家的作品集 ---
            String sqlPortfolio = "SELECT * FROM merchant_portfolio WHERE account_id = ? ORDER BY uploaded_at DESC";
            PreparedStatement psPortfolio = conn.prepareStatement(sqlPortfolio);
            psPortfolio.setInt(1, accountId);
            request.setAttribute("portfolioItems", psPortfolio.executeQuery());

            // 5. 一切就绪, 转发到 JSP 页面
            request.getRequestDispatcher("/merchant_profile.jsp").forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("数据库查询出错", e);
        } catch (ClassNotFoundException e) {
	        // [!! 新增 !!] 捕获“驱动未找到”的错误
	        e.printStackTrace();
	        // 告诉用户是驱动程序出了问题
	        response.sendRedirect("login.jsp?error=DriverNotFound");
	        
	    }finally {
            // (确保在此处关闭所有连接、PS 和 RS)
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
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
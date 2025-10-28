

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    request.setCharacterEncoding("UTF-8");
	    String username = request.getParameter("username");

	    String pass = request.getParameter("password");
	  

	    Connection conn = null;
	    PreparedStatement pstmt = null; // 用于第一张表
	    ResultSet rs = null;

	    try {
	    	 conn = DBUtil.getConnection();
	        
	        // ===============================================
	        // 1. 开始事务：关闭自动提交
	        // ===============================================
	        conn.setAutoCommit(false);

	        // -----------------------------------------------
	        // 操作 1：插入数据到 'users' 表
	        // -----------------------------------------------
	        String sql = "SELECT password_hash, user_type FROM accounts WHERE username = ?;";
	        pstmt = conn.prepareStatement(sql);
	        pstmt.setString(1, username);
	       
	        rs = pstmt.executeQuery();
	        if (rs.next()) {
	            // 2. 找到用户，验证密码
	            String storedHash = rs.getString("password_hash");
	            String storedUserType = rs.getString("user_type");
	            
	            // 必须使用相同的哈希算法验证
	            if (PasswordUtil.hashPassword(pass).equals(storedHash)) {
	                // 3. 密码正确，创建 Session
	                HttpSession session = request.getSession();
	                session.setAttribute("username", username);
	                session.setAttribute("user_type", storedUserType); // 关键！
	                
	                // 4. 核心逻辑：根据用户类型重定向
	                if ("user".equals(storedUserType)) {
	                    response.sendRedirect("user_dashboard.jsp");
	                } else if ("merchant".equals(storedUserType)) {
	                    response.sendRedirect("merchant_dashboard.jsp");
	                } else {
	                    // 其他类型 (如果有)
	                    response.sendRedirect("index.jsp");
	                }
	                
	            } else {
	                // 密码错误
	                response.sendRedirect("login.jsp?error=InvalidCredentials");
	            }
	        } else {
	            // 用户名不存在
	            response.sendRedirect("login.jsp?error=InvalidCredentials");
	        }
	        
	    } catch (SQLException e) {
	        e.printStackTrace();
	        response.sendRedirect("login.jsp?error=DatabaseError");
	    } catch (ClassNotFoundException e) {
	        // [!! 新增 !!] 捕获“驱动未找到”的错误
	        e.printStackTrace();
	        // 告诉用户是驱动程序出了问题
	        response.sendRedirect("login.jsp?error=DriverNotFound");
	        
	    }finally {
	        // 必须这样逐个关闭，并单独捕获异常
	        try {
	            if (rs != null) {
	                rs.close();
	            }
	        } catch (SQLException e) {
	            e.printStackTrace(); 
	        }
	        
	        try {
	            if (pstmt != null) {
	                pstmt.close();
	            }
	        } catch (SQLException e) {
	            e.printStackTrace(); 
	        }
	        
	        try {
	            if (conn != null) {
	                conn.close();
	            }
	        } catch (SQLException e) {
	            e.printStackTrace(); 
	        }
	    }
	}
}

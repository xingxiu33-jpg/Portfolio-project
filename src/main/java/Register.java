

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Register
 */
@WebServlet("/Register")
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Register() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		request.setCharacterEncoding("UTF-8");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String gender =request.getParameter("gender");
        String date_of_birth = request.getParameter("bday");
        String name=request.getParameter("name");
        String userType = request.getParameter("user_type");
        String phone_number = request.getParameter("phone_number");
        // 1. 哈希处理密码
        String password_hash = PasswordUtil.hashPassword(password);

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            
            // 2. 使用 PreparedStatement 防止 SQL 注入
            String sql = "INSERT INTO accounts  (username,password_hash, email,name, date_of_birth,gender,phone_number, user_type) VALUES (?, ?, ?, ?, ?,?,?,?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2,password_hash );
            pstmt.setString(3, email);
            pstmt.setString(4, name);
            pstmt.setString(5, date_of_birth);
            pstmt.setString(6, gender);
            pstmt.setString(7, phone_number);
            pstmt.setString(8, userType);
            // 3. 执行SQL
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                // 注册成功，重定向到登录页面
                response.sendRedirect("login.jsp");
            } else {
                // 注册失败
                request.setAttribute("error", "登録に失敗しました。もう一度お試しください。");
                request.getRequestDispatcher("register.jsp").forward(request, response);
            }

        } catch (SQLException e) {
            // 捕获 'Duplicate entry' 错误 (用户名或邮箱已存在)
            if (e.getErrorCode() == 1062) { // 1062 是 MySQL 'Duplicate entry' 的错误码
                request.setAttribute("error", "ユーザー名またはメールアドレスはすでに登録されています。");
            } else {
                request.setAttribute("error", "数据库错误：" + e.getMessage());
            }
            request.getRequestDispatcher("register.jsp").forward(request, response);
        } catch (ClassNotFoundException e) {
            throw new ServletException(e);
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
	}

}

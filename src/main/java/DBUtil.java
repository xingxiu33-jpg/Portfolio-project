

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
// 连接数据库
public class DBUtil {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/pr_users?useSSL=false&serverTimezone=UTC";
    private static final String JDBC_USER = "root"; // 你的数据库用户名
    private static final String JDBC_PASSWORD = ""; // 你的数据库密码

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        // 1. 加载驱动
        Class.forName("com.mysql.cj.jdbc.Driver");
        // 2. 获取连接
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }
}
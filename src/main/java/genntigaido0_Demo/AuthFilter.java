package genntigaido0_Demo;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter("/*") // 拦截所有请求
public class AuthFilter implements Filter {

	public void init(FilterConfig fConfig) throws ServletException {
		// 过滤器初始化
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		HttpSession session = req.getSession(false); // false: 不自动创建新Session

		// 获取用户请求的URI
		String uri = req.getRequestURI();

		// 1. 检查Session中是否存在登录标记
		boolean isLoggedIn = (session != null && session.getAttribute("username") != null);

		// 2. 定义哪些页面是不需要登录就能访问的
		// 比如：登录页、注册页、处理登录的Servlet、CSS/JS等静态资源
		boolean isLoginOrRegisterPage = uri.endsWith("login.jsp") ||
				uri.endsWith("register.jsp") ||
				uri.endsWith("/LoginServlet") || // 你的登录Servlet URL
				uri.endsWith("/Register") || // 你的注册Servlet URL
				uri.startsWith(req.getContextPath() + "/css/") || // 假设CSS在/css/
				uri.startsWith(req.getContextPath() + "/js/"); // 假设JS在/js/

		// 3. 核心逻辑
		if (isLoggedIn || isLoginOrRegisterPage) {
			// 如果 (已登录) 或者 (访问的是登录/注册/静态资源)
			// 则放行，让请求继续
			chain.doFilter(request, response);
		} else {
			// 如果 (未登录) 且 (访问的是受保护页面)
			// 则重定向到登录页
			System.out.println("未登录，拦截到请求: " + uri + " -> 重定向到登录页");
			res.sendRedirect(req.getContextPath() + "/login.jsp");
		}
	}

	public void destroy() {
		// 过滤器销毁
	}
}

package com.example.chatserver.filter;

import com.example.chatserver.utils.JwtUtil;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.utils.UrlPermitUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

//JWT 认证过滤器，在请求到达 Controller 之前验证 Token 是否有效
@Component
public class AuthenticationTokenFilter extends OncePerRequestFilter { //确保每个请求只经过一次该过滤器

    @Resource
    private UrlPermitUtil urlPermitUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {

        //OPTIONS 请求	浏览器跨域请求前的预检请求
        //直接放行	不需要验证 Token，否则跨域会失败
        if ("OPTIONS".equalsIgnoreCase(httpServletRequest.getMethod())) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        //获取 Token 和 URL
        String tokenName = "x-token";
        String token = httpServletRequest.getHeader(tokenName);
        String url = httpServletRequest.getRequestURI();

        // 验证url是否需要验证
        if (!urlPermitUtil.isPermitUrl(url)) {
            Claims claims = null;
            try {
                claims = JwtUtil.parseToken(token); //获取token里的数据
            } catch (Exception e) {
                tokenInvalid(httpServletResponse);
                return;
            }

            // 设置用户信息
            Map<String, Object> map = new HashMap<>();
            claims.entrySet().stream().forEach(e -> map.put(e.getKey(), e.getValue()));
            httpServletRequest.setAttribute("userinfo", map);

            //验证角色是否有权限
            String role = (String) map.get("role");
            if (!urlPermitUtil.isRoleUrl(role, url)) {
                tokenInvalid(httpServletResponse);
                return;
            }
        }
        //放行
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    public void tokenInvalid(HttpServletResponse httpServletResponse) {
        try {
            // Token 无效，返回 403
            httpServletResponse.setContentType("application/json;charset=UTF-8");
            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            PrintWriter out = httpServletResponse.getWriter();
            out.write(ResultUtil.TokenInvalid().toJSONString(0));
            out.flush();
            out.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}

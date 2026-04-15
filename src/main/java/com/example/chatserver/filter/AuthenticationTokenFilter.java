package com.example.chatserver.filter;
import com.example.chatserver.constant.UserStatus;
import com.example.chatserver.utils.JwtUtil;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.utils.UrlPermitUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

//JWT 认证过滤器，在请求到达 Controller 之前验证 Token 是否有效
@Component
@Slf4j
public class AuthenticationTokenFilter extends OncePerRequestFilter { //确保每个请求只经过一次该过滤器

    @Resource
    private UrlPermitUtil urlPermitUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    @NotNull HttpServletResponse httpServletResponse,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {

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
            try {
                Claims claims = JwtUtil.parseToken(token);
                if (!setUserInfo(claims, url, httpServletRequest, httpServletResponse)) {
                    return;//不继续放行到controller,返回在setUserInfo已处理
                }
            } catch (Exception e) {
                writeResponse(httpServletResponse, ResultUtil.TokenInvalid());
                return; //不继续放行到controller
            }
        } else {
            //不验证时查看token是否为空，不为空存入用户信息
            if (StringUtils.isNotBlank(token)) {
                try {
                    Claims claims = JwtUtil.parseToken(token);
                    if (!setUserInfo(claims, url, httpServletRequest, httpServletResponse)) {
                        return;//不继续放行到controller,返回在setUserInfo已处理
                    }
                } catch (Exception e) {
                }
            }
        }
        //放行
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
    // 统一写回JSON响应，HTTP状态码始终为200，由body中的code区分业务状态
    private void writeResponse(HttpServletResponse httpServletResponse, Object result) {
        try {
            httpServletResponse.setContentType("application/json;charset=UTF-8");
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = httpServletResponse.getWriter();
            out.write(result.toString());
            out.flush();
            out.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    // 设置用户信息，返回false表示请求被拦截（前端已写回响应），不应继续放行
    public boolean setUserInfo(Claims claims, String url,
                               HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        // 设置用户信息
        Map<String, Object> map = new HashMap<>();
        claims.entrySet().forEach(e -> map.put(e.getKey(), e.getValue()));
        //验证角色是否有权限
        String role = (String) map.get("role");
        if (!urlPermitUtil.isRoleUrl(role, url)) {
            writeResponse(httpServletResponse, ResultUtil.Forbidden());
            return false;
        }
        //验证是否被禁用
        String status = (String) map.get("status");
        if (status.equals(UserStatus.Disable)) {
            writeResponse(httpServletResponse, ResultUtil.TokenInvalid());
            return false;
        }
        httpServletRequest.setAttribute("userinfo", map);
        return true;
    }
}
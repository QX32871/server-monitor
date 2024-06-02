package com.qx32871.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.qx32871.entity.RestBean;
import com.qx32871.entity.dto.AccountDTO;
import com.qx32871.entity.dto.ClientDTO;
import com.qx32871.service.AccountService;
import com.qx32871.service.ClientService;
import com.qx32871.utils.Const;
import com.qx32871.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 用于对请求头中Jwt令牌进行校验的工具，为当前请求添加用户验证信息
 * 并将用户的ID存放在请求对象属性中，方便后续使用
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private JwtUtils utils;

    @Resource
    private ClientService clientService;

    @Resource
    private AccountService accountService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/client")) {
            if (!uri.endsWith("/register")) {
                ClientDTO client = clientService.findClientByToken(authorization);
                if (client == null) {
                    response.setStatus(401);
                    response.getWriter().write(RestBean.failure(401, "未注册").asJsonString());
                    return;
                } else {
                    request.setAttribute(Const.ATTR_CLIENT, client);
                }
            }
        } else {
            DecodedJWT jwt = utils.resolveJwt(authorization);
            if (jwt != null) {
                UserDetails user = utils.toUser(jwt);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                request.setAttribute(Const.ATTR_USER_ID, utils.toId(jwt));
                request.setAttribute(Const.ATTR_USER_ROLE, new ArrayList<>(user.getAuthorities()).get(0).getAuthority());

                //TODO 以后优化吧
                if (request.getRequestURI().startsWith("/terminal/") && !accessShell(
                        (int) request.getAttribute(Const.ATTR_USER_ID),
                        (String) request.getAttribute(Const.ATTR_USER_ROLE),
                        Integer.parseInt(request.getRequestURI().substring(10)))) {
                    response.setStatus(401);
                    response.setCharacterEncoding("utf-8");
                    response.getWriter().write(RestBean.failure(401, "无权访问").asJsonString());
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean accessShell(int userId, String userRole, int clientId) {
        if (Const.ROLE_ADMIN.equals(userRole.substring(5))) {
            return true;
        } else {
            AccountDTO account = accountService.getById(userId);
            return account.getClientList().contains(clientId);
        }
    }
}

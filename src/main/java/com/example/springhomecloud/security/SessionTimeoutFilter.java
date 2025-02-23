package com.example.springhomecloud.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import org.springframework.lang.NonNull;

@Component
public class SessionTimeoutFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null && !isPublicUrl(request.getRequestURI())) {
            long lastAccessedTime = session.getLastAccessedTime();
            long currentTime = System.currentTimeMillis();
            long maxInactiveInterval = session.getMaxInactiveInterval() * 1000L;

            if ((currentTime - lastAccessedTime) > maxInactiveInterval) {
                session.invalidate();
                response.sendRedirect("/login?expired");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isPublicUrl(String url) {
        return url.startsWith("/login") ||
                url.startsWith("/register") ||
                url.startsWith("/css") ||
                url.startsWith("/js");
    }
}
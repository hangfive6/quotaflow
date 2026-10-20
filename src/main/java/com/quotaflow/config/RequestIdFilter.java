package com.quotaflow.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

@Component @Order(1)
public class RequestIdFilter extends OncePerRequestFilter {
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String id = request.getHeader("X-Request-Id");
        if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
        request.setAttribute("requestId", id); response.setHeader("X-Request-Id", id); MDC.put("requestId", id);
        try { chain.doFilter(request, response); } finally { MDC.remove("requestId"); }
    }
}


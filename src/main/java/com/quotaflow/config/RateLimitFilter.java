package com.quotaflow.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component @Order(2)
public class RateLimitFilter extends OncePerRequestFilter {
    private record Window(long second, AtomicInteger count) {}
    private final ConcurrentHashMap<String, Window> clients = new ConcurrentHashMap<>();
    private final int requestsPerSecond;
    public RateLimitFilter(@Value("${app.rate-limit-per-second:30}") int requestsPerSecond) { this.requestsPerSecond = requestsPerSecond; }
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        if (!req.getRequestURI().startsWith("/api/")) { chain.doFilter(req, res); return; }
        long second = Instant.now().getEpochSecond();
        Window window = clients.compute(req.getRemoteAddr(), (k, old) -> old == null || old.second() != second ? new Window(second, new AtomicInteger(1)) : new Window(second, new AtomicInteger(old.count().incrementAndGet())));
        if (window.count().get() > requestsPerSecond) { res.setStatus(429); res.setContentType("application/json"); res.getWriter().write("{\"code\":\"RATE_LIMITED\",\"message\":\"Too many requests\"}"); return; }
        chain.doFilter(req, res);
    }
}


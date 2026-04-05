package com.smartgaon.ai.smartgaon_api.JwtUtil;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        long start = System.currentTimeMillis();

        log.info("Incoming request: {} {}", req.getMethod(), req.getRequestURI());

        try {
            chain.doFilter(request, response);
        } catch (Exception ex) {

            long time = System.currentTimeMillis() - start;

            // ✅ error log
            log.error("Error in request: {} {} in {} ms",
                    req.getMethod(),
                    req.getRequestURI(),
                    time,
                    ex);

            throw ex; // ⚠️ must rethrow
        } finally {

            long time = System.currentTimeMillis() - start;

            // ✅ always log (success + error)
            log.info("Completed request: {} {} -> {} in {} ms",
                    req.getMethod(),
                    req.getRequestURI(),
                    res.getStatus(),
                    time);
        }
    }
}
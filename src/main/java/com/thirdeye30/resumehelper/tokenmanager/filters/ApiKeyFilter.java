package com.thirdeye30.resumehelper.tokenmanager.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyFilter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${thirdeye.resume.updater.api.key}")
    private String apiKey;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/statuschecker");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestApiKey = request.getHeader("THIRDEYE_RESUME_UPDATER_API_KEY");
        if (requestApiKey == null) {
            requestApiKey = request.getParameter("THIRDEYE_RESUME_UPDATER_API_KEY");
        }

        if (apiKey != null && apiKey.equals(requestApiKey)) {
            filterChain.doFilter(request, response);
        } else {
            sendUnauthorizedResponse(response);
        }
    }

    private void sendUnauthorizedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("status", 401);
        errorResponse.put("message", "Invalid Request");
        errorResponse.put("data", null);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
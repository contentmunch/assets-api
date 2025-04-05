package com.contentmunch.assets.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HtmlNoCacheFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        chain.doFilter(request, response);

        if (response instanceof HttpServletResponse httpResp) {
            String contentType = httpResp.getContentType();

            if (contentType != null && contentType.contains("text/html")) {
                httpResp.setHeader("Cache-Control", "no-store, must-revalidate");
                httpResp.setHeader("Pragma", "no-cache");
                httpResp.setDateHeader("Expires", 0);
            }
        }
    }
}

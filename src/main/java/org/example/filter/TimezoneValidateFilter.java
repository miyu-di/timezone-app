package org.example.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZoneId;

@WebFilter("/time")
public class TimezoneValidateFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String tz = req.getParameter("timezone");

        if (tz != null) {
            tz = tz.replace(" ", "+");

            boolean isValid = ZoneId.getAvailableZoneIds().contains(tz)
                    || tz.matches("^UTC[+-]\\d{1,2}$");

            if (!isValid) {
                resp.setStatus(400);
                resp.setContentType("text/html; charset=UTF-8");
                resp.getWriter().write("Invalid timezone");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
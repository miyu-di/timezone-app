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

        // Кастимо до HTTP, щоб з'явилися методи getParameter та setStatus
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String tz = req.getParameter("timezone");

        if (tz != null) {
            tz = tz.replace(" ", "+"); // Рятуємо знак "+" від перетворення на пробіл

            // Валідація: або є в базі Java, або підходить під паттерн UTC+X чи UTC-X
            boolean isValid = ZoneId.getAvailableZoneIds().contains(tz)
                    || tz.matches("^UTC[+-]\\d{1,2}$");

            if (!isValid) {
                resp.setStatus(400); // Чистий HTTP код 400 Bad Request без зайвих імпортів
                resp.setContentType("text/html; charset=UTF-8");
                resp.getWriter().write("Invalid timezone");
                return; // Обриваємо ланцюжок, далі запит не йде
            }
        }

        // Якщо все ок — пропускаємо запит далі до сервлету
        chain.doFilter(request, response);
    }
}
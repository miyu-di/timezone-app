package org.example.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet("/time")
public class TimeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String tz = req.getParameter("timezone");
        ZoneId zoneId;
        String tzLabel;

        if (tz == null || tz.trim().isEmpty()) {
            zoneId = ZoneId.of("UTC");
            tzLabel = "UTC";
        } else {
            tz = tz.replace(" ", "+"); // Лікуємо пробіл з URL
            tzLabel = tz;

            if (ZoneId.getAvailableZoneIds().contains(tz)) {
                zoneId = ZoneId.of(tz);
            } else {
                // Перетворюємо кастомний UTC+2 на зрозумілий для Java GMT+2
                String javaFriendlyTz = tz.replace("UTC", "GMT");
                zoneId = ZoneId.of(javaFriendlyTz);
            }
        }

        // Форматуємо час
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String result = now.format(formatter) + " " + tzLabel;

        // Віддаємо відповідь
        resp.setContentType("text/html; charset=UTF-8");
        resp.getWriter().write("<h1>" + result + "</h1>");
    }
}
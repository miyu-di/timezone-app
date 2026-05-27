package org.example.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet("/time")
public class TimeServlet extends HttpServlet {
    private JakartaServletWebApplication application;
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        // Налаштування Thymeleaf для Jakarta Сервлетів (Thymeleaf 3.1+)
        this.application = JakartaServletWebApplication.buildApplication(getServletContext());

        WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(this.application);
        templateResolver.setTemplateMode(TemplateMode.HTML);

        // Вказуємо шлях до папки з шаблонами всередині webapp (наприклад, WEB-INF/templates/)
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding("UTF-8");

        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String tzParam = req.getParameter("timezone");
        String resolvedTz = null;

        if (tzParam != null && !tzParam.trim().isEmpty()) {
            // Якщо параметр є, чистимо пробіли (як у твоїй початковій логіці)
            resolvedTz = tzParam.replace(" ", "+");

            // Зберігаємо цей валідний часовий пояс у Cookie
            Cookie cookie = new Cookie("lastTimezone", resolvedTz);
            cookie.setMaxAge(60 * 60 * 24); // Кукі житиме 1 добу
            cookie.setPath("/time");        // Доступно тільки для цього сервлету
            resp.addCookie(cookie);
        } else {
            // Якщо параметра немає, шукаємо часовий пояс у Cookie
            Cookie[] cookies = req.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("lastTimezone".equals(c.getName())) {
                        resolvedTz = c.getValue();
                        break;
                    }
                }
            }
            // Якщо і в Cookie порожньо — дефолтимо на UTC
            if (resolvedTz == null) {
                resolvedTz = "UTC";
            }
        }

        // Парсимо ZoneId на основі визначеного resolvedTz
        ZoneId zoneId;
        if (ZoneId.getAvailableZoneIds().contains(resolvedTz)) {
            zoneId = ZoneId.of(resolvedTz);
        } else {
            String javaFriendlyTz = resolvedTz.replace("UTC", "GMT");
            zoneId = ZoneId.of(javaFriendlyTz);
        }

        // Форматуємо поточний час
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = now.format(formatter);

        // Готуємо контекст даних для Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(req, resp);
        WebContext context = new WebContext(webExchange, webExchange.getLocale());

        // Передаємо змінні в HTML шаблон
        context.setVariable("currentTime", formattedTime);
        context.setVariable("timezone", resolvedTz);

        // Встановлюємо правильний контент-тайп та рендеримо сторінку
        resp.setContentType("text/html; charset=UTF-8");
        templateEngine.process("time", context, resp.getWriter());
    }
}
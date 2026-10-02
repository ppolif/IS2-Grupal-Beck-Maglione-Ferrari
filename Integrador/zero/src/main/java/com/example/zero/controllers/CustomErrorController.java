package com.example.zero.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, HttpServletResponse response, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        if (status != null) {
            try {
                int statusCode = Integer.parseInt(status.toString());
                response.setStatus(statusCode);

                if (statusCode == HttpStatus.NOT_FOUND.value()) {
                    model.addAttribute("errorMessage", "La página o recurso que buscas no existe o ha sido movido temporalmente.");
                    return "admin/page-404";
                }
            } catch (NumberFormatException ignored) {}
        }

        model.addAttribute("errorMessage", message != null && !message.toString().isBlank()
                ? message.toString()
                : "La página que buscas no existe o ha sido movida temporalmente.");
        return "admin/page-404";
    }
}

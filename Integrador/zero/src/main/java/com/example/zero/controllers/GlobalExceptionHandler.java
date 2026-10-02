package com.example.zero.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler({org.springframework.web.servlet.resource.NoResourceFoundException.class,
                       org.springframework.web.servlet.NoHandlerFoundException.class})
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
    public String handleNotFound(Exception ex, org.springframework.ui.Model model) {
        model.addAttribute("errorMessage", "El recurso o producto solicitado no fue encontrado.");
        return "admin/page-404";
    }
}

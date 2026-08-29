package com.example.tinder.controladores;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
public class ErrorController implements org.springframework.boot.webmvc.error.ErrorController {

    @RequestMapping(value = "/error", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView renderErrorPage(HttpServletRequest httpRequest){

        ModelAndView errorPage = new ModelAndView("error");
        String errorMsg = "";
        int httpErrorCode = getErrorCode(httpRequest);

        switch (httpErrorCode){
            case 400:{
                errorMsg = "El recurso solicitado no existe";
                break;
            }

            case 403: {
                errorMsg = "No tiene permisos para acceder a este recurso";
                break;
            }

            case 401:{
                errorMsg = "No se encuentra autorizado";
                break;
            }

            case 404: {
                errorMsg = "El recurso solicitado no fue encontrado";
                break;
            }

            case 500: {
                errorMsg = "Ocurrió un error interno";
                break;
            }

        }
        errorPage.addObject("codigo", httpErrorCode);
        errorPage.addObject("mensaje",errorMsg);
        return errorPage;
    }

    private int getErrorCode(HttpServletRequest httpRequest) {
        // Buscamos el atributo estándar donde los servidores guardan el código de error
        Object errorCode = httpRequest.getAttribute("jakarta.servlet.error.status_code");

        if (errorCode != null) {
            // Si hay un error, lo convertimos a int y lo devolvemos
            return Integer.parseInt(errorCode.toString());
        }

        // Si no hay error en la petición, devolvemos 0
        return 0;
    }


}

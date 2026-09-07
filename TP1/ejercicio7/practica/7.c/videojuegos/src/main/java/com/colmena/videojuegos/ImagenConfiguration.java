package com.colmena.videojuegos;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class ImagenConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path rutaDirectorio = Paths.get("imagenes");
        String rutaAbsoluta = rutaDirectorio.toFile().getAbsolutePath();

        File dir = new File(rutaAbsoluta);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        registry.addResourceHandler("/imagenes/**")
                .addResourceLocations("file:" + rutaAbsoluta + "/");
    }
}
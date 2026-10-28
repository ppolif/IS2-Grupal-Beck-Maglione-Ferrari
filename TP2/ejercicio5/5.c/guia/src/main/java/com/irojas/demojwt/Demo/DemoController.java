package com.irojas.demojwt.Demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = {"/api", "/api/v1"})
@RequiredArgsConstructor
public class DemoController {
    
    @GetMapping(value = "demo")
    public String welcomeGet()
    {
        return "You accessed our protected endpoint";
    }

    @PostMapping(value = "demo")
    public String welcomePost()
    {
        return "You accessed our protected endpoint";
    }
}

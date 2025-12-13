package com.mycompany.sapo_leyendo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    // Forward all non-API, non-static routes to index.html for SPA routing
    @RequestMapping(value = {
        "/",
        "/{path:[^\\.]*}",
        "/{path:[^\\.]*}/{subpath:[^\\.]*}",
        "/{path:[^\\.]*}/{subpath:[^\\.]*}/{subsubpath:[^\\.]*}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}

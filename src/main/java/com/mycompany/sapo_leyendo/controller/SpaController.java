package com.mycompany.sapo_leyendo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    // Forward all non-API, non-static routes to index.html for SPA routing
    // Exclude /api/** paths to let REST controllers handle them
    @RequestMapping(value = {
        "/",
        "/{path:^(?!api$).*}",
        "/{path:^(?!api$).*}/{subpath:[^\\.]*}",
        "/{path:^(?!api$).*}/{subpath:[^\\.]*}/{subsubpath:[^\\.]*}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}

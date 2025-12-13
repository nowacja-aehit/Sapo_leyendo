package com.mycompany.sapo_leyendo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    // Match everything without a suffix (so not .js, .css, etc)
    @RequestMapping(value = "/{path:[^\\.]*}")
    public String redirect() {
        return "forward:/index.html";
    }

    // Also handle the root path
    @RequestMapping(value = "/")
    public String root() {
        return "forward:/index.html";
    }
}

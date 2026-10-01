package com.roknauta.milheiro.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/index.xhtml"})
    public String home() {
        return "redirect:/dashboard.xhtml";
    }
}

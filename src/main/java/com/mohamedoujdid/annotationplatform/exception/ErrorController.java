package com.mohamedoujdid.annotationplatform.exception;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    @GetMapping("/error/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}
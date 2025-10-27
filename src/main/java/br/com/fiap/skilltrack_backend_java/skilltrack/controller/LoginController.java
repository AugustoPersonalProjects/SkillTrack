package br.com.fiap.skilltrack_backend_java.skilltrack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login-page")
    public String exibirLogin() {
        return "login";
    }
}


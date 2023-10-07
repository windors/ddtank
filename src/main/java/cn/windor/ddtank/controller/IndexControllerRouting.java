package cn.windor.ddtank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexControllerRouting {
    @GetMapping({"", "/", "/index"})
    public String index() {
        return "index";
    }
}
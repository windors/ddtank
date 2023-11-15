package cn.windor.ddtank.controller;

import cn.windor.ddtank.service.DDTankDetailService;
import cn.windor.dto.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/detail")
public class DetailController {

    @Autowired
    private DDTankDetailService ddTankDetailService;

    @PostMapping("/{hwnd}/taskAutoComplete")
    public HttpResponse taskAutoComplete(@PathVariable long hwnd,
                                         @RequestParam int taskAutoComplete) {
        return HttpResponse.auto(ddTankDetailService.setTaskAutoComplete(hwnd, taskAutoComplete));
    }
}

package cn.windor.ddtank.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后端调用统一错误处理类
 */
@Controller
@Slf4j
public class CustomerErrorController extends BasicErrorController {
    InetAddress localHost = null;

    @Autowired
    Environment environment;


    {
        try {
            localHost = Inet4Address.getLocalHost();
        } catch (UnknownHostException e) {
            log.error(e.getMessage(), e);
        }
    }


    public CustomerErrorController() {
        super(new DefaultErrorAttributes(), new ErrorProperties());
    }

    public CustomerErrorController(ErrorAttributes errorAttributes, ErrorProperties errorProperties) {
        super(errorAttributes, errorProperties);
    }

    public CustomerErrorController(ErrorAttributes errorAttributes, ErrorProperties errorProperties, List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorProperties, errorViewResolvers);
    }

    @Override
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        map.put("code", status.value());
        map.put("msg", request.getAttribute("msg") == null ? status.name() : request.getAttribute("msg"));
        map.put("data", data);
        data.put("ip", localHost.getHostAddress());
        data.put("port", environment.getProperty("local.server.port"));
        data.put("uri", request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
        if (status == HttpStatus.NO_CONTENT) {
            return new ResponseEntity<>(map, status);
        }
        return new ResponseEntity<>(map, status);
    }

    @Override
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        HttpStatus status = getStatus(request);
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        map.put("code", status.value());
        map.put("msg", request.getAttribute("msg") == null ? status.name() : request.getAttribute("msg"));
        map.put("data", data);
        data.put("ip", localHost.getHostAddress());
        data.put("port", environment.getProperty("local.server.port"));
        data.put("uri", request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
        data.put("referer", request.getHeader("Referer"));
        response.setStatus(status.value());
        return new ModelAndView("error/error", map);
    }
}

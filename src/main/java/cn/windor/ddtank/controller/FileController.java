package cn.windor.ddtank.controller;

import cn.windor.ddtank.core.DDTankCoreScript;
import cn.windor.ddtank.core.DDTankLog;
import cn.windor.ddtank.service.DDTankScriptService;
import cn.windor.ddtank.service.DDTankThreadService;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileController {
    @Autowired
    private DDTankThreadService threadService;

    @Autowired
    private DDTankScriptService scriptService;

    @GetMapping("/export/logs")
    public void exportLogs(HttpServletResponse response,
                           @RequestParam(value = "hwnd", required = false) List<Long> hwnds,
                           @RequestParam(value = "index", required = false) List<Integer> indexList) throws IOException {
        List<DDTankCoreScript> scripts = new ArrayList<>();
        if(hwnds != null) {
            for (Long hwnd : hwnds) {
                DDTankCoreScript script = threadService.get(hwnd);
                if (script != null) {
                    scripts.add(script);
                }
            }
        }else if (indexList != null) {
            for (Integer index : indexList) {
                scripts.add(scriptService.getByIndex(index));
            }
        }else {
            return;
        }
        exportScriptsLogs(response, scripts);
    }

    private void exportScriptsLogs(HttpServletResponse response, List<DDTankCoreScript> scripts) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-hh:mm:ss:SSS"));
        String fileName = URLEncoder.encode("脚本运行日志-" + time, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 这里需要设置不关闭流
        ExcelWriterBuilder excelWriterBuilder = EasyExcel.write(response.getOutputStream(), DDTankLog.Log.class).inMemory(true).autoCloseStream(Boolean.FALSE);
        for (DDTankCoreScript script : scripts) {
            List<DDTankLog.Log> logs = script.getDDTankLog().getLogs();
            excelWriterBuilder.sheet(script.getName()).doWrite(logs);
        }
    }
}

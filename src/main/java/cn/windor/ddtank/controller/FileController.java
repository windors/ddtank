package cn.windor.ddtank.controller;

import cn.windor.ddtank.core.DDTankCoreScript;
import cn.windor.ddtank.core.DDTankLog;
import cn.windor.ddtank.service.DDTankScriptService;
import cn.windor.ddtank.service.DDTankThreadService;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
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
public class FileController extends BaseScriptController {

    @GetMapping("/export/logs")
    public void exportLogs(HttpServletResponse response,
                           @RequestParam(value = "hwnd", required = false) List<Long> hwnds,
                           @RequestParam(value = "index", required = false) List<Integer> indexList) throws IOException {
        exportScriptsLogs(response, getScripts(hwnds, indexList));
    }

    private void exportScriptsLogs(HttpServletResponse response, List<DDTankCoreScript> scripts) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-hh:mm:ss:SSS"));
        String fileName = URLEncoder.encode("脚本运行日志-" + time, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 这里需要设置不关闭流
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream())
                .head(DDTankLog.Log.class)
                .inMemory(true).autoCloseStream(false)
                .build();
        int i = 0;
        for (DDTankCoreScript script : scripts) {
            List<DDTankLog.Log> logs = script.getDDTankLog().getLogs();
            WriteSheet writeSheet = EasyExcel.writerSheet(i++, script.getName()).build();
            excelWriter.write(logs, writeSheet);
        }
        excelWriter.finish();
    }
}

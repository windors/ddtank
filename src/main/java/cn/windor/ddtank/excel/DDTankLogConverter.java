package cn.windor.ddtank.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.data.RichTextStringData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class DDTankLogConverter implements Converter<String> {
    @Override
    public WriteCellData<?> convertToExcelData(WriteConverterContext<String> context) throws Exception {

        String msg = context.getValue();
        if(Pattern.matches("^<span class=\".*\">.*</span>$", msg)) {
            // 设置字体样式
            RichTextStringData richTextStringData = new RichTextStringData();
            WriteFont writeFont = new WriteFont();
            if(msg.startsWith("<span class=\"log-success\">")) {
                writeFont.setColor(IndexedColors.GREEN.getIndex());
            }else if(msg.startsWith("<span class=\"log-primary\">")) {
                writeFont.setColor(IndexedColors.BLUE.getIndex());
            }else if(msg.startsWith("<span class=\"log-warn\">")) {
                writeFont.setColor(IndexedColors.DARK_YELLOW.getIndex());
            }else if(msg.startsWith("<span class=\"log-error\">")) {
                writeFont.setColor(IndexedColors.RED.getIndex());
            }
            msg = msg.replaceAll("<span class=\".*\">", "").replaceAll("</span>", "");
            // 应用字体样式
            richTextStringData.setTextString(msg);
            richTextStringData.setWriteFont(writeFont);
            WriteCellData<String> result = new WriteCellData<>();
            result.setType(CellDataTypeEnum.RICH_TEXT_STRING);
            result.setRichTextStringDataValue(richTextStringData);
            return result;
        }
        return new WriteCellData<>(msg);
    }

}

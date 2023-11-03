// 当页面发生切换时刷新选择难度的图片
function freshSlider() {
    $(".sliderlens.horiz").css("width", "100%");
    $(".sliderlens.horiz").css("height", "34px");
    $(".handle .slider-img").css("height", "auto");
}

// 一键设置与批量启动
$("#start-all-form").submit(function () {
    let allForm = $(this);
    // 版本配置
    $(".ddtank-start-form select[name='version']").val(allForm.find("select[name='version']").val());
    $(".ddtank-start-form select[name='propertiesMode']").val(allForm.find("select[name='propertiesMode']").val());
    $(".ddtank-start-form select[name='levelLine']").val(allForm.find("select[name='levelLine']").val());
    $(".ddtank-start-form select[name='levelRow']").val(allForm.find("select[name='levelRow']").val());
    $(".ddtank-start-form input[name='levelDifficulty']").val(allForm.find("input[name='levelDifficulty']").val());
    $(".ddtank-start-form input[name='attackSkill']").val(allForm.find("input[name='attackSkill']").val());
    $(".ddtank-start-form select[name='enemyFindMode']").val(allForm.find("select[name='enemyFindMode']").val());
    $(".ddtank-start-form input[name='isHandleCalcDistance']").attr("checked", false);
    if (allForm.find("input[name='isHandleCalcDistance']").val() === 'true') {
        $(".ddtank-start-form input[name='isHandleCalcDistance'][value='true']").attr("checked", true);
    } else {
        $(".ddtank-start-form input[name='isHandleCalcDistance'][value='false']").attr("checked", true);
    }
    $(".ddtank-start-form input[name='handleDistance']").val(allForm.find("input[name='handleDistance']").val());
    $(".ddtank-start-form").submit();
    windorAlert('提示', '已尝试一键启动');
    return false;
})

// 单个脚本启动
$(".ddtank-start-form").submit(function () {
    postAjax("/util/start", $(this).serialize(), function (data) {
        windorAlert('提示', '脚本启动成功！');
    });
    return false;
})

// 重启某个线程
function restart(hwnd) {
    postAjax("/util/restart", {hwnd: hwnd}, function (data) {
        windorAlert('提示', '已重启脚本')
    });
}

// 移除某个线程
function remove(hwnd) {
    postAjax("/util/remove", {hwnd: hwnd}, function (data) {
        windorAlert('提示', '已移除脚本')
    });
}

// 停止某个脚本
function stop(hwnd) {
    postAjax("/util/stop", {hwnd: hwnd}, function (data) {
        windorAlert('提示', '已停止脚本')
    })
}

function getCheckedHwndSerialize() {
    let hwnds = "";
    $("#ddt input[type='checkbox'][name='hwnd']").each(function (index, checkbox) {
        if ($(checkbox).prop('checked')) {
            hwnds = hwnds + "hwnd=" + $(checkbox).val() + "&";
        }
    })
    if (hwnds.length > 0) {
        hwnds = hwnds.substring(0, hwnds.length - 1);
    }
    return hwnds;
}

// 多选框的选择
$(function () {
    $(".time-ms").each(function (i, obj) {
        let ms = $(obj).html();
        ms = Math.floor(ms / 1000);
        let hour = Math.floor(ms / 3600);
        if(hour < 10) {
            hour = "0" + hour;
        }
        let minute = Math.floor(ms % 3600 / 60);
        if(minute < 10) {
            minute = "0" + minute;
        }
        let second = ms % 60;
        if(second < 10) {
            second = "0" + second;
        }
        $(obj).html(hour + ":" + minute + ":" + second)
    })
    // 取消继承
    $("#ddt tr td:first-child input,th:first-child input").each(function (i, input) {
        $(input).click(function (e) {
            e.stopPropagation();
        });
    });
    $("#ddt a").each(function (i, a) {
        $(a).click(function (e) {
            e.stopPropagation();
        })
    });

    // 点击input的上一级的时候就点击input
    $("#ddt tr td, th").click(function () {
        $(this).parent().find("input[type='checkbox']").click();
    })
    // 全选按钮
    $("#ddt tr th:first-child input").click(function () {
        let checked = $(this).prop('checked');
        $("#ddt tr td:first-child input").each(function (i, checkbox) {
            $(checkbox).prop('checked', checked);
        })
    })


    $("#ddt-suspend").click(function () {
        let hwnds = getCheckedHwndSerialize();
        if(hwnds === "") {
            windorAlert('提示', '请 <b>选中一些脚本后</b> 再执行本方法')
        }else{
            postAjax('/util/suspend', hwnds, function (data) {
                windorAlert('提示', '已尝试暂停选中的脚本');
            })
        }
    })
    $("#ddt-continue").click(function () {
        let hwnds = getCheckedHwndSerialize();
        if(hwnds === "") {
            windorAlert('提示', '请 <b>选中一些脚本后</b> 再执行本方法')
        }else{
            postAjax('/util/continue', hwnds, function (data) {
                windorAlert('提示', '已尝试恢复选中的脚本');
            })
        }
    })
    $("#ddt-stop").click(function () {
        let hwnds = getCheckedHwndSerialize();
        if(hwnds === "") {
            windorAlert('提示', '请 <b>选中一些脚本后</b> 再执行本方法')
        }else{
            postAjax('/util/stop', hwnds, function (data) {
                windorAlert('提示', '已尝试停止选中的脚本');
            })
        }
    })
    $("#ddt-restart").click(function () {
        let hwnds = getCheckedHwndSerialize();
        if(hwnds === "") {
            windorAlert('提示', '请 <b>选中一些脚本后</b> 再执行本方法')
        }else{
            postAjax('/util/restart', hwnds, function (data) {
                windorAlert('提示', '已尝试重启指定脚本');
            })
        }
    })
    $("#ddt-remove").click(function () {
        let hwnds = getCheckedHwndSerialize();
        if(hwnds === "") {
            windorAlert('提示', '请 <b>选中一些脚本后</b> 再执行本方法')
        }else{
            postAjax('/util/remove', hwnds, function (data) {
                windorAlert('提示', '已尝试移除指定脚本');
            })
        }
    })
})

/*曲包管理*/
var packClicked = false;
var toggleCss = "pack-chose";

// 懒加载一个曲包
$("#pack").click(function () {
    if (!packClicked) {
        packClicked = true;
        getAjax("/package", {}, function (data) {
            for (let pack in data.data) {
                let tr = createPackNode(data.data[pack]);
                // 再次发送ajax获取当前谱面是否已经被添加在包里
                getAjax("/package/record/" + data.data[pack].id + "/" + cid, {}, function (data) {
                    if (data.data) {
                        $(tr).toggleClass(toggleCss);
                    }
                })
            }
        });
    }
    $("#pack-modal").modal('show');
})

// 新增一个打包
$("#pack-add").click(function () {
    postAjax("/package", {note: "新建曲包"}, function (data) {
        let p = createPackNode(data.data);
        $(p).click();
        $(p).dblclick();
    });
});

/**
 * 创建一个结点
 * @param data :{id: int, note: "str", createTime: "str", updateTime: "str"}
 * @return 存放点击切换css样式的结点
 */
function createPackNode(data) {
    let tr = document.createElement("tr");
    let td = document.createElement("td");
    $(tr).append(td);
    $(td).html(data.note);
    $(td).attr("pid", data.id);
    $(tr).click(function () {
        $(tr).toggleClass(toggleCss);
    });
    /*初始化双击函数，后面函数内部会互相调用*/
    $(td).dblclick(packDataDblclick);
    $(tr).dblclick(function () {
        $(td).dblclick();
    })
    $("#pack-data").append(tr);
    return tr;
}

// 删除已选中曲包
$("#pack-del").click(function () {
    if (confirm("确认删除选中的曲包吗?")) {
        let datas = $("#pack-data ." + toggleCss + " td");
        for (let i = 0; i < datas.length; i++) {
            let pid = $(datas[i]).attr("pid");
            deleteAjax("/package/" + pid, {}, function (data) {
                $(datas[i]).parent().remove();
            });
        }
    }
});

/**
 * 曲包双击改名
 */
function packDataDblclick() {
    let node = document.createElement("input");
    /*TODO 获取pid*/
    $(node).val($(this).html());
    $(node).addClass("form-control");
    let old = $(this)[0];
    $(node).blur(packDataBlur);
    // 需要保存id
    $(this).replaceWith(node);
    $(node).attr("pid", $(this).attr("pid"));
    $(node).focus();
}

/**
 * 失去焦点后执行的函数，需要更新包
 */
function packDataBlur() {
    // 发送AJAX，获取是否重用名成功
    console.log("request uri: /package/" + $(this).attr("pid") + ", 重用名: " + $(this).val());
    putAjax("/package/" + $(this).attr("pid"), {"note": $(this).val()});
    let node = document.createElement("td");
    node.ondblclick = packDataDblclick;
    $(node).html($(this).val());
    $(node).attr("pid", $(this).attr("pid"));
    $(this).replaceWith(node);
}


// 添加谱面记录，将选中的谱面进行更新
$("#pack-chart-add").click(function () {
    let ids = [];
    let data = $("#pack-data ." + toggleCss);

    for (let i = 0; i < data.length; i++) {
        ids[i] = $($(data[i]).children(0)).attr("pid");
    }

    if(ids.length === 0) {
        deleteAjax("/package/record/delete/" + cid, {}, function () {
            malodyAlert("提示", "操作成功!");
        })
    }else{
        $.ajax("/package/record/update/" + cid, {
            method: "POST",
            traditional: true,
            data: {
                pids: ids
            },
            success: function (data) {
                if (data.code === 200) {
                    malodyAlert("提示", "操作成功!");
                } else {
                    malodyResponseAlert(data);
                }
            },
            error: function () {
                malodyAlert("提示", "操作失败，服务器出错!");
            }
        });
    }
})
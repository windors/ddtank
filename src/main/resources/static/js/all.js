/* 统一返回方法 --- get */
function getAjax(url, data, successFunc) {
    $.get(url, data, function (data) {
        if (data.code === 200) {
            successFunc(data);
        } else {
            malodyResponseAlert(data);
        }
    })
}

/* 统一返回方法 --- post */
function postAjax(url, data, successFunc) {
    $.ajax(url, {
        type: "POST",
        data: data,
        success: function (data) {
            if (data.code === 200) {
                successFunc(data);
            } else {
                windorResponseAlert(data);
            }
        },
        error: function (error, text) {
            windorAlert("错误代码：" + error.status, error.responseJSON["error"]);
        }
    });
}

/*
*
*       var datas = new FormData();
        datas.append("lid", $("#lid").val());
        datas.append("file", $("#file-vedio")[0].files[0]);
        datas.append("file", $("#file-cover")[0].files[0]);
*
*
*/
function postFileAjax(url, data, successFunc) {
    $.ajax({
        type: 'post',
        url: url,
        data: data,
        async: true,
        cache: false,
        // 数据不需要编码
        contentType: false,
        // 数据对象不需要转化成键值对的方式
        processData: false,
        dataType: 'json',
        success: function (data) {
            if (data.code === 200) {
                successFunc(data);
            } else {
                windorResponseAlert(data);
            }
        }
    });
}

/* 统一返回方法 --- delete */
function deleteAjax(url, data, successFunc) {
    if (data instanceof Array) {
        data.push({
            name: "_method",
            value: "DELETE"
        })
    } else {
        data["_method"] = "DELETE";
    }
    $.post(url, data, function (data) {
        if (data.code === 200) {
            successFunc(data);
        } else {
            windorResponseAlert(data);
        }
    });
}

/* 统一返回方法 --- put */
function putAjax(url, data, successFunc) {
    if (data instanceof Array) {
        data.push({
            name: "_method",
            value: "PUT"
        })
    } else if (typeof data == 'string') {
        if (data.length === 0) {
            data += "_method=PUT";
        } else {
            data += "&_method=PUT"
        }
    } else {
        data["_method"] = "PUT";
    }
    $.post(url, data, function (data) {
        if (data.code === 200) {
            successFunc(data);
        } else {
            windorResponseAlert(data);
        }
    });
}

/*底部波浪*/
$(document).ready(function () {


    $(".official-plat ul li:first-child").hover(function () {
        $(".weixin").show();
        $(".weibo").hide();
    });
    $("li[title='点击打开官方微博']").hover(function () {
        $(".weixin").hide();
        $(".weibo").show();
    });

    //href="#a_null"的统一设置为无效链接
    $("a[href='#a_null']").click(function () {
        return false;
    });


});

//波浪动画
$(function () {
    var marqueeScroll = function (id1, id2, id3, timer) {
        var $parent = $("#" + id1);
        var $goal = $("#" + id2);
        var $closegoal = $("#" + id3);
        $closegoal.html($goal.html());

        function Marquee() {
            if (parseInt($parent.scrollLeft()) - $closegoal.width() >= 0) {
                $parent.scrollLeft(parseInt($parent.scrollLeft()) - $goal.width());
            } else {
                $parent.scrollLeft($parent.scrollLeft() + 1);
            }
        }

        setInterval(Marquee, timer);
    }
    var marqueeScroll1 = new marqueeScroll("marquee-box", "wave-list-box1", "wave-list-box2", 20);
    var marqueeScroll2 = new marqueeScroll("marquee-box3", "wave-list-box4", "wave-list-box5", 40);
});


// 毫秒转时间
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
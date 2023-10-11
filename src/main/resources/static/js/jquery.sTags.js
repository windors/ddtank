/*
	jQuery 标签插件 	ver 0.1

	https://github.com/28269890/sTags

	DEMO:https://28269890.github.io/sTags/
*/

(function ($) {
    $.fn.sTags = function (options) {
        var o = $.extend({}, $.fn.sTags.defaults, options);
        var id = Date.now() + "" + Math.ceil(Math.random() * 1000);
        var this_ = $(this);

        if (o.dataAttr.length == 3) {//设定数据属性
            for (var i in o.data) {
                if (!o.data[i].id) {
                    o.data[i].id = o.data[i][o.dataAttr[0]]
                }
                if (!o.data[i].name) {
                    o.data[i].name = o.data[i][o.dataAttr[1]]
                }
                if (!o.data[i].screen) {
                    o.data[i].screen = o.data[i][o.dataAttr[2]]
                }
            }
        }

        if (o.tagName == "") {
            o.tagName = "div"
        }

        var inputDiv = $('<div/>', {//定义绑定输入框的div 即 标签输入框
            class: o.tagInputCSS,
            "tag-id": id
        })
        var tagList = $('<div/>', {//定义选择数据的div
            class: o.tagListCSS,
            "tag-list-id": id
        })

        var list = function (target) {//列出数据 target目标div指 定义选择数据的div

            if (o.screen) {//如果启用筛选
                //定义筛选框
                $("<input>", o.screenInput).keyup(function () {
                    // var skey = $(this).val().replace(/[^a-zA-Z]/g,"")
                    var skey = $(this).val().trim();
                    if (skey === "") {
                        // 搜索框为空时展示所有
                        $(target + ">" + o.tagName + "[screen]").show()
                    } else {
                        $(target + ">" + o.tagName + "[screen]").hide()
                        $(target + ">" + o.tagName + "[screen^='" + skey + "']").show()
                    }
                }).appendTo(target)
            }

            var color = 0 //颜色内容
            var color_i = 0 //颜色计数
            var color_s = ""  //按筛选内容的首字母换色，保存首字母内容

            if (o.data.length > 0) {//如果有标签数据

                for (var i in o.data) {//循环标签数据

                    var attr = {}
                    for (var j in o.tagAttr) {
                        attr[j] = o.tagAttr[j].replace('{name}', o.data[i].name).replace('{id}', o.data[i].id)
                    }

                    attr.tagid = o.data[i].id //定义标签id

                    if (o.screen) { //如果启用筛选 则添加标签属性
                        attr.screen = o.data[i].screen
                    }

                    if (o.color == 1 && o.data[i].screen) { //按照筛选首字母换色
                        if (color_s == "") {
                            color_s = o.data[i].screen.substr(0, 1)
                            color = o.colorData[color_i]
                        } else {
                            if (color_s != o.data[i].screen.substr(0, 1)) {
                                color_i++
                                if (color_i == o.colorData.length) {
                                    color_i = 0
                                }
                                color_s = o.data[i].screen.substr(0, 1)
                                color = o.colorData[color_i]
                            }
                        }
                        attr.style = "background:" + color[0] + ";color:" + color[1] + ";"
                    }

                    if (o.color == 2) {//随机换色
                        color = o.colorData[Math.floor(Math.random() * o.colorData.length)]
                        attr.style = "background:" + color[0] + ";color:" + color[1] + ";"
                    }


                    if (this_.prop("tagName") == "DIV" && (typeof (o.data[i].fn) != "function" && typeof (o.click) != "function") && o.tagName != "a") {//鼠标形态
                        attr.style += "cursor:default;"
                    }


                    var E = $("<" + o.tagName + "/>", attr)
                    if (o.tagHtml) {
                        E.html(o.tagHtml.replace('{name}', o.data[i].name).replace('{id}', o.data[i].id))
                    } else {
                        E.html('<span>' + o.data[i].name + '</span>')
                    }

                    E.data("fn", o.data[i].fn)

                    E.click(function () {
                        if (this_.prop("tagName") == "DIV") {
                            if (typeof ($(this).data("fn")) == "function") {
                                $(this).data("fn")($(this))
                            }
                            if (typeof (o.click) == "function") {
                                o.click($(this))
                            }
                        }
                        if (this_.prop("tagName") == "INPUT") {
                            addtag($(this))
                        }
                    })
                    E.appendTo(target)
                }
            }

            for (var i in o.data_) { //附加标签 作为按钮来使用
                var attr = {}
                if (o.data_[i].color) {
                    attr.style = "background:" + o.data_[i].color[0] + ";color:" + o.data_[i].color[1];
                } else {
                    if (o.color == 1) {
                        color_i++
                        if (color_i == o.colorData.length) {
                            color_i = 0
                        }
                        color = o.colorData[color_i]
                        attr.style = "background:" + color[0] + ";color:" + color[1]
                    }
                    if (o.color == 2) {
                        color = o.colorData[Math.floor(Math.random() * o.colorData.length)]
                        attr.style = "background:" + color[0] + ";color:" + color[1]
                    }
                }
                var div = $("<div/>", attr)
                    .html(o.data_[i].name)
                    .data("fn", o.data_[i].fn)
                    .click(function () {
                        if (typeof ($(this).data("fn")) == "function") {
                            $(this).data("fn")($(this))
                        }
                    })
                if (o.data_[i].position) {
                    div.prependTo(target)
                } else {
                    div.appendTo(target)
                }
            }

            $(target).prepend(o.tagTXT)//添加标签文本
        }


        if ($(this).prop("tagName") == "DIV") {//如果作用于div标签。
            $(this)
                .attr("tag-list-id", id)
                .addClass(o.tagListCSS)
            var newarr = [];
            if (Array.isArray(o.defaultData)) {
                if (o.data.length > 0) {
                    for (var i in o.data) {
                        if (o.defaultData.indexOf(Number(o.data[i].id)) > -1) {
                            newarr.push(o.data[i])
                        }
                    }
                }
                o.data = newarr
            }
            list("[tag-list-id=" + id + "]")
        }

        if ($(this).prop("tagName") == "INPUT") { //如果作用域输入框

            $(this).hide();
            $(this).after(tagList)
            $(this).after(inputDiv)

            var nullHandle = function (isNull) {

                if (isNull) {
                    this_.next().addClass("hidden")
                } else {
                    this_.next().removeClass("hidden")
                }
            }
            var addtag = function (e) { //向标签输入框中添加标签
                var val = this_.val();//获取当前值
                nullHandle(false)
                if (val == "") { //将当前值处理为数组
                    val = [];
                } else {
                    val = val.split(",")
                }

                val = val.map(Number)

                if (val.indexOf(Number(e.attr("tagid"))) > -1) { //如果当前值已存在点击值
                    return false
                }

                val.push(e.attr("tagid")) //将点击值添加到当前值中

                this_.val(val.join(","))//修改当前值

                var cls = $("<a>x</a>").click(function () {
                    deltag($(this))
                })//删除标签

                var newtag = $('<div><span>' + e.text() + '</span></div>')
                    .attr("tagid", e.attr("tagid"))
                    .append(cls)//向标签输入框中添加的新标签。

                if (Array.isArray(o.defaultData)) {//如果默认值是数组
                    if (o.defaultData.indexOf(Number(e.attr("tagid"))) > -1) {//如果默认值中包含该值
                        newtag.attr("class", o.tagCSS[0]) //添加默认值样式
                    } else {
                        newtag.attr("class", o.tagCSS[1]) //添加新值样式
                    }
                } else {
                    newtag.attr("class", o.tagCSS[1]) //添加新值样式
                }

                newtag.appendTo("[tag-id=" + id + "]") //将新标签放入 标签输入框中。

            }

            var deltag = function (e) {
                var tag = e.parent()
                var val = this_.val();
                var re = new RegExp("([^\d]?)" + tag.attr("tagid") + "([^\d]?)", "g")
                val = val.replace(/[^\d,]/).replace(re, "$1$2").replace(/,,/, ",").replace(/^,/, "").replace(/,$/, "")
                this_.val(val)
                tag.remove()
                if (this_.val() === "") {
                    nullHandle(true)
                }
            }

            list("[tag-list-id=" + id + "]")

            if (this_.val()) { //处理默认值
                o.defaultData = this_.val().split(",")
                this_.val("")
            }

            if (Array.isArray(o.defaultData)) {//如果默认值是数组
                o.defaultData = o.defaultData.map(Number)
            }

            for (var i in o.defaultData) {
                $("[tag-list-id=" + id + "]>[tagid=" + o.defaultData[i] + "]").click()
            }

        }

    }

    $.fn.sTags.defaults = {
        data: [],//格式:{id:数字,name:文本,screen:筛选文本}
        data_: [],//附加标签（按钮）数据
        dataAttr: [],//因数据格式不同，这里填写三个分别对应要展现的 id name screen 属性的对应属性。如填写则必须填写3个
        //defaultData:[],//默认的数据，内容为data.id
        tagInputCSS: "sTags-input hidden",//输入框css
        tagListCSS: "sTags",//列表css
        tagCSS: ["sTags-old", "sTags-new"],//输入框内标签样式，第一个为默认标签样式，第二个g为新增标签样式
        color: 1,//标签列表颜色，0不使用，1按screen的首字母，按顺序循环数组内颜色，2随机颜色，数组内颜色，值0为背景色，1为字色
        colorData: [
            ["#90c5f0", "#fff"],
            ["#8E388E", "#fff"],
            ["#FFA500", "#fff"],
            ["#FBF", "#fff"],
            ["#DA70D6", "#fff"],
            ["#A2CD5A", "#fff"],
            ["#228B22", "#fff"],
            ["#CDC0B0", "#fff"],
            ["#CD7054", "#fff"],
            ["#00688B", "#fff"]
        ],//标签列表颜色，0不使用，1按screen的首字母，按顺序循环数组内颜色，2随机颜色，数组内颜色，值0为背景色，1为字色
        screen: true,//是否启用筛选功能
        screenInput: {
            type: "text",
            size: 8,
            placeholder: "搜索标签",
            class: "form-control",
            style: "margin-bottom: 6px;"
        },//筛选输入框属性,
        tagTXT: {},//标签列表前缀
        //click:function(e){console.log(e.attr("tagid"))},//当目标元素为div时，列表的点击事件。e为点击元素自身
        tagName: "",//标签列表使用的html标签，默认为div，如要改为div和a之外的其他标签则需要修改css
        tagHtml: "",//自定义标签列表中的html内容。{name} 替换为 tag.name {id}将转换为 tag.id,
        tagAttr: {}//标签属性
    };
})(jQuery);


// 刷新滑块宽度
$("#btn-addTask").click(function () {
    $(".sliderlens.horiz").css("width", "100%");
    $(".sliderlens.horiz").css("height", "34px");
    $(".handle .slider-img").css("height", "auto");
});

/*难度滑块*/
$(".slider-img").each(function (index, element) {
    $(element).rsSliderLens({
        handle: {
            zoom: 1.5,
            pos: 0.5
        },
        ruler: {
            visible: false
        },
        onChange: function (event, value, isFirstHandle) {
            $(event.currentTarget).parent("div").next("input[type='hidden']").val(value);
        },
        value: $(element).attr("slider-default")
    });
});
$(".handle .slider-img").each(function (index, element) {
    $(element).css("width", "1000%");
})

var labels = [];
for (var i = 0; i <= 3000;) {
    labels.push(i);
    if (i < 500) {
        i += 50;
    } else if (i < 1000) {
        i += 100;
    } else if (i < 2000) {
        i += 200;
    } else {
        i += 500;
    }
}
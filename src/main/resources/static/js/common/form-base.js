$(function () {
    /**/
    $(".slider-img").each(function (index, element) {
        $(element).rsSliderLens({
            handle: {
                zoom: 1.5, pos: 0.5
            }, ruler: {
                visible: false
            }, onChange: function (event, value, isFirstHandle) {
                $(event.currentTarget).parent("div").next("input[type='hidden']").val(value);
            }, value: $(element).attr("slider-default")
        });
    });
    /*将平移的图片放大*/
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
    $(".slider").rsSliderLens({
        step: 1, paddingStart: 0.008, paddingEnd: 0.008, max: 2000, height: 48, handle: {
            zoom: 1
        }, ruler: {
            labels: {
                values: labels
            }, tickMarks: {
                short: {
                    step: 10
                }, long: {
                    step: 50
                }
            }
        }, range: {
            type: [20, 2000], draggable: true
        }, value: 1000, onFinalChange: function (event, value, isFirstHandle) {
            $("#delay").val(value);
        }
    })
})
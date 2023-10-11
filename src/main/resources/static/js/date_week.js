//  获取当前年月日'
var curTime = new Date(),
    curYear = curTime.getFullYear(),
    curMonth = curTime.getMonth(),
    curDate = curTime.getDate()
var clickDate = []
//  赋值默认当月的显示
$('.tody').html(curDate)
$('.today_span').html(curYear + '-' + (curMonth * 1 + 1))

//  指定12个得数组
var MonthArry = [31, 28 + is_leap(curMonth), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31]

//   判断是否是闰年
function is_leap(year) {
    return year % 100 == 0 ? (res = year % 400 == 0 ? 1 : 0) : (res = year % 4 == 0 ? 1 : 0)
}

function showDate(year, month, today) {

    var thisTime = new Date(year, month, 1) //获取每个月的第一天
    var lasttime = new Date(year, month + 1, 0)

    var firstday = thisTime.getDay() //当月第一天星期几
    var tr_str = Math.ceil((MonthArry[month] + firstday) / 7) //表格所需要行数

    //TODO  尾部不全日期   传入的年份有问题 2023 11 月有问题。。。
    var preYear = month == 0 ? year * 1 - 1 : year
    var nextYear = month == 11 ? year * 1 + 1 : year
    var preMonth = month == 0 ? 11 : month - 1 //  后续会自动处理 month 根据下标
    var nextMonth = month == 11 ? 0 : month + 1
    var nowYear = null
    var nowMonth = null
    var dateArr = []
    for (i = 0; i < tr_str; i++) {
        for (k = 0; k < 7; k++) {
            var idx = i * 7 + k //表格单元的自然序号
            var date_str = idx - firstday + 1 //计算日期
            //console.log(date_str, 'date_str')
            if (date_str <= 0) {
                //  前一个月
                nowYear = preYear
                nowMonth = preMonth
                var x = Math.abs(date_str - 1)
                date_str = new Date(thisTime - x * 24 * 60 * 60 * 1000).getDate()
            } else if (date_str > MonthArry[month]) {
                nowYear = nextYear
                nowMonth = nextMonth
                date_str -= MonthArry[month]
            } else {
                //  当月的日期值
                nowYear = year
                nowMonth = month
                date_str = idx - firstday + 1
            }
            //console.log(curMonth, 'curYear----')
            dateArr.push({
                year: nowYear,
                month: nowMonth,
                tdody: date_str,
                noli: calendar.solar2lunar(nowYear, nowMonth + 1, date_str),
                shark: 0
            })
        }
    }
    // console.log(dateArr)
    rederDate(dateArr)
}

showDate(curYear, curMonth, curDate)
updateSignDateAjax(curYear, curMonth + 1)

//  显示日历
function rederDate(dateArr) {
    // 确定当前月
    var surMonth = dateArr[15].noli.cMonth

    var str = ''
    for (i = 0; i < dateArr.length; i++) {
        var curMounthFlog = dateArr[i].month + 1 == surMonth ? '' : 'nowNonth'
        var shark = dateArr[i].shark == 1 ? `<div class="dayshark">.</div>` : ''
        var tody = dateArr[i].tdody == curDate && dateArr[i].month == curMonth && dateArr[i].year == curYear ? 'today' : ''
        var nullDom = dateArr[i].tdody == ' ' ? 'nullDom' : '' //  判断空值
        var isTerm = 'term'
        var curday = null
        //  先计算是否 普通节气 --  24节气
        if (dateArr[i].noli.Term && dateArr[i].noli.isTerm) {
            curday = dateArr[i].noli.Term
        } else if (dateArr[i].noli.festival) {
            curday = dateArr[i].noli.festival
        } else if (dateArr[i].noli.lunarFestival) {
            curday = dateArr[i].noli.lunarFestival
        } else {
            curday = dateArr[i].noli.IDayCn
            isTerm = ' '
        }

        str += `   
     <div class="day_list  ${curMounthFlog}" date="${dateArr[i].noli.date}" noli="${dateArr[i].noli.IMonthCn}${dateArr[i].noli.IDayCn}"
    nolicons="${dateArr[i].noli.gzYear}年${dateArr[i].noli.gzMonth}月${dateArr[i].noli.gzDay}日"
    >
        <div class="day_center ${tody}  ${nullDom}">
          <div class="dayone">${dateArr[i].noli.cDay}</div>
          <div class="daytwo ${isTerm}" >${curday}</div>
         ${shark}
        </div>
      </div>`
    }
    // console.log(str)
    $('#day_item').html(str)
    $('.day_list').click(function () {
        $('.day_list').find('.day_center ').removeClass('today')
        $(this).find('.day_center ').addClass('today')
        var arr = $(this).attr('date').split('-')
        // var year = arr[0]
        // var month = arr[1]
        var today = $(this).find('.dayone').html()
        //  发送请求数渲染右边
        $('.tody').html(today)
        clickDate = $(this).attr('date').split('-')
    })
}

//重渲染日历
function reszeDate(year, month, tody) {
    //  接受传入的年月日
    is_leap(year) ? (MonthArry[1] = 29) : (MonthArry[1] = 28)

    showDate(year, month, tody)
}

//  点击左右切换
$('.icon-caret-left').click(function () {
    var year = $('.today_span').html().substring(0, 4)
    var month = $('.today_span').html().substring(5)
    var tody = $('.tody').html()
    month--
    if (month < 1) {
        month = 12
        // console.log(year)
        year -= 1
    }
    // console.log(year)
    $('.tody').html(curDate)
    $('.today_span').html(year + '-' + month)
    //重渲染日历
    reszeDate(year, month - 1, tody)
    updateSignDateAjax(year, month)
})
$('.icon-caret-right').click(function () {
    var year = $('.today_span').html().substring(0, 4)
    var month = $('.today_span').html().substring(5)
    var tody = $('.tody').html()
    month++
    if (month > 12) {
        month = 1
        // console.log(year)
        year = year * 1 + 1
    }
    // console.log(year)
    $('.tody').html(curDate)
    $('.today_span').html(year + '-' + month)
    //重渲染日历
    reszeDate(year, month - 1, tody)
    updateSignDateAjax(year, month)
})
//  点击今天
$('.current').click(function () {
    showDate(curYear, curMonth, curDate)
    //  赋值默认当月的显示
    $('.tody').html(curDate)
    $('.today_span').html(curYear + '-' + (curMonth * 1 + 1))
})
//console.log('-------------', curYear, curMonth, curDate)

//console.log(calendar.solar2lunar(2021, 12, 26))
function toTwo(date) {
    return date < 10 ? '0' + date : date
}


function updateSignDateAjax(year, month) {
    console.log(year + ", " + month)
    getAjax("/sign", {year: year, month: month}, function (data) {
        for(let date of data.data) {
            let year = parseInt(date.substring(0, 4));
            let month = parseInt(date.substring(5, 7));
            let day = parseInt(date.substring(8, 10));
            $("#day_item div[date='" + year + "-" + month + "-" + day + "']").addClass("day_signed");
        }
    })
}
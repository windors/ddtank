<!--处理卡片的时长显示-->
$('[data-toggle="tooltip"]').tooltip();
$(".song-length").each(function () {
    let len = parseInt($(this).html());
    let minutes = Math.trunc(len / 60);
    let seconds = len % 60;
    $(this).html(minutes + ":" + seconds)
})
// 需要先引入JQuery
// 当前页、最大页、最多显示多少页、最大到第几个就不往后走、回调用函数（将序号当参数传入）
function printPagination(current, maxPage, maxShowPages, maxShowStopPage, callback) {
    let begin, end;
    begin = current - maxShowStopPage + 1;
    begin = begin > 0 ? begin : 1;
    end = begin + maxShowPages - 1;
    if(end > maxPage){
        begin -= end - maxPage;
        end = maxPage;
    }
    begin = begin > 0 ? begin : 1;
    for(let i = begin; i <= end; i++) {
        callback(i);
    }
}
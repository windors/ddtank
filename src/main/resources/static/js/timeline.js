$(document).ready(function() {
    $timelineExpandableTitle = $('.timeline-action.is-expandable .title');

    $($timelineExpandableTitle).attr('tabindex', '0');

    // Give timelines ID's
    $('.timeline').each(function(i, $timeline) {
        var $timelineActions = $($timeline).find('.timeline-action.is-expandable');

        $($timelineActions).each(function(j, $timelineAction) {
            var $milestoneContent = $($timelineAction).find('.content');

            $($milestoneContent).attr('id', 'timeline-' + i + '-milestone-content-' + j).attr('role', 'region');
            $($milestoneContent).attr('aria-expanded', $($timelineAction).hasClass('expanded'));

            $($timelineAction).find('.title').attr('aria-controls', 'timeline-' + i + '-milestone-content-' + j);
        });
    });

    $($timelineExpandableTitle).click(function() {
        $(this).parent().toggleClass('is-expanded');
        $(this).siblings('.content').attr('aria-expanded', $(this).parent().hasClass('is-expanded'));
    });

    // Expand or navigate back and forth between sections
    $($timelineExpandableTitle).keyup(function(e) {
        if (e.which == 13){ //Enter key pressed
            $(this).click();
        } else if (e.which == 37 ||e.which == 38) { // Left or Up
            $(this).closest('.timeline-milestone').prev('.timeline-milestone').find('.timeline-action .title').focus();
        } else if (e.which == 39 ||e.which == 40) { // Right or Down
            $(this).closest('.timeline-milestone').next('.timeline-milestone').find('.timeline-action .title').focus();
        }
    });
});
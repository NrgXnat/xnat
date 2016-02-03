/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/tabWrangler/tabWrangler.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 9/5/13 2:38 PM
 */

// initialize a global var for the <body> element
var $body ;


$(function(){
    // dynamically add the css for the tabWrangler
    if (!$('link[href*="tabWrangler.css"]').length){
        $('head').append('<link rel="stylesheet" type="text/css" href="'+serverRoot+'/scripts/tabWrangler/tabWrangler.css">');
    }

    $body = $('body');
    var notLinks = 'a[href^="/##"], a[href^="##"], a[href^="!"], a[href^="*"]';

    $body.on('click',notLinks,function(e){
        e.preventDefault();
    });
});


function prevTabsWidth(_$tab,_n){
    var tab_number = _$tab.data('tab');
    var prev_tabs_width = 0 ;
    _$tab.prevAll('li').each(function(){
        //if ($(this).data('tab') <= tab_number){
            prev_tabs_width += $(this).outerWidth();
            prev_tabs_width += _n || 1 ;
        //}
    });
    return prev_tabs_width ;
}


//var tab_select_opts = [];

function renderAddTabSelect(_$wrapper,_val){

    var $select_add_tab = _$wrapper.find('.select_add_tab');

    if ($select_add_tab.length){

        _$wrapper.find('.flipper_box').css('width','310px');

        if (!_$wrapper.find('.flippers .selector').length){
            _$wrapper.find('.flippers').prepend('' +
                '<span class="selector" style="width:122px;border-right:none;">' +
                //'   <ul class="proxy" style="width:110px;margin:2px;padding:3px 0;font-size:11px;"></ul>' +
                '</span>' +
                '');
        }

        _$wrapper.find('.flippers .selector select').detach();

        _$wrapper.find('.flippers .selector').append($select_add_tab);

        // need to figure out how to disable <select> if there's no more data types to load into tabs
        if (_$wrapper.find('.flippers .selector select option').length === 0){
            _$wrapper.find('.flippers .selector select').prop('disabled',true).attr('disabled','disabled').addClass('disabled');
        }

    }
}


function renderFlippers(_$wrapper){
    //var _$wrapper = $(_wrapper) /* || $('.yui-navset') */ ;
    var $tabs_ul = _$wrapper.find('ul.yui-nav');
    var $tabs = $tabs_ul.find('li').not('.phantom');
    var $select_add_tab = _$wrapper.find('.select_add_tab');
    if ($tabs.length > 1 || $select_add_tab.length){
        var has_flippers = !!_$wrapper.find('.flipper_box').length;
        var $flipper_box ;
        var $content_wrapper = _$wrapper.find('.yui-content');
        if (has_flippers === false){
            $content_wrapper.after('<div class="flipper_box"></div>');
            $flipper_box = $content_wrapper.next('div.flipper_box');
            $flipper_box.html('' +
                '<span class="flippers">' +
                '<a href="##" class="flipper first"><b>&laquo;</b></a>' +
                '<a href="##" class="flipper left"><b>&lsaquo;</b> prev </a>' +
                '<a href="##" class="flipper right">next <b>&rsaquo;</b></a>' +
                '<a href="##" class="flipper last"><b>&raquo;</b></a>' +
                '</span>' +
                '');
        }
        // disable the left flippers if we're on the first tab
        if ($tabs_ul.find('li').first().attr('title') === 'active'){
            $('.flipper.first,.flipper.left').addClass('disabled');
        }
        else {
            $('.flipper.first,.flipper.left').removeClass('disabled');
        }
        // disable the right flippers if we're on the last tab
        if ($tabs_ul.find('li').not('.phantom').last().attr('title') === 'active'){
            $('.flipper.last,.flipper.right').addClass('disabled');
        }
        else {
            $('.flipper.last,.flipper.right').removeClass('disabled');
        }
    }
    // disable all flippers if there are NO tabs
    if (!$tabs.length){
        $('.flipper').addClass('disabled');
    }
    renderAddTabSelect(_$wrapper,'');
}


// show the content for the selected tab
// right now this just triggers a click
// to fire the current YUI event(s), but
// can be modified in the future to work
// without YUI
function showTabContent(_$wrapper,_n){
    _$wrapper.find('.yui-nav li[data-tab="'+_n+'"]').trigger('click');
}


function moveToTab(_$wrapper,_$tab,_n,_x){

    // the tab we're moving to
    //var $the_tab = _$wrapper.find('li[data-tab="' + _n + '"]');

    var $the_tab = _$tab ;

//    _n = _n || 0 ;
//    _x = _x || 0 ;

    // width of said tab
    var the_tab_width = $the_tab.outerWidth(true);

    // width of all tabs BEFORE this one
    var prev_tabs_width = prevTabsWidth($the_tab);

    var all_tabs_width = prev_tabs_width + the_tab_width;

    var content_width = _$wrapper.find('.yui-content').outerWidth(true);

    // if there's a 'selector' tab, get the width, otherwise set to 0
    //var selector_tab_width = ($the_tab.next('li').hasClass('selector')) ? parseInt(_$wrapper.find('li.selector').outerWidth()-20) : 40 ;
    //var selector_tab_width = (_$wrapper.find('.select_add_tab').length) ? 100 : 20 ;

    var flipper_box_width = (_$wrapper.find('.select_add_tab').length) ? 320 : 210 ;

    // how much visible space do we have to show the tabs? (need to ensure they are viewable inside this space)
    var width_limit = parseInt(content_width - flipper_box_width);

    var move_x ;
    if (parseInt(all_tabs_width) > parseInt(width_limit)){
        move_x = _x || parseInt(content_width - all_tabs_width - flipper_box_width);
    }
    else {
        move_x = 0;
    }

    _$wrapper.find('ul.yui-nav').animate({
        //left: '-' + parseInt(prev_tabs_width-width_limit-50)
        left: move_x
    },200);

}


function wrangleTabs(_wrapper,_force){  // initialize the wrangler

    var force = _force || false ;
    var tabs_wrapper = _wrapper || '.yui-navset' ;
    var tabs_ul = _wrapper + ' .yui-nav' ;
    var $tabs_wrapper = $(tabs_wrapper);
    var $tabs_ul = $(tabs_ul);

    if (!$tabs_ul.parent('div.wrangler').length){
        $tabs_ul.wrap('<div class="wrangler" style="width:100%;overflow:hidden;border-bottom:5px solid #084FAB"></div>');
    }

    $tabs_wrapper.addClass('wrangled');

    // is there a <select> element in the tabs? ('selector' class added in wrangleTabs() function)
    var $phantom_tab = $tabs_wrapper.find('.yui-nav li em:contains("+")').closest('li');

    $phantom_tab.addClass('phantom');

    // var the tab <li>s -- probably have to update this var if tabs are added/subtracted
    var tabs_ = tabs_ul + ' > li';

    // all tabs INCLUDING the selector tab (underscore at end)
    var $tabs_ = $(tabs_);

    // all tabs EXCEPT the selector tab (remove trailing underscore)
    var $tabs = $tabs_.not('.phantom');

    $tabs.removeClass('last');
    // var first and last tabs and add classes
    $tabs.first().addClass('first');
    $tabs.last().addClass('last');

    // var the <select>
    // jQuery gracefully handles this - won't freak out if there's no <select>
    var $select = $tabs_wrapper.find('.select_add_tab');

    // we're not gonna do anything if there's only one tab and no <select> element
    if ((force === true) || (($tabs.length > 1) || ($select.length))){

        // var to hold 'selected' tab number
        // tab/content #1 selected by default
        var n_selected = 1 ;
        // boolean if there's at least one tab that's 'active'
        var has_active = !!$(tabs_+'[title="active"]').length;

        // number the tab <li>s and set '.selected' and title="active"
        // also set 'selected' var to value of i for 'selected'/'active' tab
        $tabs.each(function(i){
            i++ ;
            var tab_width = $(this).outerWidth();
            $(this).attr('data-tab',i).attr('data-width',tab_width);
            // if there's already an 'active' tab (overrides '.selected' tab)
            if (has_active === true){
                // if THIS tab is the 'active' tab
                // if there's more than one that's 'active' (this should not happen),
                // then the last 'active' tab will be 'selected'
                if ($(this).is('[title="active"]')){
                    n_selected = i ;
                    $tabs.removeClass('selected');
                    $(this).addClass('selected');
                }
            }
            // else if there's no tab already flagged as 'active'
            // maybe it's '.selected'?
            else {
                if ($(this).hasClass('selected')){
                    n_selected = i ;
                    // probably don't need to remove the 'title' attr
                    // since we've already checked for that
                    // but it doesn't hurt to remove it anyway
                    $tabs.removeAttr('title');
                    $(this).attr('title','active');
                }
            }
        });

        // if nothing is selected by now... select the first tab
        if (!$(tabs_+'.selected').length){
            $tabs.first().addClass('selected whut').attr('title','active');
        }

        var $selected_tab = $tabs_ul.find('li[title="active"]');
        moveToTab($tabs_wrapper,$selected_tab);

        showTabContent($tabs_wrapper,n_selected);

    }

    renderFlippers($tabs_wrapper);

}


function clickWrangledTab(_$tab,_move){

    var $this_tab = _$tab || $(this);
    var $this_navset = _$tab.closest('.yui-navset');
    var $tabs_ = $this_navset.find('.yui-nav > li');
    var $tabs = $tabs_.not('.phantom');

    var n_selected = $this_tab.data('tab');
    $tabs.removeClass('selected').removeAttr('title');
    $this_tab.addClass('selected').attr('title','active');

    if (_move === true){
        moveToTab($this_navset,$this_tab);
    }
    else {
        //alert('move='+_move);
    }

    renderFlippers($this_navset);

}


$(function(){

    $body = $('body');

    $body.on('click','.wrangled .yui-nav li:not(.phantom)',function(){
        var move_ ;
        if ($(this).hasClass('dont_move')){
            move_ = false ;
            $(this).removeClass('dont_move');
        }
        else {
            move_ = true ;
        }
        clickWrangledTab($(this),move_);
    });


    // we need to be able to move to the previous tab when closing one
    // won't work for some reason
//    $body.on('click','.close',function(){
//        //showTabContent($(this).closest('.yui-navset'),$(this).closest('li').prev('li').data('tab'));
//        var $this_tab = $(this).closest('li');
//        $this_tab.prev('li').trigger('click');
//    });


    $body.on('click','a.flipper:not(.disabled)',function(){

        var
            $flipper = $(this),
            $flippers = $flipper.closest('.flippers'),
            $this_navset = $flippers.closest('.yui-navset'),
            $this_tab_ul = $this_navset.find('ul.yui-nav'),
            $these_tabs = $this_tab_ul.find('li:not(.phantom)'),
            $active_tab = $this_tab_ul.find('li[title="active"]') || $this_tab_ul.find('li.selected'), // check title="active" first
            $prev_tab = $active_tab.prev('li'),
            $next_tab = $active_tab.next('li'),
            $first_tab = $this_tab_ul.find('li.first'),
            //$last_tab = $this_tab_ul.find('li.last'),
            $last_tab = $this_tab_ul.find('li:not(.phantom)').last(),
            $this_content = $this_navset.find('.yui-content'),
            $selector_tab = $this_tab_ul.find('li.selector')
            ;

        var active_tab_number = $active_tab.data('tab');

        var
            navset_width = $this_navset.outerWidth(),
            content_width = $this_content.outerWidth(),
            prev_tabs_width = 0,
            tabs_count = 0,
            tabs_total_width = 0,
            active_tab_width = $active_tab.outerWidth(),
            //selector_tab_width = $selector_tab.outerWidth(),
            selector_tab_width = ($selector_tab.length) ? $selector_tab.outerWidth() : 0,
            width_limit = parseInt(content_width - 200 - selector_tab_width - 5),
            these_tabs_width = 0 //$active_tab.outerWidth()
            ;

        $these_tabs.each(function(){
            these_tabs_width += $(this).outerWidth();
        });

        // click 'left' flipper
        if ($flipper.hasClass('left')){
            $prev_tab.trigger('click');
        }
        // click 'right' flipper
        if ($flipper.hasClass('right')){
            $next_tab.trigger('click');
        }

        // click 'first' flipper
        if ($flipper.hasClass('first')){
            $first_tab.trigger('click');
        }

        // click 'last' flipper
        if ($flipper.hasClass('last')){
            $last_tab.trigger('click');
        }
    });

});

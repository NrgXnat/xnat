/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/xMenu/xMenu.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 9/5/13 2:38 PM
 */

/* *********************************
    xMenu Usage - see xMenu.html
   ********************************* */

// this requires jQuery

// set global var for $html and $body and define it after the DOM loads
var $html, $body, $xMenu ;
$(document).ready(function(){
    $html=$('html');
    $body=$('body');
    $html.addClass('xmenu');
    $body.addClass('xmenu');
    $xMenu=$('body.xmenu');
});

// pass in a jQuery selector
function xMenuCreate(selects){

    var xmenu_count = '' ;

    $xMenu.find(selects).each(function(){
        var $this_select = $(this);
        // don't do ANYTHING if the <select> has class="off"
        if (!($this_select.hasClass('off'))){
            //xmenu_count++ ;
            $this_select.hide();
            var select_id = $this_select.attr('id');
            var menu_id = select_id+'_xmenu'+xmenu_count ;  // xmenu_count there in case of future option to increment xMenus
            var $default_option ;
            var default_button_content ;
            var $this_menu = $this_select.next('div.xmenu');
            var $static_button = $this_menu.find('.button_content.static');

            if ($this_menu.length) {
                if ($static_button.length) {
                    $static_button.unwrap('div.xmenu_button');
                    $static_button.unwrap('div.xmenu');
                }
                else {
                    $this_menu.detach();
                }
            }

            // if there's no 'custom' button for the <select> put in a 'default' blank one
            if (!($this_select.next('.button_content').length)) {
                $this_select.after('<span class="button_content"></span>');
            }

            if ($this_select.next('.button_content').html() === ''){
                if ($this_select.find('option').length){
                    if ($this_select.find('option.default').length){
                        $default_option = $this_select.find('option.default');
                    }
                    else {
                        $default_option = $this_select.find('option:first');
                    }
                    //$default_option.prop('selected',true).attr('selected','selected').addClass('selected');
                    default_button_content = $default_option.text();
                }
                else {
                    default_button_content = 'Select' ;
                }
                $this_select.next('.button_content').html(default_button_content);
            }

            // wrap the xMenu elements around the "button_content"
            $this_select.next('.button_content').wrap('<div id="' + menu_id + '" class="xmenu"><div class="xmenu_button"></div></div>');
            $this_menu = $('#'+menu_id);

            //$this_select.next('.button_content').addClass('xmenu');
            if ($this_menu.find('ul.'+select_id).length){
                $this_menu.find('ul.'+select_id).html('');
            }
            else {
                $this_menu.append('<ul class="'+ select_id +' xmenu" style="display:none;"></ul>');
            }
            $this_menu
                .addClass($this_select.attr('class'))
                .attr('data-select-id',select_id)
                .attr('id',menu_id);
            //.append('<ul class="'+ select_id +' xmenu"></ul>');
            //$this_select.attr('id',menu_id);
            //$this_select.prev('label').attr('id',menu_id);
            $this_select.find('option').each(function(){
                var $option = $(this);
                var value = $option.attr('value');
                var content = $option.html();
                var this_class ;
                if ($option.attr('class') > ''){
                    this_class = $option.attr('class');
                }
                else {
                    this_class = '' ;
                }
                if ($option.hasClass('image')){
                    var img_src = $option.attr('data-img');
                    $this_select.next('div.xmenu').find('ul').append('' +
                        '<li class="'+this_class+'"><a href="javascript:" class="'+ this_class +'" data-value="'+ value +'" style="background-image:url('+img_src+');">'+content+'</a></li>' +
                        '');
                }
                else {
                    if (!$option.hasClass('button_default')){
                    $this_select.next('div.xmenu').find('ul').append('' +
                        '<li class="'+this_class+'"><a href="javascript:" class="'+ this_class +'" data-value="'+ value +'">' + content + '</a></li>' +
                        '');
                }
                }
            });
            //var $this_menu = $(this).next('div.xmenu');
            $this_menu.find('.button_content').show();
            var button_height = $this_menu.find('div.xmenu_button').height();
            //$this_menu.css({top:parseFloat(-(button_height/2)+0)});
            $this_select.addClass('ready');
        }
    });

}


// pass in a jQuery object
function xMenuSelect($this){
    var this_val = $this.attr('data-value');
    //alert (this_val);
    var $this_menu = $this.closest('div.xmenu');
    //var $this_select = $this_menu.prev('select.xmenu');
    var $this_ul = $this.closest('ul');
    var select_id = $this_menu.attr('data-select-id');
    var $this_select = $('#'+select_id);
    var $this_button = $this_menu.find('.button_content');
    //$this_button.not('.static').html($this.html());
    if (!$this_button.hasClass('static')) {
        $this_button.html($this.html());
        if ($this_select.find('option[value="' + this_val + '"]').attr('data-img') > '') {
            var this_bkgd = $this_select.find('option[value="' + this_val + '"]').attr('data-img');
            $this_button.addClass('image');
            $this_button.prepend('<img src="' + this_bkgd + '" alt="">');
        }
        else {
            $this_button.removeClass('image');
        }
    }
    $this_select.find('option').prop('selected',false).removeAttr('selected').removeClass('selected');
    $this_select.find('option[value="'+this_val+'"]').prop('selected',true).attr('selected','selected').addClass('selected');
    $this_select.change();
    $this_ul.find('li').removeClass('selected');
    $this.closest('li').addClass('selected');
    $this_ul.find('a').removeClass('selected');
    $this.addClass('selected');
    $this_ul.slideUp(50);
    $this_ul.removeClass('open');
    $this.closest('div.xmenu').removeClass('open');
}


$(document).ready(function(){

    // let the page know we're gonna use xMenu for menus
//    $html.addClass('xmenu');
//    $body.addClass('xmenu');
//    $xMenu = $('body.xmenu');

    // add the required CSS file to <head>
    if (!($('link[href*="xMenu.css"]').length)){
        $('head').append('<link type="text/css" rel="stylesheet" href="'+ serverRoot + '/scripts/xMenu/xMenu.css">');
    }
//    else {
//        alert('xMenu CSS already loaded');
//    }

    var xmenu_z = 9999 ;
    var xmenu_ul_z ;

    // selector for target selects - #id or .class
    //xMenuCreate('select.xmenu');

    $xMenu.on('click','div.xmenu_button',function(){

        if (!($(this).closest('div.xmenu').hasClass('disabled'))){

            var $the_menus = $('div.xmenu');
            var $this_menu = $(this).closest('div.xmenu');
            var $other_menus = $the_menus.not($this_menu);

            $other_menus.removeClass('open');
            $other_menus.find('ul').hide().removeClass('open');

            if ($this_menu.hasClass('open')){
                $this_menu.removeClass('open');
                $this_menu.find('ul').slideUp(50).removeClass('open');
            }
            else {
                $this_menu.addClass('open');
                $this_menu.find('ul').slideDown(50).addClass('open');
                xmenu_z = xmenu_z+2 ;
                xmenu_ul_z = xmenu_z+1;
            }
            $this_menu.css({zIndex:xmenu_z});
            $this_menu.find('ul').css({zIndex:xmenu_ul_z});
        }

    });


    // click outside of menu closes any that are open
    $xMenu.on('click', function(e) {
        if ($(e.target).closest('div.xmenu').length === 0) {
            $('div.xmenu').removeClass('open');
            $('div.xmenu ul').slideUp(50).removeClass('open');
        }
    });



    $xMenu.on('click','div.xmenu ul.open a',function(){
        var select_id = $(this).closest('div.xmenu').attr('data-select-id');
        //alert(select_id);
        if (!($('#'+select_id).hasClass('custom_action'))){
            xMenuSelect($(this));
        }
    });

});

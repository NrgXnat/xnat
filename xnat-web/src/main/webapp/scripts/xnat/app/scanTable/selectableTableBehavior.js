/* selectable table checkbox behavior */
console.log('selectableTableBehavior.js');

$(document).on('mousedown','.selectable-select-all',function(){
    var selectAllToggle = $(this);
    if (selectAllToggle.prop('indeterminate')) {
        selectAllToggle.data('indeterminate','true');
    }
});

$(document).on('click','.selectable-select-all',function(){
    var checkProp = false;
    var selectAllToggle = $(this);
    var containingTable = selectAllToggle.parents('.data-table-container');

    // check all checkboxes in the table body to find any unchecked. Default behavior will be to check all.
    containingTable.find('tbody').find('.selectable-select-one').each(function(){
        if (!$(this).is(':checked')) {
            checkProp = 'checked';
            return false;
        }
    });

    // now iterate over all checkboxes again, performing the default operation
    containingTable.find('tbody').find('.selectable-select-one').each(function(){
        $(this).prop('checked',checkProp);
    });

    if (checkProp === 'checked') {
        containingTable.find('.data-table-action').removeClass('disabled');
    } else {
        containingTable.find('.data-table-action').addClass('disabled');
    }

    // do a final check on the select-all selector to ensure it is set to true when clicked from an intedeterminate state
    if (selectAllToggle.data('indeterminate') === 'true') {
        selectAllToggle.data('indeterminate','false');
        if (selectAllToggle.prop('checked') === false) selectAllToggle.prop('checked','checked');
    }
});

// set toggle-all selector state based on internal checkbox statuses
$(document).on('click','.selectable-select-one',function(){
    var allChecked = true,
        allUnchecked = true,
        containingTable = $(this).parents('.data-table-container'),
        selectAll = containingTable.find('.selectable-select-all');

    // check for unchecked boxes. If any found, allChecked = false
    containingTable.find('tbody').find('.selectable-select-one').each(function(){
        if (!$(this).is(':checked')) {
            allChecked = false;
            return false;
        }
    });

    // check for checked boxes. If any found, unchecked = false
    containingTable.find('tbody').find('.selectable-select-one').each(function(){
        if ($(this).is(':checked')) {
            allUnchecked = false;
            return false;
        }
    });

    var indeterminate = !(allChecked || allUnchecked); // indeterminate is only true if all checkboxes are neither checked or unchecked

    selectAll.prop('checked',allChecked).prop('indeterminate',indeterminate);

    if (allUnchecked) {
        containingTable.find('.data-table-action').addClass('disabled');
    } else {
        containingTable.find('.data-table-action').removeClass('disabled');
    }
});

// use the inline menu toggle as the trigger for opening the menu.
// hide the menu after a short time if the user does not mouse into the menu.
$(document).on('mouseenter','.inline-actions-menu-container',function(){
    var container = $(this),
        menu = $(this).find('.inline-actions-menu');

    container.addClass('active');
    menu.show();

    // if the user hovers over the container icon, but does not mouse into the popup menu, fade out the menu.
    setTimeout(function(){
        if (!menu.hasClass('active')) {
            menu.fadeOut(150);
            container.removeClass('active');
        }
    },1500);
});

$(document).on('mouseenter','.inline-actions-menu',function(){
    $(this).addClass('active');
});
$(document).on('mouseleave','.inline-actions-menu',function(){
    $(this).hide().removeClass('active');
});

$(document).on('mouseleave','.inline-actions-menu-container',function(event){
    var container = $(this),
        menu = $(this).find('.inline-actions-menu');

    container.removeClass('active');
    menu.hide();
});
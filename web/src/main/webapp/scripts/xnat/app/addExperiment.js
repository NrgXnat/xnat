/*!
 * "Add Experiment" page
 */

var XNAT = getObject(XNAT);

(function(factory){
    if (typeof define === 'function' && define.amd) {
        define(factory);
    }
    else if (typeof exports === 'object') {
        module.exports = factory();
    }
    else {
        return factory();
    }
}(function(){

    var undef, addExperiment;

    XNAT.app =
        getObject(XNAT.app || {});

    XNAT.app.addExperiment = addExperiment =
        getObject(XNAT.app.addExperiment || {});


    // init function for XNAT.misc.blank
    addExperiment.init = function(container){

        debugLog('addExperiment');

        XNAT.app.dataTypeAccess.getElements['browseableCreateable'].ready(function(data){

            var exptListContainer = $$(container || '#expt-types');
            var exptListItems = [];

            var sortedElements = sortObjects(data.elements, 'singular');

            forEach(sortedElements, function(element){
                if (element.subjectAssessor) {
                    var rowContents = [];
                    var exptTypeRow = ['div.expt-type.row.match', {
                        title: element.singular,
                        data: { type: element.elementName }
                    }, rowContents];
                    rowContents.push(['h4', [
                        ['a.data_type', {
                            id: 'dt_' + element.elementName,
                            title: element.elementName
                        }, element.singular]
                    ]]);
                    rowContents.push(['p', element.xsDescription || element.singular]);
                    exptListItems.push(spawn.apply(null, exptTypeRow));
                }
            });
            // exptListContainer.find('div.rows').append(exptListItems);
            exptListContainer.empty().append(exptListItems);

            var exptListItems$ = $(exptListItems);
            var exptListFilter = $('#expt_list_filter').find('input');

            function resetFocusFilter() {
                exptListFilter.val('').focus();
                exptListItems$.addClass('match');
            }

            $('#filter_clear').on('click', resetFocusFilter);

            exptListFilter.on('focus', function(){
                $(this).select();
            });

            exptListFilter.on('keyup', function(e){

                if (e.keyCode === 27) {  // key 27 = 'esc'
                    resetFocusFilter();
                    return;
                }

                var filterValue = this.value;

                if (!filterValue) {
                    exptListItems$.addClass('match');
                    return;
                }

                exptListItems$.removeClass('match').filter(function(){
                    return this.textContent.toLowerCase().indexOf(filterValue.toLowerCase()) > -1;
                }).addClass('match');

            });

            exptListContainer.on('click', '.row', function(){

                var data_type_val = $(this).find('.data_type').attr('title');

                $('#data_type').val(data_type_val);

                if ($('#project').val() > '') {
                    if ($('#part_id').val() > '') {
                        $('#form1').submit();
                    }
                    else {
                        XNAT.dialog.message('Add Experiment Validation', '<p>Please select a ' + XNAT.app.displayNames.singular.subject.toLowerCase() + '.</p>');
                    }
                }
                else {
                    XNAT.dialog.message('Add Experiment Validation', '<p>Please select a ' + XNAT.app.displayNames.singular.project.toLowerCase() + '.</p>');
                }
            })

        });

    };

    return XNAT.app.addExperiment = addExperiment;

}));

/**
 * New "universal" project-subject selector.
 * Checks against user's permissions to determine
 * which experiment types can be created.
 */

// doesn't hurt to check for this, right?
if (typeof XNAT == 'undefined') { var XNAT={} }
if (typeof XNAT.app == 'undefined') { XNAT.app={} }

XNAT.app.projectSubjectSelector = function( proj_menu, subj_menu, submit_button, selected_project, selected_subject ) {

    var app = this;

    var $proj_menu, $subj_menu, $submit, $expt_list,
        projectsArray=[],
        subjectsArray=[],
        fn = {},
        $body = $body || $(document.body);


    // this will make sure we've got a jQuery DOM object
//    function jqObj(el){
//        if (!el) { return false }
//        var $el = el;
//        if (!$el.jquery){
//            $el = $(el);
//            // if there's not a matching DOM element
//            // then it's PROBABLY just an id string
//            if (!$el.length){
//                $el = $('#'+el);
//            }
//        }
//        return $el;
//    }


    function getTimestampParam(){
        return 'timestamp=' + (new Date()).getTime();
    }


    // options for Chosen menus
    var chosenOpts = {
        allow_single_deselect: true,
        disable_search_threshold: 8, // hide search box for 8 or less <option>s
        inherit_select_classes: true,
        width: '300px'
    };


    // cache the DOM elements
    $proj_menu = jqObj(proj_menu);
    $subj_menu = jqObj(subj_menu);
    $submit    = jqObj(submit_button);





    //////////////////////////////////////////////////
    // PROJECTS MENU START
    //
    var projParams = [
        //'owner=true',
        //'member=true',
        'creatableTypes=true',
        'format=json'
    ];
    //
    fn.renderProjectsMenu = function(url){

        if (window.csrfToken) { projParams.push('XNAT_CSRF=' + window.csrfToken) }

        var projects_url = function(){
            // do we need timestamps for these requests? does it (help) prevent caching?
            projParams.push(getTimestampParam());

            // if going directly to a new experiment page
            // from the top menu bar, only show projects
            // the current user is allowed to create this
            // kind of experiment
            if (XNAT.data.context.xsiType > ''){
                projParams.push('data-type=' + XNAT.data.context.xsiType);
            }

            return serverRoot + '/data/projects?' + projParams.join('&');
        };

        var projectsOptions = [];

        // REST call to get the projects list JSON
        var getProjects = $.get( url || projects_url() );
        //
        getProjects.done(function(json){

            var results = json.ResultSet.Result;
            var len = results.length;
            var html = '';
            var titleProp = '';

            if (len === 0){
                $proj_menu.
                    html('<option selected disabled>(no projects)</option>').
                    prop('disabled',true);
            }
            else {

                // trying to handle both lowercase and uppercase 'ID'
                if (results[0].hasOwnProperty('secondary_id')) { titleProp = 'secondary_id' }
                if (results[0].hasOwnProperty('secondary_ID')) { titleProp = 'secondary_ID' }

                results = sortObjects(results, titleProp);

                for (var i=0, id, title, option; i < len; i++){
                    id = results[i]['ID'] || results[i]['id'];
                    title = results[i][titleProp];
                    projectsArray.push(id);
                    option = '<option' +
                        ' value="' + id + '"' +
                        ' title="' + title + '"' +
                        //' data-uri="' + results[i]['URI'] + '"' +
                        '>' + title + '</option>';
                    //
                    projectsOptions.push(option);
                }
                html += '' +
//                    '<option></option>' + // this will be needed if/when using Chosen menus
                    '<option selected disabled>Select a Project</option>' +
//                    '<option disabled style="white-space:nowrap">--------------------------------------------------------------------------------</option>' +
                    '';
                html += projectsOptions.join('');
                $proj_menu.
                    html(html).
                    removeAttr('disabled').
                    prop('disabled',false);

                chosenInit($proj_menu, null, 300);

            }

            chosenUpdate($proj_menu);
            //$proj_menu.trigger('chosen:updated');

        });

        getProjects.fail(function(jqXHR, textStatus, errorThrown){
            xModalMessage('Error', "ERROR " + textStatus + ": Failed to load " + XNAT.app.displayNames.singular.project.toLowerCase() + " list.");
        });
    
    };
    //
    // END PROJECTS MENU
    //////////////////////////////////////////////////





    //////////////////////////////////////////////////
    // SUBJECTS MENU START
    //
    var subjParams = [
        //'owner=true',
        //'member=true',
        //'creatableTypes=true',
        'format=json',
        'XNAT_CSRF=' + window.csrfToken
    ];
    //
    fn.renderSubjectsMenu = function(url){

        var subjects_url = function(){
            // do we need timestamps for these requests? does it (help) prevent caching?
            subjParams.push(getTimestampParam());
            return serverRoot + '/data/projects/' + selected_project + '/subjects?' + subjParams.join('&');
        };

        var subjectsOptions = [];

        // REST call to get the subjects list JSON
        var getSubjects = $.get( url || subjects_url() );

        getSubjects.done(function(json){

            var results = json.ResultSet.Result;
            var len = results.length;
            var html ='';
            if (len === 0){
                $subj_menu.
                    html('<option selected disabled>(no subjects)</option>').
                    prop('disabled',true);
            }
            else {
                results = sortObjects(results, 'label');
                for (var i=0, id, label, option; i < len; i++){
                    id = results[i]['ID'] || results[i]['id'];
                    label = results[i]['label'];
                    subjectsArray.push(id);
                    option = '<option' +
                        ' value="' + id + '"' +
                        ' title="' + label + '"' +
                        //' data-uri="' + results[i]['URI'] + '"' +
                        '>' + label + '</option>';
                    //
                    subjectsOptions.push(option);
                }
                html += '' +
//                    '<option></option>' + // this will be needed if/when using Chosen menus
                    '<option selected disabled>Select a Subject</option>' +
//                    '<option disabled style="white-space:nowrap">--------------------------------------------------------------------------------</option>' +
                    '';
                html += subjectsOptions.join('');
                $subj_menu.
                    html(html).
                    removeAttr('disabled').
                    prop('disabled', false);

                chosenInit($subj_menu, null, 300);
            }

            chosenUpdate($subj_menu);
            //$subj_menu.trigger('chosen:updated');

        });

        getSubjects.fail(function(jqXHR, textStatus, errorThrown){
            xModalMessage('Error', "ERROR " + textStatus + ": Failed to load " + XNAT.app.displayNames.singular.subject.toLowerCase() + " list.");
        });

    };
    //
    // END SUBJECTS MENU
    //////////////////////////////////////////////////




    //////////////////////////////////////////////////
    // FILTER EXPERIMENT LIST FOR PROJECT
    //
    fn.filterExptList = function(url){

        var expts_url =
            serverRoot + '/data/projects/' + selected_project + '?creatableTypes=true&format=json';

        url = url || expts_url;

        // REST call to get the subjects list JSON
        var getExptTypes = $.get(url);

        getExptTypes.done(function(json){

            // example of what the returned JSON looks like
            var sample = {
                "ResultSet": {
                    "Result": [
                        { "element_name": "xnat:mrSessionData" },
                        { "element_name": "xnat:subjectData" }
                    ]
                }
            };

            var results = json.ResultSet.Result;
            var len = results.length;

            var exptTypes = [];
            app.creatableTypes = [];

            for (var i=0, el_name; i < len; i++){
                el_name = results[i]['element_name'];
                app.creatableTypes.push(el_name);
                exptTypes.push('[data-type="' + el_name + '"]');
            }

            // copy to XNAT.data object
            XNAT.data.creatableTypes = app.creatableTypes;

            // Exit if we're not on the "Add Experiment" page
            if (window.page !== 'add_experiment') { return }

            $expt_list = $('#expt_list');

            $expt_list.find('.expt-type').removeClass('match').hide();
            $expt_list.find(exptTypes.join(', ')).addClass('match').show();

        });

        getExptTypes.fail(function(jqXHR, textStatus, errorThrown){
            xModalMessage('Error', "ERROR " + textStatus + ": Failed to load experiment list.");
        });

    };
    //
    // END FILTER FOR PROJECT
    //////////////////////////////////////////////////




    //////////////////////////////////////////////////
    // MAIN INIT FUNCTION
    //
    fn.init = function() {

        if (!selected_project){
            //$proj_menu.chosen(chosenOpts);
            //$subj_menu.chosen(chosenOpts);
            fn.renderProjectsMenu();
//            $expt_list.find('.expt-type').addClass('show');
        }
        else if (selected_project && !selected_subject){
            //$subj_menu.chosen(chosenOpts);
            fn.renderSubjectsMenu();
            fn.filterExptList();
        }
        else {
            fn.filterExptList();
        }

    };
    //
    // END INIT FUNCTION
    //////////////////////////////////////////////////
    fn.init();




    //////////////////////////////////////////////////
    // SELECT A PROJECT, (RE-)RENDER THE SUBJECTS
    //
    fn.onProjectChange = function($menu){
        $menu = $menu || $proj_menu;
        selected_project = $menu.val();
        fn.renderSubjectsMenu();
        fn.filterExptList();
    };
    //
    // END PROJECT SELECTION
    //////////////////////////////////////////////////


    $proj_menu.change(function(e){
        //e.preventDefault();
        fn.onProjectChange($(this));
    });


};

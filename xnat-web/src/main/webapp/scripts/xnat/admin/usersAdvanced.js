/*!
 * Functions for "Advanced" user settings
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

    var undef, usersAdvanced;

    XNAT.admin = getObject(XNAT.admin || {});

    XNAT.admin.usersAdvanced = usersAdvanced =
        getObject(XNAT.admin.usersAdvanced || {});


    //////////////////////////////////////////////////


    function updateUserRoles(form$){

        var userform$ = $$(form$ || '#userform');
        var accessInputs$ = userform$.find('input.access');

        var username = userform$.find('[name="xdat:user.login"]').val();

        function roleUrl(part){
            return XNAT.url.rootUrl('/xapi/users/' + username + part);
        }

        // collect request info
        var requests = [];

        // ALWAYS delete ALL_DATA_ACCESS and ALL_DATA_ADMIN
        // so they can be set to the ONE proper value
        requests.push({
            method: 'DELETE',
            url: roleUrl('/groups/ALL_DATA_ACCESS')
        });
        requests.push({
            method: 'DELETE',
            url: roleUrl('/groups/ALL_DATA_ADMIN')
        });

        accessInputs$.each(function(){

            var NAME = this.name;
            var VALUE = this.value;
            var CHECKED = this.checked;

            if (/custom_role/i.test(NAME)) {
                requests.unshift({
                    method: CHECKED ? 'PUT' : 'DELETE',
                    url: roleUrl('/roles/' + VALUE)
                });
            }
            else if (/xdat:user\.groups/i.test(NAME)) {
                if (CHECKED && VALUE !== 'NULL') {
                    requests.push({
                        method: 'PUT',
                        url: roleUrl('/groups/' + VALUE)
                    });
                }
            }

        });

        function doUpdate(i){

            // after the last request returns...
            if (i === (requests.length)) {
                window.setTimeout(function(){
                    // ...submit the form
                    userform$.submit();
                }, 500);
                return;
            }

            var req = requests[i];
            // req.async = false;
            req.success = function(){
                // doUpdate(++i)
            };
            req.error = function(){
                console.error(arguments);
            };
            req.always = function(){
                // keep going even if there's an error
                doUpdate(++i)
            };

            debugLog(req);

            XNAT.xhr.request(req);
        }

        doUpdate(0);

    }
    usersAdvanced.updateUserRoles = updateUserRoles;

    $(document).ready(function(){
        var userform$ = $('#userform');
        usersAdvanced.form$ = userform$;
        $('#update-user-roles').on('click', function(e){
            e.preventDefault();
            xmodal.loading.open();
            updateUserRoles(userform$)
        });
    });


    //////////////////////////////////////////////////


    // this script has loaded
    usersAdvanced.loaded = true;

    return (XNAT.admin.usersAdvanced = usersAdvanced);

}));

var XNAT = getObject(XNAT);

console.log("quickSearchDatatypeInputs.js");

(function (factory) {
  if (typeof define === 'function' && define.amd) {
    define(factory);
  }
  else if (typeof exports === 'object') {
    module.exports = factory();
  }
  else {
    return factory();
  }
}(function () {
    var baseDiv = document.getElementById("quick-search-datatypes");

    var url = "/xapi/schemas/datatypes/searchable"
    var rootUrl = XNAT.url.rootUrl;

    function getCurrentSettings() {
        return new Promise (function(resolve, reject) {
            var initial_values = [];

            XNAT.xhr.getJSON({
                url: rootUrl("xapi/siteConfig/mainPageSearchDatatypeOptions"),
                success: function(returnData) {
                    resolve(returnData)
                },
                fail: function() {
                    reject(new Error("Couldn't load initial values"));
                }
            });
        })
    }


    XNAT.xhr.getJSON({
        url: rootUrl(url),
        success: function (data) {
            var multi_search_base = "<select multiple size='6' class='xnat-menu project-multi-select-menu' data-menu-opts='width:280px' style='min-width:600px;' id='datatype-options-multi-select'></select>";

            $("#quick-search-datatypes").prepend(multi_search_base);

            for (let i = 0; i < data.length; i++) {
                $('#datatype-options-multi-select').append($('<option>', {
                    value: data[i],
                    text: data[i]
                }));
            }

            let values = getCurrentSettings();

            values.then(
                function(result) {
                    $.each(result, function(i,e){
                        $("#datatype-options-multi-select option[value='" + e + "']").prop("selected", true);
                    });
                    menuInit('select.project-multi-select-menu');
                },
                function(error) {
                    menuInit('select.project-multi-select-menu');
            });

            $(document).ready(function() {
                $("#update-quick-search").on("click",function(){
                    XNAT.xhr.postJSON({
                        url: rootUrl("xapi/siteConfig/mainPageSearchDatatypeOptions"),
                        data: JSON.stringify($("#datatype-options-multi-select").chosen().val()),
                        success: function() {
                            XNAT.ui.banner.top(2000, 'Front page quick search options updated.', 'success');
                        },
                        fail: function() {
                            XNAT.ui.banner.top(3000, 'An error occurred when attempting to update the quick search options.', 'error');
                        }
                    });
                });
            });

        },
        fail: function (e) {
            $("#quick-search-datatypes").append("<p>An error occurred when retrieving the quick search options.</p>")
        }
    });
}));
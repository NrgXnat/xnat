/*
 * web: seriesImportCleanup.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

var XNAT = getObject(XNAT);

$(function(){
    console.log('seriesImportCleanup.js');

    var sifCleanup;

    XNAT.admin =
        getObject(XNAT.admin||{});

    XNAT.admin.sifCleanup = sifCleanup =
        getObject(XNAT.admin.sifCleanup||{});

    sifCleanup.projectIdList = [];

    async function getProjectIdList(){
        XNAT.xhr.getJSON({
            url: XNAT.url.restUrl('/data/projects?format=json'),
            success: function(data){
                sifCleanup.projectIdList = data.ResultSet.Result;
                sifCleanup.projectIdList.forEach(function(item,i){ sifCleanup.projectIdList[i] = item.ID });
            }
        });
    }

    function sifCleanupInput(){
        return {
            tag: 'textarea.disabled.sifProjectIdList',
            element: {
                disabled: 'disabled',
                style: {
                    background: '#f0f0f0',
                    'font-family': 'Courier, monospace',
                    width: '100%'
                },
                type: 'text',
                value: XNAT.data.siteConfig.enableProjectsSeriesImportFilter
            }
        }
    }

    function sifCleanupButton(){
        return {
            tag: 'button.btn.btn-sm.sifProjectCleanup',
            element: {
                on: {
                    'click': function(){
                         const sifInput = $(document).find('textarea.sifProjectIdList')[0];
                         XNAT.admin.sifCleanup.performCleanup(sifInput);
                    }
                },
                disabled: (XNAT.data.siteConfig.enableProjectsSeriesImportFilter.length) ? false : 'disabled'
            },
            content: 'Clean Up Deleted Projects'
        }
    }

    sifCleanup.performCleanup = function(element){
        const sifList = XNAT.data.siteConfig.enableProjectsSeriesImportFilter.split(',');
        let cleanedSifList = [], itemsToRemove = [];
        sifList.forEach(function(item){
            // prune project IDs that do not show up in the list of known project IDs
            if (sifCleanup.projectIdList.includes(item)) {
                cleanedSifList.push(item);
            } else {
                itemsToRemove.push(item);
            }
        });
        if (itemsToRemove.length) {
            XNAT.ui.dialog.confirm({
                title: 'Project IDs to Remove',
                width: 400,
                content: '<p>The following project IDs do not match any active projects in your system and can be removed from this system warning dialog: </p><p>' + itemsToRemove.join(', ')+ '</p>',
                okAction: function(){
                    XNAT.xhr.postJSON({
                        url: XNAT.url.csrfUrl('/xapi/siteConfig/enableProjectsSeriesImportFilter'),
                        data: cleanedSifList.join(','),
                        success: function(){
                            element.value = cleanedSifList.join(',');
                            XNAT.ui.banner.top(3000,'Removed deleted project IDs from Series Import Filter warning list','success');
                        },
                        fail: function(e){
                            console.error(e);
                        }
                    })
                }
            });
        } else {
            XNAT.ui.dialog.message('No Action Taken','There are no invalid Project IDs to remove from this list.');
        }
    }

    sifCleanup.init = async function(){
        await getProjectIdList();
        const sifCleanupPanel = $(document).find('#sifCleanup');
        sifCleanupPanel.empty();

        // check for project warnings in the sitewide config
        if (XNAT.data.siteConfig.enableProjectsSeriesImportFilter && XNAT.data.siteConfig.enableProjectsSeriesImportFilter.length) {
            XNAT.spawner.spawn({
                sifCleanupExplanation: {
                    tag: 'div.warning',
                    element: { style: { 'margin': '2em 0' }},
                    content: 'Your system has one or more projects identified as running a Series Import Filter. If this list of projects contains deleted Project IDs, use this cleanup function to remove those IDs from the warning list.'
                },
                cleanupPanel: {
                    kind: 'panel.element',
                    label: 'Project List Cleanup',
                    contents: {
                        sifCleanupListP: {
                            tag: 'span',
                            element: { style: { display: 'block' }},
                            contents: { sifCleanupListPInput: sifCleanupInput() }
                        },
                        sifCleanupButton: {
                            tag: 'p',
                            contents: { sifCleanupListButton: sifCleanupButton() }
                        }
                    }
                }
            }).render(sifCleanupPanel);
        }
    }

    sifCleanup.init();
});
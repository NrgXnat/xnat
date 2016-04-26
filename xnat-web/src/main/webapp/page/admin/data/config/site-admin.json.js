XNAT.siteAdmin =
    // array of tab config objects
[
    {
        "name": "meta",
        "kind": "meta",
        "meta": {
            "url": '/data/admin'
        }
    },
    {
        "kind":  "tab",
        "name":  "siteInfo", // REQUIRED - 'name' that matches database name
        "id":    "site-info", // OPTIONAL - hyphenated id; defaults to hyphenated version of "name"
        "label": "Site Information", // REQUIRED - text for tab flipper
        "url":   "/data/admin/site-info", // REQUIRED to send data back to XNAT - saves ALL data for this entire tab
        "contents": [ // array of panels or UI elements for this tab pane
            {
                "kind": "panel", // what kind of UI element/widget is this?
                "name": "siteInfoId", // OPTIONAL
                "id": "site-info-id", // OPTIONAL
                "label": "Site ID", // REQUIRED
                "description": "The id used to refer to this site (also used to generate database ids). No spaces or non-alphanumeric characters. It should be a short, one-word name or acronym which describes your site.",
                "elements": [ // array of form elements or other content for this panel
                    {
                        // "input-alphanum-id" will only accept values that would be valid for an ID (starts with letter and contains only letters, numbers, underscores, and hyphens)
                        "kind": "input-alphanum-id", // choose from a predefined widget type - other examples could be "input-alphanum-safe", "input-alpha", "input-number-int", "input-email"
                        "name": "siteId", // REQUIRED for form elements
                        "label": "Site ID", // REQUIRED
                        "id": "site-id", // OPTIONAL
                        "description": "Site ID accepts letters, numbers, and underscores",
                        "value": "XNAT", // saved or default value for form elements
                        "required": true
                    }
                ]
            },
            {
                "kind": "panel", // what kind of UI element/widget is this?
                "name": "siteDescription",
                "id": "site-info-description", // OPTIONAL
                "label": "Site Description", // REQUIRED
                "description": "A short description of this site to be displayed on the login page.",
                "elements": [ // array of form elements or other content for this panel

                    // SITE DESCRIPTION TYPE (TEXT OR PAGE)
                    {
                        // 'kind' property will tell the front-end code which kind of
                        // HTML element to render, as well as any specific attributes,
                        // properties, or methods needed for that specific 'kind'
                        "kind": "select", // more examples: "select-multi", "radio", "checkbox"
                        "name": "siteDescriptionType", // REQUIRED for form elements
                        "label": "Site Description Type", // REQUIRED
                        "id": "site-description-type",
                        "description": "select 'Text' or 'Page'",
                        "value": "text",
                        "required": true, // only set for required elements
                        "options": [ // options for select menu
                            {
                                "label": "Text",
                                "value": "text"
                            },
                            {
                                "label": "Page",
                                "value": "page"
                            }
                        ],
                        // brainstorming how to specify dependencies between elements...
                        // it's probably useful to be able to define these on the back-end
                        // to minimize the number of places where things need to be specified
                        // (this is definitely NOT final)
                        "on": {
                            // do something as soon as the element is rendered
                            "render": "XNAT.admin.siteDescriptionType.change",
                            "change": "XNAT.admin.siteDescriptionType.change"
                            // pass the namespaced function name to be called
                        }
                    },

                    // SITE DESCRIPTION TEXT
                    {
                        "kind": "textarea",
                        "id": "site-description-text",
                        "name": "siteDescriptionText",
                        "label": "Site Description Text",
                        "description": "enter text/html for the site description",
                        "value": "This is XNAT.",
                        "hide": true,
                        "show": "~siteDescriptionType.value=='text'"
                    },

                    // SITE DESCRIPTION PAGE URL
                    {
                        "kind": "input-url", // accepts only valid URL string
                        "id": "site-description-page",
                        "name": "siteDescriptionPage",
                        "label": "Site Description Page",
                        "description": "enter path to file containing the site description",
                        "value": "/screens/site_description.vm",
                        "hide": true,
                        "show": "(XNAT.siteAdmin.siteInfo.siteDescription.siteDescriptionType.value=='page')"
                    }
                ]
            }
        ]
    }
];

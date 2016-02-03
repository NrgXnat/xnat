function MultiValueConfig(controlId, url, allowSiteDefault, _default, _handlers) {
    this.url = (url.startsWith(serverRoot)) ? url : serverRoot + url;
    this.id = controlId;
    this.allowSiteDefault = allowSiteDefault != undefined && Boolean(allowSiteDefault);
    this._default = (_default == undefined) ? "" : _default;
    this._handlers = (_handlers == undefined) ? {} : _handlers;

    this.init = function () {
        var that = this;
        jq.get(this.url + "?contents=true&timestamp=" + (new Date()).getTime() + ((this.allowSiteDefault) ? "&defaultToSiteWide=true" : ""))
            .done(function (data) {
                jq(that.id).val(data);
                jq(that.id).attr('disabled', false);
            })
            .fail(function (xhr, status, error) {
                if (xhr.status == 404) {
                    jq(that.id).attr('checked', that._default);
                    jq(that.id).attr('disabled', false);
                } else {
                    showMessage("page_body", "Error", "Failed to load setting into control " + that.id + ": [" + xhr.status + "] " + error);
                }
            });
    };

    this.update = function () {
        jq(this.id).attr('disabled', true);
        var that = this;
        var value = jq(this.id).val();
        if (that._handlers.hasOwnProperty(value)) {
            that._handlers[value]();
        } else {
            jq.ajax({
                url: this.url + "?inbody=true&XNAT_CSRF=" + window.csrfToken,
                type: 'PUT',
                data: "" + value,
                contentType: "text/plain"
            })
            .done(function () {
                jq(that.id).attr('disabled', false);
            })
            .fail(function (xhr, status, error) {
                jq(that.id).attr('disabled', false);
                showMessage("page_body", "Failure", "Failed to modify setting in control " + that.id + ": [" + xhr.status + "] " + error);
            });
        }
    };
}
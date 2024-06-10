console.log('migrationActivityMonitoring.js');

var XNAT = getObject(XNAT || {});
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
}(function() {
    XNAT = getObject(XNAT || {});
    XNAT.customVariableMigrator= getObject(XNAT.customVariableMigrator || {});
    XNAT.customVariableMigrator.updateMigrationProgress = function (itemDivId, detailsTag, jsonobj, lastProgressIdx) {
        const succeeded = jsonobj['succeeded'];
        const payload = JSON.parse(jsonobj['payload']);
        let messages = "";
        let entryList = payload ? (payload['entryList'] || []) : [];
        if (entryList.length === 0 && succeeded == null) {
            return [null, lastProgressIdx];
        }
        entryList.forEach(function (e, i) {
            if (i <= lastProgressIdx) {
                return;
            }
            let level = e.status;
            let message = e.message.charAt(0).toUpperCase() + e.message.substr(1);
            let clazz;
            switch (level) {
                case 'Waiting':
                case 'InProgress':
                    clazz = 'info';
                    break;
                case 'Warning':
                    clazz = 'warning';
                    break;
                case 'Failed':
                    clazz = 'error';
                    break;
                case 'Completed':
                    clazz = 'success';
                    break;
            }

            messages += '<div class="prog ' + clazz + '">' + message + '</div>';
            lastProgressIdx = i;
        });

        if (succeeded != null) {
            messages += '<div class="prog ' + (succeeded ? "success" : "warning") + '">' + jsonobj['finalMessage'] + '</div>';
        }

        if (messages) {
            $(detailsTag).append(messages);
        }

        return {succeeded: succeeded, lastProgressIdx: lastProgressIdx};
    }
}))
/*
 * web: validate.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Form and value validation functions for XNAT
 * Some code adapted from http://rickharrison.github.com/validate.js
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

    var undefined, undef, validate;

    XNAT.validation = getObject(XNAT.validation || {});

    // HELPERS

    function isNull(value){
        return !!(value == null || value === '');
    }

    // copy of jQuery's $.isNumeric() method
    function isNumeric( num ) {
        return !isArray( num ) && (num - parseFloat( num ) + 1) >= 0;
    }

    // TODO: implement display of error messages
    var message = {
        required: 'The __NAME__ field is required.',
        matches: 'The __NAME__ field does not match the __VALUE__ field.',
        "default": 'The __NAME__ field is still set to default, please change.',
        email: 'The __NAME__ field must contain a valid email address.',
        emails: 'The __NAME__ field must contain all valid email addresses.',
        minLength: 'The __NAME__ field must be at least __VALUE__ characters in length.',
        maxLength: 'The __NAME__ field must not exceed __VALUE__ characters in length.',
        exactLength: 'The __NAME__ field must be exactly __VALUE__ characters in length.',
        greaterThan: 'The __NAME__ field must contain a number greater than __VALUE__.',
        lessThan: 'The __NAME__ field must contain a number less than __VALUE__.',
        alpha: 'The __NAME__ field must only contain alphabetical characters.',
        alphaNumeric: 'The __NAME__ field must only contain alpha-numeric characters.',
        alphaDash: 'The __NAME__ field must only contain alpha-numeric characters, underscores, and dashes.',
        numeric: 'The __NAME__ field must contain only numbers.',
        number: "The __NAME__ value must be of type 'number'.",
        integer: 'The __NAME__ field must contain an integer.',
        decimal: 'The __NAME__ field must contain a decimal number.',
        natural: 'The __NAME__ field must contain only positive numbers.',
        naturalNoZero: 'The __NAME__ field must contain a number greater than zero.',
        ip: 'The __NAME__ field must contain a valid IP.',
        base64: 'The __NAME__ field must contain a base64 string.',
        creditCard: 'The __NAME__ field must contain a valid credit card number.',
        fileType: 'The __NAME__ field must contain only __VALUE__ files.',
        validUrl: 'The __NAME__ field must contain a valid URL.',
        greaterThanDate: 'The __NAME__ field must contain a more recent date than __VALUE__.',
        lessThanDate: 'The __NAME__ field must contain an older date than __VALUE__.',
        greaterThanOrEqualDate: "The __NAME__ field must contain a date that's at least as recent as __VALUE__.",
        lessThanOrEqualDate: "The __NAME__ field must contain a date that's __VALUE__ or older."
    };

    // auto-generate alternate property names from camelCase names
    // creates hyphen-ated and under_score aliases
    // clutters up the namespace, but... oh, well
    forOwn(message, function(name){
        message[name.toLowerCase()] = message[name];  // lowercase names
        message[toDashed(name)]     = message[name];  // hyphen-ated names
        message[toUnderscore(name)] = message[name];  // under_score names
    });

    var regex = {
        //required: /[\S\W]+/,                // whitespace characters will still validate
        notEmpty: /[\S]/,                   // must contain more than just whitespace characters
        rule: /^(.+?)\[(.+)\]$/,            // ?
        //numeric: /^-?\d*\d{3}[,]*\d[.]*\d+$/,
        integer: /^-?[0-9]+$/,              // positive or negative whole number
        natural: /^[0-9]+$/,                // positive whole number
        naturalNoZero: /^([1-9]+[0-9]*)+$/,  // positive whole number, no leading 0s
        decimal: /^(-?[0-9]*\.?[0-9])+$/,
        hexadecimal: /^[0-9a-f]+$/i,
        email: /^([a-z0-9.!#$%&'*+\/=?^_`{|}~-]+@[a-z0-9](?:[.a-z0-9-]{0,61}[a-z0-9])?(?:\.[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?))$/i,
        // (email regex below prevents multiple 'dots' but hasn't been tested thoroughly)
        // email: /^([a-z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-z0-9](?:(\.?[a-z0-9-]){0,61}[a-z0-9])?(?:\.?[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])))$/i,
        // email should start with a letter or number, contain a single @, and end with a valid domain
        // does not allow multiple sequential . or leading or trailing - or . in domain
        // email2: /^[a-z0-9.!#$%&’*+/=?^_`{|}~-]{1,125}(@(?![.-]))([a-z0-9]*[.-]?[a-z0-9]+){1,125}$/i,
        // W3C regex for <input type="email">
        emailW3c: /^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$/,
        alpha: /^[a-z]+$/i,                    // ONLY letters
        alphaDash: /^[a-z\-]+$/i,              // ONLY letters and dash
        alphaDashSpace: /^[a-z \-]+$/i,        // ONLY letters, dash, and space
        alphaLower: /^[a-z]+$/,                // ONLY lowercase letters
        alphaUpper: /^[A-Z]+$/,                // ONLY uppercase letters
        alphaSafe: /^[a-z_]+$/i,               // ONLY letters and underscores
        alphaNum: /^[a-z0-9]+$/i,              // ONLY letters and numbers
        alphaNumSpace: /^[a-z0-9 ]+$/i,        // ONLY letters, numbers, and space
        alphaNumSafe: /^[a-z0-9_]+$/i,         // ONLY letters, numbers, and underscore
        alphaNumSafeSpace: /^[a-z0-9_ ]+$/i,   // ONLY letters, numbers, underscore, and space
        alphaNumDash: /^[a-z0-9_\-]+$/i,       // ONLY letters, numbers, underscore, and dash
        alphaNumDashSpace: /^[a-z0-9 _\-]+$/i, // ONLY letters, numbers, underscore, dash, and space
        nameSafe: /^[a-z \-]+$/i,              // safe to use for names - letters, spaces, and hyphens
        // safe to use for usernames - starts with a letter or number and contains letters, numbers, underscores, hyphens and periods
        username: /^([a-z0-9]+[a-z0-9._-]*)$/i,
        idSafe: /^([a-z][a-z0-9_\-]*)$/i,      // safe to use as an ID - alphasafe and must start with a letter
        idStrict: /^([a-z][a-z0-9_]*)$/i,      // 'idSafe' without hyphens
        ip: /^(((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\.){3}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2}))$/i,
        base64: /^([^a-zA-Z0-9\/+=])+$/i,
        numericDash: /^[\d\-\s]+$/,
        //fqdn: /^([a-z0-9]*(\.(?![.-]))*(-(?!\.))*([a-z0-9](?![^a-z0-9]))*){1,252}$/i,
        //url: /^(((http|https):\/\/(\w+:{0,1}\w*@)?(\S+)|)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/]))?)$/,
        //url: /^(https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{2,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\+.~#?&/=]*))$/i,
        url: /^(https?:\/\/[^\/\s]+(\/.*)?)$/i, // keep it simple for less strict url validation
        //uri: /^(([\/](\w+:{0,1}\w*@)?(\S+)|)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@!\-\/])))$/,
        uri: /^(\/\w*)/i, // simpler URI check only requires string start with a single '/'
        // these date regexes can't check leap years or other incorrect MM/DD combos
        //dateISO: /^((19|20)\d\d([- /.])(0[1-9]|1[012])\2(0[1-9]|[12][0-9]|3[01]))$/,
        //dateUS: /^((0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])[- /.](19|20)\d\d)$/,
        //dateEU: /^((0[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /.](19|20)\d\d)$/,
        // CRON!!!!!  (Say it like "KHAN!!!!!")
        cronWords: /^@(reboot|yearly|annually|monthly|weekly|daily|midnight|hourly)$/i,
        cronSeconds: /^((\*|\?|0|([1-9]|[1-5][0-9]))(\/\d+)?)$/,
        cronMinutes: /^((\*|\?|0|([1-9]|[1-5][0-9]))(\/\d+)?)$/,
        cronHours: /^((\*|\?|([0-9]|1[0-9]|2[0-3]))(\/\d+)?)$/,
        cronDay: /^((\*|\?|([0-9]|[1-2][0-9]|3[0-1]))(\/\d+)?)$/,
        cronMonth: /^((\*|\?|([0-9]|1[0-2]))(\/\d+)?)$/,
        cronMonths: /^(((\*|\?|([0-9]|1[0-2]))(\/\d+)?)|(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|DEC))$/i,
        cronMonthNames: /^(\*|\?|JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|DEC)$/i,
        cronWeekday: /^((\*|\?|([0-7]))(\/\d+)?)$/,
        cronWeekdays: /^(((\*|\?|([0-7]))(\/\d+)?)|(MON|TUE|WED|THU|FRI|SAT|SUN))$/i,
        cronWeekdayNames: /^(\*|\?|MON|TUE|WED|THU|FRI|SAT|SUN)$/i,
        // cronAlt: /^(\*|([0-9]|1[0-9]|2[0-9]|3[0-9]|4[0-9]|5[0-9])|\*\/([0-9]|1[0-9]|2[0-9]|3[0-9]|4[0-9]|5[0-9])) (\*|([0-9]|1[0-9]|2[0-3])|\*\/([0-9]|1[0-9]|2[0-3])) (\*|([1-9]|1[0-9]|2[0-9]|3[0-1])|\*\/([1-9]|1[0-9]|2[0-9]|3[0-1])) (\*|([1-9]|1[0-2])|\*\/([1-9]|1[0-2])) (\*|([0-6])|\*\/([0-6]))$/,
        // cron regex lifted from this post: http://stackoverflow.com/questions/235504/validating-crontab-entries-w-php
        // cron: /^\s*($|#|\w+\s*=|(\*(?:\/\d+)?|(?:[0-5]?\d)(?:-(?:[0-5]?\d)(?:\/\d+)?)?(?:,(?:[0-5]?\d)(?:-(?:[0-5]?\d)(?:\/\d+)?)?)*)\s+(\*(?:\/\d+)?|(?:[01]?\d|2[0-3])(?:-(?:[01]?\d|2[0-3])(?:\/\d+)?)?(?:,(?:[01]?\d|2[0-3])(?:-(?:[01]?\d|2[0-3])(?:\/\d+)?)?)*)\s+(\*(?:\/\d+)?|(?:0?[1-9]|[12]\d|3[01])(?:-(?:0?[1-9]|[12]\d|3[01])(?:\/\d+)?)?(?:,(?:0?[1-9]|[12]\d|3[01])(?:-(?:0?[1-9]|[12]\d|3[01])(?:\/\d+)?)?)*)\s+(\*(?:\/\d+)?|(?:[1-9]|1[012])(?:-(?:[1-9]|1[012])(?:\/\d+)?)?(?:,(?:[1-9]|1[012])(?:-(?:[1-9]|1[012])(?:\/\d+)?)?)*|jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\s+(\*(?:\/\d+)?|(?:[0-6])(?:-(?:[0-6])(?:\/\d+)?)?(?:,(?:[0-6])(?:-(?:[0-6])(?:\/\d+)?)?)*|mon|tue|wed|thu|fri|sat|sun)\s+|(@reboot|@yearly|@annually|@monthly|@weekly|@daily|@midnight|@hourly)\s+)([^\s]+)\s+(.*)$/,
        bogus: /bogus/i // filler
    };
    // aliases
    regex.int = regex.integer;
    //regex.number = regex.numeric;
    regex.float = regex.decimal;
    regex.hex = regex.hexadecimal;
    regex.alphaDashSp = regex.alphaDashSpace;
    regex.alphaNumeric = regex.alphaNum;
    regex.alphaNumSp = regex.alphaNumSP = regex.alphaNumSpace;
    regex.alphaNumSafeSp = regex.alphaNumSafeSP = regex.alphaNumSafeSpace;
    regex.alphaNumericSafe = regex.alphaNumSafe;
    regex.usernameSafe = regex.username;
    regex.ipAddr = regex.ipAddress = regex.ip;
    regex.fullUrl = regex.url;
    regex.path = regex.uri;
    // regex.date = regex.dateISO;

    // auto-generate alternate property names from camelCase names
    // creates hyphen-ated and under_score aliases
    // clutters up the namespace, but... oh, well
    forOwn(regex, function(name){
        regex[name.toLowerCase()] = regex[name];  // lowercase names
        regex[toDashed(name)]     = regex[name];  // hyphen-ated names
        regex[toUnderscore(name)] = regex[name];  // under_score names
    });

    // export combined 'regex' object back to global namespace
    XNAT.validation.regex = extend(regex, XNAT.validation.regex || {});


    // define custom test methods for more complex validations
    var test = {};

    test.required = function(){
        // 'this' is the parent Validator instance
        return (new Validator(this.element)).required().validated;
    };

    test.empty = function(value){
        return !(value+'');
    };

    test.not = function(value, not){
        return (new Validator()).val(value).not(not).validated;
    };

    test.numeric = test.number = function(value){
        console.log('numeric');
        return isNumeric(value);
    };

    test.dateISO = function(value){

        // allow / - or . for separators
        var parts = typeof value === 'string' ?
                value.replace(/\s+/g,'-').split(/[-/.]/) :
                value.slice();

        // there must be exactly 3 parts
        // year, month, date
        if (parts.length !== 3) {
            return false;
        }

        var year = parts[0].trim();
        var mo   = parts[1].trim();
        var day  = parts[2].trim();

        if (year.length !== 4 || mo.length !== 2 || day.length !== 2){
            return false;
        }

        year = +(year.replace(/^0+/, ''));
        mo   = +(mo.replace(/^0+/, ''));
        day  = +(day.replace(/^0+/, ''));

        if (year < 1850 || year > 2100) {
            return false;
        }
        if (mo < 1 || mo > 12){
            return false;
        }
        if (day < 1 || day > 31){
            return false;
        }

        var isLeapYear = year % 4 === 0;
        var febMax = isLeapYear ? 29 : 28;

        // test for valid day value for February
        if (mo === 2 && day > febMax) {
            return false;
        }

        // months with 30 days
        // April, June, September, November
        var shortMonths = [4, 6, 9, 11];

        if (shortMonths.indexOf(mo) > -1) {
            if (day > 30) {
                return false;
            }
        }

        // made it to the end - must be valid
        return true;

    };

    test.dateUS = function(value){
        var parts = value.replace(/\s+/g,'-').split(/[-/.]/);
        var mo   = parts[0] || '';
        var day  = parts[1] || '';
        var year = parts[2] || '';
        return test.dateISO([year, mo, day]);
    };

    test.dateEU = function(value){
        var parts = value.replace(/\s+/g,'-').split(/[-/.]/);
        var day  = parts[0] || '';
        var mo   = parts[1] || '';
        var year = parts[2] || '';
        return test.dateISO([year, mo, day]);
    };

    // plain 'date' validation defaults
    // to ISO: 2001-12-31 (YYYY-MM-DD)
    test.date = function(value, format){
        format = format || 'iso';
        if (/iso/i.test(format)){
            return test.dateISO(value);
        }
        if (/us/i.test(format)){
            return test.dateUS(value);
        }
        if (/eu/i.test(format)){
            return test.dateEU(value);
        }
    };

    test.interval = function(value){
        console.log('interval');
        var parts = value.split(/([0-9]+)/);
        var units = /\s+(sec|second|min|minute|hour|day|week|month|year)(s)?\s*/;
        var num = true;
        var valid = value ? true : false;
        var i = parts[0] === '' ? 1 : 0; // start i at 1 if parts[0] is an empty string
        var part;
        while (parts[i] && valid === true) {
            part = (parts[i] + '');
            if (num) {
                valid = /\d+/.test(part);
            }
            else {
                valid = units.test(part);
            }
            num = !num; // flip for next iteration
            i++;
        }
        return valid;
    };

    // match to 6-field cron syntax:
    // 0 0 * * * *
    test.cron = test.cronSyntax = function(value){

        value = (value+'').trim();

        // easiest test - use words
        if (regex.cronWords.test(value)) {
            return true;
        }

        // split value to test parts
        var parts = value.split(/\s+/);

        // array of regexes to match 'parts' array
        var tests = [
            regex.cronSeconds,
            regex.cronMinutes,
            regex.cronHours,
            regex.cronDay,
            regex.cronMonths,
            regex.cronWeekdays
        ];

        var errors = 6;

        parts.forEach(function(part, i){
            errors = tests[i].test(part) ? errors - 1 : errors ;
        });

        return errors === 0;

    };

    // check a comma- or space-separated list of multiple email addresses
    test.emails = function(value){
        var errors = 0;
        value.split(/[,\s]+/).forEach(function(email){
            if (errors) return false;
            email = email.trim();
            if (!regex.email.test(email)) {
                errors++
            }
        });
        return errors === 0;
    };

    // check for a valid domain name -
    // probably not as fast as a single regex
    // but should only be called a few times at once
    test.fqdn = function(value){
        return !!value && (
                // allow single-character values (?)
                /^[a-z0-9]$/.test(value) || (
                // only letters, numbers, hyphens, and periods are valid
                /^[a-z0-9.-]+$/i.test(value) &&
                // must start AND end with a letter or number
                /^[a-z0-9].*[a-z0-9]$/i.test(value) &&
                // cannot contain consecutive dashes or dots
                !/\.{2,}|-{2,}|\.-|-\./i.test(value)
            ));
    };

    // make sure there's a minimum number of characters
    test.minLength = function(value, length){
        if (!regex.naturalNoZero.test(length)) {
            return false;
        }
        return (value.length >= parseInt(length, 10));
    };

    // don't exceed the maximum number of characters
    test.maxLength = function(value, length){
        if (!regex.naturalNoZero.test(length)) {
            return false;
        }
        return (value.length <= parseInt(length, 10));
    };

    test.exactLength = function(value, length){
        if (!regex.naturalNoZero.test(length)) {
            return false;
        }
        return (value.length === parseInt(length, 10));
    };
    test.isLength = test.exactLength;

    // XNAT.validate('#concurrent-sessions').is('greaterThan', 0).check();
    test.greaterThan = function(value, num){
        if (!regex.decimal.test(value)) {
            return false;
        }
        return (parseFloat(value) > parseFloat(num));
    };
    test.gt = test.greaterThan;

    test.greaterThanOrEqual = function(value, num){
        if (!regex.decimal.test(value)) {
            return false;
        }
        return (parseFloat(value) >= parseFloat(num));
    };
    test.greaterThanOrEqualTo = test.greaterThanOrEqual;
    test.gte = test.greaterThanOrEqual;

    // XNAT.validate('#session-timeout').is('lessThan', 999).check();
    test.lessThan = function(value, num){
        if (!regex.decimal.test(value)) {
            return false;
        }
        return (parseFloat(value) < parseFloat(num));
    };
    test.lt = test.lessThan;

    test.lessThanOrEqual = function(value, num){
        if (!regex.decimal.test(value)) {
            return false;
        }
        return (parseFloat(value) <= parseFloat(num));
    };
    test.lessThanOrEqualTo = test.lessThanOrEqual;
    test.lte = test.lessThanOrEqual;

    test.equalTo = function(value, testValue){
        if (/^![^!]/.test(testValue)){
            return value+'' !== testValue+'';
        }
        return value+'' === testValue+''
    };
    test.equals = test.equalTo;
    test.eq = test.equalTo;

    // date checks
    test.greaterThanDate = function(value, date){
        var enteredDate = getValidDate(value),
            validDate   = getValidDate(date);
        if (!validDate || !enteredDate) {
            return false;
        }
        return enteredDate > validDate;
    };
    test.gtDate = test.greaterThanDate;
    test.greaterThanOrEqualDate = function(value, date){
        var enteredDate = getValidDate(value),
            validDate   = getValidDate(date);
        if (!validDate || !enteredDate) {
            return false;
        }
        return enteredDate >= validDate;
    };
    test.greaterThanOrEqualToDate = test.greaterThanOrEqualDate;
    test.gteDate = test.greaterThanOrEqualDate;
    test.lessThanDate = function(value, date){
        var enteredDate = getValidDate(value),
            validDate   = getValidDate(date);
        if (!validDate || !enteredDate) {
            return false;
        }
        return enteredDate < validDate;
    };
    test.ltDate = test.lessThanDate;
    test.lessThanOrEqualDate = function(value, date){
        var enteredDate = getValidDate(value),
            validDate   = getValidDate(date);
        if (!validDate || !enteredDate) {
            return false;
        }
        return enteredDate <= validDate;
    };
    test.lessThanOrEqualToDate = test.lessThanOrEqualDate;
    test.lteDate = test.lessThanOrEqualDate;


    // XNAT.validate('input.credit-card').is('creditCard').check();
    test.creditCard = function(value){
        // Luhn Check Code from https://gist.github.com/4075533
        // accept only digits, dashes or spaces
        if (!regex.numericDash.test(value)) return false;

        // The Luhn Algorithm. It's so pretty.
        var nCheck = 0, nDigit = 0, bEven = false;
        var strippedField = value.replace(/\D/g, "");

        for (var n = strippedField.length - 1; n >= 0; n--) {
            var cDigit = strippedField.charAt(n);
            nDigit = parseInt(cDigit, 10);
            if (bEven) {
                if ((nDigit *= 2) > 9) nDigit -= 9;
            }

            nCheck += nDigit;
            bEven = !bEven;
        }

        return (nCheck % 10) === 0;
    };

    // Check file extension of submitted file.
    //
    // XNAT.validate('input.doc[type="file"]').is('fileType', 'doc').check()
    //
    test.fileType = function(value, type){
        //if (type !== 'file') {
        //    return true;
        //}
        var ext       = value.substr((value.lastIndexOf('.') + 1)).toLowerCase(),
            typeArray = type.split(','),
            inArray   = false,
            i         = -1,
            len       = typeArray.length;

        while (++i < len) {
            if (ext == typeArray[i].toLowerCase()) {
                inArray = true;
            }
        }

        return inArray;
    };

    test.lookup = function(value, obj){
        // TODO: use special syntax to check a value via REST or object value lookup
    };

    // auto-generate alternate property names from camelCase names
    // creates hyphen-ated and under_score aliases
    forOwn(test, function(name){
        test[name.toLowerCase()] = test[name];  // lowercase names
        test[toDashed(name)]     = test[name];  // hyphen-ated names
        test[toUnderscore(name)] = test[name];  // under_score names
    });

    // export combined 'test' object back to global namespace
    XNAT.validation.test = extend(test, XNAT.validation.test || {});


    // HELPERS

    function init(element){
        var obj = {
            len: 0,
            allowEmpty: false,
            checked: false,
            errors: 0,
            messages: [],
            method: null,
            regex: null,
            value: '',
            values: [], // use to check more than one value
            validations: {}, // keep track of validation results by method name
            validated: true // true until proven false
        };
        if (element) {
            obj.element$ = $$(element).removeClass('valid invalid');
            obj.len = obj.element$.length;
            if (obj.len) {
                obj.element = obj.element$[0];
                obj.value = obj.element.value || '';
            }
        }
        else {
            obj.element$ = $.spawn('input.tmp|type=hidden');
            obj.element = obj.element$[0];
        }
        return obj;
    }

    // TODO: get this working
    function getValidDate(date){

        var regexDateMatch = (
                date.match(regex['dateISO']) ||
                date.match(regex['dateUS']) ||
                date.match(regex['dateEU'])
        );

        if (!date.match('today') && !regexDateMatch) {
            return false;
        }

        var validDate = new Date(),
            validDateArray;

        if (!date.match('today')) {
            validDateArray = date.split(/[\s.-/]+/);
            validDate.setFullYear(validDateArray[0]);
            validDate.setMonth(validDateArray[1] - 1);
            validDate.setDate(validDateArray[2]);
        }

        return validDate;

    }


    // CONSTRUCTOR

    function Validator(element){
        extend(this, init(element));
    }

    Validator.fn = Validator.prototype;

    Validator.fn.init = function(element){
        if (element){
            extend(this, init(element));
        }
        return this;
    };

    // allow chaining of validation methods to
    // continue validating even after failed validation
    // break a chain by calling .chain(false)
    Validator.fn.chain = function(bool){
        this.chained = bool !== undefined ? bool : true;
        return this;
    };

    // break a chain by calling .unchain()
    Validator.fn.unchain = function(){
        this.chained = false;
        return this;
    };

    // explicitly set a value to check
    // XNAT.validate().val('bar@foo.org').is('email').check();
    // XNAT.validate().val('bar@foo.org').check('email');
    // -> true
    Validator.fn.val = function(value){
        // this ridiculous construct should:
        // - explicity set a 'value' if argument is passed and doesn't match existing this.value
        // - attempt to retrieve a value from an element if value arg is falsey
        // - fallback to this.value if 'value' arg is falsey
        this.value =
                !value || this.value !== value ?
                        value || this.element.value :
                        this.element.value || this.value ;
        return this;
    };

    Validator.fn.trim = function(){
        this.trimValue = true;
        this.element$.each(function(){
            this.value = this.value.trim();
        });
        return this;
    };

    // keep track of the validation methods
    // along with their successes and failures
    Validator.fn.setMethod = function(method){
        this.method = method+'';
        if (!this.validations[this.method]) {
            this.validations[this.method] = {
                success: [],
                failure: []
            };
        }
        return this;
    };

    // set className to valid/invalid
    // optionally resetting to 'valid'
    Validator.fn.setClass = function(className){
        if (this.errors) {
            this.element$.removeClass('valid').addClass('invalid');
        }
        // do not alter classes for invalid chained validations
        // else if (!this.checked  || !this.chained) {
        //     this.element$.removeClass('valid invalid').addClass(className||'');
        // }
        if (className != null) {
            this.checked = true;
        }
        return this;
    };

    // reset element so it can be validated again
    Validator.fn.reset = function(element){
        extend(this, init(element||this['element']));
        this.validated = true;
        this.checked = false;
        this.setClass(null);
        return this;
    };

    Validator.fn.is = function(type, args){

        var parts = [];

        // check all if there's more than
        // one element in the selection
        if (this.len > 1) {
            this.all(type, args);
            return this;
        }

        // return early if the validation is already false
        // (this is necessary for breaking out of chained methods)
        if (!this.chained && this.validated === false) { return this }

        // skip on* types
        if (/^on/i.test(type)) {
            return this;
        }

        this.setMethod(type);

        // try to fetch a fresh value from the element
        this.val(this.element.value);

        if (this.trimValue) {
            this.value = (this.value+'').trim();
        }

        // set 'allowEmpty' flag
        if (type === 'allow-empty') {
            this.allowEmpty = true;
            return this;
        }

        if (typeof type === 'string') {
            parts = type.split(':');
            type = parts.shift();
            if (parts.length) {
                this.is(type, parts.join(':'));
                return this;
            }
        }

        if (/^(regex|pattern)$/i.test(type)) {
            this.pattern(args);
            return this;
        }

        // start with '!' for negation
        // !eq:0
        if (/^![^!]/.test(type)){
            this.not(type.replace(/^!/, ''), args);
            return this;
        }

        // if there's a test['test'] method, use that
        if (typeof test[type] === 'function') {
            this.validated = test[type].apply(this, [].concat(this.value, args));
        }
        // if there's a regex defined (above) for 'type', use that
        else if (regex[type]) {
            this.pattern(regex[type]);
            // this.validated = regex[type].test(this.value);
        }
        // if 'type' is a string, number or boolean, do a string comparison
        else if (/string|number|boolean/i.test(typeof type)) {
            this.validated = test.equals.apply(this, [].concat(this.value, type, args));
        }
        // a 'type' function can also be passed
        // (must return boolean true or false)
        else if (typeof type === 'function') {
            this.validated = type.apply(this, [].concat(args));
        }
        // otherwise do a regex test
        else {
            try {
                this.pattern(type);
                // this.validated = type.test(this.value);
            }
            catch(e) {
                console.log(e);
            }
        }

        // let empty string validate if 'allowEmpty' is true
        if (this.allowEmpty && (this.value+'').trim() === ''){
            this.validated = true;
        }

        return this;
    };

    Validator.fn.not = function(type, args){
        this.is(type, args);
        this.validated = !this.validated;
        return this;
    };

    // validate all elements in a collection
    // XNAT.validate('input.float').all('float');
    Validator.fn.all = function(type, args){
        // var self = this;
        var invalid = 0;
        this.elements = this.element$.toArray();
        if (!type) return this;
        // if type is specified, check each element
        this.element$.each(function(){
            var elValidate = new Validator(this);
            elValidate.is(type, args);
            //valid = regex[type].test(this.value);
            if (!elValidate.valid(true)) {
                invalid++
            }
        });
        this.validated = invalid === 0;
        return this;
    };

    Validator.fn.none = function(type){
        // TODO: make sure NONE of the elements match the type
    };

    Validator.fn.pattern = function(regex){
        this.setMethod('pattern');
        this.regex = (typeof regex === 'string') ? new RegExp(regex) : regex;
        this.validated = this.regex.test(this.value);
        return this;
    };

    // match the value of another element
    // optionally trimming leading and trailing whitespace
    Validator.fn.matches = function(target, trim){
        var sourceValue = this.value + '';
        var targetValue = $$(target).val() + '';
        if (trim) {
            sourceValue = sourceValue.trim();
            targetValue = targetValue.trim();
        }
        this.validated = sourceValue === targetValue;
        this.setMethod('matches');
        return this;
    };

    // XNAT.validate('#url').required().check();
    Validator.fn.required = function(){
        this.all(function(){
            if (/checkbox|radio/.test(this.element$.type)) {
                return (this.checked === true);
            }
            return this.value+'' !== '';
        });
        this.setMethod('required');
        return this;
    };

    // set up shortcut methods (uses test[type]() methods)
    // example:
    // XNAT.validate('#sessions-concurrent-max').lessThan(1000).check();
    [   'minLength', 'maxLength', 'exactLength', 'isLength',
        'greaterThan', 'gt', 'greaterThanOrEqual', 'greaterThanOrEqualTo', 'gte',
        'lessThan', 'lt', 'lessThanOrEqual', 'lessThanOrEqualTo', 'lte',
        'equalTo', 'equals', 'fileType'   ].forEach(function(method) {

        Validator.fn[method] = function(test) {
            this.is(method, test);
            return this;
        }

    });

    // success callback method
    // XNAT.validate('#username').is('alpha-num-safe').success(doSomething)
    Validator.fn.success = function(messageOrCallback){
        if (this.validated) {
            if (isFunction(messageOrCallback)) {
                messageOrCallback.call(this);
            }
            else {
                this.messages.push(messageOrCallback);
            }
            this.validations[this.method].success.push(messageOrCallback);
        }
        return this;
    };

    // failure callback method
    // XNAT.validate('#project-id').is('id-safe').failure(doSomethingElse)
    Validator.fn.failure = function(messageOrCallback){
        if (!this.validated) {
            if (isFunction(messageOrCallback)) {
                messageOrCallback.call(this);
            }
            else {
                this.messages.push(messageOrCallback);
            }
            this.validations[this.method].failure.push(messageOrCallback);
        }
        return this;
    };

    // .valid() must be called LAST
    // XNAT.validate('#email').trim().is('email').valid(true);
    Validator.fn.valid = function(bool){
        bool = (bool === undefined) ? true : bool;
        this.validated = bool ? this.validated : !this.validated;
        this.errors += (!this.validated ? 1 : 0);
        return this.validated;
    };
    //
    // call *either* .valid() -OR- .check() last
    //
    // XNAT.validate('input.email').is('email').valid(true);
    //
    // --OR--
    //
    // XNAT.validate().value('foo').check(function(){ return this.value === 'foo' });
    //
    // .check() must be called last
    // XNAT.validate('#email').check('email');
    //
    // type can be regex['type'] string,
    // function (must return true or false),
    // or custom regex
    Validator.fn.check = function(type, opts){
        var self = this,
            types = [];
        if (type) {
            this.is(type);
        }
        else if (type !== false) {
            if (this.element$.dataAttr('validate')) {
                types = this.element$.dataAttr('validate').split(/\s+/);
                $.each(types, function(idx, item){
                    // stop if validation has already failed
                    if (!self.validated) {
                        return false;
                    }
                    // skip on* types
                    if (!/^on/i.test(type)) {
                        self.is(item);
                    }
                })
            }
        }
        if (this.allowEmpty && !this.value) {
            this.validated = true;
        }
        if (opts) {
            if (opts.success && this.validated) {
                opts.success.call(this, this.element)
            }
            else if (opts.failure && !this.validated) {
                opts.failure.call(this, this.element)
            }
        }
        this.valid(true);
        // sets 'valid' class ONLY if validation passes
        this.setClass('valid');
        return this.validated;
    };

    // perform a callback function when validation is done;
    Validator.fn.done = function(callback){
        if (!this.checked) {
            this.check();
        }
        try { callback.call(this.element, this) }
        catch(e) { console.error(e) }
        return this;
    };

    // pass an object map of validation types and associated properties
    function checkAllExample(){
        var checkFoo = XNAT.validate('#foo-input').checkAll({
            gte: 1,
            minLength: 1
        });
        checkFoo.gte.failure('Must be greater than 1.');
        checkFoo.minLength.failure('Must be at least one character.');
        console.log(checkFoo.validator.messages.join(' '));
    }
    // the .checkAll() method is slightly confusing...
    Validator.fn.checkAll = function checkAll(typesObj){
        var self = this;
        var retObj = { validator: this };
        this.chained = true;
        if (isPlainObject(typesObj)){
            forOwn(typesObj, function(type, args){
                retObj[type] = self.is.apply(self, [].concat(type, args));
            });
        }
        return retObj;
    };

    Validator.fn.isValid = function(bool){
        this.validated = this.valid(bool);
        return this;
    };
    Validator.fn.validate = Validator.fn.isValid;

    // usage:
    // XNAT.validate('#user-email').trim().is('email').check();

    validate = function(element){
        return new Validator(element);
    };


    // TODO: move the date methods below to {test} object: test['greaterThanDate']() etc...
    validate.check = {

        greater_than_date: function(field, date){
            var enteredDate = getValidDate(field.value),
                validDate   = getValidDate(date);
            if (!validDate || !enteredDate) {
                return false;
            }
            return enteredDate > validDate;
        },

        less_than_date: function(field, date){
            var enteredDate = getValidDate(field.value),
                validDate   = getValidDate(date);
            if (!validDate || !enteredDate) {
                return false;
            }
            return enteredDate < validDate;
        },

        greater_than_or_equal_date: function(field, date){
            var enteredDate = getValidDate(field.value),
                validDate   = getValidDate(date);
            if (!validDate || !enteredDate) {
                return false;
            }
            return enteredDate >= validDate;
        },

        less_than_or_equal_date: function(field, date){
            var enteredDate = getValidDate(field.value),
                validDate   = getValidDate(date);
            if (!validDate || !enteredDate) {
                return false;
            }
            return enteredDate <= validDate;
        }
    };


    // add event listeners for validation
    $(function(){

        var $body = $(document.body);

        $body.on('focus', ':input[data-validate]', function(){
            $(this).removeClass('valid invalid');
        });

        $body.on('blur', ':input[data-validate].onblur', function(){
            validate(this).check();
        });

        // TODO: enable this after testing validation methods more thoroughly
        // $body.on('submit', 'form.validate', function(e){
        //     e.preventDefault();
        //     var errors = 0,
        //         $form = $(this);
        //     $form.find(':input[data-validate]').not('.ignore').each(function(){
        //         var valid = validate(this).check();
        //         if (!valid) { errors++ }
        //     });
        //     return errors === 0;
        //     // if (errors === 0){
        //     //     //$form.removeClass('validate').submit();
        //     //     return true;
        //     // }
        //     // return false;
        // });

    });

    // this script has loaded
    validate.loaded = true;

    return XNAT.validate = validate;

}));


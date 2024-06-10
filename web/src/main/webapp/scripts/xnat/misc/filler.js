/*
 * web: filler.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * XNAT filler text generator.
 * Inspired by (and words taken from):
 * http://hipsum.co/
 *
 * Usage:
 *
 * // sentences only
 * XNAT.misc.filler.sentence(); // generates 1 sentence with 9, 12, or 18 words
 * XNAT.misc.filler.sentences(3); // generates 3 sentences with default words
 * XNAT.misc.filler.sentences(4, [8, 12]); // generates 4 sentences with 8 or 12 words each
 *
 * // paragraphs
 * XNAT.misc.filler.paragraph(); // generates 1 paragraph with 2, 3, or 5 sentences with 9, 12, or 18 words
 * XNAT.misc.filler.paragraphs(); // generates 1, 2, or 3 paragraphs with 2, 3, or 5 sentences with 9, 12, or 18 words
 * XNAT.misc.filler.paragraphs(3); // generates 3 paragraphs
 * XNAT.misc.filler.paragraphs(2, 4); // generates 2 paragraphs with 4 sentences each
 * XNAT.misc.filler.paragraphs(3, 5, [10, 15]); // generates 3 paragraphs with 5 sentences each, with 10 or 15 words per sentence
 * XNAT.misc.filler.paragraphs(['foo','bar'], 1); // generates 1 paragraph with only 'foo' and 'bar' words
 *
 * // ids
 * XNAT.misc.filler.id(); // generates 'safe' id string (starts with letter, contains only 'safe' characters)
 * XNAT.misc.filler.id(3); // generates 'safe' id string using 3 words
 * XNAT.misc.filler.id(1, 'foo', true); // id string with one word, the 'foo' prefix with the timestamp appended
 */

var XNAT = (typeof XNAT == 'object') ? XNAT : {};

(function(XNAT){

    var misc, filler;

    var lingo =
        ('trust fund|street art|mustache|roof party|health|goth|selfies|next level|hashtag|' +
        'post-ironic|sriracha|banh mi|art party|paleo|flexitarian|tweet|street|art|scenester|' +
        'whatever|chillwave|tousled|tofu|raw|denim|cred|cold-pressed|migas|listicle|' +
        'crucifix|90s|farm-to-table|actually|craft beer|beard oil|' +
        'banjo|VHS|vinyl|8-bit|readymade|blog|locavore|pickled|skateboard|disrupt|' +
        'salvia|millenial|bitters|cassette|stumptown|butcher|lomo|single-origin|coffee|' +
        'master cleanse|yr mixtape|messenger bag|jean shorts|synth|retro|' +
        'narwhal|occupy|Austin|tote bag|pug|umami|viral|semiotics|small batch|gluten-free|' +
        'fingerstache|YOLO|ethical|DIY|typewriter|organic|mumblecore|fixie|vegan|four dollar toast|' +
        'keytar|meggings|cray-cray|polaroid|truffaut|flannel|hella|gastropub|food truck').split('|');

    var punc = ('. . . ? . . . ! . . . ? . . .').split(' '); // weight periods

    XNAT.misc = misc = getObject(XNAT.misc||{});
    XNAT.misc.filler = filler = getObject(XNAT.misc.filler||{});


    filler.Config = function(wordArray, paragraphCount, sentenceCount, wordCount){

        // set XNAT.misc.filler.words = ['foo','bar','etc'] for global custom word array
        this.wordArray = wordArray || filler.words || filler.wordArray || lingo;

        this.paragraphCount = function(){
            return randomFromArray( [].concat(paragraphCount || [1, 2, 3]) );
        };

        this.sentenceCount = function(){
            return randomFromArray( [].concat(sentenceCount || [2, 3, 5]) );
        };

        this.wordCount = function(){
            return randomFromArray( [].concat(wordCount || [9, 12, 15]) );
        };

    };

    // generate sentences (text only)
    filler.sentences = function(wordArray, sentenceCount, wordCount){

        var output = [],
            temp = [],
            i = -1,
            ii = -1,
            config = {};

        if (arguments[0] instanceof filler.Config){
            config = arguments[0];
        }
        else {
            if (!isArray(wordArray)){
                wordCount = sentenceCount;
                sentenceCount = wordArray;
                wordArray = null;
            }
            config = new filler.Config(wordArray, 1, sentenceCount, wordCount);
        }

        while (++i < config.sentenceCount()) {
            temp = [];
            ii = -1;
            while (++ii < config.wordCount()) {
                temp.push(randomFromArray(config.wordArray));
            }
            output.push(sentenceCase(temp.join(' ')) + randomFromArray(punc) + ' ');
        }

        return output.join(' ').replace(/\s+$/, '');
    };

    // generate ONE sentence (text only),
    // optionally specifying word count
    filler.sentence = function(wordArray, wordCount ){

        if (!isArray(wordArray)){
            wordCount = wordArray;
            wordArray = null;
        }

        var config = new filler.Config(wordArray, 1, 1, wordCount);

        return filler.sentences(config);
    };

    // generate string appropriate for use
    // in an HTML element id attribute:
    // starts with letter and has only
    // letters, numbers, and hyphens
    // optionally appending the time in ms
    filler.id = function(wordArray, wordCount, prefix, time){

        if (!isArray(wordArray)){
            time = prefix;
            prefix = wordCount;
            wordCount = wordArray;
            wordArray = null;
        }

        var config = new filler.Config(wordArray, 1, 1, wordCount||2);

        var id = filler.sentences(config);

        if (prefix) {
            id = prefix + '-' + id;
        }

        if (time) {
            id += ('-' + Date.now());
        }

        return id.
            replace(/^[^A-Za-z]/, 'x-$&'). // starts with other than a letter
            replace(/[^A-Za-z0-9]/g, '-'). // remove non-alphanumeric
            replace(/\-+/g, '-'). // remove any multiple hyphens that were created
            slice(0, -1).toLowerCase(); // remove punctuation and lowercaserize

    };

    // generate paragraphs (HTML with <p> tags)
    filler.paragraphs = function(wordArray, paragraphCount, sentenceCount, wordCount){

        if (!isArray(wordArray)){
            wordCount = sentenceCount;
            sentenceCount = paragraphCount;
            paragraphCount = wordArray;
            wordArray = null;
        }

        var config = new filler.Config(wordArray, paragraphCount, sentenceCount, wordCount);

        var output = '', i = -1;

        while (++i < config.paragraphCount()) {
            output += ('<p class="filler">' + filler.sentences(config) + '</p>');
        }

        return output;
    };

    // generate ONE paragraph (with <p></p>),
    // optionally specifying sentence and word count
    filler.paragraph = function(wordArray, sentenceCount, wordCount){

        if (!isArray(wordArray)){
            wordCount = sentenceCount;
            sentenceCount = wordArray;
            wordArray = null;
        }

        return filler.paragraphs(wordArray, 1, sentenceCount, wordCount);
    };


    filler.fromText = function(text, delim, paragraphCount, sentenceCount, wordCount){

        delim = (typeof delim != 'number') ? delim : /\s+/;

        return filler.paragraphs(text.split(delim), paragraphCount, sentenceCount, wordCount);

    };





    // HELPER FUNCTIONS BELOW





    function isArray( arr ){
        if ( Array.isArray ) {
            return Array.isArray(arr);
        }
        else {
            return Object.prototype.toString.call(arr) === '[object Array]';
        }
    }

    // convert array-like object or arguments to a real array
    // (twice as fast as Array.prototype.slice.call(arguments))
    function toArray(arr) {
        var i = -1,
            len = arr.length,
            _array = new Array(len);
        while (++i < len) {
            _array[i] = arr[i];
        }
        return _array;
    }

    function randomFromArray(arr){
        return arr[Math.floor(Math.random() * (arr.length))]
    }

    function sentenceCase(string){
        return string.charAt(0).toUpperCase() + string.slice(1);
    }

    function isPlainObject(obj){
        return Object.prototype.toString.call(obj) === '[object Object]';
    }

    function getObject(obj){
        return isPlainObject(obj) ? obj : {};
    }

})(XNAT);

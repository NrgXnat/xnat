/*!
 * Utility for encoding/decoding strings using Base64.
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

    var undef, sub64;

    XNAT.util = getObject(XNAT.util || {});

    XNAT.util.sub64 = sub64 =
        getObject(XNAT.util.sub64 || {});

    /*
     Copyright (c) 2008 Fred Palmer fred.palmer_at_gmail.com

     Permission is hereby granted, free of charge, to any person
     obtaining a copy of this software and associated documentation
     files (the "Software"), to deal in the Software without
     restriction, including without limitation the rights to use,
     copy, modify, merge, publish, distribute, sublicense, and/or sell
     copies of the Software, and to permit persons to whom the
     Software is furnished to do so, subject to the following
     conditions:

     The above copyright notice and this permission notice shall be
     included in all copies or substantial portions of the Software.

     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
     EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
     OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
     NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
     HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
     WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
     FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
     OTHER DEALINGS IN THE SOFTWARE.
     */

    function StringBuffer(){
        this.buffer = [];
    }

    StringBuffer.prototype.append = function append(string){
        this.buffer.push(string);
        return this;
    };

    StringBuffer.prototype.toString = function toString(){
        return this.buffer.join("");
    };

    var Base64 = {

        codex: "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",

        encode: function(input){
            var output = new StringBuffer();
            var enumerator = new Utf8EncodeEnumerator(input);
            while (enumerator.moveNext()) {
                var chr1 = enumerator.current;

                enumerator.moveNext();
                var chr2 = enumerator.current;

                enumerator.moveNext();
                var chr3 = enumerator.current;

                var enc1 = chr1 >> 2;
                var enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
                var enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
                var enc4 = chr3 & 63;

                if (isNaN(chr2)) {
                    enc3 = enc4 = 64;
                }
                else if (isNaN(chr3)) {
                    enc4 = 64;
                }

                output.append(this.codex.charAt(enc1) + this.codex.charAt(enc2) + this.codex.charAt(enc3) + this.codex.charAt(enc4));
            }

            return output.toString();
        },

        decode: function(input){
            var output = new StringBuffer();
            var enumerator = new Base64DecodeEnumerator(input);
            while (enumerator.moveNext()) {
                var charCode = enumerator.current;

                if (charCode < 128)
                    output.append(String.fromCharCode(charCode));
                else if ((charCode > 191) && (charCode < 224)) {
                    enumerator.moveNext();
                    var charCode2 = enumerator.current;

                    output.append(String.fromCharCode(((charCode & 31) << 6) | (charCode2 & 63)));
                }
                else {
                    enumerator.moveNext();
                    var charCode2 = enumerator.current;

                    enumerator.moveNext();
                    var charCode3 = enumerator.current;

                    output.append(String.fromCharCode(((charCode & 15) << 12) | ((charCode2 & 63) << 6) | (charCode3 & 63)));
                }
            }

            return output.toString();
        }
    };


    function Utf8EncodeEnumerator(input){
        this._input = input;
        this._index = -1;
        this._buffer = [];
    }

    Utf8EncodeEnumerator.prototype = {

        current: Number.NaN,

        moveNext: function(){
            if (this._buffer.length > 0) {
                this.current = this._buffer.shift();
                return true;
            }
            else if (this._index >= (this._input.length - 1)) {
                this.current = Number.NaN;
                return false;
            }
            else {
                var charCode = this._input.charCodeAt(++this._index);

                // "\r\n" -> "\n"
                //
                if ((charCode == 13) && (this._input.charCodeAt(this._index + 1) == 10)) {
                    charCode = 10;
                    this._index += 2;
                }

                if (charCode < 128) {
                    this.current = charCode;
                }
                else if ((charCode > 127) && (charCode < 2048)) {
                    this.current = (charCode >> 6) | 192;
                    this._buffer.push((charCode & 63) | 128);
                }
                else {
                    this.current = (charCode >> 12) | 224;
                    this._buffer.push(((charCode >> 6) & 63) | 128);
                    this._buffer.push((charCode & 63) | 128);
                }

                return true;
            }
        }
    };

    function Base64DecodeEnumerator(input){
        this._input = input;
        this._index = -1;
        this._buffer = [];
    }

    Base64DecodeEnumerator.prototype = {

        current: 64,

        moveNext: function(){
            if (this._buffer.length > 0) {
                this.current = this._buffer.shift();
                return true;
            }
            else if (this._index >= (this._input.length - 1)) {
                this.current = 64;
                return false;
            }
            else {
                var enc1 = Base64.codex.indexOf(this._input.charAt(++this._index));
                var enc2 = Base64.codex.indexOf(this._input.charAt(++this._index));
                var enc3 = Base64.codex.indexOf(this._input.charAt(++this._index));
                var enc4 = Base64.codex.indexOf(this._input.charAt(++this._index));

                var chr1 = (enc1 << 2) | (enc2 >> 4);
                var chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
                var chr3 = ((enc3 & 3) << 6) | enc4;

                this.current = chr1;

                if (enc3 != 64)
                    this._buffer.push(chr2);

                if (enc4 != 64)
                    this._buffer.push(chr3);

                return true;
            }
        }
    };




    // XNAT implementation of the Base64 functions above

    function Sub64(str){
        // set encodeInput and decodeInput to the same
        // thing initially - they will be modified later
        this.encodeInput = (str !== undef) ? (str + '') : '';
        this.decodeInput = (str !== undef) ? (str + '') : '';
        this.encoded = '';
        this.decoded = '';
        this.output = '';
    }

    Sub64.prototype.encode = function(str){
        this.encodeInput = (str !== undef) ? (str + '') : (this.encodeInput || '');
        this.output =
            this.encoded =
                this.encodeInput ?
                    Base64.encode(this.encodeInput) :
                    '';
        return this;
    };

    Sub64.prototype.decode = function(str){
        this.decodeInput = (str !== undef) ? (str + '') : (this.decodeInput || '');
        this.output =
            this.decoded =
                this.decodeInput ?
                    Base64.decode(this.decodeInput || this.encoded) :
                    '';
        return this;
    };

    // the .get() method retuns the result of the last encode OR decode
    // subInstance.encode().get() returns the ENCODED string
    // subInstance.get()  --  still returns ENCODED string
    // subInstance.decode().get() returns the DECODED string
    // subInstance.get()  --  now returns the DECODED string, since that was the last operation
    // 
    Sub64.prototype.get = function(which){
        return which ? this[which] : this.output;
    };



    // ONLY INTIALIZE (don't encode or decode yet)
    sub64.init = function(str){
        return new Sub64(str);
    };

    // immediately return instance with encoded string
    sub64.encode = function(str){
        return (new Sub64(str)).encode();
    };

    // immediately return instance with decoded string
    sub64.decode = function(str){
        return (new Sub64(str)).decode();
    };

    // deluxe encoding
    sub64.dlxEnc = function(str){
        var enc1 = sub64.encode(str).get();
        var cne1 = enc1.split('').reverse().join('');
        var enc2 = sub64.encode(cne1).get();
        return sub64.encode(enc2.split('').reverse().join(''));
    };

    // deluxe decoding -
    sub64.dlxDec = function(str){
        var dec1 = sub64.decode(str);
        var ced1 = dec1.get().split('').reverse().join('');
        var dec2 = sub64.decode(ced1).get().split('').reverse().join('');
        return sub64.decode(dec2)
    };


    /*
     * EXAMPLES
     */

    function sub64Examples(){

        // Initialize an instance and work with that:
        var encodeMe = XNAT.sub64.init('foo');
        console.log(encodeMe.encode().get());
        console.log(encodeMe.decode().get());

        // Immediately return encoded string using .get() method:
        var ENCODED = XNAT.sub64.encode('bar').get();
        console.log(ENCODED);

        // Immediately return decoded string using 'decoded' property:
        var DECODED = XNAT.sub64.decode(ENCODED).decoded;
        console.log(DECODED);

    }

    sub64.examples = sub64Examples;





    return (XNAT.util.sub64 = XNAT.sub64 = sub64);

}));

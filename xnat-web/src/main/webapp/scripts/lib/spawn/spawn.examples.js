/*!
 * List of options for spawn() function
 */

(function(spawn){

    if (!spawn) { return }

    var examples = {

        _about: {
            info:    'The spawn() function can be used to generate HTML elements on-the-fly, deeply nested if necessary',
            why:     'Why is using spawn() more convenient or efficient that just writing HTML?',
            because: 'The content that is spawned could contain JavaScript variables or generate content dynamically.'
        },

        minimal: {
            about:  'Creates an empty div with class "foo".',
            input:  ['div.foo'],
            output: '<div class="foo"></div>'
        },

        withText: {
            about:  'Creates a p element with id "fox" and text content.',
            input:  ['p#fox', 'The quick brown fox... did some stuff.'],
            output: '<p id="fox">The quick brown fox... did some stuff.</p>'
        },

        nested: {
            about:  'Creates nested spawned objects using an array of "Spawn Arrays" as the second argument ' +
                    '(or third if passing other options or attributes).',
            _more:  '"Spawn Arrays" are arrays containing arguments to pass to a recursively-called spawn() function.',
            input:  ['div.container', {
                style: 'padding: 10px'
            }, [
                ['p.foo', 'Text that is foo.'],
                ['p.bar', 'Other text that is bar.']
            ]],
            output:
                '<div class="container">' +
                    '<p class="foo">Text that is foo.</p>' +
                    '<p class="bar">Other text that is bar.</p>' +
                '</div>',
            why: 'Why is this more convenient or efficient that just writing HTML?',
            because: 'The content that is spawned may contain JavaScript variables or generate content dynamically.'
        },

        nestedDeep: {
            about:  'Nest Spawn Arrays within Spawn Arrays to create a deeply nested HTML hierarchy.',
            tip1:   'IDs and classes can be added using CSS selector syntax. Other attributes need to be added separately',
            tip2:   'Use a | (pipe) character to add other attributes to the element. Separate multiple attributes with a , (comma) or ; (semicolon)',
            input:  ['div#outer.stuff|title="The Outer Div."', [
                ['p.inner1', [
                    'Text content can go here and will be inserted before the next item in the array.',
                    ['p.inner2a', '<b>HTML content is also fine here in the .inner2 paragragraph.</b>'],
                    ['small.inner2b', 'Maybe you want some small text after the p.inner2 element.'],
                    ['script', 'console.log("Why would you want to add a script tag here? You can if you want. It will be executed immediately.")']
                ]]
            ]],
            output:
                '<div id="outer" class="stuff" title="The Outer Div">' +
                    '<p class="inner1">' +
                        'Text content can go here and will be inserted before the next item in the array.' +
                        '<p class="inner2a"><b>HTML content is also fine here in the .inner2 paragragraph.</b></p>' +
                        '<small class="inner2b">Maybe you want some small text after the p.inner2 element.</small>' +
                        // '<script>console.log("Why would you want to add a script tag here? You can if you want. It will be executed immediately.")</script>' +
                    '</p>' +
                '</div>'
        },

        configObj1: {
            about: 'You can pass an object as the second argument to add element properties, attributes, event handlers, and jQuery methods.',
            input: ['button', {
                type: 'button',
                name: 'one-button',
                html: 'The Button', // 'html' is a shortcut for the 'innerHTML' property
                onclick: function(){ alert('You clicked ' + this.innerHTML + '.')}, // in this case, the this keyword references the element being spawned
                $: { // the $ property attaches jQuery methods to the spawned element
                    addClass: 'a-button',
                    click: function(){ alert('A second click event handler was added with jQuery.') }
                }
            }]
        },

        configObj2: {
            about: 'This demonstrates calling a jQuery method more than once by using an array instead of an object for the $ property.',
            input: ['a.clicker.link', {
                html: '<br>Going nowhere.</br>',
                href: '/take/me/somewhere',
                $: [
                    ['click', function(e){ e.preventDefault(); alert("Don't go there."); }],
                    ['click', function(e){ e.preventDefault(); alert("I didn't."); }]
                ]
            }]
        }

    };

    function isPlainObject(obj){
        return Object.prototype.toString.call(obj) === '[object Object]';
    }

    // execute spawn() on each example
    // and add it to the 'spawned' property
    function createSpawn(){
        var spawned = [];
        forOwn(examples, function(item, obj){
            if (/^[_0-9]/.test(item)) return; // skip array items and objects with a leading underscore
            if (!isPlainObject(obj)) return;
            obj.spawned = spawn(obj.input);
            obj.htmlOut = obj.spawned.outerHTML;
            spawned.push(obj.spawned);
        });
        return spawned;
    }

    // save an array of spawned elements
    examples.spawned = createSpawn();

    // spawn.examples.createSpawn();
    examples.createSpawn = function(){
        return createSpawn();
    };

    spawn.examples = examples;

})(window.spawn);


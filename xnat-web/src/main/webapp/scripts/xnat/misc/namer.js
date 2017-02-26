/*!
 * Bulk-create hipster usernames
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

    var UNDEF, namer,
            firstNames, lastNames,
            usernameList, nameList;

    XNAT.misc =
            getObject(XNAT.misc || {});

    XNAT.misc.namer = namer =
            getObject(XNAT.misc.namer || {});

    function randomFromArray(arr){
        return arr[Math.floor(Math.random() * (arr.length))]
    }

    function randomString(prefix, chars){
        var pre = prefix !== UNDEF ? prefix.replace(/[\W\s]+/g, '') : 'x';
        return pre + (Math.random() + 1).toString(36).substr(2, chars || 8);
    }

    namer.firstNames = firstNames = [
        'Dorie', 'Trey', 'Pothier', 'Stops', 'Lulu', 'Kelvin', 'Thanh', 'Lashanda', 'Morga', 'Palomares', 'Ilse',
        'Masako', 'Marquis', 'Winona', 'Lyn', 'Honyumptewa', 'Clay', 'Clement', 'Dee', 'Elly', 'Scottie', 'Emelia',
        'Alton', 'Fabian', 'Carson', 'Darrin', 'Ashleigh', 'Lizabeth', 'Jeffry', 'Jamaal', 'Redus', 'Bernie',
        'Gabriel', 'Robbin', 'Latonia', 'Xavier', 'Orlando', 'Vergie', 'Lua', 'Buhrman', 'Melonie', 'Krysta',
        'Frederic', 'Michel', 'Costillo', 'Latricia', 'Knobloch', 'Kira', 'Wilbur', 'Hettie', 'Mary', 'Emiline',
        'Jamaal', 'Haufler', 'Meta', 'Dave', 'Lavera', 'Andres', 'Kip', 'Elenor', 'Meryl', 'Devon', 'Johnathon',
        'Tawana', 'Alphonso', 'Yan', 'Violeta', 'Witchey', 'Hiroko', 'Marinaro', 'Tam', 'Quintyne', 'Edmund', 'Cyrus',
        'Milroy', 'Andrea', 'Kenny', 'Ismael', 'Dorotha', 'Mikki', 'Linette', 'Ulysses', 'Rebbeca', 'Delma',
        'Hortensia', 'Kempner', 'Reina', 'Justin', 'Erik', 'Teressa', 'Marbury', 'Antwan', 'Wilmer', 'Swindler',
        'Thiemann', 'Branda', 'Luis', 'Daryl', 'Weathersbee', 'Robby', 'Jarrett', 'Grover', 'Will', 'Bob', 'Jim',
        'Fred', 'George', 'Charlotte', 'Benett'
    ];

    namer.lastNames = lastNames = [
        'Smith', 'Johnson', 'Robertson', 'Fredrickson', 'Obama', 'Biden', 'Clinton', 'Sanders', 'Carter',
        'Williams', 'Jones', 'Miller', 'Davis', 'Wilson', 'Anderson', 'Thomas', 'Moore', 'Newman', 'Thompson',
        'Lee', 'Harris', 'Clark', 'Lewis', 'Robinson', 'Walker', 'Hall', 'Young', 'Perez', 'Sanchez', 'King',
        'Green', 'Brown', 'White', 'Black', 'Baker', 'Adams', 'Adamson', 'Franklin', 'Morris', 'Murphy', 'Cook',
        'Horton', 'Myers', 'Howard', 'Ward', 'Bell', 'Long', 'Ross', 'Ortiz', 'Jenkins'
    ];

    // store a list of unique usernames
    namer.usernameList = usernameList = [];

    // store a list of generated names in 'firstName.lastName' format
    namer.nameList = nameList = [];

    /**
     * Generate a SINGLE user object with a randomized string added to the username
     * @param [firsts] {Array} - array of firstNames
     * @param [lasts] {Array} - array of lastNames
     * @param [randomizer] {Boolean} - add random string to username?
     * @returns {Object} - returns a single user object
     */
    function randomUser(firsts, lasts, randomizer){
        var firstName = randomFromArray(firsts || firstNames);
        var lastName = randomFromArray(lasts || lastNames);
        var username = randomizer ? randomString(firstName) : firstName + '.' + lastName;
        return {
            username: username.toLowerCase(),
            firstName: firstName,
            lastName: lastName
        }
    }

    namer.randomUser = randomUser;

    /**
     * Create a single UNIQUE user object
     * @param [firsts] {Array} - optional array of first names
     * @param [lasts] {Array} - optional array of last names
     * @param [randomizer] {Boolean} - add random string to username?
     * @returns {Object} - returns unique username object
     */
    namer.unique = function(firsts, lasts, randomizer){
        var nameTemp = '';

        function createUnique(){
            var nameObj = randomUser(firsts, lasts, randomizer);
            nameTemp = (nameObj.firstName + '.' + nameObj.lastName).toLowerCase();
            if (nameList.indexOf(nameTemp) === -1) {
                nameList.push(nameTemp);
                usernameList.push(nameObj.username);
                return nameTemp;
            }
            // call again if not unique
            createUnique();
        }

        return createUnique();
    };

    /**
     * Generate an array of username objects with firstName, lastName, username properties
     * @param [count] {Number} - how many user objects will be created
     * @param [firsts] {Array|null} - array of first names (or null to use default 'firstNames' array
     * @param [lasts] {Array|null} - array of last names (or null to use default 'lastNames' array
     * @param [randomizer] {Boolean|Function} - add a random string to output values?
     * @returns {Array} - returns array of unique user objects
     */
    namer.generate = function(count, firsts, lasts, randomizer){
        var users = new Array(count = +count || 20);
        var nameStore = []; // store an array of 'firstName.lastName' strings
        var i = 0;
        var nameTemp = '';
        var nameObj = {};

        function hasName(){
            return nameStore.indexOf(nameTemp) > -1
        }

        while (i < count && i < 1000) {
            nameObj = randomUser(firsts, lasts, randomizer);
            nameTemp = (nameObj.firstName + '.' + nameObj.lastName).toLowerCase();
            if (!hasName()) {
                users[i] = nameObj;
                nameStore.push(nameTemp);
                i += 1;
            }
        }
        return users;
    };

    var alternator = 1;

    /**
     * Object model for user data
     * @param usrObj {Object} - existing user data object
     * @param [opts] {Object} - generator options
     * @constructor
     */
    function UserData(usrObj, opts){
        usrObj = usrObj || {};
        opts = opts || {};
        var optsModel = {
            email: 'bob+USERNAME@gmail.com', // base email - USERNAME is replaced
            password: 'bogus', // default password
            verified: 1, // verify every user
            enabled: 4 // enable every 4th user
        };
        var tempUser = randomUser(null, null, opts.random);
        this.username = usrObj.username || tempUser.username;
        this.firstName = usrObj.firstName || tempUser.firstName;
        this.lastName = usrObj.lastName || tempUser.lastName;
        this.email = usrObj.email ||
        opts.email ? opts.email.split('USERNAME')
                         .join(this.username.replace(/[\W\s_.-]+/g, '')) : (this.username + '@xnatdev.xnat.org');
        this.password = usrObj.password || opts.password || '';
        this.verified =
                usrObj.verified || opts.verified ? (alternator % opts.verified === 0) : false;
        this.enabled =
                usrObj.enabled || opts.enabled ? (alternator % opts.enabled === 0) : false;
        // coerce to strings
        this.verified = (this.verified + '');
        this.enabled = (this.enabled + '');
        alternator += 1;
    }

    /**
     * Submit user data to create new users (does not check for uniqueness)
     * @param userList {Array} - required array of user data objects
     * @param [opts] {Object} - optional AJAX config object
     * @param [callback] {Function} - optional function to call after final user is created
     */
    namer.postUsers = function(userList, opts, callback){
        var i = 0;
        var count = userList.length;
        alternator = 1; // reset the alternator
        opts = opts || {};
        callback = callback || function(){};
        function doPost(){
            // UserData constructor ensures we have required fields
            var userData = new UserData(userList[i], opts);
            userData = JSON.stringify(userData);
            // console.log(userData);
            XNAT.admin.usersGroups
                .userData()
                .createProfile(userData)
                .done(function(){
                    if (count > i++) {
                        // throttle to 2 requests/sec
                        setTimeout(doPost, opts.delay || 500);
                    }
                    else {
                        try {
                            callback.call(this, userData, userList)
                        }
                        catch(e) {
                            console.log(e);
                        }
                    }
                });
        }

        doPost();
    };

    /**
     * Submit only UNIQUE names ('firstname.lastname' not 'username')
     * @param [count] {Number} - number of new users to create
     * @param [userList] {Array} - optional array of user objects to submit
     * @param [opts] {Object} - other parameters
     * @param [callback] {Function} - optional function to call after final user is created
     */
    namer.postUniqueNames = function(count, userList, opts, callback){
        // retrieve list of existing users first
        var uniqueUsers = [];
        count = count != null ? +count : 20;
        XNAT.admin.usersGroups
            .userData()
            .allProfiles()
            .done(function(profiles){
                // add existing XNAT users to nameList array
                // so we don't create duplicates
                profiles.forEach(function(profile){
                    var nameTemp = (profile.firstName + '.' + profile.lastName).toLowerCase();
                    if (nameList.indexOf(nameTemp) === -1) {
                        nameList.push(nameTemp);
                        usernameList.push(profile.username);
                    }
                });
                if (!Array.isArray(userList)) {
                    // if userList is not an array,
                    // CREATE A NEW ONE
                    userList = namer.generate(count);
                }
                // count = userList.length;
                userList.forEach(function(user){
                    var nameTemp = (user.firstName + '.' + user.lastName).toLowerCase();
                    if (nameList.indexOf(nameTemp) === -1) {
                        // save unique users from userList
                        uniqueUsers.push(user);
                        nameList.push(nameTemp);
                        usernameList.push(user.username);
                    }
                });
                namer.postUsers(uniqueUsers, opts, callback);
            });
    };
    // usage:
    // XNAT.namer.postUniqueNames(12, null, {
    //     random: true,
    //     email: 'email+USERNAME@gmail.com',
    //     password: 'bogus',
    //     verified: 1,
    //     enabled: 3,
    //     delay: 500 // ms delay between requests
    // });

    namer.postUniqueUsernames = function(count, userList, opts){

    };

    // this script has loaded
    namer.loaded = true;

    return XNAT.misc.namer = XNAT.namer = namer;

}));

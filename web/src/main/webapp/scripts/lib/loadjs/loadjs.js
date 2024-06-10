(function(win, doc) {
  var head = doc.head,
      devnull = function() {},
      bundleIdCache = {},
      bundleResultCache = {},
      bundleCallbackQueue = {};
  
  
  /**
   * Subscribe to bundle load event.
   * @param {string[]} bundleIds - Bundle ids
   * @param {Function} callbackFn - The callback function
   */
  function subscribe(bundleIds, callbackFn) {
    // listify
    bundleIds = bundleIds.push ? bundleIds : [bundleIds];

    var depsNotFound = [],
        i = bundleIds.length,
        numWaiting = i,
        fn, bundleId, r, q;

    // define callback function
    fn = function(bundleId, pathsNotFound) {
      if (pathsNotFound.length) depsNotFound.push(bundleId);
      
      numWaiting -= 1;
      if (numWaiting === 0) callbackFn(depsNotFound);
    };
    
    // register callback
    while (i--) {
      bundleId = bundleIds[i];
      
      // execute callback if in result cache
      r = bundleResultCache[bundleId];
      if (r) {
        fn(bundleId, r);
        continue;
      }
      
      // add to callback queue
      q = bundleCallbackQueue[bundleId] = bundleCallbackQueue[bundleId] || [];
      q.push(fn);
    }
  }


  /**
   * Publish bundle load event.
   * @param {string} bundleId - Bundle id
   * @param {string[]} pathsNotFound - List of files not found
   */
  function publish(bundleId, pathsNotFound) {
    // exit if id isn't defined
    if (!bundleId) return;

    var q = bundleCallbackQueue[bundleId];

    // cache result
    bundleResultCache[bundleId] = pathsNotFound;
      
    // exit if queue is empty
    if (!q) return;
    
    // empty callback queue
    while (q.length) {
      q[0](bundleId, pathsNotFound);
      q.splice(0, 1);
    }
  }


  /**
   * Load individual JavaScript file.
   * @param {string} path - The file path
   * @param {Function} callbackFn - The callback function
   */
  function loadScript(path, callbackFn) {
    var s = doc.createElement('script');
    s.src = path;

    s.onload = s.onerror = function(ev) {
      // remove script
      var p = s.parentNode;
      if (p) p.removeChild(s);

      // de-reference script
      s = null;

      // execute callback
      callbackFn(path, ev.type);
    };

    // add to document
    head.appendChild(s);
  }


  /**
   * Load multiple JavaScript files.
   * @param {string[]} paths - The file paths
   * @param {Function} callbackFn - The callback function
   */
  function loadScripts(paths, callbackFn) {
    // listify paths
    paths = paths.push ? paths : [paths];

    var i = paths.length, numWaiting = i, pathsNotFound = [], fn;

    // define callback function
    fn = function(path, result) {
      if (result === 'error') pathsNotFound.push(path);

      numWaiting -= 1;
      if (numWaiting === 0) callbackFn(pathsNotFound);
    };
    
    // load scripts
    while (i--) loadScript(paths[i], fn);
  }


  /**
   * Initiate script load and register bundle.
   * @param {(string|string[])} paths - The file paths
   * @param {(string|Function)} [arg1] - The bundleId or success callback
   * @param {Function} [arg2] - The success or fail callback
   * @param {Function} [arg3] - The fail callback
   */
  function loadjs(paths, arg1, arg2, arg3) {
    var bundleId, successFn, failFn, errorMsg;

    // bundleId
    if (arg1 && !arg1.call) bundleId = arg1;

    // successFn, failFn
    if (bundleId) successFn = arg2;
    else successFn = arg1;

    // failFn
    if (bundleId) failFn = arg3;
    else failFn = arg2;

    // throw error if bundle is already defined
    if (bundleId) {
      if (bundleId in bundleIdCache) {
        errorMsg = 'LoadJS: Bundle already defined';
          // prefer to show an error message but not throw an error
          if (window.console && window.console.error) {
            window.console.error(errorMsg)
          }
          else if (window.console && window.console.log){
            window.console.log(errorMsg)
          }
          else {
            throw new Error(errorMsg);
          }
      } else {
        bundleIdCache[bundleId] = true;
      }
    }
    
    // load scripts
    win.setTimeout(function() {
      loadScripts(paths, function(pathsNotFound) {
        if (pathsNotFound.length) (failFn || devnull)(pathsNotFound);
        else (successFn || devnull)();

        // publish bundle load event
        publish(bundleId, pathsNotFound);
      });
    }, 0);  // fires after window 'load' event
  }


  /**
   * Execute callbacks when dependencies have been satisfied.
   * @param {(string|string[])} deps - List of bundle ids
   * @param {Function} [successFn] - Success callback
   * @param {Function} [failFn] - Fail callback
   */
  loadjs.ready = function (deps, successFn, failFn) {
    // subscribe to bundle load event
    subscribe(deps, function(depsNotFound) {
      // execute callbacks
      if (depsNotFound.length) (failFn || devnull)(depsNotFound);
      else (successFn || devnull)();
    });

    return loadjs;
  };


  /**
   * Manually satisfy bundle dependencies.
   * @param {string} bundleId - The bundle id
   */
  loadjs.done = function done(bundleId) {
    publish(bundleId, []);
  };


  /**
   * Load multiple scripts sequentially (in series).
   * Shortcut for nested loadjs() calls.
   * @param {string[]} scripts - Array of loadjs setup arrays
   * @param {Function} [callback] - Callback function
   */
  loadjs.series = function(/* script1, [script2, etc...,] callback */){

    var len = 0, scripts = [],
        argLen = arguments.length,
        lastArg = arguments[argLen-1],
        callback = null;

    // convert arguments to actual array
    while (len < argLen){
      scripts = scripts.concat([arguments[len]]);
      len++;
    }
    // len should now equal scripts.length
    if (typeof lastArg == 'function'){
      callback = lastArg;
    }
    if (callback && argLen > 1) {
      len = argLen - 1;
    }

    function doLoad(i){
      if (i === len) return;
      var script = scripts[i],
          name = script[0],
          url = script[1],
          fn = script[2]||null, // optional individual callback for this script
          next = i + 1;

      if (script.length === 1){
        url = name;
      }
      if (typeof script == 'string'){
        name = url = script;
      }
      // execute callback after last item
      if (next === len) {
        loadjs(url, callback);
      }
      else {
        loadjs(url, name);
        loadjs.ready(name, function(){
          doLoad(next);
          // does this item have its own callback?
          // var example = ['name', '/path/to/script.js', function(){ doStuffAfterThisScriptLoads() }];
          if (fn && typeof fn == 'function'){
            fn(name, url)
          }
        });
      }

    }
    // set it off
    doLoad(0);
  };


  // export
  win.loadjs = loadjs;
})(window, document);

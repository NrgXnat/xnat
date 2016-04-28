/*!
 * Spawn UI elements using the Spawner service
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

    var undefined,
        spawner = getObject(XNAT.spawner||{});

    function Spawner(obj){
        extend(true, this, obj);
        this.spawned = null;
        this.children = {};
    }

    Spawner.p = Spawner.prototype;

    Spawner.p.init = function(obj){

    };

    Spawner.p.render = function($container){
        $$($container).append(this.spawned);
        return this;
    };

    spawner.spawn = function(obj){
        return new Spawner(obj);
    };

    return XNAT.spawner = spawner;

}));

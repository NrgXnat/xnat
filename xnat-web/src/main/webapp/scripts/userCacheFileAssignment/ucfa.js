/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/userCacheFileAssignment/ucfa.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/17/13 2:15 PM
 */

//prep trees
XNAT.app.uca.tree1 = new YAHOO.widget.TreeView("srcTree",[XNAT.app.uca.src]);

XNAT.app.uca.tree2 = new YAHOO.widget.TreeView("destTree",[XNAT.app.uca.dest]);

//handle drag and drop
var Dom = YAHOO.util.Dom;
var Event = YAHOO.util.Event;
var DDM = YAHOO.util.DragDropMgr;

XNAT.app.uca.highlighted={
	nodes:new Array(),
	remove:function(_node){
		
	}
};

(function() {
XNAT.app.uca.DDApp = {
	rendered:false,
	renderTrees:function(){
		XNAT.app.uca.tree1.render();
		XNAT.app.uca.tree2.render();
	},
	cleanTree:function(){
		
	},
	findNode:function(ele){
		var match=XNAT.app.uca.tree1.getNodeByElement(ele);
		if(match==null){
			match=XNAT.app.uca.tree2.getNodeByElement(ele);	
			if(match!=null){
				return {"tree":XNAT.app.uca.tree2,"node":match};
			}
		}else{
			return {"tree":XNAT.app.uca.tree1,"node":match};
		}	
		
		return null;
	},
	makeDDTree:function(_node){
		new XNAT.app.uca.DDTree("ygtv" + _node.index);
	},
	makeDDTarget:function(_node){
		new YAHOO.util.DDTarget("ygtv" + _node.index);
	},
    defineSrcAndTargets:function(_parent){
    	var c=0;
	    for(var _ci=0;_ci<_parent.children.length;_ci++){
			var _child=_parent.children[_ci];
			
			if(_child.data.dest!=undefined){
				this.makeDDTarget(_child);
			}
			
			if(_child.hasChildren()){
				if(this.defineSrcAndTargets(_child)>0){
					if(_child.data.fpath!=undefined){
						this.makeDDTree(_child);
					}
					c++;
				}
			}else{
				if(_child.data.fpath!=undefined && _child.labelStyle!="icon-of"){
					this.makeDDTree(_child);
					Event.addListener(_child,"click",function(){
							if(XNAT.app.uca.highlighted.nodes.contains(this)){
								XNAT.app.uca.highlighted.remove(this);
								this.unhighlight();
							}else{
								XNAT.app.uca.highlighted.push(this);
								this.highlight();
							}
						}
					);
					c++;
				}
			}
		}
	    
	    return c;
    },
    init: function() {    	
		
    	this.renderTrees();
    	var n1=XNAT.app.uca.tree1.getRoot();
		var _filesLeft=this.defineSrcAndTargets(n1);
		if(_filesLeft==0){
			Dom.setStyle("leftTreeOuterContainer", "opacity", 0.40);
		}

    	var n2=XNAT.app.uca.tree2.getRoot();
		this.defineSrcAndTargets(n2);
    },
    buildResultArray: function(_parent,_ResultsArray) {
		if(_parent.data.dest!=undefined){
			var _path=_parent.data.dest;
		}
		
	    for(var _rmci=0;_rmci<_parent.children.length;_rmci++){
			var _child=_parent.children[_rmci];
									
			if(_child.data.fpath!=undefined){
				_ResultsArray.push({"dest":_path,"fpath":_child.data.fpath});
			}else if(_child.hasChildren()){
				this.buildResultArray(_child,_ResultsArray);
			}
		}
	    
	    return _ResultsArray;
    },
    retrieveResults:function(){
    	var n1=XNAT.app.uca.tree2.getRoot();
	    return this.buildResultArray(n1,new Array());
    }
    
};

XNAT.app.uca.DDTree = function(id, sGroup, config) {

    XNAT.app.uca.DDTree.superclass.constructor.call(this, id, sGroup, config);

    this.logger = this.logger || YAHOO;
    var el = this.getDragEl();
    Dom.setStyle(el, "opacity", 0.67); // The proxy is slightly transparent

    this.goingUp = false;
    this.lastY = 0;
};


//this implements the drag and drop... need to get the multi-select working
YAHOO.extend(XNAT.app.uca.DDTree, YAHOO.util.DDProxy, {
    startDrag: function(x, y) {
        this.logger.log(this.id + " startDrag");

        // make the proxy look like the source element
        var dragEl = this.getDragEl();
        var clickEl = this.getEl();
        Dom.setStyle(clickEl, "visibility", "hidden");

        dragEl.innerHTML = clickEl.innerHTML;

        Dom.setStyle(dragEl, "color", Dom.getStyle(clickEl, "color"));
        Dom.setStyle(dragEl, "backgroundColor", Dom.getStyle(clickEl, "backgroundColor"));
        Dom.setStyle(dragEl, "border", "2px solid gray");
    },

    endDrag: function(e) {

        var srcEl = this.getEl();
        var proxy = this.getDragEl();

        // Show the proxy element and animate it to the src element's location
        Dom.setStyle(proxy, "visibility", "");
        var a = new YAHOO.util.Motion( 
            proxy, { 
                points: { 
                    to: Dom.getXY(srcEl)
                }
            }, 
            0.2, 
            YAHOO.util.Easing.easeOut 
        )
        var proxyid = proxy.id;
        var thisid = this.id;

        // Hide the proxy and show the source element when finished with the animation
        a.onComplete.subscribe(function() {
                Dom.setStyle(proxyid, "visibility", "hidden");
                Dom.setStyle(thisid, "visibility", "");
            });
        a.animate();
    },

    onDragDrop: function(e, id) {

        // If there is one drop interaction, the li was dropped either on the list,
        // or it was dropped on the current location of the source element.
        if (DDM.interactionInfo.drop.length === 1) {

            // The position of the cursor at the time of the drop (YAHOO.util.Point)
            var pt = DDM.interactionInfo.point; 

            // The region occupied by the source element at the time of the drop
            var region = DDM.interactionInfo.sourceRegion; 

            // Check to see if we are over the source element's location.  We will
            // append to the bottom of the list once we are sure it was a drop in
            // the negative space (the area of the list without any list items)
            if (!region.intersect(pt)) {
                var destEl = Dom.get(id);
                var destDD = DDM.getDDById(id);
                var srcNode=XNAT.app.uca.DDApp.findNode(this);
                var destNode=XNAT.app.uca.DDApp.findNode(destEl);
                srcNode.tree.popNode(srcNode.node);
                srcNode.node.appendTo(destNode.node);
                destNode.node.expand();
                XNAT.app.uca.DDApp.init();
                destDD.isEmpty = false;
                DDM.refreshCache();
            }

        }
    },

    onDrag: function(e) {

        // Keep track of the direction of the drag for use during onDragOver
        var y = Event.getPageY(e);

        if (y < this.lastY) {
            this.goingUp = true;
        } else if (y > this.lastY) {
            this.goingUp = false;
        }

        this.lastY = y;
    },

    onDragOver: function(e, id) {
    
        var srcEl = this.getEl();
        var destEl = Dom.get(id);

        // We are only concerned with list items, we ignore the dragover
        // notifications for the list.
        if (destEl.nodeName.toLowerCase() == "li") {
            var orig_p = srcEl.parentNode;
            var p = destEl.parentNode;

            if (this.goingUp) {
                p.insertBefore(srcEl, destEl); // insert above
            } else {
                p.insertBefore(srcEl, destEl.nextSibling); // insert below
            }

            DDM.refreshCache();
        }
    }
});

Event.onDOMReady(XNAT.app.uca.DDApp.init, XNAT.app.uca.DDApp, true);

})();

XNAT.app.uca.moveSelected=function(){
	XNAT.app.uca.arrayToMove=XNAT.app.uca.DDApp.retrieveResults();
	
	openModalPanel("moving_files","Moving files...")
	
	XNAT.app.uca.moveNext();
}

XNAT.app.uca.moveNext=function(){
	if(XNAT.app.uca.arrayToMove!=undefined)
	{
		if(XNAT.app.uca.arrayToMove.length==0){
			XNAT.app.uca.moveFinished();
		}else{
			var entry=XNAT.app.uca.arrayToMove.pop();
			
			var params="src=" + XNAT.app.uca.userURI + entry.fpath + "&dest=" + entry.dest;

			openModalPanel("moving_files","Moving "+entry.fpath);
			
			YAHOO.util.Connect.asyncRequest('POST',serverRoot +'/REST/services/move-files?XNAT_CSRF=' + csrfToken,XNAT.app.uca.moveCallback,params,this);
		}
	}else{
		XNAT.app.uca.moveFinished();
	}
}

XNAT.app.uca.moveFinished=function(){
	closeModalPanel("moving_files");
	
	//route to some page.
}

//need better failure handling here.
XNAT.app.uca.moveCallback={
		success:function(){XNAT.app.uca.moveNext();},
		failure:function(){closeModalPanel("moving_files");alert("Failed to move files.");},
    cache:false, // Turn off caching for IE
		scope:this
}



var move_button=new YAHOO.widget.Button("move_files_button");
move_button.on("click", XNAT.app.uca.moveSelected); 

var reset_button=new YAHOO.widget.Button("reset_button");
reset_button.on("click", window.location.reload); 
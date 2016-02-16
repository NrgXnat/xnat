
/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/yui/build/treeview/taskNode.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/4/13 9:51 AM
 */
YAHOO.widget.TaskNode = function(oData, oParent, expanded, checked) {
	YAHOO.widget.TaskNode.superclass.constructor.call(this,oData,oParent,expanded);
    this.setUpCheck(checked || oData.checked);

};

YAHOO.extend(YAHOO.widget.TaskNode, YAHOO.widget.TextNode, {

    /**
     * True if checkstate is 1 (some children checked) or 2 (all children checked),
     * false if 0.
     * @type boolean
     */
    checked: false,

    /**
     * checkState
     * 0=unchecked, 1=some children checked, 2=all children checked
     * @type int
     */
    checkState: 0,

	/**
     * The node type
     * @property _type
     * @private
     * @type string
     * @default "TextNode"
     */
    _type: "TaskNode",
	
	taskNodeParentChange: function() {
        //this.updateParent();
    },
	
    setUpCheck: function(checked) {
        // if this node is checked by default, run the check code to update
        // the parent's display state
        if (checked && checked === true) {
            this.check();
        // otherwise the parent needs to be updated only if its checkstate 
        // needs to change from fully selected to partially selected
        } else if (this.parent && 2 === this.parent.checkState) {
             this.updateParent();
        }

        // set up the custom event on the tree for checkClick
        /**
         * Custom event that is fired when the check box is clicked.  The
         * custom event is defined on the tree instance, so there is a single
         * event that handles all nodes in the tree.  The node clicked is 
         * provided as an argument.  Note, your custom node implentation can
         * implement its own node specific events this way.
         *
         * @event checkClick
         * @for YAHOO.widget.TreeView
         * @param {YAHOO.widget.Node} node the node clicked
         */
        if (this.tree && !this.tree.hasEvent("checkClick")) {
            this.tree.createEvent("checkClick", this.tree);
        }

		this.tree.subscribe('clickEvent',this.checkClick);
        this.subscribe("parentChange", this.taskNodeParentChange);


    },

    /**
     * The id of the check element
     * @for YAHOO.widget.TaskNode
     * @type string
     */
    getCheckElId: function() { 
        return "ygtvcheck" + this.index; 
    },

    /**
     * Returns the check box element
     * @return the check html element (img)
     */
    getCheckEl: function() { 
        return document.getElementById(this.getCheckElId()); 
    },

    /**
     * The style of the check element, derived from its current state
     * @return {string} the css style for the current check state
     */
    getCheckStyle: function() { 
        return "ygtvcheck" + this.checkState;
    },


   /**
     * Invoked when the user clicks the check box
     */
    checkClick: function(oArgs) { 
		var node = oArgs.node;
		var target = YAHOO.util.Event.getTarget(oArgs.event);
		if (YAHOO.util.Dom.hasClass(target,'ygtvspacer')) {
	        if (node.checkState === 0) {
	            node.check();
	        } else {
	            node.uncheck();
	        }

	        node.onCheckClick(node);
	        this.fireEvent("checkClick", node);
		    return false;
		}
    },

    /**
     * Override to get the check click event
     */
    onCheckClick: function() { 
        
    },

    /**
     * Refresh the state of this node's parent, and cascade up.
     */
    updateParent: function() { 
        var p = this.parent;

        if (!p || !p.updateParent) {
            return;
        }

        var somethingChecked = false;
        var somethingNotChecked = false;

        for (var i=0, l=p.children.length;i<l;i=i+1) {

            var n = p.children[i];

            if ("checked" in n) {
                if (n.checked) {
                    somethingChecked = true;
                    // checkState will be 1 if the child node has unchecked children
                    if (n.checkState === 1) {
                        somethingNotChecked = true;
                    }
                } else {
                    somethingNotChecked = true;
                }
            }
        }

        if (p.checkState>0 && somethingChecked) {
            p.setCheckState(2);
        } else {
            p.setCheckState(0);
        }

        p.updateCheckHtml();
        p.updateParent();
    },

    /**
     * If the node has been rendered, update the html to reflect the current
     * state of the node.
     */
    updateCheckHtml: function() { 
        if (this.parent && this.parent.childrenRendered && this.data.canRead === true) {
            this.getCheckEl().className = this.getCheckStyle();
        }
    },

    /**
     * Updates the state.  The checked property is true if the state is 1 or 2
     * 
     * @param the new check state
     */
    setCheckState: function(state) { 
        this.checkState = state;
        this.checked = (state > 0);
    },

    /**
     * Check this node
     */
    check: function() { 
        this.setCheckState(2);
        for (var i=0, l=this.children.length; i<l; i=i+1) {
            var c = this.children[i];
            if (c.check) {
                c.check();
            }
        }

        this.updateCheckHtml();
        this.updateParent();
    },

    /**
     * Uncheck this node
     */
    uncheck: function() { 
        this.setCheckState(0);
        for (var i=0, l=this.children.length; i<l; i=i+1) {
            var c = this.children[i];
            if (c.uncheck) {
                c.uncheck();
            }
        }

        this.updateCheckHtml();
        this.updateParent();
    },
    // Overrides YAHOO.widget.TextNode

    getContentHtml: function() {                                                                                                                                           
        var sb = [];
        if(this.data.canRead){
           // Only add if the canRead flag is true. Added for XNAT-2408.
           sb[sb.length] = '<td';                                                                                                                                             
           sb[sb.length] = ' id="' + this.getCheckElId() + '"';                                                                                                               
           sb[sb.length] = ' class="' + this.getCheckStyle() + '"';                                                                                                           
           sb[sb.length] = '>';                                                                                                                                               
           sb[sb.length] = '<div class="ygtvspacer"></div></td>';
        }
                                                                                                                                                                           
        sb[sb.length] = '<td><span';                                                                                                                                       
        sb[sb.length] = ' id="' + this.labelElId + '"';                                                                                                                    
        if (this.title) {                                                                                                                                                  
            sb[sb.length] = ' title="' + this.title + '"';                                                                                                                 
        }                                                                                                                                                                  
        sb[sb.length] = ' class="' + this.labelStyle  + '"';                                                                                                               
        sb[sb.length] = ' >';                                                                                                                                              
        sb[sb.length] = this.label;                                                                                                                                        
        sb[sb.length] = '</span></td>';                                                                                                                                    
        return sb.join("");                                                                                                                                                
    },
    
    getCheckedNodes: function(){
    	var _a=new Array();
    	for(this.gcnC=0;this.gcnC<this.children.length;this.gcnC++){
    		if(this.children[this.gcnC].hasChildren() && this.children[this.gcnC].getCheckedNodes!=undefined){
    			_a=_a.concat(this.children[this.gcnC].getCheckedNodes());
    		}
    		if(this.children[this.gcnC].checkState!=undefined && this.children[this.gcnC].checkState==2){
    			_a.push(this.children[this.gcnC]);
    		}
    	}
    	return _a;
    }
});


YAHOO.widget.TaskTreeView = function(_id) {
	YAHOO.widget.TaskTreeView.superclass.constructor.call(this,_id);
};

YAHOO.extend(YAHOO.widget.TaskTreeView, YAHOO.widget.TreeView, {
    buildTreeFromMarkup: function (id) {
    	var Dom = YAHOO.util.Dom,
		Event = YAHOO.util.Event,
		Lang = YAHOO.lang,
		Widget = YAHOO.widget;
		var build = function (parent,markup) {
			var el, node, child, text;
			for (el = Dom.getFirstChild(markup); el; el = Dom.getNextSibling(el)) {
				if (el.nodeType == 1) {
					switch (el.tagName.toUpperCase()) {
						case 'LI':
							for (child = el.firstChild; child; child = child.nextSibling) {
								if (child.nodeType == 3) {
									text = Lang.trim(child.nodeValue);
									if (text.length) {
										var config = {
											label:text,
				                            expanded: Dom.hasClass(el,'expanded'),
				                            title: el.title || el.alt || null,
				                            className: Lang.trim(el.className.replace(/\bexpanded\b/,'')) || null
				                        };
			                            var yuiConfig = el.getAttribute('yuiConfig');
			                            if (yuiConfig) {
			                                yuiConfig = eval("(" + yuiConfig +")");
			                                if(yuiConfig.checked!=undefined)config.checked=yuiConfig.checked;
			                                if(yuiConfig.type!=undefined)config.type=yuiConfig.type;
			                                if(yuiConfig.expanded!=undefined)config.expanded=yuiConfig.expanded;
			                                if(yuiConfig.ru!=undefined)config.ru=yuiConfig.ru;
			                                if(yuiConfig.redirectHome!=undefined)config.redirectHome=yuiConfig.redirectHome;
			                                
			                                if(yuiConfig.canRead!=undefined)config.canRead=yuiConfig.canRead;
			                                if(yuiConfig.xsiType!=undefined)config.xsiType=yuiConfig.xsiType;
			                                if(yuiConfig.date!=undefined)config.date=yuiConfig.date;
			                                if(yuiConfig.primary_label!=undefined)config.primary_label=yuiConfig.primary_label;
			                            }
										
										if(config.type!=undefined){
											node = new Widget.TaskNode(config, parent, config.expanded);
										}else{
											node = new Widget.TextNode(text, parent, false);
										}
									}
								} else {
									switch (child.tagName.toUpperCase()) {
										case 'UL':
										case 'OL':
											build(node,child);
											break;
										case 'A':
											node = new Widget.TextNode({
												label:child.innerHTML,
												href: child.href,
												target:child.target,
												title:child.title ||child.alt
											},parent,false);
											break;
										default:
											node = new Widget.HTMLNode(child.parentNode.innerHTML, parent, false, true);
											break;
									}
								}
							}
							break;
						case 'UL':
						case 'OL':
							build(node, el);
							break;
					}
				}
			}
		
		};
		var markup = Dom.getChildrenBy(Dom.get(id),function (el) { 
			var tag = el.tagName.toUpperCase();
			return  tag == 'UL' || tag == 'OL';
		});
		if (markup.length) {
			build(this.root, markup[0]);
		} else {
		}
	},
    getCheckedNodes: function(){
    	var root=this.getRoot();
    	var _a=new Array();
    	for(root.gcnC=0;root.gcnC<root.children.length;root.gcnC++){
    		if(root.children[root.gcnC].hasChildren() && root.children[root.gcnC].getCheckedNodes!=undefined){
    			_a=_a.concat(root.children[root.gcnC].getCheckedNodes());
    		}
    		if(root.children[root.gcnC].checkState!=undefined && root.children[root.gcnC].checkState==2){
    			_a.push(root.children[root.gcnC]);
    		}
    	}
    	return _a;
    }
});

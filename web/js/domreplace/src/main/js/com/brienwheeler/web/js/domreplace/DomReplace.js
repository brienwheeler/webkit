/*global YUI */

YUI.add("com.brienwheeler.web.DomReplace", function(Y){

	Y.namespace("com.brienwheeler.web");
	
	/*
	 * DomReplace
	 */
	Y.com.brienwheeler.web.DomReplace = function(domReplaceRecords)
	{
		this.domReplaceRecords = domReplaceRecords;
	};

	Y.com.brienwheeler.web.DomReplace.prototype = {
		constructor : Y.com.brienwheeler.web.DomReplace,
		
		domReplaceRecords : undefined,
		ioInProgress : null,
		
		ioSuccess : function(id, ioreq, args)
		{
			var context, node;
			
			/* YUI library overrides the "this" object with context object pased from handleClick.
			 * Rename for clarity. */
			context = this;

			node = Y.one("#" + context.drr.divId);
			node.purge(true);
			node.setContent(ioreq.responseText);
			context.dr.linkEventHandlers(node);
		},
		
		ioFailure : function(id, ioreq, args)
		{
			alert("Error");
		},
		
		ioEnd : function(id, ioreq, args)
		{
			/* YUI library overrides the "this" object with context object pased from handleClick.
			 * Rename for clarity. */
			var context = this;

			context.dr.ioInProgress = null;
		},
		
		handleClick : function(e)
		{
			/* YUI library overrides the "this" object with context object passed from linkEventHandlers.
			 * Rename for clarity. */
			var context = this;
			
			e.preventDefault();
			
			if (context.dr.ioInProgress !== null) {
				context.dr.ioInProgress.abort();
			}

			context.dr.ioInProgress = Y.io(context.drr.url, {
				context : context,
				on : {
					success : Y.com.brienwheeler.web.DomReplace.prototype.ioSuccess,
					failure : Y.com.brienwheeler.web.DomReplace.prototype.ioFailure,
					end : Y.com.brienwheeler.web.DomReplace.prototype.ioEnd
				}
			});
		},
		
		linkEventHandlers : function(node)
		{
			var i, len, drr, selector, nodes;
			
			for (i=0, len=this.domReplaceRecords.length; i<len; i++)
			{
				drr = this.domReplaceRecords[i];
				selector = "a." + drr.clazz;
				nodes = Y.Lang.isUndefined(node) ? Y.all(selector) : node.all(selector);
				nodes.on("click", Y.com.brienwheeler.web.DomReplace.prototype.handleClick, 
						{ dr: this, drr : drr });
			}
		}
	};
	
	/*
	 * DomReplaceRecord
	 */
	Y.com.brienwheeler.web.DomReplaceRecord = function(clazz, divId, url)
	{
		this.clazz = clazz;
		this.divId = divId;
		this.url = url;
	};

	Y.com.brienwheeler.web.DomReplaceRecord.prototype = {
		constructor : Y.com.brienwheeler.web.DomReplaceRecord,
		
		clazz : undefined,
		divId : undefined,
		url : undefined
	};
	
},
'0.1',
{
	requires: ["node", "io"]
});

/**
 * dojo 相关的所有用户函数
 */

/**
 * 预先装载的dijit
 */
dojo.require("dojo.parser");
dojo.require("dijit.form.DateTextBox");
dojo.require("dijit.form.TimeTextBox");
dojo.require("dijit.form.TextBox");
dojo.require("dijit.form.SimpleTextarea");
dojo.require("dijit.form.Button");
dojo.require("dijit.form.FilteringSelect");
dojo.require("dijit.form.CheckBox");
dojo.require("dijit.form.DropDownButton");

dojo.require("dijit.Editor");
dojo.require("dijit.InlineEditBox");
dojo.require("dijit.TooltipDialog");
dojo.require("dijit.Dialog");
dojo.require("dijit.TitlePane");

dojo.require("dijit.layout.TabContainer");
dojo.require("dijit.layout.ContentPane");
dojo.require("dijit.layout.BorderContainer");

dojo.require("dijit.MenuBar");
dojo.require("dijit.Menu");
dojo.require("dijit.MenuItem");
dojo.require("dijit.PopupMenuBarItem");
dojo.require("dijit.PopupMenuItem");

dojo.require("dijit.Tree");
dojo.require("dojo.data.ItemFileReadStore");

/**
 * 用于：wr3.clj.dojo/index
 */
function onload_test1() {
	dojo.ready(function() {
		//alert(dojo.version);
		var fadeOutButton = dojo.byId("fadeOutButton");
        var fadeInButton = dojo.byId("fadeInButton");
        var fadeTarget = dojo.byId("fadeTarget");

		dojo.connect(fadeOutButton, "onclick", function(evt){
			dojo.fadeOut({ node: fadeTarget }).play();
		});
		dojo.connect(fadeInButton, "onclick", function(evt){
			dojo.fadeIn({ node: fadeTarget }).play();
		});
		console.log('----- loaded -----'); 
	});
}
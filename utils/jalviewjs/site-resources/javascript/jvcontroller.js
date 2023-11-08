/**
 * Jalview - A Sequence Alignment Editor and Viewer ($$Version-Rel$$)
 * Copyright (C) $$Year-Rel$$ The Jalview Authors
 * 
 * This file is part of Jalview.
 * 
 * Jalview is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  
 * Jalview is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of 
 * the GNU General Public License along with Jalview.  If not, see <http://www.gnu.org/licenses/>.
 * The Jalview Authors are detailed in the 'AUTHORS' file.
 */


$(document).ready(function() {
	processAllAppletElements();
});


    
    var currentPage = "applets";

    function processAllAppletElements() {
    	var apps = document.getElementsByTagName("applet");
    	for (var i = apps.length; --i >= 0;) {
    		processAppletElement(apps[i]);
    	}
    }
       
    var jvid = 0;

    function testBtn(e) {
    	var element = e.target.appletElement;
    	var text = element.outerHTML;
    	
    	//var
    	var Info = self.JalviewInfo || {
  code: null,
  main: "jalview.bin.Jalview",
  core: "NONE",
	
	
	width: 850,
	height: 550,
  readyFunction: null,
	serverURL: 'https://chemapps.stolaf.edu/jmol/jsmol/php/jsmol.php',
	j2sPath: 'swingjs/j2s',
//	console:'sysoutdiv',
	allowjavascript: true
}
    	
var id = "JVApplet" + jvid++;
var args = text.replace(/[\n\t]/g, " ").split("<param ");
Info.j2sAppletID = id;
Info.jalview_SCREEN_WIDTH= 100, // desktop width -- 0 to hide
Info.jalview_SCREEN_HEIGHT= 70,  // desktop height -- 0 to hide
Info.jalview_SCREEN_X= 10,
Info.jalview_SCREEN_Y= 10,
Info.jalview_EMBEDDED= true;


for (var i in Info) {
	var v = ("" + Info[i] || "null").replace(/\"/g,"'");
	args.push("name=\"Info." + i + "\" value=\"" + v + "\""); 
}
Info.args = args;
SwingJS.getApplet(id, Info);
	document.title = id;
	e.target.style.visibility="hidden";

    }
    
    function processAppletElement(element) {
        var code = element.getAttribute("code");
        var parent = element.parentElement;
        if (code == "jalview.bin.JalviewLite") {
	    	var text = element.outerHTML;
	    	console.log(text);
	    	var btn = document.createElement("button");
	    	btn.innerHTML = "Start Jalview";
	    	var a = element.getAttribute("width");
	    	btn.style.width = (a || 140) + "px";
	    	a = element.getAttribute("height");
	    	btn.style.height = (a || 25) + "px";
	        btn.appletElement = element;
	        parent.replaceChild(btn, element);        
	        $(btn).click(testBtn)
        } else {
        	parent.removeElement(element);
        }
    }
       

    /** 
     * Generate an applet tag
     * 
     * @param code
     * @param name
     * @param archive
     * @param width
     * @param height
     * @param params
     * @returns  a DOM APPLET element
     */
    function createAppletTag(code, name, archive, width, height, params){
        var app = document.createElement('applet');
    	app.code= code;
    	app.width = width;
    	app.height = height;
	app.archive = archive;

	var arrayLength = params.length;
	for (var i = 0; i < arrayLength; i++) {
    	    //console.log('name : '+ params[i][0] + ' code : '+ params[i][1]);    	    
    	    var param = document.createElement('param');
    	    param.name = params[i][0];
    	    param.value = params[i][1];
    	    app.appendChild(param);
	}
	return app;
    }

    function readCookie(name) {
        var nameEQ = name + "=";
        var ca = document.cookie.split(';');
        for(var i=0;i < ca.length;i++) {
            var c = ca[i];
            while (c.charAt(0)==' ') c = c.substring(1,c.length);
            if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
        }
        return null;
    }

    function setOrUpdateCookie(name, value, days) {
        var expires;
        if (days) {
            var date = new Date();
            date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
            expires = "; expires=" + date.toGMTString();
        }
        else {
            expires = "";
        }
        document.cookie = name + "=" + value + expires + "; path=/";
    }

   $(function(){        
        var url = window.location.href;
	var end = url.length;
	var start = url.lastIndexOf("#");
	var newPage = url.substring(start + 1, end);
	var page = ((start === -1) ? currentPage : newPage);
	//alert("page:" + page); 
       if(page === "embeddedWJmol"){
         // do nothing embeddedWJmol page already include

            $('#header').load("includes/header_jv.html");
            //$('#content').load(page + ".html");
            $('#nav').load("includes/nav_jv.html");
            $('#footer').load("includes/footer_jv.html"); 
            $('#'+ currentPage).addClass('active-trail active'); 
        }else{
            $('#header').load("includes/header_jv.html");
            $('#content').load(page + ".html");
            $('#nav').load("includes/nav_jv.html");
            $('#footer').load("includes/footer_jv.html"); 
            $('#'+ currentPage).addClass('active-trail active');  

	    var e = document.getElementById("view_decorated");
            e.style.display = 'none';
	}     
   });
   
function doSubmit(target){
   var currentPage = target+'.html';
   //alert("page:" + target); 
   if(target == "embeddedWJmol"){
      //loadJMolPage();
      window.location.href = 'embeddedWJmol.html#' + target;
      $('#content').load(currentPage);
   }else{
      window.location.href = 'index.html#' + target;
      $('#content').load(currentPage);
   }
   updateLinks(target);
}



function updateLinks(target) {
    var ul = document.getElementById("menu");
    var items = ul.getElementsByTagName("li");
    for (var i = 0; i < items.length; ++i) {
	removeClass(items[i], "active-trail active");
    }
   $('#'+ target).addClass('active-trail active');
}

function hasClass(ele,cls) {
  return !!ele.className.match(new RegExp('(\\s|^)'+cls+'(\\s|$)'));
}

function addClass(ele,cls) {
  if (!hasClass(ele,cls)) ele.className += " "+cls;
}

function removeClass(ele,cls) {
  if (hasClass(ele,cls)) {
    var reg = new RegExp('(\\s|^)'+cls+'(\\s|$)');
    ele.className=ele.className.replace(reg,' ');
  }
}


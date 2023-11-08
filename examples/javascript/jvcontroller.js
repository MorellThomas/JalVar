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
 * You should have received a copy of the GNU General Public License along with Jalview.  If not, see <http://www.gnu.org/licenses/>.
 * The Jalview Authors are detailed in the 'AUTHORS' file.
 */

 
    
    var currentPage = "applets";

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


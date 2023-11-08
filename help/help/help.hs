<?xml version="1.0" encoding="ISO-8859-1" ?>
<!--
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
-->
<!DOCTYPE helpset PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN" "http://java.sun.com/products/javahelp/helpset_1_0.dtd">
<helpset version="1.0">
   <!-- title -->
   <title>Jalview Documentation</title>
   <!-- maps -->
   <maps>
     <homeID>home</homeID>
     <mapref location="help.jhm" />
   </maps>
   <!-- views -->
   <view>
      <name>TOC</name>
      <label>Table Of Contents</label>
      <type>javax.help.TOCView</type>
      <data>helpTOC.xml</data>
   </view>
  <view>
    <name>Search</name>
    <label>Search</label>
    <type>javax.help.SearchView</type>
    <data engine="com.sun.java.help.search.DefaultSearchEngine">
      JavaHelpSearch
    </data>
  </view>
<presentation default="true" displayviews="true" displayviewimages="false">
    <name>TOPALi</name>
    <size width="800" height="700" />
    <location x="200" y="50" />
    <title>Jalview Documentation</title>
    <image>helpIcon</image>
    <toolbar>
		<helpaction image="backIcon">javax.help.BackAction</helpaction>
		<helpaction image="forwardIcon">javax.help.ForwardAction</helpaction>
		<helpaction image="homeIcon">javax.help.HomeAction</helpaction>
		<helpaction>javax.help.SeparatorAction</helpaction>
		<!--<helpaction image="reloadIcon">javax.help.ReloadAction</helpaction>-->
		<!--<helpaction image="addBookmarkIcon">javax.help.FavoritesAction</helpaction>-->
		<helpaction image="printIcon">javax.help.PrintAction</helpaction>
		<helpaction image="printSetupIcon">javax.help.PrintSetupAction</helpaction>
	</toolbar>
  </presentation>
</helpset>

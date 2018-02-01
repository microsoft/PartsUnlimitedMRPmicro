<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta charset="utf-8" />
    <title>Parts Unlimited MRP</title>

    <!-- WinJS references -->
    <link href="<spring:url value="./resources/winjs/css/ui-light.css" />" rel="stylesheet" type="text/css" />
    <script type="text/javascript" src="<spring:url value="./resources/winjs/js/winjs.js" />"></script>

    <!-- Parts Unlimited references -->
    <link href="<spring:url value="./resources/css/default.css"  />" rel="stylesheet" type="text/css"/>
    <script type="text/javascript" src="<spring:url value="./resources/js/serverconfig.js" />"></script>
    <script type="text/javascript" src="<spring:url value="./resources/js/data.js" />"></script>
    <script type="text/javascript" src="<spring:url value="./resources/js/navigator.js" />"></script>
	<script type="text/javascript" src="<spring:url value="./resources/js/date.js" />"></script>
    <script type="text/javascript" src="<spring:url value="./resources/js/default.js" />"></script>
    <script type="text/javascript" src="<spring:url value="./resources/controls/edittools/edittools.js" />"></script>
    <!-- <script type="text/javascript" src="<spring:url value="https://maps.googleapis.com/maps/api/js?libraries=places" />"></script> -->
</head>
<body>
    <ul class="navigation">
		<div class="navigationButton" data-win-control="WinJS.UI.NavBarCommand" data-win-options="{ label: 'Home', icon: 'url(./resources/images/FabrikamIcon.png)', page:'main' }"></div>
		<div class="navigationButton" data-win-control="WinJS.UI.NavBarCommand" data-win-options="{ label: 'Dealers', icon: 'url(./resources/images/DealersIcon.png)', page:'dealers' }"></div>
        <div class="navigationButton" data-win-control="WinJS.UI.NavBarCommand" data-win-options="{ label: 'Quotes', icon: 'url(./resources/images/QuoteIcon.png)', page:'quotes' }"></div>
        <div class="navigationButton" data-win-control="WinJS.UI.NavBarCommand" data-win-options="{ label: 'Orders', icon: 'url(./resources/images/OrderIcon.png)', page:'orders' }"></div>
        <div class="navigationButton" data-win-control="WinJS.UI.NavBarCommand" data-win-options="{ label: 'Deliveries', icon: 'url(./resources/images/DeliveryIcon.png)', page:'deliveries' }"></div>
        <div class="navigationButton" data-win-control="WinJS.UI.NavBarCommand" data-win-options="{ label: 'Catalog', icon: 'url(./resources/images/CatalogIcon.png)', page:'catalog' }"></div>
        <div class="navigationButton" data-win-control="WinJS.UI.NavBarCommand" data-win-options="{ label: 'Settings', icon: 'url(./resources/images/SettingsIcon.png)', page:'settings' }"></div>
    </ul>

    <div id="progressUnderlay" class="progressUnderlay"></div>
    <div id="progressContainer" class="progressContainer">
        <div class="progressMessageContainer win-type-x-large">
            <h2 id="progressMessage">Starting...</h2>
        </div>
        <progress id="progressRing" style="color: #09F;" class="win-large win-ring progressLocation"></progress>
    </div>

    <input type="checkbox" id="nav-trigger" class="nav-trigger" />
    <label for="nav-trigger"></label>
    
    <div id="contenthost" data-win-control="Application.PageControlNavigator" data-win-options="{home: './resources/pages/main/main.jsp'}"></div>
    
    <div id="confirmdialog">
        <div data-win-control="WinJS.UI.ContentDialog" data-win-options="{
             title: 'The title',
             primaryCommandText: 'Yes',
             secondaryCommandText: 'No'
        }">
        </div>
    </div>
</body>
</html>

@Portlet
@Application(name = "NPSAdministrationController")
@Bindings({
        @Binding(value = IdentityManager.class),
        @Binding(value = NpsService.class)})
@Scripts({
    @Script(id = "jQueryUI", value = "js/lib/jquery-ui.js"),
    // AngularJS is still global, should be AMDified
    @Script(id = "angularjs", value = "js/lib/angular.min.js"),
    @Script(id = "ngSanitize", value = "js/lib/angular-sanitize.js", depends = "angularjs"),
    @Script(id = "ng-google-chart", value = "js/lib/ng-google-chart.js", depends = "angularjs"),
    // services and controllers js are AMD modules, required by controllers.js
    @Script(id = "controllers", value = "js/controllers.js", depends = { "angularjs" , "ng-google-chart"}),
    @Script(id = "npsAdminAddon", value = "js/nps-admin.js", depends = { "controllers","jQueryUI" }) })

@Stylesheets({
        @Stylesheet(id = "jQueryUISkin", value = "style/jquery-ui.css") ,
        @Stylesheet(id = "nps-admin", value = "style/nps-admin.css") })

@Assets("*")
package org.exoplatform.nps.portlet.npsAdministration;

import juzu.Application;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.Script;
import juzu.plugin.asset.Scripts;
import juzu.plugin.asset.Stylesheet;
import juzu.plugin.asset.Stylesheets;
import juzu.plugin.binding.Binding;
import juzu.plugin.binding.Bindings;
import juzu.plugin.portlet.Portlet;


import org.exoplatform.nps.services.NpsService;
import org.exoplatform.social.core.manager.IdentityManager;



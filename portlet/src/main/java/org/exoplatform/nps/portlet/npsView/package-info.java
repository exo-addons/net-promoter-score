@Portlet
@Application(name = "NPSViewController")
@Bindings({ @Binding(value = NpsService.class),
        @Binding(value = NpsTypeService.class),
        @Binding(value = IdentityManager.class)
})
@Scripts({
    @Script(id = "jQueryUI", value = "js/lib/jquery-ui.js"),
    // AngularJS is still global, should be AMDified
    @Script(id = "angularjs", value = "js/lib/angular.min.js"),
    @Script(id = "ngSanitize", value = "js/lib/angular-sanitize.js", depends = "angularjs"),
    // services and controllers js are AMD modules, required by controllers.js
    @Script(id = "controllers", value = "js/controllers.js", depends = "angularjs"),
    @Script(id = "npsView", value = "js/nps-view.js", depends = { "controllers" }) })
@Stylesheets({
        @Stylesheet(id = "jQueryUISkin", value = "style/jquery-ui.css") ,
        @Stylesheet(id = "npsCssView", value = "style/nps-view.css") })
@Assets("*")
package org.exoplatform.nps.portlet.npsView;

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

import org.exoplatform.nps.services.NpsTypeService;
import org.exoplatform.social.core.manager.IdentityManager;



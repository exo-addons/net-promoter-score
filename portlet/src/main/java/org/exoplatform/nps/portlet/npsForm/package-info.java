@Portlet
@Application(name = "NPSFormController")
@Bindings({ @Binding(value = NpsService.class),
        @Binding(value = NpsTypeService.class),
        @Binding(value = ListenerService.class),
        @Binding(value = IdentityManager.class)
})
@Scripts({
    @Script(id = "jQueryUI", value = "js/lib/jquery-ui.js"),
    // AngularJS is still global, should be AMDified
    @Script(id = "angularjs", value = "js/lib/angular.min.js"),
    @Script(id = "ngSanitize", value = "js/lib/angular-sanitize.js", depends = "angularjs"),
        @Script(id = "ariajs", value = "js/lib/angular-aria.js", depends = "angularjs"),
        @Script(id = "animate", value = "js/lib/angular-animate.js", depends = "angularjs"),
        @Script(id = "material", value = "js/lib/angular-material.min.js", depends = "angularjs"),
        @Script(id = "angular-cookies", value = "js/lib/angular-cookies.js", depends = "angularjs"),
    // services and controllers js are AMD modules, required by controllers.js
    @Script(id = "controllers", value = "js/controllers.js", depends = {"angularjs", "angular-cookies"}),
    @Script(id = "npsForm", value = "js/nps-form.js", depends = { "controllers" }) })
@Stylesheets({
        @Stylesheet(id = "jQueryUISkin", value = "style/jquery-ui.css") ,
        @Stylesheet(id = "npsCssForm", value = "style/nps-form.css") })
@Assets("*")
package org.exoplatform.nps.portlet.npsForm;

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
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.social.core.manager.IdentityManager;



require( ["SHARED/jquery", "npsAdminControllers"], function ( $,  npsAdminControllers)
{
    $( document ).ready(function() {
        var npsAdminAppRoot = $('#npsAdmin');
        var npsAdminApp = angular.module('npsAdminApp', ['googlechart', 'ngMaterial', 'ngMaterialDateRangePicker']);
        try {
            npsAdminApp.controller('npsAdminCtrl', npsAdminControllers);
            angular.bootstrap(npsAdminAppRoot, ['npsAdminApp']);
        } catch(e) {
            console.log(e);
        }
});

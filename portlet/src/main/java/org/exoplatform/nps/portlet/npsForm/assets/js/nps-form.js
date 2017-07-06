require( ["SHARED/jquery", "npsFormControllers"], function ( $,  npsFormControllers)
{
    $( document ).ready(function() {
        var npsFormAppRoot = $('#npsForm');
        var npsFormApp = angular.module('npsFormApp', ['ngCookies']);
        try {
            npsFormApp.controller('npsFormCtrl', npsFormControllers);
            angular.bootstrap(npsFormAppRoot, ['npsFormApp']);
        } catch(e) {
            console.log(e);
        }

        $(".npsRadio .option-input.radio").click(function(){
            $("#npsComment").css({
                "display": "block",
                "opacity" : 1
            })
        });
    });

});

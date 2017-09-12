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
            $("#npsComment").css("display","block");
            $("#submit").css("display","inline-block");
            setTimeout(function(){
                $("#npsComment, #submit").css("opacity",1);
            }, 200);
        });

        $("#nps .close").click(function(){
            $("div#npsForm #npsFormCtrl").css("opacity","0");
            setTimeout(function(){$("div#npsForm").css("display","none");}, 500);
        });
    });

});

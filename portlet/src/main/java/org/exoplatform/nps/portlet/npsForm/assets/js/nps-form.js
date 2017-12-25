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

        $(".npsRadio .option-input.radio.detractor").click(function(){
            $("#npsDetractorComment").css("display","block");
            $("#npsPassiveComment").css("display","none");
            $("#npsPromoterComment").css("display","none");
            $("#submit").css("display","inline-block");
            setTimeout(function(){
                $("#npsDetractorComment, #submit").css("opacity",1);
            }, 200);
        });

        $(".npsRadio .option-input.radio.passives").click(function(){
            $("#npsPassiveComment").css("display","block");
            $("#npsDetractorComment").css("display","none");
            $("#npsPromoterComment").css("display","none");
            $("#submit").css("display","inline-block");
            setTimeout(function(){
                $("#npsPassiveComment, #submit").css("opacity",1);
            }, 200);
        });

        $(".npsRadio .option-input.radio.promoters").click(function(){
            $("#npsPromoterComment").css("display","block");
            $("#npsDetractorComment").css("display","none");
            $("#npsPassiveComment").css("display","none");
            $("#submit").css("display","inline-block");
            setTimeout(function(){
                $("#npsPromoterComment, #submit").css("opacity",1);
            }, 200);
        });

        $("#nps .close, #nps .thankyClose").click(function(){
            $("div#npsForm #npsFormCtrl").css("opacity","0");
            setTimeout(function(){$("div#npsForm").css("display","none");}, 500);
        });
    });

});

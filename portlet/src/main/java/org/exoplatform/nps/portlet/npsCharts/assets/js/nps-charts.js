require( ["SHARED/jquery", "npsChartsControllers"], function ( $,  npsChartsControllers)
{
    $( document ).ready(function() {
        var npsChartsAppRoot = $('#npsCharts');
        var npsChartsApp = angular.module('npsChartsApp', ['googlechart']);
        try {
            npsChartsApp.controller('npsChartsCtrl', npsChartsControllers);
            angular.bootstrap(npsChartsAppRoot, ['npsChartsApp']);
        } catch(e) {
            console.log(e);
        }

    });

});

require( ["SHARED/jquery", "npsViewControllers"], function ( $,  npsViewControllers)
{
    $( document ).ready(function() {
        var npsViewAppRoot = $('#npsView');
        var npsViewApp = angular.module('npsViewApp', []);
        try {
            npsViewApp.controller('npsViewCtrl', npsViewControllers);
            angular.bootstrap(npsViewAppRoot, ['npsViewApp']);
        } catch(e) {
            console.log(e);
        }

    });

});

require( ["SHARED/jquery", "npsAdminControllers"], function ( $,  npsAdminControllers)
{
    $( document ).ready(function() {
        var npsAdminAppRoot = $('#npsAdmin');
        var npsAdminApp = angular.module('npsAdminApp', ['googlechart']);
        try {
            npsAdminApp.controller('npsAdminCtrl', npsAdminControllers);
            angular.bootstrap(npsAdminAppRoot, ['npsAdminApp']);
        } catch(e) {
            console.log(e);
        }


        /* DATE PICKER*/
        var dateFormat = "dd/mm/yy",
              from = $( "#fromDatepicker" )
                .datepicker({
                  changeMonth: true,
                  changeYear: true,
                  numberOfMonths: 2
                })
                .on( "change", function() {
                  to.datepicker( "option", "minDate", getDate( this ) );
                }),
              to = $( "#toDatepicker" ).datepicker({
                defaultDate: "+1w",
                changeMonth: true,
                changeYear: true,
                numberOfMonths: 2
              })
              .on( "change", function() {
                from.datepicker( "option", "maxDate", getDate( this ) );
              });


//            $( "#datepicker" ).datepicker( "option", "showAnim", "slideDown" );

    });
    function getDate( element ) {
          var date;
          try {
            date = $.datepicker.parseDate( dateFormat, element.value );
          } catch( error ) {
            date = null;
          }

          return date;
    }

});

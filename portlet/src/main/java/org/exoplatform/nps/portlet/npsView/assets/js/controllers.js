define("npsViewControllers", [ "SHARED/jquery", "SHARED/juzu-ajax"], function($, jz)  {

    var npsViewCtrl = function($scope, $q, $timeout, $http) {
        var npsViewContainer = $('#npsView');
        var deferred = $q.defer();
        $scope.showAlert = false;
        $scope.scoreTypeId = 0;
        $scope.scoreTypeName="";
        $scope.npScore=0;
        $scope.scoreTypeMessage = "";
        $scope.dashoffset=0;

        $scope.setResultMessage = function (text, type) {
            $scope.resultMessageClass = "alert-" + type;
            $scope.resultMessageClassExt = "uiIcon" + type.charAt(0).toUpperCase()
                + type.slice(1);
            $scope.showAlert = true;
            $scope.resultMessage = text;

            $("div#npsView").addClass("thankyou");



            setTimeout(function () {
                $("div#npsView #npsViewCtrl").css("opacity","0");

            }, 5000);
            setTimeout(function () {
                $scope.showAlert = false;
                $("div#npsView").css("display", "none");
            }, 5500);
        }

        $scope.updateSettings = function() {
                  console.log("Update portlet settings");
                  $http({
                    data : $scope.npsSetting,
                    method : 'POST',
                     headers : {
                       'Content-Type' : 'application/json'
                    },
                    url : npsViewContainer.jzURL('NPSViewController.updateSettings')
                  }).then(function successCallback(data) {
                    $scope.i18n = data.data;
                    deferred.resolve(data);
                   }, function errorCallback(data) {
                     $scope.setResultMessage($scope.i18n.errorInitForm, "error");
                   });
                 }



        $scope.loadBundle = function () {
            $http({
                method: 'GET',
                url: npsViewContainer.jzURL('NPSViewController.getBundle') + "&locale=" + eXo.env.portal.language
            }).then(function successCallback(data) {
                $scope.i18n = data.data;
                deferred.resolve(data);
                /*$scope.setResultMessage(data, "success");*/
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }

                $scope.loadData = function (typeId) {
                    $http({
                        method: 'GET',
                        url: npsViewContainer.jzURL('NPSViewController.getData')
                    }).then(function successCallback(data) {
                        $scope.scoreTypeName=data.data.scoreTypeName;
                        $scope.npScore = data.data.npScore;
                        $scope.dashoffset = data.data.dashoffset;
                        $scope.npsColor = "yellow";
                        if($scope.npScore<25){
                        $scope.npsColor = "red";
                        }
                        if($scope.npScore>70){
                        $scope.npsColor = "green";
                        }

                        deferred.resolve(data);
                        $('#npsView').css('display', 'block');
        //                $scope.setResultMessage(data, "success");
                    }, function errorCallback(data) {
                        $scope.setResultMessage($scope.i18n.defaultError, "error");
                    });
                }




       $scope.loadBundle();
       $scope.loadData();

    };
    return npsViewCtrl;


});
define("npsFormControllers", [ "SHARED/jquery", "SHARED/juzu-ajax"], function($, jz)  {

    var npsFormCtrl = function($scope, $q, $timeout, $http, $cookies) {
        var npsFormContainer = $('#npsForm');
        var deferred = $q.defer();
        $scope.showAlert = false;
        $scope.newScore = null;
        $scope.showForm = true;
        $scope.scoreTypeId = 0;
        $scope.portletId = "";
        $scope.scoreTypeMessage = "";
        $scope.setResultMessage = function (text, type) {
            $scope.resultMessageClass = "alert-" + type;
            $scope.resultMessageClassExt = "uiIcon" + type.charAt(0).toUpperCase()
                + type.slice(1);
            $scope.showAlert = true;
            $scope.resultMessage = text;

            $("div#npsForm").addClass("thankyou");



            setTimeout(function () {
                $("div#npsForm #npsFormCtrl").css("opacity","0");

            }, 5000);
            setTimeout(function () {
                $scope.showAlert = false;
                $("div#npsForm").css("display", "none");
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
                    url : npsFormContainer.jzURL('NPSFormController.updateSettings')
                  }).then(function successCallback(data) {
                    $scope.i18n = data.data;
                    deferred.resolve(data);
                   }, function errorCallback(data) {
                     $scope.setResultMessage($scope.i18n.errorInitForm, "error");
                   });
                 }



        $scope.loadContext = function() {

            if(typeof $cookies.get("nps_status") != 'undefined'){
                $('#npsForm').css('display', 'none');
            }else{
            var cookies = ($cookies.get("_mkto_trk"));
            if(!angular.isUndefined(cookies)){
                cookies.replace("&","%26");
            }
            $http({
                method : 'GET',
                params: {mktCookie: cookies},
                url : npsFormContainer.jzURL('NPSFormController.getContext')
            }).then(function successCallback(data) {
                $scope.i18n = data.data;
                if($scope.i18n.firstLogDiff>10){
                     $scope.showForm = true;
                     $('#npsForm').css('display', 'block');
                 }
                $scope.scoreTypeId = data.data.scoreTypeId;
                $scope.scoreTypeMessage = data.data.scoreTypeMessage;
                if ($scope.scoreTypeMessage==null||$scope.scoreTypeMessage==""){
                $scope.scoreTypeMessage=data.data.messageForm;
                }
                $scope.portletId = data.data.portletId;
                $scope.showAlert = false;
                deferred.resolve(data);
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
            }
        }

        $scope.saveScore = function() {
            $scope.showAlert = false;
            $scope.newScore.typeId=$scope.scoreTypeId;
                $http({
                    data : $scope.newScore,
                    method : 'POST',
                    headers : {
                        'Content-Type' : 'application/json'
                    },
                    url : npsFormContainer.jzURL('NPSFormController.saveScore')
                }).then(function successCallback(data) {
                    var today = new Date();
                    var expiresValue = new Date(today);
                    expiresValue.setDate(today.getDate() + parseInt($scope.i18n.respondedCookiesExpiration));
                    $cookies.put("nps_status-"+$scope.portletId,"responded" , {'expires' : expiresValue});
                   // $('#npsForm').css('display', 'none');
                    $scope.showForm = false;
                    $scope.setResultMessage($scope.i18n.thanks, "success");
                }, function errorCallback(data) {
                    $scope.setResultMessage($scope.i18n.defaultError, "error");
                });

        }

        $scope.cancel = function() {
                var today = new Date();
                var expiresValue = new Date(today);
                expiresValue.setDate(today.getDate() + parseInt($scope.i18n.reportedCookiesExpiration));
                $cookies.put("nps_status-"+$scope.portletId,"enabled" , {'expires' : expiresValue});
            $('#npsForm').css('display', 'none');
            $scope.showForm = false;
        }

        $scope.disableUser = function() {
                var now = new Date();
                var expiresValue  = new Date(now.getFullYear()+20, now.getMonth(), now.getDate());
                $cookies.put("nps_status-"+$scope.portletId,"disabled" , {'expires' : expiresValue});
               $('#npsForm').css('display', 'none');
        }


        $scope.displayForm = function() {

            if(typeof $cookies.get("nps_status-"+$scope.portletId) != 'undefined'){
                $('#npsForm').css('display', 'none');
            }else{
                $('#npsForm').css('display', 'block');

                $scope.showForm = true;
                $scope.showAlert = false;
            }
            console.log($cookies.get("nps_status-"+$scope.portletId));

        }

        $scope.loadContext();

    };
    return npsFormCtrl;


});
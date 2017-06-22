define("npsFormControllers", [ "SHARED/jquery", "SHARED/juzu-ajax"], function($, jz)  {

    var npsFormCtrl = function($scope, $q, $timeout, $http, $cookies) {
        var npsFormContainer = $('#npsForm');
        var deferred = $q.defer();
        $scope.showAlert = false;
        $scope.newScore = null;
        $scope.showForm = true;


        $scope.setResultMessage = function (text, type) {
            $scope.resultMessageClass = "alert-" + type;
            $scope.resultMessageClassExt = "uiIcon" + type.charAt(0).toUpperCase()
                + type.slice(1);
            $scope.showAlert = true;
            $scope.resultMessage = text;
        }


        $scope.loadBundles = function() {
            $http({
                method : 'GET',
                url : npsFormContainer.jzURL('NPSFormController.getBundle')
            }).then(function successCallback(data) {
                $scope.i18n = data.data;
                $scope.showAlert = false;
                deferred.resolve(data);
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }

        $scope.saveScore = function() {
            $scope.showAlert = false;
               // $scope.setResultMessage($scope.i18n.savingScore, "info");
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
                    expiresValue.setDate(today.getDate() + 30);
                    $cookies.put("nps_status","responded" , {'expires' : expiresValue});
                    $('#npsForm').css('display', 'none');
                    $scope.showForm = false;
                }, function errorCallback(data) {
                    $scope.setResultMessage($scope.i18n.defaultError, "error");
                });

        }

        $scope.cancel = function() {
                var today = new Date();
                var expiresValue = new Date(today);
                expiresValue.setDate(today.getDate() + 10);
                $cookies.put("nps_status","enabled" , {'expires' : expiresValue});
                $('#npsForm').css('display', 'block');
        }

        $scope.disableUser = function() {
            $http({
                method : 'GET',
                url : npsFormContainer.jzURL('NPSFormController.disableUser')
            }).then(function successCallback(data) {
                var now = new Date();
                var expiresValue  = new Date(now.getFullYear()+2, now.getMonth(), now.getDate());
                $cookies.put("nps_status","disabled" , {'expires' : expiresValue});
                $('#npsForm').css('display', 'none');
                $scope.showAlert = false;
                deferred.resolve(data);
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }


        $scope.displayForm = function() {

            if(typeof $cookies.get("nps_status") != 'undefined'){
                $('#npsForm').css('display', 'none');
            }else{
                $http({
                    method : 'GET',
                    url : npsFormContainer.jzURL('NPSFormController.displayForm')
                }).then(function successCallback(data) {
                    if(data.data.userDisabled=='true'){
                        var now = new Date();
                        var expiresValue  = new Date(now.getFullYear()+2, now.getMonth(), now.getDate());
                        $cookies.put("nps_status","disabled" , {'expires' : expiresValue});
                    }

                    if(data.data.showForm=='true'){
                        $('#npsForm').css('display', 'block');
                    } else{
                        $('#npsForm').css('display', 'none');
                    }

                    $scope.showAlert = false;
                    deferred.resolve(data);
                }, function errorCallback(data) {
                    $scope.setResultMessage($scope.i18n.defaultError, "error");
                });
            }
            console.log($cookies.get("nps_status"));

        }

        $scope.displayForm();
        $scope.loadBundles();
        $scope.refreshController = function() {
            try {
                $scope.$digest()
            } catch (excep) {
                // No need to display errors in console
            }
        };
        $('#npsForm').css('display', 'block');
        $(".npsLoadingBar").remove();
    };
    return npsFormCtrl;


});
define("npsAdminControllers", ["SHARED/jquery", "SHARED/juzu-ajax"], function ($, jz) {
    var npsAdminCtrl = function ($scope, $q, $timeout, $http, $filter) {
        var npsAdminContainer = $('#npsAdmin');
        var deferred = $q.defer();
        $scope.currentUser = "";
        $scope.currentUserAvatar = "";
        $scope.currentUserName = "";
        $scope.scores  = [];
        $scope.scoresSum=0;
        $scope.pieChartObject = {};
        $scope.pieChartObject.options = {
            backgroundColor: 'transparent',
            legend:{position: 'bottom'},
            chartArea:{top:0,width:'85%',height:'85%'},
            pieSliceText: 'label',
            slices: {
                0: { color: '#6ccbae' },
                1: { color: '#ffca7a' },
                2: { color: '#dea2a2' }
            }
        };
        $scope.pieChartObject.type = "PieChart";
        $scope.gaugeChartObject = {};
        $scope.gaugeChartObject.type = "Gauge";
        $scope.gaugeChartObject.options = {
                majorTicks : [-100,-50,0,50,100],
                min : -100,
                redFrom: -100,
                redTo: -30,
                yellowFrom: -30,
                yellowTo: 30,
                greenFrom: 30,
                greenTo: 100,
        };
        $scope.itemsPerPage = 10;
        $scope.currentPage = 0;
        $scope.pages=[];
        $scope.setResultMessage = function (text, type) {
            $scope.resultMessageClass = "alert-" + type;
            $scope.resultMessageClassExt = "uiIcon" + type.charAt(0).toUpperCase()
                + type.slice(1);
            $scope.showAlert = true;
            $scope.resultMessage = text;
        }


        $scope.loadBundle = function () {
            $http({
                method: 'GET',
                url: npsAdminContainer.jzURL('NPSAdministrationController.getBundle') + "&locale=" + eXo.env.portal.language
            }).then(function successCallback(data) {
                $scope.i18n = data.data;
                deferred.resolve(data);
                /*$scope.setResultMessage(data, "success");*/
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }


        $scope.loadScores = function (offset,limit) {
            $http({
                method: 'GET',
                url: npsAdminContainer.jzURL('NPSAdministrationController.getScores')+ "&offset="+offset+ "&limit="+limit
            }).then(function successCallback(data) {
                $scope.scores = data.data;
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });

        };


        $scope.loadData = function () {
            $http({
                method: 'GET',
                url: npsAdminContainer.jzURL('NPSAdministrationController.getData')
            }).then(function successCallback(data) {
                $scope.currentUser = data.data.currentUser;
                $scope.currentUserAvatar = data.data.currentUserAvatar;
                $scope.currentUserName = data.data.currentUserName;
                $scope.scorsnbr = data.data.scorsnbr;
				$scope.detractorsNbr = data.data.detractorsNbr;
				$scope.promotersNbr = data.data.promotersNbr;
				$scope.passivesNbr = data.data.passivesNbr;
				$scope.detractorsPrc = data.data.detractorsPrc;
				$scope.promotersPrc = data.data.promotersPrc;
				$scope.passivesPrc = data.data.passivesPrc;
                $scope.npScore = data.data.npScore;

                $scope.gaugeChartObject.data = [
                    ['Label', 'Value'],
                    ['NPS',parseFloat($scope.npScore)]
                ];


                $scope.pieChartObject.data = {"cols": [
                    {id: "t", label: "Topping", type: "string"},
                    {id: "s", label: "Slices", type: "number"}
                ], "rows": [
                    {c: [
                        {v: "Promoters"},
                        {v: $scope.promotersNbr},
                    ]},
                    {c: [
                        {v: "Passives"},
                        {v: $scope.passivesNbr}
                    ]},
                    {c: [
                        {v: "Detractor"},
                        {v: $scope.detractorsNbr}
                    ]}
                ]};
                $scope.pages=$scope.range();
                deferred.resolve(data);
//                $scope.setResultMessage(data, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }


        $scope.deleteScore = function(score) {
            $scope.showAlert = false;
            // $scope.setResultMessage($scope.i18n.savingScore, "info");
            $http({
                data : score,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : npsAdminContainer.jzURL('NPSAdministrationController.deleteScore')
            }).then(function successCallback(data) {
                $scope.loadScores($scope.currentPage*$scope.itemsPerPage, $scope.itemsPerPage);
                $scope.setResultMessage($scope.i18n.scoreDeleted, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });

        }

        $scope.disableScore = function(score) {
            $scope.showAlert = false;
            // $scope.setResultMessage($scope.i18n.savingScore, "info");
            $http({
                data : score,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : npsAdminContainer.jzURL('NPSAdministrationController.disableScore')
            }).then(function successCallback(data) {
                $scope.loadScores($scope.currentPage*$scope.itemsPerPage, $scope.itemsPerPage);
                $scope.setResultMessage($scope.i18n.scoreEnabled, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });

        }

        $scope.enableScore = function(score) {
            $scope.showAlert = false;
            // $scope.setResultMessage($scope.i18n.savingScore, "info");
            $http({
                data : score,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : npsAdminContainer.jzURL('NPSAdministrationController.enableScore')
            }).then(function successCallback(data) {
                $scope.loadScores($scope.currentPage*$scope.itemsPerPage, $scope.itemsPerPage);
                $scope.setResultMessage($scope.i18n.scoreDisabled, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });

        }



        $scope.range = function() {
            var rangeSize = 10;
            var ret = [];
            var start=0;
            var pgCount=$scope.pageCount();
            if(pgCount>rangeSize){
            var d = $scope.currentPage-Math.ceil(rangeSize/2);
            if(d>0) start=d;
            }
            var end=start+rangeSize;
            if(end>pgCount) end=pgCount;
            for (var i=start; i<end; i++) {
                ret.push(i);
            }
            return ret;
        };

        $scope.prevPage = function() {
            if ($scope.currentPage > 0) {
                $scope.currentPage--;
                $scope.pages=$scope.range();
                $scope.loadScores($scope.currentPage*$scope.itemsPerPage, $scope.itemsPerPage);
            }
        };

        $scope.prevPageDisabled = function() {
            return $scope.currentPage === 0 ? "disabled" : "";
        };

        $scope.nextPage = function() {
            if ($scope.currentPage < $scope.pageCount() - 1) {
                $scope.currentPage++;
                $scope.pages=$scope.range();
                $scope.loadScores($scope.currentPage*$scope.itemsPerPage, $scope.itemsPerPage);
            }
        };

        $scope.nextPageDisabled = function() {
            return $scope.currentPage === $scope.pageCount() - 1 ? "disabled" : "";
        };

        $scope.pageCount = function() {
            return Math.ceil($scope.scorsnbr/$scope.itemsPerPage);
        };

        $scope.setPage = function(n) {
            if (n >= 0 && n < $scope.pageCount()) {
                $scope.currentPage = n;
                $scope.pages=$scope.range();
                $scope.loadScores(n*$scope.itemsPerPage, $scope.itemsPerPage);
            }
        };

        $scope.loadBundle();
        $scope.loadData();
        $scope.loadScores(0, $scope.itemsPerPage);
        $('#npsAdmin').css('visibility', 'visible');
        $(".npsLoadingBar").remove();
    };
    return npsAdminCtrl;

    /*
     $timeout(function() {
     $scope.showAlert = false;
     }, 2000);
     */
});

define("npsAdminControllers", ["SHARED/jquery", "SHARED/juzu-ajax"], function ($, jz) {
    var npsAdminCtrl = function ($scope, $q, $timeout, $http, $filter) {
        var npsAdminContainer = $('#npsAdmin');
        var deferred = $q.defer();
        $scope.newScoreType = null;
        $scope.scoreTypeToEdit = null;
        $scope.showEditForm = false;
        $scope.typeId = 0;
        $scope.selectModel = {};
        $scope.showForm = false;
        $scope.showGraphs = false;
        $scope.scores  = [];
        $scope.scoreTypes  = [];
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
        $scope.scoreToDelete=null;
        $scope.showDeletePopup=false;


        $scope.setResultMessage = function (text, type) {
            $scope.resultMessageClass = "alert-" + type;
            $scope.resultMessageClassExt = "uiIcon" + type.charAt(0).toUpperCase()
                + type.slice(1);
            $scope.showAlert = true;
            $scope.resultMessage = text;
        }


        $scope.loadScoreTypes = function (isDefault, question = null) {
            $http({
                method: 'GET',
                url: npsAdminContainer.jzURL('NPSAdministrationController.getScoreTypes')
            }).then(function successCallback(data) {
                $scope.scoreTypes = data.data;
                 if(isDefault && $scope.scoreTypes.length>0){
                 $scope.selectModel=$scope.scoreTypes[0];
                 $scope.typeId=$scope.scoreTypes[0].id;
                 $scope.getScoresbyType($scope.typeId);
                 }else if((!isDefault) && (question != null)){
                     $scope.selectModel=$scope.scoreTypes[question-1];
                     $scope.typeId=question;
                     $scope.getScoresbyType($scope.typeId);
                 }else if((!isDefault) && (question == null)){
                       $scope.selectModel=$scope.scoreTypes[$scope.scoreTypes.length-1];
                       $scope.typeId=$scope.scoreTypes.length;
                       $scope.getScoresbyType($scope.typeId);
                   }

                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });

        };



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


        $scope.loadScores = function (typeId,offset,limit) {
            $http({
                method: 'GET',
                url: npsAdminContainer.jzURL('NPSAdministrationController.getScores')+ "&typeId="+typeId+ "&offset="+offset+ "&limit="+limit
            }).then(function successCallback(data) {
                $scope.scores = data.data;
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });

        };


        $scope.loadData = function (typeId) {
            $http({
                method: 'GET',
                url: npsAdminContainer.jzURL('NPSAdministrationController.getData')+ "&typeId="+typeId
            }).then(function successCallback(data) {
                $scope.scorsnbr = data.data.scorsnbr;
				$scope.detractorsNbr = data.data.detractorsNbr;
				$scope.promotersNbr = data.data.promotersNbr;
				$scope.passivesNbr = data.data.passivesNbr;
				$scope.detractorsPrc = data.data.detractorsPrc;
				$scope.promotersPrc = data.data.promotersPrc;
				$scope.passivesPrc = data.data.passivesPrc;
                $scope.npScore = data.data.npScore;

                if($scope.npScore == "NaN"){
                    $scope.showGraphs = false;
                }else{
                    $scope.showGraphs = true;
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
                }

                $scope.pages=$scope.range();
               // $scope.getScoresbyType(typeId);

                deferred.resolve(data);
//                $scope.setResultMessage(data, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }

        $scope.saveScoreType= function(newScoreType)
        {
                    $scope.showAlert = false;
                    // $scope.setResultMessage($scope.i18n.savingScore, "info");
                    $http({
                        data : newScoreType,
                        method : 'POST',
                        headers : {
                            'Content-Type' : 'application/json'
                        },
                        url : npsAdminContainer.jzURL('NPSAdministrationController.saveType')
                    }).then(function successCallback(data) {
//                        $scope.setResultMessage($scope.i18n.typeSaved, "success");
                        $scope.loadScoreTypes(false);
                    }, function errorCallback(data) {
                        $scope.setResultMessage($scope.i18n.defaultError, "error");
                    });

                }

                 $scope.upadteScoreType= function(newScoreType)
                 {
                     $scope.showAlert = false;
                     // $scope.setResultMessage($scope.i18n.savingScore, "info");
                     $http({
                         data : newScoreType,
                         method : 'POST',
                         headers : {
                             'Content-Type' : 'application/json'
                         },
                         url : npsAdminContainer.jzURL('NPSAdministrationController.updateType')
                     }).then(function successCallback(data) {
//                                 $scope.setResultMessage($scope.i18n.typeSaved, "success");
                         $scope.loadScoreTypes(false, newScoreType.id);
                     }, function errorCallback(data) {
                         $scope.setResultMessage($scope.i18n.defaultError, "error");
                     });

                 }



                $scope.getScoreType = function (id) {
                    $http({
                        method: 'GET',
                        url: npsAdminContainer.jzURL('NPSAdministrationController.getScoreTypeById')+ "&id=" +id
                    }).then(function successCallback(data) {
                        $scope.scoreTypeToEdit = data.data;
                        $scope.showAlert = false;
                        $scope.showEditForm = true;
                    }, function errorCallback(data) {
                        $scope.setResultMessage($scope.i18n.defaultError, "error");
                    });

                };

        $scope.deleteScore = function(score) {
            $scope.scoreToDelete = score;
            $("#voteName").text(score.userFullName);
            $scope.showDeletePopup = true;
//            $(".npsAdminPopup").css("display", "block");
        }

        $scope.validatedDeleteScore = function(score) {
            $scope.showDeletePopup = false;
            // $scope.setResultMessage($scope.i18n.savingScore, "info");
            $http({
                data : score,
                method : 'POST',
                headers : {
                    'Content-Type' : 'application/json'
                },
                url : npsAdminContainer.jzURL('NPSAdministrationController.deleteScore')
            }).then(function successCallback(data) {
                $scope.loadData($scope.typeId);
                $scope.loadScores($scope.typeId,$scope.currentPage*$scope.itemsPerPage, $scope.itemsPerPage);
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
                $scope.loadData($scope.typeId);
                $scope.loadScores($scope.typeId,$scope.currentPage*$scope.itemsPerPage, $scope.itemsPerPage);
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
                $scope.loadData($scope.typeId);
                $scope.loadScores($scope.typeId,$scope.currentPage*$scope.itemsPerPage, $scope.itemsPerPage);
                $scope.setResultMessage($scope.i18n.scoreDisabled, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });

        }

        $scope.checkState = function(score, state) {
            state ? $scope.enableScore(score) : $scope.disableScore(score);
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
                $scope.loadScores($scope.typeId,$scope.currentPage*$scope.itemsPerPage, $scope.itemsPerPage);
            }
        };

        $scope.prevPageDisabled = function() {
            return $scope.currentPage === 0 ? "disabled" : "";
        };

        $scope.nextPage = function() {
            if ($scope.currentPage < $scope.pageCount() - 1) {
                $scope.currentPage++;
                $scope.pages=$scope.range();
                $scope.loadScores($scope.typeId,$scope.currentPage*$scope.itemsPerPage, $scope.itemsPerPage);
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
                $scope.loadScores($scope.typeId,n*$scope.itemsPerPage, $scope.itemsPerPage);
            }
        };

        $scope.getScoresbyType = function (typeId) {
                $scope.typeId=typeId;
                $scope.loadData(typeId);
                $scope.loadScores(typeId,0, $scope.itemsPerPage);

                $scope.showGraphs = true;
        };

        $scope.loadBundle();
        $scope.loadScoreTypes(true);
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

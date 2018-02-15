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
        $scope.weeklyNpScore=[];
        $scope.chartTypes = [
            {name : "Weekly", value : "weekly"},
            {name : "By Week", value : "byWeek"}
        ];
        $scope.selectedChartType={name : "Weekly", value : "weekly"};
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

         $scope.myChartObject = {};
         $scope.myChartObject.type = "LineChart";


                     $scope.myChartObject.type = "LineChart";
                     $scope.myChartObject.displayed = false;


           $scope.myChartObject.options = {
                         "title": "NPS per week",
                         "colors": ['#4285f4', '#00b36b', '#db4437'],
                         "defaultColors": ['#0000FF', '#009900', '#CC0000'],
                         "isStacked": "true",
                         "fill": 20,
                         "displayExactValues": true,
                         "vAxis": {
                             "title": "",
                             "gridlines": {
                                 "count": 10
                             }
                         },
                         "hAxis": {
                             "title": ""
                         }
                     };

                     $scope.myChartObject.view = {
                         columns: [0, 1, 2, 3]
                     };


        $scope.itemsPerPage = 10;
        $scope.currentPage = 0;
        $scope.pages=[];
        $scope.scoreToDelete=null;
        $scope.showDeletePopup=false;


        $scope.setResultMessage = function (text, type) {
            $scope.resultMessageClass = "alert-" + type;
            $scope.resultMessageClassExt = "uiIcon" + type.charAt(0).toUpperCase()+ type.slice(1);
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
                            {v: $scope.i18n.Promoters},
                            {v: $scope.promotersNbr},
                        ]},
                        {c: [
                            {v: $scope.i18n.Passives},
                            {v: $scope.passivesNbr}
                        ]},
                        {c: [
                            {v: $scope.i18n.Detractor},
                            {v: $scope.detractorsNbr}
                        ]}
                    ]};
                }
               $scope.getNPSLineChart();
                $scope.pages=$scope.range();
               // $scope.getScoresbyType(typeId);

                deferred.resolve(data);
//                $scope.setResultMessage(data, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }

       $scope.getNPSLineChart = function () {
             $http({
                 method: 'GET',
                 url: npsAdminContainer.jzURL('NPSAdministrationController.getNPSLineChart')+ "&typeId="+$scope.typeId + "&chartType="+$scope.selectedChartType.value
             }).then(function successCallback(data) {
                 $scope.weeklyNpScore = data.data;
                var NPSArray = [];
                for(var i = 0; i < $scope.weeklyNpScore.length; i++) {
                    var obj = $scope.weeklyNpScore[i];
                        NPSArray.push({ c: [{v: obj.npsDate}, {v: obj.score, f:  obj.npsDetails}, { v: 70 }, { v: 25 }]});
                }

                    $scope.myChartObject.data = {
                                         "cols": [{
                                             id: "week",
                                             label: "Week",
                                             type: "string"
                                         }, {
                                             id: "nps-id",
                                             label: "NPS",
                                             type: "number"
                                         }, {
                                             id: "industry-high",
                                             label: "Industry High",
                                             type: "number"
                                         }, {
                                             id: "industry-low",
                                             label: "Industry Low",
                                             type: "number"
                                         }                         ],
                                         "rows": NPSArray
                                     };

                 deferred.resolve(data);
 //                $scope.setResultMessage(data, "success");
             }, function errorCallback(data) {
                 $scope.setResultMessage($scope.i18n.defaultError, "error");
             });

}


$scope.hideSeries= function(selectedItem) {
            var col = selectedItem.column;
            if (selectedItem.row === null) {
                if ($scope.myChartObject.view.columns[col] == col) {
                    $scope.myChartObject.view.columns[col] = {
                        label: $scope.myChartObject.data.cols[col].label,
                        type: $scope.myChartObject.data.cols[col].type,
                        calc: function() {
                            return null;
                        }
                    };
                    $scope.myChartObject.options.colors[col - 1] = '#CCCCCC';
                }
                else {
                    $scope.myChartObject.view.columns[col] = col;
                    $scope.myChartObject.options.colors[col - 1] = $scope.myChartObject.options.defaultColors[col - 1];
                }
            }
        }


        $scope.saveScoreType= function(newScoreType)
        {
            $scope.showAlert = false;
            if(newScoreType){
               console.warn(((newScoreType.typeName != undefined) && (!newScoreType.linkedToSpace)) || ((newScoreType.linkedToSpace) && (newScoreType.typeName != undefined) && (newScoreType.spaceId != undefined) && (newScoreType.userId != undefined)));

                if(((newScoreType.typeName != undefined) && (!newScoreType.linkedToSpace)) || ((newScoreType.linkedToSpace) && (newScoreType.typeName != undefined) && (newScoreType.spaceId != undefined) && (newScoreType.userId != undefined))){

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
                }else{
                     $scope.loadScoreTypes(false);
                    $scope.showAlert = true;
                    $scope.setResultMessage($scope.i18n.defaultError, "error");
                }
            }else{
                  $scope.loadScoreTypes(false);
                 $scope.showAlert = true;
                 $scope.setResultMessage($scope.i18n.defaultError, "error");
             }
        }

         $scope.upadteScoreType= function(newScoreType)
         {
            $scope.showAlert = false;
            console.warn(((newScoreType.typeName != undefined) && (!newScoreType.linkedToSpace)) || ((newScoreType.linkedToSpace) && (newScoreType.typeName != undefined) && (newScoreType.spaceId != undefined) && (newScoreType.userId != undefined)));
            if(((newScoreType.typeName != undefined) && (!newScoreType.linkedToSpace)) || ((newScoreType.linkedToSpace) && (newScoreType.typeName != undefined) && (newScoreType.spaceId != undefined) && (newScoreType.userId != undefined))){

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
            }else{
                $scope.showAlert = true;
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            }
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

        $scope.getSpace = function (nameToSearch) {
            var rsetUrl = "/rest/nps/spaces/find?nameToSearch=" + nameToSearch + "&currentUser=" + $scope.i18n.currentUser;
            $http({
                method: 'GET',
                url: rsetUrl
            }).then(function successCallback(data) {
                console.warn(data);
                /* create a table of users IDs*/
                $(".newSpaceName").autocomplete({
                    source: function( request, response ) {
                        var users = [];
                        angular.forEach(data.data.options, function (value, key) {
                            users[key] = [];
                            users[key]['value'] = value.value;
                            users[key]['fullName'] = value.text;
                            users[key]['avatar'] = value.avatarUrl || '/eXoSkin/skin/images/system/UserAvtDefault.png';
                        });
                        response( users );
                    },
                    minLength: 3,
                    focus: function (event, ui) {

                            $(".newSpaceName").val(ui.item.value);
                            $scope.scoreTypeToEdit.spaceId = ui.item.value;

                        return false;
                    },
                    select: function (event, ui) {
                        $(".newSpaceName").val(ui.item.value);
                       $scope.scoreTypeToEdit.spaceId = ui.item.value;

                        return false;
                    }
                }).autocomplete("instance")._renderItem = function (ul, item) {

                    return $("<li>")
                        .append("<div> <img src='" + item.avatar + "' class='avataruser' /> " + item.fullName + "</div>")
                        .appendTo(ul);
                };

            }, function errorCallback(data) {
                console.log("error getEmployees");
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        };

        $scope.getEmployees = function (nameToSearch, spaceURL) {
            var rsetUrl = "/rest/nps/users/find?nameToSearch=" + nameToSearch + "&spaceURL="+spaceURL+"&currentUser=" + $scope.i18n.currentUser;

            $http({
                method: 'GET',
                url: rsetUrl
            }).then(function successCallback(data) {
                console.warn(data);
                /* create a table of users IDs*/
                $(".newUserName").autocomplete({
                    source: function( request, response ) {
                        var users = [];
                        angular.forEach(data.data.options, function (value, key) {
                            users[key] = [];
                            users[key]['value'] = value.value;
                            users[key]['fullName'] = value.text;
                            users[key]['avatar'] = value.avatarUrl || '/eXoSkin/skin/images/system/UserAvtDefault.png';
                        });
                        response( users );
                    },
                    minLength: 3,
                    focus: function (event, ui) {
                    $(".newUserName").val(ui.item.value);
                        $scope.scoreTypeToEdit.userId = ui.item.value;
                        return false;
                    },
                    select: function (event, ui) {
                    $(".newUserName").val(ui.item.value);
                        $scope.scoreTypeToEdit.userId = ui.item.value;
                        return false;
                    }
                }).autocomplete("instance")._renderItem = function (ul, item) {

                    return $("<li>")
                        .append("<div> <img src='" + item.avatar + "' class='avataruser' /> " + item.fullName + "</div>")
                        .appendTo(ul);
                };

            }, function errorCallback(data) {
                console.log("error getEmployees");
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }
    };

        return npsAdminCtrl;

    /*
     $timeout(function() {
     $scope.showAlert = false;
     }, 2000);
     */
});

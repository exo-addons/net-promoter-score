define("npsChartsControllers", ["SHARED/jquery", "SHARED/juzu-ajax"], function ($, jz) {
    var npsChartsCtrl = function ($scope, $q, $timeout, $http, $filter) {
        var npsChartsContainer = $('#npsCharts');
        var deferred = $q.defer();
        $scope.typeId = 0;
        $scope.weeklyNpScore=[];
        $scope.byWeeklyNpScore=[];
        $scope.showGraphs = false;
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

         $scope.weeklyChartObject = {};
         $scope.weeklyChartObject.type = "LineChart";
         $scope.weeklyChartObject.displayed = false;


           $scope.weeklyChartObject.options = {
                         "title": "Weekly NPS",
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

                     $scope.weeklyChartObject.view = {
                         columns: [0, 1, 2, 3]
                     };

         $scope.byWeekChartObject = {};
         $scope.byWeekChartObject.type = "LineChart";
         $scope.byWeekChartObject.displayed = false;


           $scope.byWeekChartObject.options = {
                         "title": "NPS by week",
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

                     $scope.byWeekChartObject.view = {
                         columns: [0, 1, 2, 3]
                     };



        $scope.setResultMessage = function (text, type) {
            $scope.resultMessageClass = "alert-" + type;
            $scope.resultMessageClassExt = "uiIcon" + type.charAt(0).toUpperCase()+ type.slice(1);
            $scope.showAlert = true;
            $scope.resultMessage = text;
        }



        $scope.updateSettings = function() {
                  console.log("Update portlet settings");
                  $http({
                    data : $scope.npsSetting,
                    method : 'POST',
                     headers : {
                       'Content-Type' : 'application/json'
                    },
                    url : npsViewContainer.jzURL('NPSChartsController.updateSettings')
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
                url: npsChartsContainer.jzURL('NPSChartsController.getBundle') + "&locale=" + eXo.env.portal.language
            }).then(function successCallback(data) {
                $scope.i18n = data.data;
                deferred.resolve(data);
                /*$scope.setResultMessage(data, "success");*/
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }



        $scope.loadData = function () {
            $http({
                method: 'GET',
                url: npsChartsContainer.jzURL('NPSChartsController.getData')
            }).then(function successCallback(data) {
                $scope.scorsnbr = data.data.scorsnbr;
				$scope.detractorsNbr = data.data.detractorsNbr;
				$scope.promotersNbr = data.data.promotersNbr;
				$scope.passivesNbr = data.data.passivesNbr;
				$scope.detractorsPrc = data.data.detractorsPrc;
				$scope.promotersPrc = data.data.promotersPrc;
				$scope.passivesPrc = data.data.passivesPrc;
                $scope.npScore = data.data.npScore;
                $scope.typeId= data.data.typeId;
                $scope.weeklyNpScore = data.data.weeklyNpScore;
                $scope.byWeeklyNpScore = data.data.byWeeklyNpScore;
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

               $scope.getByWeekNPSLineChart();
               $scope.getWeeklyNPSLineChart()


                $scope.showGraphs = true;
                deferred.resolve(data);
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }



            $scope.hideWeeklySeries= function(selectedItem) {
            var col = selectedItem.column;
            if (selectedItem.row === null) {
                if ($scope.weeklyChartObject.view.columns[col] == col) {
                    $scope.weeklyChartObject.view.columns[col] = {
                        label: $scope.weeklyChartObject.data.cols[col].label,
                        type: $scope.weeklyChartObject.data.cols[col].type,
                        calc: function() {
                            return null;
                        }
                    };
                    $scope.weeklyChartObject.options.colors[col - 1] = '#CCCCCC';
                }
                else {
                    $scope.weeklyChartObject.view.columns[col] = col;
                    $scope.weeklyChartObject.options.colors[col - 1] = $scope.weeklyChartObject.options.defaultColors[col - 1];
                }
            }
        }

                    $scope.hideByWeekSeries= function(selectedItem) {
                    var col = selectedItem.column;
                    if (selectedItem.row === null) {
                        if ($scope.byWeekChartObject.view.columns[col] == col) {
                            $scope.byWeekChartObject.view.columns[col] = {
                                label: $scope.byWeekChartObject.data.cols[col].label,
                                type: $scope.byWeekChartObject.data.cols[col].type,
                                calc: function() {
                                    return null;
                                }
                            };
                            $scope.byWeekChartObject.options.colors[col - 1] = '#CCCCCC';
                        }
                        else {
                            $scope.byWeekChartObject.view.columns[col] = col;
                            $scope.byWeekChartObject.options.colors[col - 1] = $scope.byWeekChartObject.options.defaultColors[col - 1];
                        }
                    }
                }

                       $scope.getWeeklyNPSLineChart = function () {
                             $http({
                                 method: 'GET',
                                 url: npsChartsContainer.jzURL('NPSChartsController.getNPSLineChart')+ "&typeId="+$scope.typeId + "&chartType=weekly"
                             }).then(function successCallback(data) {
                                 $scope.weeklyNpScore = data.data;
                                var NPSArray = [];
                                for(var i = 0; i < $scope.weeklyNpScore.length; i++) {
                                    var obj = $scope.weeklyNpScore[i];
                                        NPSArray.push({ c: [{v: obj.npsDate}, {v: obj.score, f: obj.npsDetails }, { v: 70 }, { v: 25 }]});
                                }

                                    $scope.weeklyChartObject.data = {
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

                        $scope.getByWeekNPSLineChart = function () {
                              $http({
                                  method: 'GET',
                                  url: npsChartsContainer.jzURL('NPSChartsController.getNPSLineChart')+ "&typeId="+$scope.typeId + "&chartType=byWeek"
                              }).then(function successCallback(data) {
                                  $scope.weeklyNpScore = data.data;
                                 var NPSArray = [];
                                 for(var i = 0; i < $scope.weeklyNpScore.length; i++) {
                                     var obj = $scope.weeklyNpScore[i];
                                         NPSArray.push({ c: [{v: obj.npsDate}, {v: obj.score, f: obj.npsDetails }, { v: 70 }, { v: 25 }]});
                                 }

                                     $scope.byWeekChartObject.data = {
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

        $scope.loadBundle();
        $scope.loadData();
        $('#npsCharts').css('visibility', 'visible');
        $(".npsLoadingBar").remove();

    };
        return npsChartsCtrl;

});

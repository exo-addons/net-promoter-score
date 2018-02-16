define("npsChartsControllers", ["SHARED/jquery", "SHARED/juzu-ajax"], function ($, jz) {
    var npsChartsCtrl = function ($scope, $q, $timeout, $http, $filter) {
        var npsChartsContainer = $('#npsCharts');
        var deferred = $q.defer();
        $scope.typeId = 0;
        $scope.statNpScore=[];

        $scope.showGraphs = false;
        $scope.chartTypes = [
            {name : "Global weekly NPS", value : "global"},
            {name : "Month over Month", value : "monthlyOver"},
            {name : "Week over week", value : "weeklyOver"},
            {name : "30-Days rolling Avg", value : "rolling30"},
            {name : "7-Days rolling Avg", value : "rolling7"},
        ];
        $scope.periods = [
            {name : "By week", value : "weekly"},
            {name : "By month", value : "mounthly"}
        ];
        $scope.selectedChartType={name : "Global NPS", value : "global"};
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



         $scope.lineChartObject = {};
         $scope.lineChartObject.type = "LineChart";
         $scope.lineChartObject.displayed = false;


           $scope.lineChartObject.options = {
                         "colors": ['#4285f4', '#00b36b', '#db4437'],
						 "series": {
            0: { lineWidth: 3},
            1: { lineWidth: 1, lineDashStyle: [5, 4] },
            2: { lineWidth: 1, lineDashStyle: [5, 4] }
          },
                         "defaultColors": ['#0000FF', '#009900', '#CC0000'],
						 "isStacked": "false",
                         "fill": 100,
                         "displayExactValues": true,
						 "curveType": "function",
						 "tooltip": { isHtml: true },
						 "focusTarget": 'category',
                         "vAxis": {
                             "title": "",
                             "gridlines": {
                                 "count": 10
                             }
                         },
                         "hAxis": {

                         }
                     };

                     $scope.lineChartObject.view = {
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
                $scope.statNpScore = data.data.statNpScore;
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



                $scope.showGraphs = true;
                deferred.resolve(data);
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }


                    $scope.hideLineChartSeries= function(selectedItem) {
                    var col = selectedItem.column;
                    if (selectedItem.row === null) {
                        if ($scope.lineChartObject.view.columns[col] == col) {
                            $scope.lineChartObject.view.columns[col] = {
                                label: $scope.lineChartObject.data.cols[col].label,
                                type: $scope.lineChartObject.data.cols[col].type,
                                calc: function() {
                                    return null;
                                }
                            };
                            $scope.lineChartObject.options.colors[col - 1] = '#CCCCCC';
                        }
                        else {
                            $scope.lineChartObject.view.columns[col] = col;
                            $scope.lineChartObject.options.colors[col - 1] = $scope.lineChartObject.options.defaultColors[col - 1];
                        }
                    }
                }



                        $scope.getNPSLineChart = function () {
                              $http({
                                  method: 'GET',
                                  url: npsChartsContainer.jzURL('NPSChartsController.getNPSLineChart')+ "&typeId="+$scope.typeId + "&chartType="+$scope.selectedChartType.value
                              }).then(function successCallback(data) {
                                  $scope.statNpScore = data.data;
                                 var NPSArray = [];
                                 for(var i = 0; i < $scope.statNpScore.length; i++) {
                                     var obj = $scope.statNpScore[i];
                                         NPSArray.push({ c: [{v: ""}, {v: obj.score, f: obj.npsDetails }, { v: 70 }, { v: 25 }]});
                                 }

                                     $scope.lineChartObject.data = {
                                                          "cols": [{
                                                              id: "day",
                                                              label: "Day",
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

define("npsAdminControllers", ["SHARED/jquery", "SHARED/juzu-ajax"], function ($, jz) {
    var npsAdminCtrl = function ($scope,$sce, $q, $timeout, $http, $filter) {
        var npsAdminContainer = $('#npsAdmin');
        var deferred = $q.defer();
        $scope.currentUser = "";
        $scope.currentUserAvatar = "";
        $scope.currentUserName = "";
        $scope.newScoreType = null;
        $scope.scoreTypeToEdit = null;
        $scope.showEditForm = false;
        $scope.typeId = 1;
        $scope.meanScore = 0;
        $scope.classMeanScore = "promoter";
        $scope.respCat = "all";
        $scope.repFilter = "all";
        $scope.selectModel = {};
        $scope.showForm = false;
        $scope.showTrGraphs = false;
        $scope.showDashGraphs = false;
        $scope.scores = [];
        $scope.scoreTypes = [];
        $scope.scoresSum = 0;
        $scope.weeklyNpScore = [];
        $scope.chartTypes = [];
        $scope.selectedChartType = {};
		$scope.startDate=0;
		$scope.endDate=(new Date()).getTime();
		$scope.newNote = {id: null};
		$scope.newSubNote = {id: null};
		$scope.npsAdminApp = true;
		$scope.canManage = true;
		$scope.showGetAll=false;


              var lessThan3Day = new Date();
              lessThan3Day.setDate(lessThan3Day.getDate() - 2);
              $scope.customTemplates = [
                {
                  name: 'Last 3 Days',
                  dateStart: lessThan3Day,
                  dateEnd: new Date(),
                }
              ];
              $scope.calendarModel = { selectedTemplate: 'Last 3 Days' };
              $scope.pickerModel = { selectedTemplate: 'Last 3 Days' };
              $scope.advancedModel = { selectedTemplate: 'Last 3 Days' };
              $scope.serviceModel = { selectedTemplate: 'Last 3 Days' };
              $scope.selectedDate = {};
              $scope.selectedDates = [];
              $scope.selectDateRange = function ($event, autoConfirm) {
                $mdDateRangePicker.show({
                  model: $scope.serviceModel,
                  autoConfirm: autoConfirm,
                  targetEvent: $event,
                  customTemplates: $scope.customTemplates,
                }).then(function (result) {
                  Object.assign($scope.serviceModel, result);
                }).catch(function () {
                  console.log('Cancelled');
                });
              }
              $scope.format = function (dateStart, dateEnd, template, templateName) {
                return template ? 'Range of ' + (templateName || template) : (dateStart && dateEnd && (dateStart.toLocaleDateString() + ' to ' + dateEnd.toLocaleDateString()));
              }
              $scope.isDisabledDate = function (d) {
                return d.getDay() === 0 || d.getDay() === 6;
              }


        $scope.periods = [
            {name: "By week", value: "weekly"},
            {name: "By month", value: "mounthly"}
        ];
        $scope.selectedChartType = {name: "30-Days rolling Avg", value: "rolling30"};


        $scope.lineChartObject = {};
        $scope.lineChartObject.type = "LineChart";
        $scope.lineChartObject.displayed = false;


        $scope.lineChartObject.options = {
            "colors": ['#4285f4', '#00b36b', '#db4437'],
            "series": {
                0: {lineWidth: 3},
                1: {lineWidth: 1, lineDashStyle: [5, 4]},
                2: {lineWidth: 1, lineDashStyle: [5, 4]}
            },
            "defaultColors": ['#4285f4', '#00b36b', '#db4437'],
            "isStacked": "false",
            "fill": 100,
            "displayExactValues": true,
            "curveType": "function",
            "tooltip": {isHtml: true},
            "focusTarget": 'category',
            "height": 450,
            "vAxis": {
                "title": "",
                "gridlines": {
                    "count": 10
                }
            }
        };
        $scope.lineChartObject.view = {
            columns: [0, 1, 2, 3]
        };

        $scope.ColumnChartObject = {};

        $scope.ColumnChartObject.type = "ColumnChart";

        $scope.ColumnChartObject.options = {
            width:817,height:500,
            colors: ['#476a9c']
        };


        $scope.itemsPerPageSugg = [5, 10, 20, 50];
        $scope.itemsPerPage = 5;
        $scope.currentPage = 0;
        $scope.pages = [];
        $scope.scoreToDelete = null;
        $scope.showDeletePopup = false;


        $scope.setResultMessage = function (text, type) {
            $scope.resultMessageClass = "alert-" + type;
            $scope.resultMessageClassExt = "uiIcon" + type.charAt(0).toUpperCase() + type.slice(1);
            $scope.showAlert = true;
            $scope.resultMessage = text;
        }

        $scope.setRange = function (dates) {
            $scope.showGetAll=true;
			$scope.startDate=dates[0].getTime();
			$scope.endDate=dates[dates.length-1].getTime();
			$scope.loadData($scope.typeId);
			$scope.getNPSLineChart();
			$scope.loadScores($scope.typeId, $scope.currentPage * $scope.itemsPerPage, $scope.itemsPerPage, $scope.respCat);
        }

        $scope.openTab = function (tabName) {

            $("#dashboard").css("display", "none");
            $("#trends").css("display", "none");
            $("#feedbacks").css("display", "none");

            $("#dashboardTab").removeClass("active");
            $("#trendsTab").removeClass("active");
            $("#feedbacksTab").removeClass("active");

            $("#" + tabName).css("display", "block");
            $("#" + tabName + "Tab").addClass("active");
        }

        $scope.setCategory = function (category) {
            $scope.scorstoShownbr = $scope.scorsnbr;
            if (category == "promoters")
                $scope.scorstoShownbr = $scope.allPromotersNbr;
            if (category == "detractors")
                $scope.scorstoShownbr = $scope.allDetractorsNbr;
            if (category == "passives")
                $scope.scorstoShownbr = $scope.allPassivesNbr;
            $scope.respCat = category;
            $scope.setPage(0);
        }

         $scope.setFilter = function (repFilter) {
            $scope.repFilter = repFilter;
            $scope.setPage(0);
         }

        $scope.loadScoreTypes = function (isDefault, question = null) {
            $http({
                method: 'GET',
                url: npsAdminContainer.jzURL('NPSAdministrationController.getScoreTypes')
            }).then(function successCallback(data) {
                $scope.scoreTypes = data.data;
                if (isDefault && $scope.scoreTypes.length > 0) {
                    $scope.selectModel = $scope.scoreTypes[0];
                    $scope.typeId = $scope.scoreTypes[0].id;
                    $scope.getScoresbyType($scope.typeId);
                } else if ((!isDefault) && (question != null)) {
                    $scope.selectModel = $scope.scoreTypes[question - 1];
                    $scope.typeId = question;
                    $scope.getScoresbyType($scope.typeId);
                } else if ((!isDefault) && (question == null)) {
                    $scope.selectModel = $scope.scoreTypes[$scope.scoreTypes.length - 1];
                    $scope.typeId = $scope.scoreTypes.length;
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
                $scope.chartTypes = [
                    {name: data.data.rolling30, value: "rolling30"},
                    {name: data.data.global, value: "global"},
                    {name: data.data.monthlyOver, value: "monthlyOver"},
                    {name: data.data.weeklyOver, value: "weeklyOver"},
                ];
                $scope.currentUser = data.data.currentUser;
                $scope.currentUserAvatar = data.data.currentUserAvatar;
                $scope.currentUserName = data.data.currentUserName;
                $scope.selectedChartType = {name: data.data.rolling30, value: "rolling30"};
                deferred.resolve(data);
                /*$scope.setResultMessage(data, "success");*/
                $scope.showAlert = false;
                if(data.data.scoreTypeId!=null){
                $scope.typeId = data.data.scoreTypeId;
                $scope.getScoresbyType($scope.typeId);
                $scope.typeName = data.data.typeName;
                $scope.npsAdminApp=false;
                $scope.canManage=data.data.canManage;
                }else{
                $scope.canManage=true;
                $scope.loadScoreTypes(true);
                }

            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }


        $scope.loadScores = function (typeId, offset, limit, respCat) {
            $http({
                method: 'GET',
                url: npsAdminContainer.jzURL('NPSAdministrationController.getScores') + "&typeId=" + typeId + "&respCat=" + respCat + "&offset=" + offset + "&limit=" + limit + "&startDate=" + $scope.startDate + "&endDate=" + $scope.endDate
            }).then(function successCallback(data) {
                $scope.scores = data.data;
                $scope.showAlert = false;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });

        };


        $scope.loadData = function (typeId) {
			if ($scope.showGetAll==false){
				$scope.startDate=0;
			}
            $http({
                method: 'GET',
                url: npsAdminContainer.jzURL('NPSAdministrationController.getData') + "&typeId=" + typeId + "&startDate=" + $scope.startDate + "&endDate=" + $scope.endDate
            }).then(function successCallback(data) {

                $scope.scorsnbr = data.data.scorsnbr;
                $scope.enabledScorsNbr = data.data.enabledScorsNbr;
                $scope.detractorsNbr = data.data.detractorsNbr;
                $scope.promotersNbr = data.data.promotersNbr;
                $scope.passivesNbr = data.data.passivesNbr;
                $scope.detractorsPrc = data.data.detractorsPrc;
                $scope.promotersPrc = data.data.promotersPrc;
                $scope.passivesPrc = data.data.passivesPrc;
                $scope.allDetractorsNbr = data.data.allDetractorsNbr;
                $scope.allPromotersNbr = data.data.allPromotersNbr;
                $scope.allPassivesNbr = data.data.allPassivesNbr;
                $scope.npScore = data.data.npScore;
                $scope.scorstoShownbr = $scope.scorsnbr;
                $scope.disablesScoresNbr = data.data.disablesScoresNbr;
                $scope.meanScore = data.data.meanScore;
				$scope.startDate = data.data.startDate;
				$scope.responseRate = Math.floor(data.data.responseRate);

          		$scope.pickerModel = {
                    selectedTemplateName:$scope.getDateText(),
                };

                if(data.data.meanScore < 7){
                    $scope.classMeanScore = "detractor";
                }else if((data.data.meanScore >=7) && (data.data.meanScore <=8)) {
                    $scope.classMeanScore = "passive";
                }

                if ($scope.npScore == "NaN") {
                    $scope.showDashGraphs = false;
                    $scope.showTrGraphs = false;
                } else {
                    $scope.showDashGraphs = true;
                    $scope.npScore = parseFloat($scope.npScore);
                            $scope.dashoffset = data.data.dashoffset;
                            $scope.npsColor = "#f5ba7f";
                            if($scope.npScore<25){
                            $scope.npsColor = "#ce7474";
                            }
                            if($scope.npScore>70){
                            $scope.npsColor = "#4bc0c0";
                            }


                    if($scope.npScore <= 6){$scope.npsColor = "rgba(144, 19, 21, 0.7)"}
                    else if(($scope.npScore >= 7) && ($scope.npScore < 8)){$scope.npsColor = "rgba(239, 149, 13,  0.7)"}
                    else if($scope.npScore >= 8){$scope.npsColor = "rgba(60, 175, 140, 0.7)"}


                }
               // $scope.getNPSLineChart();
                $scope.getResponsesByScoreChart();
                $scope.pages = $scope.range();
                // $scope.getScoresbyType(typeId);

                deferred.resolve(data);
//                $scope.setResultMessage(data, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }

        $scope.getNPSLineChart = function () {
        $scope.showTrGraphs = false;
            $http({
                method: 'GET',
                url: npsAdminContainer.jzURL('NPSAdministrationController.getNPSLineChart') + "&typeId=" + $scope.typeId + "&chartType=" + $scope.selectedChartType.value+ "&startDate=" + $scope.startDate + "&endDate=" + $scope.endDate
            }).then(function successCallback(data) {
                $scope.statNpScore = data.data;
                var NPSArray = [];
                for (var i = 0; i < $scope.statNpScore.length; i++) {
                    var obj = $scope.statNpScore[i];
                    NPSArray.push({c: [{v: obj.npsDate}, {v: obj.score, f: obj.npsDetails}, {v: 70}, {v: 25}]});
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
                        }],
                    "rows": NPSArray
                };
            $scope.showTrGraphs = true;
                deferred.resolve(data);
                //                $scope.setResultMessage(data, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }


        $scope.getResponsesByScoreChart = function () {
            $http({
                method: 'GET',
                url: npsAdminContainer.jzURL('NPSAdministrationController.getRespCountByScore') + "&typeId=" + $scope.typeId + "&startDate=" + $scope.startDate + "&endDate=" + $scope.endDate
            }).then(function successCallback(data) {
                $scope.statNpScore = data.data;
                $scope.statColumnScore = data.data;
                var NPSArray = [];
                var color = "#476a9c";
                for (var i = 0; i < $scope.statNpScore.length; i++) {
                    var obj = $scope.statNpScore[i];
                    NPSArray.push({c: [{v: obj.score}, {v: obj.count}]});
                }

                $scope.ColumnChartObject.data =
                    {"cols": [
                        {id: "t", label: "", type: "string", color: ""},
                        {id: "s", label: "", type: "number", color: ""}
                    ], "rows": NPSArray};

                deferred.resolve(data);
                //                $scope.setResultMessage(data, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }



        $scope.hideSeries = function (selectedItem) {
            var col = selectedItem.column;
            if (selectedItem.row === null) {
                if ($scope.lineChartObject.view.columns[col] == col) {
                    $scope.lineChartObject.view.columns[col] = {
                        label: $scope.lineChartObject.data.cols[col].label,
                        type: $scope.lineChartObject.data.cols[col].type,
                        calc: function () {
                            return null;
                        }
                    };
                    $scope.lineChartObject.options.colors[col - 1] = '#CCCCCC';
                } else {
                    $scope.lineChartObject.view.columns[col] = col;
                    $scope.lineChartObject.options.colors[col - 1] = $scope.lineChartObject.options.defaultColors[col - 1];
                }
            }
        }


        $scope.saveScoreType = function (newScoreType)
        {
            $scope.showAlert = false;
            if (newScoreType) {
                console.warn(((newScoreType.typeName != undefined) && (!newScoreType.linkedToSpace)) || ((newScoreType.linkedToSpace) && (newScoreType.typeName != undefined) && (newScoreType.spaceId != undefined) && (newScoreType.userId != undefined)));

                if (((newScoreType.typeName != undefined) && (!newScoreType.linkedToSpace)) || ((newScoreType.linkedToSpace) && (newScoreType.typeName != undefined) && (newScoreType.spaceId != undefined) && (newScoreType.userId != undefined))) {

                    $http({
                        data: newScoreType,
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        url: npsAdminContainer.jzURL('NPSAdministrationController.saveType')
                    }).then(function successCallback(data) {
                        //                        $scope.setResultMessage($scope.i18n.typeSaved, "success");
                        $scope.loadScoreTypes(false);
                    }, function errorCallback(data) {
                        $scope.setResultMessage($scope.i18n.defaultError, "error");
                    });
                } else {
                    $scope.loadScoreTypes(false);
                    $scope.showAlert = true;
                    $scope.setResultMessage($scope.i18n.defaultError, "error");
                }
            } else {
                $scope.loadScoreTypes(false);
                $scope.showAlert = true;
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            }
        }

        $scope.upadteScoreType = function (newScoreType)
        {
            $scope.showAlert = false;
            console.warn(((newScoreType.typeName != undefined) && (!newScoreType.linkedToSpace)) || ((newScoreType.linkedToSpace) && (newScoreType.typeName != undefined) && (newScoreType.spaceId != undefined) && (newScoreType.userId != undefined)));
            if (((newScoreType.typeName != undefined) && (!newScoreType.linkedToSpace)) || ((newScoreType.linkedToSpace) && (newScoreType.typeName != undefined) && (newScoreType.spaceId != undefined) && (newScoreType.userId != undefined))) {

                $http({
                    data: newScoreType,
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    url: npsAdminContainer.jzURL('NPSAdministrationController.updateType')
                }).then(function successCallback(data) {
//                                 $scope.setResultMessage($scope.i18n.typeSaved, "success");
                    $scope.loadScoreTypes(false, newScoreType.id);
                }, function errorCallback(data) {
                    $scope.setResultMessage($scope.i18n.defaultError, "error");
                });
            } else {
                $scope.showAlert = true;
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            }
        }



        $scope.getScoreType = function (id) {
            $http({
                method: 'GET',
                url: npsAdminContainer.jzURL('NPSAdministrationController.getScoreTypeById') + "&id=" + id
            }).then(function successCallback(data) {
                $scope.scoreTypeToEdit = data.data;
                $scope.showAlert = false;
                $scope.showEditForm = true;
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });

        };

        $scope.deleteScore = function (score) {
            $scope.scoreToDelete = score;
            $("#voteName").text(score.userFullName);
            $scope.showDeletePopup = true;
//            $(".npsAdminPopup").css("display", "block");
        }

        $scope.validatedDeleteScore = function (score) {
            $scope.showDeletePopup = false;
            // $scope.setResultMessage($scope.i18n.savingScore, "info");
            $http({
                data: score,
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                url: npsAdminContainer.jzURL('NPSAdministrationController.deleteScore')
            }).then(function successCallback(data) {
                $scope.loadData($scope.typeId);
                $scope.loadScores($scope.typeId, $scope.currentPage * $scope.itemsPerPage, $scope.itemsPerPage, $scope.respCat);
                $scope.setResultMessage($scope.i18n.scoreDeleted, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });
        }


        $scope.disableScore = function (score) {
            $scope.showAlert = false;
            // $scope.setResultMessage($scope.i18n.savingScore, "info");
            $http({
                data: score,
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                url: npsAdminContainer.jzURL('NPSAdministrationController.disableScore')
            }).then(function successCallback(data) {
                $scope.loadData($scope.typeId);
                $scope.loadScores($scope.typeId, $scope.currentPage * $scope.itemsPerPage, $scope.itemsPerPage, $scope.respCat);
                $scope.setResultMessage($scope.i18n.scoreEnabled, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });

        }


        $scope.saveNote = function (note, activityId, notes, commentId) {
            note.posterId = $scope.currentUser;
            note.posterAvatar = $scope.currentUserAvatar;
            note.posterName = $scope.currentUserName;
            note.activityId=activityId;
            note.commentId=commentId;
            $http({
                data: note,
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                url: npsAdminContainer.jzURL('NPSAdministrationController.saveNote')
            }).then(function successCallback(data) {
            //notes.push(note)
			$scope.newNote = {id: null};
			$scope.newSubNote = {id: null};
			$scope.loadScores($scope.typeId, $scope.currentPage * $scope.itemsPerPage, $scope.itemsPerPage, $scope.respCat);
			$('.commForm').css("display", "none");
               // $scope.setResultMessage($scope.i18n.scoreEnabled, "success");
            }, function errorCallback(data) {
               // $scope.setResultMessage($scope.i18n.defaultError, "error");
            });

        }

        $scope.enableScore = function (score) {
            $scope.showAlert = false;
            // $scope.setResultMessage($scope.i18n.savingScore, "info");
            $http({
                data: score,
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                url: npsAdminContainer.jzURL('NPSAdministrationController.enableScore')
            }).then(function successCallback(data) {
                $scope.loadData($scope.typeId);
                $scope.loadScores($scope.typeId, $scope.currentPage * $scope.itemsPerPage, $scope.itemsPerPage, $scope.respCat);
                $scope.setResultMessage($scope.i18n.scoreDisabled, "success");
            }, function errorCallback(data) {
                $scope.setResultMessage($scope.i18n.defaultError, "error");
            });

        }

        $scope.checkState = function (score, state) {
            state ? $scope.enableScore(score) : $scope.disableScore(score);
        }

        $scope.range = function () {
            var rangeSize = 10;
            var ret = [];
            var start = 0;
            var pgCount = $scope.pageCount();
            if (pgCount > rangeSize) {
                var d = $scope.currentPage - Math.ceil(rangeSize / 2);
                if (d > 0)
                    start = d;
            }
            var end = start + rangeSize;
            if (end > pgCount)
                end = pgCount;
            for (var i = start; i < end; i++) {
                ret.push(i);
            }
            return ret;
        };

        $scope.prevPage = function () {
            if ($scope.currentPage > 0) {
                $scope.currentPage--;
                $scope.pages = $scope.range();
                $scope.loadScores($scope.typeId, $scope.currentPage * $scope.itemsPerPage, $scope.itemsPerPage, $scope.respCat);
            }
        };

        $scope.prevPageDisabled = function () {
            return $scope.currentPage === 0 ? "disabled" : "";
        };

        $scope.nextPage = function () {
            if ($scope.currentPage < $scope.pageCount() - 1) {
                $scope.currentPage++;
                $scope.pages = $scope.range();
                $scope.loadScores($scope.typeId, $scope.currentPage * $scope.itemsPerPage, $scope.itemsPerPage, $scope.respCat);
            }
        };

        $scope.nextPageDisabled = function () {
            return $scope.currentPage === $scope.pageCount() - 1 ? "disabled" : "";
        };

        $scope.pageCount = function () {
            return Math.ceil($scope.scorstoShownbr / $scope.itemsPerPage);
        };

        $scope.setPage = function (n) {
            if (n >= 0 && n < $scope.pageCount()) {
                $scope.currentPage = n;
                $scope.pages = $scope.range();
                $scope.loadScores($scope.typeId, n * $scope.itemsPerPage, $scope.itemsPerPage, $scope.respCat);
            }
        };

        $scope.getScoresbyType = function (typeId) {
            $scope.typeId = typeId;
            $scope.loadData(typeId);
	//		$scope.getNPSLineChart();
            $scope.loadScores(typeId, 0, $scope.itemsPerPage, $scope.respCat);

            $scope.showGraphs = true;
        };



        $scope.getSpace = function (nameToSearch) {
            var rsetUrl = "/rest/nps/spaces/find?nameToSearch=" + nameToSearch + "&currentUser=" + $scope.i18n.currentUser;
            $http({
                method: 'GET',
                url: rsetUrl
            }).then(function successCallback(data) {
                console.warn(data);
                /* create a table of users IDs*/
                $(".newSpaceName").autocomplete({
                    source: function (request, response) {
                        var users = [];
                        angular.forEach(data.data.options, function (value, key) {
                            users[key] = [];
                            users[key]['value'] = value.value;
                            users[key]['fullName'] = value.text;
                            users[key]['avatar'] = value.avatarUrl || '/eXoSkin/skin/images/system/UserAvtDefault.png';
                        });
                        response(users);
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
            var rsetUrl = "/rest/nps/users/find?nameToSearch=" + nameToSearch + "&spaceURL=" + spaceURL + "&currentUser=" + $scope.i18n.currentUser;

            $http({
                method: 'GET',
                url: rsetUrl
            }).then(function successCallback(data) {
                console.warn(data);
                /* create a table of users IDs*/
                $(".newUserName").autocomplete({
                    source: function (request, response) {
                        var users = [];
                        angular.forEach(data.data.options, function (value, key) {
                            users[key] = [];
                            users[key]['value'] = value.value;
                            users[key]['fullName'] = value.text;
                            users[key]['avatar'] = value.avatarUrl || '/eXoSkin/skin/images/system/UserAvtDefault.png';
                        });
                        response(users);
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


        $scope.getLocaleDate = function(date) {
            if($scope.i18n&&$scope.i18n.offset){
                return date+$scope.i18n.offset;
            }else{
                return  date;
            }

        };
        $scope.getAll = function() {
            $scope.showGetAll=false;
        	$scope.startDate=0;
        	$scope.endDate=(new Date()).getTime();
            $scope.loadData($scope.typeId);
            $scope.loadScores($scope.typeId, $scope.currentPage * $scope.itemsPerPage, $scope.itemsPerPage, $scope.respCat);
        };

        $scope.renderHtml = function(html_code)
        {
            return $sce.trustAsHtml(html_code);
        };

		$scope.showCommentForm = function (score) {
            $("#commetfrom_"+score).css("display", "block");
        }

        $scope.showSubCommentForm= function (score) {
            $("#subcommetfrom_"+score).css("display", "block");
        }

        $scope.showCommentList= function (score) {
             $("#commentList_"+score).css("display", "block");
             $("#showComments_"+score).css("display", "none");
             $("#hideComments_"+score).css("display", "block");
        }

        $scope.hideCommentList= function (score) {
             $("#commentList_"+score).css("display", "none");
             $("#hideComments_"+score).css("display", "none");
             $("#showComments_"+score).css("display", "block");
        }

		 $scope.getDateText = function() {
			 var dateStart = new Date();
			 var dateEnd = new Date();
			 dateStart.setTime($scope.startDate);
			 dateEnd.setTime($scope.endDate);
			 if (getDateDiff(dateStart, dateEnd) === 0) {
				 return $filter('date')(dateStart, 'dd MMM yyyy');
			 } else {
				 return $filter('date')(dateStart, 'dd' + (dateStart.getMonth() !== dateEnd.getMonth() || dateStart.getFullYear() !== dateEnd.getFullYear() ? ' MMM' :
					 '') + (dateStart.getFullYear() !== dateEnd.getFullYear() ? ' yyyy' : '')) + ' - ' + $filter('date')(dateEnd, 'dd MMM yyyy');
			 }
		 }

		 function getDateDiff(date1, date2) {
			 if (!date1 || !date2) return;
			 var _d1 = new Date(date1.getFullYear(), date1.getMonth(),
					 date1.getDate()),
				 _d2 = new Date(date2.getFullYear(), date2.getMonth(), date2.getDate());
			 return _d2 - _d1;
		 }


        $scope.loadBundle();
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

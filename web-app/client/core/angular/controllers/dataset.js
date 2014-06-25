'use strict';

define(function () {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$routeParams', 'dataFactory',
    function($scope, $interval, $routeParams, dataFactory) {

    var datasetId = $routeParams.datasetId;

    dataFactory.getDatasetById(datasetId, function (dataset) {
      $scope.dataset = dataset;
    });

    dataFactory.getFlowsByDataset(datasetId, function(flow) {
      $scope.flow = flow;
    });


    $scope.$on("$destroy", function(){
      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });


  }];

  return Ctrl;

});
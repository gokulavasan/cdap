/*
 * Spark Controller
 */

define([], function () {

    var Controller = Em.Controller.extend({

        __STATUS_UPDATE_TIMEOUT: 1000,
        showLogsMessage: false,
        __updateStatusTimeout: null,
        __updateStatus: function(appName, jobName) {
            var model = this.get('model');

            this.HTTP.rest('apps', appName, 'spark', jobName, 'status', function (response) {

                if (!$.isEmptyObject(response)) {
                    model.set('currentState', response.status);
                }

            });
        },
        __updateMetrics: function () {
            this.get('model').getMetricsRequest(this.HTTP);
        },

        load: function () {
            var model = this.get('model');
            var self = this;

            var runStatusUpdate = function() {
                self.__updateStatusTimeout = setTimeout(function () {
                    self.__updateStatus(model.app, model.name);
                    self.__updateMetrics();
                    runStatusUpdate();
                }, self.__STATUS_UPDATE_TIMEOUT);
            };

            runStatusUpdate();
        },

        unload: function () {
            clearTimeout(this.__updateStatusTimeout);
        },

        /**
         * Action handlers from the View
         */
        exec: function () {

            var model = this.get('model'),
                action = model.get('defaultAction');

            if (action && action.toLowerCase() in model) {
                model[action.toLowerCase()](this.HTTP);
            }

        },

        config: function () {

            var self = this,
                model = this.get('model');

            this.transitionToRoute('SparkStatus.Config');

        },

        onCurrentStatusChanged: function() {
          if (this.get("model").get("currentState") === "RUNNING") {
            this.set("showLogsMessage", true);
          }
        }.observes("model.currentState"),

        logsLinkHandler: function() {
          this.transitionToRoute("Spark.Log");
        }
    });

    Controller.reopenClass({
        type: 'SparkStatus',
        kind: 'Controller'
    });

    return Controller;

});

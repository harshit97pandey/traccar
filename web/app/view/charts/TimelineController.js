Ext.define('Traccar.view.charts.TimelineController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.timeline',

    onAxisLabelRender: function (axis, label, layoutContext) {
        return label.toFixed(label < 10 ? 1: 0) + '%';
    },

    onSeriesTooltipRender: function (tooltip, record, item) {
        var title = item.series.getTitle();

        tooltip.setHtml(title + ' on ' + record.get('month') + ': ' +
            record.get(item.series.getYField()) + '%');
    },

    onColumnRender: function (v) {
        return v + '%';
    },

    onPreview: function () {
        var chart = this.lookupReference('chart');
        chart.preview();
    }

});
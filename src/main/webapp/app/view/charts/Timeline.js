Ext.define('Traccar.view.charts.Timeline', {
    extend: 'Ext.panel.Panel',
    xtype: 'timeline',
    requires: ['Traccar.view.charts.TimelineController'],
    controller: 'timeline',
    layout: {
        type: 'vbox',
        pack: 'center'
    },
    width: 650,
    items: [{
        xtype: 'cartesian',
        reference: 'chart',
        width: '100%',
        height: 500,
        legend: {
            docked: 'right'
        },
        store:  'Positions',
        insetPadding: 40,
        axes: [{
            type: 'numeric',
            fields: ['speed'],
            position: 'left',
            grid: true,
            minimum: 0,
            renderer: 'onAxisLabelRender'
        }, {
            type: 'category',
            fields: 'fixTime',
            position: 'bottom',
            grid: true,
            label: {
                rotate: {
                    degrees: -45
                }
            }
        }],
        series: [{
            type: 'line',
            title: 'Chrome',
            xField: 'speed',
            yField: 'fixTime',
            marker: {
                type: 'arrow',
                fx: {
                    duration: 200,
                    easing: 'backOut'
                }
            },
            highlightCfg: {
                scaling: 2
            },
            tooltip: {
                trackMouse: true,
                renderer: 'onSeriesTooltipRender'
            }
        }]
    }]
});

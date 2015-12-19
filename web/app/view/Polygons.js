Ext.define('Traccar.view.Polygons', {
    extend: 'Ext.form.Panel',
    xtype: 'polygonsView',
    header: false,
    requires: [
        'Traccar.view.PolygonsController'
    ],
    controller: 'polygons',

    title: 'Polygons',
    layout: 'fit',
    
    listeners: {
        afterrender: function () {
            
        }
    }
});
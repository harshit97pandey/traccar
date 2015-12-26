Ext.define('Traccar.view.Polygons', {
    extend: 'Ext.grid.Panel',
    xtype: 'polygonsView',
    requires: [
        'Traccar.view.PolygonsController'
    ],
    controller: 'polygons',
    store: 'Polygons',
    title: 'Polygons',
    layout: 'fit',
    tbar: {
        xtype: 'editToolbar',
        items: [{
            xtype: 'settingsMenu'
        }]
    },
    listeners: {

    },
    columns: [{
        text: Strings.deviceName,
        dataIndex: 'name',
        flex: 1
    }]
});
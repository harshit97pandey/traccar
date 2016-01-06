
Ext.define('Traccar.model.Alert', {
    extend: 'Ext.data.Model',
    identifier: 'negative',

    fields: [{
        name: 'id',
        type: 'int'
    },{
        name: 'deviceId',
        type: 'int'
    },{
        name: 'deviceName',
        type: 'string'
    }, {
        name: 'polygonId',
        type: 'int'
    }, {
        name: 'polygonName',
        type: 'string'
    }, {
        name: 'creationDate',
        type: 'date'
    }, {
        name: 'cancelDate',
        type: 'date'
    }]
});
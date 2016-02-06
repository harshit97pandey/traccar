Ext.define('Traccar.model.Polygon', {
    extend: 'Ext.data.Model',
    identifier: 'negative',
    fields: [{
        name: 'id',
        type: 'int'
    }, {
        name: 'type',
        type: 'string'
    }, {
        name: 'name',
        type: 'string'
    }, {
        name: 'coordinates'
    }]
});
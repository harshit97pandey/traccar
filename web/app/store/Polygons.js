
Ext.define('Traccar.store.Polygons', {
    extend: 'Ext.data.Store',
    model: 'Traccar.model.Polygon',

    proxy: {
        type: 'ajax',
        api: {
            create: '/api/polygon/add',
            read: '/api/polygon/get',
            update: '/api/polygon/update',
            destroy: '/api/polygon/remove'
        },
        reader: {
            type: 'json',
            rootProperty: 'data'
        },
        writer: {
            type: 'json',
            writeAllFields: true
        }
    }
});
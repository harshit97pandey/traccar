Ext.define('Traccar.store.Polygons', {
    extend: 'Ext.data.Store',
    model: 'Traccar.model.Polygon',
    identifier: 'negative',
    proxy: {
        type: 'ajax',
        api: {
            create: './api/polygon/add',
            read: './api/polygon/list',
            update: './api/polygon/update',
            destroy: './api/polygon/remove'
        },
        reader: {
            type: 'json'
        },
        writer: {
            type: 'json',
            writeAllFields: true
        }
    }
});
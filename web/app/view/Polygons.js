Ext.define('Traccar.view.Polygons', {
    extend : 'Ext.grid.Panel',
    xtype : 'polygonsView',
    requires : [ 'Traccar.view.PolygonsController' ],
    controller : 'polygons',
    store : 'Polygons',
    title : 'Polygons',
    layout : 'fit',
    tbar : {
        items : [ {
            xtype : 'button',
            handler : 'onAddClick',
            reference : 'toolbarAddButton',
            glyph : 'xf067@FontAwesome',
            tooltip : Strings.sharedAdd,
            tooltipType : 'title',
            menu : [ {
                text : 'Polygon'
            }, {
                text : 'Line'
            }, {
                text : 'Circle'
            } ]
        }, {
            xtype : 'button',
            disabled : true,
            handler : 'onEditClick',
            reference : 'toolbarEditButton',
            glyph : 'xf040@FontAwesome',
            tooltip : Strings.sharedEdit,
            tooltipType : 'title'
        }, {
            xtype : 'button',
            disabled : true,
            handler : 'onRemoveClick',
            reference : 'toolbarRemoveButton',
            glyph : 'xf00d@FontAwesome',
            tooltip : Strings.sharedRemove,
            tooltipType : 'title'
        }, {
            xtype : 'button',
            disabled : true,
            glyph : 'xf0c1@FontAwesome',
            reference : 'toolbarLinkButton',
            handler : 'onLinkClick',
            tooltip : 'Link',
            tooltipType : 'title'
        }, {
            xtype : 'button',
            disabled : true,
            reference : 'toolbarUnlinkButton',
            glyph : 'xf127@FontAwesome',
            handler : 'onUnlinkClick',
            tooltip : 'Unlink',
            tooltipType : 'title'
        } ]
    },
    listeners : {
        selectionchange : 'onSelectionChange'
    },
    columns : [ {
        text : Strings.deviceName,
        dataIndex : 'name',
        flex : 1
    } ]
});
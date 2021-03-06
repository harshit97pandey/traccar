Ext.define('Traccar.view.PolygonsController', {
    extend : 'Ext.app.ViewController',
    alias : 'controller.polygons',

    requires : [ 'Traccar.view.LoginController', 'Traccar.view.UserDialog',
            'Traccar.view.ServerDialog', 'Traccar.view.Users',
            'Traccar.view.BaseWindow' ],

    init : function() {
        if (Traccar.app.getUser().get('admin')) {
            this.lookupReference('settingsUsersButton').setHidden(false);
        }
    },

    onAccoountClick : function() {
        var dialog = Ext.create('Traccar.view.UserDialog');
        dialog.down('form').loadRecord(Traccar.app.getUser());
        dialog.show();
    },

    onUsersClick : function() {
        Ext.create('Traccar.view.BaseWindow', {
            title : Strings.settingsUsers,
            modal : false,
            items : {
                xtype : 'usersView'
            }
        }).show();
    },

    onLogoutClick : function() {
        Ext.create('Traccar.view.LoginController').logout();
    },

    onSelectionChange : function(selected) {
        var empty = selected.getCount() === 0;
        this.lookupReference('toolbarEditButton').setDisabled(empty);
        this.lookupReference('toolbarRemoveButton').setDisabled(empty);
        this.lookupReference('toolbarLinkButton').setDisabled(empty);
        this.lookupReference('toolbarUnlinkButton').setDisabled(empty);
        if (!empty) {
            this.fireEvent('showArea', selected.getLastSelected().data);
        }
    },

    onAddPolygonClick : function() {
        this.fireEvent('drawArea', 'Polygon');
    },

    onAddCircleClick : function() {
        this.fireEvent('drawArea', 'Circle');
    },

    onAddLineClick : function() {
        this.fireEvent('drawArea', 'LineString');
    },

    onAddSquareClick : function() {
        this.fireEvent('drawArea', 'Square');
    },

    onRemoveClick : function() {
        var polygon = this.getView().getSelectionModel().getSelection()[0];
        store = Ext.getStore('Polygons');
        store.remove(polygon);
        store.sync();
    },

    onLinkClick : function() {
        var polygon = this.getView().getSelectionModel().getSelection()[0];
        var devices = Ext.getStore('Devices');
        Ext.Ajax.request({
            scope : this,
            url : './api/polygon/link',
            method : 'POST',
            params : {
                polygonId : polygon.id,
                deviceId : devices.getAt(0).getData().id
            },
            callback : function() {
                Ext.toast('Link applied');
            }
        });
    },

    onUnlinkClick : function() {
        var polygon = this.getView().getSelectionModel().getSelection()[0];
        var devices = Ext.getStore('Devices');
        Ext.Ajax.request({
            scope : this,
            url : './api/polygon/unlink',
            method : 'POST',
            params : {
                polygonId : polygon.id,
                deviceId : devices.getAt(0).getData().id
            },
            callback : function() {
                Ext.toast('Link applied');
            }
        });
    }
});

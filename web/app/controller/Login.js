Ext.define('Traccar.controller.Login', {
    extend: 'Ext.app.Controller',
    requires: [
        'Traccar.view.Login'
    ],

    onLaunch: function () {
        Ext.Ajax.request({
            scope: this,
            url: '/api/server',
            callback: this.onServerReturn
        });
    },
    
    onServerReturn: function (options, success, response) {
        Ext.get('spinner').remove();
        if (success) {
            //Traccar.app.setServer(Ext.decode(response.responseText));
            Ext.Ajax.request({
                scope: this,
                url: '/api/session',
                callback: this.onSessionReturn
            });
        } else {
            Traccar.app.showError(response);
        }
    },

    onSessionReturn: function (options, success, response) {
        if (success) {
            this.login = Ext.create('widget.login', {
                listeners: {
                    scope: this,
                    login: this.onLogin
                }
            });
            this.login.show();
            //Traccar.app.setUser(Ext.decode(response.responseText));
            //this.loadApp();
        } else {
            this.login = Ext.create('widget.login', {
                listeners: {
                    scope: this,
                    login: this.onLogin
                }
            });
            this.login.show();
        }
    },

    onLogin: function () {
        location.href='/';
    },
});
Ext.define('Traccar.controller.Login', {
    extend: 'Ext.app.Controller',
    requires: [
        'Traccar.view.Login'
    ],

    onLaunch: function () {
        Ext.Ajax.request({
            scope: this,
            url: '/api/session',
            callback: this.onSessionReturn
        });
    },

    onSessionReturn: function (options, success, response) {
        var data = Ext.decode(response.responseText)
        if (data.valid) {
            location.href='./';
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
        location.href='./';
    },
});
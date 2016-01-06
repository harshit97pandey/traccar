/*
 * Copyright 2015 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

Ext.define('Traccar.view.AlertController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.alert',

    onSeenClick: function () {
        var selected = this.getView().getSelectionModel();
        if (selected.getCount() > 0) {
            var id = selected.getSelection()[0].getData().id;
            Ext.Ajax.request({
                scope: this,
                url: '/api/notifications/seen',
                params: {
                    notificationId: id
                },
                method: 'POST',
                callback: function() {
                    var seenChecked = this.lookupReference('seenCheck').getValue();
                    console.log(seenChecked)
                    Ext.getStore('Alerts').load({params: {all: seenChecked}});
                    
                    Ext.toast('Alert marked as seen');
                }
            });
        }
    },

    onAllChange: function(checkbox, checked){
        var store = Ext.getStore('Alerts');
        store.load({params: {all: checked}});
    },

    onSelectionChange: function (selected) {
        if (selected.getCount() > 0) {
            seen = selected.getLastSelected().getData().seen;
            this.lookupReference('seenButton').setDisabled(seen);
        }
    }
});

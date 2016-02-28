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

Ext.define('Traccar.view.RegisterController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.register',

    init: function () {
        this.lookupReference('languageField').setValue(Locale.language);
    },

    onCreateClick: function () {
        var form = this.lookupReference('form');
        var data = form.getValues();
        data['personal'] = !!data['personal'] && data['personal'] == 'on' 
        data['admin'] = true;
        if (form.isValid()) {
            Ext.Ajax.request({
                scope: this,
                method: 'POST',
                url: './api/users/register',
                jsonData: data,
                callback: this.onCreateReturn
            });
        }
    },

    onCreateReturn: function (options, success, response) {
        if (success) {
            this.closeView();
            Ext.toast(Strings.loginCreated);
        } else {
            showError(response);
        }
    },
    
    onPersonalClick: function(box,value) {
        var company = this.lookupReference('company');
        company.setDisabled(false);
        company.focus();
    },
    
    onSelectLanguage: function (selected) {
        var paramName, paramValue, url, prefix, suffix;
        paramName = 'locale';
        paramValue = selected.getValue();
        url = window.location.href;
        if (url.indexOf(paramName + '=') >= 0) {
            prefix = url.substring(0, url.indexOf(paramName));
            suffix = url.substring(url.indexOf(paramName));
            suffix = suffix.substring(suffix.indexOf('=') + 1);
            suffix = (suffix.indexOf('&') >= 0) ? suffix.substring(suffix.indexOf('&')) : '';
            url = prefix + paramName + '=' + paramValue + suffix;
        } else {
            if (url.indexOf('?') < 0) {
                url += '?' + paramName + '=' + paramValue;
            } else {
                url += '&' + paramName + '=' + paramValue;
            }
        }
        window.location.href = url;
    },
    

    showError: function (response) {
        var data;
        if (Ext.isString(response)) {
            Ext.Msg.alert(Strings.errorTitle, response);
        } else if (response.responseText) {
            data = Ext.decode(response.responseText);
            if (data.details) {
                Ext.Msg.alert(Strings.errorTitle, data.details);
            } else {
                Ext.Msg.alert(Strings.errorTitle, data.message);
            }
        } else if (response.statusText) {
            Ext.Msg.alert(Strings.errorTitle, response.statusText);
        } else {
            Ext.Msg.alert(Strings.errorTitle, Strings.errorConnection);
        }
    }

});

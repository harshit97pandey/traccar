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

Ext.define('Traccar.view.Alert', {
    extend: 'Ext.grid.Panel',
    xtype: 'alertView',

    requires: [
        'Traccar.view.AlertController'
    ],

    controller: 'alert',
    store: 'Alerts',

    title: 'Alerts',

    tbar: [{
        text: 'Mark as read',
        handler: 'onSeenClick',
        disabled: true,
        reference: 'seenButton'
    }, {
        xtype: 'checkbox',
        handler: 'onAllChange',
        reference: 'seenCheck'
    }],

    listeners: {
        selectionchange: 'onSelectionChange'
    },

    columns: [{
        text: 'seen',
        dataIndex: 'seen',
        flex: 1,
        renderer: Traccar.AttributeFormatter.getFormatter('seen')
    }, {
        text: 'polygonName',
        dataIndex: 'polygonName',
        flex: 1,
        renderer: Traccar.AttributeFormatter.getFormatter('polygonName')
    }, {
        text: 'deviceName',
        dataIndex: 'deviceName',
        flex: 1,
        renderer: Traccar.AttributeFormatter.getFormatter('deviceName')
    }, {
        text: Strings.positionFixTime,
        dataIndex: 'creationDate',
        flex: 1.5,
        xtype: 'datecolumn',
        align:'right',
        renderer: Traccar.AttributeFormatter.getFormatter('creationDate')
    }, {
        text: Strings.positionFixTime,
        dataIndex: 'cancelDate',
        flex: 1.5,
        xtype: 'datecolumn',
        align:'right',
        renderer: Traccar.AttributeFormatter.getFormatter('cancelDate')
    }]
    
});

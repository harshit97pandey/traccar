#!/bin/sh

cd $(dirname $0)/../src/main/webapp

SDK="../../../../ext-6.2.0"

./../../../../../bin/Sencha/Cmd/sencha compile --classpath=app.js,app,$SDK/packages/core/src,$SDK/packages/core/overrides,$SDK/classic/classic/src,$SDK/classic/classic/overrides \
       exclude -all \
       and \
       include -recursive -file app.js \
       and \
       exclude -namespace=Ext \
       and \
       concatenate -closure app.min.js

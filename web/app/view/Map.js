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

Ext.define('Traccar.view.Map', {
    extend: 'Ext.form.Panel',
    xtype: 'mapView',
    header: false,
    requires: [
        'Traccar.view.MapController'
    ],

    controller: 'map',

    title: Strings.mapTitle,
    layout: 'fit',

    getMap: function () {
        return this.map;
    },

    getMapView: function () {
        return this.mapView;
    },

    getLatestSource: function () {
        return this.latestSource;
    },

    getRouteSource: function () {
        return this.routeSource;
    },

    getReportSource: function () {
        return this.reportSource;
    },

    getVectorSource: function () {
        return this.vectorSource;
    },

    listeners: {
        afterrender: function () {
            var user, server, layer, type, bingKey, latestLayer, routeLayer, reportLayer, lat, lon, zoom, target;
            
            bingKey = 'Atabw4WpqrmFXz8yRA9yxMzk2u--7Znu5POGdRsivAkyFw-6QAeOtwgMU8Upcb-W';

            var coordinateFormat = function(coords){
                return coords[0].toFixed(6) + ' ' + coords[1].toFixed(6)
            }

            var createTileLayer = function(url, projection) {
                return new ol.layer.Tile({
                    source: new ol.source.XYZ({
                        url: url,
                        attributions: [new ol.Attribution({
                            html: ''
                        })]
                    }),
                    projection: projection,
                    visible: false
                });
            }
            
            var createBingLayer = function(imagerySet) {
                return new ol.layer.Tile({
                    source: new ol.source.BingMaps({
                        key: bingKey,
                        imagerySet: imagerySet
                    }),
                    visible: false
                });
            }
            
            var OSMLayer = new ol.layer.Tile({
                    source: new ol.source.OSM({}),
                    visible: false
                });
            var bingRoadLayer = createBingLayer('Road');
            var bingAerialLayer = createBingLayer('Aerial');
            var googleHybridLayer = createTileLayer('http://mt0.google.com/vt/lyrs=y&hl=en&x={x}&y={y}&z={z}&s=Ga');
            var googleSateliteLayer = createTileLayer('http://mt0.google.com/vt/lyrs=s&hl=en&x={x}&y={y}&z={z}&s=Ga');
            var googleRoadsLayer = createTileLayer('http://mt0.google.com/vt/lyrs=m&hl=en&x={x}&y={y}&z={z}&s=Ga');
            var arcgisTopo = createTileLayer('http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}.png');
            var arcgisImage = createTileLayer('http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}.png');
            
            var layersData = [{
                name: 'OpenStreetMaps',
                layer: OSMLayer
            }, {
                name: 'Bing Road',
                layer: bingRoadLayer
            }, {
                name: 'Bing Areal',
                layer: bingAerialLayer
            }, {
                name: 'Google Roads',
                layer: googleRoadsLayer
            }, {
                name: 'Google Satelite',
                layer: googleSateliteLayer
            }, {
                name: 'Google Hybrid',
                layer: googleHybridLayer
            }, {
                name: 'ArcGis Topo',
                layer: arcgisTopo
            }, {
                name: 'ArcGis Imagery',
                layer: arcgisImage
            }];

            var saveControl= function(map) {
                var save = document.createElement('button');
                    save.className = 'ol-save-button fa-floppy-o';
                    save.title = 'Save location';
                var handleClick = function(e) {
                    var currentLayer;
                    for (var i = 0; i < layersData.length; ++i) {
                        if (layersData[i].layer.getVisible()) {
                            currentLayer = layersData[i].name; 
                        }
                    }
                    Ext.Ajax.request({
                        scope: this,
                        url: '/api/user/location/update',
                        method: 'POST',
                        params: {
                            map: currentLayer,
                            zoom: map.getView().getZoom(),
                            longitude: ol.proj.toLonLat(map.getView().getCenter())[0],
                            latitude: ol.proj.toLonLat(map.getView().getCenter())[1]
                        },
                        callback: function(){Ext.toast('Location saved');}
                    });
                }
                save.addEventListener('click', handleClick, false);

                var element = document.createElement('div');
                element.className = 'ol-unselectable ol-control ol-save-div';
                element.appendChild(save);

                ol.control.Control.call(this, {
                    element: element
                })
            }
            ol.inherits(saveControl, ol.control.Control);
            
            
            var layerControl= function(map) {
                var select = document.createElement('select');
                    select.className = 'ol-layers-select';
                var selectedIndex = 0;
                for (var i = 0; i < layersData.length; ++i) {
                    var option = document.createElement("option")
                    option.text = layersData[i].name;
                    option.value = layersData[i].name;
                    if (layersData[i].name == type) {
                        selectedIndex = i;
                        layersData[i].layer.setVisible(true);
                    }
                    select.add(option);
                }
                select.selectedIndex = selectedIndex;

                var handleChange = function(e) {
                    for (var i = 0; i < layersData.length; ++i) {
                        layersData[i].layer.setVisible(select.value === layersData[i].name);
                    }
                }
                select.addEventListener('change', handleChange, false);

                var element = document.createElement('div');
                element.className = 'ol-unselectable ol-control ol-layers-div';
                element.appendChild(select);

                ol.control.Control.call(this, {
                    element: element
                })
            }
            ol.inherits(layerControl, ol.control.Control);

            user = Traccar.app.getUser();
            server = Traccar.app.getServer();

            type = user.get('map') || server.get('map');

            this.latestSource = new ol.source.Vector({});
            latestLayer = new ol.layer.Vector({
                source: this.latestSource
            });

            this.routeSource = new ol.source.Vector({});
            routeLayer = new ol.layer.Vector({
                source: this.routeSource
            });

            this.reportSource = new ol.source.Vector({});
            reportLayer = new ol.layer.Vector({
                source: this.reportSource
            });

            this.vectorSource = new ol.source.Vector({wrapX: false});

            var vector = new ol.layer.Vector({
              source: this.vectorSource,
              style: new ol.style.Style({
                fill: new ol.style.Fill({
                  color: 'rgba(255, 255, 255, 0.2)'
                }),
                stroke: new ol.style.Stroke({
                  color: '#ffcc33',
                  width: 2
                }),
                image: new ol.style.Circle({
                  radius: 7,
                  fill: new ol.style.Fill({
                    color: '#ffcc33'
                  })
                })
              })
            });

            lat = user.get('latitude') || server.get('latitude') || Traccar.Style.mapDefaultLat;
            lon = user.get('longitude') || server.get('longitude') || Traccar.Style.mapDefaultLon;
            zoom = user.get('zoom') || server.get('zoom') || Traccar.Style.mapDefaultZoom;

            this.mapView = new ol.View({
                center: ol.proj.fromLonLat([lon, lat]),
                zoom: zoom,
                maxZoom: Traccar.Style.mapMaxZoom
            });

            var layers = [];
            
            for (var i = 0; i < layersData.length; ++i) {
                layers.push(layersData[i].layer);
            }
            layers.push(routeLayer);
            layers.push(reportLayer);
            layers.push(latestLayer);
            layers.push(vector);
            
            this.map = new ol.Map({
                target: this.body.dom.id,
                layers: layers,
                view: this.mapView,
                interactions: ol.interaction.defaults().extend([
                    new ol.interaction.DragRotateAndZoom()
                ])
            });

            var layerControl = new layerControl(this.map)
            this.map.addControl(layerControl)
            var scaleLine = new ol.control.ScaleLine()
            this.map.addControl(scaleLine)
            var zoomslider = new ol.control.ZoomSlider()
            this.map.addControl(zoomslider)
            var position = new ol.control.MousePosition({
                    projection: ol.proj.get('EPSG:4326'),
                    coordinateFormat: coordinateFormat
                });
            this.map.addControl(position)
            var fullScreen = new ol.control.FullScreen()
            this.map.addControl(fullScreen);
            var saveControl = new saveControl(this.map)
            this.map.addControl(saveControl);
            
            target = this.map.getTarget();
            if (typeof target === 'string') {
                target = Ext.get(target).dom;
            }

            this.map.on('pointermove', function (e) {
                var hit = this.forEachFeatureAtPixel(e.pixel, function (feature, layer) {
                    return true;
                });
                if (hit) {
                    target.style.cursor = 'pointer';
                } else {
                    target.style.cursor = '';
                }
            });
            
            
            this.map.on('click', function (e) {
                this.map.forEachFeatureAtPixel(e.pixel, function (feature, layer) {
                    this.fireEvent('selectFeature', feature);
                }, this);
            }, this);
        },

        resize: function () {
            this.map.updateSize();
        }
    }
});

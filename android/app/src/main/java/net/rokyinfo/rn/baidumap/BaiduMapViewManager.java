package net.rokyinfo.rn.baidumap;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BaiduMapViewManager extends SimpleViewManager<MapView> {

    private static final String REACT_CLASS = "RCTBaiduMapView";

    private MapView mMapView;

    // 普通图标缓存
    private HashMap<String, Marker> mMarkerMap = new HashMap<>();
    private HashMap<String, List<Marker>> mMarkersMap = new HashMap<>();

    // 聚合图标缓存
    private float mDensity = -1;
    private IconGenerator mIconGenerator;
    private TextView mMarkerText;
    private static final int[] BUCKETS = {10, 20, 50, 100, 200, 500, 1000};
    private ShapeDrawable mColoredCircleBackground;
    private SparseArray<BitmapDescriptor> mIcons = new SparseArray<>();

    // 定位相关
    private LocationClient mLocClient;

    private MyLocationListenner myListener = new MyLocationListenner();
    // 是否首次定位
    private boolean isFirstLoc = true;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public MapView createViewInstance(ThemedReactContext context) {

        // 初始化聚合显示点
        if (mIconGenerator == null) {
            mDensity = context.getResources().getDisplayMetrics().density;
            mIconGenerator = new IconGenerator(context);
            mIconGenerator.setContentView(makeSquareTextView(context));
            mIconGenerator.setTextAppearance(R.style.ClusterIcon_TextAppearance);
            mIconGenerator.setBackground(makeClusterBackground());
        }

        MapView mMapView = new MapView(context);
        setListeners(mMapView);

        return mMapView;
    }

    @ReactProp(name = "myLocationEnabled")
    public void setMyLocationEnabled(MapView mapView, boolean myLocationEnabled) {

        // 开启定位图层
        mapView.getMap().setMyLocationEnabled(myLocationEnabled);
        if (myLocationEnabled) {
            // 定位初始化
            mMapView = mapView;
            mLocClient = new LocationClient(mapView.getContext().getApplicationContext());
            mLocClient.registerLocationListener(myListener);
            LocationClientOption option = new LocationClientOption();
            option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
            option.setCoorType("bd09ll");
            option.setIsNeedAddress(true);
            option.setIsNeedAltitude(true);
            option.setIsNeedLocationDescribe(true);
            option.setOpenGps(true);

            mLocClient.setLocOption(option);
            mLocClient.start();
        } else {
            if (mLocClient != null) {
                mLocClient.stop();
            }
        }
    }

    @ReactProp(name = "zoomControlsVisible")
    public void setZoomControlsVisible(MapView mapView, boolean zoomControlsVisible) {
        mapView.showZoomControls(zoomControlsVisible);
    }

    @ReactProp(name = "trafficEnabled")
    public void setTrafficEnabled(MapView mapView, boolean trafficEnabled) {
        mapView.getMap().setTrafficEnabled(trafficEnabled);
    }

    @ReactProp(name = "baiduHeatMapEnabled")
    public void setBaiduHeatMapEnabled(MapView mapView, boolean baiduHeatMapEnabled) {
        mapView.getMap().setBaiduHeatMapEnabled(baiduHeatMapEnabled);
    }

    @ReactProp(name = "mapType")
    public void setMapType(MapView mapView, int mapType) {
        mapView.getMap().setMapType(mapType);
    }

    @ReactProp(name = "zoom")
    public void setZoom(MapView mapView, float zoom) {
        MapStatus mapStatus = new MapStatus.Builder().zoom(zoom).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        mapView.getMap().setMapStatus(mapStatusUpdate);
    }

    @ReactProp(name = "center")
    public void setCenter(MapView mapView, ReadableMap position) {
        if (position != null) {
            double latitude = position.getDouble("latitude");
            double longitude = position.getDouble("longitude");
            LatLng point = new LatLng(latitude, longitude);
            MapStatus mapStatus = new MapStatus.Builder()
                    .target(point)
                    .build();
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
            mapView.getMap().setMapStatus(mapStatusUpdate);
        }
    }

    @ReactProp(name = "marker")
    public void setMarker(MapView mapView, ReadableMap option) {
        if (option != null) {
            String key = "marker_" + mapView.getId();
            Marker marker = mMarkerMap.get(key);
            if (marker != null) {
                MarkerUtil.updateMaker(marker, option);
            } else {
                marker = MarkerUtil.addMarker(mapView, option);
                mMarkerMap.put(key, marker);
            }
        }
    }

    @ReactProp(name = "markers")
    public void setMarkers(MapView mapView, ReadableArray options) {
        String key = "markers_" + mapView.getId();
        List<Marker> markers = mMarkersMap.get(key);
        if (markers == null) {
            markers = new ArrayList<>();
        }
        for (int i = 0; i < options.size(); i++) {
            ReadableMap option = options.getMap(i);
            if (markers.size() > i + 1 && markers.get(i) != null) {
                MarkerUtil.updateMaker(markers.get(i), option);
            } else {
                markers.add(i, MarkerUtil.addMarker(mapView, option));
            }
        }
        if (options.size() < markers.size()) {
            int start = markers.size() - 1;
            int end = options.size();
            for (int i = start; i >= end; i--) {
                markers.get(i).remove();
                markers.remove(i);
            }
        }
        mMarkersMap.put(key, markers);
    }

    @ReactProp(name = "clusterMarkers")
    public void setClusterMarkers(MapView mapView, ReadableArray options) {
        String key = "cluster_markers_" + mapView.getId();
        List<Marker> markers = mMarkersMap.get(key);
        if (markers == null) {
            markers = new ArrayList<>();
        }
        for (int i = 0; i < options.size(); i++) {
            ReadableMap option = options.getMap(i);
            int clusterSize = option.getInt("clusterSize");
            if (markers.size() > i + 1 && markers.get(i) != null) {
                MarkerUtil.updateMaker(markers.get(i), option, getClusterIcon(clusterSize));
            } else {
                markers.add(i, MarkerUtil.addMarker(mapView, option, getClusterIcon(clusterSize)));
            }
        }
        if (options.size() < markers.size()) {
            int start = markers.size() - 1;
            int end = options.size();
            for (int i = start; i >= end; i--) {
                markers.get(i).remove();
                markers.remove(i);
            }
        }
        mMarkersMap.put(key, markers);
    }

    /**
     * @param mapView
     */
    private void setListeners(final MapView mapView) {
        BaiduMap map = mapView.getMap();

        if (mMarkerText == null) {
            mMarkerText = new TextView(mapView.getContext());
            mMarkerText.setBackgroundResource(R.drawable.popup);
            mMarkerText.setPadding(32, 32, 32, 32);
        }
        map.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {

            private WritableMap getEventParams(MapStatus mapStatus) {
                WritableMap writableMap = Arguments.createMap();
                WritableMap target = Arguments.createMap();
                target.putDouble("latitude", mapStatus.target.latitude);
                target.putDouble("longitude", mapStatus.target.longitude);
                writableMap.putMap("target", target);
                writableMap.putDouble("zoom", mapStatus.zoom);
                writableMap.putDouble("overlook", mapStatus.overlook);
                return writableMap;
            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {
                sendEvent(mapView, "onMapStatusChangeStart", getEventParams(mapStatus));
            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {
                sendEvent(mapView, "onMapStatusChangeStart", getEventParams(mapStatus));
            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {
                sendEvent(mapView, "onMapStatusChange", getEventParams(mapStatus));
            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                if (mMarkerText.getVisibility() != View.GONE) {
                    mMarkerText.setVisibility(View.GONE);
                }
                sendEvent(mapView, "onMapStatusChangeFinish", getEventParams(mapStatus));
            }
        });

        map.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                sendEvent(mapView, "onMapLoaded", null);
            }
        });

        map.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mapView.getMap().hideInfoWindow();
                WritableMap writableMap = Arguments.createMap();
                writableMap.putDouble("latitude", latLng.latitude);
                writableMap.putDouble("longitude", latLng.longitude);
                sendEvent(mapView, "onMapClick", writableMap);
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                WritableMap writableMap = Arguments.createMap();
                writableMap.putString("name", mapPoi.getName());
                writableMap.putString("uid", mapPoi.getUid());
                writableMap.putDouble("latitude", mapPoi.getPosition().latitude);
                writableMap.putDouble("longitude", mapPoi.getPosition().longitude);
                sendEvent(mapView, "onMapPoiClick", writableMap);
                return true;
            }
        });
        map.setOnMapDoubleClickListener(new BaiduMap.OnMapDoubleClickListener() {
            @Override
            public void onMapDoubleClick(LatLng latLng) {
                WritableMap writableMap = Arguments.createMap();
                writableMap.putDouble("latitude", latLng.latitude);
                writableMap.putDouble("longitude", latLng.longitude);
                sendEvent(mapView, "onMapDoubleClick", writableMap);
            }
        });

        map.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.getTitle().length() > 0) {
                    mMarkerText.setText(marker.getTitle());
                    InfoWindow infoWindow = new InfoWindow(mMarkerText, marker.getPosition(), -80);
                    mMarkerText.setVisibility(View.GONE);
                    mapView.getMap().showInfoWindow(infoWindow);
                } else {
                    mapView.getMap().hideInfoWindow();
                }
                WritableMap writableMap = Arguments.createMap();
                WritableMap position = Arguments.createMap();
                position.putDouble("latitude", marker.getPosition().latitude);
                position.putDouble("longitude", marker.getPosition().longitude);
                writableMap.putMap("position", position);
                writableMap.putString("title", marker.getTitle());
                sendEvent(mapView, "onMarkerClick", writableMap);
                return true;
            }
        });

    }

    /**
     * @param eventName
     * @param params
     */
    private void sendEvent(MapView mapView, String eventName, @Nullable WritableMap params) {
        WritableMap event = Arguments.createMap();
        event.putMap("params", params);
        event.putString("type", eventName);
        ((ReactContext) mapView.getContext())
                .getJSModule(RCTEventEmitter.class)
                .receiveEvent(mapView.getId(),
                        "topChange",
                        event);
    }

    private BitmapDescriptor getClusterIcon(int clusterSize) {
        int bucket = getBucket(clusterSize);
        BitmapDescriptor descriptor = mIcons.get(bucket);
        if (descriptor == null) {
            mColoredCircleBackground.getPaint().setColor(Color.argb(204, 0xE6, 0x4E, 0x42));
            descriptor = BitmapDescriptorFactory.fromBitmap(mIconGenerator.makeIcon(getClusterText(bucket)));
            mIcons.put(bucket, descriptor);
        }
        // TODO: consider adding anchor(.5, .5) (Individual markers will overlap more often)
        return descriptor;
    }

    private LayerDrawable makeClusterBackground() {
        mColoredCircleBackground = new ShapeDrawable(new OvalShape());
        ShapeDrawable outline = new ShapeDrawable(new OvalShape());
        outline.getPaint().setColor(0x80ffffff); // Transparent white.
        LayerDrawable background = new LayerDrawable(new Drawable[]{outline, mColoredCircleBackground});
        int strokeWidth = (int) (mDensity * 3);
        background.setLayerInset(1, strokeWidth, strokeWidth, strokeWidth, strokeWidth);
        return background;
    }

    private SquareTextView makeSquareTextView(Context context) {
        SquareTextView squareTextView = new SquareTextView(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        squareTextView.setLayoutParams(layoutParams);
        squareTextView.setId(R.id.text);
        int twelveDpi = (int) (12 * mDensity);
        squareTextView.setPadding(twelveDpi, twelveDpi, twelveDpi, twelveDpi);
        return squareTextView;
    }

    private String getClusterText(int bucket) {
        if (bucket < BUCKETS[0]) {
            return String.valueOf(bucket);
        }
        return String.valueOf(bucket) + "+";
    }

    /**
     * Gets the "bucket" for a particular cluster. By default, uses the number of points within the
     * cluster, bucketed to some set points.
     */
    private int getBucket(int clusterSize) {
        if (clusterSize <= BUCKETS[0]) {
            return clusterSize;
        }
        for (int i = 0; i < BUCKETS.length - 1; i++) {
            if (clusterSize < BUCKETS[i + 1]) {
                return BUCKETS[i];
            }
        }
        return BUCKETS[BUCKETS.length - 1];
    }


    public class MyLocationListenner extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置

            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(0).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mMapView.getMap().setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mMapView.getMap().animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }

    }


}

package net.rokyinfo.rn.baidumap;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.facebook.react.bridge.ReadableMap;

/**
 * Created by lovebing on Sept 28, 2016.
 */
public class MarkerUtil {

    public static void updateMaker(Marker maker, ReadableMap option) {
        LatLng position = getLatLngFromOption(option);
        maker.setPosition(position);
        maker.setTitle(option.getString("title"));
    }

    public static Marker addMarker(MapView mapView, ReadableMap option) {
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.icon_gcoding);
        LatLng position = getLatLngFromOption(option);
        OverlayOptions overlayOptions = new MarkerOptions()
                .icon(bitmap)
                .position(position)
                .title(option.getString("title"));

        Marker marker = (Marker) mapView.getMap().addOverlay(overlayOptions);
        return marker;
    }

    public static void updateMaker(Marker maker, ReadableMap option, BitmapDescriptor icon) {
        LatLng position = getLatLngFromOption(option);
        maker.setIcon(icon);
        maker.setPosition(position);
        maker.setTitle(option.getString("title"));
    }

    public static Marker addMarker(MapView mapView, ReadableMap option, BitmapDescriptor icon) {
        LatLng position = getLatLngFromOption(option);
        OverlayOptions overlayOptions = new MarkerOptions()
                .icon(icon)
                .position(position)
                .title(option.getString("title"));

        Marker marker = (Marker) mapView.getMap().addOverlay(overlayOptions);
        return marker;
    }

    public static LatLng getLatLngFromOption(ReadableMap option) {

        double latitude = option.getDouble("latitude");
        double longitude = option.getDouble("longitude");

        if (option.getString("coordType").equals("wgs84")) {
            // 将GPS设备采集的原始GPS坐标转换成百度坐标
            CoordinateConverter converter = new CoordinateConverter();
            converter.from(CoordinateConverter.CoordType.GPS);

            // sourceLatLng待转换坐标
            converter.coord(new LatLng(latitude, longitude));
            return converter.convert();
        } else if (option.getString("coordType").equals("gcj02")) {
            // 国测局GPS坐标转换成百度坐标
            CoordinateConverter converter = new CoordinateConverter();
            converter.from(CoordinateConverter.CoordType.COMMON);

            // sourceLatLng待转换坐标
            converter.coord(new LatLng(latitude, longitude));
            return converter.convert();
        } else {
            return new LatLng(latitude, longitude);
        }
    }
}

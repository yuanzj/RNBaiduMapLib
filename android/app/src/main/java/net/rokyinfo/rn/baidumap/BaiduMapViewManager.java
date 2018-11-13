package net.rokyinfo.rn.baidumap;


import com.baidu.mapapi.map.MapView;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;

public class BaiduMapViewManager extends SimpleViewManager<MapView> {

    public static final String REACT_CLASS = "RCTBaiduMapView";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected MapView createViewInstance(ThemedReactContext reactContext) {
        return new MapView(reactContext);
    }
    
}

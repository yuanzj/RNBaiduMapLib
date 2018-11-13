import React, {Component} from 'react';
import {
  requireNativeComponent
} from 'react-native';
 
let RCTBaiduMapView = requireNativeComponent('RCTBaiduMapView', MapView);
class MapView extends Component {
  
  render() {
    return <RCTBaiduMapView  {...this.props}/>
  }
}
 
module.exports = MapView;


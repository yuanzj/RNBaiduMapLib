/**
 * @author lovebing
 */

import React, { Component, PropTypes } from 'react';

import MapTypes from './MapTypes';
import MapView from './MapView';
import Geolocation from './Geolocation';

import {
	Button,
	AppRegistry,
	StyleSheet,
	Text,
	View,
	TouchableHighlight,
	NativeModules,
	Platform,
	DeviceEventEmitter
} from 'react-native';

import Dimensions from 'Dimensions';

export default class BaiduMapDemo extends Component {
	constructor() {
		super();

		this.state = {
			mayType: MapTypes.NORMAL,
			zoom: 15,
			center: {
				longitude: 113.981718,
				latitude: 22.542449
			},
			myLocationEnabled: true,
			trafficEnabled: false,
			baiduHeatMapEnabled: false,
			markers: [
				{
					longitude: 113.989718,
					latitude: 22.542449,
					coordType: 'wgs84',
					title: 'Window of the world'
				},
				{
					longitude: 113.998516,
					latitude: 22.537642,
					coordType: 'wgs84',
					title: ''
				}
			],
			clusterMarkers: [
				{
					longitude: 113.981718,
					latitude: 22.542449,
					coordType: 'wgs84',
					clusterSize: 5,
					title: 'Window of the world'
				},
				{
					longitude: 113.995516,
					latitude: 22.537642,
					coordType: 'wgs84',
					clusterSize: 100,
					title: ''
				}
			]
		};
	}

	componentDidMount() {}

	render() {
		return (
			<View style={styles.container}>
				<MapView
					myLocationEnabled={this.state.myLocationEnabled}
					trafficEnabled={this.state.trafficEnabled}
					baiduHeatMapEnabled={this.state.baiduHeatMapEnabled}
					zoom={this.state.zoom}
					mapType={this.state.mapType}
					center={this.state.center}
					marker={this.state.marker}
					markers={this.state.markers}
					clusterMarkers={this.state.clusterMarkers}
					style={styles.map}
					onMarkerClick={(e) => {
						console.warn(JSON.stringify(e));
					}}
					onMapClick={(e) => {}}
					onMapStatusChangeFinish={(e) => {
						console.warn(JSON.stringify(e));
					}}
				/>

				<View style={styles.row}>
					<Button
						title="Normal"
						onPress={() => {
							this.setState({
								mapType: MapTypes.NORMAL
							});
						}}
					/>
					<Button
						style={styles.btn}
						title="Satellite"
						onPress={() => {
							this.setState({
								mapType: MapTypes.SATELLITE
							});
						}}
					/>

					<Button
						style={styles.btn}
						title="Locate"
						onPress={() => {
							console.warn('center', this.state.center);
							Geolocation.getCurrentPosition()
								.then((data) => {
									console.warn(JSON.stringify(data));
									this.setState({
										zoom: 15,
										marker: {
											latitude: data.latitude,
											longitude: data.longitude,
											coordType: 'bd09',
											title: 'Your location'
										},
										center: {
											latitude: data.latitude,
											longitude: data.longitude,
											coordType: 'bd09',
											rand: Math.random()
										}
									});
								})
								.catch((e) => {
									console.warn(e, 'error');
								});
						}}
					/>
				</View>

				<View style={styles.row}>
					<Button
						title="Zoom+"
						onPress={() => {
							this.setState({
								zoom: this.state.zoom + 1
							});
						}}
					/>
					<Button
						title="Zoom-"
						onPress={() => {
							if (this.state.zoom > 0) {
								this.setState({
									zoom: this.state.zoom - 1
								});
							}
						}}
					/>
				</View>

				<View style={styles.row}>
					<Button
						title="Traffic"
						onPress={() => {
							this.setState({
								trafficEnabled: !this.state.trafficEnabled
							});
						}}
					/>

					<Button
						title="Baidu HeatMap"
						onPress={() => {
							this.setState({
								baiduHeatMapEnabled: !this.state.baiduHeatMapEnabled
							});
						}}
					/>
				</View>
			</View>
		);
	}
}

const styles = StyleSheet.create({
	row: {
		flexDirection: 'row',
		height: 40
	},
	container: {
		flex: 1,
		justifyContent: 'flex-start',
		alignItems: 'center',
		backgroundColor: '#F5FCFF'
	},
	map: {
		width: Dimensions.get('window').width,
		height: Dimensions.get('window').height - 200,
		marginBottom: 16
	}
});

/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React, {Component} from 'react';
import {Platform, StyleSheet, Text, View} from 'react-native';

import BaiduMapDemo from './BaiduMapDemo';

type Props = {};
export default class App extends Component<Props> {

  render() {
    return (
      <View style={styles.container}>
        <BaiduMapDemo 
          style={{flex: 1}}  />
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'column',
    justifyContent: 'center',
    alignItems: 'stretch',
    backgroundColor: '#FFFFFF',
  },
});

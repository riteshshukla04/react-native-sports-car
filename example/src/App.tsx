import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Platform,
} from 'react-native';
import AndroidAutoExample from './AndroidAutoExample';
import AppLifecycleExample from './AppLifecycleExample';

export default function App() {
  const [currentExample, setCurrentExample] = React.useState<
    'basic' | 'lifecycle'
  >('basic');
  if (Platform.OS === 'ios') {
    return <Text> Only Android is supported on iOS</Text>;
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>React Native Sportscar</Text>
        <Text style={styles.subtitle}>Android Auto Media Player Examples</Text>
      </View>

      <View style={styles.tabContainer}>
        <TouchableOpacity
          style={[styles.tab, currentExample === 'basic' && styles.activeTab]}
          onPress={() => setCurrentExample('basic')}
        >
          <Text
            style={[
              styles.tabText,
              currentExample === 'basic' && styles.activeTabText,
            ]}
          >
            Basic Example
          </Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[
            styles.tab,
            currentExample === 'lifecycle' && styles.activeTab,
          ]}
          onPress={() => setCurrentExample('lifecycle')}
        >
          <Text
            style={[
              styles.tabText,
              currentExample === 'lifecycle' && styles.activeTabText,
            ]}
          >
            App Lifecycle
          </Text>
        </TouchableOpacity>
      </View>

      <View style={styles.content}>
        {currentExample === 'basic' ? (
          <AndroidAutoExample />
        ) : (
          <AppLifecycleExample />
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  header: {
    backgroundColor: '#2196F3',
    padding: 20,
    paddingTop: 60,
    alignItems: 'center',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: 'white',
    marginBottom: 5,
  },
  subtitle: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.8)',
  },
  tabContainer: {
    flexDirection: 'row',
    backgroundColor: 'white',
    borderBottomWidth: 1,
    borderBottomColor: '#e0e0e0',
  },
  tab: {
    flex: 1,
    paddingVertical: 15,
    alignItems: 'center',
  },
  activeTab: {
    borderBottomWidth: 2,
    borderBottomColor: '#2196F3',
  },
  tabText: {
    fontSize: 16,
    color: '#666',
  },
  activeTabText: {
    color: '#2196F3',
    fontWeight: 'bold',
  },
  content: {
    flex: 1,
  },
});

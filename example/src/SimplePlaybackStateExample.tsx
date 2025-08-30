import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { usePlaybackStateChange } from 'react-native-sportscar';

/**
 * Simple example showing basic playback state tracking
 */
const SimplePlaybackStateExample: React.FC = () => {
  const { playbackInfo, isLoading, isPlaying, isStopped, isBuffering } = usePlaybackStateChange({
    fetchInitialState: true,
    onStateChange: (info) => {
      console.log('🎵 Playback state changed:', info.state);
    },
  });

  const getStatusEmoji = () => {
    if (isLoading) return '🔄';
    if (isPlaying) return '▶️';
    if (isBuffering) return '⏳';
    if (isStopped) return '⏹️';
    return '❓';
  };

  const getStatusText = () => {
    if (isLoading) return 'Loading...';
    if (isPlaying) return 'Playing';
    if (isBuffering) return 'Buffering';
    if (isStopped) return 'Stopped';
    return 'Unknown';
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Playback State</Text>
      
      <View style={styles.statusContainer}>
        <Text style={styles.statusEmoji}>{getStatusEmoji()}</Text>
        <Text style={styles.statusText}>{getStatusText()}</Text>
      </View>

      {playbackInfo && (
        <View style={styles.infoContainer}>
          <Text style={styles.infoText}>
            Current Media: {playbackInfo.currentMediaId || 'None'}
          </Text>
          <Text style={styles.infoText}>
            Position: {Math.floor(playbackInfo.positionMs / 1000)}s
          </Text>
          <Text style={styles.infoText}>
            Duration: {Math.floor(playbackInfo.durationMs / 1000)}s
          </Text>
        </View>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: '#f5f5f5',
    justifyContent: 'center',
    alignItems: 'center',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 30,
    color: '#333',
  },
  statusContainer: {
    alignItems: 'center',
    marginBottom: 30,
  },
  statusEmoji: {
    fontSize: 48,
    marginBottom: 10,
  },
  statusText: {
    fontSize: 18,
    fontWeight: '600',
    color: '#333',
  },
  infoContainer: {
    backgroundColor: '#fff',
    padding: 20,
    borderRadius: 10,
    alignItems: 'center',
  },
  infoText: {
    fontSize: 16,
    marginBottom: 5,
    color: '#666',
  },
});

export default SimplePlaybackStateExample;

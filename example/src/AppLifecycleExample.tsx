import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Alert,
  AppState,
} from 'react-native';
import { AndroidAuto } from 'react-native-sportscar';
import type {
  AppState as SportscarAppState,
  LastPlayedMediaInfo,
} from '../../src/types';

/**
 * Example demonstrating app lifecycle management and background audio playback
 *
 * This example shows how to:
 * 1. Handle app state changes (foreground/background/destroyed)
 * 2. Keep music playing when app is closed
 * 3. Restore playback state when app is reopened
 * 4. Handle direct app launch from Android Auto
 */
const AppLifecycleExample: React.FC = () => {
  const [currentAppState, setCurrentAppState] =
    useState<SportscarAppState>('foreground');
  const [isPlaying, setIsPlaying] = useState(false);
  const [lastPlayedInfo, setLastPlayedInfo] =
    useState<LastPlayedMediaInfo | null>(null);

  useEffect(() => {
    // Initialize the media library
    initializeMediaLibrary();

    // Set up app state change listener using the correct API
    const handleAppStateChangeWrapper = (nextAppState: string) => {
      handleAppStateChange(nextAppState);
    };

    AppState.addEventListener('change', handleAppStateChangeWrapper);

    // Check current service state on mount
    checkServiceState();

    // Set up Android Auto event listeners with error handling
    let playbackSubscription: any = null;
    try {
      if (AndroidAuto && AndroidAuto.addEventListener) {
        playbackSubscription = AndroidAuto.addEventListener(
          'playbackStateChanged',
          handlePlaybackStateChange
        );
      } else {
        console.warn('AndroidAuto module not available');
      }
    } catch (error) {
      console.error('Failed to set up Android Auto event listener:', error);
    }

    return () => {
      // Remove listeners
      try {
        // Note: AppState.removeEventListener is deprecated in React Native 0.81.0
        // The listener will be automatically cleaned up when the component unmounts
        if (playbackSubscription && playbackSubscription.remove) {
          playbackSubscription.remove();
        }
      } catch (error) {
        console.error('Error removing listeners:', error);
      }
    };
  }, []);

  const initializeMediaLibrary = async () => {
    try {
      if (!AndroidAuto || !AndroidAuto.initializeMediaLibrary) {
        console.warn('AndroidAuto module not available');
        return;
      }

      const mediaLibrary = {
        layoutType: 'list' as const,
        rootItems: [
          {
            id: 'song1',
            title: 'Background Test Song',
            subtitle: 'This song will continue playing when app is closed',
            isPlayable: true,
            mediaUrl:
              'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3',
            mediaType: 'audio' as const,
            durationMs: 180000, // 3 minutes
          },
          {
            id: 'song2',
            title: 'Another Background Song',
            subtitle: 'This one too!',
            isPlayable: true,
            mediaUrl:
              'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
            mediaType: 'audio' as const,
            durationMs: 240000, // 4 minutes
          },
        ],
      };

      await AndroidAuto.initializeMediaLibrary(mediaLibrary);
      console.log('✅ Media library initialized');
    } catch (error) {
      console.error('❌ Failed to initialize media library:', error);
    }
  };

  const handleAppStateChange = async (nextAppState: string) => {
    console.log('📱 App state changed:', nextAppState);

    let newState: SportscarAppState;
    switch (nextAppState) {
      case 'active':
        newState = 'foreground';
        break;
      case 'background':
        newState = 'background';
        break;
      case 'inactive':
        newState = 'destroyed';
        break;
      default:
        newState = 'foreground';
    }

    setCurrentAppState(newState);

    // Notify the Android Auto service about the app state change
    try {
      if (AndroidAuto && AndroidAuto.handleAppStateChange) {
        await AndroidAuto.handleAppStateChange(newState);
        console.log('✅ App state change handled by service');
      } else {
        console.warn('AndroidAuto.handleAppStateChange not available');
      }
    } catch (error) {
      console.error('❌ Failed to handle app state change:', error);
    }
  };

  const handlePlaybackStateChange = (event: any) => {
    if (event.data) {
      setIsPlaying(event.data.state === 'playing');
    }
  };

  const checkServiceState = async () => {
    try {
      if (!AndroidAuto) {
        console.warn('AndroidAuto module not available');
        return;
      }

      // Check if service is currently playing
      const playing = AndroidAuto.isCurrentlyPlaying
        ? await AndroidAuto.isCurrentlyPlaying()
        : false;
      setIsPlaying(playing);

      // Get current app state from service
      const appState = AndroidAuto.getCurrentAppState
        ? await AndroidAuto.getCurrentAppState()
        : 'foreground';
      setCurrentAppState(appState);

      // Get last played media info
      const lastInfo = AndroidAuto.getLastPlayedMediaInfo
        ? await AndroidAuto.getLastPlayedMediaInfo()
        : null;
      setLastPlayedInfo(lastInfo);

      console.log('📊 Service state checked:', { playing, appState, lastInfo });
    } catch (error) {
      console.error('❌ Failed to check service state:', error);
    }
  };

  const playSong = async (songId: string) => {
    try {
      if (!AndroidAuto || !AndroidAuto.playMedia) {
        Alert.alert('Error', 'AndroidAuto module not available');
        return;
      }

      const success = await AndroidAuto.playMedia(songId);
      if (success) {
        console.log('▶️ Started playing song:', songId);
        Alert.alert(
          'Success',
          'Song started playing! Try closing the app - it should continue playing.'
        );
      } else {
        Alert.alert('Error', 'Failed to start playback');
      }
    } catch (error) {
      console.error('❌ Failed to play song:', error);
      Alert.alert('Error', 'Failed to play song');
    }
  };

  const pausePlayback = async () => {
    try {
      if (!AndroidAuto || !AndroidAuto.pause) {
        console.warn('AndroidAuto.pause not available');
        return;
      }
      const success = await AndroidAuto.pause();
      if (success) {
        console.log('⏸️ Playback paused');
      }
    } catch (error) {
      console.error('❌ Failed to pause:', error);
    }
  };

  const resumePlayback = async () => {
    try {
      if (!AndroidAuto || !AndroidAuto.resume) {
        console.warn('AndroidAuto.resume not available');
        return;
      }
      const success = await AndroidAuto.resume();
      if (success) {
        console.log('▶️ Playback resumed');
      }
    } catch (error) {
      console.error('❌ Failed to resume:', error);
    }
  };

  const stopPlayback = async () => {
    try {
      if (!AndroidAuto || !AndroidAuto.stop) {
        console.warn('AndroidAuto.stop not available');
        return;
      }
      const success = await AndroidAuto.stop();
      if (success) {
        console.log('⏹️ Playback stopped');
      }
    } catch (error) {
      console.error('❌ Failed to stop:', error);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>App Lifecycle & Background Audio</Text>

      <View style={styles.statusContainer}>
        <Text style={styles.statusLabel}>Current App State:</Text>
        <Text
          style={[
            styles.statusValue,
            { color: getAppStateColor(currentAppState) },
          ]}
        >
          {currentAppState.toUpperCase()}
        </Text>
      </View>

      <View style={styles.statusContainer}>
        <Text style={styles.statusLabel}>Service Playing:</Text>
        <Text
          style={[
            styles.statusValue,
            isPlaying ? styles.playingText : styles.notPlayingText,
          ]}
        >
          {isPlaying ? 'YES' : 'NO'}
        </Text>
      </View>

      {lastPlayedInfo && (
        <View style={styles.statusContainer}>
          <Text style={styles.statusLabel}>Last Played:</Text>
          <Text style={styles.statusValue}>{lastPlayedInfo.mediaId}</Text>
          <Text style={styles.statusValue}>
            Position: {Math.round(lastPlayedInfo.positionMs / 1000)}s
          </Text>
        </View>
      )}

      <View style={styles.buttonContainer}>
        <TouchableOpacity
          style={styles.button}
          onPress={() => playSong('song1')}
        >
          <Text style={styles.buttonText}>Play Song 1</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.button}
          onPress={() => playSong('song2')}
        >
          <Text style={styles.buttonText}>Play Song 2</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.button, styles.controlButton]}
          onPress={isPlaying ? pausePlayback : resumePlayback}
        >
          <Text style={styles.buttonText}>
            {isPlaying ? 'Pause' : 'Resume'}
          </Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.button, styles.stopButton]}
          onPress={stopPlayback}
        >
          <Text style={styles.buttonText}>Stop</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.button, styles.infoButton]}
          onPress={checkServiceState}
        >
          <Text style={styles.buttonText}>Refresh State</Text>
        </TouchableOpacity>
      </View>

      <View style={styles.infoContainer}>
        <Text style={styles.infoTitle}>How to Test:</Text>
        <Text style={styles.infoText}>
          1. Start playing a song{'\n'}
          2. Close the app (swipe up and swipe away){'\n'}
          3. Music should continue playing{'\n'}
          4. Control playback from notification or Android Auto{'\n'}
          5. Reopen the app - it should reconnect to the service{'\n'}
          6. Try launching directly from Android Auto
        </Text>
      </View>
    </View>
  );
};

const getAppStateColor = (state: SportscarAppState): string => {
  switch (state) {
    case 'foreground':
      return '#4CAF50';
    case 'background':
      return '#FF9800';
    case 'destroyed':
      return '#F44336';
    default:
      return '#757575';
  }
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: '#f5f5f5',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 30,
    color: '#333',
  },
  playingText: {
    color: '#4CAF50',
  },
  notPlayingText: {
    color: '#F44336',
  },
  statusContainer: {
    backgroundColor: 'white',
    padding: 15,
    borderRadius: 8,
    marginBottom: 15,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  statusLabel: {
    fontSize: 14,
    color: '#666',
    marginBottom: 5,
  },
  statusValue: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
  },
  buttonContainer: {
    marginBottom: 20,
  },
  button: {
    backgroundColor: '#2196F3',
    padding: 15,
    borderRadius: 8,
    marginBottom: 10,
    alignItems: 'center',
  },
  controlButton: {
    backgroundColor: '#FF9800',
  },
  stopButton: {
    backgroundColor: '#F44336',
  },
  infoButton: {
    backgroundColor: '#9C27B0',
  },
  buttonText: {
    color: 'white',
    fontSize: 16,
    fontWeight: 'bold',
  },
  infoContainer: {
    backgroundColor: 'white',
    padding: 15,
    borderRadius: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  infoTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 10,
    color: '#333',
  },
  infoText: {
    fontSize: 14,
    color: '#666',
    lineHeight: 20,
  },
});

export default AppLifecycleExample;

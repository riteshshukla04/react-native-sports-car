import React, {
  useEffect,
  useState,
  useCallback,
  useMemo,
} from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  Alert,
} from 'react-native';
import AndroidAuto from 'react-native-sportscar';
import type { MediaLibrary, PlaybackInfo } from 'react-native-sportscar';

const AndroidAutoExample: React.FC = () => {
  const [playbackInfo, setPlaybackInfo] = useState<PlaybackInfo | null>(null);
  const [isInitialized, setIsInitialized] = useState(false);
  // Note: Nitro modules use callbacks instead of subscriptions, so no need for subscriptionsRef

  // Sample media library with songs and videos
  const sampleMediaLibrary: MediaLibrary = useMemo(
    () => ({
      layoutType: 'list', // Default layout (can be overridden per folder)
      rootItems: [
        {
          id: 'music_folder',
          title: 'Music',
          subtitle: 'Browse your music collection',
          isPlayable: false,
          mediaType: 'folder',
          layoutType: 'grid', // Explicitly set music to grid to test
          children: [
            {
              id: 'album_1',
              title: 'Greatest Hits',
              subtitle:
                'https://plus.unsplash.com/premium_photo-1664474619075-644dd191935f?q=80&w=1738&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
              isPlayable: false,
              mediaType: 'folder',
              iconUrl:
                'https://plus.unsplash.com/premium_photo-1664474619075-644dd191935f?q=80&w=1738&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
              children: [
                {
                  id: 'song_1',
                  title: 'Sample Song 1',
                  subtitle: 'Artist Name',
                  iconUrl:
                    'https://plus.unsplash.com/premium_photo-1664474619075-644dd191935f?q=80&w=1738&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
                  isPlayable: true,
                  mediaType: 'audio',
                  mediaUrl:
                    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3',
                  durationMs: 180000, // 3 minutes
                  metadata: {
                    genre: 'Pop',
                    year: 2023,
                    album: 'Greatest Hits',
                  },
                },
                {
                  id: 'song_2',
                  title: 'Sample Song 2',
                  subtitle: 'Another Artist',
                  iconUrl:
                    'https://plus.unsplash.com/premium_photo-1664474619075-644dd191935f?q=80&w=1738&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
                  isPlayable: true,
                  mediaType: 'audio',
                  mediaUrl:
                    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
                  durationMs: 210000, // 3.5 minutes
                  metadata: {
                    genre: 'Rock',
                    year: 2023,
                    album: 'Greatest Hits',
                  },
                },
              ],
            },
          ],
        },
        {
          id: 'videos_folder',
          title: 'Videos',
          subtitle: 'Browse your video collections',
          isPlayable: false,
          mediaType: 'folder',
          layoutType: 'list',
          children: [
            {
              id: 'video_1',
              title: 'Sample Video 3',
              subtitle: 'Demo Video',
              iconUrl:
                'https://plus.unsplash.com/premium_photo-1664474619075-644dd191935f?q=80&w=1738&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
              isPlayable: true,
              mediaType: 'video',
              mediaUrl:
                'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
              durationMs: 596000, // ~10 minutes
              metadata: {
                resolution: '1080p',
                format: 'mp4',
              },
            },
          ],
        },
        {
          id: 'playlists_folder',
          title: 'Playlists',
          subtitle: 'Your custom playlists',
          isPlayable: false,
          iconUrl:
            'https://plus.unsplash.com/premium_photo-1664474619075-644dd191935f?q=80&w=1738&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
          mediaType: 'folder',
          layoutType: 'grid', // Playlists will be displayed as a grid
          children: [
            {
              id: 'playlist_1',
              title: 'Road Trip Mix',
              subtitle: '5 songs • 15 min',
              iconUrl:
                'https://plus.unsplash.com/premium_photo-1664474619075-644dd191935f?q=80&w=1738&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
              isPlayable: true,
              mediaType: 'audio',
              mediaUrl:
                'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3',
              durationMs: 900000, // 15 minutes
              metadata: {
                songCount: 5,
                totalDuration: 900000,
              },
            },
          ],
        },
      ],
    }),
    []
  );

  const updatePlaybackInfo = useCallback(async () => {
    try {
      const info = await AndroidAuto.getPlaybackState();
      setPlaybackInfo(info);
    } catch (error) {
      console.error('Failed to get playback info:', error);
    }
  }, []);

  const initializeAndroidAuto = useCallback(async () => {
    try {
      const success =
        await AndroidAuto.initializeMediaLibrary(sampleMediaLibrary);
      if (success) {
        setIsInitialized(true);
        Alert.alert('Success', 'Android Auto media library initialized!');
        console.log('✅ Android Auto initialized successfully');
      } else {
        Alert.alert('Error', 'Failed to initialize Android Auto');
      }
    } catch (error) {
      console.error('❌ Failed to initialize Android Auto:', error);
      Alert.alert('Error', 'Failed to initialize Android Auto');
    }
  }, [sampleMediaLibrary]);

  const setupEventListeners = useCallback(() => {
    // Clear any existing callbacks (Nitro modules use callbacks instead of subscriptions)
    AndroidAuto.setPlaybackStateCallback(null);
    AndroidAuto.setMediaPlayerEventCallback(null);

    // Set up callbacks for playback state changes (Nitro modules use callbacks instead of event listeners)
    AndroidAuto.setPlaybackStateCallback((playbackInfo) => {
      console.log('🎵 Playback state changed:', playbackInfo);
      updatePlaybackInfo();
    });

    // Set up callbacks for media player events
    AndroidAuto.setMediaPlayerEventCallback((event) => {
      console.log('🎶 Media player event:', event.type, event.data);
      
      switch (event.type) {
        case 'playbackStateChanged':
          updatePlaybackInfo();
          break;
        case 'mediaChanged':
          updatePlaybackInfo();
          break;
        case 'positionChanged':
          console.log('⏰ Position changed:', event.data);
          break;
        case 'error':
          console.error('❌ Android Auto error:', event.data);
          Alert.alert('Playback Error', 'An error occurred during playback');
          break;
        default:
          console.log('📱 Other event:', event.type, event.data);
      }
    });
  }, [updatePlaybackInfo]);

  useEffect(() => {
    initializeAndroidAuto();
    setupEventListeners();

    return () => {
      // Clean up callbacks properly (Nitro modules use callbacks instead of subscriptions)
      AndroidAuto.setPlaybackStateCallback(null);
      AndroidAuto.setMediaPlayerEventCallback(null);
    };
  }, [initializeAndroidAuto, setupEventListeners]);

  const handlePlayMedia = async (mediaId: string) => {
    try {
      const success = await AndroidAuto.playMedia(mediaId);
      if (success) {
        console.log(`▶️ Playing media: ${mediaId}`);
      } else {
        Alert.alert('Error', 'Failed to play media');
      }
    } catch (error) {
      console.error('Failed to play media:', error);
      Alert.alert('Error', 'Failed to play media');
    }
  };

  const handlePause = async () => {
    try {
      await AndroidAuto.pause();
      console.log('⏸️ Playback paused');
    } catch (error) {
      console.error('Failed to pause:', error);
    }
  };

  const handleResume = async () => {
    try {
      await AndroidAuto.resume();
      console.log('▶️ Playback resumed');
    } catch (error) {
      console.error('Failed to resume:', error);
    }
  };

  const handleStop = async () => {
    try {
      await AndroidAuto.stop();
      console.log('⏹️ Playback stopped');
    } catch (error) {
      console.error('Failed to stop:', error);
    }
  };

  const handleSeek = async (positionMs: number) => {
    try {
      await AndroidAuto.seekTo(positionMs);
      console.log(`⏩ Seeked to ${positionMs}ms`);
    } catch (error) {
      console.error('Failed to seek:', error);
    }
  };

  const handleSetLayoutType = async (layoutType: 'grid' | 'list') => {
    try {
      await AndroidAuto.setLayoutType(layoutType);
      console.log(`📱 Layout type set to: ${layoutType}`);
      Alert.alert('Success', `Layout changed to ${layoutType}`);
    } catch (error) {
      console.error('Failed to set layout type:', error);
    }
  };

  const handleRefreshUI = async () => {
    try {
      await AndroidAuto.updateMediaLibrary(sampleMediaLibrary);
      console.log('🔄 Android Auto UI refreshed');
      Alert.alert('Success', 'Android Auto UI refreshed!');
    } catch (error) {
      console.error('Failed to refresh Android Auto UI:', error);
      Alert.alert('Error', 'Failed to refresh Android Auto UI');
    }
  };

  const handleUpdateLibrary = async () => {
    try {
      // Add a new song to demonstrate library updates
      const updatedLibrary: MediaLibrary = {
        ...sampleMediaLibrary,
        rootItems: [
          ...sampleMediaLibrary.rootItems,
          {
            id: 'new_song',
            title: 'New Song Added',
            subtitle: 'Dynamic Content',
            iconUrl: 'https://via.placeholder.com/150/FF7675/FFFFFF?text=New',
            isPlayable: true,
            mediaType: 'audio',
            mediaUrl:
              'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3',
            durationMs: 240000,
          },
        ],
      };

      const success = await AndroidAuto.updateMediaLibrary(updatedLibrary);
      if (success) {
        Alert.alert('Success', 'Media library updated with new content!');
      }
    } catch (error) {
      console.error('Failed to update library:', error);
    }
  };

  const renderPlaybackControls = () => (
    <View style={styles.controlsContainer}>
      <Text style={styles.sectionTitle}>Playback Controls</Text>

      {playbackInfo && (
        <View style={styles.playbackInfo}>
          <Text>State: {playbackInfo.state}</Text>
          <Text>Position: {Math.round(playbackInfo.positionMs / 1000)}s</Text>
          <Text>Duration: {Math.round(playbackInfo.durationMs / 1000)}s</Text>
          <Text>Speed: {playbackInfo.playbackSpeed}x</Text>
        </View>
      )}

      <View style={styles.buttonRow}>
        <TouchableOpacity
          style={styles.button}
          onPress={() => handlePlayMedia('song_1')}
        >
          <Text style={styles.buttonText}>Play Song 1</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={styles.button}
          onPress={() => handlePlayMedia('video_1')}
        >
          <Text style={styles.buttonText}>Play Video 1</Text>
        </TouchableOpacity>
      </View>

      <View style={styles.buttonRow}>
        <TouchableOpacity style={styles.button} onPress={handlePause}>
          <Text style={styles.buttonText}>Pause</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={handleResume}>
          <Text style={styles.buttonText}>Resume</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={handleStop}>
          <Text style={styles.buttonText}>Stop</Text>
        </TouchableOpacity>
      </View>

      <View style={styles.buttonRow}>
        <TouchableOpacity
          style={styles.button}
          onPress={() => handleSeek(30000)}
        >
          <Text style={styles.buttonText}>Seek to 30s</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={updatePlaybackInfo}>
          <Text style={styles.buttonText}>Refresh Info</Text>
        </TouchableOpacity>
      </View>
    </View>
  );

  const renderLayoutControls = () => (
    <View style={styles.controlsContainer}>
      <Text style={styles.sectionTitle}>Layout Controls</Text>
      <View style={styles.buttonRow}>
        <TouchableOpacity
          style={styles.button}
          onPress={() => handleSetLayoutType('grid')}
        >
          <Text style={styles.buttonText}>Grid Layout</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={styles.button}
          onPress={() => handleSetLayoutType('list')}
        >
          <Text style={styles.buttonText}>List Layout</Text>
        </TouchableOpacity>
      </View>
      <View style={styles.buttonRow}>
        <TouchableOpacity style={styles.button} onPress={handleRefreshUI}>
          <Text style={styles.buttonText}>🔄 Refresh UI</Text>
        </TouchableOpacity>
      </View>
    </View>
  );

  const renderLibraryControls = () => (
    <View style={styles.controlsContainer}>
      <Text style={styles.sectionTitle}>Library Controls</Text>
      <TouchableOpacity style={styles.button} onPress={handleUpdateLibrary}>
        <Text style={styles.buttonText}>Add New Song</Text>
      </TouchableOpacity>
    </View>
  );

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Android Auto Media Player</Text>

      <View style={styles.statusContainer}>
        <Text style={styles.statusText}>
          Status: {isInitialized ? '✅ Initialized' : '❌ Not Initialized'}
        </Text>
      </View>

      {isInitialized ? (
        <>
          {renderPlaybackControls()}
          {renderLayoutControls()}
          {renderLibraryControls()}
        </>
      ) : (
        <TouchableOpacity style={styles.button} onPress={initializeAndroidAuto}>
          <Text style={styles.buttonText}>Initialize Android Auto</Text>
        </TouchableOpacity>
      )}
    </ScrollView>
  );
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
    marginBottom: 20,
    color: '#333',
  },
  statusContainer: {
    backgroundColor: '#fff',
    padding: 15,
    borderRadius: 8,
    marginBottom: 20,
    alignItems: 'center',
  },
  statusText: {
    fontSize: 16,
    fontWeight: '600',
  },
  controlsContainer: {
    backgroundColor: '#fff',
    padding: 15,
    borderRadius: 8,
    marginBottom: 15,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 15,
    color: '#333',
  },
  buttonRow: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    marginBottom: 10,
  },
  button: {
    backgroundColor: '#007AFF',
    paddingHorizontal: 15,
    paddingVertical: 10,
    borderRadius: 6,
    minWidth: 80,
  },
  buttonText: {
    color: '#fff',
    textAlign: 'center',
    fontSize: 14,
    fontWeight: '600',
  },
  playbackInfo: {
    backgroundColor: '#f8f9fa',
    padding: 10,
    borderRadius: 6,
    marginBottom: 15,
  },
});

export default AndroidAutoExample;

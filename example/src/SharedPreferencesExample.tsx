import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Alert,
  ScrollView,
} from 'react-native';
import SharedPreferencesModule from '../../src/SharedPreferencesModule';

// Example media library data
const exampleGridData = {
  layoutType: 'GRID',
  rootItems: [
    {
      id: 'albums_id',
      title: 'Albums',
      subtitle: 'Browse by albums',
      isPlayable: false,
      children: [
        {
          id: 'album_thriller',
          title: 'Thriller',
          subtitle: 'Michael Jackson',
          iconUrl:
            'https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D',
          isPlayable: false,
          children: [
            {
              id: 'track_billie_jean',
              title: 'Billie Jean',
              subtitle: 'Michael Jackson',
              iconUrl:
                'https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D',
              isPlayable: true,
              mediaUrl:
                'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3',
            },
            {
              id: 'track_beat_it',
              title: 'Beat It',
              subtitle: 'Michael Jackson',
              iconUrl:
                'https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D',
              isPlayable: true,
              mediaUrl:
                'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
            },
          ],
        },
        {
          id: 'album_black',
          title: 'Back in Black',
          subtitle: 'AC/DC',
          iconUrl:
            'https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D',
          isPlayable: false,
          children: [
            {
              id: 'track_hells_bells',
              title: 'Hells Bells',
              subtitle: 'AC/DC',
              iconUrl:
                'https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D',
              isPlayable: true,
              mediaUrl:
                'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3',
            },
            {
              id: 'track_shoot_thrill',
              title: 'Shoot to Thrill',
              subtitle: 'AC/DC',
              iconUrl:
                'https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D',
              isPlayable: true,
              mediaUrl:
                'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3',
            },
          ],
        },
      ],
    },
    {
      id: 'artists_id',
      title: 'Artists',
      subtitle: 'Browse by artists',
      isPlayable: false,
      children: [
        {
          id: 'artist_mj',
          title: 'Michael Jackson',
          subtitle: 'Pop Legend',
          iconUrl:
            'https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D',
          isPlayable: false,
          children: [
            {
              id: 'album_thriller',
              title: 'Thriller',
              subtitle: 'Album',
              iconUrl:
                'https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D',
              isPlayable: false,
            },
          ],
        },
        {
          id: 'artist_acdc',
          title: 'AC/DC',
          subtitle: 'Rock Band',
          iconUrl:
            'https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D',
          isPlayable: false,
          children: [
            {
              id: 'album_black',
              title: 'Back in Black',
              subtitle: 'Album',
              iconUrl:
                'https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D',
              isPlayable: false,
            },
          ],
        },
      ],
    },
    {
      id: 'playlists_id',
      title: 'Playlists',
      subtitle: 'Your playlists',
      isPlayable: false,
      children: [
        {
          id: 'playlist_workout',
          title: 'Workout Mix',
          subtitle: 'Mixed Artists',
          iconUrl:
            'https://images.unsplash.com/photo-1526779259212-939e64788e3c?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8N3x8ZnJlZSUyMGltYWdlc3xlbnwwfHwwfHx8MA%3D%3D',
          isPlayable: true,
          mediaUrl:
            'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3',
        },
      ],
    },
  ],
};

const exampleListData = {
  layoutType: 'LIST',
  rootItems: [
    {
      id: 'artists_id',
      title: 'Artists',
      subtitle: 'Browse by artists',
      isPlayable: false,
      children: [
        {
          id: 'artist_mj',
          title: 'Michael Jackson',
          subtitle: 'Pop Legend',
          isPlayable: false,
        },
      ],
    },
  ],
};

const SharedPreferencesExample: React.FC = () => {
  const [hasData, setHasData] = useState<boolean>(false);
  const [dataSize, setDataSize] = useState<number>(0);
  const [layoutType, setLayoutType] = useState<string>('');

  useEffect(() => {
    checkDataStatus();
  }, []);

  const checkDataStatus = async () => {
    try {
      const exists = await SharedPreferencesModule.hasMediaLibraryData();
      const size = await SharedPreferencesModule.getDataSize();
      const layout = await SharedPreferencesModule.readLayoutType();

      setHasData(exists);
      setDataSize(size);
      setLayoutType(layout);
    } catch (error) {
      console.error('Error checking data status:', error);
    }
  };

  const writeGridData = async () => {
    try {
      const jsonString = JSON.stringify(exampleGridData);
      await SharedPreferencesModule.writeMediaLibraryData(jsonString);
      Alert.alert('Success', 'Grid data written to SharedPreferences');
      checkDataStatus();
    } catch (error) {
      Alert.alert('Error', `Failed to write grid data: ${error}`);
    }
  };

  const writeListData = async () => {
    try {
      const jsonString = JSON.stringify(exampleListData);
      await SharedPreferencesModule.writeMediaLibraryData(jsonString);
      Alert.alert('Success', 'List data written to SharedPreferences');
      checkDataStatus();
    } catch (error) {
      Alert.alert('Error', `Failed to write list data: ${error}`);
    }
  };

  const readData = async () => {
    try {
      const data = await SharedPreferencesModule.readMediaLibraryData();
      if (data) {
        Alert.alert(
          'Data Retrieved',
          `Data size: ${data.length} characters\n\nFirst 200 chars:\n${data.substring(0, 200)}...`
        );
      } else {
        Alert.alert('No Data', 'No data found in SharedPreferences');
      }
    } catch (error) {
      Alert.alert('Error', `Failed to read data: ${error}`);
    }
  };

  const clearData = async () => {
    try {
      await SharedPreferencesModule.clearMediaLibraryData();
      Alert.alert('Success', 'All data cleared from SharedPreferences');
      checkDataStatus();
    } catch (error) {
      Alert.alert('Error', `Failed to clear data: ${error}`);
    }
  };

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>SharedPreferences Module Example</Text>

      <View style={styles.statusContainer}>
        <Text style={styles.statusText}>Data Status:</Text>
        <Text style={styles.statusText}>
          • Has Data: {hasData ? 'Yes' : 'No'}
        </Text>
        <Text style={styles.statusText}>• Data Size: {dataSize} bytes</Text>
        <Text style={styles.statusText}>
          • Layout Type: {layoutType || 'None'}
        </Text>
      </View>

      <View style={styles.buttonContainer}>
        <TouchableOpacity style={styles.button} onPress={writeGridData}>
          <Text style={styles.buttonText}>Write Grid Data</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.button} onPress={writeListData}>
          <Text style={styles.buttonText}>Write List Data</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.button} onPress={readData}>
          <Text style={styles.buttonText}>Read Data</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.button, styles.clearButton]}
          onPress={clearData}
        >
          <Text style={styles.buttonText}>Clear All Data</Text>
        </TouchableOpacity>
      </View>

      <View style={styles.infoContainer}>
        <Text style={styles.infoTitle}>How it works:</Text>
        <Text style={styles.infoText}>
          1. Write Grid/List data to SharedPreferences{'\n'}
          2. The Android Auto service will automatically load this data on
          startup{'\n'}
          3. The layout type (GRID/LIST) will be applied to the Android Auto
          interface{'\n'}
          4. Data persists between app restarts
        </Text>
      </View>
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
    elevation: 2,
  },
  statusText: {
    fontSize: 16,
    marginBottom: 5,
    color: '#555',
  },
  buttonContainer: {
    marginBottom: 20,
  },
  button: {
    backgroundColor: '#007AFF',
    padding: 15,
    borderRadius: 8,
    marginBottom: 10,
    alignItems: 'center',
  },
  clearButton: {
    backgroundColor: '#FF3B30',
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
  infoContainer: {
    backgroundColor: '#fff',
    padding: 15,
    borderRadius: 8,
    elevation: 2,
  },
  infoTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 10,
    color: '#333',
  },
  infoText: {
    fontSize: 14,
    lineHeight: 20,
    color: '#666',
  },
});

export default SharedPreferencesExample;

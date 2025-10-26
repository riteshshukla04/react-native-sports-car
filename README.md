# React Native Sports Car 🏎️

A React Native module for Android Auto integration with customizable media player support for both audio and video content.

## Features

- 🎵 **Audio & Video Support**: Play songs and videos through Android Auto (⚠️ Video support is experimental)
- 🎨 **Fully Customizable UI**: Configure layouts and media items from TypeScript
- 🚗 **Android Auto Integration**: Native `MediaBrowserServiceCompat` implementation
- ⚡ **Real-time Controls**: Play, pause, seek, speed control, and more
- 📱 **Callback System**: React to playback state changes and media transitions (Nitro Modules)
- 🖼️ **Image Caching**: Efficient artwork loading with Glide integration
- 🚀 **Nitro Modules**: Built with the latest React Native architecture for better performance

> **⚠️ Important**: Video content only plays in **parked mode** due to **Android Auto platform restrictions**, not module limitations. While driving, only audio from videos will play.
>
> **🧪 Experimental**: Video support is currently experimental and may have limitations or issues. Audio playback is fully stable and recommended for production use.

## Installation

```bash
npm install react-native-sportscar react-native-nitro-modules

# or
yarn add react-native-sportscar react-native-nitro-modules
```

**Dependencies**:

- React Native >= 0.60
- Android minSdkVersion >= 29
- Android Auto compatible device or DHU

## Android Setup

#### 1. Update your `android/build.gradle` (project level)

```gradle
buildscript {
    ext {
        minSdkVersion = 29  // Required for Android Auto
        compileSdkVersion = 34
        targetSdkVersion = 34
    }
}
```

#### 2. Add to your `android/app/src/main/AndroidManifest.xml`

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Required Permissions -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

    <!-- Android Auto Features -->
    <uses-feature android:name="android.hardware.screen.landscape" />
    <uses-feature android:name="android.software.leanback" />

    <application>
        <!-- Android Auto Media Service -->
        <service
            android:name="com.sportscar.service.AndroidAutoMediaService"
            android:exported="true"
            android:foregroundServiceType="mediaPlayback">
            <intent-filter>
                <action android:name="android.media.browse.MediaBrowserService" />
            </intent-filter>
        </service>

        <!-- Android Auto App Descriptor -->
        <meta-data
            android:name="com.google.android.gms.car.application"
            android:resource="@xml/automotive_app_desc" />

        <!-- Handle potential conflicts with other AndroidX Car App dependencies -->
        <meta-data
            android:name="androidx.car.app.CarAppMetadataHolderService.CAR_HARDWARE_MANAGER"
            android:value="androidx.car.app.hardware.ProjectedCarHardwareManager"
            tools:replace="android:value" />
    </application>
</manifest>
```

#### 3. Create `android/app/src/main/res/xml/automotive_app_desc.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<automotiveApp>
    <uses name="media" />
    <uses name="video" />
</automotiveApp>
```

## Usage

### React Hooks

The library provides custom React hooks for easier state management:

#### usePlaybackStateChange Hook

A simple hook for tracking Android Auto playback state:

```typescript
import { usePlaybackStateChange } from 'react-native-sportscar';

const MyComponent = () => {
  const {
    playbackInfo,
    isLoading,
    isPlaying,
    isStopped,
    isBuffering,
    refresh,
  } = usePlaybackStateChange({
    fetchInitialState: true,
    onStateChange: (info) => {
      console.log('Playback state changed:', info.state);
    },
  });

  return (
    <View>
      <Text>State: {playbackInfo?.state}</Text>
      <Text>Playing: {isPlaying ? 'Yes' : 'No'}</Text>
      <Text>Stopped: {isStopped ? 'Yes' : 'No'}</Text>
      <Text>Buffering: {isBuffering ? 'Yes' : 'No'}</Text>
    </View>
  );
};
```

### Basic Setup

```typescript
import AndroidAuto from 'react-native-sports-car';
import type {
  MediaLibrary,
  AndroidAutoMediaItem,
} from 'react-native-sports-car';

// Define your media library
const mediaLibrary: MediaLibrary = {
  layoutType: 'GRID',
  items: [
    {
      id: 'song1',
      title: 'Bohemian Rhapsody',
      artist: 'Queen',
      album: 'A Night at the Opera',
      duration: 355000, // in milliseconds
      mediaType: 'AUDIO',
      mediaUrl: 'https://example.com/bohemian-rhapsody.mp3',
      artworkUrl: 'https://example.com/queen-album-art.jpg',
      genre: 'Rock',
      year: 1975,
    },
    {
      id: 'video1',
      title: 'Music Video',
      artist: 'Artist Name',
      duration: 240000,
      mediaType: 'VIDEO',
      mediaUrl: 'https://example.com/music-video.mp4',
      artworkUrl: 'https://example.com/video-thumbnail.jpg',
    },
  ],
};

// Initialize the media library
await AndroidAuto.initializeMediaLibrary(JSON.stringify(mediaLibrary));
```

### Playback Controls

```typescript
// Play media
await AndroidAuto.playMedia('song1');

// Control playback
await AndroidAuto.pause();
await AndroidAuto.resume();
await AndroidAuto.stop();

// Next/Previous tracks
await AndroidAuto.skipToNext();
await AndroidAuto.skipToPrevious();

// Seek to position (in milliseconds)
await AndroidAuto.seekTo(120000); // Seek to 2 minutes

// Set playback speed
await AndroidAuto.setPlaybackSpeed(1.5); // 1.5x speed

// Get current playback state
const playbackInfo = await AndroidAuto.getPlaybackState();
console.log('Current position:', playbackInfo.currentPosition);
console.log('Is playing:', playbackInfo.isPlaying);
```

### Event Callbacks (Nitro Modules)

Since Nitro Modules don't support event listeners, the library uses callback-based event handling:

```typescript
import { useEffect } from 'react';

useEffect(() => {
  // Set up playback state callback
  AndroidAuto.setPlaybackStateCallback((playbackInfo) => {
    console.log('Playback state:', playbackInfo.state);
    console.log('Current position:', playbackInfo.positionMs);
    console.log('Is playing:', playbackInfo.state === 'playing');
  });

  // Set up media player event callback
  AndroidAuto.setMediaPlayerEventCallback((eventType, data) => {
    switch (eventType) {
      case 'mediaItemChanged':
        console.log('Now playing:', data.mediaItem.title);
        break;
      case 'error':
        console.error('Playback error:', data.error);
        break;
      case 'playbackStateChanged':
        console.log('Playback state changed:', data.state);
        break;
    }
  });

  return () => {
    // Clean up callbacks
    AndroidAuto.setPlaybackStateCallback(null);
    AndroidAuto.setMediaPlayerEventCallback(null);
  };
}, []);
```

### Playlist Management

```typescript
import { createPlaylist, getNextTrack, getPreviousTrack } from 'react-native-sportscar';

// Create playlist from all playable items
const playlist = createPlaylist(mediaLibrary, { includeAllItems: true });

// Navigate tracks manually
const nextTrackId = getNextTrack(playlist, currentTrackId, true); // with repeat
const prevTrackId = getPreviousTrack(playlist, currentTrackId, true);

if (nextTrackId) {
  await AndroidAuto.playMedia(nextTrackId);
}
```

### Dynamic Library Updates

```typescript
// Update media library with new content
const updatedLibrary: MediaLibrary = {
  layoutType: 'LIST',
  items: [...existingItems, ...newItems],
};

await AndroidAuto.updateMediaLibrary(JSON.stringify(updatedLibrary));
```

### Layout Types

```typescript
// Grid layout (default)
const gridLibrary: MediaLibrary = {
  layoutType: 'GRID',
  items: mediaItems,
};

// List layout
const listLibrary: MediaLibrary = {
  layoutType: 'LIST',
  items: mediaItems,
};

// Set layout type separately
await AndroidAuto.setLayoutType('GRID');
```

## API Reference

### Methods

| Method                        | Parameters                                   | Return Type                            | Description                     |
| ----------------------------- | -------------------------------------------- | -------------------------------------- | ------------------------------- |
| `initializeMediaLibrary`      | `jsonString: string`                         | `Promise<boolean>`                     | Initialize media library        |
| `updateMediaLibrary`          | `jsonString: string`                         | `Promise<boolean>`                     | Update media library            |
| `getMediaLibrary`             | -                                            | `Promise<MediaLibrary>`                | Get current media library       |
| `playMedia`                   | `mediaId: string`                            | `Promise<boolean>`                     | Play specific media item        |
| `pause`                       | -                                            | `Promise<boolean>`                     | Pause playback                  |
| `resume`                      | -                                            | `Promise<boolean>`                     | Resume playback                 |
| `stop`                        | -                                            | `Promise<boolean>`                     | Stop playback                   |
| `skipToNext`                  | -                                            | `Promise<boolean>`                     | Skip to next track              |
| `skipToPrevious`              | -                                            | `Promise<boolean>`                     | Skip to previous track          |
| `seekTo`                      | `positionMs: number`                         | `Promise<boolean>`                     | Seek to position                |
| `setPlaybackSpeed`            | `speed: number`                              | `Promise<boolean>`                     | Set playback speed              |
| `getPlaybackState`            | -                                            | `Promise<PlaybackInfo>`                | Get current state               |
| `setLayoutType`               | `layoutType: string`                         | `Promise<boolean>`                     | Set UI layout                   |
| `refreshAndroidAutoUI`        | -                                            | `Promise<boolean>`                     | Refresh Android Auto UI         |
| `isCurrentlyPlaying`          | -                                            | `Promise<boolean>`                     | Check if currently playing      |
| `getLastPlayedMediaInfo`      | -                                            | `Promise<LastPlayedMediaInfo \| null>` | Get last played media info      |
| `handleAppStateChange`        | `appState: string`                           | `Promise<boolean>`                     | Handle app state change         |
| `getCurrentAppState`          | -                                            | `Promise<AppState>`                    | Get current app state           |
| `setPlaybackStateCallback`    | `callback: PlaybackStateCallback \| null`    | `void`                                 | Set playback state callback     |
| `setMediaPlayerEventCallback` | `callback: MediaPlayerEventCallback \| null` | `void`                                 | Set media player event callback |

### Callbacks (Nitro Modules)

| Callback                   | Parameters                                              | Description                        |
| -------------------------- | ------------------------------------------------------- | ---------------------------------- |
| `PlaybackStateCallback`    | `(playbackInfo: PlaybackInfo) => void`                  | Called when playback state changes |
| `MediaPlayerEventCallback` | `(eventType: MediaPlayerEventType, data?: any) => void` | Called for media player events     |

### Event Types

| Event Type             | Description            |
| ---------------------- | ---------------------- |
| `playbackStateChanged` | Playback state updated |
| `mediaItemChanged`     | Media item changed     |
| `error`                | Error occurred         |

### Types

```typescript
// Media Types
export type LayoutType = 'grid' | 'list';
export type MediaType = 'audio' | 'video' | 'folder';
export type PlaybackState =
  | 'playing'
  | 'paused'
  | 'stopped'
  | 'buffering'
  | 'error';
export type AppState = 'foreground' | 'background' | 'destroyed';
export type RepeatMode = 'none' | 'one' | 'all';

// Media Item Interface
interface MediaItem {
  id: string;
  title: string;
  artist?: string;
  album?: string;
  duration?: number; // milliseconds
  mediaType: MediaType;
  mediaUrl: string;
  artworkUrl?: string;
  genre?: string;
  year?: number;
  children?: MediaItem[]; // for folders
}

// Media Library Interface
interface MediaLibrary {
  layoutType: LayoutType;
  items: MediaItem[];
}

// Playback Information
interface PlaybackInfo {
  state: PlaybackState;
  currentMediaId?: string;
  positionMs: number; // milliseconds
  durationMs: number; // milliseconds
  playbackSpeed: number;
  shuffleEnabled: boolean;
  repeatMode: RepeatMode;
}

// Last Played Media Info
interface LastPlayedMediaInfo {
  mediaId: string;
  positionMs: number; // milliseconds
}

// Callback Types
type PlaybackStateCallback = (playbackInfo: PlaybackInfo) => void;
type MediaPlayerEventCallback = (
  eventType: MediaPlayerEventType,
  data?: any
) => void;
type MediaPlayerEventType =
  | 'playbackStateChanged'
  | 'mediaItemChanged'
  | 'error';
```

## Testing with Android Auto

### Desktop Head Unit (DHU)

1. **Install Android Auto DHU**:

   ```bash
   # Download from Android Developer site
   # Extract and run
   ./desktop-head-unit
   ```

2. **Enable Developer Mode**:
   - Install Android Auto app on your phone
   - Tap version number 10 times to enable developer mode
   - Enable "Developer settings" and "Unknown sources"

3. **Connect via USB**:
   - Connect phone to computer via USB
   - Enable USB debugging
   - Launch your app and DHU

4. **Enable Parked Mode** (REQUIRED for video playback):
   - In DHU, go to Settings → Developer settings
   - Enable "Simulate parked mode"
   - Or use ADB: `adb shell settings put global android_auto_parked 1`
   - **Note**: Without parked mode, videos will only play audio

### Physical Android Auto Head Unit

1. Connect phone to car's Android Auto system
2. Launch your app
3. Navigate to Media section in Android Auto interface

## Video Playback Notes

> **🚨 Critical**: Video content **ONLY** plays in parked mode due to **Android Auto platform safety restrictions**.
>
> **🧪 Experimental Feature**: Video support is currently experimental and may not work reliably in all scenarios.

- **While Driving**: Only audio from video files will play (video shows as audio track)
- **Parked Mode**: Full video playback with visual content available
- **Platform Limitation**: This is enforced by Android Auto itself, **NOT** by this module
- **Universal Behavior**: All Android Auto media apps have this same restriction
- **Experimental Status**: Video functionality may have bugs, performance issues, or compatibility problems
- **Production Recommendation**: Use audio-only content for production apps until video support stabilizes
- **Metadata**: Video files show enhanced metadata in Android Auto interface
- **Format Support**: Supports standard video formats (MP4, WebM, etc.)

### Enabling Parked Mode for Testing

**Desktop Head Unit (DHU)**:

```bash
# Method 1: DHU Settings
# Go to Settings → Developer settings → Enable "Simulate parked mode"

# Method 2: ADB Command
adb shell settings put global android_auto_parked 1
```

**Physical Car**:

- Video will automatically play when the car is in park
- No manual configuration needed

## Troubleshooting

### Common Issues

1. **Build Errors**:

   ```bash
   # Clean and rebuild
   cd android && ./gradlew clean
   cd .. && npx react-native run-android
   ```

2. **AndroidX Car App Dependency Conflicts**:

   ```
   Error: Attribute meta-data#androidx.car.app.CarAppMetadataHolderService.CAR_HARDWARE_MANAGER@value
   is also present at [androidx.car.app:app-automotive:1.4.0]
   ```

   **Solution**:
   - Add `xmlns:tools="http://schemas.android.com/tools"` to your manifest
   - Add the meta-data element with `tools:replace="android:value"` as shown in step 2 above
   - If you have `androidx.car.app:app-automotive` in your dependencies, consider removing it if not needed
   - **📖 Detailed Guide**: See [ANDROID_CONFLICT_RESOLUTION.md](./ANDROID_CONFLICT_RESOLUTION.md) for comprehensive solutions

3. **Service Not Found**:
   - Verify service name in AndroidManifest.xml matches exactly
   - Check that automotive_app_desc.xml exists

4. **Video Not Playing While Driving**:
   - **This is normal behavior** - Android Auto restricts video playback while driving
   - Only audio will play from video files for safety reasons
   - Enable parked mode for testing: `adb shell settings put global android_auto_parked 1`

5. **Video Issues (Experimental Feature)**:
   - **Current Limitation**: Video currently only plays audio due to MediaBrowserService constraints
   - Video support is experimental and may have various issues
   - **Technical Issue**: MediaBrowserServiceCompat doesn't provide video rendering surface
   - **Workaround**: Consider using Android Auto Car App Library for full video support
   - Try using audio-only content if experiencing problems
   - Check Android Auto logs for video-specific errors
   - Consider video as a beta feature not ready for production

6. **Playback Issues**:
   - Ensure media URLs are accessible
   - Check network permissions
   - Verify audio focus is properly handled

7. **Android Auto Not Detecting**:
   - Confirm minSdkVersion >= 29
   - Verify all required permissions are added
   - Check automotive app descriptor

### Debug Commands

```bash
# Check Android Auto service
adb shell dumpsys activity service CarService

# Monitor logs
adb logcat | grep -E "(AndroidAuto|MediaService|CAR\.)"

# Check parked mode status
adb shell settings get global android_auto_parked
```

## Contributing

Contributions are welcome! Please read our contributing guidelines and submit pull requests to our repository.

## License

MIT License - see LICENSE file for details.

## Support

For issues and questions:

- GitHub Issues: [Create an issue](https://github.com/your-repo/react-native-sports-car/issues)
- Documentation: [Full API docs](https://github.com/your-repo/react-native-sports-car/docs)

---

Made with ❤️ for the React Native community

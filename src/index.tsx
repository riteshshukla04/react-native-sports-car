import { Platform } from 'react-native';
import { NitroModules } from 'react-native-nitro-modules';
import type { Sportscar } from './Sportscar.nitro';
import type {
  AndroidAutoMediaPlayer,
  PlaybackStateCallback,
  MediaPlayerEventCallback,
  MediaPlayerEventType,
} from './types';

// Platform detection
const isIOS = Platform.OS === 'ios';

let SportscarHybridObject: Sportscar | null = null;

if (!isIOS) {
  SportscarHybridObject =
    NitroModules.createHybridObject<Sportscar>('Sportscar');
}

// Log which architecture is being used (only in development)
if (__DEV__) {
  console.log(
    '🏎️ React Native Sportscar: Using New Architecture (Nitro Modules)'
  );
}

// Callback storage for Nitro modules (since listeners are not supported)
let playbackStateCallback: PlaybackStateCallback | null = null;
let mediaPlayerEventCallback: MediaPlayerEventCallback | null = null;

// Export these for potential use by native code
export { playbackStateCallback, mediaPlayerEventCallback };

// Helper function to check platform and module availability
const checkPlatformAndModule = (methodName: string): boolean => {
  if (isIOS) {
    console.warn(
      `🏎️ React Native Sportscar: iOS does not support ${methodName}`
    );
    return false;
  }

  if (!SportscarHybridObject) {
    console.warn(
      '🏎️ React Native Sportscar: SportscarHybridObject not available'
    );
    return false;
  }

  return true;
};

// Helper function to get the non-null SportscarHybridObject
const getSportscarHybridObject = (): Sportscar => {
  if (!SportscarHybridObject) {
    throw new Error('SportscarHybridObject is not available');
  }
  return SportscarHybridObject;
};

// Helper function to emit events via callbacks
const emitEvent = (eventType: MediaPlayerEventType, data?: any) => {
  if (isIOS) {
    console.warn(
      `🏎️ React Native Sportscar: iOS does not support event emission for ${eventType}`
    );
    return;
  }

  try {
    if (mediaPlayerEventCallback) {
      mediaPlayerEventCallback({ type: eventType, data });
    }
  } catch (error) {
    console.error(
      `🏎️ React Native Sportscar: Failed to emit event ${eventType}:`,
      error
    );
  }
};

// Helper function to emit playback state changes via callbacks
// This function is kept for potential future use by native code
// const emitPlaybackStateChange = (playbackInfo: PlaybackInfo) => {
//   if (isIOS) {
//     console.warn(
//       '🏎️ React Native Sportscar: iOS does not support playback state changes'
//     );
//     return;
//   }

//   try {
//     if (playbackStateCallback) {
//       playbackStateCallback(playbackInfo);
//     }
//   } catch (error) {
//     console.error(
//       '🏎️ React Native Sportscar: Failed to emit playback state change:',
//       error
//     );
//   }
// };

export const AndroidAuto: AndroidAutoMediaPlayer = {
  /**
   * Initialize the Android Auto media service with your media library
   * @param mediaLibrary - The media library structure to display in Android Auto
   * @returns Promise<boolean> - true if initialization was successful
   */
  initializeMediaLibrary: (mediaLibrary) => {
    if (!checkPlatformAndModule('initializeMediaLibrary')) {
      return Promise.resolve(false);
    }

    return getSportscarHybridObject().initializeMediaLibrary(
      JSON.stringify(mediaLibrary)
    );
  },

  /**
   * Update the media library with new content
   * @param mediaLibrary - Updated media library structure
   * @returns Promise<boolean> - true if update was successful
   */
  updateMediaLibrary: (mediaLibrary) => {
    if (!checkPlatformAndModule('updateMediaLibrary')) {
      return Promise.resolve(false);
    }

    return getSportscarHybridObject().updateMediaLibrary(
      JSON.stringify(mediaLibrary)
    );
  },

  /**
   * Set the layout type for the media browser
   * @param layoutType - 'grid' or 'list' layout
   * @returns Promise<boolean> - true if layout was set successfully
   */
  setLayoutType: (layoutType) => {
    if (!checkPlatformAndModule('setLayoutType')) {
      return Promise.resolve(false);
    }

    return getSportscarHybridObject().setLayoutType(layoutType);
  },

  /**
   * Force refresh the Android Auto UI
   * @returns Promise<boolean> - true if refresh was successful
   */
  refreshAndroidAutoUI: () => {
    if (!checkPlatformAndModule('refreshAndroidAutoUI')) {
      return Promise.resolve(false);
    }

    return getSportscarHybridObject().refreshAndroidAutoUI();
  },

  /**
   * Play media by ID
   * @param mediaId - The ID of the media item to play
   * @returns Promise<boolean> - true if playback started successfully
   */
  playMedia: (mediaId) => {
    if (!checkPlatformAndModule('playMedia')) {
      return Promise.resolve(false);
    }

    return getSportscarHybridObject().playMedia(mediaId);
  },

  /**
   * Pause current playback
   * @returns Promise<boolean> - true if pause was successful
   */
  pause: () => {
    if (!checkPlatformAndModule('pause')) {
      return Promise.resolve(false);
    }

    return getSportscarHybridObject().pause();
  },

  /**
   * Resume current playback
   * @returns Promise<boolean> - true if resume was successful
   */
  resume: () => {
    if (!checkPlatformAndModule('resume')) {
      return Promise.resolve(false);
    }

    return getSportscarHybridObject().resume();
  },

  /**
   * Stop current playback
   * @returns Promise<boolean> - true if stop was successful
   */
  stop: () => {
    if (!checkPlatformAndModule('stop')) {
      return Promise.resolve(false);
    }

    return getSportscarHybridObject().stop();
  },

  /**
   * Seek to a specific position in the current media
   * @param positionMs - Position in milliseconds
   * @returns Promise<boolean> - true if seek was successful
   */
  seekTo: (positionMs) => {
    if (!checkPlatformAndModule('seekTo')) {
      return Promise.resolve(false);
    }

    return getSportscarHybridObject().seekTo(positionMs);
  },

  /**
   * Get current playback state
   * @returns Promise<PlaybackState> - Current playback state
   */
  getPlaybackState: () => {
    if (!checkPlatformAndModule('getPlaybackState')) {
      return Promise.resolve({
        state: 'stopped',
        currentMediaId: undefined,
        positionMs: 0,
        durationMs: 0,
        playbackSpeed: 1.0,
        shuffleEnabled: false,
        repeatMode: 'none',
      });
    }

    return getSportscarHybridObject().getPlaybackState();
  },

  /**
   * Set playback speed
   * @param speed - Playback speed (0.5 to 2.0)
   * @returns Promise<boolean> - true if speed was set successfully
   */
  setPlaybackSpeed: (speed) => {
    if (!checkPlatformAndModule('setPlaybackSpeed')) {
      return Promise.resolve(false);
    }

    return getSportscarHybridObject().setPlaybackSpeed(speed);
  },

  /**
   * Handle app state changes (foreground/background/destroyed)
   * This allows the service to respond to app lifecycle changes
   * @param appState - Current app state
   * @returns Promise<boolean> - true if state change was handled successfully
   */
  handleAppStateChange: (appState) => {
    if (!checkPlatformAndModule('handleAppStateChange')) {
      return Promise.resolve(false);
    }

    return getSportscarHybridObject().handleAppStateChange(appState);
  },

  /**
   * Get current app state
   * @returns Promise<AppState> - Current app state
   */
  getCurrentAppState: () => {
    if (!checkPlatformAndModule('getCurrentAppState')) {
      return Promise.resolve('foreground');
    }

    return getSportscarHybridObject().getCurrentAppState();
  },

  /**
   * Check if service is currently playing
   * @returns Promise<boolean> - true if currently playing
   */
  isCurrentlyPlaying: () => {
    if (!checkPlatformAndModule('isCurrentlyPlaying')) {
      return Promise.resolve(false);
    }

    return getSportscarHybridObject().isCurrentlyPlaying();
  },

  /**
   * Get last played media information
   * @returns Promise<LastPlayedMediaInfo | null> - Last played media info or null
   */
  getLastPlayedMediaInfo: () => {
    if (!checkPlatformAndModule('getLastPlayedMediaInfo')) {
      return Promise.resolve(null);
    }

    return getSportscarHybridObject().getLastPlayedMediaInfo();
  },

  /**
   * Set callback for playback state changes
   * Note: Nitro modules use callbacks instead of event listeners
   * @param callback - Callback function for playback state changes
   */
  setPlaybackStateCallback: (callback) => {
    if (isIOS) {
      console.warn(
        '🏎️ React Native Sportscar: iOS does not support playback state callbacks'
      );
      return;
    }

    playbackStateCallback = callback;
  },

  /**
   * Set callback for media player events
   * Note: Nitro modules use callbacks instead of event listeners
   * @param callback - Callback function for media player events
   */
  setMediaPlayerEventCallback: (callback) => {
    if (isIOS) {
      console.warn(
        '🏎️ React Native Sportscar: iOS does not support media player event callbacks'
      );
      return;
    }

    mediaPlayerEventCallback = callback;
  },

  /**
   * Emit an event (for testing purposes)
   * @param eventType - Type of event to emit
   * @param data - Event data
   */
  emit: (eventType, data) => {
    emitEvent(eventType, data);
  },
};

// Export types for TypeScript users
export * from './types';

// Export hooks
export * from './hooks';

// Default export
export default AndroidAuto;

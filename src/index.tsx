import { NativeEventEmitter, Platform } from 'react-native';
import type { AndroidAutoMediaPlayer } from './types';
import AndroidAutoSpec from './specs/NativeSportscarSpec';

// This library requires React Native's New Architecture (TurboModules)
const AndroidAutoModule = Platform.OS === 'android' ? AndroidAutoSpec : null;

// Platform detection
const isIOS = Platform.OS === 'ios';

// Log which architecture is being used (only in development)
if (__DEV__) {
  console.log(
    '🏎️ React Native Sportscar: Using New Architecture (TurboModules)'
  );
}

// Create event emitter for media player events (TurboModules use undefined for native module)
// On iOS, we create a dummy event emitter that does nothing
const eventEmitter = isIOS
  ? {
      addListener: () => ({
        remove: () => {
          console.warn(
            '🏎️ React Native Sportscar: Dummy event listener removed'
          );
        },
      }),
      removeAllListeners: () => {
        console.warn(
          '🏎️ React Native Sportscar: Dummy removeAllListeners called'
        );
      },
      emit: () => {
        console.warn('🏎️ React Native Sportscar: Dummy event emit called');
      },
    }
  : new NativeEventEmitter(undefined);

/**
 * React Native Android Auto Media Player
 *
 * This module provides a bridge between React Native and Android Auto
 * for creating customizable media player experiences in vehicles.
 */

// Helper function to check platform and module availability
const checkPlatformAndModule = (methodName: string) => {
  if (isIOS) {
    console.warn(
      `🏎️ React Native Sportscar: iOS does not support ${methodName}`
    );
    return false;
  }

  if (!AndroidAutoModule) {
    console.warn('🏎️ React Native Sportscar: AndroidAutoModule not available');
    return false;
  }

  return true;
};

// Helper function to emit events safely
const emitEvent = (eventType: string, data?: any) => {
  if (isIOS) {
    console.warn(
      `🏎️ React Native Sportscar: iOS does not support event emission for ${eventType}`
    );
    return;
  }

  try {
    eventEmitter.emit(eventType, data);
  } catch (error) {
    console.error(
      `🏎️ React Native Sportscar: Failed to emit event ${eventType}:`,
      error
    );
  }
};

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

    return AndroidAutoModule!.initializeMediaLibrary(
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

    return AndroidAutoModule!.updateMediaLibrary(JSON.stringify(mediaLibrary));
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

    return AndroidAutoModule!.setLayoutType(layoutType);
  },

  /**
   * Force refresh the Android Auto UI
   * @returns Promise<boolean> - true if refresh was successful
   */
  refreshAndroidAutoUI: () => {
    if (!checkPlatformAndModule('refreshAndroidAutoUI')) {
      return Promise.resolve(false);
    }

    return AndroidAutoModule!.refreshAndroidAutoUI();
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

    return AndroidAutoModule!.playMedia(mediaId);
  },

  /**
   * Pause current playback
   * @returns Promise<boolean> - true if pause was successful
   */
  pause: () => {
    if (!checkPlatformAndModule('pause')) {
      return Promise.resolve(false);
    }

    return AndroidAutoModule!.pause();
  },

  /**
   * Resume current playback
   * @returns Promise<boolean> - true if resume was successful
   */
  resume: () => {
    if (!checkPlatformAndModule('resume')) {
      return Promise.resolve(false);
    }

    return AndroidAutoModule!.resume();
  },

  /**
   * Stop current playback
   * @returns Promise<boolean> - true if stop was successful
   */
  stop: () => {
    if (!checkPlatformAndModule('stop')) {
      return Promise.resolve(false);
    }

    return AndroidAutoModule!.stop();
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

    return AndroidAutoModule!.seekTo(positionMs);
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

    return AndroidAutoModule!.getPlaybackState();
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

    return AndroidAutoModule!.setPlaybackSpeed(speed);
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

    return AndroidAutoModule!.handleAppStateChange(appState);
  },

  /**
   * Get current app state
   * @returns Promise<AppState> - Current app state
   */
  getCurrentAppState: () => {
    if (!checkPlatformAndModule('getCurrentAppState')) {
      return Promise.resolve('foreground');
    }

    return AndroidAutoModule!.getCurrentAppState();
  },

  /**
   * Check if service is currently playing
   * @returns Promise<boolean> - true if currently playing
   */
  isCurrentlyPlaying: () => {
    if (!checkPlatformAndModule('isCurrentlyPlaying')) {
      return Promise.resolve(false);
    }

    return AndroidAutoModule!.isCurrentlyPlaying();
  },

  /**
   * Get last played media information
   * @returns Promise<LastPlayedMediaInfo | null> - Last played media info or null
   */
  getLastPlayedMediaInfo: () => {
    if (!checkPlatformAndModule('getLastPlayedMediaInfo')) {
      return Promise.resolve(null);
    }

    return AndroidAutoModule!.getLastPlayedMediaInfo();
  },

  /**
   * Add event listener for media player events
   * @param eventType - Type of event to listen for
   * @param listener - Callback function
   * @returns Subscription object with remove() method
   */
  addEventListener: (eventType, listener) => {
    if (isIOS) {
      throw new Error(
        '🏎️ React Native Sportscar: iOS does not support addEventListener'
      );
    }

    return eventEmitter.addListener(eventType, listener);
  },

  /**
   * Remove event listener
   * @param eventType - Type of event to remove listener for (unused, kept for compatibility)
   * @param listener - Subscription object returned by addEventListener
   */
  removeEventListener: (_eventType, listener) => {
    if (isIOS) {
      throw new Error(
        '🏎️ React Native Sportscar: iOS does not support removeEventListener'
      );
    }

    if (listener && typeof listener.remove === 'function') {
      listener.remove();
    }
  },

  /**
   * Remove all event listeners for a specific event type
   * @param eventType - Type of event to remove all listeners for
   */
  removeAllListeners: (eventType?: string) => {
    if (isIOS) {
      throw new Error(
        '🏎️ React Native Sportscar: iOS does not support removeAllListeners'
      );
    }

    if (eventType) {
      eventEmitter.removeAllListeners(eventType);
    } else {
      // Remove all listeners for all event types
      ['playbackStateChanged', 'mediaItemChanged', 'error'].forEach((type) => {
        eventEmitter.removeAllListeners(type);
      });
    }
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

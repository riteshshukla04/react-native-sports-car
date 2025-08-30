import { NativeModules, NativeEventEmitter } from 'react-native';
import type { AndroidAutoMediaPlayer } from './types';

const { AndroidAutoModule } = NativeModules;

// Create event emitter for media player events
const eventEmitter = new NativeEventEmitter(AndroidAutoModule);

/**
 * React Native Android Auto Media Player
 *
 * This module provides a bridge between React Native and Android Auto
 * for creating customizable media player experiences in vehicles.
 */
export const AndroidAuto: AndroidAutoMediaPlayer = {
  /**
   * Initialize the Android Auto media service with your media library
   * @param mediaLibrary - The media library structure to display in Android Auto
   * @returns Promise<boolean> - true if initialization was successful
   */
  initializeMediaLibrary: (mediaLibrary) => {
    return AndroidAutoModule.initializeMediaLibrary(
      JSON.stringify(mediaLibrary)
    );
  },

  /**
   * Update the media library with new content
   * @param mediaLibrary - Updated media library structure
   * @returns Promise<boolean> - true if update was successful
   */
  updateMediaLibrary: (mediaLibrary) => {
    return AndroidAutoModule.updateMediaLibrary(JSON.stringify(mediaLibrary));
  },

  /**
   * Set the layout type for the media browser
   * @param layoutType - 'grid' or 'list' layout
   * @returns Promise<boolean> - true if layout was set successfully
   */
  setLayoutType: (layoutType) => {
    return AndroidAutoModule.setLayoutType(layoutType);
  },

  /**
   * Play media by ID
   * @param mediaId - The ID of the media item to play
   * @returns Promise<boolean> - true if playback started successfully
   */
  playMedia: (mediaId) => {
    return AndroidAutoModule.playMedia(mediaId);
  },

  /**
   * Pause current playback
   * @returns Promise<boolean> - true if pause was successful
   */
  pause: () => {
    return AndroidAutoModule.pause();
  },

  /**
   * Resume current playback
   * @returns Promise<boolean> - true if resume was successful
   */
  resume: () => {
    return AndroidAutoModule.resume();
  },

  /**
   * Stop current playback
   * @returns Promise<boolean> - true if stop was successful
   */
  stop: () => {
    return AndroidAutoModule.stop();
  },

  /**
   * Seek to a specific position in the current media
   * @param positionMs - Position in milliseconds
   * @returns Promise<boolean> - true if seek was successful
   */
  seekTo: (positionMs) => {
    return AndroidAutoModule.seekTo(positionMs);
  },

  /**
   * Get current playback state
   * @returns Promise<PlaybackState> - Current playback state
   */
  getPlaybackState: () => {
    return AndroidAutoModule.getPlaybackState();
  },

  /**
   * Set playback speed
   * @param speed - Playback speed (0.5 to 2.0)
   * @returns Promise<boolean> - true if speed was set successfully
   */
  setPlaybackSpeed: (speed) => {
    return AndroidAutoModule.setPlaybackSpeed(speed);
  },

  /**
   * Add event listener for media player events
   * @param eventType - Type of event to listen for
   * @param listener - Callback function
   * @returns Subscription object with remove() method
   */
  addEventListener: (eventType, listener) => {
    return eventEmitter.addListener(eventType, listener);
  },

  /**
   * Remove event listener
   * @param eventType - Type of event to remove listener for (unused, kept for compatibility)
   * @param listener - Subscription object returned by addEventListener
   */
  removeEventListener: (_eventType, listener) => {
    if (listener && typeof listener.remove === 'function') {
      listener.remove();
    }
  },

  /**
   * Remove all event listeners for a specific event type
   * @param eventType - Type of event to remove all listeners for
   */
  removeAllListeners: (eventType?: string) => {
    if (eventType) {
      eventEmitter.removeAllListeners(eventType);
    } else {
      // Remove all listeners for all event types
      ['playbackStateChanged', 'mediaItemChanged', 'error'].forEach((type) => {
        eventEmitter.removeAllListeners(type);
      });
    }
  },
};

// Export types for TypeScript users
export * from './types';

// Default export
export default AndroidAuto;

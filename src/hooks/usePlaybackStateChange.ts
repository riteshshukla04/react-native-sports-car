import { useEffect, useState, useCallback } from 'react';
import { AndroidAuto } from '../index';
import type { PlaybackInfo, MediaPlayerEvent } from '../types';

/**
 * Simple hook for tracking Android Auto playback state
 * 
 * @param options - Configuration options for the hook
 * @returns Object containing current playback state
 */
export const usePlaybackStateChange = (options?: {
  /** Whether to automatically fetch initial playback state on mount */
  fetchInitialState?: boolean;
  /** Callback function called when playback state changes */
  onStateChange?: (playbackInfo: PlaybackInfo) => void;
}) => {
  const [playbackInfo, setPlaybackInfo] = useState<PlaybackInfo | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const {
    fetchInitialState = true,
    onStateChange,
  } = options || {};

  /**
   * Fetch current playback state from Android Auto
   */
  const fetchPlaybackState = useCallback(async () => {
    try {
      setIsLoading(true);
      const state = await AndroidAuto.getPlaybackState();
      setPlaybackInfo(state);
      onStateChange?.(state);
      return state;
    } catch (err) {
      console.error('Failed to fetch playback state:', err);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, [onStateChange]);

  /**
   * Handle playback state change events
   */
  const handlePlaybackStateChange = useCallback((event: MediaPlayerEvent) => {
    if (event.data) {
      setPlaybackInfo(event.data);
      onStateChange?.(event.data);
    }
  }, [onStateChange]);

  // Set up event listeners
  useEffect(() => {
    const subscriptions: Array<{ remove: () => void }> = [];

    // Listen for playback state changes
    const playbackSubscription = AndroidAuto.addEventListener(
      'playbackStateChanged',
      handlePlaybackStateChange
    );
    subscriptions.push(playbackSubscription);

    // Fetch initial state if requested
    if (fetchInitialState) {
      fetchPlaybackState().catch(() => {
        // Error is already handled in fetchPlaybackState
      });
    }

    // Cleanup function
    return () => {
      subscriptions.forEach(subscription => {
        try {
          subscription.remove();
        } catch (err) {
          console.warn('Error removing subscription:', err);
        }
      });
    };
  }, [fetchInitialState, fetchPlaybackState, handlePlaybackStateChange]);

  return {
    // Current playback state
    playbackInfo,
    isLoading,
    
    // Simple state flags
    isPlaying: playbackInfo?.state === 'playing',
    isStopped: playbackInfo?.state === 'stopped',
    isBuffering: playbackInfo?.state === 'buffering',
    
    // Refresh function
    refresh: fetchPlaybackState
  };
};

export default usePlaybackStateChange;

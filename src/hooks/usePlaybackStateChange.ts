import { useEffect, useState, useCallback, useRef } from 'react';
import { AndroidAuto } from '../index';
import type { PlaybackInfo, MediaPlayerEvent } from '../types';

/**
 * Simple hook for tracking Android Auto playback state
 * Updated to use callbacks instead of event listeners for Nitro modules
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
  const isInitializedRef = useRef(false);

  const { fetchInitialState = true, onStateChange } = options || {};

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
   * Handle playback state change events via callback
   */
  const handlePlaybackStateChange = useCallback(
    (playbackInfo: PlaybackInfo) => {
      setPlaybackInfo(playbackInfo);
      onStateChange?.(playbackInfo);
    },
    [onStateChange]
  );

  /**
   * Handle media player events via callback
   */
  const handleMediaPlayerEvent = useCallback(
    (event: MediaPlayerEvent) => {
      if (event.data && event.type === 'playbackStateChanged') {
        setPlaybackInfo(event.data);
        onStateChange?.(event.data);
      }
    },
    [onStateChange]
  );

  // Set up callbacks for Nitro modules
  useEffect(() => {
    // Set up playback state callback
    AndroidAuto.setPlaybackStateCallback(handlePlaybackStateChange);
    
    // Set up media player event callback
    AndroidAuto.setMediaPlayerEventCallback(handleMediaPlayerEvent);

    // Fetch initial state if requested
    if (fetchInitialState && !isInitializedRef.current) {
      isInitializedRef.current = true;
      fetchPlaybackState().catch(() => {
        // Error is already handled in fetchPlaybackState
      });
    }

    // Cleanup function - remove callbacks
    return () => {
      AndroidAuto.setPlaybackStateCallback(null);
      AndroidAuto.setMediaPlayerEventCallback(null);
    };
  }, [fetchInitialState, fetchPlaybackState, handlePlaybackStateChange, handleMediaPlayerEvent]);

  return {
    // Current playback state
    playbackInfo,
    isLoading,

    // Simple state flags
    isPlaying: playbackInfo?.state === 'playing',
    isStopped: playbackInfo?.state === 'stopped',
    isBuffering: playbackInfo?.state === 'buffering',

    // Refresh function
    refresh: fetchPlaybackState,
  };
};

export default usePlaybackStateChange;

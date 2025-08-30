/**
 * Layout type for the Android Auto media browser
 */
export type LayoutType = 'grid' | 'list';

/**
 * Media type for different kinds of content
 */
export type MediaType = 'audio' | 'video' | 'folder';

/**
 * Playback state of the media player
 */
export type PlaybackState =
  | 'playing'
  | 'paused'
  | 'stopped'
  | 'buffering'
  | 'error';

/**
 * Media item that can be displayed in Android Auto
 */
export interface MediaItem {
  /** Unique identifier for the media item */
  id: string;

  /** Display title */
  title: string;

  /** Optional subtitle/description */
  subtitle?: string;

  /** Optional icon/artwork URL */
  iconUrl?: string;

  /** Whether this item can be played directly */
  isPlayable: boolean;

  /** Media URL for playable items */
  mediaUrl?: string;

  /** Media type */
  mediaType?: MediaType;

  /** Duration in milliseconds for media items */
  durationMs?: number;

  /** Child items for browsable folders */
  children?: MediaItem[];

  /** Layout type for folder items (overrides library default) */
  layoutType?: LayoutType;

  /** Custom metadata */
  metadata?: Record<string, any>;
}

/**
 * Media library structure for Android Auto
 */
export interface MediaLibrary {
  /** Layout type for the media browser (applies to all folders) */
  layoutType: LayoutType;

  /** Root level media items */
  rootItems: MediaItem[];

  /** Optional app name to display */
  appName?: string;

  /** Optional app icon URL */
  appIconUrl?: string;
}

/**
 * Current playback information
 */
export interface PlaybackInfo {
  /** Current playback state */
  state: PlaybackState;

  /** Currently playing media ID */
  currentMediaId?: string;

  /** Current position in milliseconds */
  positionMs: number;

  /** Total duration in milliseconds */
  durationMs: number;

  /** Current playback speed */
  playbackSpeed: number;

  /** Whether shuffle is enabled */
  shuffleEnabled: boolean;

  /** Repeat mode */
  repeatMode: 'none' | 'one' | 'all';
}

/**
 * Media player event types
 */
export type MediaPlayerEventType =
  | 'playbackStateChanged'
  | 'mediaChanged'
  | 'positionChanged'
  | 'error'
  | 'buffering'
  | 'ready';

/**
 * Event data for media player events
 */
export interface MediaPlayerEvent {
  type: MediaPlayerEventType;
  data?: any;
}

/**
 * Android Auto Media Player interface
 */
export interface AndroidAutoMediaPlayer {
  /**
   * Initialize the Android Auto media service with your media library
   */
  initializeMediaLibrary(mediaLibrary: MediaLibrary): Promise<boolean>;

  /**
   * Update the media library with new content
   */
  updateMediaLibrary(mediaLibrary: MediaLibrary): Promise<boolean>;

  /**
   * Set the layout type for the media browser
   */
  setLayoutType(layoutType: LayoutType): Promise<boolean>;

  /**
   * Force refresh the Android Auto UI
   */
  refreshAndroidAutoUI(): Promise<boolean>;

  /**
   * Play media by ID
   */
  playMedia(mediaId: string): Promise<boolean>;

  /**
   * Pause current playback
   */
  pause(): Promise<boolean>;

  /**
   * Resume current playback
   */
  resume(): Promise<boolean>;

  /**
   * Stop current playback
   */
  stop(): Promise<boolean>;

  /**
   * Seek to a specific position in the current media
   */
  seekTo(positionMs: number): Promise<boolean>;

  /**
   * Get current playback state
   */
  getPlaybackState(): Promise<PlaybackInfo>;

  /**
   * Set playback speed
   */
  setPlaybackSpeed(speed: number): Promise<boolean>;

  /**
   * Add event listener for media player events
   */
  addEventListener(
    eventType: MediaPlayerEventType,
    listener: (event: MediaPlayerEvent) => void
  ): { remove(): void };

  /**
   * Remove event listener
   */
  removeEventListener(
    eventType: MediaPlayerEventType,
    listener: { remove(): void }
  ): void;

  /**
   * Remove all event listeners
   */
  removeAllListeners(eventType?: MediaPlayerEventType): void;
}

/**
 * Configuration options for Android Auto
 */
export interface AndroidAutoConfig {
  /** Enable debug logging */
  enableDebugLogging?: boolean;

  /** Custom color scheme */
  colorScheme?: {
    primary?: string;
    secondary?: string;
    background?: string;
    text?: string;
  };

  /** Maximum number of items to show per page */
  maxItemsPerPage?: number;

  /** Enable image caching */
  enableImageCaching?: boolean;

  /** Image cache size in MB */
  imageCacheSize?: number;
}

import type { HybridObject } from 'react-native-nitro-modules';

// Types for Android Auto functionality
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

export interface MediaItem {
  id: string;
  title: string;
  subtitle?: string;
  iconUrl?: string;
  isPlayable: boolean;
  mediaUrl?: string;
  mediaType?: MediaType;
  durationMs?: number;
  children?: MediaItem[];
  layoutType?: LayoutType;
  metadata?: Record<string, any>;
}

export interface MediaLibrary {
  layoutType: LayoutType;
  rootItems: MediaItem[];
  appName?: string;
  appIconUrl?: string;
}

export interface PlaybackInfo {
  state: PlaybackState;
  currentMediaId?: string;
  positionMs: number;
  durationMs: number;
  playbackSpeed: number;
  shuffleEnabled: boolean;
  repeatMode: RepeatMode;
}

export interface LastPlayedMediaInfo {
  mediaId?: string;
  positionMs: number;
}

export interface Sportscar
  extends HybridObject<{ ios: 'swift'; android: 'kotlin' }> {
  // Android Auto Media Library Management
  initializeMediaLibrary(mediaLibraryJson: string): Promise<boolean>;
  updateMediaLibrary(mediaLibraryJson: string): Promise<boolean>;
  getMediaLibrary(): Promise<string>;
  setLayoutType(layoutType: string): Promise<boolean>;
  refreshAndroidAutoUI(): Promise<boolean>;

  // Media Playback Control
  playMedia(mediaId: string): Promise<boolean>;
  pause(): Promise<boolean>;
  resume(): Promise<boolean>;
  stop(): Promise<boolean>;
  seekTo(positionMs: number): Promise<boolean>;
  setPlaybackSpeed(speed: number): Promise<boolean>;

  // Playback State Management
  getPlaybackState(): Promise<PlaybackInfo>;
  isCurrentlyPlaying(): Promise<boolean>;
  getLastPlayedMediaInfo(): Promise<LastPlayedMediaInfo | null>;

  // App Lifecycle Management
  handleAppStateChange(appState: string): Promise<boolean>;
  getCurrentAppState(): Promise<AppState>;
}

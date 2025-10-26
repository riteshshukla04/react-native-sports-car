import type { MediaItem, MediaLibrary } from '../types';

/**
 * Utility functions for managing playlists and track navigation in Android Auto
 */

export interface PlaylistItem {
  id: string;
  title: string;
  subtitle?: string;
  mediaUrl?: string;
  mediaType?: 'audio' | 'video';
  durationMs?: number;
  iconUrl?: string;
}

export interface PlaylistOptions {
  /** Whether to include all playable items from the library */
  includeAllItems?: boolean;
  /** Specific folder ID to create playlist from */
  folderId?: string;
  /** Custom list of media IDs to include in playlist */
  mediaIds?: string[];
  /** Whether to shuffle the playlist */
  shuffle?: boolean;
  /** Whether to repeat the playlist */
  repeat?: boolean;
}

/**
 * Create a playlist from a media library
 * @param mediaLibrary - The media library to create playlist from
 * @param options - Playlist creation options
 * @returns Array of playlist items
 */
export function createPlaylist(
  mediaLibrary: MediaLibrary,
  options: PlaylistOptions = {}
): PlaylistItem[] {
  const {
    includeAllItems = true,
    folderId,
    mediaIds,
    shuffle = false,
  } = options;

  let playableItems: MediaItem[] = [];

  if (mediaIds && mediaIds.length > 0) {
    // Create playlist from specific media IDs
    playableItems = mediaIds
      .map((id) => findMediaItemById(mediaLibrary, id))
      .filter((item): item is MediaItem => item !== null && item.isPlayable);
  } else if (folderId) {
    // Create playlist from specific folder
    const folder = findMediaItemById(mediaLibrary, folderId);
    if (folder && folder.children) {
      playableItems = folder.children.filter((item) => item.isPlayable);
    }
  } else if (includeAllItems) {
    // Create playlist from all playable items
    playableItems = getAllPlayableItems(mediaLibrary);
  }

  // Convert to playlist items
  let playlistItems: PlaylistItem[] = playableItems.map((item) => ({
    id: item.id,
    title: item.title,
    subtitle: item.subtitle,
    mediaUrl: item.mediaUrl,
    mediaType: item.mediaType === 'folder' ? undefined : item.mediaType,
    durationMs: item.durationMs,
    iconUrl: item.iconUrl,
  }));

  // Shuffle if requested
  if (shuffle) {
    playlistItems = shuffleArray(playlistItems);
  }

  return playlistItems;
}

/**
 * Get the next track in a playlist
 * @param playlist - The playlist to navigate
 * @param currentTrackId - ID of the current track
 * @param repeat - Whether to repeat the playlist
 * @returns Next track ID or null if no next track
 */
export function getNextTrack(
  playlist: PlaylistItem[],
  currentTrackId: string,
  repeat: boolean = false
): string | null {
  const currentIndex = playlist.findIndex((item) => item.id === currentTrackId);

  if (currentIndex === -1) {
    return null; // Current track not found in playlist
  }

  if (currentIndex < playlist.length - 1) {
    return playlist[currentIndex + 1]?.id || null;
  } else if (repeat && playlist.length > 0) {
    return playlist[0]?.id || null; // Loop back to beginning
  }

  return null; // No next track
}

/**
 * Get the previous track in a playlist
 * @param playlist - The playlist to navigate
 * @param currentTrackId - ID of the current track
 * @param repeat - Whether to repeat the playlist
 * @returns Previous track ID or null if no previous track
 */
export function getPreviousTrack(
  playlist: PlaylistItem[],
  currentTrackId: string,
  repeat: boolean = false
): string | null {
  const currentIndex = playlist.findIndex((item) => item.id === currentTrackId);

  if (currentIndex === -1) {
    return null; // Current track not found in playlist
  }

  if (currentIndex > 0) {
    return playlist[currentIndex - 1]?.id || null;
  } else if (repeat && playlist.length > 0) {
    return playlist[playlist.length - 1]?.id || null; // Loop to end
  }

  return null; // No previous track
}

/**
 * Get all playable items from a media library
 * @param mediaLibrary - The media library to search
 * @returns Array of all playable media items
 */
export function getAllPlayableItems(mediaLibrary: MediaLibrary): MediaItem[] {
  const playableItems: MediaItem[] = [];

  function collectPlayableItems(items: MediaItem[]) {
    items.forEach((item) => {
      if (item.isPlayable && item.mediaUrl) {
        playableItems.push(item);
      }
      if (item.children) {
        collectPlayableItems(item.children);
      }
    });
  }

  collectPlayableItems(mediaLibrary.rootItems);
  return playableItems;
}

/**
 * Find a media item by ID in a media library
 * @param mediaLibrary - The media library to search
 * @param id - The ID to search for
 * @returns The media item or null if not found
 */
export function findMediaItemById(
  mediaLibrary: MediaLibrary,
  id: string
): MediaItem | null {
  function searchItems(items: MediaItem[]): MediaItem | null {
    for (const item of items) {
      if (item.id === id) {
        return item;
      }
      if (item.children) {
        const found = searchItems(item.children);
        if (found) {
          return found;
        }
      }
    }
    return null;
  }

  return searchItems(mediaLibrary.rootItems);
}

/**
 * Create a playlist from a specific folder
 * @param mediaLibrary - The media library to search
 * @param folderId - ID of the folder to create playlist from
 * @param shuffle - Whether to shuffle the playlist
 * @returns Array of playlist items from the folder
 */
export function createPlaylistFromFolder(
  mediaLibrary: MediaLibrary,
  folderId: string,
  shuffle: boolean = false
): PlaylistItem[] {
  return createPlaylist(mediaLibrary, { folderId, shuffle });
}

/**
 * Create a playlist from specific media IDs
 * @param mediaLibrary - The media library to search
 * @param mediaIds - Array of media IDs to include
 * @param shuffle - Whether to shuffle the playlist
 * @returns Array of playlist items
 */
export function createPlaylistFromIds(
  mediaLibrary: MediaLibrary,
  mediaIds: string[],
  shuffle: boolean = false
): PlaylistItem[] {
  return createPlaylist(mediaLibrary, { mediaIds, shuffle });
}

/**
 * Shuffle an array using Fisher-Yates algorithm
 * @param array - Array to shuffle
 * @returns Shuffled array
 */
function shuffleArray<T>(array: T[]): T[] {
  const shuffled = [...array];
  for (let i = shuffled.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    const temp = shuffled[i];
    shuffled[i] = shuffled[j]!;
    shuffled[j] = temp!;
  }
  return shuffled;
}

/**
 * Get playlist statistics
 * @param playlist - The playlist to analyze
 * @returns Playlist statistics
 */
export function getPlaylistStats(playlist: PlaylistItem[]) {
  const totalDuration = playlist.reduce(
    (sum, item) => sum + (item.durationMs || 0),
    0
  );
  const audioCount = playlist.filter(
    (item) => item.mediaType === 'audio'
  ).length;
  const videoCount = playlist.filter(
    (item) => item.mediaType === 'video'
  ).length;

  return {
    totalTracks: playlist.length,
    audioTracks: audioCount,
    videoTracks: videoCount,
    totalDurationMs: totalDuration,
    totalDurationMinutes: Math.round(totalDuration / 60000),
    totalDurationHours: Math.round(totalDuration / 3600000),
  };
}

/**
 * Filter playlist by media type
 * @param playlist - The playlist to filter
 * @param mediaType - Media type to filter by
 * @returns Filtered playlist
 */
export function filterPlaylistByType(
  playlist: PlaylistItem[],
  mediaType: 'audio' | 'video'
): PlaylistItem[] {
  return playlist.filter((item) => item.mediaType === mediaType);
}

/**
 * Search playlist by title or subtitle
 * @param playlist - The playlist to search
 * @param query - Search query
 * @returns Matching playlist items
 */
export function searchPlaylist(
  playlist: PlaylistItem[],
  query: string
): PlaylistItem[] {
  const lowercaseQuery = query.toLowerCase();
  return playlist.filter(
    (item) =>
      item.title.toLowerCase().includes(lowercaseQuery) ||
      (item.subtitle && item.subtitle.toLowerCase().includes(lowercaseQuery))
  );
}

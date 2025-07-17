import { NativeModules } from 'react-native';

interface SharedPreferencesModuleInterface {
  /**
   * Write JSON data to SharedPreferences
   * @param jsonString JSON string to store
   * @returns Promise<boolean> - true if successful
   */
  writeMediaLibraryData(jsonString: string): Promise<boolean>;

  /**
   * Read JSON data from SharedPreferences
   * @returns Promise<string | null> - JSON string or null if no data
   */
  readMediaLibraryData(): Promise<string | null>;

  /**
   * Read only the layout type from SharedPreferences
   * @returns Promise<string> - Layout type ("GRID" or "LIST")
   */
  readLayoutType(): Promise<string>;

  /**
   * Clear all stored data from SharedPreferences
   * @returns Promise<boolean> - true if successful
   */
  clearMediaLibraryData(): Promise<boolean>;

  /**
   * Check if media library data exists in SharedPreferences
   * @returns Promise<boolean> - true if data exists
   */
  hasMediaLibraryData(): Promise<boolean>;

  /**
   * Get the size of stored JSON data in bytes
   * @returns Promise<number> - Size in bytes
   */
  getDataSize(): Promise<number>;
}

const { SharedPreferencesModule } = NativeModules;

export default SharedPreferencesModule as SharedPreferencesModuleInterface;

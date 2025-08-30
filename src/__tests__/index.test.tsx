import AndroidAuto from '../index';

describe('AndroidAuto', () => {
  it('should export AndroidAuto module', () => {
    expect(AndroidAuto).toBeDefined();
    expect(typeof AndroidAuto.initializeMediaLibrary).toBe('function');
    expect(typeof AndroidAuto.playMedia).toBe('function');
    expect(typeof AndroidAuto.pause).toBe('function');
    expect(typeof AndroidAuto.resume).toBe('function');
    expect(typeof AndroidAuto.stop).toBe('function');
    expect(typeof AndroidAuto.seekTo).toBe('function');
    expect(typeof AndroidAuto.getPlaybackState).toBe('function');
    expect(typeof AndroidAuto.setPlaybackSpeed).toBe('function');
    expect(typeof AndroidAuto.setLayoutType).toBe('function');
    expect(typeof AndroidAuto.updateMediaLibrary).toBe('function');
    expect(typeof AndroidAuto.addEventListener).toBe('function');
    expect(typeof AndroidAuto.removeEventListener).toBe('function');
    expect(typeof AndroidAuto.removeAllListeners).toBe('function');
  });
});

# AndroidX Car App Dependency Conflict Resolution

## Problem Description

When installing `react-native-sportscar`, users may encounter the following error:

```
Error: Attribute meta-data#androidx.car.app.CarAppMetadataHolderService.CAR_HARDWARE_MANAGER@value 
value=(androidx.car.app.hardware.ProjectedCarHardwareManager) from [androidx.car.app:app-projected:1.4.0] 
is also present at [androidx.car.app:app-automotive:1.4.0] 
value=(androidx.car.app.hardware.AutomotiveCarHardwareManager).
```

This occurs because:
1. `react-native-sportscar` includes `androidx.car.app:app:1.4.0` dependency
2. User's app may have `androidx.car.app:app-automotive:1.4.0` dependency
3. Both dependencies provide the same meta-data element with different values

## Solutions

### Solution 1: Add Tools Namespace and Conflict Resolution (Recommended)

Update your `android/app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
    
    <!-- ... existing permissions and features ... -->

    <application>
        <!-- ... existing application content ... -->
        
        <!-- Handle AndroidX Car App dependency conflicts -->
        <meta-data
            android:name="androidx.car.app.CarAppMetadataHolderService.CAR_HARDWARE_MANAGER"
            android:value="androidx.car.app.hardware.ProjectedCarHardwareManager"
            tools:replace="android:value" />
    </application>
</manifest>
```

### Solution 2: Remove Conflicting Dependency

If you don't need the `app-automotive` dependency, remove it from your `android/app/build.gradle`:

```gradle
dependencies {
    // Remove this line if not needed:
    // implementation "androidx.car.app:app-automotive:1.4.0"
    
    // Keep other dependencies...
}
```

### Solution 3: Exclude Conflicting Dependencies

If you need both dependencies but want to exclude the conflicting meta-data:

```gradle
dependencies {
    implementation("androidx.car.app:app-automotive:1.4.0") {
        exclude group: 'androidx.car.app', module: 'app-projected'
    }
}
```

## Why This Happens

The `react-native-sportscar` module uses `MediaBrowserServiceCompat` for Android Auto integration, which requires the `androidx.car.app:app` dependency. This dependency includes the `ProjectedCarHardwareManager` which is designed for Android Auto projection.

If your app also includes `androidx.car.app:app-automotive`, it provides `AutomotiveCarHardwareManager` for embedded Android Automotive OS. Both try to register the same meta-data element, causing the conflict.

## Verification

After applying the fix, verify by:

1. Clean your project:
   ```bash
   cd android && ./gradlew clean
   ```

2. Rebuild:
   ```bash
   npx react-native run-android
   ```

3. Check that the build completes without the conflict error.

## Additional Notes

- The `tools:replace="android:value"` directive tells the Android build system to use our specified value instead of the conflicting one
- This solution maintains compatibility with both Android Auto and Android Automotive OS
- The `ProjectedCarHardwareManager` is the correct choice for Android Auto integration
- If you're building for Android Automotive OS specifically, you may need to adjust the approach

## Support

If you continue to experience issues after applying these solutions, please:

1. Check that you've applied the correct solution for your use case
2. Ensure all build files are properly cleaned and rebuilt
3. Create an issue in the repository with your specific error details

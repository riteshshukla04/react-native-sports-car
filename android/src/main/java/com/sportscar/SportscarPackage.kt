package com.sportscar

import com.facebook.react.TurboReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider
import java.util.HashMap

/**
 * TurboReactPackage for React Native's New Architecture
 * This library only supports the New Architecture (TurboModules)
 */
class SportscarPackage : TurboReactPackage() {
  
  override fun getModule(name: String, reactContext: ReactApplicationContext): NativeModule? {
    return when (name) {
      AndroidAutoTurboModule.NAME -> AndroidAutoTurboModule(reactContext)
      else -> null
    }
  }

  override fun getReactModuleInfoProvider(): ReactModuleInfoProvider {
    return ReactModuleInfoProvider {
      val moduleInfos: MutableMap<String, ReactModuleInfo> = HashMap()
      
      // AndroidAutoModule as TurboModule
      moduleInfos[AndroidAutoTurboModule.NAME] = ReactModuleInfo(
        AndroidAutoTurboModule.NAME,
        AndroidAutoTurboModule.NAME,
        false,  // canOverrideExistingModule
        false,  // needsEagerInit
        false,  // isCxxModule
        true    // isTurboModule
      )
      
      moduleInfos
    }
  }
}

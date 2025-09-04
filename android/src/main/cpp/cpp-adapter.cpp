#include <jni.h>
#include "sportscarOnLoad.hpp"

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
  return margelo::nitro::sportscar::initialize(vm);
}

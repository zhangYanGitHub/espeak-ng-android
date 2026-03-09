
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)


# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("espeak-jni")


set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
#set(CMAKE_CXX_EXTENSIONS OFF)  # 禁用编译器特性扩展

include_directories(${CMAKE_SOURCE_DIR}/include/espeak)
include_directories(${CMAKE_SOURCE_DIR}/include/espeak-ng)
include_directories(${CMAKE_SOURCE_DIR}/include)

add_library(espeak-jni SHARED
        # List C/C++ source files with relative paths to this CMakeLists.txt.
        espeak_api.cpp
        espeak-jni.cpp
        tashkeel_api.cpp
        tashkeel_jni.cpp
)


add_library(espeak-ng SHARED IMPORTED)

set_target_properties(espeak-ng
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI}/libespeak-ng.so)




add_library(ucd SHARED IMPORTED)

set_target_properties(ucd
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI}/libucd.so)


add_library(onnxruntime SHARED IMPORTED)


set_target_properties(onnxruntime
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI}/libonnxruntime.so)


add_library(piper_phonemize SHARED IMPORTED)


set_target_properties(piper_phonemize
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI}/libpiper_phonemize.so)


#add_library(libtashkeel SHARED IMPORTED)
#
#
#set_target_properties(libtashkeel
#        PROPERTIES IMPORTED_LOCATION
#        ${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI}/libtashkeel.so)

target_link_libraries(
        espeak-jni
        log
        android
        ucd
        espeak-ng
        onnxruntime
        piper_phonemize
)


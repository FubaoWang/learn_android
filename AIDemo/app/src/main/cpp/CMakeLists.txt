cmake_minimum_required(VERSION 3.10.2)
project("wfbmodel")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fopenmp")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fopenmp")
if (DEFINED ANDROID_NDK_MAJOR AND ${ANDROID_NDK_MAJOR} GREATER 20)
    set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -static-openmp")
endif ()

aux_source_directory(. SRC_LIST)
aux_source_directory(./ocr/src OCR_SRC_LIST)


add_library( # Sets the name of the library.
        wfbmodel
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        ${SRC_LIST}
        ${OCR_SRC_LIST})

find_library(log-lib log)
find_library(android-lib android)
find_library(jnigraphics-lib jnigraphics)
find_library(vulkan-lib vulkan)

include_directories(
        ${CMAKE_SOURCE_DIR}/ocr/include
)

# opencv
include_directories(
        ${CMAKE_SOURCE_DIR}/opencv/include/
)
add_library(libopencv_java4 STATIC IMPORTED)
set_target_properties(
        libopencv_java4
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/opencv/${ANDROID_ABI}/libopencv_java4.so
)

# MNN的编译
include_directories(${CMAKE_SOURCE_DIR}/mnn/include/)
include_directories(${CMAKE_SOURCE_DIR}/mnn/include/expr/)

add_library(libMNN STATIC IMPORTED)
set_target_properties(
        libMNN
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/mnn/${ANDROID_ABI}/libMNN.so
)
add_library(libMNN_CL STATIC IMPORTED)
set_target_properties(
        libMNN_CL
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/mnn/${ANDROID_ABI}/libMNN_CL.so
)

# NCNN编译
include_directories(
        ncnnvulkan/arm64-v8a/include
        ncnnvulkan/armeabi-v7a/include
)

#add_library( ncnn STATIC IMPORTED )
#set_target_properties( # Specifies the target library.
#        ncnn
#
#        # Specifies the parameter you want to define.
#        PROPERTIES IMPORTED_LOCATION
#
#        # Provides the path to the library you want to import.
#        #        ${CMAKE_SOURCE_DIR}/ncnn/${ANDROID_ABI}/libncnn.a
#        ${CMAKE_SOURCE_DIR}/ncnn/${ANDROID_ABI}/libncnn.a)


# ncnnvulkan
add_library( ncnnvk STATIC IMPORTED )
set_target_properties( # Specifies the target library.
        ncnnvk

        # Specifies the parameter you want to define.
        PROPERTIES IMPORTED_LOCATION

        # Provides the path to the library you want to import.
        #        ${CMAKE_SOURCE_DIR}/ncnn/${ANDROID_ABI}/libncnn.a
        ${CMAKE_SOURCE_DIR}/ncnnvulkan/${ANDROID_ABI}/lib/libncnn.a)

add_library(glslang STATIC IMPORTED)
add_library(OGLCompiler STATIC IMPORTED)
add_library(OSDependent STATIC IMPORTED)
add_library(SPIRV STATIC IMPORTED)
set_target_properties(glslang PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/ncnnvulkan/${ANDROID_ABI}/lib/libglslang.a)
set_target_properties(OGLCompiler PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/ncnnvulkan/${ANDROID_ABI}/lib/libOGLCompiler.a)
set_target_properties(OSDependent PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/ncnnvulkan/${ANDROID_ABI}/lib/libOSDependent.a)
set_target_properties(SPIRV PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/ncnnvulkan/${ANDROID_ABI}/lib/libSPIRV.a)



target_link_libraries( # Specifies the target library.
        wfbmodel

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        ${vulkan-lib}
        ${android-lib}
        ${jnigraphics-lib}
        libopencv_java4
        libMNN libMNN_CL
        ncnnvk
        glslang SPIRV OGLCompiler OSDependent)







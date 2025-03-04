# This file provides debug information about the CMake build configuration
# It can be used to print out important variables during the build process

message(STATUS "========== CMake Debug Information ==========")
message(STATUS "CMAKE_BUILD_TYPE: ${CMAKE_BUILD_TYPE}")
message(STATUS "CMAKE_SYSTEM_NAME: ${CMAKE_SYSTEM_NAME}")
message(STATUS "CMAKE_SYSTEM_VERSION: ${CMAKE_SYSTEM_VERSION}")
message(STATUS "CMAKE_CXX_COMPILER_ID: ${CMAKE_CXX_COMPILER_ID}")
message(STATUS "CMAKE_CXX_COMPILER_VERSION: ${CMAKE_CXX_COMPILER_VERSION}")

message(STATUS "App Settings:")
message(STATUS "  APP_NAME: ${APP_NAME}")
message(STATUS "  APP_ICON: ${APP_ICON}")
message(STATUS "  APP_PACKAGE_NAME: ${APP_PACKAGE_NAME}")
message(STATUS "  APP_VERSION: ${APP_VERSION}")
message(STATUS "  APP_VERSION_STR: ${APP_VERSION_STR}")
message(STATUS "  APK_VERSION_CODE: ${APK_VERSION_CODE}")

if(ANDROID)
  message(STATUS "Android Settings:")
  message(STATUS "  ANDROID_PLATFORM: ${ANDROID_PLATFORM}")
  message(STATUS "  ANDROID_ABI: ${ANDROID_ABI}")
  message(STATUS "  ANDROID_NDK_PLATFORM: ${ANDROID_NDK_PLATFORM}")
  message(STATUS "  ANDROID_STL: ${ANDROID_STL}")
  message(STATUS "  ANDROID_PACKAGE_SOURCE_DIR: ${ANDROID_PACKAGE_SOURCE_DIR}")
endif()

message(STATUS "Library Info:")
message(STATUS "  QT_VERSION: ${Qt6Core_VERSION}")

# QCA debugging information
if(TARGET qca)
  get_target_property(QCA_LOCATION qca IMPORTED_LOCATION)
  message(STATUS "  QCA IMPORTED_LOCATION: ${QCA_LOCATION}")
  message(STATUS "  QCA_LIB_TYPE: ${QCA_LIB_TYPE}")
  message(STATUS "  QCA_LIBRARY: ${QCA_LIBRARY}")
else()
  message(STATUS "  QCA target not found")
endif()

# Check if QCA files exist in expected locations
if(EXISTS "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libqca.so")
  message(STATUS "  Found libqca.so")
elseif(EXISTS "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libqca.a")
  message(STATUS "  Found libqca.a")
else()
  message(STATUS "  No QCA library found in standard locations")
  # List all files in the lib directory to help diagnose the issue
  file(GLOB LIB_FILES "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/*")
  foreach(FILE ${LIB_FILES})
    message(STATUS "    Found in lib: ${FILE}")
  endforeach()
endif()

message(STATUS "  QGIS Version: ${QGIS_VERSION}")
message(STATUS "  GDAL Version: ${GDAL_VERSION}")
message(STATUS "  PROJ Version: ${PROJ_VERSION}")
message(STATUS "  ZLIB LIBRARIES: ${ZLIB_LIBRARIES}")
message(STATUS "  OPENSSL_LIBRARIES: ${OPENSSL_LIBRARIES}")

message(STATUS "Build and Install Directories:")
message(STATUS "  CMAKE_BINARY_DIR: ${CMAKE_BINARY_DIR}")
message(STATUS "  VCPKG_INSTALLED_DIR: ${VCPKG_INSTALLED_DIR}")
message(STATUS "  VCPKG_TARGET_TRIPLET: ${VCPKG_TARGET_TRIPLET}")
message(STATUS "  QCA_DIR: ${QCA_DIR}")

message(STATUS "======== End CMake Debug Information ========")

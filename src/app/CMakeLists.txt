# Find required Qt packages
find_package(Qt6QuickCompiler QUIET)
find_package(Qt6 COMPONENTS LinguistTools REQUIRED)
find_package(Qt6 COMPONENTS Core5Compat REQUIRED)

# Configure platform-specific settings
if(MSVC)
  configure_file("${CMAKE_SOURCE_DIR}/platform/windows/appicon.rc.in"
                 "${CMAKE_CURRENT_BINARY_DIR}/appicon.rc")
  set(ICON_SRC "${CMAKE_CURRENT_BINARY_DIR}/appicon.rc")
endif()

if(CMAKE_SYSTEM_NAME STREQUAL "Darwin")
  set(APP_ICON ${CMAKE_SOURCE_DIR}/platform/macos/sigpac_logo.icns)
  set_source_files_properties(${APP_ICON} PROPERTIES 
                             MACOSX_PACKAGE_LOCATION "Resources")
  list(APPEND RESOURCES ${APP_ICON})
endif()

# Include manually copied .qm files
file(GLOB QM_FILES_MANUAL ${CMAKE_SOURCE_DIR}/i18n/*.qm)

# Configure logo resource
configure_file(${CMAKE_SOURCE_DIR}/images/logo.qrc.in
               ${CMAKE_BINARY_DIR}/images/logo.qrc @ONLY)

# Define main executable creation function
function(create_executable)
  cmake_parse_arguments(exe "" "TARGET" "EXTRA_ARGS" "" ${ARGN})

  # Define source files
  set(APP_SOURCES
      main.cpp 
      ${RESOURCES} 
      ${CMAKE_SOURCE_DIR}/images/images.qrc
      ${CMAKE_BINARY_DIR}/images/logo.qrc
      ${CMAKE_SOURCE_DIR}/resources/resources.qrc 
      ${ICON_SRC})
  if(ANDROID)
    configure_file(
     "${CMAKE_CURRENT_SOURCE_DIR}/android-deployment-settings.json.in"
     "${CMAKE_BINARY_DIR}/src/app/android-sigpac-go-deployment-settings.json"
     @ONLY
	)
  endif()

  if(ANDROID)
    qt_add_executable(sigpac-go ${APP_SOURCES})
    
    # Android-specific configurations
    set_property(TARGET sigpac-go PROPERTY QT_ANDROID_PACKAGE_SOURCE_DIR
                                         ${ANDROID_PACKAGE_SOURCE_DIR})
    set_property(TARGET sigpac-go PROPERTY QT_QML_IMPORT_PATH
                                         $ENV{Qt6_DIR}/qml)
    set_property(TARGET sigpac-go PROPERTY QT_QML_ROOT_PATH
                                         ${CMAKE_SOURCE_DIR}/src/qml)

    # Generate Android deployment settings
    qt_android_generate_deployment_settings(sigpac-go)
    set_target_properties(sigpac-go PROPERTIES
        QT_ANDROID_DEPLOYMENT_SETTINGS_FILE ${CMAKE_CURRENT_BINARY_DIR}/android-sigpac-go-deployment-settings.json
    )

    set(PROJECT_NAME ${exe_TARGET})
  else()
    add_executable(${exe_TARGET} ${exe_EXTRA_ARGS} ${APP_SOURCES})
  endif()

  # Translation files
  file(GLOB TS_FILES ${CMAKE_SOURCE_DIR}/i18n/*.ts)
  set_source_files_properties(
    ${TS_FILES} PROPERTIES OUTPUT_LOCATION
                           "${CMAKE_BINARY_DIR}/i18n${exe_TARGET}")
  qt_add_translation(QM_FILES ${TS_FILES})

  # Add translations as resources
  qt_add_resources(
    ${exe_TARGET}
    "translations${exe_TARGET}"
    PREFIX
    "/i18n"
    BASE
    "${CMAKE_BINARY_DIR}/i18n${exe_TARGET}"
    FILES
    ${QM_FILES})

  # Link required libraries
  target_link_libraries(${exe_TARGET} PRIVATE qfield_core qfield_qml)

  if(ANDROID)
    target_link_libraries(${exe_TARGET} PRIVATE sigpac-go_service)
  endif()

  if(WITH_SENTRY)
    target_link_libraries(${exe_TARGET} PRIVATE qfield_sentry)
  endif()

  # Handle static builds
  get_target_property(QT_TARGET_TYPE Qt::Core TYPE)
  if(${QT_TARGET_TYPE} STREQUAL "STATIC_LIBRARY")
    find_package(Qt6 COMPONENTS QmlImportScanner)

    get_target_property(qml_files qfield_qml QML_FILES)
    file(COPY ${qml_files}
         DESTINATION "${CMAKE_CURRENT_BINARY_DIR}/qmldrop/app")

    # Remove optional feature files if not enabled
    if(NOT WITH_BLUETOOTH)
      file(REMOVE
           "${CMAKE_CURRENT_BINARY_DIR}/qmldrop/app/BluetoothDeviceChooser.qml")
    endif()
    if(NOT WITH_SERIALPORT)
      file(REMOVE
           "${CMAKE_CURRENT_BINARY_DIR}/qmldrop/app/SerialPortChooser.qml")
    endif()

    qt_import_qml_plugins(${exe_TARGET} PATH_TO_SCAN
                          "${CMAKE_CURRENT_BINARY_DIR}/qmldrop/app")
  endif()

  # Link additional Qt modules and plugins
  target_link_libraries(${exe_TARGET} PUBLIC Qt::Core5Compat)
  qt_import_plugins(${exe_TARGET} INCLUDE Qt::QSvgPlugin)
  qt_import_plugins(${exe_TARGET} INCLUDE Qt::Core5Compat)

  # Platform-specific configurations
  if(IOS OR CMAKE_SYSTEM_NAME STREQUAL "Darwin")
    target_link_libraries(
      ${exe_TARGET}
      PUBLIC Qt6::QDarwinCameraPermissionPlugin
             Qt6::QDarwinMicrophonePermissionPlugin
             Qt6::QDarwinLocationPermissionPlugin)
    qt_import_plugins(
      ${exe_TARGET} INCLUDE Qt6::QDarwinCameraPermissionPlugin
      Qt6::QDarwinMicrophonePermissionPlugin
      Qt6::QDarwinLocationPermissionPlugin)
  endif()

  if(WITH_VCPKG)
    target_compile_definitions(${exe_TARGET} PUBLIC HAVE_STATIC_QCA_PLUGINS)
  endif()

  # Set common target properties
  set_target_properties(${exe_TARGET} PROPERTIES AUTORCC TRUE)

  # macOS specific settings
  if(CMAKE_SYSTEM_NAME STREQUAL "Darwin")
    qt_finalize_executable(sigpac-go)

    set_target_properties(
      ${exe_TARGET}
      PROPERTIES 
        MACOSX_BUNDLE_BUNDLE_NAME "${PROJECT_NAME}"
        MACOSX_BUNDLE_INFO_STRING "${PROJECT_NAME} SIGPAC"
        MACOSX_BUNDLE_ICON_FILE "sigpac_logo.icns"
        MACOSX_BUNDLE_GUI_IDENTIFIER "${PROJECT_NAME}"
        MACOSX_BUNDLE_LONG_VERSION_STRING "${PROJECT_VERSION}"
        MACOSX_BUNDLE_SHORT_VERSION_STRING
        "${PROJECT_VERSION_MAJOR}.${PROJECT_VERSION_MINOR}"
        MACOSX_BUNDLE_BUNDLE_VERSION "${PROJECT_VERSION}"
        MACOSX_BUNDLE_COPYRIGHT "Copyright (c) SIGPAC")
  endif()
endfunction()

# Create the main executable
if(CMAKE_SYSTEM_NAME STREQUAL "Darwin")
  set(EXTRA_ARGS MACOSX_BUNDLE)
endif()
create_executable(TARGET sigpac-go EXTRA_ARGS ${EXTRA_ARGS})

# Add test executable if enabled
if(WITH_SPIX)
  create_executable(TARGET sigpac_spix)
  target_link_libraries(sigpac_spix PRIVATE Spix::Spix)
  target_compile_definitions(sigpac_spix PUBLIC WITH_SPIX)
endif()

# Installation configuration
install(
  TARGETS sigpac-go
  BUNDLE DESTINATION ${SIGPAC_BIN_DIR}
  RUNTIME DESTINATION ${SIGPAC_BIN_DIR}
  LIBRARY DESTINATION ${SIGPAC_LIB_DIR}
  ARCHIVE DESTINATION ${SIGPAC_LIB_DIR}
  FRAMEWORK DESTINATION ${SIGPAC_FW_SUBDIR}
  PUBLIC_HEADER DESTINATION ${SIGPAC_INCLUDE_DIR})

# iOS specific configuration
if(IOS)
  include("${CMAKE_SOURCE_DIR}/cmake/AddQtIosApp.cmake")
  add_qt_ios_app(
    sigpac-go
    NAME "SIGPAC-Go"
    VERSION "${APK_VERSION_CODE}"
    SHORT_VERSION "${APK_VERSION_CODE}"
    LONG_VERSION "${APK_VERSION_CODE}"
    ASSET_DIR "${PROJECT_SOURCE_DIR}/platform/ios/Assets.xcassets"
    CATALOG_APPICON "AppIcon${APP_ICON_SUFFIX}"
    LAUNCHSCREEN_STORYBOARD
      "${PROJECT_SOURCE_DIR}/platform/ios/LaunchScreen.storyboard"
    RESOURCE_FILES "${SHARE_DIR}"
    CUSTOM_PLIST "${PROJECT_SOURCE_DIR}/platform/ios/Info.plist.in"
    BUNDLE_IDENTIFIER "${IOS_APP_IDENTIFIER}"
    ORIENTATION_PORTRAIT
    ORIENTATION_PORTRAIT_UPDOWN
    ORIENTATION_LANDSCAPE_LEFT
    ORIENTATION_LANDSCAPE_RIGHT
    REQUIRES_FULL_SCREEN
    HIDDEN_STATUS_BAR
    IPA
    UPLOAD_SYMBOL
    VERBOSE)
endif()
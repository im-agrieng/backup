# Find required Qt packages
find_package(Qt6 COMPONENTS Core Gui Qml Quick Widgets LinguistTools Core5Compat REQUIRED)
find_package(Qt6QuickCompiler QUIET)

if(POLICY QTP0002)
    qt_policy(SET QTP0002 NEW)
endif()

# Find OpenSSL and ZLIB for crypto support
find_package(ZLIB REQUIRED)
find_package(OpenSSL REQUIRED)

# Configure QCA paths and find package
if(WITH_VCPKG)
    # Set QCA paths for vcpkg
    set(QCA_DIR "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/cmake/Qca")
    set(CMAKE_PREFIX_PATH ${CMAKE_PREFIX_PATH} ${QCA_DIR})
    find_package(Qca REQUIRED)
    message(STATUS "Found Qca: ${QCA_DIR}")
    
    # Check if QCA is available as shared or static library
    if(EXISTS "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libqca.so")
        message(STATUS "QCA Libraries: ${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libqca.so")
        set(QCA_LIB_TYPE "SHARED")
        set(QCA_LIBRARY "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libqca.so")
    elseif(EXISTS "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libqca.a")
        message(STATUS "QCA Libraries: ${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libqca.a")
        set(QCA_LIB_TYPE "STATIC")
        set(QCA_LIBRARY "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libqca.a")
    else()
        message(FATAL_ERROR "QCA library not found at ${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/. 
        Please make sure QCA is properly installed for ${VCPKG_TARGET_TRIPLET} by running:
        vcpkg install qca:${VCPKG_TARGET_TRIPLET} --features=ossl --buildtype=release --options=qt-shared-libs=ON")
    endif()
endif()

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

add_executable(sigpac-go ${APP_SOURCES}) # Define the executable
target_link_libraries(sigpac-go  Qt::Core Qt::Gui) # Link required libraries

# Set the correct application binary name for Android
set(QT_ANDROID_APPLICATION_BINARY "sigpac-go")

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

    # Configure Android deployment settings
    if(ANDROID)
        configure_file(
            "${CMAKE_CURRENT_SOURCE_DIR}/android-deployment-settings.json.in"
            "${CMAKE_BINARY_DIR}/src/app/android-sigpac-go-deployment-settings.json"
            @ONLY
        )
    endif()

    # Create executable differently based on platform
    if(ANDROID)
        qt_add_executable(${exe_TARGET} ${APP_SOURCES})
        
        # Android-specific configurations
        set_property(TARGET ${exe_TARGET} PROPERTY QT_ANDROID_PACKAGE_SOURCE_DIR
                                         ${ANDROID_PACKAGE_SOURCE_DIR})
        set_property(TARGET ${exe_TARGET} PROPERTY QT_QML_IMPORT_PATH
                                         $ENV{Qt6_DIR}/qml)
        set_property(TARGET ${exe_TARGET} PROPERTY QT_QML_ROOT_PATH
                                         ${CMAKE_SOURCE_DIR}/src/qml)

        if(WITH_VCPKG)
            # QCA configuration for Android
            target_include_directories(${exe_TARGET} PRIVATE 
                ${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/include)
            
            # Link with appropriate library (static or shared)
            if(QCA_LIB_TYPE STREQUAL "SHARED")
                target_link_libraries(${exe_TARGET} PRIVATE 
                    ${QCA_LIBRARY}
                    ${OPENSSL_CRYPTO_LIBRARY}
                    ${OPENSSL_SSL_LIBRARY}
                    ${ZLIB_LIBRARIES})
                    
                # Add QCA plugins path for Android
                if(EXISTS "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/plugins/crypto")
                    set_property(TARGET ${exe_TARGET} APPEND PROPERTY QT_ANDROID_EXTRA_PLUGINS 
                                "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/plugins")
                endif()
                
                # Add all required libraries to Android extras
                set_property(TARGET ${exe_TARGET} APPEND PROPERTY QT_ANDROID_EXTRA_LIBS
                            "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libqca.so"
                            "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libcrypto.so"
                            "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libssl.so"
                            "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libz.so")
            else()
                # Static library configuration
                target_link_libraries(${exe_TARGET} PRIVATE 
                    ${QCA_LIBRARY}
                    ${OPENSSL_CRYPTO_LIBRARY}
                    ${OPENSSL_SSL_LIBRARY}
                    ${ZLIB_LIBRARIES})
                
                # For static builds, we need to define this macro
                target_compile_definitions(${exe_TARGET} PRIVATE HAVE_STATIC_QCA_PLUGINS)
                
                # Add just the dependencies to Android extras (no QCA since it's static)
                set_property(TARGET ${exe_TARGET} APPEND PROPERTY QT_ANDROID_EXTRA_LIBS
                            "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libcrypto.so"
                            "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libssl.so"
                            "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libz.so")
            endif()
        endif()
        
        set_target_properties(${exe_TARGET} PROPERTIES
            QT_ANDROID_DEPLOYMENT_SETTINGS_FILE ${CMAKE_BINARY_DIR}/src/app/android-sigpac-go-deployment-settings.json
        )
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
    target_link_libraries(${exe_TARGET} 
        PRIVATE 
        qfield_core 
        qfield_qml
        Qt::Core
        Qt::Gui
        Qt::Qml
        Qt::Quick
        Qt::Widgets
        Qt::Core5Compat
    )

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

    # Link additional Qt plugins
    qt_import_plugins(${exe_TARGET} INCLUDE Qt::QSvgPlugin)
endfunction()  # Added missing endfunction() statement here

# Call the function to create the main executable (this part might be missing in your file)

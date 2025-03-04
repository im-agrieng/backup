# Find required Qt packages
find_package(Qt6 COMPONENTS Core Gui Qml Quick Widgets LinguistTools Core5Compat REQUIRED)
find_package(Qt6QuickCompiler QUIET)

# Fix Qt policy issue with Android paths
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
    
    # Try to find QCA but don't report errors yet
    find_package(Qca QUIET)
    
    if(Qca_FOUND)
        message(STATUS "Found Qca: ${QCA_DIR}")
        
        # Check if QCA is available as shared or static library
        if(EXISTS "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libqca.so.2.3.9" OR 
           EXISTS "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libqca.so.2.3" OR
           EXISTS "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libqca.so.2" OR
           EXISTS "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libqca.so")
            # Find the actual library file
            file(GLOB QCA_LIBRARY_FILES "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libqca.so*")
            list(GET QCA_LIBRARY_FILES 0 QCA_LIBRARY)
            message(STATUS "QCA Libraries (shared): ${QCA_LIBRARY}")
            set(QCA_LIB_TYPE "SHARED")
        elseif(EXISTS "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libqca.a")
            message(STATUS "QCA Libraries (static): ${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libqca.a")
            set(QCA_LIB_TYPE "STATIC")
            set(QCA_LIBRARY "${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/lib/libqca.a")
        elseif(TARGET qca)
            # Try to find through Qca target properties
            get_target_property(QCA_LIBRARY_LOCATION qca IMPORTED_LOCATION)
            if(QCA_LIBRARY_LOCATION)
                message(STATUS "QCA Library found through target properties: ${QCA_LIBRARY_LOCATION}")
                set(QCA_LIBRARY "${QCA_LIBRARY_LOCATION}")
                if(QCA_LIBRARY_LOCATION MATCHES ".*\\.a$")
                    set(QCA_LIB_TYPE "STATIC")
                else()
                    set(QCA_LIB_TYPE "SHARED")
                endif()
            else()
                message(STATUS "QCA target exists but location not found, assuming static library")
                set(QCA_LIB_TYPE "STATIC")
            endif()
        else
            message(STATUS "QCA package found but no library detected, proceeding without QCA support")
            set(QCA_FOUND FALSE)
        endif()
    else()
        message(STATUS "QCA not found, proceeding without QCA support")
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

# Set the correct application binary name for Android
set(QT_ANDROID_APPLICATION_BINARY "sigpacgo")

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
            "${CMAKE_BINARY_DIR}/src/app/android-sigpacgo-deployment-settings.json"
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

        if(WITH_VCPKG AND Qca_FOUND)
            # QCA configuration for Android
            target_include_directories(${exe_TARGET} PRIVATE 
                ${VCPKG_INSTALLED_DIR}/${VCPKG_TARGET_TRIPLET}/include)
            
            # Link with appropriate library (static or shared)
            if(QCA_LIB_TYPE STREQUAL "SHARED" AND QCA_LIBRARY)
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
                
                # Add all required libraries to Android extras - use the actual found library path
                if(QCA_LIBRARY)
                    set_property(TARGET ${exe_TARGET} APPEND PROPERTY QT_ANDROID_EXTRA_LIBS
                                "${QCA_LIBRARY}")
                endif()
                # Add OpenSSL and zlib dependencies
                set_property(TARGET ${exe_TARGET} APPEND PROPERTY QT_ANDROID_EXTRA_LIBS
                            "${OPENSSL_CRYPTO_LIBRARY}"
                            "${OPENSSL_SSL_LIBRARY}"
                            "${ZLIB_LIBRARIES}")
            else()
                # Static library configuration or QCA not found
                if(TARGET qca)
                    target_link_libraries(${exe_TARGET} PRIVATE qca)
                    # For static builds, we need to define this macro
                    target_compile_definitions(${exe_TARGET} PRIVATE HAVE_STATIC_QCA_PLUGINS)
                endif()
                
                # Always link OpenSSL and zlib
                target_link_libraries(${exe_TARGET} PRIVATE 
                    ${OPENSSL_CRYPTO_LIBRARY}
                    ${OPENSSL_SSL_LIBRARY}
                    ${ZLIB_LIBRARIES})
                
                # Add dependencies to Android extras
                set_property(TARGET ${exe_TARGET} APPEND PROPERTY QT_ANDROID_EXTRA_LIBS
                            "${OPENSSL_CRYPTO_LIBRARY}"
                            "${OPENSSL_SSL_LIBRARY}"
                            "${ZLIB_LIBRARIES}")
            endif()
        else()
            # If no QCA, just link the base OpenSSL and ZLIB libraries
            target_link_libraries(${exe_TARGET} PRIVATE 
                ${OPENSSL_CRYPTO_LIBRARY}
                ${OPENSSL_SSL_LIBRARY}
                ${ZLIB_LIBRARIES})
                
            # Add dependencies to Android extras
            set_property(TARGET ${exe_TARGET} APPEND PROPERTY QT_ANDROID_EXTRA_LIBS
                        "${OPENSSL_CRYPTO_LIBRARY}"
                        "${OPENSSL_SSL_LIBRARY}"
                        "${ZLIB_LIBRARIES}")
        endif()
        
        set_property(TARGET ${exe_TARGET} PROPERTIES
            QT_ANDROID_DEPLOYMENT_SETTINGS_FILE ${CMAKE_BINARY_DIR}/src/app/android-sigpacgo-deployment-settings.json
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
        target_link_libraries(${exe_TARGET} PRIVATE qfield_service)
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
endfunction()

# Call the function to create the main executable
create_executable(TARGET sigpacgo)

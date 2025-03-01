set(QFIELD_SERVICE_SRCS qfieldcloudservice.cpp qfieldpositioningservice.cpp)
set(QFIELD_SERVICE_HDRS qfieldcloudservice.h qfieldpositioningservice.h)

#installation directories
set(SIGPAC_BIN_DIR "${QFIELD_BIN_DIR}" CACHE PATH "Binary installation directory")
set(SIGPAC_LIB_DIR "${QFIELD_LIB_DIR}" CACHE PATH "Library installation directory")
set(SIGPAC_INCLUDE_DIR "${QFIELD_INCLUDE_DIR}" CACHE PATH "Include installation directory")
set(SIGPAC_FW_SUBDIR "${QFIELD_FW_SUBDIR}" CACHE PATH "Framework installation subdirectory")

# Add library with new name but maintain QField compatibility
add_library(sigpac-go_service STATIC ${QFIELD_SERVICE_SRCS} ${QFIELD_SERVICE_HDRS})

# Generate export header with QField naming to maintain compatibility with existing includes
include(GenerateExportHeader)
generate_export_header(sigpac-go_service
    BASE_NAME qfield_service
    EXPORT_FILE_NAME ${CMAKE_CURRENT_BINARY_DIR}/qfield_service_export.h)

# Make sure both build and source directories are in include path
target_include_directories(sigpac-go_service SYSTEM
                         PUBLIC ${CMAKE_CURRENT_BINARY_DIR})

target_include_directories(sigpac-go_service
                         PUBLIC ${CMAKE_SOURCE_DIR}/src/service)

target_compile_features(sigpac-go_service PUBLIC cxx_std_17)

set_target_properties(sigpac-go_service PROPERTIES 
    AUTOMOC TRUE
    OUTPUT_NAME "sigpac-go_service"  # Ensure output name matches new naming
)

target_link_libraries(sigpac-go_service PRIVATE qfield_core ${QGIS_CORE_LIBRARY})

target_link_libraries(sigpac-go_service PUBLIC Qt::Core Qt::Positioning
                                            Qt::CorePrivate)

# Install the export header along with other headers
install(FILES 
    ${QFIELD_SERVICE_HDRS}
    ${CMAKE_CURRENT_BINARY_DIR}/qfield_service_export.h
    DESTINATION ${SIGPAC_INCLUDE_DIR}
)

install(
  TARGETS sigpac-go_service
  BUNDLE DESTINATION ${SIGPAC_BIN_DIR}
  RUNTIME DESTINATION ${SIGPAC_BIN_DIR}
  LIBRARY DESTINATION ${SIGPAC_LIB_DIR}
  ARCHIVE DESTINATION ${SIGPAC_LIB_DIR}
  FRAMEWORK DESTINATION ${SIGPAC_FW_SUBDIR}
  PUBLIC_HEADER DESTINATION ${SIGPAC_INCLUDE_DIR})

# Configure and install package config
configure_file(
    "${CMAKE_CURRENT_SOURCE_DIR}/sigpac-go-config.cmake.in"
    "${CMAKE_CURRENT_BINARY_DIR}/sigpac-go-config.cmake"
    @ONLY
)

install(
    FILES "${CMAKE_CURRENT_BINARY_DIR}/sigpac-go-config.cmake"
    DESTINATION "${SIGPAC_LIB_DIR}/cmake/sigpac-go"
)
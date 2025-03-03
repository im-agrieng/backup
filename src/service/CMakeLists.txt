set(QFIELD_SERVICE_SRCS qfieldcloudservice.cpp qfieldpositioningservice.cpp)
set(QFIELD_SERVICE_HDRS qfieldcloudservice.h qfieldpositioningservice.h)

#installation directories
set(SIGPAC_BIN_DIR "${QFIELD_BIN_DIR}" CACHE PATH "Binary installation directory")
set(SIGPAC_LIB_DIR "${QFIELD_LIB_DIR}" CACHE PATH "Library installation directory")
set(SIGPAC_INCLUDE_DIR "${QFIELD_INCLUDE_DIR}" CACHE PATH "Include installation directory")
set(SIGPAC_FW_SUBDIR "${QFIELD_FW_SUBDIR}" CACHE PATH "Framework installation subdirectory")

#library with new name
add_library(qfield_service STATIC ${QFIELD_SERVICE_SRCS} ${QFIELD_SERVICE_HDRS})

# Generate both export headers to maintain compatibility during transition
include(GenerateExportHeader)
generate_export_header(qfield_service
    BASE_NAME qfield_service  # Changed to qfield_service
    EXPORT_FILE_NAME ${CMAKE_CURRENT_BINARY_DIR}/qfield_service_export.h)  # Changed to qfield_service_export.h

# There is no need to create a compatibility symlink or copy as the original name is used
# configure_file(
#    ${CMAKE_CURRENT_BINARY_DIR}/sigpac-go_service_export.h
#    ${CMAKE_CURRENT_BINARY_DIR}/qfield_service_export.h
#    COPYONLY
# )

target_include_directories(qfield_service SYSTEM
                         PUBLIC 
                         ${CMAKE_CURRENT_BINARY_DIR}
                         ${CMAKE_CURRENT_SOURCE_DIR})

target_include_directories(qfield_service
                         PUBLIC ${CMAKE_SOURCE_DIR}/src/service)

target_compile_features(qfield_service PUBLIC cxx_std_17)

set_target_properties(qfield_service PROPERTIES 
    AUTOMOC TRUE
    OUTPUT_NAME "qfield_service"
)

target_link_libraries(qfield_service PRIVATE qfield_core ${QGIS_CORE_LIBRARY})

target_link_libraries(qfield_service PUBLIC Qt::Core Qt::Positioning
                                            Qt::CorePrivate)

# Install both export headers
install(FILES 
    ${QFIELD_SERVICE_HDRS}
    ${CMAKE_CURRENT_BINARY_DIR}/qfield_service_export.h # Only install qfield_service_export.h
    DESTINATION ${SIGPAC_INCLUDE_DIR}
)

install(
  TARGETS qfield_service
  BUNDLE DESTINATION ${SIGPAC_BIN_DIR}
  RUNTIME DESTINATION ${SIGPAC_BIN_DIR}
  LIBRARY DESTINATION ${SIGPAC_LIB_DIR}
  ARCHIVE DESTINATION ${SIGPAC_LIB_DIR}
  FRAMEWORK DESTINATION ${SIGPAC_FW_SUBDIR}
  PUBLIC_HEADER DESTINATION ${SIGPAC_INCLUDE_DIR})

configure_file(
    "${CMAKE_CURRENT_SOURCE_DIR}/sigpac-go-config.cmake.in"
    "${CMAKE_CURRENT_BINARY_DIR}/sigpac-go-config.cmake"
    @ONLY
)

install(
    FILES "${CMAKE_CURRENT_BINARY_DIR}/sigpac-go-config.cmake"
    DESTINATION "${SIGPAC_LIB_DIR}/cmake/sigpac-go"
)
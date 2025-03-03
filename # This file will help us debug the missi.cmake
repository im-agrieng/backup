# This file will help us debug the missing parenthesis issue
# Add this to your project and include it at the end of CMakeLists.txt

# Print line numbers for the main CMakeLists.txt file
file(READ "${CMAKE_SOURCE_DIR}/CMakeLists.txt" CMAKE_CONTENT)
string(REGEX REPLACE "\n" ";" CMAKE_LINES "${CMAKE_CONTENT}")

message(STATUS "CMakeLists.txt content line by line:")
set(LINE_NUMBER 1)
foreach(LINE ${CMAKE_LINES})
  # Check for unbalanced parentheses on this line
  string(REGEX MATCHALL "\\(" OPEN_PARENS "${LINE}")
  string(REGEX MATCHALL "\\)" CLOSE_PARENS "${LINE}")
  list(LENGTH OPEN_PARENS OPEN_COUNT)
  list(LENGTH CLOSE_PARENS CLOSE_COUNT)
  
  # If there's an imbalance, print the line
  if(NOT ${OPEN_COUNT} EQUAL ${CLOSE_COUNT})
    message(STATUS "Line ${LINE_NUMBER}: ${LINE} (Parentheses: ${OPEN_COUNT} open, ${CLOSE_COUNT} close)")
  endif()
  
  math(EXPR LINE_NUMBER "${LINE_NUMBER} + 1")
endforeach()

message(STATUS "End of file reached. Make sure all functions are closed properly.")

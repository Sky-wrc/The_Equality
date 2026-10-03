cmake_minimum_required(VERSION 3.22.1)
project(equality C)

set(SOLVERS_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../c)

add_library(equality SHARED
        equality_jni.c
        ${SOLVERS_DIR}/solving_scripts/linear.c
        ${SOLVERS_DIR}/solving_scripts/quadratic.c
        ${SOLVERS_DIR}/tools/mymath.c)

target_include_directories(equality PRIVATE ${SOLVERS_DIR}/include)
target_link_libraries(equality m)

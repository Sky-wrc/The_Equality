#include <jni.h>

#include "linear.h"
#include "quadratic.h"

JNIEXPORT jdoubleArray JNICALL
Java_com_skywrc_am_equality_NativeSolver_linear(JNIEnv *env, jclass clazz,
                                                jdouble a, jdouble b, jchar domain)
{
    double n1 = 0.0;
    double n2 = 0.0;
    linear_solver(&n1, &n2, a, b, (char) domain);

    jdouble out[2] = { n1, n2 };
    jdoubleArray result = (*env)->NewDoubleArray(env, 2);
    if (result != NULL) {
        (*env)->SetDoubleArrayRegion(env, result, 0, 2, out);
    }
    return result;
}

JNIEXPORT jdoubleArray JNICALL
Java_com_skywrc_am_equality_NativeSolver_quadratic(JNIEnv *env, jclass clazz,
                                                   jdouble a, jdouble b, jdouble c,
                                                   jchar domain)
{
    /* the complex branch of quadratic_solver writes n1[1] and n2[1] */
    double n1[2] = { 0.0, 0.0 };
    double n2[2] = { 0.0, 0.0 };
    double n3 = 0.0;
    quadratic_solver(n1, n2, &n3, a, b, c, (char) domain);

    jdouble out[5] = { n1[0], n1[1], n2[0], n2[1], n3 };
    jdoubleArray result = (*env)->NewDoubleArray(env, 5);
    if (result != NULL) {
        (*env)->SetDoubleArrayRegion(env, result, 0, 5, out);
    }
    return result;
}

#ifndef QUADRATIC_H
#define QUADRATIC_H
#include <stdbool.h>
void quadratic_solver(double* n1, double* n2, double* n3, double a, double b, double c, char M, bool Rt);
double discriminant(double a, double b, double c, char M);
#endif
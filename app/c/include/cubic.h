#ifndef CUBIC_H
#define CUBIC_H
/*
 * Roots go to x1, x2, x3 as {re, im}; *count says how many of them are filled:
 *   0..3  roots in the domain M, sorted by re, a multiple root is written once
 *         (in C with D < 0: x1 is the real root, x2 and x3 the complex pair)
 *   -1    a == 0, not a cubic equation
 *   -2    unknown domain
 * *D = 18abcd - 4b^3d + b^2c^2 - 4ac^3 - 27a^2d^2
 *   D > 0: three different real roots, D == 0: a multiple root, D < 0: one real root and a complex pair
 */
void cubic_solver(double* x1, double* x2, double* x3, double* D, int* count,
                  double a, double b, double c, double d, char M);
double cubic_discriminant(double a, double b, double c, double d);
#endif

#include <math.h>
#include "../include/mymath.h"
#include "../include/cubic.h"

//67=C 78=N 82=R 90=Z
#define CUBIC_EPS 1e-9

double cubic_discriminant(double a, double b, double c, double d)
{
    return 18*a*b*c*d - 4*b*b*b*d + b*b*c*c - 4*a*c*c*c - 27*a*a*d*d;
}

static double cubic_value(double a, double b, double c, double d, double x)
{
    return ((a*x + b)*x + c)*x + d; // Horner
}

// one Newton step removes most of the rounding error of cbrt/cos
static double polish(double a, double b, double c, double d, double x)
{
    double df = (3*a*x + 2*b)*x + c;
    if (df != 0)
        x -= cubic_value(a, b, c, d, x) / df;
    return x;
}

static void sort_roots(double* r, int n)
{
    for (int i = 1; i < n; i++)
        for (int j = i; j > 0 && r[j-1] > r[j]; j--) {
            double tm = r[j];
            r[j] = r[j-1];
            r[j-1] = tm;
        }
}

void cubic_solver(double* x1, double* x2, double* x3, double* D, int* count,
                  double a, double b, double c, double d, char M)
{
    double* out[3] = { x1, x2, x3 };
    double r[3];                 // distinct real roots
    int n = 0;
    double re_c = 0, im_c = 0;   // complex pair re_c ± i*im_c when D < 0

    for (int i = 0; i < 3; i++)
        out[i][0] = out[i][1] = 0;
    *D = 0;
    *count = 0;

    if (a == 0) {                // not a cubic equation
        *count = -1;
        return;
    }
    if (M != 'C' && M != 'N' && M != 'R' && M != 'Z') {
        *count = -2;
        return;
    }

    // x = t + shift turns the equation into t^3 + p*t + q = 0
    double shift = -b / (3*a);
    double p = (3*a*c - b*b) / (3*a*a);
    double q = (2*b*b*b - 9*a*b*c + 27*a*a*d) / (27*a*a*a);
    *D = cubic_discriminant(a, b, c, d);

    if (*D > 0 && p < 0) {       // three different real roots (trigonometric form)
        double pi = acos(-1.0);
        double m = 2*sqrt(-p/3);
        double arg = 3*q / (p*m);
        if (arg > 1) arg = 1;
        if (arg < -1) arg = -1;
        double theta = acos(arg) / 3;
        for (int k = 0; k < 3; k++)
            r[k] = polish(a, b, c, d, m*cos(theta - 2*pi*k/3) + shift);
        n = 3;
    }
    else if (*D == 0) {          // multiple root
        if (3*a*c - b*b == 0) {  // p == 0, then q == 0 too: triple root
            r[0] = shift;
            n = 1;
        }
        else {                   // simple root and double root (Newton is not used on the double one)
            r[0] = polish(a, b, c, d, 3*q/p + shift);
            r[1] = -3*q/(2*p) + shift;
            n = 2;
        }
    }
    else {                       // one real root (Cardano) and a complex pair
        double Q = (q/2)*(q/2) + (p/3)*(p/3)*(p/3);
        double s = sqrt(Q > 0 ? Q : 0);
        double u = cbrt(-q/2 + s);
        double v = cbrt(-q/2 - s);
        r[0] = polish(a, b, c, d, u + v + shift);
        n = 1;
        re_c = (-b/a - r[0]) / 2; // Vieta: x1 + x2 + x3 = -b/a
        im_c = sqrt(3.0)/2 * fabs(u - v);
    }
    sort_roots(r, n);

    if (M == 'R' || M == 'C') {
        for (int i = 0; i < n; i++)
            out[i][0] = r[i];
        *count = n;
        if (M == 'C' && *D < 0) {
            x2[0] = re_c;
            x2[1] = im_c;
            x3[0] = re_c;
            x3[1] = -im_c;
            *count = 3;
        }
    }
    else {                       // Z and N: round and substitute instead of x == (int)x
        int integer_coefficients = a == floor(a) && b == floor(b) && c == floor(c) && d == floor(d);
        int found = 0;
        for (int i = 0; i < n; i++) {
            double k = round(r[i]);
            double value = cubic_value(a, b, c, d, k);
            int is_root = integer_coefficients ? value == 0 : fabs(value) < CUBIC_EPS;
            if (is_root && (M == 'Z' || k > 0) && (found == 0 || out[found-1][0] != k)) {
                out[found][0] = k;
                found++;
            }
        }
        *count = found;
    }
}

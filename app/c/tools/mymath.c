#include <stdio.h>

double mysqrt(double n)
{
    double r=n,l=0;
    double s = (l + n)/2;
    int k = 0;
    while (s*s!=n){
        k +=1;
        if (k >100)
            break;
        if  (s*s<n){
            l = s;
            s = (l + r)/2;
        }
        else {
            r = s;
            s = (l + r)/2;
        }
        // printf("%f", s);
        // printf("\n");
    }
    return s;
}

double max_num(double a, double b)
{
    double max;
    if (a >= b)
        max = a;
    else
        max = b;
    return max;
}
double min_num(double a, double b)
{
    double min;
    if (a >= b)
        min = b;
    else
        min = a;
    return min;
}


int GCD(int a, int b)
{
    if (a < b){
        int tm = a;
        a = b;
        b = tm;
    }  
    while (b > 0){
        int tm = b;
        b = a % b;
        a = tm;
    }

    return a;
}

// int main ()
// {
//     int t = 5;
//     printf("%f",mysqrt(t));
//     return 0;
// }
#include <stdio.h>
#include "../include/mymath.h"

double linear_solver(double a, double b, char M)
{   
    double result = -1;
    if(M == 67)
        return -1;
    else if (M == 78){
        result =(int)(-b) / a;
        if(result < 1){
            return -1;
        }
    }
    else if(M == 81)
        result =(-b) / a;
    else if(M==90)
        result =(int)((-b) / a);

    return result;
}

// int main()
// {
//     printf("%f", linear_solver(0.52,-2,90));

//     return 0;
// }

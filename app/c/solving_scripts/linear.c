#include <stdio.h>
#include "../include/mymath.h"

void linear_solver(double* n1, double *n2, double a, double b, char M)
{   
    if(a!=0)
        if(M == 67){
            *n1 = -1;
            *n2 = -1; //for Complex numbers
        }
        else if (M == 78){
            if((int)((-b) / a) > 0){
                *n1 = (int)((-b) / a);
                *n2 = (int)(-b) % (int)a;
            }
            else{
                *n1 = -1;
                *n2 = -2; //Annatural answer
            }
        }
        else if(M == 82){
            *n1 =(-b) / a;
            *n2 = 0; // Answer is Real
        }
            
        else if(M==90){
            *n1 = (int)((-b) / a);
            *n2 = (int)(-b) % (int)a;
        }
        else;
    else{
        *n1 = 0;
        *n2 = -3; //Devision by zero
    }
}

// int main()
// {
//     printf("%f", linear_solver(0.52,-2,90));

//     return 0;
// }

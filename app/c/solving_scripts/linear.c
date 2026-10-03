#include "../include/mymath.h"

void linear_solver(double* n1, double *n2, double a, double b, char M)
{   
    *n2 = 0;
    if(a!=0)
        if(M == 'C'){
            *n1 =(-b) / a;
            *n2 = -1; //for Complex numbers
        }
        else if (M == 'N'){
            if((int)((-b) / a) > 0){
                if(a>=1 || a<=-1){
                *n1 = (int)((-b) / a);
                *n2 = (int)(-b) % (int)a;
                }
                else{
                    *n1 = (int)((-b) / a);
                    *n2 = -4; //modullo can't be found
                }
            }
            else{
                *n1 = -1;
                *n2 = -2; //Annatural answer
            }
        }
        else if(M == 'R'){
            *n1 =(-b) / a;
            *n2 = 0; // Answer is Real
        }
            
        else if(M=='Z'){
            if(a>=1 || a<=-1){
                *n1 = (int)((-b) / a);
                *n2 = (int)(-b) % (int)a;
            }
            else{
                *n1 = (int)((-b) / a);
                *n2 = -4.1; //modullo can't be found
            }
        }
        else;
    else if(b==0 && a==0){
        *n1 = 0.1;
        *n2 = -5.1; //infinite number of roots
    }
    else{
        *n1 = 0.1;
        *n2 = -3.1; //No roots
    }
}

// int main()
// {
//     printf("%f", linear_solver(0.52,-2,90));

//     return 0;
// }

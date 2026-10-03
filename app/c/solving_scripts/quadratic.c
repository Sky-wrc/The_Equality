#include <math.h>
#include "../include/mymath.h"

// #define SIZE =10;
//67=C 78=N 82=R 90=Z
double discriminant( double a, double b, double c, char M)
{
    if(M!='C')
    {
        if((b*b - 4*a*c)<0)
            return -1;
        if(M=='N' || M=='Z' ){
            return (b*b - 4*a*c);
        }
        else
            return (b*b - 4*a*c);
    }
    else if(M=='C')
        return (b*b - 4*a*c);
    else
        return -9;
    
}

void quadratic_solver(double* n1, double* n2, double* n3, double a, double b, double c, char M)
{
    int g = 1;
    if(a!=0){
        if (a == (int)a && b == (int)b && c == (int)c){
            int a_z = (int)a,b_z = (int)b,c_z = (int)c;
            g = GCD(GCD(a,b),c);
            a_z/=g;
            b_z/=g;
            c_z/=g;
            // printf("%d %d %d", a_z, b_z, c_z);
            // return 0;
            a = (double)a_z;
            b = (double)b_z;
            c = (double)c_z;
        }
        *n3 = discriminant(a,b,c,M);
        if(M!='C')
        {
            if (*n3<0) //Discriminant<0
            {
                n1[0] = -1;
                n2[0] = -1;
                *n3 = -1;
            }
            else{
                double x1 = min_num(((-b+sqrt(*n3))/(2*a)),((-b-sqrt(*n3))/(2*a)));
                double x2 = max_num(((-b+sqrt(*n3))/(2*a)),((-b-sqrt(*n3))/(2*a)));
                if(M=='R')
                    if(*n3 == 0){ //Discriminant = 0
                    
                    n1[0] = x1;
                    n2[0] = -1; //no second root
                    //printf("%f %f %f ",*n1, *n2, *n3);
                    }
                    else if(*n3>0) //Discriminant > 0
                    {
                        n1[0] = x1;
                        n2[0] = x2;
                        *n3 = (*n3*g*g);
                        //printf("%f %f %f ",*n1, *n2, *n3);
                    }
                    else
                        ;
                else if (M=='Z')
                    if(*n3 == 0){ //Discriminant = 0
                        if(x1 == (int)x1){//no second root
                            n1[0] = (int)(x1);
                            n2[0] = -1;
                        }
                        else{// no integer roots
                            n1[0] = 0;
                            n2[0] = -5; 
                        }
                        *n3 = (int)*n3;
                    }
                    else if(*n3>0){ //Discriminant > 0
                        if(x1 == (int)x1){
                            n1[0] = (int)(x1);
                        }
                        else{//smaller root is not integer
                            n1[0] = -0.000001;
                        }
                        if(x2 == (int)x2){
                            n2[0] = (int)(x2);
                        }
                        else{//bigger root is not integer
                            n2[0] = -0.000001;
                        }
                        *n3 = (int)(*n3*g*g);
                        //printf("%f %f %f ",*n1, *n2, *n3);
                        
                    }
                    else
                        ;
                else if(M=='N')
                    //printf("%f %f %f ",*n1, *n2, *n3);
                    if(*n3 == 0){ //Discriminant = 0
                        if(x1 == (int)x1 && x1 > 0){//no second root
                            n1[0] = (int)(x1);
                            n2[0] = -1;
                        }
                        else{// no natural roots
                            n1[0] = 0;
                            n2[0] = -8; 
                        }
                        *n3 = (int)*n3;
                    }
                    else if(*n3>0){//Discriminant > 0
                        
                        if(x1 == (int)x1 && x1 > 0){
                            n1[0] = (int)(x1);
                        }
                        else{//smaller root is not natural
                            n1[0] = -3;
                        }
                        if(x2 == (int)x2 && x2 > 0){
                            n2[0] = (int)(x2);
                        }
                        else{//bigger root is not natural
                            n2[0] = -4;
                        }
                        *n3 = (int)(*n3*g*g);
                        
                    }
                    else;
                }
        }
        else if(M=='C')
        {   
            if (*n3 >=0){
                double x1 = min_num(((-b+sqrt(*n3))/(2*a)),((-b-sqrt(*n3))/(2*a)));
                double x2 = max_num(((-b+sqrt(*n3))/(2*a)),((-b-sqrt(*n3))/(2*a)));
                if(*n3==0){ // one uncoplex root
                n2[0] = -1;
                n2[1] = -1;
                n1[0] = x1;
                n1[1] = 0;
                *n3=(*n3*g*g);
                }
                else{
                    n2[0] = x2;
                    n2[1] = 0;
                    n1[0] = x1;
                    n1[1] = 0.0;
                    *n3=(*n3*g*g);
                }
            }
            else{
                n2[0] = ((b*-1)/(2*a));
                n2[1] = (-1*sqrt(*n3*-1))/(2*a);
                n1[0] = ((b*-1)/(2*a));
                n1[1] = (sqrt(*n3*-1))/(2*a);
                *n3=(*n3*g*g);
            }
            
        }
        else;
    }
    else{ //Not a quadratic equasion
        n1[0] = -2;
        n2[0] = -2;
        *n3 = -2;
    }
}

// int main()
// {   
//     //67=C 78=N 82=R 90=Z
//     //quadratic_solver(1,-2,1,81);
//     char M=67;
//     double n1[2],n2[2],n3;
//     quadratic_solver(n1, n2, &n3, 2, 0, 2,M);
//     if(M!='C')
//         if (n2[0] == -1 && n3 == 0)
//             printf("x = %f\nD = %f\n",n1[0],n3);
//         else if(n1[0] == -1 && n2[0] == -1 && n3 == -1)
//             printf("D<0 <=> No roots in Real Numbers");
//         else if(n3 >= 0)
//             printf("x1 = %f\nx2 = %f\nD = %f Scrt(D) = %f\n",n1[0],n2[0],n3,sqrt(n3));
//         else
//             printf("x1 = %f\nx2 = %f\nD = %f\n",n1[0],n2[0],n3);
//     else if(M==67)
//         if(n2[1] == -1 && n2[0] == -1 && n3 == 0)
//             printf("x = (%f,%fi)\nD = %f\n",n1[0],n1[1],n3);
//         else if(n3 < 0)
//             printf("x1 = (%f,%fi)\nx2 = (%f,%fi)\nD = %f Scrt(D) = %fi\n",n1[0],n1[1],n2[0],n2[1],n3,sqrt(-1*n3));
//         else
//             printf("x1 = (%f,%fi)\nx2 = (%f,%fi)\nD = %f Scrt(D) = %fi\n",n1[0],n1[1],n2[0],n2[1],n3,sqrt(n3));
//     else;
//     return 0;
// }
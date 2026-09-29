#include <stdio.h>
#include "../include/mymath.h"
//67=C 78=N 82=R 90=Z
double discriminant( double a, double b, double c, char M)
{
    if(M>67)
    {
        if((b*b - 4*a*c)<0)
            return -1;
        if(M==78 || M==90){
            return (int)(b*b - 4*a*c);
        }
        else
            return (b*b - 4*a*c);
        
    }
    else
        return -1;
    
}

void quadratic_solver(double* n1, double* n2, double* n3, double a, double b, double c, char M)
{
    if(a!=0){
        if (a == (int)a && b == (int)b && c == (int)c){
            int a_z = (int)a,b_z = (int)b,c_z = (int)c;
            int g = GCD(GCD(a,b),c);
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
        if(M>67)
        {
            
            if (*n3<0) //Discriminant<0
            {
                *n1 = -1;
                *n2 = -1;
                *n3 = -1;
            }
            else{
                double x1 = min_num(((-b+mysqrt(*n3))/(2*a)),((-b-mysqrt(*n3))/(2*a)));
                double x2 = max_num(((-b+mysqrt(*n3))/(2*a)),((-b-mysqrt(*n3))/(2*a)));
                if(M==82)
                    if(*n3 == 0){ //Discriminant = 0
                    
                    *n1 = x1;
                    *n2 = -1; //no second root
                    //printf("%f %f %f ",*n1, *n2, *n3);
                    }
                    else if(*n3>0) //Discriminant > 0
                    {
                        *n1 = x1;
                        *n2 = x2;
                        //printf("%f %f %f ",*n1, *n2, *n3);
                    }
                    else
                        ;
                else if (M==90)
                    if(*n3 == 0){ //Discriminant = 0
                        if(x1 == (int)x1){//no second root
                            *n1 = (int)(x1);
                            *n2 = -1;
                        }
                        else{// no integer roots
                            *n1 = 0;
                            *n2 = -5; 
                        }
                        *n3 = (int)*n3;
                    }
                    else if(*n3>0){ //Discriminant > 0
                        if(x1 == (int)x1){
                            *n1 = (int)(x1);
                        }
                        else{//smaller root is not integer
                            *n1 = -0.000001;
                        }
                        if(x2 == (int)x2){
                            *n2 = (int)(x2);
                        }
                        else{//bigger root is not integer
                            *n2 = -0.000001;
                        }
                        *n3 = (int)*n3;
                        //printf("%f %f %f ",*n1, *n2, *n3);
                        
                    }
                    else
                        ;
                else if(M==78)
                    //printf("%f %f %f ",*n1, *n2, *n3);
                    if(*n3 == 0){ //Discriminant = 0
                        if(x1 == (int)x1 && x1 > 0){//no second root
                            *n1 = (int)(x1);
                            *n2 = -1;
                        }
                        else{// no natural roots
                            *n1 = 0;
                            *n2 = -8; 
                        }
                        *n3 = (int)*n3;
                    }
                    else if(*n3>0){//Discriminant > 0
                        
                        if(x1 == (int)x1 && x1 > 0){
                            *n1 = (int)(x1);
                        }
                        else{//smaller root is not natural
                            *n1 = -3;
                        }
                        if(x2 == (int)x2 && x2 > 0){
                            *n2 = (int)(x2);
                        }
                        else{//bigger root is not natural
                            *n2 = -4;
                        }
                        *n3 = (int)*n3;
                        
                    }
                    else;
                }
        }
    }
    else{ //Not a quadratic equasion
        *n1 = -2;
        *n2 = -2;
        *n3 = -2;
    }
}

int main()
{   
    //67=C 78=N 82=R 90=Z
    //quadratic_solver(1,-2,1,81);
    double n1,n2,n3;
    quadratic_solver(&n1, &n2, &n3, 4, 13, 9,90);
    if (n2 == -1 && n3 == 0)
        printf("x = %f\nD = %f\n",n1,n3);
    else if(n1 == -1 && n2 == -1 && n3 == -1)
        printf("D<0 <=> No roots in Real Numbers");
    else
        printf("x1 = %f\nx2 = %f\nD = %f Scrt(D) = %f\n",n1,n2,n3,mysqrt(n3));
    return 0;
}
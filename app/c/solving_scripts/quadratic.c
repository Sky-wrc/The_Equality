#include <stdio.h>
#include "../include/mymath.h"
//67=C 78=N 81=Q 90=Z
double discriminant(double a, double b, double c, char M)
{
    if(M>67)
    {
        if((b*b - 4*a*c)<0)
            return -1;
        else
            return (b*b - 4*a*c);
    }
    else
        return -1;
    
}

double quadratic_solver(double a, double b, double c, char M)
{
    double x1,x2;
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
    double D = discriminant(a,b,c,M);
    if(D == 0){
        
        double x = (-b)/(2*a);
        printf("%f %f %f",x, -1.0, D);
        return x, -1, D;
    }
    if(D>0)
    {
        x1 = min_num(((-b+mysqrt(D))/(2*a)),((-b-mysqrt(D))/(2*a)));
        x2 = max_num(((-b+mysqrt(D))/(2*a)),((-b-mysqrt(D))/(2*a)));
        printf("%f %f %f",x1,x2,D);
        return x1,x2,D;
    }
        
}

int main()
{   
    //quadratic_solver(1,-2,1,81);
    printf("%f %f %f",quadratic_solver(1,-2,1,81));

    return 0;
}
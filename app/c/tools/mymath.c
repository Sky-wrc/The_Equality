#include <stdio.h>

// double mysqrt(double n)
// {
//     double r=n,l=0,s;
//     // printf("%f\n", n);
//     if (n < 1){
//         r=1;
//         l = n;
//         s = (l + r)/2;
//         int k = 0;
//         while (s*s!=n){
//             // printf("%f\n", s);
//             k +=1;
//             if (k >100){
//                 //printf("%f\n",s);
//                 break;
//             }
//             if( -0.00001 < (s*s-n) < 0.00001)
//                 break;  
//             if  (s*s>n){
//                 r = s;
//                 s = (l + r)/2;
//             }
//             else {
//                 l = s;
//                 s = (l + r)/2;
//             }
            
//         }
//     }
//     else
//     {
//         s = (l + r)/2;
//         int k = 0;
//         while (s*s!=n){
//             k +=1;
//             if (k >100){
//                 //printf("%f\n",s);
//                 break;
//             } 
//             if  (s*s>n){
//                 r = s;
//                 s = (l + r)/2;
//             }
//             else {
//                 l = s;
//                 s = (l + r)/2;
//             }
            
//         // printf("\n");
//         }
//     }
    
//     return s;
// }

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
    while (b != 0){
        int tm = b;
        b = a % b;
        a = tm;
    }

    return a;
}

// int main ()
// {
//     int t = 0.25;
//     printf("%f",mysqrt(0.25));
//     return 0;
// }
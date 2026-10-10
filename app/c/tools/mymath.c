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
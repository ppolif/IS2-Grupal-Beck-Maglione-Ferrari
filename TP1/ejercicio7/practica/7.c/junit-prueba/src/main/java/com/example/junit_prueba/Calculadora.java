package com.example.junit_prueba;

public class Calculadora {

    public Integer sumar(Integer a, Integer b) {
        if (a==null) {
            a = 0;
        }

        if (b==null) {
            b = 0;
        }
        return a + b;
    }

    public Double dividir(Double num, Double den) throws Exception{
        if (den == null || den == 0){
            throw new Exception("Denominador igual a 0");
        }

        return num / den;
    }
}

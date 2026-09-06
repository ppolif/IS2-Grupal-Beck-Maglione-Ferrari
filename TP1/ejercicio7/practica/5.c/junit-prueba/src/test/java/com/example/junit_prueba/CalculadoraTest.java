package com.example.junit_prueba;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CalculadoraTest {
    Calculadora calc;

    @BeforeAll
    public static void beforeAll(){
        System.out.println("beforeAll");
    }

    @AfterAll
    public static void afterAll() {
        System.out.println("afterAll");
    }

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        calc = new Calculadora();
        System.out.println("inicio" + testInfo.getDisplayName());
    }

    @AfterEach
    public void afterEach(TestInfo testInfo) {
        System.out.println("finaliza" + testInfo.getDisplayName());
    }

    @Test
    @Order(1)
    public void testSumar() {
        Integer a=2;
        Integer b = 3;
        Integer c = 5;
        Integer resultado=0;

        resultado = calc.sumar(a, b);

        System.out.println("resultado " + resultado);
    }

    @Test
    @Order(2)
    public void testDividir() {
        Double a=2.0;
        Double b = 3.0;
        Double esperado = 0.6666666666666666;
        Double resultado=0.0;

        try {
            resultado = calc.dividir(a, b);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Assertions.assertEquals(esperado, resultado);

        System.out.println("resultado " + resultado);
    }

    @Test
    @Order(3)
    public void testDividirAssertTrue() {
        Double a=2.0;
        Double b = 3.0;
        Double esperado = 0.6666666666666666;
        Double resultado=0.0;

        try {
            resultado = calc.dividir(a, b);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Assertions.assertTrue(esperado.equals(resultado));

        System.out.println("resultado " + resultado);
    }

    @Order(4)
    @RepeatedTest(3)
    public void testDividirException() {
        Double a=2.0;
        Double b = 0.0;

        try {
            java.lang.Exception ex = assertThrows(Exception.class, () -> {
                calc.dividir(a, b);
            });

            assertEquals("Denominador igual a 0", ex.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}

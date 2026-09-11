package com.example.tinder.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MascotaE2ETest {

    private WebDriver driver;

    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
        options.setBinary("/usr/bin/brave-browser");
        driver = new ChromeDriver(options);
        // Le damos hasta 5 segundos al robot para encontrar los elementos en pantalla
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @Test
    void crearMascota_FlujoCompleto_CreaYApareceEnLista() {
        // --- PASO 1: PREPARAR EL ESTADO (LOGIN) ---
        driver.get("http://localhost:9000/login");

        // ¡IMPORTANTE!: Cambia estos valores por un usuario que ya exista en tu base de datos local
        driver.findElement(By.name("email")).sendKeys("augustobeck472@gmail.com");
        driver.findElement(By.name("clave")).sendKeys("1234567");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // --- PASO 2: IR A LA PANTALLA DE MASCOTAS ---
        driver.get("http://localhost:9000/mascota/mis-mascotas");

        // --- PASO 3: ABRIR EL FORMULARIO DE CREACIÓN ---
        // Buscamos el botón "Agrega una Mascota!" por su ruta[cite: 4]
        driver.findElement(By.cssSelector("a[href*='/mascota/editar-perfil']")).click();

        // --- PASO 4: LLENAR EL FORMULARIO ---
        // Usamos un número aleatorio para el nombre, así puedes correr el test muchas veces
        // sin confundirte viendo cuál mascota se acaba de crear
        String nombreMascota = "Robotito_" + System.currentTimeMillis();
        driver.findElement(By.name("nombre")).sendKeys(nombreMascota);

        // Seleccionamos el Tipo desde el combobox[cite: 4]
        Select selectTipo = new Select(driver.findElement(By.name("tipo")));
        selectTipo.selectByIndex(0); // Elige la primera opción de tu Enum

        // Seleccionamos el Sexo desde el combobox[cite: 4]
        Select selectSexo = new Select(driver.findElement(By.name("sexo")));
        selectSexo.selectByIndex(0); // Elige la primera opción de tu Enum

        driver.findElement(By.name("archivo")).sendKeys("/home/pity/Imágenes/perro.jpeg");

        // --- PASO 5: ENVIAR EL FORMULARIO ---
        driver.findElement(By.xpath("//button[contains(text(), 'Crear Mascota')]")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.urlContains("/inicio")); //hay que pedirle al usuario tester que espere unos segundos para ver datos persistidos en la vista

        // --- PASO 6: VERIFICAR QUE SE GUARDÓ EXITOSAMENTE ---
        // Según tu controlador, cuando guardas con éxito te manda a /inicio[cite: 2]
        // Para estar 100% seguros de que impactó en la Base de Datos, volvemos a tu lista
        driver.get("http://localhost:9000/mascota/mis-mascotas");

        // Leemos todo el HTML de la página y buscamos el nombre que generamos en el Paso 4
        String codigoFuentePantalla = driver.getPageSource();
        assertTrue(codigoFuentePantalla.contains(nombreMascota),
                "La mascota creada (" + nombreMascota + ") no se encontró en la tabla de Mis Mascotas.");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
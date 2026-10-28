package com.DulceMayor.backend;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests automatizados de arquitectura con ArchUnit (sección 10.1 de GENERAL.md y alcance del Paso 2).
 * Garantizan en cada ejecución de {@code mvn test} que se respeten las reglas de dependencia
 * entre capas, el patrón DTO en controladores y la prohibición de inyección por campo.
 */
class ArquitecturaTests {

    private static final String PAQUETE_BASE = "com.DulceMayor.backend";

    private static JavaClasses clasesProduccion;

    @BeforeAll
    static void importarClases() {
        clasesProduccion = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(PAQUETE_BASE);
    }

    @Test
    @DisplayName("Regla 1: Un *Service solo puede depender del repositorio <Entidad>Repository de su propia entidad (salvo *LecturaService)")
    void servicioSoloDependeDeSuPropioRepositorio() {
        ArchRule regla = classes()
                .that().resideInAPackage("..services..")
                .and().haveSimpleNameEndingWith("Service")
                .and().haveSimpleNameNotEndingWith("LecturaService")
                .and().doNotHaveSimpleName("BaseService")
                .should(dependerUnicamenteDeSuPropioRepositorio());

        regla.check(clasesProduccion);
    }

    @Test
    @DisplayName("Regla 2: Ninguna clase de controlador depende de repositorios ni de entidades")
    void controladoresNoDependenDeRepositoriosNiDeEntidades() {
        ArchRule regla = noClasses()
                .that().resideInAPackage("..controllers..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..repositories..", "..entities..");

        regla.check(clasesProduccion);
    }

    @Test
    @DisplayName("Regla 3a: Las entidades no dependen de servicios ni de controladores")
    void entidadesNoDependenDeServiciosNiDeControladores() {
        ArchRule regla = noClasses()
                .that().resideInAPackage("..entities..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..services..", "..controllers..");

        regla.check(clasesProduccion);
    }

    @Test
    @DisplayName("Regla 3b: Los servicios no dependen de controladores")
    void serviciosNoDependenDeControladores() {
        ArchRule regla = noClasses()
                .that().resideInAPackage("..services..")
                .should().dependOnClassesThat()
                .resideInAPackage("..controllers..");

        regla.check(clasesProduccion);
    }

    @Test
    @DisplayName("Regla 4: Sin @Autowired sobre campos (solo inyección por constructor)")
    void sinAutowiredEnCampos() {
        ArchRule regla = noFields()
                .should().beAnnotatedWith(Autowired.class);

        regla.check(clasesProduccion);
    }

    @Test
    @DisplayName("Regla 5: Las capas de aplicación y dominio (controllers, services, repositories, dtos, entities) no forman ciclos")
    void capasSinCiclos() {
        ArchRule regla = slices()
                .matching("com.DulceMayor.backend.(controllers|services|repositories|dtos|entities)..")
                .should().beFreeOfCycles();

        regla.check(clasesProduccion);
    }

    private static ArchCondition<JavaClass> dependerUnicamenteDeSuPropioRepositorio() {
        return new ArchCondition<>("depender únicamente del repositorio <Entidad>Repository de su propia entidad") {
            @Override
            public void check(JavaClass serviceClass, ConditionEvents events) {
                String serviceName = serviceClass.getSimpleName();
                String entidadPrefix = serviceName.substring(0, serviceName.length() - "Service".length());
                String repositorioEsperado = entidadPrefix + "Repository";

                for (Dependency dependency : serviceClass.getDirectDependenciesFromSelf()) {
                    JavaClass target = dependency.getTargetClass();
                    if (target.getPackageName().contains(".repositories")) {
                        String targetName = target.getSimpleName();
                        boolean esRepositorioPropio = targetName.equals(repositorioEsperado);
                        boolean esRepositorioBase = targetName.equals("BaseRepository");
                        if (!esRepositorioPropio && !esRepositorioBase) {
                            String mensaje = String.format(
                                    "El servicio %s depende de %s, pero solo tiene permitido depender de %s",
                                    serviceClass.getName(),
                                    target.getName(),
                                    repositorioEsperado
                            );
                            events.add(SimpleConditionEvent.violated(serviceClass, mensaje));
                        }
                    }
                }
            }
        };
    }
}


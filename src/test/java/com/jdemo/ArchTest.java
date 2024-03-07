package com.jdemo;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class ArchTest {

    @Test
    void servicesAndRepositoriesShouldNotDependOnWebLayer() {
        JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.jdemo");

        noClasses()
            .that()
            .resideInAnyPackage("com.jdemo.service..")
            .or()
            .resideInAnyPackage("com.jdemo.repository..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..com.jdemo.web..")
            .because("Services and repositories should not depend on web layer")
            .check(importedClasses);
    }
}

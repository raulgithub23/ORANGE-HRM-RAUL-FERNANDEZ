# 🤖 ORANGE-HRM-RAUL-FERNANDEZ
### Automatización de Pruebas de Software — Evaluación Parcial 3
**Asignatura:** API0101 · Automatización de Pruebas de Software  
**Sistema bajo prueba:** OrangeHRM Demo · `https://opensource-demo.orangehrmlive.com`  
**Credenciales demo:** Admin / admin123  
**Alumno:** Raúl Fernández  

---

## 📋 Descripción del proyecto

Suite de pruebas automatizadas sobre el sistema de gestión de recursos humanos **OrangeHRM**, desarrollada con el stack **Java + Selenium + Cucumber (BDD/Gherkin)** siguiendo el patrón Data-Driven Testing con archivos Excel.

La suite cubre 7 módulos funcionales con **28 escenarios** y **121 steps**, logrando un **99,17% de tasa de éxito** en la ejecución final.

---

## 🧪 Casos de prueba cubiertos

| ID     | Descripción                                       | Módulo        | Tipo         |
|--------|---------------------------------------------------|---------------|--------------|
| SETUP  | Recargar créditos de tipos de licencia            | Entitlements  | Precondición |
| CP-01  | Login exitoso con credenciales válidas            | Login         | Funcional    |
| CP-02  | Login fallido con credenciales inválidas          | Login         | Funcioanl    |
| CP-03  | Cierre de sesión correcto                         | Login         | Funcional    |
| CP-04  | Captura automática ante fallo de assert           | Login         | Framework    |
| CP-05  | Registro de empleado con datos desde Excel (x3)   | Empleados     | Data-Driven  |
| CP-06  | Búsqueda de empleado con filtros desde Excel (x3) | Empleados     | Data-Driven  |
| CP-07  | Edición de empleado con datos desde Excel (x3)    | Empleados     | Data-Driven  |
| CP-08  | Eliminación de empleado con datos desde Excel (x3)| Empleados     | Data-Driven  |
| CP-09  | Reporte Cucumber HTML publicado online            | Reportes      | Funcional    |
| CP-10  | Reporte Maven Masterthought generado              | Reportes      | Funcional    |
| CP-11  | Métricas de ejecución disponibles en el reporte   | Reportes      | Funcional    |
| CP-12  | Artefactos JSON y XML generados para CI/CD        | Reportes      | Funcional    |
| CP-13  | Editar campo de perfil con datos desde Excel (x3) | Perfil        | Data-Driven  |
| CP-14  | Registrar licencia con datos desde Excel (x3)     | Licencias     | Data-Driven  |
| CP-15  | Verificar métricas finales de la suite completa   | Suite         | Integración  |

---

## 🏗️ Estructura del proyecto
 
```
ORANGE-HRM-RAUL-FERNANDEZ/
├── src/test/
│   ├── java/
│   │   ├── hooks/
│   │   │   └── Hooks.java                      # @After: captura screenshot con java.awt.Robot
│   │   ├── pageObjects/                        # Patrón Page Object Model
│   │   │   ├── BasePage.java                   # Clase base con métodos comunes
│   │   │   ├── LoginPage.java
│   │   │   ├── DashboardPage.java
│   │   │   ├── AddEmployeePage.java
│   │   │   ├── PimEmployeeListPage.java
│   │   │   ├── MyInfoPage.java
│   │   │   ├── LeaveApplyPage.java
│   │   │   ├── LeaveListPage.java
│   │   │   ├── LeaveTypesPage.java
│   │   │   └── AddLeaveEntitlementPage.java
│   │   ├── runner/
│   │   │   ├── TestRunner.java                 # Runner CP-01 a CP-14
│   │   │   └── SuiteCompleta.java              # Runner suite completa (CP-15)
│   │   ├── stepDefinitions/
│   │   │   ├── Configuracion.java
│   │   │   ├── LoginSteps.java
│   │   │   ├── EmpleadosSteps.java
│   │   │   ├── BusquedaSteps.java
│   │   │   ├── LicenciasSteps.java
│   │   │   ├── EntitlementsSteps.java          # Setup de tipos de licencia
│   │   │   ├── PerfilSteps.java
│   │   │   └── ReportesSteps.java
│   │   └── utilidades/
│   │       ├── ExcelUtils.java                 # Lectura de Excel con Apache POI
│   │       └── Utility.java
│   └── resources/
│       ├── features/
│       │   ├── entitlements.feature            # Setup Gherkin para CP-14
│       │   ├── empleados.feature
│       │   ├── licencias.feature
│       │   ├── login.feature
│       │   ├── perfil.feature
│       │   ├── reportes.feature
│       │   └── suite.feature
│       ├── testData/
│       │   ├── dataEmpleados.xlsx
│       │   ├── dataLicencias.xlsx              # Tipos de licencia en español vigentes
│       │   ├── dataEdicion.xlsx
│       │   ├── dataEliminacion.xlsx
│       │   ├── dataFiltros.xlsx
│       │   └── dataPerfil.xlsx
│       └── cucumber.properties
├── evidencias/                                 # Screenshots automáticos por escenario
├── pom.xml
└── README.md
```
 
---

## ⚙️ Requisitos previos

| Herramienta       | Versión recomendada       |
|-------------------|---------------------------|
| Java JDK          | 17 (OpenJDK 64-bit)       |
| Maven             | 3.9.x                     |
| Google Chrome     | Última versión estable     |
| ChromeDriver      | Gestionado automáticamente por WebDriverManager |
| Sistema operativo | Windows 10/11             |

> ⚠️ **ChromeDriver** es gestionado automáticamente por la dependencia `webdrivermanager` (v5.9.2). No es necesario descargarlo ni configurarlo manualmente.

---

## 🚀 Instrucciones de ejecución

### 1. Clonar el repositorio
```bash
git clone https://github.com/raulgithub23/ORANGE-HRM-RAUL-FERNANDEZ.git
cd ORANGE-HRM-RAUL-FERNANDEZ
```

### 2. Ejecutar todos los casos y generar el reporte Maven detallado
```bash
mvn clean verify "-Dmaven.test.failure.ignore=true"
```

> Este es el **comando principal**. Ejecuta todos los casos de prueba (CP-01 a CP-15), ignora fallos para no interrumpir la suite, y al finalizar genera automáticamente el reporte detallado Maven Masterthought en la fase `verify`.

### 3. Ver los reportes generados
Abrir en el navegador tras ejecutar el comando anterior:
```
target/cucumber-reports-detallados/overview-features.html   ← Reporte Maven Masterthought
target/cucumber-reports/reporte-general.html                ← Reporte Cucumber HTML
```

---

## 📊 Resultados de la ejecución final

| Métrica                     | Valor                      |
|-----------------------------|----------------------------|
| Features ejecutadas         | 7 (85,71% passed)          |
| Escenarios totales          | 28                         |
| Escenarios passed           | 27 (96,43%)                |
| Escenarios failed           | 1                          |
| Steps totales               | 121                        |
| Steps passed                | 120 (99,17%)               |
| Duración total              | 10m 10.709s                |
| Screenshots generados       | 28 (uno por escenario)     |
| Plataforma                  | Windows 11                 |
| JVM                         | OpenJDK 64-bit 17.0.19+10  |
| Framework                   | cucumber-jvm 7.15.0        |

---

## 🔧 Cambios relevantes del último commit

### 1. Corrección de screenshots en blanco — `Hooks.java`
**Problema:** `TakesScreenshot` de Selenium devolvía imágenes de 7 KB sin contenido visual.  
**Solución:** Reemplazado por `java.awt.Robot.createScreenCapture()` con espera previa `WebDriverWait` hasta `document.readyState == complete`. Las evidencias ahora pesan entre 120–226 KB con contenido real.

### 2. Setup Gherkin para CP-14 — `EntitlementsSteps.java` + `entitlements.feature`
**Problema:** CP-14 fallaba cuando los tipos de licencia no existían en la demo compartida (se resetea periódicamente).  
**Solución:** Se creó un escenario SETUP que verifica/crea los tipos de licencia y recarga sus créditos antes de ejecutar `licencias.feature`. Se aprovecha el orden alfabético de Cucumber para ejecutarlo automáticamente primero.

### 3. Actualización de `dataLicencias.xlsx`
**Problema:** Los nombres de tipos de licencia en inglés (`US - Vacation`, `US - Personal`, `US - Bereavement`) fueron eliminados de la base de datos de la demo. Sus IDs internos quedaron bloqueados y no podían recrearse.  
**Solución:** Actualizados a los nombres en español vigentes: `US - Vacaciones`, `US - Asunto Personales`, `US - Luto`.

### 4. Comando de ejecución actualizado a `mvn clean verify`
El comando principal pasó de `mvn test` a `mvn clean verify "-Dmaven.test.failure.ignore=true"` para garantizar que el plugin `maven-cucumber-reporting` se ejecute en la fase `verify` y genere el reporte detallado automáticamente al finalizar la suite.

---

## 🏛️ Stack tecnológico

| Tecnología              | Versión  | Uso                                      |
|-------------------------|----------|------------------------------------------|
| Java                    | 17       | Lenguaje de implementación               |
| Selenium WebDriver      | 4.20.0   | Automatización del navegador             |
| Cucumber-JVM            | 7.15.0   | Framework BDD / Gherkin                  |
| Apache POI              | 5.2.5    | Lectura de archivos Excel (.xlsx)        |
| Maven                   | 3.9.x    | Gestión de dependencias y build          |
| WebDriverManager        | 5.9.2    | Gestión automática de ChromeDriver       |
| java.awt.Robot          | —        | Captura de screenshots del SO            |
| Maven Masterthought     | 5.8.1    | Reportes HTML enriquecidos               |
| JUnit                   | 4.13.2   | Runner de pruebas                        |

---

## 📁 Evidencias

Las capturas de pantalla se generan automáticamente al finalizar cada escenario mediante el hook `@After(order=10)` en `Hooks.java`. Se almacenan en la carpeta `evidencias/` con nomenclatura descriptiva:

```
PASS_CP-01_Login_exitoso_con_credenciales_validas_YYYYMMDD_HHMMSS.png
FAIL_CP-04_Captura_automatica_ante_fallo_de_assert_YYYYMMDD_HHMMSS.png
PASS_CP-14_Registrar_licencia_con_datos_desde_Excel_YYYYMMDD_HHMMSS.png
...
```

---

## 🔗 Repositorio

**GitHub:** [https://github.com/raulgithub23/ORANGE-HRM-RAUL-FERNANDEZ](https://github.com/raulgithub23/ORANGE-HRM-RAUL-FERNANDEZ)  
**Rama principal:** `main`
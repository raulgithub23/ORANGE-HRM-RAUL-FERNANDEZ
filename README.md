# 🤖 ORANGE-HRM-RAUL-FERNANDEZ
### Automatización de Pruebas de Software — Evaluación Parcial 3
**Asignatura:** API0101 · Automatización de Pruebas de Software  
**Sistema bajo prueba:** OrangeHRM Demo · `https://opensource-demo.orangehrmlive.com`  
**Credenciales demo:** Admin / admin123  
**Alumno:** Raúl Fernández  

---

## 📋 Descripción del proyecto

Suite de pruebas automatizadas sobre el sistema de gestión de recursos humanos **OrangeHRM**, desarrollada con el stack **Java + Selenium + Cucumber (BDD/Gherkin)** siguiendo el patrón Data-Driven Testing con archivos Excel.

La suite cubre 6 módulos funcionales con **27 escenarios** y **113 steps**, logrando un **100% de tasa de éxito** en la ejecución final.

---

## 🧪 Casos de prueba cubiertos

| ID     | Descripción                                      | Módulo        | Tipo         |
|--------|--------------------------------------------------|---------------|--------------|
| SETUP  | Recargar créditos de tipos de licencia           | Entitlements  | Precondición |
| CP-01  | Login exitoso con credenciales válidas           | Login         | Funcional    |
| CP-02  | Login fallido con credenciales inválidas         | Login         | Negativo     |
| CP-03  | Cierre de sesión correcto                        | Login         | Funcional    |
| CP-04  | Captura automática ante fallo de assert          | Login         | Framework    |
| CP-05  | Registro de empleado con datos desde Excel (x3)  | Empleados     | Data-Driven  |
| CP-06  | Búsqueda de empleado con filtros desde Excel (x3)| Empleados     | Data-Driven  |
| CP-07  | Edición de empleado con datos desde Excel (x3)   | Empleados     | Data-Driven  |
| CP-08  | Eliminación de empleado con datos desde Excel (x3)| Empleados    | Data-Driven  |
| CP-09  | Reporte Cucumber HTML publicado online           | Reportes      | Funcional    |
| CP-10  | Reporte Maven Masterthought generado             | Reportes      | Funcional    |
| CP-11  | Métricas de ejecución disponibles en el reporte  | Reportes      | Funcional    |
| CP-12  | Artefactos JSON y XML generados para CI/CD       | Reportes      | Funcional    |
| CP-13  | Editar campo de perfil con datos desde Excel (x3)| Perfil        | Data-Driven  |
| CP-14  | Registrar licencia con datos desde Excel (x3)    | Licencias     | Data-Driven  |
| CP-15  | Verificar métricas finales de la suite completa  | Suite         | Integración  |

---

## 🏗️ Estructura del proyecto

```
ORANGE-HRM-RAUL-FERNANDEZ/
├── src/test/
│   ├── java/
│   │   ├── hooks/
│   │   │   └── Hooks.java              # @After: captura screenshot con java.awt.Robot
│   │   ├── runner/
│   │   │   ├── TestRunner.java         # Runner CP-01 a CP-14
│   │   │   └── SuiteCompleta.java      # Runner suite completa (CP-15)
│   │   ├── stepDefinitions/
│   │   │   ├── LoginSteps.java
│   │   │   ├── EmpleadosSteps.java
│   │   │   ├── LicenciasSteps.java
│   │   │   ├── EntitlementsSteps.java  # NUEVO: setup de tipos de licencia
│   │   │   ├── PerfilSteps.java
│   │   │   ├── ReportesSteps.java
│   │   │   ├── BusquedaSteps.java
│   │   │   └── Configuracion.java
│   │   └── utilidades/
│   │       ├── ExcelUtils.java         # Lectura de Excel con Apache POI
│   │       └── Utility.java
│   └── resources/
│       ├── features/
│       │   ├── entitlements.feature    # NUEVO: setup Gherkin para CP-14
│       │   ├── empleados.feature
│       │   ├── licencias.feature
│       │   ├── login.feature
│       │   ├── perfil.feature
│       │   ├── reportes.feature
│       │   └── suite.feature
│       └── testData/
│           ├── dataEmpleados.xlsx
│           ├── dataLicencias.xlsx      # Actualizado: tipos de licencia vigentes
│           ├── dataEdicion.xlsx
│           ├── dataEliminacion.xlsx
│           ├── dataFiltros.xlsx
│           └── dataPerfil.xlsx
├── evidencias/                         # Screenshots automáticos por escenario
├── reportes/                           # Reportes HTML Cucumber y Maven
├── pom.xml
└── README.md
```

---

## ⚙️ Requisitos previos

| Herramienta     | Versión recomendada         |
|-----------------|-----------------------------|
| Java JDK        | 17 (OpenJDK 64-bit)         |
| Maven           | 3.8+                        |
| Google Chrome   | Última versión estable       |
| ChromeDriver    | Debe coincidir con Chrome   |
| Sistema operativo | Windows 10/11             |

> ⚠️ **Importante:** Verificar que `chromedriver --version` y `google-chrome --version` coincidan antes de ejecutar.

---

## 🚀 Instrucciones de ejecución

### 1. Clonar el repositorio
```bash
git clone https://github.com/raulgithub23/ORANGE-HRM-RAUL-FERNANDEZ.git
cd ORANGE-HRM-RAUL-FERNANDEZ
```

### 2. Compilar el proyecto
```bash
mvn compile
```

### 3. Ejecutar todos los casos (CP-01 a CP-14)
```bash
mvn test -Dtest=TestRunner
```

### 4. Ejecutar la suite completa (incluye CP-15)
```bash
mvn test -Dtest=SuiteCompleta
```

### 5. Ver los reportes generados
Abrir en el navegador:
```
reportes/cucumber-html-reports/overview-features.html   ← Reporte Maven Masterthought
reportes/reporte-general.html                           ← Reporte Cucumber HTML
```

---

## 📊 Resultados de la ejecución final

| Métrica                     | Valor              |
|-----------------------------|--------------------|
| Features ejecutadas         | 6 (100% passed)    |
| Escenarios totales          | 27                 |
| Escenarios passed           | 27 (100%)          |
| Escenarios failed           | 0                  |
| Steps totales               | 113                |
| Steps passed                | 113 (100%)         |
| Duración total              | 8m 4.955s          |
| Screenshots generados       | 27 (uno por escenario) |
| Plataforma                  | Windows 11         |
| JVM                         | OpenJDK 64-bit 17.0.17+10 |
| Framework                   | cucumber-jvm 7.15.0 |

---

## 🔧 Cambios relevantes del último commit (`85b2b28`)

### 1. Corrección de screenshots en blanco — `Hooks.java`
**Problema:** `TakesScreenshot` de Selenium devolvía imágenes de 7 KB sin contenido visual.  
**Solución:** Reemplazado por `java.awt.Robot.createScreenCapture()` con espera previa `WebDriverWait` hasta `document.readyState == complete`. Las evidencias ahora pesan entre 120–226 KB con contenido real.

### 2. Setup Gherkin para CP-14 — `EntitlementsSteps.java` + `entitlements.feature`
**Problema:** CP-14 fallaba cuando los tipos de licencia no existían en la demo compartida (se resetea periódicamente).  
**Solución:** Se creó un escenario SETUP que verifica/crea los tipos de licencia y recarga sus créditos antes de ejecutar `licencias.feature`. Se aprovecha el orden alfabético de Cucumber para ejecutarlo automáticamente primero.

### 3. Actualización de `dataLicencias.xlsx`
**Problema:** Los nombres de tipos de licencia en inglés (`US - Vacation`, `US - Personal`, `US - Bereavement`) fueron eliminados de la base de datos de la demo. Sus IDs internos quedaron bloqueados y no podían recrearse.  
**Solución:** Actualizados a los nombres en español vigentes: `US - Vacaciones`, `US - Asunto Personales`, `US - Luto`.

---

## 🏛️ Stack tecnológico

| Tecnología         | Uso                                      |
|--------------------|------------------------------------------|
| Java 17            | Lenguaje de implementación               |
| Selenium WebDriver | Automatización del navegador             |
| Cucumber-JVM 7.15  | Framework BDD / Gherkin                  |
| Apache POI         | Lectura de archivos Excel (.xlsx)        |
| Maven              | Gestión de dependencias y build          |
| java.awt.Robot     | Captura de screenshots del SO            |
| Maven Masterthought| Reportes HTML enriquecidos               |
| JUnit 4            | Runner de pruebas                        |
| ChromeDriver       | Driver para Google Chrome                |

---

## 📁 Evidencias

Las capturas de pantalla se generan automáticamente al finalizar cada escenario mediante el hook `@After(order=10)` en `Hooks.java`. Se almacenan en la carpeta `evidencias/` con nomenclatura descriptiva:

```
PASS_CP-01_Login_exitoso_con_credenciales_validas_YYYYMMDD_HHMMSS.png
PASS_CP-14_Registrar_licencia_con_datos_desde_Excel_YYYYMMDD_HHMMSS.png
...
```

---

## 🔗 Repositorio

**GitHub:** [https://github.com/raulgithub23/ORANGE-HRM-RAUL-FERNANDEZ](https://github.com/raulgithub23/ORANGE-HRM-RAUL-FERNANDEZ)  
**Rama principal:** `main`  
**Commits:** 9  
**Último commit:** `85b2b28` — fix: corregir capturas blancas, refactorizar entitlements y actualizar dataLicencias

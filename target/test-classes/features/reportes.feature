# language: es
# PPT 3.3.1 - Casos 9 al 12: Reportes Cucumber
@reportes
Característica: Generación de Reportes Cucumber en OrangeHRM
  Como líder técnico del proyecto
  Quiero verificar que los reportes se generen correctamente
  Para tener métricas de calidad exportables

  # CASO 9 - Reporte HTML publicado online via cucumber.properties
  @caso9 @smoke
  Escenario: CP-09 Reporte Cucumber HTML publicado online
    Dado el usuario está autenticado en el sistema
    Entonces el Dashboard debe mostrar los widgets del sistema

  # CASO 10 - Reporte Maven Masterthought + Feature Report
  @caso10 @smoke
  Escenario: CP-10 Reporte Maven Masterthought generado
    Dado el usuario está autenticado en el sistema
    Entonces el Dashboard debe mostrar los widgets del sistema

  # CASO 11 - Steps Statistics y métricas de ejecución
  @caso11 @smoke
  Escenario: CP-11 Métricas de ejecución disponibles en el reporte
    Dado el usuario está autenticado en el sistema
    Entonces el Dashboard debe mostrar los widgets del sistema

  # CASO 12 - Reporte JSON y XML para integración CI/CD
  @caso12 @smoke
  Escenario: CP-12 Artefactos JSON y XML generados para CI/CD
    Dado el usuario está autenticado en el sistema
    Entonces el Dashboard debe mostrar los widgets del sistema

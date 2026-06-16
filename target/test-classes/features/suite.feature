# language: es
# PPT 3.1.1 + 3.2.1 + 3.3.1 - Caso 15: Suite completa con evidencias y métricas
@suite
Característica: Suite Completa de Automatización OrangeHRM
  Como equipo de QA
  Quiero ejecutar todos los casos en un único runner
  Para obtener métricas globales y evidencias completas del proyecto

  # CASO 15 - Verificación de métricas finales de la suite
  @caso15 @smoke
  Escenario: CP-15 Verificar métricas finales de la suite completa
    Dado el usuario está autenticado en el sistema
    Entonces el Dashboard debe mostrar los widgets del sistema
    Y las métricas de la suite deben estar dentro de los umbrales definidos

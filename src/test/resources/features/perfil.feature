# language: es
# PPT 3.1.1 + 3.2.1 - Caso 13: Gestión de perfiles (Data-Driven + Screenshots)
@perfil
Característica: Actualización de perfil en My Info
  Como usuario autenticado
  Quiero actualizar mis datos de perfil usando datos desde Excel
  Para validar el módulo My Info de forma parametrizada con evidencia

  # CASO 13 - Gestión de perfiles con datos externos + screenshot
  @caso13 @regression
  Esquema del escenario: CP-13 Editar campo de perfil con datos desde Excel
    Dado el usuario está autenticado en el sistema
    Cuando navega al módulo My Info
    Y actualiza el campo de perfil de la fila <NroFila> del archivo de perfil
    Y hace click en Save del perfil
    Entonces el campo debe reflejar el nuevo valor
    Y se captura screenshot como evidencia del perfil

    Ejemplos:
      | NroFila |
      | 1       |
      | 2       |
      | 3       |

# language: es
# PPT 3.2.1 + 3.3.1 - Caso 14: Asignación de licencias con datos externos
@licencias
Característica: Solicitud de Licencias en OrangeHRM
  Como empleado del sistema
  Quiero registrar solicitudes de licencia con datos desde Excel
  Para validar el módulo Leave de forma parametrizada

  # CASO 14 - Asignación de licencias con datos externos
  @caso14 @regression
  Esquema del escenario: CP-14 Registrar licencia con datos desde Excel
    Dado el usuario está autenticado en el sistema
    Cuando navega al módulo de solicitud de licencias
    Y selecciona el tipo de licencia de la fila <NroFila>
    Y ingresa las fechas de la fila <NroFila>
    Y hace clic en aplicar la solicitud
    Entonces la solicitud debe quedar registrada en el sistema

    Ejemplos:
      | NroFila |
      | 1       |
      | 2       |
      | 3       |

# language: es
# PPT 3.2.1 - Casos 5 al 8: Pruebas Data-Driven con Excel
@datadriven
Característica: Gestión de Empleados Data-Driven en OrangeHRM
  Como administrador del sistema
  Quiero ejecutar pruebas con datos externos desde Excel
  Para validar múltiples empleados sin modificar el código

  # CASO 5 - Registro de empleados Data-Driven (Excel)
  @caso5 @smoke
  Esquema del escenario: CP-05 Registro de empleado con datos desde Excel
    Dado el administrador está autenticado y accede al módulo PIM
    Cuando hace clic en el botón para agregar un nuevo empleado
    Y carga nombre y apellido desde la fila <NroFila> del archivo de empleados
    Y confirma el registro presionando Guardar
    Entonces el sistema debe abrir la ficha del nuevo empleado

    Ejemplos:
      | NroFila |
      | 1       |
      | 2       |
      | 3       |

  # CASO 6 - Búsqueda con filtros combinados (Excel)
  @caso6 @smoke
  Esquema del escenario: CP-06 Búsqueda de empleado con filtros desde Excel
    Dado el administrador está autenticado y accede al módulo PIM
    Cuando busca con los filtros de la fila <NroFila> del archivo de filtros
    Entonces los resultados coinciden con lo esperado en la fila <NroFila>

    Ejemplos:
      | NroFila |
      | 1       |
      | 2       |
      | 3       |

  # CASO 7 - Edición de empleado con datos externos (Excel)
  @caso7 @regression
  Esquema del escenario: CP-07 Edición de empleado con datos desde Excel
    Dado el administrador está autenticado y accede al módulo PIM
    Cuando busca al empleado por nombre de la fila <NroFila> del archivo de edicion
    Y actualiza el cargo del empleado con los datos de la fila <NroFila>
    Entonces los datos actualizados deben reflejarse en el perfil

    Ejemplos:
      | NroFila |
      | 1       |
      | 2       |
      | 3       |

  # CASO 8 - Eliminar empleado con múltiples IDs (Excel)
  @caso8 @regression
  Esquema del escenario: CP-08 Eliminación de empleado con datos desde Excel
    Dado el administrador está autenticado y accede al módulo PIM
    Cuando busca al empleado por nombre de la fila <NroFila> del archivo de eliminacion
    Y selecciona el checkbox y hace click en Delete
    Y confirma la eliminación en el diálogo
    Entonces el empleado no debe aparecer en la búsqueda posterior

    Ejemplos:
      | NroFila |
      | 1       |
      | 2       |
      | 3       |

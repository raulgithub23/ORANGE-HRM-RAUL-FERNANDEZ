# language: es
# PPT 3.2.1 + 3.3.1 - Precondición de Caso 14: Carga de créditos de licencia
@entitlements
Característica: Asignación de Créditos de Licencia (Entitlements)
  Como administrador del sistema
  Quiero asignar créditos de licencia al usuario Admin
  Para que el Caso 14 pueda registrar solicitudes sin error de balance insuficiente

  # Se ejecuta UNA sola vez, antes que @caso14, gracias al orden alfabético
  # de archivos .feature (entitlements.feature corre antes que licencias.feature).
  #
  # IMPORTANTE: la demo pública de OrangeHRM se reinicia cada 24h y es
  # compartida con otros usuarios, así que los Leave Types personalizados
  # (US - Vacaciones, US - Asunto Personales, US - Luto) pueden desaparecer
  # en cualquier momento. Por eso, antes de recargar créditos, se asegura
  # que cada tipo exista: si ya existe no se vuelve a crear, y si no existe
  # se crea desde cero en Leave > Configure > Leave Types.
  @setupEntitlements @regression
  Escenario: SETUP - Recargar créditos de US - Vacaciones, US - Asunto Personales y US - Luto
    Dado el usuario está autenticado en el sistema
    Y asegura que el tipo de licencia "US - Vacaciones" existe
    Y asegura que el tipo de licencia "US - Asunto Personales" existe
    Y asegura que el tipo de licencia "US - Luto" existe
    Cuando recarga el crédito de licencia para "US - Vacaciones"
    Y recarga el crédito de licencia para "US - Asunto Personales"
    Y recarga el crédito de licencia para "US - Luto"
    Entonces los créditos de licencia deben quedar asignados correctamente

# language: es
# PPT 3.1.1 - Casos 1 al 4: Capturas de evidencia
@autenticacion
Característica: Autenticación en OrangeHRM
  Como usuario del sistema
  Quiero iniciar y cerrar sesión correctamente
  Para garantizar el acceso seguro a la plataforma

  # CASO 1 - Login exitoso + screenshot
  @caso1 @smoke
  Escenario: CP-01 Login exitoso con credenciales válidas
    Dado el usuario navega a "https://opensource-demo.orangehrmlive.com"
    Cuando ingresa usuario "Admin" y contraseña "admin123"
    Y hace click en el botón Login
    Entonces debe visualizarse el Dashboard de OrangeHRM

  # CASO 2 - Login fallido + screenshot de error
  @caso2 @smoke
  Escenario: CP-02 Login fallido con credenciales inválidas
    Dado el usuario navega a "https://opensource-demo.orangehrmlive.com"
    Cuando ingresa usuario "usuarioFalso" y contraseña "claveErronea"
    Y hace click en el botón Login
    Entonces se muestra el mensaje "Invalid credentials"

  # CASO 3 - Cierre de sesión + screenshot
  @caso3 @smoke
  Escenario: CP-03 Cierre de sesión correcto
    Dado el usuario está autenticado en OrangeHRM
    Cuando hace click en el menú de usuario
    Y selecciona la opción Logout
    Entonces debe redirigirse a la pantalla de Login

  # CASO 4 - Captura automática en fallo de assert (via hook @After)
  @caso4 @regression
  Escenario: CP-04 Captura automática ante fallo de assert
    Dado el usuario está autenticado en OrangeHRM
    Cuando navega al módulo About
    Entonces el título de la página debe contener "ModuloAbout"

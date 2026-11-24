# Pedidos360 - Microservicio Users

Microservicio de usuarios de Pedidos360, desarrollado con Java 21,
Spring Boot 4.1.1, PostgreSQL y Amazon Cognito.

## Responsabilidades

- Autenticar usuarios mediante Amazon Cognito.
- Devolver tokens de autenticacion.
- Registrar usuarios de prueba en Cognito y PostgreSQL.
- Asignar ROLE_CLIENTE durante el registro.
- Consultar usuarios almacenados localmente.

## API REST

| Metodo | Ruta | Descripcion |
| --- | --- | --- |
| POST | /api/usuarios/login | Autenticacion con Cognito |
| POST | /api/usuarios | Registro de usuarios |
| GET | /api/usuarios | Listado de usuarios |

## Ejecucion local

Requisitos:

- Java 21.
- PostgreSQL.
- Credenciales AWS validas para el laboratorio.
- Configuracion de Cognito.

La aplicacion tiene configurado el puerto 8082 por defecto.
Para la integracion con Angular se utiliza el puerto 8084.

En PowerShell:

    $env:SERVER_PORT = "8084"
    $env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/bdusuarios"
    $env:DB_PASSWORD = Read-Host "Clave local de PostgreSQL"
    .\mvnw.cmd spring-boot:run

Angular consulta el backend mediante:

    http://localhost:8084/api/usuarios

## Integracion con AWS Cognito

El inicio de sesion usa ADMIN_USER_PASSWORD_AUTH.

Durante las pruebas se verificaron los grupos:

- ROLE_ADMIN
- ROLE_CLIENTE

El registro implementado asigna ROLE_CLIENTE, sin aceptar
que el usuario elija libremente un rol administrativo.

## Seguridad y limitaciones actuales

IMPORTANTE: el registro actual es experimental y no esta listo
para publicarse en Internet.

El endpoint POST /api/usuarios utiliza operaciones privilegiadas
de Cognito y todavia no exige una verificacion real del correo.

El endpoint GET /api/usuarios tampoco debe exponerse publicamente
sin autenticacion y autorizacion adecuadas.

Antes de publicar se requiere:

- Registro autonomo seguro.
- Confirmacion del correo electronico.
- Proteccion de los endpoints administrativos.
- Validacion de entradas y manejo seguro de errores.
- Pruebas de seguridad adicionales.

No subir contrasenas, tokens ni credenciales AWS al repositorio.

## Estado de las pruebas

- Compilacion del microservicio realizada.
- Registro de usuario de prueba verificado en Cognito.
- Persistencia en PostgreSQL comprobada.
- Inicio de sesion de cliente comprobado.
- Integracion de login con Angular comprobada.

El formulario Registrarme de Angular aun no implementa
el registro autonomo y la confirmacion de correo.

## Control de versiones

El desarrollo del registro se realiza en la rama:

    feat/registro-externo-cognito

Los cambios se revisan mediante Pull Request antes de
incorporarlos a main.

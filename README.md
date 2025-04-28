# GATEWAY SERVER
> Este gateway se encarga de publicar los urls de los microservicios de la aplicacion

## Variables de entorno
```
USERS_URL=http://localhost:8080/v1/users
```
```
POSTS_URL=http://localhost:8080/v1/posts
```
```
COMMENTS_URL=http://localhost:8080/v1/comments
```
```
AUTH_SERVER=http://localhost:9000
```
```
REDIRECT_URL=http://localhost:4200
```
```
APP_URL=http://localhost:4200
```


## Tabla de recursos
| NOMBRE                       | RUTA                |  
|------------------------------|---------------------|
| Microservicio de usuarios    | /api/v1/users/**    |
| Microservicio de posts       | /api/v1/posts/**    |
| Microservicio de comentarios | /api/v1/comments/** |

## Stack
* Spring boot
* Spring cloud gateway
* Github Actions
* Docker
# Instalación en Windows con Eclipse

## 1. Requisitos

Instala:

- JDK 17 o superior.
- Eclipse IDE for Enterprise Java and Web Developers.
- Maven, si quieres ejecutar desde consola.

Comprueba Java:

```cmd
java -version
javac -version
```

Debe salir Java 17 o superior.

## 2. Importar en Eclipse

1. Descomprime el ZIP.
2. Abre Eclipse.
3. `File > Import > Maven > Existing Maven Projects`.
4. Selecciona la carpeta que contiene `pom.xml`.
5. Pulsa `Finish`.
6. Espera a que descargue dependencias.

## 3. Ejecutar

Abre:

```text
src/main/java/com/alfonso/flexcommerce/FlexCommerceApplication.java
```

Ejecuta:

```text
Run As > Java Application
```

Luego abre:

```text
http://localhost:8081
```

## 4. Panel admin

```text
http://localhost:8081/admin
```

Usuario:

```text
admin
```

Contraseña:

```text
admin123
```

## 5. H2 Console

```text
http://localhost:8081/h2-console
```

Datos:

```text
JDBC URL: jdbc:h2:file:./data/flexcommerce
User: sa
Password: vacío
```

## 6. Si el puerto 8081 está ocupado

Edita:

```text
src/main/resources/application.properties
```

Cambia:

```properties
server.port=8081
```

Por ejemplo:

```properties
server.port=8082
```

Y entra por:

```text
http://localhost:8082
```

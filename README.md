# SistemaDefinitivo

Sistema de gestión para pastelería, desarrollado en Java con JavaFX, Hibernate y MySQL. Permite administrar productos, insumos, empleados, proveedores, agenda, pedidos, eventos y estadísticas, con generación de reportes en Excel.

## Tabla de Contenidos

- [Características](#características)
- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Autores](#autores)
- [Cómo importar y ejecutar el proyecto fácilmente](#cómo-importar-y-ejecutar-el-proyecto-fácilmente)

---

## Características

- Gestión de productos, insumos, proveedores y empleados.
- Control de stock y recetas.
- Agenda de tareas y eventos.
- Registro de pedidos y eventos.
- Estadísticas y reportes exportables a Excel.
- Control de usuarios y permisos.
- Notificaciones configurables.
- Interfaz moderna y personalizable con CSS.

## Tecnologías utilizadas

- Java 22
- JavaFX 23
- Hibernate 6
- MySQL
- Maven
- ControlsFX
- Apache POI (reportes Excel)
- Lombok
- JUnit 5 (pruebas)
- Log4j (logs)

## Estructura del proyecto

```
src/
  main/
    java/
      application/         # Clase principal App.java
      controller/          # Controladores JavaFX (UI)
      model/               # Entidades y modelos de dominio
      persistence/dao/     # DAOs para acceso a datos
      service/             # Lógica de negocio y servicios
      utilities/           # Utilidades y helpers
    resources/
      com/example/pasteleria/  # Archivos FXML (vistas)
      com.example.image/       # Imágenes
      css/                     # Hojas de estilo CSS
      META-INF/                # persistence.xml (JPA)
pom.xml
README.md
```

## Autores

- Martín Schönberger
- Sofia Lopez

## Cómo importar y ejecutar el proyecto fácilmente

1. **Instalá Java 22** en tu computadora.
2. **Descargá e instala IntelliJ IDEA**.
3. **Importá el proyecto como Maven Project:**
   - En IntelliJ: `File > Open` y selecciona la carpeta del proyecto.
4. **Maven descargará automáticamente todas las dependencias** (incluyendo JavaFX y demás librerías).
5. **Ejecuta el proyecto:**
   - Desde la terminal, en la carpeta del proyecto:
     ```sh
     mvn javafx:run
     ```

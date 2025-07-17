# SistemaDefinitivo

Sistema de gestión para pastelería, desarrollado en Java con JavaFX, Hibernate y MySQL. Permite administrar productos, insumos, empleados, proveedores, agenda, pedidos, eventos y estadísticas, con generación de reportes en Excel.

## Tabla de Contenidos

- [Características](#características)
- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Autores](#autores)

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

# SistemaDefinitivo

Sistema de gestión para pastelería, desarrollado en Java con JavaFX, Hibernate y MySQL. Permite administrar productos, insumos, empleados, proveedores, agenda, pedidos, eventos y estadísticas, con generación de reportes en Excel.

## Tabla de Contenidos

- [Características](#características)
- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Flujo Operativo Recomendado del Sistema](#flujo-operativo-recomendado-del-sistema)
- [Usuarios de ejemplo del sistema](#usuarios-de-ejemplo-del-sistema)
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

## Flujo Operativo Recomendado del Sistema
A continuación se describe el flujo sugerido para la utilización integral y correcta del sistema de gestión de pastelería, asegurando la trazabilidad de los datos y la demostración de las principales funcionalidades:

### Gestión de Proveedores
- Acceda al módulo de proveedores.
- Registre un nuevo proveedor, completando los datos requeridos para su identificación y contacto.

### Gestión de Insumos
- Diríjase a la sección de configuración y seleccione el apartado de insumos.
- Registre un nuevo insumo, asociándolo al proveedor previamente creado para garantizar la trazabilidad de origen.

### Control de Stock
- Ingrese a la pestaña de stock.
- Registre una nueva compra del insumo recientemente creado, incrementando así el inventario disponible y reflejando la entrada de mercancía.

### Gestión de Recetas
- Acceda al módulo de recetas.
- Cree una nueva receta, incorporando el insumo cargado en los pasos anteriores como parte de su composición.

### Gestión de Productos
- Diríjase a la sección de productos.
- Registre un nuevo producto y asígnele la receta creada, estableciendo la relación entre producto final y sus componentes.

### Gestión de Clientes
- Desde el módulo de configuración o directamente desde el apartado de pedidos, registre un nuevo cliente, asegurando que los datos estén disponibles para futuras transacciones.

### Gestión de Pedidos
1. Ingrese al módulo de pedidos.
2. Cree un nuevo pedido seleccionando el cliente y el producto previamente registrados.
3. Para cargar los datos del cliente, ingrese el DNI en el campo correspondiente y presione Enter. Por ejemplo: DNI: 99.
4. Utilice la funcionalidad de tablero para modificar el estado del pedido, avanzando por las distintas etapas del ciclo de vida: “Pendiente” → “En proceso” → “Hecho” → “Entregado”.
5. Además, según el estado del pedido, puede modificar, eliminar, entregar o visualizar el detalle del mismo haciendo clic derecho sobre la etiqueta del pedido.

### Gestión de Eventos
- Acceda al módulo de eventos.
- Registre un nuevo evento, como puede ser una entrega especial, promoción o actividad relevante para la pastelería.

### Análisis y Reportes
- Diríjase a la sección de estadísticas.
- Visualice los diferentes gráficos generados por el sistema, tales como ventas, consumo de insumos, desempeño de productos, entre otros.
- Genere y descargue el reporte correspondiente para su análisis o presentación.

#### Notas adicionales
Se recomienda seguir este flujo para garantizar la correcta vinculación de datos entre módulos y la demostración de la funcionalidad integral del sistema.

## Usuarios de ejemplo del sistema

| DNI        | Contraseña         | Rol           |
|------------|--------------------|---------------|
| 12345678   | contraseña123      | Administrador |
| 15975368   | contraseña_jose    | Cajera        |
| 39393939   | contraseña_lionel  | Empleado      |

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

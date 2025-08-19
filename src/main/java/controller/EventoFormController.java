package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Evento;
import model.EventoProducto;
import model.Producto;
import persistence.dao.EventoDAO;
import utilities.ActionLogger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public class EventoFormController {

    @FXML
    private TextField nombreEventoField;
    @FXML
    private TextArea descripcionEventoField; // <- se vuelve solo visual
    @FXML
    private TextField nombreClienteField;
    @FXML
    private TextField telefonoClienteField;
    @FXML
    private TextField direccionEventoField;
    @FXML
    private DatePicker fechaEventoPicker;
    @FXML
    private TextField cantPersonasField;
    @FXML
    private TextField presupuestoField;
    @FXML
    private TextField horarioEventoField;

    private Evento eventoActual;
    private CatalogoEventoController catalogoEventoController;

    @FXML
    public void initialize() {
        // Hacer descripción solo visual
        descripcionEventoField.setEditable(false);
        descripcionEventoField.setFocusTraversable(false);
        descripcionEventoField.setPromptText("Se completa automáticamente con los productos del evento");

        if (fechaEventoPicker != null) {
            fechaEventoPicker.setDayCellFactory(new Callback<DatePicker, DateCell>() {
                @Override
                public DateCell call(DatePicker param) {
                    return new DateCell() {
                        @Override
                        public void updateItem(LocalDate date, boolean empty) {
                            super.updateItem(date, empty);
                            if (date.isBefore(LocalDate.now())) {
                                setDisable(true);
                                setStyle("-fx-background-color: #ffc0cb;");
                            }
                        }
                    };
                }
            });
        }
    }

    public void setEvento(Evento evento) {
        this.eventoActual = evento;
        if (evento != null) {
            cargarDatosEvento(evento);

            if (evento.getHorario_evento() != null) {
                horarioEventoField.setText(evento.getHorario_evento().toString());
            } else {
                horarioEventoField.setText("");
            }

            // ✅ Completar la descripción con los items ya guardados
            descripcionEventoField.setText(construirResumenDesdeEvento(evento));
        }
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
        String nombreEvento = nombreEventoField.getText();
        String nombreCliente = nombreClienteField.getText();
        String telefonoCliente = telefonoClienteField.getText();
        String direccionEvento = direccionEventoField.getText();
        LocalDate fechaEvento = fechaEventoPicker.getValue();

        String horarioStr = horarioEventoField.getText();
        java.time.LocalTime horarioEvento = null;
        if (horarioStr != null && !horarioStr.isBlank()) {
            try {
                horarioEvento = java.time.LocalTime.parse(horarioStr);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error de Validación",
                        "El horario debe tener formato HH:mm (por ejemplo, 14:30)");
                return;
            }
        }

        int cantPersonas;
        BigDecimal presupuesto;
        try {
            cantPersonas = Integer.parseInt(cantPersonasField.getText());
            presupuesto = new BigDecimal(presupuestoField.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación",
                    "Por favor, ingresa valores válidos para la cantidad de personas y el presupuesto.");
            return;
        }

        // 🔄 Asegurar que la descripción visual esté actualizada antes de validar
        // (si se abrió catálogo, la tomamos de ahí)
        if (catalogoEventoController != null) {
            String resumenSeleccion = construirResumenDesdeSeleccion();
            if (!resumenSeleccion.isBlank()) {
                descripcionEventoField.setText(resumenSeleccion);
            }
        }
        String descripcionEvento = descripcionEventoField.getText();

        if (!validateFields(nombreEvento, descripcionEvento, nombreCliente, telefonoCliente, direccionEvento,
                fechaEvento, cantPersonas, presupuesto)) {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", "Todos los campos deben ser llenados.");
            return;
        }

        try {
            EventoDAO eventoDAO = new EventoDAO();

            if (eventoActual == null) {
                // Validar fecha única solo al crear
                Evento eventoExistente = eventoDAO.findByFecha(fechaEvento);
                if (eventoExistente != null) {
                    showAlert(Alert.AlertType.ERROR, "Error de Validación",
                            "Ya existe un evento programado para esta fecha.");
                    return;
                }

                // Crear NUEVO evento
                Evento nuevoEvento = new Evento();
                nuevoEvento.setNombre_evento(nombreEvento);
                nuevoEvento.setDescripcion_evento(descripcionEvento); // <- solo visual
                nuevoEvento.setNombre_cliente(nombreCliente);
                nuevoEvento.setTelefono_cliente(telefonoCliente);
                nuevoEvento.setDireccion_evento(direccionEvento);
                nuevoEvento.setFecha_evento(fechaEvento);
                nuevoEvento.setCant_personas(cantPersonas);
                nuevoEvento.setPresupuesto(presupuesto);
                nuevoEvento.setHorario_evento(horarioEvento);

                // 👇 APLICAR selección del catálogo ANTES de guardar (para cascade)
                aplicarSeleccionDeCatalogoAlEvento(nuevoEvento);

                // Sincronizar descripción con items definitivos por si cambió el total
                nuevoEvento.setDescripcion_evento(construirResumenDesdeEvento(nuevoEvento));

                eventoDAO.save(nuevoEvento);
                ActionLogger.log("Nuevo evento guardado: " + nombreEvento + " para la fecha " + fechaEvento);

            } else {
                // Actualizar EXISTENTE
                eventoActual.setNombre_evento(nombreEvento);
                // La descripción la volvemos a calcular desde los items (no editable)
                eventoActual.setNombre_cliente(nombreCliente);
                eventoActual.setTelefono_cliente(telefonoCliente);
                eventoActual.setDireccion_evento(direccionEvento);
                eventoActual.setFecha_evento(fechaEvento);
                eventoActual.setCant_personas(cantPersonas);
                eventoActual.setHorario_evento(horarioEvento);

                // 👇 APLICAR selección del catálogo ANTES del update
                aplicarSeleccionDeCatalogoAlEvento(eventoActual);

                // Sincronizar descripción con items
                eventoActual.setDescripcion_evento(construirResumenDesdeEvento(eventoActual));
                descripcionEventoField.setText(eventoActual.getDescripcion_evento());

                eventoDAO.update(eventoActual);
                ActionLogger.log("Evento actualizado: " + nombreEvento + " para la fecha " + fechaEvento);
            }

            // Cerrar el formulario
            ((Stage) nombreEventoField.getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error al guardar el evento: " + e.getMessage());
        }
    }

    private boolean validateFields(String nombreEvento, String descripcionEvento, String nombreCliente,
            String telefonoCliente, String direccionEvento, LocalDate fechaEvento,
            int cantPersonas, BigDecimal presupuesto) {
        return nombreEvento != null && !nombreEvento.isEmpty() &&
                descripcionEvento != null && !descripcionEvento.isEmpty() && // ← mantenemos obligatorio
                nombreCliente != null && !nombreCliente.isEmpty() &&
                telefonoCliente != null && !telefonoCliente.isEmpty() &&
                direccionEvento != null && !direccionEvento.isEmpty() &&
                fechaEvento != null &&
                cantPersonas > 0 &&
                presupuesto != null && presupuesto.compareTo(BigDecimal.ZERO) > 0;
    }

    public void cargarDatosEvento(Evento evento) {
        nombreEventoField.setText(evento.getNombre_evento());
        // La descripción se recalcula visualmente desde los items:
        descripcionEventoField.setText(construirResumenDesdeEvento(evento));
        nombreClienteField.setText(evento.getNombre_cliente());
        telefonoClienteField.setText(evento.getTelefono_cliente());
        direccionEventoField.setText(evento.getDireccion_evento());
        fechaEventoPicker.setValue(evento.getFecha_evento());
        cantPersonasField.setText(String.valueOf(evento.getCant_personas()));
        presupuestoField.setText(evento.getPresupuesto().toString());
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        ActionLogger.log("Formulario de evento cerrado sin guardar.");
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    @FXML
    private void abrirCatalogoEventos(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pasteleria/CatalogoEvento.fxml"));
            Parent root = loader.load();

            catalogoEventoController = loader.getController();

            // 👇 Precargar cantidades si es edición
            if (eventoActual != null && eventoActual.getItems() != null) {
                Map<Long, Integer> preseleccion = eventoActual.getItems().stream()
                        .collect(Collectors.toMap(
                                ep -> ep.getProducto().getId(),
                                ep -> ep.getCantidad(),
                                Integer::sum // por si hay repetidos
                        ));
                catalogoEventoController.precargarSeleccionDesdeEvento(preseleccion);
            }

            Stage stage = new Stage();
            stage.setTitle("Catálogo de productos para el evento");
            stage.setScene(new Scene(root));
            stage.initOwner(((Node) e.getSource()).getScene().getWindow());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refrescar descripción visual
            descripcionEventoField.setText(construirResumenDesdeSeleccion());
            actualizarCamposDesdeSeleccionVisual();

        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo abrir el catálogo: " + ex.getMessage());
        }
    }

    private void aplicarSeleccionDeCatalogoAlEvento(Evento evento) {
        if (catalogoEventoController == null)
            return;

        Map<Producto, Integer> seleccion = catalogoEventoController.getProductosGuardados();
        if (seleccion == null)
            return;

        // indexar ítems actuales por productoId
        Map<Long, EventoProducto> actualesPorProdId = evento.getItems().stream()
                .collect(java.util.stream.Collectors.toMap(
                        ep -> ep.getProducto().getId(),
                        ep -> ep));

        // actualizar existentes y agregar nuevos
        for (Map.Entry<Producto, Integer> e : seleccion.entrySet()) {
            Producto prod = e.getKey();
            int nuevaCant = e.getValue();

            EventoProducto existente = actualesPorProdId.remove(prod.getId());
            if (existente != null) {
                // existe en el evento: si cambió la cantidad => resetear hecho
                if (existente.getCantidad() != nuevaCant) {
                    existente.setCantidad(nuevaCant);
                    existente.setHecho(false); // ❗ resetea solo si hubo cambio
                }
                // si NO cambió la cantidad, NO tocamos 'hecho'
            } else {
                // nuevo ítem => arrancar con hecho=false
                EventoProducto nuevo = new EventoProducto(evento, prod, nuevaCant, prod.getPrecio());
                nuevo.setHecho(false);
                evento.addItem(nuevo);
            }
        }

        // eliminar ítems que ya no están en la selección
        for (EventoProducto toRemove : actualesPorProdId.values()) {
            evento.removeItem(toRemove); // asumimos método de conveniencia + orphanRemoval=true
        }

        // recalcular presupuesto
        java.math.BigDecimal total = evento.getItems().stream()
                .map(model.EventoProducto::getSubtotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        evento.setPresupuesto(total);
        presupuestoField.setText(total.toPlainString());

        // actualizar descripción visual
        descripcionEventoField.setText(construirResumenDesdeEvento(evento));
    }

    // ===========================
    // Helpers de descripción
    // ===========================

    /**
     * Construye "2 Tortas, 2 Docenas de chipás" desde los productos seleccionados
     * en el catálogo.
     */
    private String construirResumenDesdeSeleccion() {
        if (catalogoEventoController == null)
            return "";
        Map<Producto, Integer> seleccion = catalogoEventoController.getProductosGuardados();
        if (seleccion == null || seleccion.isEmpty())
            return "";

        return seleccion.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getNombre(), String.CASE_INSENSITIVE_ORDER))
                .map(e -> e.getValue() + " " + e.getKey().getNombre())
                .collect(Collectors.joining(", "));
    }

    /** Construye la descripción a partir de los items ya asociados al Evento. */
    private String construirResumenDesdeEvento(Evento evento) {
        if (evento == null || evento.getItems() == null || evento.getItems().isEmpty()) {
            return "";
        }
        return evento.getItems().stream()
                .sorted(Comparator.comparing(ep -> ep.getProducto().getNombre(), String.CASE_INSENSITIVE_ORDER))
                .map(ep -> ep.getCantidad() + " " + ep.getProducto().getNombre())
                .collect(Collectors.joining(", "));
    }

    private BigDecimal calcularTotalDesdeSeleccion() {
        if (catalogoEventoController == null)
            return BigDecimal.ZERO;
        Map<Producto, Integer> sel = catalogoEventoController.getProductosGuardados();
        if (sel == null || sel.isEmpty())
            return BigDecimal.ZERO;

        return sel.entrySet().stream()
                .map(e -> e.getKey().getPrecio().multiply(BigDecimal.valueOf(e.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void actualizarCamposDesdeSeleccionVisual() {
        // descripción a partir de la selección actual
        String resumen = construirResumenDesdeSeleccion();
        if (resumen != null) {
            descripcionEventoField.setText(resumen);
        }
        // presupuesto visual (solo UI, sin tocar DB ni items del evento)
        BigDecimal total = calcularTotalDesdeSeleccion();
        presupuestoField.setText(total.toPlainString());
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

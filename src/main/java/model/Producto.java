    package model;

    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import java.math.BigDecimal;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Objects;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Entity
    @Table(name = "productos")
    public class Producto {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id_producto")
        private Long id;

        @Column(name = "nombre")
        private String nombre;

        @Column(name = "descripcion")
        private String descripcion;

        @ManyToOne
        @JoinColumn(name = "id_categoria")
        private Categoria categoria;

        @Column(name = "precio")
        private BigDecimal precio;

        @ManyToMany
        @JoinTable(
                name = "producto_sabor",
                joinColumns = @JoinColumn(name = "id_producto"),
                inverseJoinColumns = @JoinColumn(name = "id_sabor")
        )
        private List<Sabor> sabores = new ArrayList<>();

        @Lob
        @Column(name = "imagen")
        private byte[] imagen;

        @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
        @JoinColumn(name = "id_receta")
        private Receta receta;

        public Producto(String nombre, String descripcion, Categoria categoria, BigDecimal precio, byte[] imagen) {
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.categoria = categoria;
            this.precio = precio;
            this.imagen = imagen;
        }

        @Override
        public String toString() {
            return nombre;
        }

        public List<Sabor> getSabores() {
            return sabores;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Producto that = (Producto) obj;
            return Objects.equals(this.id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

    }

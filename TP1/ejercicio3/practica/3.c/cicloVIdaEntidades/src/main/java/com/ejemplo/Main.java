package com.ejemplo;

import jakarta.persistence.*;

public class Main {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("cicloVidaPU");
        EntityManager em = emf.createEntityManager();

        // ===== Estado NUEVO (Transient) =====
        Producto producto = new Producto("Laptop", 1200.0);
        System.out.println("Estado NUEVO (Transient): " + producto);

        // ===== De NUEVO a GESTIONADO (persist) =====
        em.getTransaction().begin();
        em.persist(producto); // ahora está gestionado
        em.getTransaction().commit();
        System.out.println("Estado GESTIONADO (Persistent): " + producto);

        // ===== Cambios en estado GESTIONADO =====
        em.getTransaction().begin();
        producto.setPrecio(1100.0); // JPA lo detecta automáticamente
        em.getTransaction().commit();
        System.out.println("Cambio en estado GESTIONADO: " + producto);

        // ===== De GESTIONADO a DESASOCIADO (Detached) =====
        em.close(); // cerramos el EntityManager
        System.out.println("Estado DESASOCIADO (Detached): " + producto);

        // ===== Cambios en Detached (NO se guardan) =====
        producto.setPrecio(999.0);
        System.out.println("Cambio en Detached (NO se guarda): " + producto);

        // ===== De DESASOCIADO a GESTIONADO (merge) =====
        EntityManager em2 = emf.createEntityManager();
        em2.getTransaction().begin();
        Producto productoGestionado = em2.merge(producto); // vuelve a gestionado
        em2.getTransaction().commit();
        System.out.println("Estado GESTIONADO otra vez (con merge): " + productoGestionado);

        // ===== Estado ELIMINADO (Removed) =====
        em2.getTransaction().begin();
        em2.remove(productoGestionado); // marcado para eliminar
        em2.getTransaction().commit();
        System.out.println("Estado ELIMINADO (Removed): producto eliminado de la base");

        em2.close();
        emf.close();
    }
}


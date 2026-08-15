package main;

import entidades.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

// Yo uso h2 asi que para la consola web necesito importar:
import org.h2.tools.Server;

import javax.lang.model.type.ArrayType;

public class PersistenceApp {
    public static void main(String[] args) {

        Server h2Server = null;
        try {
            //iniciar servidor web de la consola de h2
            h2Server = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
        } catch (Exception e) {
            System.out.println("Hubo un error al arrancar la base de datos:");
        }

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("MinimarketPU");
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            Factura factura1 = new Factura();

            factura1.setNumero(12);
            factura1.setFecha("14/8/26");

            Domicilio dom = new Domicilio("San Martin", 1200);
            Cliente cliente = new Cliente("Leandro", "Spadaro", 40000000, dom);
            dom.setCliente(cliente);

            factura1.setCliente(cliente);

            Categoria perecederos = new Categoria("perecederos");
            Categoria lacteos = new Categoria("lacteos");
            Categoria limpieza = new Categoria("limpieza");

            Articulo articulo1 = new Articulo(200, "Yogurt frutilla", 20);
            Articulo articulo2 = new Articulo(300, "Magistral", 40);

            articulo1.getCategorias().add(perecederos);
            articulo1.getCategorias().add(lacteos);
            articulo2.getCategorias().add(limpieza);

            lacteos.getArticulos().add(articulo1);
            perecederos.getArticulos().add(articulo1);
            limpieza.getArticulos().add(articulo2);

            DetalleFactura detalle1 = new DetalleFactura();
            detalle1.setArticulo(articulo1);
            detalle1.setCantidad(2);
            detalle1.setSubtotal(40);

            articulo1.getDetalle().add(detalle1);
            factura1.getDetalles().add(detalle1);
            detalle1.setFactura(factura1);

            DetalleFactura detalle2 = new DetalleFactura();

            detalle2.setArticulo(articulo2);
            detalle2.setCantidad(2);
            detalle2.setSubtotal(80);

            articulo2.getDetalle().add(detalle2);
            factura1.getDetalles().add(detalle2);
            detalle2.setFactura(factura1);

            factura1.setTotal(120);


            em.persist(factura1);

            Factura factura2 = em.find(Factura.class, 1L);
            factura2.setNumero(35);

            em.merge(factura2);


            //em.flush();

            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        }

        em.close();
        emf.close();
        //h2Server.stop();

//        Factura factura = Factura.builder().numero(15).fecha("15/8/26").build();
//        Factura factura2 = Factura.builder().total(200).fecha("15/8/26").build();
//
//        System.out.println(factura.toString());
//        System.out.println(factura2.toString());

    }
}

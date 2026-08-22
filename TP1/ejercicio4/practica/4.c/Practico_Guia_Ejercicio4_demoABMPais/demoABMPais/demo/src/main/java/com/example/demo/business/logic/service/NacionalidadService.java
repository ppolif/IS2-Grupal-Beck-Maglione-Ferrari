package com.example.demo.business.logic.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.business.domain.entity.Nacionalidad;
import com.example.demo.business.logic.error.ErrorServiceException;
import com.example.demo.business.persitence.repository.NacionalidadRepository;

import jakarta.persistence.NoResultException;

//@Service es una anotación propia de spring boot para dar soporte al modelo
// MVC. Indica que la clase es un componente de servicio
@Service
public class NacionalidadService {

    //Es la herramiento que nos da Spring Boot para dar soporte al patrón de
    //inyeccion de dependencias. esta anotacion se coloca sobre los atributos
    //que referencian a otras clases para que Spring inyecte la dependencia y así
    //no tener que crear el objeto nosotros mismos
	@Autowired
	private NacionalidadRepository repository; 
    
    public void validar(String nombre)throws ErrorServiceException {
        
        try{
            
            if (nombre == null || nombre.isEmpty()) {
                throw new ErrorServiceException("Debe indicar el nombre");
            }
            
        } catch (ErrorServiceException e) {
            throw e;
        } catch (Exception ex){
            ex.printStackTrace();
            throw new ErrorServiceException("Error de Sistemas");
        }
    }

    //esta anotación automatiza la tarea de abrir y cerrar transacciones con
    //las operaciones de JPA (begin y commit) cuando vamos a utilizar la base de datos.
    //al colocar esta anotación sobre un método, Spring hace esto automáticamente.
	@Transactional
    public void crearNacionalidad(String nombre) throws ErrorServiceException {

        try {
            
            validar(nombre);

            try {
            	Nacionalidad nacionalidad = repository.buscarNacionalidadPorNombre(nombre);
            	if (nacionalidad != null && !nacionalidad.isEliminado()) {
                 throw new ErrorServiceException("Existe una nacionalidad con el nombre indicado");
            	} 
            } catch (NoResultException ex) {}

            Nacionalidad nacionalidad = new Nacionalidad();
            nacionalidad.setId(UUID.randomUUID().toString());
            nacionalidad.setNombre(nombre);
            nacionalidad.setEliminado(false);

            //el método save propio de la interfaz JpaRepository (al que podemos acceder
            //mediante la interfaz NacionalidadRepository) permiten operar sobre la base de datos
            //mientras proveen abstracción, ya que evitan que se escriban consultas a la base de datos.
            //save es el método de JPA Respository para la persistencia de objetos.
            //es decir que guarda el objeto que tiene por argumento en la tabla correspondiente

            repository.save(nacionalidad);

        } catch (ErrorServiceException e) {
            throw e;
        } catch (Exception ex){
            ex.printStackTrace();
            throw new ErrorServiceException("Error de Sistemas");
        }
    }

    //vemos que todas las operaciones que modifiquen a la base de datos deben ser
    //transaccionales, para asegurar la integridad de los datos, por lo que es repsonsabilidad
    //del programador colocar la anotación sobre todo método que haga cambios en la base de datos
	@Transactional
    public void modificarNacionalidad(String idNacionalidad, String nombre) throws ErrorServiceException {

        try {

            Nacionalidad nacionalidad = buscarNacionalidad(idNacionalidad);

            validar(nombre);

            try{
                Nacionalidad NacionalidadExsitente = repository.buscarNacionalidadPorNombre(nombre);
                if (NacionalidadExsitente != null && !NacionalidadExsitente.getId().equals(idNacionalidad) && !NacionalidadExsitente.isEliminado()){
                  throw new ErrorServiceException("Existe una nacionalidad con el nombre indicado");  
                }
            } catch (NoResultException ex) {}

            nacionalidad.setNombre(nombre);
            nacionalidad.setEliminado(false);

            //el método save sirve tanto para insertar como para actuañizar
            //un objeto presente en la base de datos
            repository.save(nacionalidad);

        } catch (ErrorServiceException e) {
            throw e;
        } catch (Exception ex){
            ex.printStackTrace();
            throw new ErrorServiceException("Error de Sistemas");
        }
    }
	
	public Nacionalidad buscarNacionalidad(String id) throws ErrorServiceException {

        try {
            
            if (id == null || id.isEmpty()) {
                throw new ErrorServiceException("Debe indicar la nacionalidad");
            }

            //findById es un metodo de Jpa Repository para buscar elementos en la base de datps
            //por su id. El metodo devuelve Optional<E>, que es un contenedor que puede contener
            //un objeto de tipo E o null, en caso de que no se encuentre en ña base de datos
            //un objeto con el id especificado
            Optional<Nacionalidad> optional = repository.findById(id);
            Nacionalidad nacionalidad = null;
            if (optional.isPresent()) {
            	nacionalidad= optional.get();
    			if (nacionalidad.isEliminado()){
                    throw new ErrorServiceException("No se encuentra la nacionalidad indicada");
                }
    		}
            
            return nacionalidad;
            
        } catch (ErrorServiceException ex) {  
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ErrorServiceException("Error de sistema");
        }
    }

    //nuevamente esta anotación, ya que este método elimina un objeto de la base de datos
    @Transactional
    public void eliminarNacionalidad(String id) throws ErrorServiceException {

        try {

            Nacionalidad nacionalidad = buscarNacionalidad(id);
            nacionalidad.setEliminado(true);

            //en el caso de eliminar una nacionalidad nuevamente usamos save, ya que
            //implementamos borrado lógico y por lo tanto queremos actualizar el valor
            //del atributo eliminado a true
            repository.save(nacionalidad);

        } catch (ErrorServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ErrorServiceException("Error de sistema");
        }

    }

    public Collection<Nacionalidad> listarNacionalidad() throws ErrorServiceException {
        try {

            //el método findAll de Jpa Repository devuelve una lista como todos los objetos
            // de tipo nacionalidad en este caso.
            return repository.findAll();

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ErrorServiceException("Error de sistema");
        }
    }
    
    public List<Nacionalidad> listarNacionalidadActivo() throws ErrorServiceException {
        try {
            
            return repository.listarNacionalidadActivo();

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ErrorServiceException("Error de sistema");
        }
    }
}

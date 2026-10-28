package com.DulceMayor.backend.config;

import com.DulceMayor.backend.entities.RolUsuario;
import com.DulceMayor.backend.entities.Usuario;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Adaptador de la entidad Usuario al contrato UserDetails de Spring Security.
 */
public class UsuarioPrincipal implements UserDetails {

    private final String id;
    private final String nombreUsuario;
    private final String clave;
    private final RolUsuario rol;
    private final boolean claveTemporal;
    private final boolean eliminado;

    public UsuarioPrincipal(Usuario usuario) {
        this.id = usuario.getId();
        this.nombreUsuario = usuario.getNombreUsuario();
        this.clave = usuario.getClave();
        this.rol = usuario.getRol();
        this.claveTemporal = usuario.isClaveTemporal();
        this.eliminado = usuario.isEliminado();
    }

    public String getId() {
        return id;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public boolean isClaveTemporal() {
        return claveTemporal;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return clave;
    }

    @Override
    public String getUsername() {
        return nombreUsuario;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !eliminado;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !eliminado;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !eliminado;
    }
}


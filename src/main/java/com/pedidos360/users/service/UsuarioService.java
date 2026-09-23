package com.pedidos360.users.service;

import com.pedidos360.users.dto.UsuarioDto;
import com.pedidos360.users.entity.Usuario;

import java.util.List;

public interface UsuarioService {

    List<Usuario> listarUsuarios();

    Usuario crearUsuario(UsuarioDto usuarioDto);
}
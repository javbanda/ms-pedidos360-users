package com.pedidos360.users.controller;

import com.pedidos360.users.dto.LoginRequestDto;
import com.pedidos360.users.dto.LoginResponseDto;
import com.pedidos360.users.dto.UsuarioDto;
import com.pedidos360.users.entity.Usuario;
import com.pedidos360.users.service.UsuarioService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @PostMapping
    public ResponseEntity<Usuario> crearUsuario(@RequestBody UsuarioDto usuarioDto) {

        Usuario usuarioCreado = usuarioService.crearUsuario(usuarioDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioCreado);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @RequestBody LoginRequestDto loginRequest) {

        LoginResponseDto respuesta = usuarioService.login(loginRequest);

        return ResponseEntity.ok(respuesta);
    }
}
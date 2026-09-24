package com.pedidos360.users.service;

import com.pedidos360.users.dto.LoginRequestDto;
import com.pedidos360.users.dto.LoginResponseDto;
import com.pedidos360.users.dto.UsuarioDto;
import com.pedidos360.users.entity.Usuario;
import com.pedidos360.users.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CognitoIdentityProviderClient cognitoClient;

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${aws.cognito.clientId}")
    private String clientId;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            CognitoIdentityProviderClient cognitoClient) {

        this.usuarioRepository = usuarioRepository;
        this.cognitoClient = cognitoClient;
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario crearUsuario(UsuarioDto usuarioDto) {

        // 1. Crear usuario en AWS Cognito
        AdminCreateUserRequest createRequest = AdminCreateUserRequest.builder()
                .userPoolId(userPoolId)
                .username(usuarioDto.getEmail())
                .temporaryPassword(usuarioDto.getPassword())
                .userAttributes(
                        AttributeType.builder()
                                .name("email")
                                .value(usuarioDto.getEmail())
                                .build(),
                        AttributeType.builder()
                                .name("email_verified")
                                .value("true")
                                .build()
                )
                .messageAction("SUPPRESS")
                .build();

        AdminCreateUserResponse createResponse =
                cognitoClient.adminCreateUser(createRequest);

        // 2. Obtener el SUB generado por Cognito
        String cognitoSub = createResponse.user()
                .attributes()
                .stream()
                .filter(attribute -> attribute.name().equals("sub"))
                .map(AttributeType::value)
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("No se pudo obtener el sub de Cognito"));

        // 3. Agregar usuario al grupo indicado en el DTO
        AdminAddUserToGroupRequest groupRequest =
                AdminAddUserToGroupRequest.builder()
                        .userPoolId(userPoolId)
                        .username(usuarioDto.getEmail())
                        .groupName(usuarioDto.getRol())
                        .build();

        cognitoClient.adminAddUserToGroup(groupRequest);

        // 4. Guardar usuario en PostgreSQL
        Usuario usuario = new Usuario();

        usuario.setCognitoSub(cognitoSub);
        usuario.setNombre(usuarioDto.getNombre());
        usuario.setEmail(usuarioDto.getEmail());
        usuario.setRol(usuarioDto.getRol());

        return usuarioRepository.save(usuario);
    }

    @Override
    public LoginResponseDto login(LoginRequestDto loginRequest) {

        Map<String, String> authParameters = new HashMap<>();

        authParameters.put("USERNAME", loginRequest.getEmail());
        authParameters.put("PASSWORD", loginRequest.getPassword());

        AdminInitiateAuthRequest authRequest =
                AdminInitiateAuthRequest.builder()
                        .userPoolId(userPoolId)
                        .clientId(clientId)
                        .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                        .authParameters(authParameters)
                        .build();

        AdminInitiateAuthResponse authResponse =
                cognitoClient.adminInitiateAuth(authRequest);

        if (authResponse.authenticationResult() == null) {
            throw new RuntimeException(
                    "Cognito requiere completar un desafío antes de iniciar sesión");
        }

        return new LoginResponseDto(
                authResponse.authenticationResult().accessToken(),
                authResponse.authenticationResult().idToken(),
                authResponse.authenticationResult().refreshToken(),
                authResponse.authenticationResult().expiresIn(),
                authResponse.authenticationResult().tokenType()
        );
    }
}
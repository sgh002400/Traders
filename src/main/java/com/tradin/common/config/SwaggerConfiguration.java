package com.tradin.common.config;

import com.tradin.common.annotation.DisableAuthInSwagger;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.Collections;
import java.util.List;

@Configuration
public class SwaggerConfiguration {
    @Bean
    public OpenAPI openApi() {
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("Access Token");

        return new OpenAPI()
                .addServersItem(new Server().url("/"))
                .info(new Info()
                        .title("Tradin API")
                        .description("자물쇠가 있는 API는 요청 헤더에 Key: Authorization, Value: Bearer {token}을 포함해야 합니다."))
                .components(new Components()
                        .addSecuritySchemes("Access Token",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")))
                .security(List.of(securityRequirement));
    }

    @Bean
    public OperationCustomizer customize() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            DisableAuthInSwagger methodAnnotation =
                    handlerMethod.getMethodAnnotation(DisableAuthInSwagger.class);

            if (methodAnnotation != null) {
                operation.setSecurity(Collections.emptyList());
            }
            return operation;
        };
    }
}

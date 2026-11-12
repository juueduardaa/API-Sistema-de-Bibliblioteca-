package com.senac.tsi.MinhaPrimeiraApi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bibliotecaOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Sistema de Biblioteca - API REST")
                .version("1.0.0")
                .description("API para gerenciar leitores, carteirinhas, livros, autores, categorias e empréstimos.Todas as listagens são paginadas (page, size, sort) e as respostas utilizam os códigos HTTP adequados.<br><br>"
                        + "TERMOS DE USO: o sistema permite cadastrar novos registros, consultar as informações já existentes, atualizar dados quando necessário e excluir registros que não precisam mais ser mantidos. Dessa forma, facilita a organização, a consulta e o gerenciamento das informações de maneira simples e prática.<br><br>"
                        + "FUNCIONAMENTO: Permite cadastrar, consultar, atualizar e excluir registros <br><br>"
                        + "CONTATO: Caso necessario procure por meio do Email: julia.efmonteiro@senacsp.edu.br")
                .contact(new Contact()
                        .name("Projeto API - TSI")));
    }
}

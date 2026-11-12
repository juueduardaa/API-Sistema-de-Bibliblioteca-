package com.senac.tsi.MinhaPrimeiraApi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Testes de integração: sobem a aplicação inteira numa porta aleatória e chamam a API de verdade
 * (usando apenas o HttpClient do JDK). Servem para provar que o Swagger e o fluxo principal funcionam.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "app.seed.enabled=false")
class BibliotecaApiIntegrationTests {

    @Value("${local.server.port}")
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        HttpRequest.BodyPublisher publisher = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .method(method, publisher)
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private long idDe(HttpResponse<String> response) {
        Matcher m = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(response.body());
        assertTrue(m.find(), "Resposta sem id: " + response.body());
        return Long.parseLong(m.group(1));
    }

    @Test
    void swaggerEOpenApiEstaoDisponiveis() throws Exception {
        HttpResponse<String> docs = send("GET", "/v3/api-docs", null);
        assertEquals(200, docs.statusCode());
        for (String rota : new String[] {"/api/leitores", "/api/carteirinhas", "/api/livros",
                "/api/autores", "/api/categorias", "/api/emprestimos"}) {
            assertTrue(docs.body().contains(rota), "Rota ausente na documentação: " + rota);
        }
        assertEquals(200, send("GET", "/swagger-ui/index.html", null).statusCode());
        assertEquals(200, send("GET", "/api", null).statusCode());
    }

    @Test
    void fluxoCompletoDaBiblioteca() throws Exception {
        HttpResponse<String> categoria = send("POST", "/api/categorias",
                "{\"nome\":\"Teste Ficcao\",\"descricao\":\"Categoria de teste\"}");
        assertEquals(201, categoria.statusCode(), categoria.body());
        assertTrue(categoria.body().contains("_links"), "Resposta sem links HATEOAS");
        long categoriaId = idDe(categoria);

        HttpResponse<String> autor = send("POST", "/api/autores",
                "{\"nome\":\"Autora de Teste\",\"nacionalidade\":\"Brasileira\"}");
        assertEquals(201, autor.statusCode(), autor.body());
        long autorId = idDe(autor);

        HttpResponse<String> leitor = send("POST", "/api/leitores",
                "{\"nome\":\"Leitor Teste\",\"email\":\"leitor.teste@example.com\",\"telefone\":\"11999999999\"}");
        assertEquals(201, leitor.statusCode(), leitor.body());
        long leitorId = idDe(leitor);

        // e-mail duplicado -> 409
        assertEquals(409, send("POST", "/api/leitores",
                "{\"nome\":\"Outro\",\"email\":\"leitor.teste@example.com\"}").statusCode());
        // validacao -> 400
        assertEquals(400, send("POST", "/api/leitores", "{\"nome\":\"\",\"email\":\"invalido\"}").statusCode());

        HttpResponse<String> livro = send("POST", "/api/livros",
                "{\"titulo\":\"Livro de Teste\",\"isbn\":\"978-1-23456-789-7\",\"anoPublicacao\":2020,"
                        + "\"categoriaId\":" + categoriaId + ",\"autoresIds\":[" + autorId + "]}");
        assertEquals(201, livro.statusCode(), livro.body());
        long livroId = idDe(livro);

        String emprestimoJson = "{\"dataEmprestimo\":\"2026-10-11\",\"dataPrevistaDevolucao\":\"2026-10-25\","
                + "\"leitorId\":" + leitorId + ",\"livroId\":" + livroId + "}";

        // sem carteirinha -> 409
        assertEquals(409, send("POST", "/api/emprestimos", emprestimoJson).statusCode());

        HttpResponse<String> carteirinha = send("POST", "/api/carteirinhas",
                "{\"codigo\":\"TST-0001\",\"dataEmissao\":\"2026-10-01\",\"ativa\":true,\"leitorId\":" + leitorId + "}");
        assertEquals(201, carteirinha.statusCode(), carteirinha.body());

        // segunda carteirinha para o mesmo leitor -> 409 (one-to-one)
        assertEquals(409, send("POST", "/api/carteirinhas",
                "{\"codigo\":\"TST-0002\",\"dataEmissao\":\"2026-10-01\",\"ativa\":true,\"leitorId\":" + leitorId + "}").statusCode());

        HttpResponse<String> emprestimo = send("POST", "/api/emprestimos", emprestimoJson);
        assertEquals(201, emprestimo.statusCode(), emprestimo.body());
        assertTrue(emprestimo.body().contains("\"ATIVO\""));
        long emprestimoId = idDe(emprestimo);

        // livro agora indisponivel -> novo emprestimo = 409
        assertEquals(409, send("POST", "/api/emprestimos", emprestimoJson).statusCode());
        assertTrue(send("GET", "/api/livros/" + livroId, null).body().contains("\"disponivel\":false"));

        // listagens paginadas e buscas personalizadas
        assertEquals(200, send("GET", "/api/livros?page=0&size=5&sort=titulo,asc", null).statusCode());
        HttpResponse<String> busca = send("GET", "/api/livros/buscar?titulo=teste", null);
        assertEquals(200, busca.statusCode());
        assertTrue(busca.body().contains("Livro de Teste"));
        assertEquals(200, send("GET", "/api/emprestimos/buscar?status=ATIVO", null).statusCode());
        assertEquals(400, send("GET", "/api/emprestimos/buscar?status=INEXISTENTE", null).statusCode());

        // devolucao libera o livro
        assertEquals(200, send("POST", "/api/emprestimos/" + emprestimoId + "/devolucao", null).statusCode());
        assertTrue(send("GET", "/api/livros/" + livroId, null).body().contains("\"disponivel\":true"));
        assertEquals(409, send("POST", "/api/emprestimos/" + emprestimoId + "/devolucao", null).statusCode());

        // 404 e exclusao protegida
        assertEquals(404, send("GET", "/api/livros/999999", null).statusCode());
        assertEquals(409, send("DELETE", "/api/categorias/" + categoriaId, null).statusCode());
        assertEquals(204, send("DELETE", "/api/emprestimos/" + emprestimoId, null).statusCode());
        assertEquals(204, send("DELETE", "/api/livros/" + livroId, null).statusCode());
        assertEquals(204, send("DELETE", "/api/categorias/" + categoriaId, null).statusCode());
    }
}

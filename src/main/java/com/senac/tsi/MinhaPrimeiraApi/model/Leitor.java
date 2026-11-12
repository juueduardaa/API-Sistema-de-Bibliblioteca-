package com.senac.tsi.MinhaPrimeiraApi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Leitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank
    @Email
    @Size(max = 150)
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Size(max = 20)
    @Pattern(regexp = "^\\+?[0-9()\\- ]{8,20}$", message = "telefone deve conter de 8 a 20 dígitos, espaços, parênteses ou hífen")
    @Column(length = 20)
    private String telefone;

    /** One-to-One (lado inverso): a carteirinha guarda a chave estrangeira. */
    @OneToOne(mappedBy = "leitor")
    private Carteirinha carteirinha;

    /** One-to-Many: um leitor realiza vários empréstimos. */
    @OneToMany(mappedBy = "leitor")
    private List<Emprestimo> emprestimos = new ArrayList<>();

    public Leitor() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public Carteirinha getCarteirinha() { return carteirinha; }
    public void setCarteirinha(Carteirinha carteirinha) { this.carteirinha = carteirinha; }
    public List<Emprestimo> getEmprestimos() { return emprestimos; }
    public void setEmprestimos(List<Emprestimo> emprestimos) { this.emprestimos = emprestimos; }
}

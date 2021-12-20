package br.com.fatec.projetoOrdensDeServicos.entity;

public class Cliente {
    private String nome;
    private String email;
    private String telefone;
    private String statusConta;

    public Cliente() {
    }

    public Cliente(String nome, String email, String telefone, String statusConta) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.statusConta = statusConta;
    }

    public Cliente(String nome, String email, String telefone) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
    }

    public void setStatusConta(String statusConta) {
        this.statusConta = statusConta;
    }

    public String getStatusConta() {
        return statusConta;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

}

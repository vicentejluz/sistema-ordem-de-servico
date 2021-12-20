package br.com.fatec.projetoOrdensDeServicos.entity;

import com.google.firebase.Timestamp;

public class OrdemServico {
    private String nomeServico;
    private String descricao;
    private String status;
    private Double preco;
    private Timestamp dataAbertura;
    private Timestamp datafinalizacao;

    public OrdemServico() {
    }

    public OrdemServico(String nomeServico, String descricao, Double preco) {
        this.nomeServico = nomeServico;
        this.descricao = descricao;
        this.preco = preco;
    }

    public OrdemServico(String nomeServico, String descricao, Double preco,
                        Timestamp dataAbertura, Timestamp datafinalizacao, String status) {
        this.nomeServico = nomeServico;
        this.descricao = descricao;
        this.preco = preco;
        this.dataAbertura = dataAbertura;
        this.datafinalizacao = datafinalizacao;
        this.status = status;
    }


    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }

    public String getNomeServico() {
        return nomeServico;
    }

    public String getDescricao() {
        return descricao;
    }

    public Double getPreco() {
        return preco;
    }

    public Timestamp getDataAbertura() {
        return dataAbertura;
    }

    public Timestamp getDatafinalizacao() {
        return datafinalizacao;
    }

    public String getStatus() {
        return status;
    }

}

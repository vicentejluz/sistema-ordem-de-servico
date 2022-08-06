package br.com.fatec.projetoOrdensDeServicos.entity;

import com.google.firebase.Timestamp;

public class OrdemServico {
    private String nomeServico;
    private String descricao;
    private StatusOrdemServico status;
    private Double preco;
    private Timestamp dataAbertura;
    private Timestamp datafinalizacao;

    public OrdemServico() {
    }

    public OrdemServico(String nomeServico, String descricao, StatusOrdemServico status) {
        this.nomeServico = nomeServico;
        this.descricao = descricao;
        this.status = status;
    }

    public OrdemServico(String nomeServico, String descricao, Double preco,
                        Timestamp dataAbertura, Timestamp datafinalizacao, StatusOrdemServico status) {
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

    public StatusOrdemServico getStatus() {
        return status;
    }

}

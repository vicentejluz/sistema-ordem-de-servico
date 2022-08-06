package br.com.fatec.projetoOrdensDeServicos.entity;

import com.google.firebase.Timestamp;

public class Comentario {
    private Timestamp dataEnvio;
    private String descricao;
    private String fromId;
    private String toId;

    public Comentario() {
    }

    public Comentario(Timestamp dataEnvio, String descricao, String fromId, String toId) {
        this.dataEnvio = dataEnvio;
        this.descricao = descricao;
        this.fromId = fromId;
        this.toId = toId;
    }

    public Timestamp getDataEnvio() {
        return dataEnvio;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getFromId() {
        return fromId;
    }

    public String getToId() {
        return toId;
    }
}

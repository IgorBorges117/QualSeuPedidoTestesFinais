package br.com.qualseupedido.entidade;

import java.io.Serializable;

public class Pedido implements Serializable {

    private Long id;
    private String descricao;
    private String status; // PENDENTE, ACEITO, RECUSADO

    public Pedido() {}

    public Pedido(Long id, String descricao) {
        this.id = id;
        this.descricao = descricao;
        this.status = "PENDENTE";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
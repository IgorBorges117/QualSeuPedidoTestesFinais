package br.com.qualseupedido.entidade;

import java.io.Serializable;

public class Prato implements Serializable {

    private Long id;
    private Long chefId;
    private String nome;
    private String descricao;
    private Double preco;
    private String fotoDataUrl;
    private boolean visivelNoPerfilPublico;

    public Prato() {
        this.visivelNoPerfilPublico = true;
    }

    public Prato(String nome, String descricao, Double preco) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.visivelNoPerfilPublico = true;
    }

    public Prato(Long id, Long chefId, String nome, String descricao, Double preco) {
        this(id, chefId, nome, descricao, preco, null, true);
    }

    public Prato(Long id, Long chefId, String nome, String descricao, Double preco, String fotoDataUrl) {
        this(id, chefId, nome, descricao, preco, fotoDataUrl, true);
    }

    public Prato(Long id,
                 Long chefId,
                 String nome,
                 String descricao,
                 Double preco,
                 String fotoDataUrl,
                 boolean visivelNoPerfilPublico) {
        this.id = id;
        this.chefId = chefId;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.fotoDataUrl = fotoDataUrl;
        this.visivelNoPerfilPublico = visivelNoPerfilPublico;
    }

    public Long getId() {
        return id;
    }

    public Long getChefId() {
        return chefId;
    }

    public void setChefId(Long chefId) {
        this.chefId = chefId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }

    public String getFotoDataUrl() {
        return fotoDataUrl;
    }

    public void setFotoDataUrl(String fotoDataUrl) {
        this.fotoDataUrl = fotoDataUrl;
    }

    public boolean isVisivelNoPerfilPublico() {
        return visivelNoPerfilPublico;
    }

    public void setVisivelNoPerfilPublico(boolean visivelNoPerfilPublico) {
        this.visivelNoPerfilPublico = visivelNoPerfilPublico;
    }
}
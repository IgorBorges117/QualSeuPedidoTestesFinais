package br.com.qualseupedido.entidade;

public class PedidoItem {

    private String nome;
    private double preco;

    public PedidoItem(String nome, double preco) {
        this.nome = nome;
        this.preco = preco;
    }

    public String getNome() { return nome; }
    public double getPreco() { return preco; }
}

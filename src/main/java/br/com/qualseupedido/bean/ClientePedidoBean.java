package br.com.qualseupedido.beans;

import br.com.qualseupedido.bean.PedidoBean;
import br.com.qualseupedido.entidade.Pedido;
import br.com.qualseupedido.entidade.PedidoItem;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@SessionScoped
public class ClientePedidoBean implements Serializable {

    private List<PedidoItem> carrinho = new ArrayList<>();
    private Long pedidoId; // acompanha o pedido enviado

    @Inject
    private PedidoBean pedidoBean;

    public void adicionar(String nome, double preco) {
        carrinho.add(new PedidoItem(nome, preco));
    }

    public List<PedidoItem> getCarrinho() {
        return carrinho;
    }

    public double getTotal() {
        double total = 0;
        for (PedidoItem i : carrinho) total += i.getPreco();
        return total;
    }

    public String enviarPedido() {
        if (carrinho == null || carrinho.isEmpty()) return null;

        StringBuilder desc = new StringBuilder("Itens: ");
        for (PedidoItem item : carrinho) {
            desc.append(item.getNome()).append(" (R$ ").append(item.getPreco()).append("), ");
        }
        desc.append("Total: R$ ").append(getTotal());

        Pedido p = pedidoBean.adicionarPedido(desc.toString());
        pedidoId = p.getId();

        carrinho.clear();
        return null;
    }

    public Pedido getPedidoAtual() {
        return pedidoBean.buscarPorId(pedidoId);
    }

    // botão "Atualizar status" só precisa fazer postback
    public String atualizarStatus() {
        return null;
    }

    public boolean isPedidoAceito() {
        Pedido p = getPedidoAtual();
        return p != null && "ACEITO".equals(p.getStatus());
    }

    public boolean isPedidoEnviado() {
        return pedidoId != null;
    }
}
package br.com.qualseupedido.bean;

import br.com.qualseupedido.entidade.Pedido;

import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@ApplicationScoped
public class PedidoBean implements Serializable {

    private final List<Pedido> pedidos = new ArrayList<>();
    private long seq = 1;

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public Pedido adicionarPedido(String descricao) {
        Pedido p = new Pedido(seq++, descricao);
        pedidos.add(p);
        return p;
    }

    public Pedido buscarPorId(Long id) {
        if (id == null) return null;
        for (Pedido p : pedidos) {
            if (id.equals(p.getId())) return p;
        }
        return null;
    }

    public void aceitarPedido(Pedido pedido) {
        pedido.setStatus("ACEITO");
    }

    public void recusarPedido(Pedido pedido) {
        pedido.setStatus("RECUSADO");
    }
}
package br.com.qualseupedido.bean;

import br.com.qualseupedido.entidade.Prato;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Named
@SessionScoped
public class PagamentoBean implements Serializable {

    public enum StatusPagamento {
        PENDENTE,
        CONFIRMADO,
        FALHA
    }

    private Long chefId;
    private String chefNome;
    private Long clienteId;
    private String clienteNome;
    private List<Prato> pratos = new ArrayList<>();
    private String dataEvento;
    private String horarioEvento;
    private Integer quantidadePessoasEvento;
    private Double totalPedido;
    private boolean pagamentoPendente;
    private StatusPagamento statusPagamento;

    @Inject
    private SolicitacaoServicoBean solicitacaoServicoBean;
    @Inject
    private AuthBean authBean;

    public void iniciarPagamento(Long chefId,
                                 String chefNome,
                                 Long clienteId,
                                 String clienteNome,
                                 List<Prato> pratosSelecionados,
                                 String dataEvento,
                                 String horarioEvento,
                                 Integer quantidadePessoasEvento) {
        this.chefId = chefId;
        this.chefNome = chefNome;
        this.clienteId = clienteId;
        this.clienteNome = clienteNome;
        this.pratos = pratosSelecionados == null ? new ArrayList<>() : new ArrayList<>(pratosSelecionados);
        this.dataEvento = dataEvento;
        this.horarioEvento = horarioEvento;
        this.quantidadePessoasEvento = quantidadePessoasEvento;
        this.totalPedido = calcularTotal(this.pratos, quantidadePessoasEvento);
        this.pagamentoPendente = true;
        this.statusPagamento = StatusPagamento.PENDENTE;
    }

    public String confirmarPagamento() {
        if (!pagamentoPendente || chefId == null || clienteId == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Pagamento invalido", "Nenhum pedido pendente encontrado."));
            return "/pages/cliente-home.xhtml?faces-redirect=true";
        }
        if (statusPagamento != StatusPagamento.PENDENTE) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Pagamento nao autorizado", "O pagamento nao esta pendente."));
            return null;
        }
        if (!authBean.isCliente() || authBean.getUsuarioLogado() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Acesso negado", "Faca login como cliente para continuar."));
            return "/pages/login.xhtml?faces-redirect=true";
        }
        if (!clienteId.equals(authBean.getUsuarioLogado().getId())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Sessao divergente", "O pedido nao pertence ao usuario atual."));
            return "/pages/cliente-home.xhtml?faces-redirect=true";
        }

        Long chefIdRetorno = chefId;
        solicitacaoServicoBean.criarSolicitacao(
                chefId,
                chefNome,
                clienteId,
                clienteNome,
                pratos,
                dataEvento,
                horarioEvento,
                quantidadePessoasEvento
        );

        statusPagamento = StatusPagamento.CONFIRMADO;
        limparPedido();
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Pagamento confirmado", "O pedido foi enviado ao chef."));

        return "/pages/chef-perfil-publico.xhtml?faces-redirect=true&chefId=" + chefIdRetorno;
    }

    public String cancelarPagamento() {
        Long chef = chefId;
        limparPedido();
        if (chef != null) {
            return "/pages/chef-perfil-publico.xhtml?faces-redirect=true&chefId=" + chef;
        }
        return "/pages/cliente-home.xhtml?faces-redirect=true";
    }

    public String simularFalhaPagamento() {
        if (!pagamentoPendente) {
            return "/pages/cliente-home.xhtml?faces-redirect=true";
        }
        statusPagamento = StatusPagamento.FALHA;
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Pagamento falhou", "Simulacao de falha aplicada."));
        return null;
    }

    public String simularPendenciaPagamento() {
        if (!pagamentoPendente) {
            return "/pages/cliente-home.xhtml?faces-redirect=true";
        }
        statusPagamento = StatusPagamento.PENDENTE;
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Pagamento pendente", "Pagamento em analise."));
        return null;
    }

    private void limparPedido() {
        chefId = null;
        chefNome = null;
        clienteId = null;
        clienteNome = null;
        pratos = new ArrayList<>();
        dataEvento = null;
        horarioEvento = null;
        quantidadePessoasEvento = null;
        totalPedido = null;
        pagamentoPendente = false;
        statusPagamento = null;
    }

    private Double calcularTotal(List<Prato> itens, Integer quantidadePessoas) {
        double total = 0;
        if (itens != null) {
            for (Prato prato : itens) {
                if (prato != null && prato.getPreco() != null) {
                    total += prato.getPreco();
                }
            }
        }
        int pessoas = quantidadePessoas != null && quantidadePessoas > 0 ? quantidadePessoas : 1;
        return total * pessoas;
    }

    public boolean isPagamentoPendente() {
        return pagamentoPendente;
    }

    public boolean isStatusPendente() {
        return statusPagamento == StatusPagamento.PENDENTE;
    }

    public boolean isStatusFalha() {
        return statusPagamento == StatusPagamento.FALHA;
    }

    public boolean isStatusConfirmado() {
        return statusPagamento == StatusPagamento.CONFIRMADO;
    }

    public String getStatusPagamentoLabel() {
        if (statusPagamento == null) {
            return "Nao informado";
        }
        switch (statusPagamento) {
            case CONFIRMADO:
                return "Pagamento confirmado";
            case FALHA:
                return "Falha no pagamento";
            default:
                return "Pagamento pendente";
        }
    }

    public List<Prato> getPratos() {
        return Collections.unmodifiableList(pratos);
    }

    public String getChefNome() {
        return chefNome;
    }

    public String getDataEvento() {
        return dataEvento;
    }

    public String getHorarioEvento() {
        return horarioEvento;
    }

    public Integer getQuantidadePessoasEvento() {
        return quantidadePessoasEvento;
    }

    public Double getTotalPedido() {
        return totalPedido;
    }
}

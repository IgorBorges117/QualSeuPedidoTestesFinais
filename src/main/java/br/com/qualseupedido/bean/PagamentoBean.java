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
    @Inject
    private UsuarioBean usuarioBean;

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
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Pagamento inválido", "Nenhum pedido pendente encontrado."));
            return "/pages/cliente-home.xhtml?faces-redirect=true";
        }
        if (statusPagamento != StatusPagamento.PENDENTE) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Pagamento não autorizado", "O pagamento não está pendente."));
            return null;
        }
        if (!authBean.isCliente() || authBean.getUsuarioLogado() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Acesso negado", "Faca login como cliente para continuar."));
            return "/pages/login.xhtml?faces-redirect=true";
        }
        if (!clienteId.equals(authBean.getUsuarioLogado().getId())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Sessão divergente", "O pedido não pertence ao usuário atual."));
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
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Pagamento confirmado", "Pedido enviado ao chef. Acesse seu perfil para continuar a negociação."));

        return "/pages/cliente-perfil.xhtml?faces-redirect=true";
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
            return "Não informado";
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

    public String getChefFotoDataUrl() {
        if (chefId == null || usuarioBean == null) {
            return null;
        }
        UsuarioSistema chef = usuarioBean.buscarPorId(chefId);
        if (chef == null) {
            return null;
        }
        return chef.getFotoPerfilDataUrl();
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

    public int getQuantidadePessoasExibicao() {
        return quantidadePessoasEvento != null && quantidadePessoasEvento > 0 ? quantidadePessoasEvento : 1;
    }

    public Double getTotalPedido() {
        return totalPedido;
    }

    public Double totalItem(Prato prato) {
        if (prato == null || prato.getPreco() == null) {
            return 0.0;
        }
        return prato.getPreco() * getQuantidadePessoasExibicao();
    }
}

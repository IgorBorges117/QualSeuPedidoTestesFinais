package br.com.qualseupedido.bean;

import br.com.qualseupedido.entidade.Prato;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Named
@ViewScoped
public class ChefPerfilPublicoBean implements Serializable {

    private Long chefId;
    private String mensagemCliente;

    @Inject
    private UsuarioBean usuarioBean;
    @Inject
    private AuthBean authBean;
    @Inject
    private ChefConteudoBean chefConteudoBean;
    @Inject
    private PratoBean pratoBean;
    @Inject
    private SolicitacaoServicoBean solicitacaoServicoBean;

    private String[] pratosSelecionados;
    private String pratosSelecionadosTexto;
    private String dataEvento;
    private String horarioEvento;
    private String observacoes;

    public UsuarioSistema getChefSelecionado() {
        Long id = getChefId();
        if (id == null) {
            return null;
        }
        return usuarioBean.buscarPorId(id);
    }

    public List<ChefConteudoBean.PostagemChef> getPostagensChef() {
        UsuarioSistema chef = getChefSelecionado();
        if (chef == null) {
            return Collections.emptyList();
        }
        return chefConteudoBean.listarPostagensChef(chef.getId());
    }

    public List<ChefConteudoBean.MensagemChef> getHistoricoConversa() {
        UsuarioSistema chef = getChefSelecionado();
        if (chef == null || authBean.getUsuarioLogado() == null) {
            return Collections.emptyList();
        }
        return chefConteudoBean.listarConversaClienteChef(chef.getId(), authBean.getUsuarioLogado().getId());
    }

    public List<Prato> getPratosChefSelecionado() {
        UsuarioSistema chef = getChefSelecionado();
        if (chef == null) {
            return Collections.emptyList();
        }
        return pratoBean.getPratosPublicosDoChef(chef.getId());
    }

    public List<Prato> getPratosChefSelecionadoPorCategoria(String categoria) {
        UsuarioSistema chef = getChefSelecionado();
        if (chef == null) {
            return Collections.emptyList();
        }
        return pratoBean.getPratosPublicosDoChefPorCategoria(chef.getId(), categoria);
    }

    public List<Prato> pratosChefSelecionadoPorCategoria(String categoria) {
        return getPratosChefSelecionadoPorCategoria(categoria);
    }

    public boolean pratoSelecionado(Long pratoId) {
        if (pratoId == null) {
            return false;
        }
        for (Long id : idsPratosSelecionados()) {
            if (pratoId.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public String solicitarMenu() {
        UsuarioSistema chef = getChefSelecionado();
        if (chef == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Chef nao encontrado", "Selecione um chef valido."));
            return null;
        }
        if (!authBean.isCliente()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Acesso negado", "Apenas clientes podem enviar solicitacoes."));
            return null;
        }

        List<Long> idsSelecionados = idsPratosSelecionados();
        if (idsSelecionados.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Pratos obrigatorios", "Selecione pelo menos um prato."));
            return null;
        }
        if (dataEvento == null || dataEvento.trim().isEmpty() || horarioEvento == null || horarioEvento.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Data e horario obrigatorios", "Informe a data e o horario do evento."));
            return null;
        }

        List<Prato> pratos = new ArrayList<>();
        for (Long id : idsSelecionados) {
            Prato prato = pratoBean.buscarPublicoDoChefPorId(chef.getId(), id);
            if (prato != null) {
                pratos.add(prato);
            }
        }
        if (pratos.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Pratos invalidos", "Selecione pratos validos do chef."));
            return null;
        }

        solicitacaoServicoBean.criarSolicitacao(
                chef.getId(),
                chef.getNome(),
                authBean.getUsuarioLogado().getId(),
                authBean.getUsuarioLogado().getNome(),
                pratos,
                dataEvento,
                horarioEvento,
                observacoes
        );

        pratosSelecionados = null;
        pratosSelecionadosTexto = "";
        dataEvento = "";
        horarioEvento = "";
        observacoes = "";

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Solicitacao enviada", "O chef recebera seu pedido para avaliacao."));
        return null;
    }

    public String enviarMensagem() {
        UsuarioSistema chef = getChefSelecionado();
        if (chef == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Chef nao encontrado", "Selecione um chef valido."));
            return null;
        }
        if (!authBean.isCliente()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Acesso negado", "Apenas clientes podem enviar mensagens."));
            return null;
        }
        if (mensagemCliente == null || mensagemCliente.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Mensagem obrigatoria", "Escreva uma mensagem para o chef."));
            return null;
        }
        chefConteudoBean.enviarMensagemCliente(
                chef.getId(),
                authBean.getUsuarioLogado().getId(),
                authBean.getUsuarioLogado().getNome(),
                mensagemCliente
        );
        mensagemCliente = "";
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Mensagem enviada", "O chef recebera sua mensagem."));
        return null;
    }

    private List<Long> idsPratosSelecionados() {
        Set<Long> ids = new LinkedHashSet<>();
        adicionarIdsArray(ids, pratosSelecionados);

        if (pratosSelecionadosTexto != null && !pratosSelecionadosTexto.trim().isEmpty()) {
            String[] partes = pratosSelecionadosTexto.split(",");
            adicionarIdsArray(ids, partes);
        }

        return new ArrayList<>(ids);
    }

    private void adicionarIdsArray(Set<Long> destino, String[] origem) {
        if (origem == null) {
            return;
        }
        for (String valor : origem) {
            if (valor == null || valor.trim().isEmpty()) {
                continue;
            }
            try {
                destino.add(Long.valueOf(valor.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public Long getChefId() {
        if (chefId != null) {
            return chefId;
        }
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String param = params.get("chefId");
        if (param == null || param.trim().isEmpty()) {
            return null;
        }
        try {
            chefId = Long.valueOf(param);
            return chefId;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void setChefId(Long chefId) {
        this.chefId = chefId;
    }

    public String getMensagemCliente() {
        return mensagemCliente;
    }

    public void setMensagemCliente(String mensagemCliente) {
        this.mensagemCliente = mensagemCliente;
    }

    public String[] getPratosSelecionados() {
        return pratosSelecionados;
    }

    public void setPratosSelecionados(String[] pratosSelecionados) {
        this.pratosSelecionados = pratosSelecionados;
    }

    public String getPratosSelecionadosTexto() {
        return pratosSelecionadosTexto;
    }

    public void setPratosSelecionadosTexto(String pratosSelecionadosTexto) {
        this.pratosSelecionadosTexto = pratosSelecionadosTexto;
    }

    public String getDataEvento() {
        return dataEvento;
    }

    public void setDataEvento(String dataEvento) {
        this.dataEvento = dataEvento;
    }

    public String getHorarioEvento() {
        return horarioEvento;
    }

    public void setHorarioEvento(String horarioEvento) {
        this.horarioEvento = horarioEvento;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}

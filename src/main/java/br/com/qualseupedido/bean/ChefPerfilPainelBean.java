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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Named
@ViewScoped
public class ChefPerfilPainelBean implements Serializable {

    @Inject
    private AuthBean authBean;
    @Inject
    private PratoBean pratoBean;
    @Inject
    private ChefConteudoBean chefConteudoBean;

    private String novaPostagem;
    private final Map<Long, String> textosEdicaoPorPostagem = new HashMap<>();
    private Long clienteSelecionadoId;
    private String respostaChat;

    public List<Prato> getPratosDoChef() {
        if (!authBean.isCozinheiro() || authBean.getUsuarioLogado() == null) {
            return Collections.emptyList();
        }
        return pratoBean.getPratosDoChef(authBean.getUsuarioLogado().getId());
    }

    public String alternarVisibilidadePrato(Long pratoId) {
        if (!authBean.isCozinheiro() || authBean.getUsuarioLogado() == null) {
            return null;
        }
        pratoBean.alternarVisibilidadeNoPerfil(authBean.getUsuarioLogado().getId(), pratoId);
        return null;
    }

    public String publicarPostagem() {
        if (!authBean.isCozinheiro() || authBean.getUsuarioLogado() == null) {
            return null;
        }
        if (novaPostagem == null || novaPostagem.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Postagem obrigatoria", "Escreva algo para publicar."));
            return null;
        }
        chefConteudoBean.adicionarPostagem(authBean.getUsuarioLogado().getId(), novaPostagem);
        novaPostagem = "";
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Publicado", "Postagem criada com sucesso."));
        return null;
    }

    public List<ChefConteudoBean.PostagemChef> getPostagens() {
        if (!authBean.isCozinheiro() || authBean.getUsuarioLogado() == null) {
            return Collections.emptyList();
        }
        return chefConteudoBean.listarPostagensChef(authBean.getUsuarioLogado().getId());
    }

    public Map<Long, String> getTextosEdicaoPorPostagem() {
        preencherRascunhosPostagem();
        return textosEdicaoPorPostagem;
    }

    public String salvarEdicaoPostagem(Long postagemId) {
        if (!authBean.isCozinheiro() || authBean.getUsuarioLogado() == null) {
            return null;
        }
        preencherRascunhosPostagem();

        String novoTexto = textosEdicaoPorPostagem.get(postagemId);
        if (novoTexto == null || novoTexto.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Texto obrigatorio", "Digite o texto atualizado da postagem."));
            return null;
        }

        boolean ok = chefConteudoBean.editarPostagem(authBean.getUsuarioLogado().getId(), postagemId, novoTexto);
        if (ok) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Postagem atualizada", "Edicao salva com sucesso."));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Falha", "Nao foi possivel atualizar a postagem."));
        }
        return null;
    }

    public List<ClienteChatResumo> getClientesComMensagens() {
        if (!authBean.isCozinheiro() || authBean.getUsuarioLogado() == null) {
            return Collections.emptyList();
        }

        List<ChefConteudoBean.MensagemChef> mensagens = chefConteudoBean.listarMensagensChef(authBean.getUsuarioLogado().getId());
        Map<Long, ClienteChatResumo> mapa = new LinkedHashMap<>();

        for (ChefConteudoBean.MensagemChef msg : mensagens) {
            if (msg.getClienteId() == null) {
                continue;
            }
            ClienteChatResumo resumo = mapa.get(msg.getClienteId());
            if (resumo == null) {
                resumo = new ClienteChatResumo(msg.getClienteId(), msg.getNomeCliente());
                mapa.put(msg.getClienteId(), resumo);
            }
            resumo.incrementarTotal();
            if (msg.getRespostaChef() == null || msg.getRespostaChef().trim().isEmpty()) {
                resumo.incrementarPendentes();
            }
        }

        List<ClienteChatResumo> clientes = new ArrayList<>(mapa.values());
        ajustarClienteSelecionado(clientes);
        return clientes;
    }

    public List<ChefConteudoBean.MensagemChef> getConversaClienteSelecionado() {
        if (!authBean.isCozinheiro() || authBean.getUsuarioLogado() == null || clienteSelecionadoId == null) {
            return Collections.emptyList();
        }
        return chefConteudoBean.listarConversaClienteChef(authBean.getUsuarioLogado().getId(), clienteSelecionadoId);
    }

    public String responderClienteSelecionado() {
        if (!authBean.isCozinheiro() || authBean.getUsuarioLogado() == null) {
            return null;
        }
        if (clienteSelecionadoId == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cliente obrigatorio", "Selecione um cliente para responder."));
            return null;
        }
        if (respostaChat == null || respostaChat.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Resposta obrigatoria", "Digite a resposta para o cliente."));
            return null;
        }

        List<ChefConteudoBean.MensagemChef> conversa = getConversaClienteSelecionado();
        if (conversa.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Sem mensagens", "Esse cliente ainda nao enviou mensagens."));
            return null;
        }

        Long mensagemAlvoId = null;
        for (int i = conversa.size() - 1; i >= 0; i--) {
            ChefConteudoBean.MensagemChef msg = conversa.get(i);
            if (msg.getRespostaChef() == null || msg.getRespostaChef().trim().isEmpty()) {
                mensagemAlvoId = msg.getId();
                break;
            }
        }
        if (mensagemAlvoId == null) {
            mensagemAlvoId = conversa.get(conversa.size() - 1).getId();
        }

        boolean ok = chefConteudoBean.responderMensagem(authBean.getUsuarioLogado().getId(), mensagemAlvoId, respostaChat);
        if (ok) {
            respostaChat = "";
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Resposta enviada", "Cliente respondido com sucesso."));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Falha", "Nao foi possivel enviar a resposta."));
        }
        return null;
    }

    private void preencherRascunhosPostagem() {
        for (ChefConteudoBean.PostagemChef post : getPostagens()) {
            textosEdicaoPorPostagem.putIfAbsent(post.getId(), post.getTexto());
        }
    }

    private void ajustarClienteSelecionado(List<ClienteChatResumo> clientes) {
        if (clientes.isEmpty()) {
            clienteSelecionadoId = null;
            return;
        }
        if (clienteSelecionadoId == null) {
            clienteSelecionadoId = clientes.get(0).getClienteId();
            return;
        }
        for (ClienteChatResumo cliente : clientes) {
            if (clienteSelecionadoId.equals(cliente.getClienteId())) {
                return;
            }
        }
        clienteSelecionadoId = clientes.get(0).getClienteId();
    }

    public String getNovaPostagem() {
        return novaPostagem;
    }

    public void setNovaPostagem(String novaPostagem) {
        this.novaPostagem = novaPostagem;
    }

    public Long getClienteSelecionadoId() {
        return clienteSelecionadoId;
    }

    public void setClienteSelecionadoId(Long clienteSelecionadoId) {
        this.clienteSelecionadoId = clienteSelecionadoId;
    }

    public String getRespostaChat() {
        return respostaChat;
    }

    public void setRespostaChat(String respostaChat) {
        this.respostaChat = respostaChat;
    }

    public static class ClienteChatResumo implements Serializable {
        private final Long clienteId;
        private final String nomeCliente;
        private int totalMensagens;
        private int pendentes;

        public ClienteChatResumo(Long clienteId, String nomeCliente) {
            this.clienteId = clienteId;
            this.nomeCliente = (nomeCliente == null || nomeCliente.trim().isEmpty()) ? "Cliente" : nomeCliente;
        }

        public void incrementarTotal() {
            totalMensagens++;
        }

        public void incrementarPendentes() {
            pendentes++;
        }

        public Long getClienteId() {
            return clienteId;
        }

        public String getNomeCliente() {
            return nomeCliente;
        }

        public int getTotalMensagens() {
            return totalMensagens;
        }

        public int getPendentes() {
            return pendentes;
        }

        public String getRotulo() {
            if (pendentes > 0) {
                return nomeCliente + " (" + pendentes + " pendente(s))";
            }
            return nomeCliente + " (" + totalMensagens + " mensagem(ns))";
        }
    }
}
package br.com.qualseupedido.bean;

import br.com.qualseupedido.entidade.Prato;
import br.com.qualseupedido.util.ImagemUtil;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Named
@ViewScoped
public class ChefPerfilPainelBean implements Serializable {

    private static final long TAMANHO_MAXIMO = 3L * 1024L * 1024L;
    private static final int MAX_LARGURA_POSTAGEM = 1400;
    private static final int MAX_ALTURA_POSTAGEM = 900;
    private static final float QUALIDADE_JPEG = 0.82f;

    @Inject
    private AuthBean authBean;
    @Inject
    private PratoBean pratoBean;
    @Inject
    private ChefConteudoBean chefConteudoBean;

    private String novaPostagem;
    private transient Part fotoPostagem;
    private Long clienteSelecionadoId;
    private String respostaChat;

    public List<Prato> getPratosDoChef() {
        if (!authBean.isCozinheiro() || authBean.getUsuarioLogado() == null) {
            return Collections.emptyList();
        }
        return pratoBean.getPratosDoChef(authBean.getUsuarioLogado().getId());
    }

    public List<Prato> getPratosDoChefPorCategoria(String categoria) {
        if (!authBean.isCozinheiro() || authBean.getUsuarioLogado() == null) {
            return Collections.emptyList();
        }
        return pratoBean.getPratosDoChefPorCategoria(authBean.getUsuarioLogado().getId(), categoria);
    }

    public List<Prato> pratosDoChefPorCategoria(String categoria) {
        return getPratosDoChefPorCategoria(categoria);
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

        boolean textoVazio = novaPostagem == null || novaPostagem.trim().isEmpty();
        boolean semFoto = fotoPostagem == null || fotoPostagem.getSize() == 0;
        if (textoVazio && semFoto) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Postagem vazia", "Escreva um texto ou envie uma foto."));
            return null;
        }

        String fotoDataUrl = null;
        if (!semFoto) {
            if (fotoPostagem.getSize() > TAMANHO_MAXIMO) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Arquivo muito grande", "Use uma imagem de ate 3MB."));
                return null;
            }
            String contentType = fotoPostagem.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Formato invalido", "Envie apenas arquivos de imagem."));
                return null;
            }
            try {
                fotoDataUrl = ImagemUtil.processarDataUrl(
                        fotoPostagem.getInputStream(),
                        MAX_LARGURA_POSTAGEM,
                        MAX_ALTURA_POSTAGEM,
                        QUALIDADE_JPEG
                );
            } catch (IOException e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Falha no upload", "Nao foi possivel processar a foto da postagem."));
                return null;
            }
        }

        chefConteudoBean.adicionarPostagem(authBean.getUsuarioLogado().getId(), novaPostagem, fotoDataUrl);
        novaPostagem = "";
        fotoPostagem = null;
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

    public Part getFotoPostagem() {
        return fotoPostagem;
    }

    public void setFotoPostagem(Part fotoPostagem) {
        this.fotoPostagem = fotoPostagem;
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

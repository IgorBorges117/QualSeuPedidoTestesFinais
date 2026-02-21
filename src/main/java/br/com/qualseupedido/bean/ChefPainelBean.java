package br.com.qualseupedido.bean;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named
@RequestScoped
public class ChefPainelBean implements Serializable {

    private String novaPostagem;
    private final Map<Long, String> respostas = new HashMap<>();

    @Inject
    private AuthBean authBean;
    @Inject
    private ChefConteudoBean chefConteudoBean;

    public List<ChefConteudoBean.MensagemChef> getMensagensRecebidas() {
        if (!authBean.isCozinheiro() || authBean.getUsuarioLogado() == null) {
            return java.util.Collections.emptyList();
        }
        return chefConteudoBean.listarMensagensChef(authBean.getUsuarioLogado().getId());
    }

    public List<ChefConteudoBean.PostagemChef> getPostagensChef() {
        if (!authBean.isCozinheiro() || authBean.getUsuarioLogado() == null) {
            return java.util.Collections.emptyList();
        }
        return chefConteudoBean.listarPostagensChef(authBean.getUsuarioLogado().getId());
    }

    public String publicar() {
        if (!authBean.isCozinheiro()) {
            return null;
        }
        if (novaPostagem == null || novaPostagem.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Postagem obrigatória", "Escreva algo antes de publicar."));
            return null;
        }
        chefConteudoBean.adicionarPostagem(authBean.getUsuarioLogado().getId(), novaPostagem);
        novaPostagem = "";
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Publicado", "Sua postagem foi publicada."));
        return null;
    }

    public String responder(Long mensagemId) {
        if (!authBean.isCozinheiro()) {
            return null;
        }
        String resposta = respostas.get(mensagemId);
        if (resposta == null || resposta.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Resposta obrigatória", "Digite uma resposta para o cliente."));
            return null;
        }
        boolean ok = chefConteudoBean.responderMensagem(authBean.getUsuarioLogado().getId(), mensagemId, resposta);
        if (ok) {
            respostas.put(mensagemId, "");
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Resposta enviada", "Resposta registrada com sucesso."));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Falha", "Não foi possível responder à mensagem."));
        }
        return null;
    }

    public String getNovaPostagem() {
        return novaPostagem;
    }

    public void setNovaPostagem(String novaPostagem) {
        this.novaPostagem = novaPostagem;
    }

    public Map<Long, String> getRespostas() {
        return respostas;
    }
}

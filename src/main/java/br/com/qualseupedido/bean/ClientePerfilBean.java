package br.com.qualseupedido.bean;

import br.com.qualseupedido.util.ImagemUtil;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.Serializable;

@Named
@RequestScoped
public class ClientePerfilBean implements Serializable {

    private static final long TAMANHO_MAXIMO = 3L * 1024L * 1024L;
    private static final int MAX_LARGURA_PERFIL = 720;
    private static final int MAX_ALTURA_PERFIL = 720;
    private static final float QUALIDADE_JPEG = 0.82f;
    private Part fotoPerfil;

    @Inject
    private AuthBean authBean;

    public String salvarDadosCliente() {
        if (!authBean.isCliente()) {
            return "/pages/index.xhtml?faces-redirect=true";
        }
        authBean.getUsuarioLogado().setPerfilClienteConfigurado(true);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Dados salvos", "Suas configurações foram atualizadas."));
        return null;
    }

    public String salvarFotoPerfilCliente() {
        if (!authBean.isCliente()) {
            return "/pages/index.xhtml?faces-redirect=true";
        }
        if (fotoPerfil == null || fotoPerfil.getSize() == 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Foto obrigatória", "Selecione uma foto de perfil."));
            return null;
        }
        if (fotoPerfil.getSize() > TAMANHO_MAXIMO) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Arquivo muito grande", "Use uma imagem de até 3MB."));
            return null;
        }

        String contentType = fotoPerfil.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Formato inválido", "Envie um arquivo de imagem."));
            return null;
        }

        try {
            String dataUrl = ImagemUtil.processarDataUrl(
                    fotoPerfil.getInputStream(),
                    MAX_LARGURA_PERFIL,
                    MAX_ALTURA_PERFIL,
                    QUALIDADE_JPEG
            );
            authBean.getUsuarioLogado().setFotoPerfilDataUrl(dataUrl);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Imagem salva", "Foto de perfil atualizada."));
            return null;
        } catch (IOException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Falha no upload", "Não foi possível salvar a imagem."));
            return null;
        }
    }

    public String salvarFotoEContinuar() {
        if (!authBean.isCliente()) {
            return "/pages/index.xhtml?faces-redirect=true";
        }
        if (fotoPerfil == null || fotoPerfil.getSize() == 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Foto obrigatória", "Selecione uma foto de perfil."));
            return null;
        }
        if (fotoPerfil.getSize() > TAMANHO_MAXIMO) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Arquivo muito grande", "Use uma imagem de até 3MB."));
            return null;
        }

        String contentType = fotoPerfil.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Formato inválido", "Envie um arquivo de imagem."));
            return null;
        }

        try {
            String dataUrl = ImagemUtil.processarDataUrl(
                    fotoPerfil.getInputStream(),
                    MAX_LARGURA_PERFIL,
                    MAX_ALTURA_PERFIL,
                    QUALIDADE_JPEG
            );
            authBean.getUsuarioLogado().setFotoPerfilDataUrl(dataUrl);
            return authBean.concluirOnboardingCliente();
        } catch (IOException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Falha no upload", "Não foi possível salvar a imagem."));
            return null;
        }
    }

    public Part getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(Part fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }
}

package br.com.qualseupedido.bean;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Base64;

@Named
@RequestScoped
public class ClientePerfilBean implements Serializable {

    private static final long TAMANHO_MAXIMO = 3L * 1024L * 1024L;
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
            byte[] bytes = lerBytes(fotoPerfil.getInputStream());
            String base64 = Base64.getEncoder().encodeToString(bytes);
            authBean.getUsuarioLogado().setFotoPerfilDataUrl("data:" + contentType + ";base64," + base64);
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
            byte[] bytes = lerBytes(fotoPerfil.getInputStream());
            String base64 = Base64.getEncoder().encodeToString(bytes);
            authBean.getUsuarioLogado().setFotoPerfilDataUrl("data:" + contentType + ";base64," + base64);
            return authBean.concluirOnboardingCliente();
        } catch (IOException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Falha no upload", "Não foi possível salvar a imagem."));
            return null;
        }
    }

    private byte[] lerBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int lidos;
        while ((lidos = in.read(buffer)) != -1) {
            out.write(buffer, 0, lidos);
        }
        return out.toByteArray();
    }

    public Part getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(Part fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }
}

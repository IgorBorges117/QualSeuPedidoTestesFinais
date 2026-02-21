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
public class ChefPerfilBean implements Serializable {

    private static final long TAMANHO_MAXIMO = 3L * 1024L * 1024L;
    private Part fotoPerfil;
    private Part fotoCapa;

    @Inject
    private AuthBean authBean;

    public String salvarDadosChef() {
        if (!authBean.isCozinheiro()) {
            return "/pages/index.xhtml?faces-redirect=true";
        }
        authBean.getUsuarioLogado().setPerfilChefConfigurado(true);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Dados salvos", "Configurações do chef atualizadas."));
        return null;
    }

    public String salvarFotoChef() {
        if (!authBean.isCozinheiro()) {
            return "/pages/index.xhtml?faces-redirect=true";
        }
        return salvarImagem(
                fotoPerfil,
                true,
                "Foto obrigatória",
                "Selecione uma foto de perfil.",
                "Foto de perfil atualizada com sucesso."
        );
    }

    public String salvarFotoCapaChef() {
        if (!authBean.isCozinheiro()) {
            return "/pages/index.xhtml?faces-redirect=true";
        }
        return salvarImagem(
                fotoCapa,
                false,
                "Capa obrigatória",
                "Selecione uma foto de capa.",
                "Foto de capa atualizada com sucesso."
        );
    }

    private String salvarImagem(Part arquivo,
                                boolean perfil,
                                String tituloObrigatorio,
                                String detalheObrigatorio,
                                String sucessoDetalhe) {
        if (arquivo == null || arquivo.getSize() == 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, tituloObrigatorio, detalheObrigatorio));
            return null;
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Arquivo muito grande", "Use uma imagem de até 3MB."));
            return null;
        }

        String contentType = arquivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Formato inválido", "Envie um arquivo de imagem."));
            return null;
        }

        try {
            byte[] bytes = lerBytes(arquivo.getInputStream());
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String dataUrl = "data:" + contentType + ";base64," + base64;
            if (perfil) {
                authBean.getUsuarioLogado().setFotoPerfilDataUrl(dataUrl);
            } else {
                authBean.getUsuarioLogado().setFotoCapaDataUrl(dataUrl);
            }
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Imagem salva", sucessoDetalhe));
            return null;
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

    public Part getFotoCapa() {
        return fotoCapa;
    }

    public void setFotoCapa(Part fotoCapa) {
        this.fotoCapa = fotoCapa;
    }
}

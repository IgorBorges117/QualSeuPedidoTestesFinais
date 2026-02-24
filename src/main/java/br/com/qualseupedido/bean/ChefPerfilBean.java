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
public class ChefPerfilBean implements Serializable {

    private static final String[] ESPECIALIDADES_DISPONIVEIS = new String[]{
            "Chef particular",
            "Culinaria brasileira",
            "Culinaria italiana",
            "Culinaria japonesa",
            "Culinaria francesa",
            "Churrasco e grelhados",
            "Massas artesanais",
            "Confeitaria e sobremesas",
            "Cozinha vegetariana",
            "Cozinha vegana",
            "Cozinha fitness",
            "Eventos corporativos"
    };

    private static final long TAMANHO_MAXIMO = 3L * 1024L * 1024L;
    private static final int MAX_LARGURA_PERFIL = 720;
    private static final int MAX_ALTURA_PERFIL = 720;
    private static final int MAX_LARGURA_CAPA = 1600;
    private static final int MAX_ALTURA_CAPA = 700;
    private static final float QUALIDADE_JPEG = 0.82f;
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
            String dataUrl = ImagemUtil.processarDataUrl(
                    arquivo.getInputStream(),
                    perfil ? MAX_LARGURA_PERFIL : MAX_LARGURA_CAPA,
                    perfil ? MAX_ALTURA_PERFIL : MAX_ALTURA_CAPA,
                    QUALIDADE_JPEG
            );
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

    public String[] getEspecialidadesDisponiveis() {
        return ESPECIALIDADES_DISPONIVEIS;
    }
}

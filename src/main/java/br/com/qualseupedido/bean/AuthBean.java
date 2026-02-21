package br.com.qualseupedido.bean;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class AuthBean implements Serializable {

    private String login;
    private String senha;
    private UsuarioSistema usuarioLogado;

    @Inject
    private UsuarioBean usuarioBean;

    public String entrar() {
        UsuarioSistema usuario = usuarioBean.autenticar(login, senha);
        if (usuario == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login inválido", "Verifique login e senha."));
            return null;
        }

        autenticarDireto(usuario);
        login = "";
        senha = "";
        return redirecionarPosLogin();
    }

    public void autenticarDireto(UsuarioSistema usuario) {
        this.usuarioLogado = usuario;
    }

    public String redirecionarPosLogin() {
        if (usuarioLogado == null) {
            return "/pages/login.xhtml?faces-redirect=true";
        }
        if (usuarioLogado.getTipo() == TipoUsuario.COZINHEIRO) {
            if (!usuarioLogado.isPerfilChefConfigurado()) {
                return "/pages/chef-onboarding.xhtml?faces-redirect=true";
            }
            return "/pages/chef.xhtml?faces-redirect=true";
        }
        if (!usuarioLogado.isPerfilClienteConfigurado()) {
            return "/pages/cliente-perfil-edicao.xhtml?faces-redirect=true";
        }
        return "/pages/cliente-perfil.xhtml?faces-redirect=true";
    }

    public String concluirOnboardingChef() {
        if (!isCozinheiro()) {
            return "/pages/index.xhtml?faces-redirect=true";
        }
        usuarioLogado.setPerfilChefConfigurado(true);
        return "/pages/chef.xhtml?faces-redirect=true";
    }

    public String concluirOnboardingCliente() {
        if (!isCliente()) {
            return "/pages/index.xhtml?faces-redirect=true";
        }
        usuarioLogado.setPerfilClienteConfigurado(true);
        return "/pages/cliente-perfil.xhtml?faces-redirect=true";
    }

    public String sair() {
        usuarioLogado = null;
        return "/pages/index.xhtml?faces-redirect=true";
    }

    public boolean isLogado() {
        return usuarioLogado != null;
    }

    public boolean isCliente() {
        return isLogado() && usuarioLogado.getTipo() == TipoUsuario.CLIENTE;
    }

    public boolean isCozinheiro() {
        return isLogado() && usuarioLogado.getTipo() == TipoUsuario.COZINHEIRO;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public UsuarioSistema getUsuarioLogado() {
        return usuarioLogado;
    }
}

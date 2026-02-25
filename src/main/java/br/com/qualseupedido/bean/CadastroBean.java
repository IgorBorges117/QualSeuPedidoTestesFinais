package br.com.qualseupedido.bean;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@RequestScoped
public class CadastroBean implements Serializable {

    private String nome;
    private String dataNascimento;
    private String cpf;
    private String sexo;
    private String email;
    private String senha;
    private String confirmarSenha;
    private String telefone;
    private String endereco;
    private String complemento;
    private String numero;
    private String bairro;
    private String cep;
    private String cidade;
    private String estado;
    private String preferenciaAlimentar;
    private TipoUsuario tipo = TipoUsuario.CLIENTE;
    private boolean aceitouTermos;

    @Inject
    private UsuarioBean usuarioBean;
    @Inject
    private AuthBean authBean;

    public String cadastrar() {
        if (senha == null || !senha.equals(confirmarSenha)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Senha inválida", "As senhas precisam ser iguais."));
            return null;
        }

        if (!usuarioBean.emailFormatoValido(email)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "E-mail inválido", "Informe um e-mail válido."));
            return null;
        }
        if (!aceitouTermos) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Termos obrigatórios", "Você precisa aceitar os termos de uso."));
            return null;
        }

        boolean ok = usuarioBean.cadastrar(
                nome,
                email,
                senha,
                tipo,
                dataNascimento,
                cpf,
                sexo,
                telefone,
                endereco,
                complemento,
                numero,
                bairro,
                cep,
                cidade,
                estado,
                preferenciaAlimentar
        );
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cadastro não realizado", "Dados inválidos ou e-mail já existente."));
            return null;
        }

        UsuarioSistema usuario = usuarioBean.autenticar(email, senha);
        authBean.autenticarDireto(usuario);
        return authBean.redirecionarPosLogin();
    }

    public String[] getSexosDisponiveis() {
        return new String[]{"Masculino", "Feminino", "Outro"};
    }

    public String[] getPreferenciasDisponiveis() {
        return new String[]{"Sem restrição", "Vegetariana", "Vegana", "Sem glúten", "Sem lactose"};
    }

    public TipoUsuario[] getTiposConta() {
        return TipoUsuario.values();
    }

    public SelectItem[] getTiposContaLabel() {
        return new SelectItem[]{
                new SelectItem(TipoUsuario.CLIENTE, "Pessoa física"),
                new SelectItem(TipoUsuario.COZINHEIRO, "Cozinheiro / Chefe")
        };
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(String dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getConfirmarSenha() {
        return confirmarSenha;
    }

    public void setConfirmarSenha(String confirmarSenha) {
        this.confirmarSenha = confirmarSenha;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPreferenciaAlimentar() {
        return preferenciaAlimentar;
    }

    public void setPreferenciaAlimentar(String preferenciaAlimentar) {
        this.preferenciaAlimentar = preferenciaAlimentar;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setTipo(TipoUsuario tipo) {
        this.tipo = tipo;
    }

    public boolean isAceitouTermos() {
        return aceitouTermos;
    }

    public void setAceitouTermos(boolean aceitouTermos) {
        this.aceitouTermos = aceitouTermos;
    }
}


package br.com.qualseupedido.bean;

import java.io.Serializable;

public class UsuarioSistema implements Serializable {

    private final Long id;
    private final String email;
    private final String senha;
    private final String nome;
    private final TipoUsuario tipo;
    private boolean perfilChefConfigurado;
    private boolean perfilClienteConfigurado;
    private String dataNascimento;
    private String cpf;
    private String sexo;
    private String telefone;
    private String endereco;
    private String complemento;
    private String numero;
    private String bairro;
    private String cep;
    private String cidade;
    private String estado;
    private String preferenciaAlimentar;
    private String fotoPerfilDataUrl;
    private String fotoCapaDataUrl;
    private boolean chefVerificado;
    private String faixaPrecoChef;
    private String disponibilidadeChef;

    public UsuarioSistema(Long id, String email, String senha, String nome, TipoUsuario tipo) {
        this.id = id;
        this.email = email;
        this.senha = senha;
        this.nome = nome;
        this.tipo = tipo;
        this.perfilChefConfigurado = false;
        this.perfilClienteConfigurado = false;
        this.chefVerificado = false;
        this.faixaPrecoChef = "$$";
        this.disponibilidadeChef = "Hoje";
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public String getNome() {
        return nome;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public boolean isPerfilChefConfigurado() {
        return perfilChefConfigurado;
    }

    public void setPerfilChefConfigurado(boolean perfilChefConfigurado) {
        this.perfilChefConfigurado = perfilChefConfigurado;
    }

    public boolean isPerfilClienteConfigurado() {
        return perfilClienteConfigurado;
    }

    public void setPerfilClienteConfigurado(boolean perfilClienteConfigurado) {
        this.perfilClienteConfigurado = perfilClienteConfigurado;
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

    public String getFotoPerfilDataUrl() {
        return fotoPerfilDataUrl;
    }

    public void setFotoPerfilDataUrl(String fotoPerfilDataUrl) {
        this.fotoPerfilDataUrl = fotoPerfilDataUrl;
    }

    public String getFotoCapaDataUrl() {
        return fotoCapaDataUrl;
    }

    public void setFotoCapaDataUrl(String fotoCapaDataUrl) {
        this.fotoCapaDataUrl = fotoCapaDataUrl;
    }

    public boolean isChefVerificado() {
        return chefVerificado;
    }

    public void setChefVerificado(boolean chefVerificado) {
        this.chefVerificado = chefVerificado;
    }

    public String getFaixaPrecoChef() {
        return faixaPrecoChef;
    }

    public void setFaixaPrecoChef(String faixaPrecoChef) {
        this.faixaPrecoChef = faixaPrecoChef;
    }

    public String getDisponibilidadeChef() {
        return disponibilidadeChef;
    }

    public void setDisponibilidadeChef(String disponibilidadeChef) {
        this.disponibilidadeChef = disponibilidadeChef;
    }
}

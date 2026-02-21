package br.com.qualseupedido.bean;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Named
@ApplicationScoped
public class UsuarioBean implements Serializable {

    private final List<UsuarioSistema> usuarios = new ArrayList<>();
    private long seq = 1L;

    public UsuarioBean() {
        UsuarioSistema chefDemo = new UsuarioSistema(seq++, "chef@demo.com", "123456", "Chef Demo", TipoUsuario.COZINHEIRO);
        chefDemo.setPerfilChefConfigurado(true);
        chefDemo.setPerfilClienteConfigurado(true);
        chefDemo.setChefVerificado(true);
        chefDemo.setFaixaPrecoChef("$$");
        chefDemo.setDisponibilidadeChef("Hoje");

        UsuarioSistema clienteDemo = new UsuarioSistema(seq++, "cliente@demo.com", "123456", "Cliente Demo", TipoUsuario.CLIENTE);
        clienteDemo.setPerfilClienteConfigurado(true);
        clienteDemo.setPerfilChefConfigurado(true);

        usuarios.add(chefDemo);
        usuarios.add(clienteDemo);
    }

    public synchronized boolean cadastrar(String nome,
                                          String email,
                                          String senha,
                                          TipoUsuario tipo,
                                          String dataNascimento,
                                          String cpf,
                                          String sexo,
                                          String telefone,
                                          String endereco,
                                          String complemento,
                                          String numero,
                                          String bairro,
                                          String cep,
                                          String cidade,
                                          String estado,
                                          String preferenciaAlimentar) {
        if (nome == null || nome.trim().isEmpty()) return false;
        if (email == null || email.trim().isEmpty()) return false;
        if (senha == null || senha.trim().isEmpty()) return false;
        if (tipo == null) return false;

        String chave = email.trim().toLowerCase();
        for (UsuarioSistema u : usuarios) {
            if (u.getEmail().equalsIgnoreCase(chave)) return false;
        }

        UsuarioSistema novo = new UsuarioSistema(seq++, chave, senha, nome.trim(), tipo);
        novo.setDataNascimento(dataNascimento);
        novo.setCpf(cpf);
        novo.setSexo(sexo);
        novo.setTelefone(telefone);
        novo.setEndereco(endereco);
        novo.setComplemento(complemento);
        novo.setNumero(numero);
        novo.setBairro(bairro);
        novo.setCep(cep);
        novo.setCidade(cidade);
        novo.setEstado(estado);
        novo.setPreferenciaAlimentar(preferenciaAlimentar);

        usuarios.add(novo);
        return true;
    }

    public synchronized UsuarioSistema autenticar(String login, String senha) {
        if (login == null || senha == null) return null;

        String chave = login.trim().toLowerCase();
        for (UsuarioSistema u : usuarios) {
            if ((u.getEmail().equalsIgnoreCase(chave) || u.getNome().equalsIgnoreCase(login.trim()))
                    && u.getSenha().equals(senha)) {
                return u;
            }
        }
        return null;
    }

    public List<UsuarioSistema> getUsuarios() {
        return Collections.unmodifiableList(usuarios);
    }

    public synchronized UsuarioSistema buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        for (UsuarioSistema u : usuarios) {
            if (id.equals(u.getId())) {
                return u;
            }
        }
        return null;
    }

    public synchronized List<UsuarioSistema> getChefsDisponiveis() {
        List<UsuarioSistema> chefs = new ArrayList<>();
        for (UsuarioSistema u : usuarios) {
            if (u.getTipo() == TipoUsuario.COZINHEIRO) {
                chefs.add(u);
            }
        }
        return Collections.unmodifiableList(chefs);
    }
}

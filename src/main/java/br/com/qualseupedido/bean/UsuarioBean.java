package br.com.qualseupedido.bean;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Named
@ApplicationScoped
public class UsuarioBean implements Serializable {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,63}$");

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
        if (!emailFormatoValido(email)) return false;
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

    public boolean emailFormatoValido(String email) {
        if (email == null) {
            return false;
        }
        String valor = email.trim();
        if (valor.isEmpty() || valor.length() > 254) {
            return false;
        }
        if (!EMAIL_PATTERN.matcher(valor).matches()) {
            return false;
        }
        int arroba = valor.indexOf('@');
        String local = valor.substring(0, arroba);
        String dominio = valor.substring(arroba + 1);

        if (local.length() > 64 || local.startsWith(".") || local.endsWith(".") || local.contains("..")) {
            return false;
        }
        if (dominio.startsWith(".") || dominio.endsWith(".") || dominio.contains("..")) {
            return false;
        }

        String[] labels = dominio.split("\\.");
        for (String label : labels) {
            if (label.isEmpty() || label.startsWith("-") || label.endsWith("-")) {
                return false;
            }
        }
        return true;
    }
}

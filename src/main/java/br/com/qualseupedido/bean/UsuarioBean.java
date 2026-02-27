package br.com.qualseupedido.bean;

import br.com.qualseupedido.util.ImagemUtil;
import br.com.qualseupedido.util.SenhaUtil;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

@Named
@ApplicationScoped
public class UsuarioBean implements Serializable {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,63}$");
    private static final int MAX_LARGURA_PERFIL = 720;
    private static final int MAX_ALTURA_PERFIL = 720;
    private static final int MAX_LARGURA_CAPA = 1600;
    private static final int MAX_ALTURA_CAPA = 700;
    private static final float QUALIDADE_JPEG = 0.82f;

    private final List<UsuarioSistema> usuarios = new ArrayList<>();
    private long seq = 1L;

    public UsuarioBean() {
        UsuarioSistema adminDemo = new UsuarioSistema(seq++, "admin@demo.com", SenhaUtil.hashSha256("123456"), "Administrador", TipoUsuario.ADMIN);
        adminDemo.setPerfilClienteConfigurado(true);
        adminDemo.setPerfilChefConfigurado(true);

        UsuarioSistema chefDemo = new UsuarioSistema(seq++, "chef@demo.com", SenhaUtil.hashSha256("123456"), "Ana Maria Braga", TipoUsuario.COZINHEIRO);
        chefDemo.setPerfilChefConfigurado(true);
        chefDemo.setPerfilClienteConfigurado(true);
        chefDemo.setChefVerificado(true);
        chefDemo.setFaixaPrecoChef("$$$");
        chefDemo.setDisponibilidadeChef("Fim de semana");
        chefDemo.setPreferenciaAlimentar("Culinaria brasileira");
        chefDemo.setCidade("Sao Joaquim da Barra");
        chefDemo.setEstado("SP");
        chefDemo.setDataNascimento("01/04/1949");
        chefDemo.setSexo("Feminino");
        chefDemo.setFotoPerfilDataUrl(carregarImagemLocal(
                "C:\\Users\\IgorB\\Downloads\\chefes dm\\Ana M. perfil chef.png",
                MAX_LARGURA_PERFIL,
                MAX_ALTURA_PERFIL
        ));
        chefDemo.setFotoCapaDataUrl(carregarImagemLocal(
                "C:\\Users\\IgorB\\Downloads\\chefes dm\\capa ana maria braga.webp",
                MAX_LARGURA_CAPA,
                MAX_ALTURA_CAPA
        ));

        UsuarioSistema clienteDemo = new UsuarioSistema(seq++, "cliente@demo.com", SenhaUtil.hashSha256("123456"), "Cliente Demo", TipoUsuario.CLIENTE);
        clienteDemo.setPerfilClienteConfigurado(true);
        clienteDemo.setPerfilChefConfigurado(true);

        usuarios.add(adminDemo);
        usuarios.add(chefDemo);
        usuarios.add(clienteDemo);
    }

    private String carregarImagemLocal(String caminho, int maxWidth, int maxHeight) {
        if (caminho == null || caminho.trim().isEmpty()) {
            return null;
        }
        try {
            Path path = Paths.get(caminho);
            if (!Files.exists(path)) {
                return null;
            }
            try (InputStream input = Files.newInputStream(path)) {
                return ImagemUtil.processarDataUrl(input, maxWidth, maxHeight, QUALIDADE_JPEG);
            }
        } catch (IOException e) {
            return null;
        }
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
        if (tipo == null || tipo == TipoUsuario.ADMIN) return false;

        String chave = email.trim().toLowerCase();
        for (UsuarioSistema u : usuarios) {
            if (u.getEmail().equalsIgnoreCase(chave)) return false;
        }

        String senhaHash = SenhaUtil.hashSha256(senha);
        UsuarioSistema novo = new UsuarioSistema(seq++, chave, senhaHash, nome.trim(), tipo);
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
                    && SenhaUtil.confereSenha(senha, u.getSenha())) {
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
            if (u.getTipo() == TipoUsuario.COZINHEIRO && !u.isSuspensoAgora()) {
                chefs.add(u);
            }
        }
        return Collections.unmodifiableList(chefs);
    }

    public synchronized boolean atualizarEmail(Long usuarioId, String novoEmail) {
        if (usuarioId == null || novoEmail == null) {
            return false;
        }
        String chave = novoEmail.trim().toLowerCase();
        if (!emailFormatoValido(chave)) {
            return false;
        }

        UsuarioSistema alvo = buscarPorId(usuarioId);
        if (alvo == null) {
            return false;
        }

        for (UsuarioSistema usuario : usuarios) {
            if (!usuarioId.equals(usuario.getId()) && usuario.getEmail().equalsIgnoreCase(chave)) {
                return false;
            }
        }

        alvo.setEmail(chave);
        return true;
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

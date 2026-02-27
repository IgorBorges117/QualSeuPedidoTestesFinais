package br.com.qualseupedido.bean;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named
@ApplicationScoped
public class ChefConteudoBean implements Serializable {

    private long seqMensagem = 1L;
    private long seqPostagem = 1L;
    private final Map<Long, List<MensagemChef>> mensagensPorChef = new HashMap<>();
    private final Map<Long, List<PostagemChef>> postagensPorChef = new HashMap<>();

    public synchronized void enviarMensagemCliente(Long chefId, Long clienteId, String clienteNome, String texto) {
        if (chefId == null) {
            return;
        }
        if (clienteId == null) {
            return;
        }
        if (texto == null || texto.trim().isEmpty()) {
            return;
        }
        List<MensagemChef> mensagens = mensagensPorChef.computeIfAbsent(chefId, k -> new ArrayList<>());
        mensagens.add(new MensagemChef(seqMensagem++, clienteId, clienteNome, texto.trim()));
    }

    public synchronized List<MensagemChef> listarMensagensChef(Long chefId) {
        if (chefId == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(mensagensPorChef.getOrDefault(chefId, new ArrayList<>()));
    }

    public synchronized List<MensagemChef> listarConversaClienteChef(Long chefId, Long clienteId) {
        if (chefId == null || clienteId == null) {
            return new ArrayList<>();
        }
        List<MensagemChef> mensagens = mensagensPorChef.getOrDefault(chefId, new ArrayList<>());
        List<MensagemChef> conversa = new ArrayList<>();
        for (MensagemChef msg : mensagens) {
            if (clienteId.equals(msg.getClienteId())) {
                conversa.add(msg);
            }
        }
        return conversa;
    }

    public synchronized boolean responderMensagem(Long chefId, Long mensagemId, String resposta) {
        if (mensagemId == null || resposta == null || resposta.trim().isEmpty()) {
            return false;
        }
        if (chefId == null) {
            return false;
        }
        List<MensagemChef> mensagens = mensagensPorChef.get(chefId);
        if (mensagens == null) {
            return false;
        }
        for (MensagemChef msg : mensagens) {
            if (mensagemId.equals(msg.getId())) {
                msg.setRespostaChef(resposta.trim());
                return true;
            }
        }
        return false;
    }

    public synchronized void adicionarPostagem(Long chefId, String texto) {
        adicionarPostagem(chefId, texto, null);
    }

    public synchronized void adicionarPostagem(Long chefId, String texto, String fotoDataUrl) {
        String textoNormalizado = texto == null ? "" : texto.trim();
        String fotoDataUrlNormalizada = fotoDataUrl == null ? null : fotoDataUrl.trim();
        boolean semTexto = textoNormalizado.isEmpty();
        boolean semFoto = fotoDataUrlNormalizada == null || fotoDataUrlNormalizada.isEmpty();

        if (semTexto && semFoto) {
            return;
        }
        if (chefId == null) {
            return;
        }
        List<PostagemChef> posts = postagensPorChef.computeIfAbsent(chefId, k -> new ArrayList<>());
        posts.add(0, new PostagemChef(seqPostagem++, textoNormalizado, fotoDataUrlNormalizada));
    }

    public synchronized List<PostagemChef> listarPostagensChef(Long chefId) {
        if (chefId == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(postagensPorChef.getOrDefault(chefId, new ArrayList<>()));
    }

    public synchronized boolean editarPostagem(Long chefId, Long postagemId, String novoTexto) {
        if (chefId == null || postagemId == null || novoTexto == null || novoTexto.trim().isEmpty()) {
            return false;
        }
        List<PostagemChef> posts = postagensPorChef.get(chefId);
        if (posts == null) {
            return false;
        }
        for (PostagemChef post : posts) {
            if (postagemId.equals(post.getId())) {
                post.setTexto(novoTexto.trim());
                return true;
            }
        }
        return false;
    }

    public synchronized boolean excluirPostagem(Long chefId, Long postagemId) {
        if (chefId == null || postagemId == null) {
            return false;
        }
        List<PostagemChef> posts = postagensPorChef.get(chefId);
        if (posts == null) {
            return false;
        }
        for (int i = 0; i < posts.size(); i++) {
            if (postagemId.equals(posts.get(i).getId())) {
                posts.remove(i);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean possuiRespostasDoChef(Long chefId) {
        if (chefId == null) {
            return false;
        }
        List<MensagemChef> mensagens = mensagensPorChef.getOrDefault(chefId, new ArrayList<>());
        for (MensagemChef msg : mensagens) {
            if (msg.getRespostaChef() != null && !msg.getRespostaChef().trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static class MensagemChef implements Serializable {
        private final Long id;
        private final Long clienteId;
        private final String nomeCliente;
        private final String textoCliente;
        private String respostaChef;

        public MensagemChef(Long id, Long clienteId, String nomeCliente, String textoCliente) {
            this.id = id;
            this.clienteId = clienteId;
            this.nomeCliente = (nomeCliente == null || nomeCliente.trim().isEmpty()) ? "Cliente" : nomeCliente.trim();
            this.textoCliente = textoCliente;
        }

        public Long getId() {
            return id;
        }

        public String getNomeCliente() {
            return nomeCliente;
        }

        public Long getClienteId() {
            return clienteId;
        }

        public String getTextoCliente() {
            return textoCliente;
        }

        public String getRespostaChef() {
            return respostaChef;
        }

        public void setRespostaChef(String respostaChef) {
            this.respostaChef = respostaChef;
        }
    }

    public static class PostagemChef implements Serializable {
        private final Long id;
        private String texto;
        private final String fotoDataUrl;

        public PostagemChef(Long id, String texto, String fotoDataUrl) {
            this.id = id;
            this.texto = texto;
            this.fotoDataUrl = fotoDataUrl;
        }

        public Long getId() {
            return id;
        }

        public String getTexto() {
            return texto;
        }

        public void setTexto(String texto) {
            this.texto = texto;
        }

        public String getFotoDataUrl() {
            return fotoDataUrl;
        }
    }
}

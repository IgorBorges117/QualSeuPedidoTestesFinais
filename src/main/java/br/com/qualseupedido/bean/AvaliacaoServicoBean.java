package br.com.qualseupedido.bean;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Named
@ApplicationScoped
public class AvaliacaoServicoBean implements Serializable {

    private static final DateTimeFormatter FORMATO_DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final List<Integer> ESTRELAS = Collections.unmodifiableList(Arrays.asList(1, 2, 3, 4, 5));

    private final List<AvaliacaoContrato> avaliacoes = new ArrayList<>();
    private final Map<Long, AvaliacaoContrato> porSolicitacao = new HashMap<>();
    private long seq = 1L;

    public synchronized boolean registrarAvaliacao(Long solicitacaoId,
                                                   Long chefId,
                                                   String chefNome,
                                                   Long clienteId,
                                                   String clienteNome,
                                                   Integer nota,
                                                   String comentario) {
        if (solicitacaoId == null || chefId == null || clienteId == null || nota == null) {
            return false;
        }
        if (nota < 1 || nota > 5) {
            return false;
        }
        if (porSolicitacao.containsKey(solicitacaoId)) {
            return false;
        }

        String comentarioNormalizado = comentario == null ? "" : comentario.trim();
        AvaliacaoContrato avaliacao = new AvaliacaoContrato(
                seq++,
                solicitacaoId,
                chefId,
                nomePadrao(chefNome, "Chef"),
                clienteId,
                nomePadrao(clienteNome, "Cliente"),
                nota,
                comentarioNormalizado,
                LocalDate.now()
        );

        avaliacoes.add(avaliacao);
        porSolicitacao.put(solicitacaoId, avaliacao);
        return true;
    }

    public synchronized boolean possuiAvaliacaoParaSolicitacao(Long solicitacaoId) {
        return solicitacaoId != null && porSolicitacao.containsKey(solicitacaoId);
    }

    public synchronized List<AvaliacaoContrato> getAvaliacoes() {
        return Collections.unmodifiableList(avaliacoes);
    }

    public synchronized List<AvaliacaoContrato> avaliacoesChef(Long chefId) {
        if (chefId == null) {
            return Collections.emptyList();
        }
        List<AvaliacaoContrato> lista = new ArrayList<>();
        for (int i = avaliacoes.size() - 1; i >= 0; i--) {
            AvaliacaoContrato avaliacao = avaliacoes.get(i);
            if (chefId.equals(avaliacao.getChefId())) {
                lista.add(avaliacao);
            }
        }
        return lista;
    }

    public synchronized int totalAvaliacoesChef(Long chefId) {
        if (chefId == null) {
            return 0;
        }
        int total = 0;
        for (AvaliacaoContrato avaliacao : avaliacoes) {
            if (chefId.equals(avaliacao.getChefId())) {
                total++;
            }
        }
        return total;
    }

    public synchronized double mediaChef(Long chefId) {
        if (chefId == null) {
            return 0.0;
        }
        int total = 0;
        int soma = 0;
        for (AvaliacaoContrato avaliacao : avaliacoes) {
            if (chefId.equals(avaliacao.getChefId())) {
                total++;
                soma += avaliacao.getNota();
            }
        }
        if (total == 0) {
            return 0.0;
        }
        return ((double) soma) / total;
    }

    public String formatarMediaChef(Long chefId) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(new Locale("pt", "BR"));
        DecimalFormat df = new DecimalFormat("0.0", symbols);
        return df.format(mediaChef(chefId));
    }

    public int notaArredondadaChef(Long chefId) {
        return (int) Math.round(mediaChef(chefId));
    }

    public List<Integer> getEstrelas() {
        return ESTRELAS;
    }

    public String iniciaisNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return "CL";
        }
        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 1) {
            String primeira = partes[0].substring(0, 1).toUpperCase(Locale.ROOT);
            return primeira + primeira;
        }
        return (partes[0].substring(0, 1) + partes[partes.length - 1].substring(0, 1)).toUpperCase(Locale.ROOT);
    }

    private String nomePadrao(String nome, String padrao) {
        if (nome == null || nome.trim().isEmpty()) {
            return padrao;
        }
        return nome.trim();
    }

    public static class AvaliacaoContrato implements Serializable {
        private final Long id;
        private final Long solicitacaoId;
        private final Long chefId;
        private final String chefNome;
        private final Long clienteId;
        private final String clienteNome;
        private final int nota;
        private final String comentario;
        private final LocalDate dataAvaliacao;

        public AvaliacaoContrato(Long id,
                                 Long solicitacaoId,
                                 Long chefId,
                                 String chefNome,
                                 Long clienteId,
                                 String clienteNome,
                                 int nota,
                                 String comentario,
                                 LocalDate dataAvaliacao) {
            this.id = id;
            this.solicitacaoId = solicitacaoId;
            this.chefId = chefId;
            this.chefNome = chefNome;
            this.clienteId = clienteId;
            this.clienteNome = clienteNome;
            this.nota = nota;
            this.comentario = comentario;
            this.dataAvaliacao = dataAvaliacao;
        }

        public Long getId() {
            return id;
        }

        public Long getSolicitacaoId() {
            return solicitacaoId;
        }

        public Long getChefId() {
            return chefId;
        }

        public String getChefNome() {
            return chefNome;
        }

        public Long getClienteId() {
            return clienteId;
        }

        public String getClienteNome() {
            return clienteNome;
        }

        public int getNota() {
            return nota;
        }

        public String getComentario() {
            return comentario;
        }

        public String getComentarioExibicao() {
            if (comentario == null || comentario.trim().isEmpty()) {
                return "Cliente nao deixou comentario.";
            }
            return comentario;
        }

        public LocalDate getDataAvaliacao() {
            return dataAvaliacao;
        }

        public String getDataAvaliacaoFormatada() {
            if (dataAvaliacao == null) {
                return "";
            }
            return dataAvaliacao.format(FORMATO_DATA_BR);
        }
    }
}

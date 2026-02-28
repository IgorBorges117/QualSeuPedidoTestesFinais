package br.com.qualseupedido.bean;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

@Named
@ViewScoped
public class SolicitacaoPainelBean implements Serializable {

    private static final DateTimeFormatter FORMATO_DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA_BR = DateTimeFormatter.ofPattern("HH:mm");

    @Inject
    private AuthBean authBean;
    @Inject
    private SolicitacaoServicoBean solicitacaoServicoBean;
    @Inject
    private AvaliacaoServicoBean avaliacaoServicoBean;
    @Inject
    private UsuarioBean usuarioBean;

    private final Map<Long, String> rascunhosChef = new HashMap<>();
    private final Map<Long, String> rascunhosCliente = new HashMap<>();
    private final Map<Long, String> notasAvaliacaoCliente = new HashMap<>();
    private final Map<Long, String> comentariosAvaliacaoCliente = new HashMap<>();

    public List<SolicitacaoServicoBean.SolicitacaoServico> getSolicitacoesChef() {
        if (!authBean.isCozinheiro() || authBean.getUsuarioLogado() == null) {
            return Collections.emptyList();
        }
        return solicitacaoServicoBean.listarPorChef(authBean.getUsuarioLogado().getId());
    }

    public List<SolicitacaoServicoBean.SolicitacaoServico> getSolicitacoesChefPendentes() {
        return filtrarSolicitacoes(getSolicitacoesChef(), SolicitacaoServicoBean.STATUS_PENDENTE);
    }

    public List<SolicitacaoServicoBean.SolicitacaoServico> getSolicitacoesChefNegociacao() {
        return filtrarSolicitacoes(getSolicitacoesChef(),
                SolicitacaoServicoBean.STATUS_ACEITO,
                SolicitacaoServicoBean.STATUS_EM_NEGOCIACAO);
    }

    public List<SolicitacaoServicoBean.SolicitacaoServico> getSolicitacoesCliente() {
        if (!authBean.isCliente() || authBean.getUsuarioLogado() == null) {
            return Collections.emptyList();
        }
        return solicitacaoServicoBean.listarPorCliente(authBean.getUsuarioLogado().getId());
    }

    public List<SolicitacaoServicoBean.SolicitacaoServico> getSolicitacoesClientePendentes() {
        return filtrarSolicitacoes(getSolicitacoesCliente(), SolicitacaoServicoBean.STATUS_PENDENTE);
    }

    public List<SolicitacaoServicoBean.SolicitacaoServico> getSolicitacoesClienteNegociacao() {
        return filtrarSolicitacoes(getSolicitacoesCliente(),
                SolicitacaoServicoBean.STATUS_ACEITO,
                SolicitacaoServicoBean.STATUS_EM_NEGOCIACAO);
    }

    public int getTotalSolicitacoesChefPendentes() {
        return getSolicitacoesChefPendentes().size();
    }

    public int getTotalSolicitacoesChefNegociacao() {
        return getSolicitacoesChefNegociacao().size();
    }

    public int getTotalSolicitacoesClientePendentes() {
        return getSolicitacoesClientePendentes().size();
    }

    public int getTotalSolicitacoesClienteNegociacao() {
        return getSolicitacoesClienteNegociacao().size();
    }

    public int getNotificacoesClienteAceite() {
        if (!authBean.isCliente() || authBean.getUsuarioLogado() == null) {
            return 0;
        }
        return solicitacaoServicoBean.contarAceitasPorCliente(authBean.getUsuarioLogado().getId());
    }

    public List<SolicitacaoServicoBean.SolicitacaoServico> getContratacoesChef() {
        if (!authBean.isCozinheiro() || authBean.getUsuarioLogado() == null) {
            return Collections.emptyList();
        }
        List<SolicitacaoServicoBean.SolicitacaoServico> origem = solicitacaoServicoBean.listarPorChef(authBean.getUsuarioLogado().getId());
        List<SolicitacaoServicoBean.SolicitacaoServico> contratos = new ArrayList<>();
        for (SolicitacaoServicoBean.SolicitacaoServico solicitacao : origem) {
            if (SolicitacaoServicoBean.STATUS_CONTRATADO.equals(solicitacao.getStatus())) {
                contratos.add(solicitacao);
            }
        }
        return contratos;
    }

    public List<SolicitacaoServicoBean.SolicitacaoServico> getContratacoesCliente() {
        if (!authBean.isCliente() || authBean.getUsuarioLogado() == null) {
            return Collections.emptyList();
        }
        List<SolicitacaoServicoBean.SolicitacaoServico> origem = solicitacaoServicoBean.listarPorCliente(authBean.getUsuarioLogado().getId());
        List<SolicitacaoServicoBean.SolicitacaoServico> contratos = new ArrayList<>();
        for (SolicitacaoServicoBean.SolicitacaoServico solicitacao : origem) {
            if (SolicitacaoServicoBean.STATUS_CONTRATADO.equals(solicitacao.getStatus())) {
                contratos.add(solicitacao);
            }
        }
        return contratos;
    }

    public boolean podeAvaliarCliente(SolicitacaoServicoBean.SolicitacaoServico solicitacao) {
        if (!authBean.isCliente() || authBean.getUsuarioLogado() == null || solicitacao == null) {
            return false;
        }
        if (!SolicitacaoServicoBean.STATUS_CONTRATADO.equals(solicitacao.getStatus())) {
            return false;
        }
        if (!authBean.getUsuarioLogado().getId().equals(solicitacao.getClienteId())) {
            return false;
        }
        return !avaliacaoServicoBean.possuiAvaliacaoParaSolicitacao(solicitacao.getId());
    }

    public boolean jaAvaliado(Long solicitacaoId) {
        return avaliacaoServicoBean.possuiAvaliacaoParaSolicitacao(solicitacaoId);
    }

    public String enviarMensagemChef(Long solicitacaoId) {
        if (!authBean.isCozinheiro() || authBean.getUsuarioLogado() == null) {
            return null;
        }
        String texto = rascunhosChef.get(solicitacaoId);
        boolean ok = solicitacaoServicoBean.enviarMensagemChef(solicitacaoId, authBean.getUsuarioLogado().getId(), texto);
        if (ok) {
            rascunhosChef.put(solicitacaoId, "");
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Mensagem enviada", "Resposta enviada para o cliente.");
        } else {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Falha no envio", "Não foi possível enviar a mensagem.");
        }
        return null;
    }

    public String enviarMensagemCliente(Long solicitacaoId) {
        if (!authBean.isCliente() || authBean.getUsuarioLogado() == null) {
            return null;
        }
        String texto = rascunhosCliente.get(solicitacaoId);
        boolean ok = solicitacaoServicoBean.enviarMensagemCliente(solicitacaoId, authBean.getUsuarioLogado().getId(), texto);
        if (ok) {
            rascunhosCliente.put(solicitacaoId, "");
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Mensagem enviada", "Mensagem enviada para o chef.");
        } else {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Falha no envio", "Não foi possível enviar a mensagem.");
        }
        return null;
    }

    public String confirmarComoChef(Long solicitacaoId) {
        if (!authBean.isCozinheiro() || authBean.getUsuarioLogado() == null) {
            return null;
        }
        boolean ok = solicitacaoServicoBean.confirmarContratacaoChef(solicitacaoId, authBean.getUsuarioLogado().getId());
        if (ok) {
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Confirmação registrada", "Agora aguarde a confirmação do cliente.");
        } else {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Falha", "Não foi possível confirmar esta contratação.");
        }
        return null;
    }

    public String confirmarComoCliente(Long solicitacaoId) {
        if (!authBean.isCliente() || authBean.getUsuarioLogado() == null) {
            return null;
        }
        boolean ok = solicitacaoServicoBean.confirmarContratacaoCliente(solicitacaoId, authBean.getUsuarioLogado().getId());
        if (ok) {
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Confirmação registrada", "Agora aguarde a confirmação do chef.");
        } else {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Falha", "Não foi possível confirmar esta contratação.");
        }
        return null;
    }

    public String enviarAvaliacaoCliente(Long solicitacaoId) {
        if (!authBean.isCliente() || authBean.getUsuarioLogado() == null) {
            return null;
        }

        SolicitacaoServicoBean.SolicitacaoServico solicitacao = solicitacaoServicoBean.buscarPorId(solicitacaoId);
        if (solicitacao == null || !authBean.getUsuarioLogado().getId().equals(solicitacao.getClienteId())) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Falha", "Não foi possível localizar este contrato.");
            return null;
        }
        if (!SolicitacaoServicoBean.STATUS_CONTRATADO.equals(solicitacao.getStatus())) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Avaliação indisponível", "Apenas contratos finalizados podem ser avaliados.");
            return null;
        }
        if (avaliacaoServicoBean.possuiAvaliacaoParaSolicitacao(solicitacaoId)) {
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Avaliação existente", "Este contrato já foi avaliado.");
            return null;
        }

        Integer nota = parseNotaAvaliacao(notasAvaliacaoCliente.get(solicitacaoId));
        if (nota == null || nota < 1 || nota > 5) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Nota obrigatória", "Selecione uma nota de 1 a 5 estrelas.");
            return null;
        }

        String comentario = comentariosAvaliacaoCliente.get(solicitacaoId);
        if (comentario == null || comentario.trim().isEmpty()) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Comentário obrigatório", "Escreva um comentário sobre o serviço.");
            return null;
        }

        boolean ok = avaliacaoServicoBean.registrarAvaliacao(
                solicitacao.getId(),
                solicitacao.getChefId(),
                solicitacao.getChefNome(),
                solicitacao.getClienteId(),
                solicitacao.getClienteNome(),
                nota,
                comentario
        );

        if (ok) {
            notasAvaliacaoCliente.remove(solicitacaoId);
            comentariosAvaliacaoCliente.remove(solicitacaoId);
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Avaliação enviada", "Obrigado por avaliar o chef.");
        } else {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Falha", "Não foi possível registrar a avaliação.");
        }
        return null;
    }

    public boolean exibeNegociacao(String status) {
        return SolicitacaoServicoBean.STATUS_ACEITO.equals(status)
                || SolicitacaoServicoBean.STATUS_EM_NEGOCIACAO.equals(status)
                || SolicitacaoServicoBean.STATUS_CONTRATADO.equals(status);
    }

    public String statusLabel(String status) {
        if (SolicitacaoServicoBean.STATUS_PENDENTE.equals(status)) return "Pendente";
        if (SolicitacaoServicoBean.STATUS_ACEITO.equals(status)) return "Aceito";
        if (SolicitacaoServicoBean.STATUS_RECUSADO.equals(status)) return "Recusado";
        if (SolicitacaoServicoBean.STATUS_CANCELADO.equals(status)) return "Cancelado";
        if (SolicitacaoServicoBean.STATUS_EM_NEGOCIACAO.equals(status)) return "Em negociação";
        if (SolicitacaoServicoBean.STATUS_CONTRATADO.equals(status)) return "Contratado";
        return status;
    }

    public String statusClass(String status) {
        if (SolicitacaoServicoBean.STATUS_PENDENTE.equals(status)) return "status-chip pending";
        if (SolicitacaoServicoBean.STATUS_ACEITO.equals(status)) return "status-chip accepted";
        if (SolicitacaoServicoBean.STATUS_RECUSADO.equals(status)) return "status-chip rejected";
        if (SolicitacaoServicoBean.STATUS_CANCELADO.equals(status)) return "status-chip canceled";
        if (SolicitacaoServicoBean.STATUS_EM_NEGOCIACAO.equals(status)) return "status-chip negotiating";
        if (SolicitacaoServicoBean.STATUS_CONTRATADO.equals(status)) return "status-chip contracted";
        return "status-chip";
    }

    public Map<Long, String> getRascunhosChef() {
        return rascunhosChef;
    }

    public Map<Long, String> getRascunhosCliente() {
        return rascunhosCliente;
    }

    public Map<Long, String> getNotasAvaliacaoCliente() {
        return notasAvaliacaoCliente;
    }

    public Map<Long, String> getComentariosAvaliacaoCliente() {
        return comentariosAvaliacaoCliente;
    }

    public String formatarData(String data) {
        if (data == null || data.trim().isEmpty()) {
            return "-";
        }

        String valor = data.trim();
        if (valor.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                return LocalDate.parse(valor).format(FORMATO_DATA_BR);
            } catch (DateTimeParseException ignored) {
                return valor;
            }
        }
        return valor;
    }

    public String formatarHorario(String horario) {
        if (horario == null || horario.trim().isEmpty()) {
            return "-";
        }

        String valor = horario.trim();
        if (valor.matches("\\d{2}:\\d{2}(:\\d{2})?")) {
            try {
                return LocalTime.parse(valor).format(FORMATO_HORA_BR);
            } catch (DateTimeParseException ignored) {
                return valor;
            }
        }
        return valor;
    }

    public String codigoComprovante(Long solicitacaoId) {
        if (solicitacaoId == null) {
            return "QSP-CONTRATO";
        }
        return String.format(Locale.ROOT, "QSP-%05d", solicitacaoId);
    }

    public String enderecoClienteComprovante(SolicitacaoServicoBean.SolicitacaoServico contrato) {
        if (contrato == null || contrato.getClienteId() == null) {
            return "Não informado";
        }
        UsuarioSistema cliente = usuarioBean.buscarPorId(contrato.getClienteId());
        if (cliente == null) {
            return "Não informado";
        }
        return formatarEnderecoCompleto(cliente);
    }

    private String formatarEnderecoCompleto(UsuarioSistema cliente) {
        String endereco = valorTexto(cliente.getEndereco());
        String numero = valorTexto(cliente.getNumero());
        String complemento = valorTexto(cliente.getComplemento());
        String bairro = valorTexto(cliente.getBairro());
        String cidade = valorTexto(cliente.getCidade());
        String estado = valorTexto(cliente.getEstado());
        String cep = valorTexto(cliente.getCep());

        StringBuilder logradouro = new StringBuilder();
        if (endereco != null) {
            logradouro.append(endereco);
        }
        if (numero != null) {
            if (logradouro.length() > 0) {
                logradouro.append(", ");
            }
            logradouro.append(numero);
        }
        if (complemento != null) {
            if (logradouro.length() > 0) {
                logradouro.append(" - ");
            }
            logradouro.append(complemento);
        }

        StringBuilder cidadeEstado = new StringBuilder();
        if (cidade != null) {
            cidadeEstado.append(cidade);
        }
        if (estado != null) {
            if (cidadeEstado.length() > 0) {
                cidadeEstado.append(" - ");
            }
            cidadeEstado.append(estado);
        }

        StringJoiner joiner = new StringJoiner(" | ");
        if (logradouro.length() > 0) {
            joiner.add(logradouro.toString());
        }
        if (bairro != null) {
            joiner.add("Bairro: " + bairro);
        }
        if (cidadeEstado.length() > 0) {
            joiner.add(cidadeEstado.toString());
        }
        if (cep != null) {
            joiner.add("CEP: " + cep);
        }

        return joiner.length() == 0 ? "Não informado" : joiner.toString();
    }

    private String valorTexto(String valor) {
        if (valor == null) {
            return null;
        }
        String texto = valor.trim();
        return texto.isEmpty() ? null : texto;
    }

    private Integer parseNotaAvaliacao(String notaTexto) {
        if (notaTexto == null || notaTexto.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(notaTexto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void adicionarMensagem(FacesMessage.Severity severity, String resumo, String detalhe) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, resumo, detalhe));
    }

    private List<SolicitacaoServicoBean.SolicitacaoServico> filtrarSolicitacoes(
            List<SolicitacaoServicoBean.SolicitacaoServico> origem,
            String... status) {
        if (origem == null || origem.isEmpty()) {
            return Collections.emptyList();
        }
        List<SolicitacaoServicoBean.SolicitacaoServico> resultado = new ArrayList<>();
        for (SolicitacaoServicoBean.SolicitacaoServico solicitacao : origem) {
            if (solicitacao != null && statusEm(solicitacao.getStatus(), status)) {
                resultado.add(solicitacao);
            }
        }
        return resultado;
    }

    private boolean statusEm(String valor, String... status) {
        if (valor == null || status == null) {
            return false;
        }
        for (String item : status) {
            if (valor.equals(item)) {
                return true;
            }
        }
        return false;
    }
}

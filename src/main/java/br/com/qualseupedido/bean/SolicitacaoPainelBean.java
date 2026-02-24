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

    private final Map<Long, String> rascunhosChef = new HashMap<>();
    private final Map<Long, String> rascunhosCliente = new HashMap<>();
    private final Map<Long, Integer> notasAvaliacaoCliente = new HashMap<>();
    private final Map<Long, String> comentariosAvaliacaoCliente = new HashMap<>();

    public List<SolicitacaoServicoBean.SolicitacaoServico> getSolicitacoesChef() {
        if (!authBean.isCozinheiro() || authBean.getUsuarioLogado() == null) {
            return Collections.emptyList();
        }
        return solicitacaoServicoBean.listarPorChef(authBean.getUsuarioLogado().getId());
    }

    public List<SolicitacaoServicoBean.SolicitacaoServico> getSolicitacoesCliente() {
        if (!authBean.isCliente() || authBean.getUsuarioLogado() == null) {
            return Collections.emptyList();
        }
        return solicitacaoServicoBean.listarPorCliente(authBean.getUsuarioLogado().getId());
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
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Falha no envio", "Nao foi possivel enviar a mensagem.");
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
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Falha no envio", "Nao foi possivel enviar a mensagem.");
        }
        return null;
    }

    public String confirmarComoChef(Long solicitacaoId) {
        if (!authBean.isCozinheiro() || authBean.getUsuarioLogado() == null) {
            return null;
        }
        boolean ok = solicitacaoServicoBean.confirmarContratacaoChef(solicitacaoId, authBean.getUsuarioLogado().getId());
        if (ok) {
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Confirmacao registrada", "Agora aguarde a confirmacao do cliente.");
        } else {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Falha", "Nao foi possivel confirmar esta contratacao.");
        }
        return null;
    }

    public String confirmarComoCliente(Long solicitacaoId) {
        if (!authBean.isCliente() || authBean.getUsuarioLogado() == null) {
            return null;
        }
        boolean ok = solicitacaoServicoBean.confirmarContratacaoCliente(solicitacaoId, authBean.getUsuarioLogado().getId());
        if (ok) {
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Confirmacao registrada", "Agora aguarde a confirmacao do chef.");
        } else {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Falha", "Nao foi possivel confirmar esta contratacao.");
        }
        return null;
    }

    public String enviarAvaliacaoCliente(Long solicitacaoId) {
        if (!authBean.isCliente() || authBean.getUsuarioLogado() == null) {
            return null;
        }

        SolicitacaoServicoBean.SolicitacaoServico solicitacao = solicitacaoServicoBean.buscarPorId(solicitacaoId);
        if (solicitacao == null || !authBean.getUsuarioLogado().getId().equals(solicitacao.getClienteId())) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Falha", "Nao foi possivel localizar este contrato.");
            return null;
        }
        if (!SolicitacaoServicoBean.STATUS_CONTRATADO.equals(solicitacao.getStatus())) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Avaliacao indisponivel", "Apenas contratos finalizados podem ser avaliados.");
            return null;
        }
        if (avaliacaoServicoBean.possuiAvaliacaoParaSolicitacao(solicitacaoId)) {
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Avaliacao existente", "Este contrato ja foi avaliado.");
            return null;
        }

        Integer nota = notasAvaliacaoCliente.get(solicitacaoId);
        if (nota == null || nota < 1 || nota > 5) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Nota obrigatoria", "Selecione uma nota de 1 a 5 estrelas.");
            return null;
        }

        String comentario = comentariosAvaliacaoCliente.get(solicitacaoId);
        if (comentario == null || comentario.trim().isEmpty()) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Comentario obrigatorio", "Escreva um comentario sobre o servico.");
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
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Avaliacao enviada", "Obrigado por avaliar o chef.");
        } else {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Falha", "Nao foi possivel registrar a avaliacao.");
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
        if (SolicitacaoServicoBean.STATUS_EM_NEGOCIACAO.equals(status)) return "Em negociacao";
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

    public Map<Long, Integer> getNotasAvaliacaoCliente() {
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

    private void adicionarMensagem(FacesMessage.Severity severity, String resumo, String detalhe) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, resumo, detalhe));
    }
}

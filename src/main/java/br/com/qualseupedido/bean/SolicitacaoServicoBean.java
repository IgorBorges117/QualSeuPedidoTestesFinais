package br.com.qualseupedido.bean;

import br.com.qualseupedido.entidade.Prato;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Named
@ApplicationScoped
public class SolicitacaoServicoBean implements Serializable {

    public static final String STATUS_PENDENTE = "PENDENTE";
    public static final String STATUS_ACEITO = "ACEITO";
    public static final String STATUS_RECUSADO = "RECUSADO";
    public static final String STATUS_CANCELADO = "CANCELADO";
    public static final String STATUS_EM_NEGOCIACAO = "EM_NEGOCIACAO";
    public static final String STATUS_CONTRATADO = "CONTRATADO";

    private long seq = 1L;
    private long seqMensagem = 1L;
    private final List<SolicitacaoServico> solicitacoes = new ArrayList<>();

    @Inject
    private UsuarioBean usuarioBean;

    public String getStatusPendente() {
        return STATUS_PENDENTE;
    }

    public String getStatusAceito() {
        return STATUS_ACEITO;
    }

    public String getStatusRecusado() {
        return STATUS_RECUSADO;
    }

    public String getStatusCancelado() {
        return STATUS_CANCELADO;
    }

    public String getStatusEmNegociacao() {
        return STATUS_EM_NEGOCIACAO;
    }

    public String getStatusContratado() {
        return STATUS_CONTRATADO;
    }

    public synchronized void criarSolicitacao(Long chefId,
                                              String chefNome,
                                              Long clienteId,
                                              String clienteNome,
                                              List<Prato> pratos,
                                              String dataEvento,
                                              String horarioEvento,
                                              Integer quantidadePessoasEvento) {
        if (!usuarioAtivo(chefId) || !usuarioAtivo(clienteId)) {
            return;
        }

        List<String> nomesPratos = new ArrayList<>();
        double total = 0.0;
        for (Prato prato : pratos) {
            nomesPratos.add(prato.getNome());
            if (prato.getPreco() != null) {
                total += prato.getPreco();
            }
        }
        int pessoas = quantidadePessoasEvento != null && quantidadePessoasEvento > 0 ? quantidadePessoasEvento : 1;
        total = total * pessoas;

        solicitacoes.add(new SolicitacaoServico(
                seq++,
                chefId,
                chefNome,
                clienteId,
                clienteNome,
                nomesPratos,
                total,
                dataEvento,
                horarioEvento,
                quantidadePessoasEvento
        ));
    }

    public synchronized List<SolicitacaoServico> listarPorChef(Long chefId) {
        if (chefId == null) {
            return Collections.emptyList();
        }
        List<SolicitacaoServico> resultado = new ArrayList<>();
        for (SolicitacaoServico s : solicitacoes) {
            if (chefId.equals(s.getChefId())) {
                resultado.add(s);
            }
        }
        return resultado;
    }

    public synchronized List<SolicitacaoServico> listarTodas() {
        return new ArrayList<>(solicitacoes);
    }

    public synchronized int contarPendentesPorChef(Long chefId) {
        if (chefId == null) {
            return 0;
        }
        int total = 0;
        for (SolicitacaoServico s : solicitacoes) {
            if (chefId.equals(s.getChefId()) && STATUS_PENDENTE.equals(s.getStatus())) {
                total++;
            }
        }
        return total;
    }

    public synchronized int contarAceitasPorCliente(Long clienteId) {
        if (clienteId == null) {
            return 0;
        }
        int total = 0;
        for (SolicitacaoServico s : solicitacoes) {
            if (clienteId.equals(s.getClienteId()) && STATUS_ACEITO.equals(s.getStatus())) {
                total++;
            }
        }
        return total;
    }

    public synchronized List<SolicitacaoServico> listarPorCliente(Long clienteId) {
        if (clienteId == null) {
            return Collections.emptyList();
        }
        List<SolicitacaoServico> resultado = new ArrayList<>();
        for (SolicitacaoServico s : solicitacoes) {
            if (clienteId.equals(s.getClienteId())) {
                resultado.add(s);
            }
        }
        return resultado;
    }

    public synchronized void aceitar(Long id, Long chefId) {
        SolicitacaoServico solicitacao = buscarPorId(id);
        if (solicitacao == null || !chefCorresponde(solicitacao, chefId)) {
            return;
        }
        if (!STATUS_PENDENTE.equals(solicitacao.getStatus())) {
            return;
        }
        solicitacao.setStatus(STATUS_ACEITO);
        solicitacao.setContratacaoConfirmadaChef(false);
        solicitacao.setContratacaoConfirmadaCliente(false);
    }

    public synchronized void recusar(Long id, Long chefId) {
        SolicitacaoServico solicitacao = buscarPorId(id);
        if (solicitacao == null || !chefCorresponde(solicitacao, chefId)) {
            return;
        }
        if (!STATUS_PENDENTE.equals(solicitacao.getStatus())) {
            return;
        }
        solicitacao.setStatus(STATUS_RECUSADO);
    }

    public synchronized void cancelarComoCliente(Long id, Long clienteId) {
        SolicitacaoServico solicitacao = buscarPorId(id);
        if (solicitacao == null || !clienteCorresponde(solicitacao, clienteId)) {
            return;
        }
        if (STATUS_RECUSADO.equals(solicitacao.getStatus())
                || STATUS_CANCELADO.equals(solicitacao.getStatus())
                || STATUS_CONTRATADO.equals(solicitacao.getStatus())) {
            return;
        }
        solicitacao.setStatus(STATUS_CANCELADO);
    }

    public synchronized boolean enviarMensagemChef(Long id, Long chefId, String texto) {
        SolicitacaoServico solicitacao = buscarPorId(id);
        if (solicitacao == null || !chefCorresponde(solicitacao, chefId)) {
            return false;
        }
        if (!usuarioAtivo(solicitacao.getChefId()) || !usuarioAtivo(solicitacao.getClienteId())) {
            return false;
        }
        if (texto == null || texto.trim().isEmpty()) {
            return false;
        }
        if (!statusPermiteNegociacao(solicitacao.getStatus())) {
            return false;
        }
        solicitacao.getMensagensNegociacao().add(new MensagemNegociacao(seqMensagem++, true, texto.trim()));
        if (STATUS_ACEITO.equals(solicitacao.getStatus())) {
            solicitacao.setStatus(STATUS_EM_NEGOCIACAO);
        }
        return true;
    }

    public synchronized boolean enviarMensagemCliente(Long id, Long clienteId, String texto) {
        SolicitacaoServico solicitacao = buscarPorId(id);
        if (solicitacao == null || !clienteCorresponde(solicitacao, clienteId)) {
            return false;
        }
        if (!usuarioAtivo(solicitacao.getChefId()) || !usuarioAtivo(solicitacao.getClienteId())) {
            return false;
        }
        if (texto == null || texto.trim().isEmpty()) {
            return false;
        }
        if (!statusPermiteNegociacao(solicitacao.getStatus())) {
            return false;
        }
        solicitacao.getMensagensNegociacao().add(new MensagemNegociacao(seqMensagem++, false, texto.trim()));
        if (STATUS_ACEITO.equals(solicitacao.getStatus())) {
            solicitacao.setStatus(STATUS_EM_NEGOCIACAO);
        }
        return true;
    }

    public synchronized boolean editarMensagemNegociacao(Long solicitacaoId, Long mensagemId, String novoTexto) {
        if (solicitacaoId == null || mensagemId == null || novoTexto == null || novoTexto.trim().isEmpty()) {
            return false;
        }
        SolicitacaoServico solicitacao = buscarPorId(solicitacaoId);
        if (solicitacao == null) {
            return false;
        }
        for (MensagemNegociacao mensagem : solicitacao.getMensagensNegociacao()) {
            if (mensagemId.equals(mensagem.getId())) {
                mensagem.setTexto(novoTexto.trim());
                return true;
            }
        }
        return false;
    }

    public synchronized boolean excluirMensagemNegociacao(Long solicitacaoId, Long mensagemId) {
        if (solicitacaoId == null || mensagemId == null) {
            return false;
        }
        SolicitacaoServico solicitacao = buscarPorId(solicitacaoId);
        if (solicitacao == null) {
            return false;
        }

        List<MensagemNegociacao> mensagens = solicitacao.getMensagensNegociacao();
        for (int i = 0; i < mensagens.size(); i++) {
            if (mensagemId.equals(mensagens.get(i).getId())) {
                mensagens.remove(i);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean confirmarContratacaoChef(Long id, Long chefId) {
        SolicitacaoServico solicitacao = buscarPorId(id);
        if (solicitacao == null || !chefCorresponde(solicitacao, chefId) || !statusPermiteNegociacao(solicitacao.getStatus())) {
            return false;
        }
        solicitacao.setContratacaoConfirmadaChef(true);
        atualizarStatusContratacao(solicitacao);
        return true;
    }

    public synchronized boolean confirmarContratacaoCliente(Long id, Long clienteId) {
        SolicitacaoServico solicitacao = buscarPorId(id);
        if (solicitacao == null || !clienteCorresponde(solicitacao, clienteId) || !statusPermiteNegociacao(solicitacao.getStatus())) {
            return false;
        }
        solicitacao.setContratacaoConfirmadaCliente(true);
        atualizarStatusContratacao(solicitacao);
        return true;
    }

    public synchronized SolicitacaoServico buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        for (SolicitacaoServico s : solicitacoes) {
            if (id.equals(s.getId())) {
                return s;
            }
        }
        return null;
    }

    private boolean chefCorresponde(SolicitacaoServico s, Long chefId) {
        return chefId != null && chefId.equals(s.getChefId());
    }

    private boolean clienteCorresponde(SolicitacaoServico s, Long clienteId) {
        return clienteId != null && clienteId.equals(s.getClienteId());
    }

    private boolean statusPermiteNegociacao(String status) {
        return STATUS_ACEITO.equals(status) || STATUS_EM_NEGOCIACAO.equals(status) || STATUS_CONTRATADO.equals(status);
    }

    private boolean usuarioAtivo(Long usuarioId) {
        UsuarioSistema usuario = usuarioBean.buscarPorId(usuarioId);
        return usuario != null && !usuario.isSuspensoAgora();
    }

    private void atualizarStatusContratacao(SolicitacaoServico solicitacao) {
        if (solicitacao.isContratacaoConfirmadaChef() && solicitacao.isContratacaoConfirmadaCliente()) {
            solicitacao.setStatus(STATUS_CONTRATADO);
        } else if (STATUS_ACEITO.equals(solicitacao.getStatus())) {
            solicitacao.setStatus(STATUS_EM_NEGOCIACAO);
        }
    }

    public static class SolicitacaoServico implements Serializable {
        private final Long id;
        private final Long chefId;
        private final String chefNome;
        private final Long clienteId;
        private final String clienteNome;
        private final List<String> pratos;
        private final double total;
        private final String dataEvento;
        private final String horarioEvento;
        private final Integer quantidadePessoasEvento;
        private final List<MensagemNegociacao> mensagensNegociacao;
        private boolean contratacaoConfirmadaChef;
        private boolean contratacaoConfirmadaCliente;
        private String status;

        public SolicitacaoServico(Long id,
                                  Long chefId,
                                  String chefNome,
                                  Long clienteId,
                                  String clienteNome,
                                  List<String> pratos,
                                  double total,
                                  String dataEvento,
                                  String horarioEvento,
                                  Integer quantidadePessoasEvento) {
            this.id = id;
            this.chefId = chefId;
            this.chefNome = chefNome;
            this.clienteId = clienteId;
            this.clienteNome = clienteNome;
            this.pratos = pratos;
            this.total = total;
            this.dataEvento = dataEvento;
            this.horarioEvento = horarioEvento;
            this.quantidadePessoasEvento = quantidadePessoasEvento;
            this.mensagensNegociacao = new ArrayList<>();
            this.status = STATUS_PENDENTE;
            this.contratacaoConfirmadaChef = false;
            this.contratacaoConfirmadaCliente = false;
        }

        public Long getId() {
            return id;
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

        public List<String> getPratos() {
            return pratos;
        }

        public String getPratosResumo() {
            return String.join(", ", pratos);
        }

        public double getTotal() {
            return total;
        }

        public String getDataEvento() {
            return dataEvento;
        }

        public String getHorarioEvento() {
            return horarioEvento;
        }

        public Integer getQuantidadePessoasEvento() {
            return quantidadePessoasEvento;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<MensagemNegociacao> getMensagensNegociacao() {
            return mensagensNegociacao;
        }

        public boolean isContratacaoConfirmadaChef() {
            return contratacaoConfirmadaChef;
        }

        public void setContratacaoConfirmadaChef(boolean contratacaoConfirmadaChef) {
            this.contratacaoConfirmadaChef = contratacaoConfirmadaChef;
        }

        public boolean isContratacaoConfirmadaCliente() {
            return contratacaoConfirmadaCliente;
        }

        public void setContratacaoConfirmadaCliente(boolean contratacaoConfirmadaCliente) {
            this.contratacaoConfirmadaCliente = contratacaoConfirmadaCliente;
        }
    }

    public static class MensagemNegociacao implements Serializable {
        private final Long id;
        private final boolean enviadaPorChef;
        private String texto;

        public MensagemNegociacao(Long id, boolean enviadaPorChef, String texto) {
            this.id = id;
            this.enviadaPorChef = enviadaPorChef;
            this.texto = texto;
        }

        public Long getId() {
            return id;
        }

        public boolean isEnviadaPorChef() {
            return enviadaPorChef;
        }

        public String getTexto() {
            return texto;
        }

        public void setTexto(String texto) {
            this.texto = texto;
        }
    }
}

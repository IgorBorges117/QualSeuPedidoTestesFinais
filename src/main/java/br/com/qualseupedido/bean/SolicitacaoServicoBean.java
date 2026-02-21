package br.com.qualseupedido.bean;

import br.com.qualseupedido.entidade.Prato;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Named
@ApplicationScoped
public class SolicitacaoServicoBean implements Serializable {

    private long seq = 1L;
    private final List<SolicitacaoServico> solicitacoes = new ArrayList<>();

    public synchronized void criarSolicitacao(Long chefId,
                                              String chefNome,
                                              Long clienteId,
                                              String clienteNome,
                                              List<Prato> pratos,
                                              String dataEvento,
                                              String horarioEvento,
                                              String observacoes) {
        List<String> nomesPratos = new ArrayList<>();
        double total = 0.0;
        for (Prato prato : pratos) {
            nomesPratos.add(prato.getNome());
            if (prato.getPreco() != null) {
                total += prato.getPreco();
            }
        }

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
                observacoes
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
        if (!"PENDENTE".equals(solicitacao.getStatus())) {
            return;
        }
        solicitacao.setStatus("ACEITO");
    }

    public synchronized void recusar(Long id, Long chefId) {
        SolicitacaoServico solicitacao = buscarPorId(id);
        if (solicitacao == null || !chefCorresponde(solicitacao, chefId)) {
            return;
        }
        if (!"PENDENTE".equals(solicitacao.getStatus())) {
            return;
        }
        solicitacao.setStatus("RECUSADO");
    }

    public synchronized void cancelarComoCliente(Long id, Long clienteId) {
        SolicitacaoServico solicitacao = buscarPorId(id);
        if (solicitacao == null || !clienteCorresponde(solicitacao, clienteId)) {
            return;
        }
        if ("RECUSADO".equals(solicitacao.getStatus()) || "CANCELADO".equals(solicitacao.getStatus())) {
            return;
        }
        solicitacao.setStatus("CANCELADO");
    }

    private SolicitacaoServico buscarPorId(Long id) {
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
        private final String observacoes;
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
                                  String observacoes) {
            this.id = id;
            this.chefId = chefId;
            this.chefNome = chefNome;
            this.clienteId = clienteId;
            this.clienteNome = clienteNome;
            this.pratos = pratos;
            this.total = total;
            this.dataEvento = dataEvento;
            this.horarioEvento = horarioEvento;
            this.observacoes = observacoes;
            this.status = "PENDENTE";
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

        public String getObservacoes() {
            return observacoes;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}

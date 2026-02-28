package br.com.qualseupedido.bean;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Named
@ViewScoped
public class AdminBean implements Serializable {

    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Pattern TELEFONE_PATTERN = Pattern.compile("^[0-9()+\\-\\s]{8,20}$");
    private static final Pattern CEP_PATTERN = Pattern.compile("^\\d{5}-?\\d{3}$");
    private static final Pattern NUMERO_PATTERN = Pattern.compile("^[0-9A-Za-z\\-\\/]{1,20}$");

    private static final String[] ESPECIALIDADES_CHEF = new String[]{
            "Chef particular",
            "Culinaria brasileira",
            "Culinaria italiana",
            "Culinaria japonesa",
            "Culinaria francesa",
            "Churrasco e grelhados",
            "Massas artesanais",
            "Confeitaria e sobremesas",
            "Cozinha vegetariana",
            "Cozinha vegana",
            "Cozinha fitness",
            "Eventos corporativos"
    };
    private static final String[] PREFERENCIAS_CLIENTE = new String[]{
            "Sem restricao",
            "Vegetariana",
            "Vegana",
            "Sem gluten",
            "Sem lactose"
    };
    private static final String[] DISPONIBILIDADES_CHEF = new String[]{
            "Hoje",
            "Fim de semana"
    };
    private static final String[] FAIXAS_PRECO_CHEF = new String[]{
            "$",
            "$$",
            "$$$"
    };
    private static final Set<String> SET_ESPECIALIDADES_CHEF = new HashSet<>(Arrays.asList(ESPECIALIDADES_CHEF));
    private static final Set<String> SET_PREFERENCIAS_CLIENTE = new HashSet<>(Arrays.asList(PREFERENCIAS_CLIENTE));
    private static final Set<String> SET_DISPONIBILIDADES_CHEF = new HashSet<>(Arrays.asList(DISPONIBILIDADES_CHEF));
    private static final Set<String> SET_FAIXAS_PRECO_CHEF = new HashSet<>(Arrays.asList(FAIXAS_PRECO_CHEF));

    @Inject
    private AuthBean authBean;
    @Inject
    private UsuarioBean usuarioBean;
    @Inject
    private ChefConteudoBean chefConteudoBean;
    @Inject
    private SolicitacaoServicoBean solicitacaoServicoBean;

    private String filtroUsuarios;
    private String filtroConteudo;

    private Long usuarioSelecionadoId;
    private String nomeEdicao;
    private String emailEdicao;
    private String telefoneEdicao;
    private String cidadeEdicao;
    private String estadoEdicao;
    private String enderecoEdicao;
    private String numeroEdicao;
    private String bairroEdicao;
    private String cepEdicao;
    private String preferenciaEdicao;
    private String disponibilidadeChefEdicao;
    private String faixaPrecoChefEdicao;

    private boolean suspensaoIndeterminada;
    private Integer diasSuspensao;
    private String motivoSuspensao;

    private Long postagemChefIdSelecionada;
    private Long postagemIdSelecionada;
    private String postagemTextoEdicao;

    private Long solicitacaoIdSelecionada;
    private Long mensagemIdSelecionada;
    private String mensagemTextoEdicao;

    public boolean isAdminAtivo() {
        return authBean.isAdmin();
    }

    public List<UsuarioSistema> getUsuariosGerenciaveis() {
        List<UsuarioSistema> resultado = new ArrayList<>();
        String filtro = normalizarFiltro(filtroUsuarios);
        for (UsuarioSistema usuario : usuarioBean.getUsuarios()) {
            if (usuario.getTipo() == TipoUsuario.ADMIN) {
                continue;
            }
            if (filtro == null || combinaFiltroUsuario(usuario, filtro)) {
                resultado.add(usuario);
            }
        }
        resultado.sort(Comparator.comparingLong(UsuarioSistema::getId));
        return resultado;
    }

    public String selecionarUsuario(Long usuarioId) {
        if (!isAdminAtivo()) {
            return null;
        }
        UsuarioSistema usuario = usuarioBean.buscarPorId(usuarioId);
        if (usuario == null || usuario.getTipo() == TipoUsuario.ADMIN) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Usuario invalido", "Nao foi possivel selecionar o usuario.");
            return null;
        }

        usuarioSelecionadoId = usuario.getId();
        nomeEdicao = usuario.getNome();
        emailEdicao = usuario.getEmail();
        telefoneEdicao = usuario.getTelefone();
        cidadeEdicao = usuario.getCidade();
        estadoEdicao = usuario.getEstado();
        enderecoEdicao = usuario.getEndereco();
        numeroEdicao = usuario.getNumero();
        bairroEdicao = usuario.getBairro();
        cepEdicao = usuario.getCep();
        preferenciaEdicao = usuario.getPreferenciaAlimentar();
        disponibilidadeChefEdicao = usuario.getDisponibilidadeChef();
        faixaPrecoChefEdicao = usuario.getFaixaPrecoChef();
        suspensaoIndeterminada = false;
        diasSuspensao = null;
        motivoSuspensao = null;
        postagemChefIdSelecionada = null;
        postagemIdSelecionada = null;
        postagemTextoEdicao = null;
        solicitacaoIdSelecionada = null;
        mensagemIdSelecionada = null;
        mensagemTextoEdicao = null;

        adicionarMensagem(FacesMessage.SEVERITY_INFO, "Usuario selecionado", "Agora voce pode editar perfil ou suspensao.");
        return null;
    }

    public String salvarPerfilUsuarioSelecionado() {
        if (!isAdminAtivo()) {
            return null;
        }
        UsuarioSistema usuario = getUsuarioSelecionado();
        if (usuario == null) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Usuario obrigatorio", "Selecione um usuario antes de salvar.");
            return null;
        }
        if (nomeEdicao == null || nomeEdicao.trim().isEmpty()) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Nome obrigatorio", "Informe o nome do usuario.");
            return null;
        }
        if (emailEdicao == null || emailEdicao.trim().isEmpty()) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "E-mail obrigatorio", "Informe um e-mail valido.");
            return null;
        }
        if (!usuarioBean.emailFormatoValido(emailEdicao)) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "E-mail invalido", "Informe um e-mail no formato correto.");
            return null;
        }
        if (!usuarioBean.atualizarEmail(usuario.getId(), emailEdicao)) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "E-mail indisponivel", "E-mail invalido ou ja utilizado.");
            return null;
        }
        if (!telefoneValido(telefoneEdicao)) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Telefone invalido", "Use apenas numeros e simbolos validos.");
            return null;
        }
        if (!cepValido(cepEdicao)) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "CEP invalido", "Informe um CEP no formato 00000-000.");
            return null;
        }
        if (!numeroValido(numeroEdicao)) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Numero invalido", "Informe um numero de endereco valido.");
            return null;
        }

        usuario.setNome(nomeEdicao.trim());
        usuario.setTelefone(textoOuNull(telefoneEdicao));
        usuario.setCidade(textoOuNull(cidadeEdicao));
        usuario.setEstado(textoOuNull(estadoEdicao));
        usuario.setEndereco(textoOuNull(enderecoEdicao));
        usuario.setNumero(textoOuNull(numeroEdicao));
        usuario.setBairro(textoOuNull(bairroEdicao));
        usuario.setCep(textoOuNull(cepEdicao));
        if (usuario.getTipo() == TipoUsuario.COZINHEIRO) {
            if (!valorEmListaOpcional(preferenciaEdicao, SET_ESPECIALIDADES_CHEF)) {
                adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Especialidade invalida", "Selecione uma especialidade valida.");
                return null;
            }
            if (!valorEmListaOpcional(disponibilidadeChefEdicao, SET_DISPONIBILIDADES_CHEF)) {
                adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Disponibilidade invalida", "Selecione uma disponibilidade valida.");
                return null;
            }
            if (!valorEmListaOpcional(faixaPrecoChefEdicao, SET_FAIXAS_PRECO_CHEF)) {
                adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Faixa de preco invalida", "Selecione uma faixa de preco valida.");
                return null;
            }
            usuario.setPreferenciaAlimentar(textoOuNull(preferenciaEdicao));
            usuario.setDisponibilidadeChef(textoOuNull(disponibilidadeChefEdicao));
            usuario.setFaixaPrecoChef(textoOuNull(faixaPrecoChefEdicao));
        } else {
            if (!valorEmListaOpcional(preferenciaEdicao, SET_PREFERENCIAS_CLIENTE)) {
                adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Preferencia invalida", "Selecione uma preferencia valida.");
                return null;
            }
            usuario.setPreferenciaAlimentar(textoOuNull(preferenciaEdicao));
        }


        adicionarMensagem(FacesMessage.SEVERITY_INFO, "Perfil atualizado", "Dados do usuario salvos com sucesso.");
        return null;
    }

    public String aplicarSuspensaoUsuarioSelecionado() {
        if (!isAdminAtivo()) {
            return null;
        }
        UsuarioSistema usuario = getUsuarioSelecionado();
        if (usuario == null) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Usuario obrigatorio", "Selecione um usuario antes de suspender.");
            return null;
        }
        if (motivoSuspensao == null || motivoSuspensao.trim().isEmpty()) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Motivo obrigatorio", "Informe o motivo da suspensao.");
            return null;
        }
        if (suspensaoIndeterminada) {
            usuario.suspenderIndeterminado(motivoSuspensao);
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Suspensao aplicada", "Usuario suspenso por tempo indeterminado.");
            return null;
        }
        if (diasSuspensao == null || diasSuspensao <= 0) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Periodo invalido", "Informe dias de suspensao maiores que zero.");
            return null;
        }

        LocalDateTime fim = LocalDateTime.now().plusDays(diasSuspensao);
        usuario.suspenderAte(fim, motivoSuspensao);
        adicionarMensagem(FacesMessage.SEVERITY_INFO, "Suspensao aplicada", "Usuario suspenso ate " + fim.format(FORMATO_DATA_HORA) + ".");
        return null;
    }

    public String removerSuspensaoUsuarioSelecionado() {
        if (!isAdminAtivo()) {
            return null;
        }
        UsuarioSistema usuario = getUsuarioSelecionado();
        if (usuario == null) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Usuario obrigatorio", "Selecione um usuario antes de remover suspensao.");
            return null;
        }
        usuario.removerSuspensao();
        adicionarMensagem(FacesMessage.SEVERITY_INFO, "Suspensao removida", "Usuario liberado novamente.");
        return null;
    }

    public String descricaoSuspensao(UsuarioSistema usuario) {
        if (usuario == null || !usuario.isSuspensoAgora()) {
            return "Ativo";
        }
        String motivo = textoOuNull(usuario.getMotivoSuspensao());
        if (usuario.isSuspensaoIndeterminada()) {
            return motivo == null
                    ? "Suspenso por tempo indeterminado"
                    : "Suspenso por tempo indeterminado. Motivo: " + motivo;
        }
        if (usuario.getSuspensoAte() != null) {
            String base = "Suspenso ate " + usuario.getSuspensoAte().format(FORMATO_DATA_HORA);
            return motivo == null ? base : base + ". Motivo: " + motivo;
        }
        return motivo == null ? "Suspenso" : "Suspenso. Motivo: " + motivo;
    }

    public List<PostagemModeracaoItem> getPostagensModeracao() {
        List<PostagemModeracaoItem> itens = new ArrayList<>();
        UsuarioSistema selecionado = getUsuarioSelecionado();
        if (selecionado == null || selecionado.getTipo() != TipoUsuario.COZINHEIRO) {
            return itens;
        }
        String filtro = normalizarFiltro(filtroConteudo);
        for (ChefConteudoBean.PostagemChef postagem : chefConteudoBean.listarPostagensChef(selecionado.getId())) {
            PostagemModeracaoItem item = new PostagemModeracaoItem(
                    selecionado.getId(),
                    selecionado.getNome(),
                    postagem.getId(),
                    postagem.getTexto(),
                    postagem.getFotoDataUrl() != null && !postagem.getFotoDataUrl().trim().isEmpty()
            );
            if (filtro == null || item.combinaFiltro(filtro)) {
                itens.add(item);
            }
        }
        itens.sort(Comparator.comparingLong(PostagemModeracaoItem::getPostagemId).reversed());
        return itens;
    }

    public String selecionarPostagem(Long chefId, Long postagemId) {
        if (!isAdminAtivo()) {
            return null;
        }
        if (chefId == null || postagemId == null) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Postagem invalida", "Selecione uma postagem valida.");
            return null;
        }

        ChefConteudoBean.PostagemChef postagem = buscarPostagem(chefId, postagemId);
        if (postagem == null) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Postagem nao encontrada", "Atualize a lista e tente novamente.");
            return null;
        }

        postagemChefIdSelecionada = chefId;
        postagemIdSelecionada = postagemId;
        postagemTextoEdicao = postagem.getTexto();
        adicionarMensagem(FacesMessage.SEVERITY_INFO, "Postagem selecionada", "Edite o texto ou remova a postagem.");
        return null;
    }

    public String salvarPostagemSelecionada() {
        if (!isAdminAtivo()) {
            return null;
        }
        if (postagemChefIdSelecionada == null || postagemIdSelecionada == null) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Postagem obrigatoria", "Selecione uma postagem para editar.");
            return null;
        }
        boolean ok = chefConteudoBean.editarPostagem(postagemChefIdSelecionada, postagemIdSelecionada, postagemTextoEdicao);
        if (ok) {
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Postagem atualizada", "Texto da postagem salvo.");
        } else {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Falha", "Nao foi possivel salvar a postagem.");
        }
        return null;
    }

    public String excluirPostagemSelecionada() {
        if (!isAdminAtivo()) {
            return null;
        }
        if (postagemChefIdSelecionada == null || postagemIdSelecionada == null) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Postagem obrigatoria", "Selecione uma postagem para excluir.");
            return null;
        }
        boolean ok = chefConteudoBean.excluirPostagem(postagemChefIdSelecionada, postagemIdSelecionada);
        if (ok) {
            postagemChefIdSelecionada = null;
            postagemIdSelecionada = null;
            postagemTextoEdicao = null;
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Postagem removida", "Postagem excluida com sucesso.");
        } else {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Falha", "Nao foi possivel excluir a postagem.");
        }
        return null;
    }

    public List<MensagemModeracaoItem> getMensagensModeracao() {
        List<MensagemModeracaoItem> itens = new ArrayList<>();
        UsuarioSistema selecionado = getUsuarioSelecionado();
        if (selecionado == null) {
            return itens;
        }
        Long usuarioId = selecionado.getId();
        String filtro = normalizarFiltro(filtroConteudo);

        for (SolicitacaoServicoBean.SolicitacaoServico solicitacao : solicitacaoServicoBean.listarTodas()) {
            if (!usuarioId.equals(solicitacao.getChefId()) && !usuarioId.equals(solicitacao.getClienteId())) {
                continue;
            }
            for (SolicitacaoServicoBean.MensagemNegociacao mensagem : solicitacao.getMensagensNegociacao()) {
                MensagemModeracaoItem item = new MensagemModeracaoItem(
                        solicitacao.getId(),
                        mensagem.getId(),
                        solicitacao.getChefNome(),
                        solicitacao.getClienteNome(),
                        mensagem.isEnviadaPorChef(),
                        mensagem.getTexto()
                );
                if (filtro == null || item.combinaFiltro(filtro)) {
                    itens.add(item);
                }
            }
        }

        itens.sort(Comparator.comparingLong(MensagemModeracaoItem::getMensagemId).reversed());
        return itens;
    }

    public String selecionarMensagem(Long solicitacaoId, Long mensagemId) {
        if (!isAdminAtivo()) {
            return null;
        }
        SolicitacaoServicoBean.MensagemNegociacao mensagem = buscarMensagem(solicitacaoId, mensagemId);
        if (mensagem == null) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Mensagem nao encontrada", "Atualize a lista e tente novamente.");
            return null;
        }

        solicitacaoIdSelecionada = solicitacaoId;
        mensagemIdSelecionada = mensagemId;
        mensagemTextoEdicao = mensagem.getTexto();
        adicionarMensagem(FacesMessage.SEVERITY_INFO, "Mensagem selecionada", "Edite o texto ou exclua a mensagem.");
        return null;
    }

    public String salvarMensagemSelecionada() {
        if (!isAdminAtivo()) {
            return null;
        }
        boolean ok = solicitacaoServicoBean.editarMensagemNegociacao(solicitacaoIdSelecionada, mensagemIdSelecionada, mensagemTextoEdicao);
        if (ok) {
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Mensagem atualizada", "Mensagem editada com sucesso.");
        } else {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Falha", "Nao foi possivel editar a mensagem.");
        }
        return null;
    }

    public String excluirMensagemSelecionada() {
        if (!isAdminAtivo()) {
            return null;
        }
        boolean ok = solicitacaoServicoBean.excluirMensagemNegociacao(solicitacaoIdSelecionada, mensagemIdSelecionada);
        if (ok) {
            solicitacaoIdSelecionada = null;
            mensagemIdSelecionada = null;
            mensagemTextoEdicao = null;
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Mensagem removida", "Mensagem excluida com sucesso.");
        } else {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Falha", "Nao foi possivel remover a mensagem.");
        }
        return null;
    }

    public String tipoLabel(TipoUsuario tipo) {
        if (tipo == null) {
            return "-";
        }
        if (tipo == TipoUsuario.COZINHEIRO) {
            return "Chef";
        }
        if (tipo == TipoUsuario.CLIENTE) {
            return "Cliente";
        }
        return tipo.name();
    }

    public UsuarioSistema getUsuarioSelecionado() {
        if (usuarioSelecionadoId == null) {
            return null;
        }
        UsuarioSistema usuario = usuarioBean.buscarPorId(usuarioSelecionadoId);
        if (usuario == null || usuario.getTipo() == TipoUsuario.ADMIN) {
            return null;
        }
        return usuario;
    }

    public boolean isUsuarioSelecionadoChef() {
        UsuarioSistema usuario = getUsuarioSelecionado();
        return usuario != null && usuario.getTipo() == TipoUsuario.COZINHEIRO;
    }

    public boolean isUsuarioSelecionadoCliente() {
        UsuarioSistema usuario = getUsuarioSelecionado();
        return usuario != null && usuario.getTipo() == TipoUsuario.CLIENTE;
    }

    public String[] getEspecialidadesChefDisponiveis() {
        return ESPECIALIDADES_CHEF;
    }

    public String[] getPreferenciasClienteDisponiveis() {
        return PREFERENCIAS_CLIENTE;
    }

    public String[] getDisponibilidadesChefDisponiveis() {
        return DISPONIBILIDADES_CHEF;
    }

    public String[] getFaixasPrecoChefDisponiveis() {
        return FAIXAS_PRECO_CHEF;
    }

    private ChefConteudoBean.PostagemChef buscarPostagem(Long chefId, Long postagemId) {
        for (ChefConteudoBean.PostagemChef postagem : chefConteudoBean.listarPostagensChef(chefId)) {
            if (postagemId.equals(postagem.getId())) {
                return postagem;
            }
        }
        return null;
    }

    private SolicitacaoServicoBean.MensagemNegociacao buscarMensagem(Long solicitacaoId, Long mensagemId) {
        if (solicitacaoId == null || mensagemId == null) {
            return null;
        }
        SolicitacaoServicoBean.SolicitacaoServico solicitacao = solicitacaoServicoBean.buscarPorId(solicitacaoId);
        if (solicitacao == null) {
            return null;
        }
        for (SolicitacaoServicoBean.MensagemNegociacao mensagem : solicitacao.getMensagensNegociacao()) {
            if (mensagemId.equals(mensagem.getId())) {
                return mensagem;
            }
        }
        return null;
    }

    private boolean combinaFiltroUsuario(UsuarioSistema usuario, String filtro) {
        return contem(usuario.getNome(), filtro)
                || contem(usuario.getEmail(), filtro)
                || contem(usuario.getCidade(), filtro)
                || contem(usuario.getTelefone(), filtro);
    }

    private String normalizarFiltro(String valor) {
        if (valor == null) {
            return null;
        }
        String filtro = valor.trim().toLowerCase(Locale.ROOT);
        return filtro.isEmpty() ? null : filtro;
    }

    private boolean contem(String valor, String filtro) {
        if (valor == null || filtro == null) {
            return false;
        }
        return valor.toLowerCase(Locale.ROOT).contains(filtro);
    }

    private boolean telefoneValido(String telefone) {
        String valor = textoOuNull(telefone);
        return valor == null || TELEFONE_PATTERN.matcher(valor).matches();
    }

    private boolean cepValido(String cep) {
        String valor = textoOuNull(cep);
        return valor == null || CEP_PATTERN.matcher(valor).matches();
    }

    private boolean numeroValido(String numero) {
        String valor = textoOuNull(numero);
        return valor == null || NUMERO_PATTERN.matcher(valor).matches();
    }

    private boolean valorEmListaOpcional(String valor, Set<String> permitidos) {
        String texto = textoOuNull(valor);
        return texto == null || permitidos.contains(texto);
    }

    private String textoOuNull(String valor) {
        if (valor == null) {
            return null;
        }
        String texto = valor.trim();
        return texto.isEmpty() ? null : texto;
    }

    private void adicionarMensagem(FacesMessage.Severity severity, String resumo, String detalhe) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, resumo, detalhe));
    }

    public String getFiltroUsuarios() {
        return filtroUsuarios;
    }

    public void setFiltroUsuarios(String filtroUsuarios) {
        this.filtroUsuarios = filtroUsuarios;
    }

    public String getFiltroConteudo() {
        return filtroConteudo;
    }

    public void setFiltroConteudo(String filtroConteudo) {
        this.filtroConteudo = filtroConteudo;
    }

    public String getNomeEdicao() {
        return nomeEdicao;
    }

    public void setNomeEdicao(String nomeEdicao) {
        this.nomeEdicao = nomeEdicao;
    }

    public String getEmailEdicao() {
        return emailEdicao;
    }

    public void setEmailEdicao(String emailEdicao) {
        this.emailEdicao = emailEdicao;
    }

    public String getTelefoneEdicao() {
        return telefoneEdicao;
    }

    public void setTelefoneEdicao(String telefoneEdicao) {
        this.telefoneEdicao = telefoneEdicao;
    }

    public String getCidadeEdicao() {
        return cidadeEdicao;
    }

    public void setCidadeEdicao(String cidadeEdicao) {
        this.cidadeEdicao = cidadeEdicao;
    }

    public String getEstadoEdicao() {
        return estadoEdicao;
    }

    public void setEstadoEdicao(String estadoEdicao) {
        this.estadoEdicao = estadoEdicao;
    }

    public String getEnderecoEdicao() {
        return enderecoEdicao;
    }

    public void setEnderecoEdicao(String enderecoEdicao) {
        this.enderecoEdicao = enderecoEdicao;
    }

    public String getNumeroEdicao() {
        return numeroEdicao;
    }

    public void setNumeroEdicao(String numeroEdicao) {
        this.numeroEdicao = numeroEdicao;
    }

    public String getBairroEdicao() {
        return bairroEdicao;
    }

    public void setBairroEdicao(String bairroEdicao) {
        this.bairroEdicao = bairroEdicao;
    }

    public String getCepEdicao() {
        return cepEdicao;
    }

    public void setCepEdicao(String cepEdicao) {
        this.cepEdicao = cepEdicao;
    }

    public String getPreferenciaEdicao() {
        return preferenciaEdicao;
    }

    public void setPreferenciaEdicao(String preferenciaEdicao) {
        this.preferenciaEdicao = preferenciaEdicao;
    }

    public String getDisponibilidadeChefEdicao() {
        return disponibilidadeChefEdicao;
    }

    public void setDisponibilidadeChefEdicao(String disponibilidadeChefEdicao) {
        this.disponibilidadeChefEdicao = disponibilidadeChefEdicao;
    }

    public String getFaixaPrecoChefEdicao() {
        return faixaPrecoChefEdicao;
    }

    public void setFaixaPrecoChefEdicao(String faixaPrecoChefEdicao) {
        this.faixaPrecoChefEdicao = faixaPrecoChefEdicao;
    }

    public boolean isSuspensaoIndeterminada() {
        return suspensaoIndeterminada;
    }

    public void setSuspensaoIndeterminada(boolean suspensaoIndeterminada) {
        this.suspensaoIndeterminada = suspensaoIndeterminada;
        if (suspensaoIndeterminada) {
            this.diasSuspensao = null;
        }
    }

    public Integer getDiasSuspensao() {
        return diasSuspensao;
    }

    public void setDiasSuspensao(Integer diasSuspensao) {
        this.diasSuspensao = diasSuspensao;
    }

    public String getMotivoSuspensao() {
        return motivoSuspensao;
    }

    public void setMotivoSuspensao(String motivoSuspensao) {
        this.motivoSuspensao = motivoSuspensao;
    }

    public Long getPostagemChefIdSelecionada() {
        return postagemChefIdSelecionada;
    }

    public Long getPostagemIdSelecionada() {
        return postagemIdSelecionada;
    }

    public String getPostagemTextoEdicao() {
        return postagemTextoEdicao;
    }

    public void setPostagemTextoEdicao(String postagemTextoEdicao) {
        this.postagemTextoEdicao = postagemTextoEdicao;
    }

    public Long getSolicitacaoIdSelecionada() {
        return solicitacaoIdSelecionada;
    }

    public Long getMensagemIdSelecionada() {
        return mensagemIdSelecionada;
    }

    public String getMensagemTextoEdicao() {
        return mensagemTextoEdicao;
    }

    public void setMensagemTextoEdicao(String mensagemTextoEdicao) {
        this.mensagemTextoEdicao = mensagemTextoEdicao;
    }

    public static class PostagemModeracaoItem implements Serializable {
        private final Long chefId;
        private final String chefNome;
        private final Long postagemId;
        private final String texto;
        private final boolean possuiFoto;

        public PostagemModeracaoItem(Long chefId, String chefNome, Long postagemId, String texto, boolean possuiFoto) {
            this.chefId = chefId;
            this.chefNome = chefNome;
            this.postagemId = postagemId;
            this.texto = texto;
            this.possuiFoto = possuiFoto;
        }

        public Long getChefId() {
            return chefId;
        }

        public String getChefNome() {
            return chefNome;
        }

        public Long getPostagemId() {
            return postagemId;
        }

        public String getTexto() {
            return texto;
        }

        public boolean isPossuiFoto() {
            return possuiFoto;
        }

        public boolean combinaFiltro(String filtro) {
            if (filtro == null) {
                return true;
            }
            String nome = chefNome == null ? "" : chefNome.toLowerCase(Locale.ROOT);
            String conteudo = texto == null ? "" : texto.toLowerCase(Locale.ROOT);
            return nome.contains(filtro) || conteudo.contains(filtro);
        }
    }

    public static class MensagemModeracaoItem implements Serializable {
        private final Long solicitacaoId;
        private final Long mensagemId;
        private final String chefNome;
        private final String clienteNome;
        private final boolean enviadaPorChef;
        private final String texto;

        public MensagemModeracaoItem(Long solicitacaoId,
                                     Long mensagemId,
                                     String chefNome,
                                     String clienteNome,
                                     boolean enviadaPorChef,
                                     String texto) {
            this.solicitacaoId = solicitacaoId;
            this.mensagemId = mensagemId;
            this.chefNome = chefNome;
            this.clienteNome = clienteNome;
            this.enviadaPorChef = enviadaPorChef;
            this.texto = texto;
        }

        public Long getSolicitacaoId() {
            return solicitacaoId;
        }

        public Long getMensagemId() {
            return mensagemId;
        }

        public String getChefNome() {
            return chefNome;
        }

        public String getClienteNome() {
            return clienteNome;
        }

        public boolean isEnviadaPorChef() {
            return enviadaPorChef;
        }

        public String getTexto() {
            return texto;
        }

        public boolean combinaFiltro(String filtro) {
            if (filtro == null) {
                return true;
            }
            String chef = chefNome == null ? "" : chefNome.toLowerCase(Locale.ROOT);
            String cliente = clienteNome == null ? "" : clienteNome.toLowerCase(Locale.ROOT);
            String conteudo = texto == null ? "" : texto.toLowerCase(Locale.ROOT);
            return chef.contains(filtro) || cliente.contains(filtro) || conteudo.contains(filtro);
        }
    }
}

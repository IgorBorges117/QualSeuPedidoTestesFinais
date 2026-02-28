package br.com.qualseupedido.bean;

import br.com.qualseupedido.entidade.Prato;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import br.com.qualseupedido.util.ImagemUtil;
import java.io.InputStream;
import java.util.Locale;

@Named
@ApplicationScoped
public class PratoBean implements Serializable {

    private static final long TAMANHO_MAXIMO = 3L * 1024L * 1024L;
    private static final int MAX_LARGURA_PRATO = 1000;
    private static final int MAX_ALTURA_PRATO = 700;
    private static final float QUALIDADE_JPEG = 0.8f;

    private final List<Prato> pratos = new ArrayList<>();
    private long seq = 1L;

    @Inject
    private UsuarioBean usuarioBean;

    private String nome;
    private String descricao;
    private Double preco;
    private String precoTexto;
    private String categoria = Prato.CATEGORIA_PRINCIPAL;
    private transient Part fotoPrato;
    private Long pratoEmEdicaoId;

    @PostConstruct
    private void carregarPratosDemo() {
        if (!pratos.isEmpty() || usuarioBean == null) {
            return;
        }
        Long anaId = buscarChefIdPorEmail("chef@demo.com");
        if (anaId != null) {
            adicionarPratoDemo(anaId, "Bruschetta clássica", "Pão artesanal com tomate e azeite.", 22.0, Prato.CATEGORIA_ENTRADA, "/demo/pratos/ana-bruschetta.jpg");
            adicionarPratoDemo(anaId, "Risoto de cogumelos", "Arroz cremoso com funghi e parmesão.", 58.0, Prato.CATEGORIA_PRINCIPAL, "/demo/pratos/ana-risoto.jpg");
            adicionarPratoDemo(anaId, "Filé de frango ao molho", "Filé grelhado com ervas e molho suave.", 46.0, Prato.CATEGORIA_PRINCIPAL, "/demo/pratos/ana-frango.png");
            adicionarPratoDemo(anaId, "Pudim de leite", "Sobremesa clássica com calda de caramelo.", 18.0, Prato.CATEGORIA_SOBREMESA, "/demo/pratos/ana-pudim.png");
        }

        Long erickId = buscarChefIdPorEmail("erick@demo.com");
        if (erickId != null) {
            adicionarPratoDemo(erickId, "Soupe à l'oignon", "Sopa francesa com cebolas caramelizadas e gratinada.", 32.0, Prato.CATEGORIA_ENTRADA, "/demo/pratos/erick-soupe-oignon.jpg");
            adicionarPratoDemo(erickId, "Boeuf bourguignon", "Carne cozida lentamente no vinho tinto.", 78.0, Prato.CATEGORIA_PRINCIPAL, "/demo/pratos/erick-boeuf-bourguignon.png");
            adicionarPratoDemo(erickId, "Filé mignon ao molho de vinho", "Filé alto com molho intenso e batatas.", 84.0, Prato.CATEGORIA_PRINCIPAL, "/demo/pratos/erick-file-mignon.jpg");
            adicionarPratoDemo(erickId, "Crème brûlée", "Sobremesa cremosa com açúcar caramelizado.", 26.0, Prato.CATEGORIA_SOBREMESA, "/demo/pratos/erick-creme-brulee.png");
        }

        Long fogacaId = buscarChefIdPorEmail("fogaca@demo.com");
        if (fogacaId != null) {
            adicionarPratoDemo(fogacaId, "Pão de alho artesanal", "Pão tostado com manteiga temperada e alho.", 24.0, Prato.CATEGORIA_ENTRADA, "/demo/pratos/henrique-pao-de-alho.png");
            adicionarPratoDemo(fogacaId, "Costela assada ao fogo", "Costela suculenta preparada lentamente.", 92.0, Prato.CATEGORIA_PRINCIPAL, "/demo/pratos/henrique-costela.jpg");
            adicionarPratoDemo(fogacaId, "Burger defumado", "Blend bovino, queijo e molho especial.", 48.0, Prato.CATEGORIA_PRINCIPAL, "/demo/pratos/henrique-burger-defumado.jpg");
            adicionarPratoDemo(fogacaId, "Pudim de doce de leite", "Sobremesa cremosa com caramelo.", 22.0, Prato.CATEGORIA_SOBREMESA, "/demo/pratos/henrique-pudim-doce-leite.jpg");
        }
    }

    private Long buscarChefIdPorEmail(String email) {
        if (email == null) {
            return null;
        }
        for (UsuarioSistema usuario : usuarioBean.getUsuarios()) {
            if (email.equalsIgnoreCase(usuario.getEmail())) {
                return usuario.getId();
            }
        }
        return null;
    }

    private void adicionarPratoDemo(Long chefId, String nome, String descricao, Double preco, String categoria, String fotoRecurso) {
        String fotoDataUrl = carregarFotoRecurso(fotoRecurso);
        pratos.add(new Prato(
                seq++,
                chefId,
                nome,
                descricao,
                preco,
                fotoDataUrl,
                true,
                normalizarCategoria(categoria)
        ));
    }

    private String carregarFotoRecurso(String recurso) {
        if (recurso == null || recurso.trim().isEmpty()) {
            return null;
        }
        String normalizado = recurso.trim();
        String dataUrl = carregarFotoDoClasspath(normalizado);
        if (dataUrl != null) {
            return dataUrl;
        }

        String recursoRelativo = normalizado.startsWith("/") ? normalizado.substring(1) : normalizado;
        Path path = Paths.get("src", "main", "resources").resolve(recursoRelativo.replace("/", java.io.File.separator));
        if (!Files.exists(path)) {
            return null;
        }
        try (InputStream input = Files.newInputStream(path)) {
            return ImagemUtil.processarDataUrl(input, MAX_LARGURA_PRATO, MAX_ALTURA_PRATO, QUALIDADE_JPEG);
        } catch (IOException e) {
            return null;
        }
    }

    private String carregarFotoDoClasspath(String recurso) {
        try (InputStream input = PratoBean.class.getResourceAsStream(recurso)) {
            if (input == null) {
                return null;
            }
            return ImagemUtil.processarDataUrl(input, MAX_LARGURA_PRATO, MAX_ALTURA_PRATO, QUALIDADE_JPEG);
        } catch (IOException e) {
            return null;
        }
    }

    public List<Prato> getPratos() {
        return Collections.unmodifiableList(pratos);
    }

    public List<Prato> getPratosDoChef(Long chefId) {
        if (chefId == null) {
            return Collections.emptyList();
        }
        List<Prato> resultado = new ArrayList<>();
        for (Prato prato : pratos) {
            if (chefId.equals(prato.getChefId())) {
                resultado.add(prato);
            }
        }
        return resultado;
    }

    public List<Prato> pratosDoChef(Long chefId) {
        return getPratosDoChef(chefId);
    }

    public List<Prato> getPratosDoChefPorCategoria(Long chefId, String categoria) {
        String categoriaNormalizada = normalizarCategoria(categoria);
        List<Prato> resultado = new ArrayList<>();
        for (Prato prato : getPratosDoChef(chefId)) {
            if (categoriaNormalizada.equals(normalizarCategoria(prato.getCategoria()))) {
                resultado.add(prato);
            }
        }
        return resultado;
    }

    public List<Prato> pratosDoChefPorCategoria(Long chefId, String categoria) {
        return getPratosDoChefPorCategoria(chefId, categoria);
    }

    public List<Prato> getPratosPublicosDoChef(Long chefId) {
        if (chefId == null) {
            return Collections.emptyList();
        }
        List<Prato> resultado = new ArrayList<>();
        for (Prato prato : pratos) {
            if (chefId.equals(prato.getChefId()) && prato.isVisivelNoPerfilPublico()) {
                resultado.add(prato);
            }
        }
        return resultado;
    }

    public List<Prato> pratosPublicosDoChef(Long chefId) {
        return getPratosPublicosDoChef(chefId);
    }

    public List<Prato> getPratosPublicosDoChefPorCategoria(Long chefId, String categoria) {
        String categoriaNormalizada = normalizarCategoria(categoria);
        List<Prato> resultado = new ArrayList<>();
        for (Prato prato : getPratosPublicosDoChef(chefId)) {
            if (categoriaNormalizada.equals(normalizarCategoria(prato.getCategoria()))) {
                resultado.add(prato);
            }
        }
        return resultado;
    }

    public List<Prato> pratosPublicosDoChefPorCategoria(Long chefId, String categoria) {
        return getPratosPublicosDoChefPorCategoria(chefId, categoria);
    }

    public Prato buscarDoChefPorId(Long chefId, Long pratoId) {
        if (chefId == null || pratoId == null) {
            return null;
        }
        for (Prato prato : pratos) {
            if (pratoId.equals(prato.getId()) && chefId.equals(prato.getChefId())) {
                return prato;
            }
        }
        return null;
    }

    public Prato buscarPublicoDoChefPorId(Long chefId, Long pratoId) {
        Prato prato = buscarDoChefPorId(chefId, pratoId);
        if (prato == null || !prato.isVisivelNoPerfilPublico()) {
            return null;
        }
        return prato;
    }

    public void alternarVisibilidadeNoPerfil(Long chefId, Long pratoId) {
        Prato prato = buscarDoChefPorId(chefId, pratoId);
        if (prato == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Prato não encontrado", "Não foi possível localizar o prato para alterar visibilidade."));
            return;
        }
        prato.setVisivelNoPerfilPublico(!prato.isVisivelNoPerfilPublico());
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Visibilidade atualizada",
                        prato.isVisivelNoPerfilPublico() ? "Prato exibido para clientes." : "Prato ocultado no perfil publico."));
    }

    public double getPrecoMedioDoChef(Long chefId) {
        List<Prato> lista = getPratosDoChef(chefId);
        if (lista.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        int count = 0;
        for (Prato prato : lista) {
            if (prato.getPreco() != null) {
                total += prato.getPreco();
                count++;
            }
        }
        if (count == 0) {
            return 0.0;
        }
        return total / count;
    }

    public double precoMedioDoChef(Long chefId) {
        return getPrecoMedioDoChef(chefId);
    }

    public String getFaixaPrecoDoChef(Long chefId) {
        double precoMedio = getPrecoMedioDoChef(chefId);
        if (precoMedio <= 0.0) {
            return "$$";
        }
        if (precoMedio < 40.0) {
            return "$";
        }
        if (precoMedio < 90.0) {
            return "$$";
        }
        return "$$$";
    }

    public String faixaPrecoDoChef(Long chefId) {
        return getFaixaPrecoDoChef(chefId);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
        if (preco != null) {
            this.precoTexto = String.valueOf(preco);
        }
    }

    public String getPrecoTexto() {
        return precoTexto;
    }

    public void setPrecoTexto(String precoTexto) {
        this.precoTexto = precoTexto;
    }

    public Part getFotoPrato() {
        return fotoPrato;
    }

    public void setFotoPrato(Part fotoPrato) {
        this.fotoPrato = fotoPrato;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = normalizarCategoria(categoria);
    }

    public String categoriaLabel(String categoria) {
        String categoriaNormalizada = normalizarCategoria(categoria);
        if (Prato.CATEGORIA_ENTRADA.equals(categoriaNormalizada)) {
            return "Entrada";
        }
        if (Prato.CATEGORIA_SOBREMESA.equals(categoriaNormalizada)) {
            return "Sobremesa";
        }
        return "Prato principal";
    }

    public void adicionarPrato(Long chefId) {
        if (chefId == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Chef inválido", "Não foi possível identificar o chef."));
            return;
        }
        if (nome == null || nome.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Nome obrigatório", "Informe o nome do prato."));
            return;
        }
        if (descricao == null || descricao.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Descrição obrigatória", "Informe a descrição do prato."));
            return;
        }
        Double precoValido = parsePreco(precoTexto);
        if (precoValido == null) {
            precoValido = preco;
        }
        if (precoValido == null || precoValido <= 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Preço inválido", "Informe um preço maior que zero."));
            return;
        }

        String fotoDataUrl = null;
        if (fotoPrato != null && fotoPrato.getSize() > 0) {
            if (fotoPrato.getSize() > TAMANHO_MAXIMO) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Arquivo muito grande", "Use uma imagem de até 3MB."));
                return;
            }
            String contentType = fotoPrato.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Formato inválido", "Envie apenas imagem para a foto do prato."));
                return;
            }
            try {
                fotoDataUrl = ImagemUtil.processarDataUrl(
                        fotoPrato.getInputStream(),
                        MAX_LARGURA_PRATO,
                        MAX_ALTURA_PRATO,
                        QUALIDADE_JPEG
                );
            } catch (IOException e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Falha no upload", "Não foi possível salvar a foto do prato."));
                return;
            }
        }

        pratos.add(new Prato(
                seq++,
                chefId,
                nome.trim(),
                descricao.trim(),
                precoValido,
                fotoDataUrl,
                true,
                normalizarCategoria(categoria)
        ));
        limparFormularioBasico();
    }

    public void selecionarParaEdicao(Long chefId, Long pratoId) {
        Prato prato = buscarDoChefPorId(chefId, pratoId);
        if (prato == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Prato não encontrado", "Não foi possível localizar o prato para edição."));
            return;
        }
        pratoEmEdicaoId = pratoId;
        nome = prato.getNome();
        descricao = prato.getDescricao();
        preco = prato.getPreco();
        precoTexto = formatarPrecoEdicao(prato.getPreco());
        categoria = prato.getCategoria();
        fotoPrato = null;
    }

    public void salvarEdicao(Long chefId) {
        if (pratoEmEdicaoId == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Edição inválida", "Selecione um prato para editar."));
            return;
        }
        Prato prato = buscarDoChefPorId(chefId, pratoEmEdicaoId);
        if (prato == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Prato não encontrado", "Não foi possível localizar o prato para salvar alterações."));
            pratoEmEdicaoId = null;
            return;
        }
        if (nome == null || nome.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Nome obrigatório", "Informe o nome do prato."));
            return;
        }
        if (descricao == null || descricao.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Descrição obrigatória", "Informe a descrição do prato."));
            return;
        }
        Double precoValido = parsePreco(precoTexto);
        if (precoValido == null) {
            precoValido = preco;
        }
        if (precoValido == null || precoValido <= 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Preço inválido", "Informe um preço maior que zero."));
            return;
        }

        String fotoDataUrl = prato.getFotoDataUrl();
        if (fotoPrato != null && fotoPrato.getSize() > 0) {
            if (fotoPrato.getSize() > TAMANHO_MAXIMO) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Arquivo muito grande", "Use uma imagem de até 3MB."));
                return;
            }
            String contentType = fotoPrato.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Formato inválido", "Envie apenas imagem para a foto do prato."));
                return;
            }
            try {
                fotoDataUrl = ImagemUtil.processarDataUrl(
                        fotoPrato.getInputStream(),
                        MAX_LARGURA_PRATO,
                        MAX_ALTURA_PRATO,
                        QUALIDADE_JPEG
                );
            } catch (IOException e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Falha no upload", "Não foi possível salvar a foto do prato."));
                return;
            }
        }

        prato.setNome(nome.trim());
        prato.setDescricao(descricao.trim());
        prato.setPreco(precoValido);
        prato.setFotoDataUrl(fotoDataUrl);
        prato.setCategoria(normalizarCategoria(categoria));

        pratoEmEdicaoId = null;
        limparFormularioBasico();
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Prato atualizado", "As alterações foram salvas."));
    }

    public void cancelarEdicao() {
        pratoEmEdicaoId = null;
        categoria = Prato.CATEGORIA_PRINCIPAL;
        limparFormularioBasico();
    }

    public void excluirPrato(Long chefId, Long pratoId) {
        Prato prato = buscarDoChefPorId(chefId, pratoId);
        if (prato == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Prato não encontrado", "Não foi possível localizar o prato para excluir."));
            return;
        }
        pratos.remove(prato);
        if (pratoEmEdicaoId != null && pratoEmEdicaoId.equals(pratoId)) {
            cancelarEdicao();
        }
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Prato excluído", "O prato foi removido do cardápio."));
    }

    public boolean isEditando() {
        return pratoEmEdicaoId != null;
    }

    private void limparFormularioBasico() {
        nome = "";
        descricao = "";
        preco = null;
        precoTexto = "";
        fotoPrato = null;
    }

    private String formatarPrecoEdicao(Double valor) {
        if (valor == null) {
            return "";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "BR"));
        DecimalFormat df = new DecimalFormat("0.00", symbols);
        return df.format(valor);
    }

    private Double parsePreco(String valor) {
        if (valor == null) {
            return null;
        }

        String normalizado = valor
                .trim()
                .replace("R$", "")
                .replace("r$", "")
                .replace(" ", "");
        if (normalizado.isEmpty()) {
            return null;
        }

        normalizado = normalizado.replaceAll("[^0-9,\\.]", "");
        if (normalizado.isEmpty()) {
            return null;
        }

        int ultimaVirgula = normalizado.lastIndexOf(',');
        int ultimoPonto = normalizado.lastIndexOf('.');

        if (ultimaVirgula >= 0 && ultimoPonto >= 0) {
            if (ultimaVirgula > ultimoPonto) {
                normalizado = normalizado.replace(".", "");
                normalizado = normalizado.replace(",", ".");
            } else {
                normalizado = normalizado.replace(",", "");
            }
        } else if (ultimaVirgula >= 0) {
            normalizado = normalizado.replace(",", ".");
        }

        try {
            return Double.valueOf(normalizado);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalizarCategoria(String categoria) {
        if (categoria == null) {
            return Prato.CATEGORIA_PRINCIPAL;
        }
        String valor = categoria.trim().toUpperCase();
        if (Prato.CATEGORIA_ENTRADA.equals(valor) || Prato.CATEGORIA_SOBREMESA.equals(valor)) {
            return valor;
        }
        return Prato.CATEGORIA_PRINCIPAL;
    }

}

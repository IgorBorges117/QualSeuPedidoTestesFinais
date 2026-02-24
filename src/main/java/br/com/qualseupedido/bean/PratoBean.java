package br.com.qualseupedido.bean;

import br.com.qualseupedido.entidade.Prato;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import br.com.qualseupedido.util.ImagemUtil;

@Named
@ApplicationScoped
public class PratoBean implements Serializable {

    private static final long TAMANHO_MAXIMO = 3L * 1024L * 1024L;
    private static final int MAX_LARGURA_PRATO = 1000;
    private static final int MAX_ALTURA_PRATO = 700;
    private static final float QUALIDADE_JPEG = 0.8f;

    private final List<Prato> pratos = new ArrayList<>();
    private long seq = 1L;

    private String nome;
    private String descricao;
    private Double preco;
    private String precoTexto;
    private String categoria = Prato.CATEGORIA_PRINCIPAL;
    private transient Part fotoPrato;

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
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Prato nao encontrado", "Nao foi possivel localizar o prato para alterar visibilidade."));
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
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Chef invalido", "Nao foi possivel identificar o chef."));
            return;
        }
        if (nome == null || nome.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Nome obrigatorio", "Informe o nome do prato."));
            return;
        }
        if (descricao == null || descricao.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Descricao obrigatoria", "Informe a descricao do prato."));
            return;
        }
        Double precoValido = parsePreco(precoTexto);
        if (precoValido == null) {
            precoValido = preco;
        }
        if (precoValido == null || precoValido <= 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Preco invalido", "Informe um preco maior que zero."));
            return;
        }

        String fotoDataUrl = null;
        if (fotoPrato != null && fotoPrato.getSize() > 0) {
            if (fotoPrato.getSize() > TAMANHO_MAXIMO) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Arquivo muito grande", "Use uma imagem de ate 3MB."));
                return;
            }
            String contentType = fotoPrato.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Formato invalido", "Envie apenas imagem para a foto do prato."));
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
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Falha no upload", "Nao foi possivel salvar a foto do prato."));
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
        nome = "";
        descricao = "";
        preco = null;
        precoTexto = "";
        fotoPrato = null;
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

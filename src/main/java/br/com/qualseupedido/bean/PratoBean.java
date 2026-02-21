package br.com.qualseupedido.bean;

import br.com.qualseupedido.entidade.Prato;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Named
@ApplicationScoped
public class PratoBean implements Serializable {

    private static final long TAMANHO_MAXIMO = 3L * 1024L * 1024L;

    private final List<Prato> pratos = new ArrayList<>();
    private long seq = 1L;

    private String nome;
    private String descricao;
    private Double preco;
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
    }

    public Part getFotoPrato() {
        return fotoPrato;
    }

    public void setFotoPrato(Part fotoPrato) {
        this.fotoPrato = fotoPrato;
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
        if (preco == null || preco <= 0) {
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
                fotoDataUrl = paraDataUrl(fotoPrato, contentType);
            } catch (IOException e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Falha no upload", "Nao foi possivel salvar a foto do prato."));
                return;
            }
        }

        pratos.add(new Prato(seq++, chefId, nome.trim(), descricao.trim(), preco, fotoDataUrl, true));
        nome = "";
        descricao = "";
        preco = null;
        fotoPrato = null;
    }

    private String paraDataUrl(Part arquivo, String contentType) throws IOException {
        byte[] bytes = lerBytes(arquivo.getInputStream());
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return "data:" + contentType + ";base64," + base64;
    }

    private byte[] lerBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int lidos;
        while ((lidos = in.read(buffer)) != -1) {
            out.write(buffer, 0, lidos);
        }
        return out.toByteArray();
    }
}
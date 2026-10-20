package org.p1produto.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.p1produto.model.Categoria;
import org.p1produto.model.Produto;
import org.p1produto.model.ProdutoService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProdutoController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNome;
    @FXML private TextField txtDescricao;
    @FXML private DatePicker dpFabricacao;
    @FXML private DatePicker dpValidade;
    @FXML private TextField txtPrecoCompra;
    @FXML private TextField txtPrecoVenda;
    @FXML private TextField txtEstoque;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private TextField txtBuscaCodigo;

    @FXML private TableView<Produto> tblProdutos;
    @FXML private TableColumn<Produto, String> colCodigo;
    @FXML private TableColumn<Produto, String> colNome;
    @FXML private TableColumn<Produto, BigDecimal> colPrecoVenda;
    @FXML private TableColumn<Produto, Integer> colEstoque;
    @FXML private TableColumn<Produto, LocalDate> colValidade;
    @FXML private TableColumn<Produto, String> colCategoria;

    @FXML private TextArea txtAreaRelatorio;

    private ProdutoService produtoService;

    @FXML
    public void initialize() {
        produtoService = new ProdutoService();

        // Configurar colunas da tabela principal
        colCodigo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCodigo()));
        colNome.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNome()));
        colPrecoVenda.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getPrecoVenda()));
        colEstoque.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getQuantidadeEstoque()));
        colValidade.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDataValidade()));
        colCategoria.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCategoria() != null ? data.getValue().getCategoria().getNome() : ""
        ));

        // Popular ComboBox de Categorias pré-definidas (ou ajuste conforme sua preferência)
        cmbCategoria.setItems(FXCollections.observableArrayList(
                new Categoria(1, "Alimentos", "Produtos alimentícios", "Mercearia"),
                new Categoria(2, "Bebidas", "Bebidas em geral", "Bebidas"),
                new Categoria(3, "Limpeza", "Produtos de limpeza", "Higiene")
        ));

        atualizarTabela();
    }

    @FXML
    private void handleCadastrar() {
        try {
            Categoria categoriaSelecionada = cmbCategoria.getSelectionModel().getSelectedItem();
            if (categoriaSelecionada == null) {
                exibirAlerta(Alert.AlertType.WARNING, "Atenção", "Selecione uma categoria.");
                return;
            }

            Produto produto = new Produto(
                    txtCodigo.getText().trim(),
                    txtNome.getText().trim(),
                    txtDescricao.getText().trim(),
                    dpFabricacao.getValue(),
                    dpValidade.getValue(),
                    new BigDecimal(txtPrecoCompra.getText().trim().replace(",", ".")),
                    new BigDecimal(txtPrecoVenda.getText().trim().replace(",", ".")),
                    Integer.parseInt(txtEstoque.getText().trim()),
                    categoriaSelecionada
            );

            produtoService.cadastrar(produto);
            atualizarTabela();
            handleLimpar();
            exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Produto cadastrado com sucesso!");

        } catch (NumberFormatException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Verifique se os campos numéricos estão corretos.");
        } catch (IllegalArgumentException e) {
            exibirAlerta(Alert.AlertType.WARNING, "Validação", e.getMessage());
        }
    }

    @FXML
    private void handleLimpar() {
        txtCodigo.clear();
        txtNome.clear();
        txtDescricao.clear();
        dpFabricacao.setValue(null);
        dpValidade.setValue(null);
        txtPrecoCompra.clear();
        txtPrecoVenda.clear();
        txtEstoque.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        txtBuscaCodigo.clear();
    }

    @FXML
    private void handleSalvar() {
        // Como o serviço já salva a cada alteração, este botão serve para confirmar o estado atual
        exibirAlerta(Alert.AlertType.INFORMATION, "Persistência", "Dados sincronizados com o arquivo CSV com sucesso!");
    }

    @FXML
    private void handleExcluir() {
        Produto selecionado = tblProdutos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            exibirAlerta(Alert.AlertType.WARNING, "Atenção", "Selecione um produto na tabela para excluir.");
            return;
        }

        if (produtoService.excluir(selecionado.getCodigo())) {
            atualizarTabela();
            exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Produto excluído!");
        }
    }

    @FXML
    private void handleBuscar() {
        String codigo = txtBuscaCodigo.getText().trim();
        if (codigo.isEmpty()) {
            exibirAlerta(Alert.AlertType.WARNING, "Atenção", "Digite um código para buscar.");
            return;
        }

        Optional<Produto> opt = produtoService.consultarPorCodigo(codigo);
        if (opt.isPresent()) {
            Produto p = opt.get();
            txtCodigo.setText(p.getCodigo());
            txtNome.setText(p.getNome());
            txtDescricao.setText(p.getDescricao());
            dpFabricacao.setValue(p.getDataFabricacao());
            dpValidade.setValue(p.getDataValidade());
            txtPrecoCompra.setText(p.getPrecoCompra().toString());
            txtPrecoVenda.setText(p.getPrecoVenda().toString());
            txtEstoque.setText(String.valueOf(p.getQuantidadeEstoque()));
            cmbCategoria.setValue(p.getCategoria());
        } else {
            exibirAlerta(Alert.AlertType.INFORMATION, "Busca", "Produto não encontrado.");
        }
    }

    // --- RELATÓRIOS (STREAM API) ---

    @FXML
    private void handleRelatorioVencimento() {
        List<Produto> lista = produtoService.relatorioProximosVencimento();
        StringBuilder sb = new StringBuilder("=== PRODUTOS PRÓXIMOS AO VENCIMENTO (60 DIAS) ===\n\n");
        lista.forEach(p -> sb.append(String.format("Código: %s | Nome: %s | Validade: %s\n",
                p.getCodigo(), p.getNome(), p.getDataValidade())));
        txtAreaRelatorio.setText(sb.toString());
    }

    @FXML
    private void handleRelatorioEstoqueBaixo() {
        List<Produto> lista = produtoService.relatorioEstoqueBaixo();
        StringBuilder sb = new StringBuilder("=== PRODUTOS COM ESTOQUE BAIXO (< 10) ===\n\n");
        lista.forEach(p -> sb.append(String.format("Código: %s | Nome: %s | Estoque: %d\n",
                p.getCodigo(), p.getNome(), p.getQuantidadeEstoque())));
        txtAreaRelatorio.setText(sb.toString());
    }

    @FXML
    private void handleRelatorioMargemMedia() {
        Map<String, Double> margens = produtoService.relatorioMargemLucroMediaPorCategoria();
        StringBuilder sb = new StringBuilder("=== MARGEM DE LUCRO MÉDIA POR CATEGORIA ===\n\n");
        margens.forEach((cat, margem) -> sb.append(String.format("Categoria: %s | Margem Média: R$ %.2f\n", cat, margem)));
        txtAreaRelatorio.setText(sb.toString());
    }

    @FXML
    private void handleRelatorioAgruparSetor() {
        Map<String, List<Produto>> grupos = produtoService.relatorioAgrupadoPorSetor();
        StringBuilder sb = new StringBuilder("=== PRODUTOS AGRUPADOS POR SETOR ===\n\n");
        grupos.forEach((setor, lista) -> {
            sb.append("Setor: ").append(setor).append("\n");
            lista.forEach(p -> sb.append(String.format("   - %s (%s)\n", p.getNome(), p.getCodigo())));
        });
        txtAreaRelatorio.setText(sb.toString());
    }

    private void atualizarTabela() {
        ObservableList<Produto> observableList = FXCollections.observableArrayList(produtoService.listarTodos());
        tblProdutos.setItems(observableList);
    }

    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
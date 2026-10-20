package org.p1produto.model;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

    public class ProdutoService {

        private static final String FILE_PATH = "produtos.csv";
        private static final String SEPARATOR = ";";
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        private List<Produto> produtos;

        public ProdutoService() {
            this.produtos = new ArrayList<>();
            carregarDoCSV();
        }

        // --- MÉTODOS CRUD BÁSICOS ---

        public List<Produto> listarTodos() {
            return new ArrayList<>(produtos);
        }

        public void cadastrar(Produto produto) throws IllegalArgumentException {
            validarProduto(produto);

            boolean existe = produtos.stream()
                    .anyMatch(p -> p.getCodigo().equalsIgnoreCase(produto.getCodigo()));
            if (existe) {
                throw new IllegalArgumentException("Já existe um produto com o código informado.");
            }

            produtos.add(produto);
            salvarNoCSV();
        }

        public boolean excluir(String codigo) {
            boolean removido = produtos.removeIf(p -> p.getCodigo().equalsIgnoreCase(codigo));
            if (removido) {
                salvarNoCSV();
            }
            return removido;
        }

        public Optional<Produto> consultarPorCodigo(String codigo) {
            return produtos.stream()
                    .filter(p -> p.getCodigo().equalsIgnoreCase(codigo))
                    .findFirst();
        }

        // --- REQUISITOS TÉCNICOS: VALIDAÇÃO DE DADOS ---

        private void validarProduto(Produto produto) {
            if (produto.getCodigo() == null || !produto.getCodigo().matches("^[a-zA-Z0-9]{1,10}$")) {
                throw new IllegalArgumentException("O código deve ter entre 1 e 10 caracteres.");
            }
            if (produto.getNome() == null || produto.getNome().trim().length() < 2) {
                throw new IllegalArgumentException("Nome do produto deve ter no mínimo 2 caracteres.");
            }
            if (produto.getDataFabricacao() != null && produto.getDataFabricacao().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Data de fabricação não pode ser posterior à data atual.");
            }
            if (produto.getDataValidade() != null && produto.getDataFabricacao() != null &&
                    produto.getDataValidade().isBefore(produto.getDataFabricacao())) {
                throw new IllegalArgumentException("Data de validade não pode ser anterior à data de fabricação.");
            }
            if (produto.getPrecoCompra() == null || produto.getPrecoVenda() == null ||
                    produto.getPrecoVenda().compareTo(produto.getPrecoCompra()) <= 0) {
                throw new IllegalArgumentException("Preço de venda deve ser estritamente maior que o preço de compra.");
            }
            if (produto.getQuantidadeEstoque() < 0) {
                throw new IllegalArgumentException("Quantidade em estoque não pode ser negativa.");
            }
            if (produto.getCategoria() == null) {
                throw new IllegalArgumentException("Selecione uma categoria válida.");
            }
        }

        // --- PERSISTÊNCIA EM ARQUIVO CSV ---

        private void salvarNoCSV() {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_PATH))) {
                for (Produto p : produtos) {
                    Categoria c = p.getCategoria();
                    String linha = String.join(SEPARATOR,
                            p.getCodigo(),
                            p.getNome(),
                            p.getDescricao() != null ? p.getDescricao() : "",
                            p.getDataFabricacao().format(DATE_FORMATTER),
                            p.getDataValidade().format(DATE_FORMATTER),
                            p.getPrecoCompra().toString(),
                            p.getPrecoVenda().toString(),
                            String.valueOf(p.getQuantidadeEstoque()),
                            String.valueOf(c.getId()),
                            c.getNome(),
                            c.getDescricao() != null ? c.getDescricao() : "",
                            c.getSetor()
                    );
                    writer.write(linha);
                    writer.newLine();
                }
            } catch (IOException e) {
                System.err.println("Erro ao salvar arquivo CSV: " + e.getMessage());
            }
        }

        private void carregarDoCSV() {
            Path path = Paths.get(FILE_PATH);
            if (!Files.exists(path)) return;

            try (BufferedReader reader = Files.newBufferedReader(path)) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    if (linha.trim().isEmpty()) continue;
                    String[] dados = linha.split(SEPARATOR);
                    if (dados.length >= 12) {
                        Categoria categoria = new Categoria(
                                Integer.parseInt(dados[8]),
                                dados[9],
                                dados[10],
                                dados[11]
                        );

                        Produto produto = new Produto(
                                dados[0],
                                dados[1],
                                dados[2],
                                LocalDate.parse(dados[3], DATE_FORMATTER),
                                LocalDate.parse(dados[4], DATE_FORMATTER),
                                new BigDecimal(dados[5]),
                                new BigDecimal(dados[6]),
                                Integer.parseInt(dados[7]),
                                categoria
                        );
                        produtos.add(produto);
                    }
                }
            } catch (IOException | RuntimeException e) {
                System.err.println("Erro ao carregar dados do CSV: " + e.getMessage());
            }
        }

        // --- RELATÓRIOS UTILIZANDO STREAM API ---

        // 1. Produtos próximos ao vencimento (próximos 60 dias)
        public List<Produto> relatorioProximosVencimento() {
            LocalDate hoje = LocalDate.now();
            LocalDate limite = hoje.plusDays(60);
            return produtos.stream()
                    .filter(p -> p.getDataValidade() != null &&
                            !p.getDataValidade().isBefore(hoje) &&
                            !p.getDataValidade().isAfter(limite))
                    .collect(Collectors.toList());
        }

        // 2. Produtos com estoque baixo (menos de 10 unidades)
        public List<Produto> relatorioEstoqueBaixo() {
            return produtos.stream()
                    .filter(p -> p.getQuantidadeEstoque() < 10)
                    .collect(Collectors.toList());
        }

        // 3. Margem de lucro média por categoria
        public Map<String, Double> relatorioMargemLucroMediaPorCategoria() {
            return produtos.stream()
                    .filter(p -> p.getCategoria() != null)
                    .collect(Collectors.groupingBy(
                            p -> p.getCategoria().getNome(),
                            Collectors.averagingDouble(p ->
                                    p.getPrecoVenda().subtract(p.getPrecoCompra()).doubleValue()
                            )
                    ));
        }

        // 4. Produtos agrupados por setor de categoria
        public Map<String, List<Produto>> relatorioAgrupadoPorSetor() {
            return produtos.stream()
                    .filter(p -> p.getCategoria() != null && p.getCategoria().getSetor() != null)
                    .collect(Collectors.groupingBy(p -> p.getCategoria().getSetor()));
        }
    }


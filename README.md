 Sistema de Gerenciamento de Produtos (P1 - JavaFX)

Este é um sistema de gerenciamento de produtos para lojas e supermercados, desenvolvido em Java com interface gráfica JavaFX. O projeto aplica conceitos de Programação Orientada a Objetos (POO), manipulação de arquivos CSV e processamento de dados com a Stream API.

 Funcionalidades
- Cadastro e Validação: Adição de novos produtos com validação rigorosa de regras de negócio (código alfanumérico, datas de validade consistentes, preços e estoque válidos).
- Consulta e Exclusão: Busca de produtos por código e remoção do sistema.
- Persistência de Dados: Salvamento e carregamento automático dos produtos em um arquivo `produtos.csv`.
- Relatórios Avançados (Stream API): Geração de relatórios para produtos próximos ao vencimento, com estoque baixo, cálculo de margem média de lucro e agrupamento por setor.

 Como executar o projeto no IntelliJ IDEA

1. Clone este repositório na sua máquina local:
   `git clone <https://github.com/gilvandr/Projeto_Produto.git>`
2. Abra o IntelliJ IDEA e selecione Open (ou Abrir) e escolha a pasta do projeto.
3. Certifique-se de que o JDK 17 (ou superior) está configurado no projeto (`File > Project Structure > SDK`).
4. Verifique se as dependências do JavaFX estão sendo carregadas corretamente (via Maven/Gradle ou biblioteca local, dependendo da configuração).
5. Localize a classe principal (geralmente `Main.java` contendo o método `start`).
6. Clique com o botão direito na classe e selecione Run 'Main.main()'.

import model.Locação;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;


public class LocaçãoController {

    @FXML
    private TextField IdLivroField;

    @FXML
    private TextField CPFField;

    @FXML
    private DatePicker DataSaidaPicker;

    @FXML
    private DatePicker DataEntregaPicker;

    @FXML
    private Button alugarButton;

    @FXML
    private Button EntregaButton;

    @FXML
    private void switchToInicial() throws IOException {
        App.setRoot("inicial");
    }

    @FXML
    private int idLivro;

    @FXML
    private TableView<Locação> Tabela;

    @FXML
    private TableColumn<Locação, Integer> codigoLocaçãoColumn;

    @FXML
    private TableColumn<Locação, Integer> IdLivroColumn;

    @FXML
    private TableColumn<Locação, String> CPFColumn;

    @FXML
    private TableColumn<Locação, LocalDate> DataEntregaColumn;

    @FXML
    private TableColumn<Locação, LocalDate> DataSaidaColumn;

    @FXML
    private TableColumn<Locação, String> StatusColumn;

    private ObservableList<Locação> locacoesList;

    // Adicione este método para permitir que a classe PagamentoController acesse a lista de locações
    public ObservableList<Locação> getLocacoesList() {
        return locacoesList;
    }

    // Adicione este método para permitir que a classe PrincipalController configure a lista de locações
    public void setLocacoesList(ObservableList<Locação> locacoesList) {
        this.locacoesList = locacoesList;
    }

    @FXML
    private void initialize() {
        // Inicialize a lista de locações
        locacoesList = FXCollections.observableArrayList();

        // Configurar as colunas
        codigoLocaçãoColumn.setCellValueFactory(cellData -> cellData.getValue().codigoLocacaoProperty().asObject());
        IdLivroColumn.setCellValueFactory(cellData -> cellData.getValue().IdLivroProperty().asObject());
        CPFColumn.setCellValueFactory(cellData -> cellData.getValue().cpfProperty());
        DataSaidaColumn.setCellValueFactory(cellData -> cellData.getValue().dataSaidaProperty());
        DataEntregaColumn.setCellValueFactory(cellData -> cellData.getValue().dataEntregaProperty());
        StatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // Configurar o alinhamento das células para centralizar horizontalmente
        codigoLocaçãoColumn.setStyle("-fx-alignment: CENTER;");
        IdLivroColumn.setStyle("-fx-alignment: CENTER;");
        CPFColumn.setStyle("-fx-alignment: CENTER;");
        DataSaidaColumn.setStyle("-fx-alignment: CENTER;");
        DataEntregaColumn.setStyle("-fx-alignment: CENTER;");
        StatusColumn.setStyle("-fx-alignment: CENTER;");

        // Buscar dados do banco e preencher a tabela
        carregarDadosDoBanco();

        // Adicionar a tabela à interface gráfica
        Tabela.setItems(locacoesList);

        // Adicionar o listener de seleção na tabela
        Tabela.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> selecionarLocacaoParaEdicao());

        DataSaidaPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Verifica se a nova data de saída não é nula
            if (newValue != null) {
                // Adiciona 30 dias à data de saída e configura a DataEntregaPicker
                LocalDate dataEntrega = newValue.plusDays(30);
                DataEntregaPicker.setValue(dataEntrega);
            }
        });
        alugarButton.setOnAction(event -> alugar());

        CPFField.textProperty().addListener((observable, oldValue, newValue) -> {
            String cpfLimpo = FormatacaoCPF.formatarCPF(newValue);
            CPFField.setText(cpfLimpo);
        });
    }

    // Método chamado quando uma linha da tabela é selecionada
    private void selecionarLocacaoParaEdicao() {
        Locação locacaoSelecionada = Tabela.getSelectionModel().getSelectedItem();

        if (locacaoSelecionada != null) {
            // Preencher os campos com os dados da locação selecionada
            IdLivroField.setText(String.valueOf(locacaoSelecionada.getIdLivro()));
            CPFField.setText(locacaoSelecionada.getcpf());
            DataSaidaPicker.setValue(locacaoSelecionada.getDataSaida());
            DataEntregaPicker.setValue(locacaoSelecionada.getDataEntrega());

            // Desativar o botão de aluguel durante a edição
            alugarButton.setDisable(true);
        }
    }

    // Método para carregar dados do banco e preencher a lista de locações
    private void carregarDadosDoBanco() {
        try (Connection conexao = ConexaoMysql.conectar()) {
            if (conexao != null) {
                locacoesList = FXCollections.observableArrayList();

                String sql = "SELECT * FROM locacao";
                try (PreparedStatement statement = conexao.prepareStatement(sql);
                    ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        int codigoLocacao = resultSet.getInt("id_locacao");
                        int idLivro = resultSet.getInt("id_livro");
                        String cpf = resultSet.getString("CPF");
                        LocalDate dataSaida = resultSet.getDate("data_saida").toLocalDate();
                        LocalDate dataEntrega = resultSet.getDate("data_entrega").toLocalDate();
                        String status = resultSet.getString("situacao");

                        Locação locacao = new Locação(codigoLocacao, idLivro, cpf, dataSaida, dataEntrega, status);
                        locacoesList.add(locacao);
                    }
                }
            } else {
                System.out.println("Falha na conexão com o banco de dados.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
void ConfirmarEntrega(ActionEvent event) {
    // Obtendo a locação selecionada na tabela
    Locação locacaoSelecionada = Tabela.getSelectionModel().getSelectedItem();

    if (locacaoSelecionada != null) {
        // Verifica se a entrega já foi confirmada
        if ("alugado".equals(locacaoSelecionada.getStatus())) {
            // Calcula a diferença em dias entre as datas
            long diasDiferenca = ChronoUnit.DAYS.between(locacaoSelecionada.getDataSaida(), DataEntregaPicker.getValue());

            // Verifica se a diferença está entre 1 e 30 dias
            if (diasDiferenca >= 1 && diasDiferenca <= 30) {
                locacaoSelecionada.setDataEntrega(DataEntregaPicker.getValue());
                locacaoSelecionada.setStatus("entregue");
            } else {
                // Se a diferença for maior que 30 dias, define como "excedido"
                locacaoSelecionada.setDataEntrega(DataEntregaPicker.getValue());
                locacaoSelecionada.setStatus("excedido");
            }

            // Atualizar a locação no banco de dados
            atualizarLocacaoNoBanco(locacaoSelecionada);

            // Atualizar a tabela
            Tabela.refresh();
        }
    }
}

    // Método para verificar se o livro já está alugado
    private boolean verificarLivroAlugado(int idLivro) {
        try (Connection conexao = ConexaoMysql.conectar()) {
            if (conexao != null) {
                String sql = "SELECT situacao FROM locacao WHERE id_livro = ? AND situacao = 'alugado'";
                try (PreparedStatement statement = conexao.prepareStatement(sql)) {
                    statement.setInt(1, idLivro);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        return resultSet.next(); // Retorna true se o livro estiver alugado, caso contrário, retorna false
                    }
                }
            } else {
                System.out.println("Falha na conexão com o banco de dados.");
                return true; // Retorna true para evitar aluguel em caso de falha na conexão
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // Retorna true para evitar aluguel em caso de exceção SQL
        }
    }

    // Método para atualizar a locação no banco de dados
    private void atualizarLocacaoNoBanco(Locação locacao) {
        try (Connection conexao = ConexaoMysql.conectar()) {
            if (conexao != null) {
                String sql = "UPDATE locacao SET data_entrega = ?, situacao = ? WHERE id_livro = ? AND CPF = ?";
                try (PreparedStatement statement = conexao.prepareStatement(sql)) {
                    // Configurar os parâmetros do PreparedStatement corretamente
                    statement.setDate(1, java.sql.Date.valueOf(locacao.getDataEntrega()));
                    statement.setString(2, locacao.getStatus());
                    statement.setInt(3, locacao.getIdLivro());
                    statement.setString(4, locacao.getcpf());  

                    statement.executeUpdate();

                    Alert sucessoAlert = new Alert(Alert.AlertType.INFORMATION);
                    sucessoAlert.setTitle("");
                    sucessoAlert.setHeaderText(null);
                    sucessoAlert.setContentText("Entrega confirmada!");
                    sucessoAlert.showAndWait();
                }
            } else {
                System.out.println("Falha na conexão com o banco de dados.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void alugar() {
        try (Connection conexao = ConexaoMysql.conectar()) {
            if (conexao != null) {
                // Verificar se o livro já está alugado
                if (verificarLivroAlugado(Integer.parseInt(IdLivroField.getText()))) {
                    // Mostrar um alerta informando que o livro já está alugado
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Aviso");
                    alert.setHeaderText(null);
                    alert.setContentText("O livro já está alugado.");
                    alert.showAndWait();
                    return; // Não prosseguir com o aluguel se o livro já estiver alugado
                }

                // Restante do código de aluguel
                String sql = "INSERT INTO locacao (id_livro, CPF, data_saida, data_entrega, situacao) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement statement = conexao.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, Integer.parseInt(IdLivroField.getText()));
                    statement.setString(2, CPFField.getText());
                    statement.setDate(3, java.sql.Date.valueOf(DataSaidaPicker.getValue()));
                    statement.setDate(4, java.sql.Date.valueOf(DataEntregaPicker.getValue()));
                    statement.setString(5, "alugado");

                    statement.executeUpdate();

                    Alert sucessoAlert = new Alert(Alert.AlertType.INFORMATION);
                    sucessoAlert.setTitle("");
                    sucessoAlert.setHeaderText(null);
                    sucessoAlert.setContentText("Alugado!");
                    sucessoAlert.showAndWait();

                    // Obtém o ID da locação gerado
                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int idLocacao = generatedKeys.getInt(1);

                            // Adicionar a nova locação à lista
                            Locação novaLocacao = new Locação(idLocacao, Integer.parseInt(IdLivroField.getText()), CPFField.getText(),
                                    DataSaidaPicker.getValue(), DataEntregaPicker.getValue(), "alugado");

                            // Certifique-se de que locacoesList seja inicializado antes de chamar add
                            if (locacoesList == null) {
                                locacoesList = FXCollections.observableArrayList();
                            }

                            locacoesList.add(novaLocacao);

                            // Atualizar a tabela
                            Tabela.refresh();

                            // Limpar os campos após a inserção
                            IdLivroField.clear();
                            CPFField.clear();
                            DataSaidaPicker.setValue(null);
                            DataEntregaPicker.setValue(null);
                        } else {
                            System.out.println("Falha ao obter o ID da locação.");
                        }
                    }
                }
            } else {
                System.out.println("Falha na conexão com o banco de dados.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class FormatacaoCPF {

        public static String formatarCPF(String cpf) {
            // Remove caracteres não numéricos do CPF
            String cpfLimpo = cpf.replaceAll("[^0-9]", "");

            // Adiciona pontos e traço ao CPF
            return cpfLimpo.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
        }
    }

    // Método para configurar o ID do livro
    public void setIdLivro(int idLivro) {
        this.idLivro = idLivro;
        IdLivroField.setText(String.valueOf(idLivro));
    }
}
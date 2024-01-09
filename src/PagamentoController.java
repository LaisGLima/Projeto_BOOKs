import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;

public class PagamentoController {

    @FXML
    private MenuItem CartaoMenu;

    @FXML
    private MenuItem DinheiroMenu;

    @FXML
    private MenuButton FormaPagamento;

    @FXML
    private Button PagamentoButton;

    @FXML
    private MenuItem PixMenu;

    @FXML
    private TextField Total;

    @FXML
    private LocalDate dataSaida = null;
    private LocalDate dataEntrega = null;

    @FXML
    private TextField codigoLocacaoField;

    // Variável para armazenar a forma de pagamento escolhida
    private String formaPagamentoEscolhida;

    @FXML
    private void switchToInicial() throws IOException {
        App.setRoot("inicial");
    }

    @FXML
    void initialize() {
        // Adiciona um ouvinte de mudança para o campo de texto do código de locação
        codigoLocacaoField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // Chama o método calcularTotal quando o código de locação é alterado
                calcularTotal(null);
            }
        });
    }

    @FXML
private void calcularTotal(ActionEvent event) {
    String codigoLocacaoTexto = codigoLocacaoField.getText().trim();

    // Verifica se o campo de texto não está vazio
    if (codigoLocacaoTexto.isEmpty()) {
        // Se estiver vazio, limpa o TextField do Total e retorna
        Total.clear();
        return;
    }

    int idLocacao;
    try {
        idLocacao = Integer.parseInt(codigoLocacaoTexto);
    } catch (NumberFormatException e) {
        // Se a conversão falhar, limpa o TextField do Total e retorna
        Total.clear();
        return;
    }

    try (Connection conexao = ConexaoMysql.conectar()) {
        String sql = "SELECT data_saida, data_entrega, situacao FROM locacao WHERE id_locacao = ?";
        try (PreparedStatement statement = conexao.prepareStatement(sql)) {
            statement.setInt(1, idLocacao);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String situacao = resultSet.getString("situacao");

                    // Verifica se a situação é "excedido" antes de prosseguir com o cálculo
                    if ("excedido".equals(situacao)) {
                        dataSaida = resultSet.getDate("data_saida").toLocalDate();
                        dataEntrega = resultSet.getDate("data_entrega").toLocalDate();

                        // Calcular a diferença em dias entre data de saída e data de entrega
                        long diasAtraso = Math.max(0, ChronoUnit.DAYS.between(dataSaida, dataEntrega));

                        // Limite de dias sem multa
                        int limiteDiasSemMulta = 30;

                        // Taxa por dia após o limite
                        double taxaPorDia = 1.0;

                        // Calcular a taxa total
                        double taxaTotal = (diasAtraso > limiteDiasSemMulta) ? (diasAtraso - limiteDiasSemMulta) * taxaPorDia : 0;

                        // Formatar o valor no formato "R$ X,XX"
                        DecimalFormat decimalFormat = new DecimalFormat("R$ #,##0.00");
                        String valorFormatado = decimalFormat.format(taxaTotal);

                        // Exibir o valor total calculado no TextField
                        Total.setStyle("-fx-background-color: E6E6F1;-fx-background-radius: 30; -fx-text-fill: red;");
                        Total.setText(valorFormatado);
                        } else {
                        Total.setStyle("-fx-background-color: E6E6F1;-fx-background-radius: 30; -fx-text-fill: black;");
                        Total.setText("R$0,00");
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Aviso");
                        alert.setHeaderText(null);
                        alert.setContentText("Entrega não excedida");
                        alert.showAndWait(); 
                    }
                }
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    @FXML
    void selecionarFormaPagamento(ActionEvent event) {
        // Obtém o item do menu escolhido
        MenuItem menuItem = (MenuItem) event.getSource();

        // Obtém o texto do item e armazena na variável
        formaPagamentoEscolhida = menuItem.getText();

        // Define o texto da forma de pagamento na TextField
        FormaPagamento.setText(formaPagamentoEscolhida);
    }

    @FXML
    private void confirmarPagamento(ActionEvent event) {
        try (Connection conexao = ConexaoMysql.conectar()) {
            String sql = "INSERT INTO pagamento (id_locacao, forma_pagamento, total_pagar) VALUES (?, ?, ?)";
            try (PreparedStatement statement = conexao.prepareStatement(sql)) {
                int idLocacao;
                String formaPagamento;
    
                // Tenta converter o idLocacao para um inteiro
                try {
                    idLocacao = Integer.parseInt(codigoLocacaoField.getText());
                } catch (NumberFormatException e) {
                    exibirAlerta("Erro", "ID de locação inválido", "Por favor, insira um ID de locação válido.");
                    return;
                }
    
                formaPagamento = formaPagamentoEscolhida;
    
                // Verifica se a forma de pagamento foi escolhida
                if (formaPagamento == null || formaPagamento.isEmpty()) {
                    exibirAlerta("Aviso", "Forma de pagamento não selecionada", "Por favor, selecione uma forma de pagamento.");
                    return;
                }
    
                statement.setInt(1, idLocacao);
                statement.setString(2, formaPagamento);
                statement.setString(3, Total.getText());

                statement.executeUpdate();

                atualizarSituacaoLocacao(conexao, idLocacao);

                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("");
                alert.setHeaderText(null);
                alert.setContentText("Pagamento atualizado.");
                alert.showAndWait();

                codigoLocacaoField.clear();
                Total.clear();
                FormaPagamento.setText("selecione"); 
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void atualizarSituacaoLocacao(Connection conexao, int idLocacao) throws SQLException {
        // Atualiza a tabela locacao para marcar como "entregue"
        String sqlAtualizacao = "UPDATE locacao SET situacao = ? WHERE id_locacao = ?";
        try (PreparedStatement statementAtualizacao = conexao.prepareStatement(sqlAtualizacao)) {
            statementAtualizacao.setString(1, "entregue");
            statementAtualizacao.setInt(2, idLocacao);
            statementAtualizacao.executeUpdate();
        }
    }
    
    private void exibirAlerta(String titulo, String cabecalho, String conteudo) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(cabecalho);
        alerta.setContentText(conteudo);
        alerta.showAndWait();
    }
    

    @FXML
    private boolean verificarExcedido(int idLocacao, double totalPagar) {
        try (Connection conexao = ConexaoMysql.conectar()) {
            String sql = "SELECT situacao FROM locacao WHERE id_locacao = ?";
            try (PreparedStatement statement = conexao.prepareStatement(sql)) {
                statement.setInt(1, idLocacao);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String situacao = resultSet.getString("situacao");
                        // Verifique se a situação é diferente de "excedido" e o total a pagar é 0
                        return !"excedido".equals(situacao) && totalPagar == 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; 
        }
        return false;
    }
}

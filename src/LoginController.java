import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private Button loginButton;

    @FXML
    private Button logoutButton;

    @FXML
    private TextField usuarioField;

    @FXML
    private PasswordField senhaField;

    private String usuarioAnterior;
    private String senhaAnterior;

    @FXML
    private void switchToInicial() throws IOException {
        if (usuarioEstaLogado()) {
            App.setRoot("inicial");
        } else {
            exibirAvisoLogin();
        }
    }

    @FXML
    void cadastrarusuario(ActionEvent event) throws IOException {
        try (Connection conexao = ConexaoMysql.conectar()) {
            if (conexao != null) {
                if (usuarioJaCadastrado(conexao, usuarioField.getText(), senhaField.getText())) {
                    usuarioAnterior = usuarioField.getText();
                    senhaAnterior = senhaField.getText();
                    loginButton.toBack();
                    logoutButton.toFront();
                } else {
                    if (verificarSenhaFixa(conexao)) {
                        String sql = "INSERT INTO funcionarios (id, senha) VALUES (?, ?)";
                        try (PreparedStatement statement = conexao.prepareStatement(sql)) {
                            statement.setString(1, usuarioField.getText());
                            statement.setString(2, senhaField.getText());

                            statement.executeUpdate();
                            usuarioAnterior = usuarioField.getText();
                            senhaAnterior = senhaField.getText();
                            loginButton.toBack();
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("");
                            alert.setHeaderText(null);
                            alert.setContentText("USUÁRIO CADASTRADO COM SUCESSO!");
                            alert.showAndWait();
                            logoutButton.toFront();
                        }
                    } else {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Aviso");
                        alert.setHeaderText(null);
                        alert.setContentText("Senha incorreta. Não foi possível fazer o login.");
                        alert.showAndWait();
                    }
                }

                ConexaoMysql.fechar(conexao);
            } else {
                System.out.println("Falha na conexão com o banco de dados.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean usuarioJaCadastrado(Connection conexao, String usuario, String senha) throws SQLException {
        String sql = "SELECT id, senha FROM funcionarios WHERE id = ? AND senha = ?";
        try (PreparedStatement statement = conexao.prepareStatement(sql)) {
            statement.setString(1, usuario);
            statement.setString(2, senha);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }      
    
    private boolean verificarSenhaFixa(Connection conexao) throws SQLException {
        String senhaFixaBanco = obterSenhaFixaBanco(conexao);
        String senhaDigitada = senhaField.getText();

        return senhaDigitada.equals(senhaFixaBanco);
    }

    private String obterSenhaFixaBanco(Connection conexao) throws SQLException {
        String sql = "SELECT senha FROM funcionarios LIMIT 1";
        try (PreparedStatement statement = conexao.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getString("senha") : "biblioteca1234";
        }
    }

    private boolean usuarioEstaLogado() {
        return usuarioAnterior != null && senhaAnterior != null
                && usuarioAnterior.equals(usuarioField.getText()) && senhaAnterior.equals(senhaField.getText());
    }

    private void exibirAvisoLogin() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText("Faça login para acessar a tela inicial.");

        alert.showAndWait();
    }

    @FXML
    void clearFields(ActionEvent event) {
        usuarioField.clear();
        senhaField.clear();
        usuarioAnterior = null;
        senhaAnterior = null;
        logoutButton.toBack();
        loginButton.toFront();
    }
}

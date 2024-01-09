import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class CadastroController {
    @FXML
    private TextField TítuloLabel;

    @FXML
    private TextField GêneroLabel;

    @FXML
    private TextField AutorLabel;

    @FXML
    private TextField EditoraLabel;

    @FXML
    private Button btnimg;

    @FXML
    private Button CadastrarButton1;

    @FXML
    private Button LimparButton;

    @FXML
    private String imagePath;

    @FXML
    void uploadImagem(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.jpg", "*.png", "*.jpeg", "*.pgn"));

        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            btnimg.setStyle("-fx-background-color: #E6E6F1; -fx-background-radius: 30; -fx-border-color: #395679; -fx-border-radius: 30;");
            imagePath = selectedFile.getAbsolutePath();
        }
    }

    @FXML
    void cadastrarLivro(ActionEvent event) throws IOException {
        try (Connection conexao = ConexaoMysql.conectar()) {
            if (conexao != null) {
                // Preparar a instrução SQL
                String sql = "INSERT INTO livros (titulo, genero, autor, editora, imagem) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement statement = conexao.prepareStatement(sql)) {
                    // Preencher os parâmetros
                    statement.setString(1, TítuloLabel.getText());
                    statement.setString(2, GêneroLabel.getText());
                    statement.setString(3, AutorLabel.getText());
                    statement.setString(4, EditoraLabel.getText());
                    if (imagePath != null && !imagePath.isEmpty()) {
                        statement.setString(5, imagePath);
                    } else {
                        statement.setNull(5, Types.VARCHAR);
                    }
                    

                    // Executar a inserção
                    statement.executeUpdate();
                    switchToInicial();
                }
                ConexaoMysql.fechar(conexao);

            } else {
                System.out.println("Falha na conexão com o banco de dados.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void limparCampos(ActionEvent event) {
        // Limpa os campos da interface de usuário (View)
        TítuloLabel.clear();
        GêneroLabel.clear();
        AutorLabel.clear();
        EditoraLabel.clear();
        btnimg.setStyle("-fx-background-color: #E6E6F1; -fx-background-radius: 30;");
        imagePath = null;
    }
    
    @FXML
    private void switchToInicial() throws IOException {
        App.setRoot("inicial");
    }

}
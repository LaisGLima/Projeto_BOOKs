import java.io.File;
import java.io.IOException;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import model.Locação;
import javafx.scene.layout.HBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.scene.Node;



public class InicialController {

    @FXML
    private void switchToLogin() throws IOException {
        System.out.println("Switching to Login");
        App.setRoot("login");
    }

    @FXML
    private void switchToCadastro() throws IOException {
        System.out.println("Switching to Cadastro");
        App.setRoot("cadastro de livro");
    }
    
    @FXML
    private void switchToLocação() throws IOException {
        App.setRoot("locação");
    }
    
    @FXML
    private void switchToPagamento() throws IOException {
        App.setRoot("pagamento de multas");
    }

    @FXML
    private MenuItem Geralmenu;

    @FXML
    private MenuItem Autormenu;

    @FXML
    private MenuItem Editoramenu;

    @FXML
    private MenuItem Gêneromenu;

    @FXML
    private MenuItem Titulomenu;

    @FXML
    private HBox cardContainer;

    @FXML
    private VBox card;

    @FXML
    private ImageView cardFalha;
    
    @FXML
    private Label TítuloLabel;
    
    @FXML
    private Label AutorLabel;
            
    @FXML
    private Label GêneroLabel;

    @FXML
    private Label EditoraLabel;
    
    @FXML
    private ImageView img;
    
    @FXML
    private ScrollPane resultScrollPane;

    @FXML
    private TextField buscaField;


    @FXML
    private String filtroSelecionado = null;

    @FXML
    private void initialize() {
        // Adiciona um listener ao campo de busca para monitorar mudanças
        buscaField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                // Limpa a ScrollPane antes de adicionar novos resultados
                cardContainer.getChildren().clear();
                // Realiza a busca no banco de dados com base no novo texto no campo de busca e no filtro selecionado
                buscarLivrosNoBanco(newValue);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Configura os listeners para os menus de filtro
        configureFilterMenuListeners();

        // Trate a exceção IOException ao chamar o método
        try {
            carregarLivrosDoBanco();
        } catch (IOException e) {
            e.printStackTrace(); // Ou maneje a exceção de acordo com a sua lógica
        }

    }

    @FXML
    private void configureFilterMenuListeners() {
        Geralmenu.setOnAction(event -> {
            filtroSelecionado = null; // Define o filtro geral
            cardContainer.getChildren().clear(); // Limpa a ScrollPane antes de adicionar novos resultados
            try {
                buscarLivrosNoBanco(buscaField.getText()); // Realiza a busca no banco com base no texto atual
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    
        Autormenu.setOnAction(event -> {
            filtroSelecionado = "autor";
            cardContainer.getChildren().clear();
            try {
                buscarLivrosNoBanco(buscaField.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    
        Editoramenu.setOnAction(event -> {
            filtroSelecionado = "editora";
            cardContainer.getChildren().clear();
            try {
                buscarLivrosNoBanco(buscaField.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    
        Gêneromenu.setOnAction(event -> {
            filtroSelecionado = "genero";
            cardContainer.getChildren().clear();
            try {
                buscarLivrosNoBanco(buscaField.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    
        Titulomenu.setOnAction(event -> {
            filtroSelecionado = "titulo";
            cardContainer.getChildren().clear();
            try {
                buscarLivrosNoBanco(buscaField.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }    

    private void buscarLivrosNoBanco(String termoBusca) throws IOException {
        try (Connection conexao = ConexaoMysql.conectar()) {
            if (conexao != null) {
                String sql;
                if (filtroSelecionado != null) {
                    sql = "SELECT * FROM livros WHERE " + filtroSelecionado + " LIKE ?";
                } else {
                    sql = "SELECT * FROM livros WHERE titulo LIKE ? OR autor LIKE ? OR genero LIKE ? OR editora LIKE ?";
                }

                try (PreparedStatement statement = conexao.prepareStatement(sql)) {
                    // Configura os parâmetros do PreparedStatement
                    if (filtroSelecionado != null) {
                        statement.setString(1, "%" + termoBusca + "%");
                    } else {
                        for (int i = 1; i <= 4; i++) {
                            statement.setString(i, "%" + termoBusca + "%");
                        }
                    }

                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            criarCartaoLivro(resultSet);
                        }
                    }
                }
            } else {
                System.out.println("Filtro não selecionado ou falha na conexão com o banco de dados.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void carregarLivrosDoBanco() throws IOException {
        try (Connection conexao = ConexaoMysql.conectar()) {
            if (conexao != null) {
                String sql = "SELECT * FROM livros";
                try (PreparedStatement statement = conexao.prepareStatement(sql)) {
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            criarCartaoLivro(resultSet);
                        }
                    }
                }
            } else {
                System.out.println("Falha na conexão com o banco de dados.");
            }
        } catch (SQLException e) {
            throw new IOException("Erro ao carregar livros do banco de dados", e);
        }
    }

    private void criarCartaoLivro(ResultSet resultSet) throws IOException {
        try {
            // Criar VBox para a imagem
            VBox imagemVBox = new VBox();
            ImageView imagemImageView = new ImageView();
            String imagemURL = resultSet.getString("imagem");
            imagemVBox.setAlignment(Pos.CENTER);
            if (imagemURL != null && !imagemURL.isEmpty()) {
                File file = new File(imagemURL);
                String localUrl = file.toURI().toURL().toString();
                imagemImageView.setFitWidth(188);
                imagemImageView.setFitHeight(250);
                imagemImageView.setImage(new Image(localUrl));
                imagemImageView.setPreserveRatio(false);
                imagemVBox.getChildren().add(imagemImageView);
            } else {
                File file = new File("src/imagens/CARD.png");
                String localUrl = file.toURI().toURL().toString();
                ImageView espacoVazio = new ImageView(new Image(localUrl));
                espacoVazio.setFitWidth(188);
                espacoVazio.setFitHeight(250);
                imagemVBox.getChildren().add(espacoVazio);
            }
    
            // Criar VBox para outras informações
            VBox infoVBox = new VBox();
    
            Label tituloLabel = new Label(resultSet.getString("titulo"));
            tituloLabel.setAlignment(Pos.CENTER);
            tituloLabel.setFont(Font.font("Cambria", 24.0));

            Label autorLabel = new Label(resultSet.getString("autor"));
            autorLabel.setAlignment(Pos.CENTER);
            autorLabel.setFont(Font.font("Cambria", 20.0));
    
            Label generoLabel = new Label(resultSet.getString("genero"));
            generoLabel.setAlignment(Pos.CENTER);
            generoLabel.setFont(Font.font("Cambria", 20.0));
    
            Label editoraLabel = new Label(resultSet.getString("editora"));
            editoraLabel.setAlignment(Pos.CENTER);
            editoraLabel.setFont(Font.font("Cambria", 20.0));
            infoVBox.setSpacing(10);

            // Criar o botão "Alugar" programaticamente
            Button alugarButton = new Button("ALUGAR");
            alugarButton.setPrefWidth(120);
            alugarButton.setStyle("-fx-background-color: white; -fx-background-radius: 30; -fx-border-width: 2; -fx-border-color: #ff7f48; -fx-border-radius: 30; -fx-font-family: 'Arial Black'; -fx-text-fill: #ff7f48;");

            infoVBox.getChildren().addAll(tituloLabel, autorLabel, generoLabel, editoraLabel, alugarButton);
            infoVBox.setAlignment(Pos.CENTER);
            // Criar HBox que contém as VBoxes da imagem e das informações
            VBox card = new VBox(imagemVBox, infoVBox);
            cardContainer.getChildren().add(card);
            cardContainer.setSpacing(25);
            card.setStyle("-fx-border-color: #c7c7c7;");

            // Configurar o evento de ação do botão
            alugarButton.setOnAction(event -> {
                try {
                    alugarLivro(event);
                } catch (IOException e) {
                    e.printStackTrace(); // Ou maneje a exceção de acordo com a sua lógica
                }
            });

            // Definir o ID do livro como dados do usuário
            alugarButton.setUserData(resultSet.getInt("id"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void alugarLivro(ActionEvent event) throws IOException {
        // Obtém o botão clicado
        Button button = (Button) event.getSource();

        // Obtém o ID do livro associado ao botão
        Integer idLivro = (Integer) button.getUserData();

        // Redireciona para a página de locação com o ID do livro
        FXMLLoader loader = new FXMLLoader(getClass().getResource("locação.fxml"));
        Parent locacaoParent = loader.load();
        LocaçãoController locacaoController = loader.getController();

        // Chame o método setIdLivro para configurar o ID do livro no LocaçãoController
        locacaoController.setIdLivro(idLivro);

        // Obtenha a cena atual
        Scene locacaoScene = ((Node) event.getSource()).getScene();

        // Defina a nova raiz da cena como a página de locação
        locacaoScene.setRoot(locacaoParent);
    }

    public void setLocacoesList(ObservableList<Locação> locacoesList) {

    }
} 
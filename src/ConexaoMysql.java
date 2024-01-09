import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoMysql {
    public static Connection conectar() {
        // Configurações do banco de dados
        String url = "jdbc:mysql://localhost:3306/booksdb";
        String usuario = "root";
        String senha = "BOOks40028922";

        // Objeto de conexão
        Connection conexao = null;

        try {
            // Registrar o driver JDBC
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Estabelecer a conexão
            conexao = DriverManager.getConnection(url, usuario, senha);

            if (conexao != null) {
                System.out.println("Conexão bem-sucedida!");
                // Faça as operações no banco de dados aqui, se necessário
            } else {
                System.out.println("Não foi possível conectar ao banco de dados.");
            }

        } catch (ClassNotFoundException e) {
            System.out.println("Driver JDBC não encontrado.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Erro ao conectar ao banco de dados.");
            e.printStackTrace();
        }
        return conexao;
    }

    public static void  fechar(Connection conexao) {
        //Connection conexao = null;
        try {
                if (conexao != null && !conexao.isClosed()) {
                    conexao.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }
}

package view;

import java.sql.PreparedStatement;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;

import java.sql.Statement;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import DataAcessObject.*;

import java.sql.Connection;
import java.sql.DriverManager;

import model.*;
import database.databaseConnect;

public class Busca extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Gerenciador de biblioteca");
        primaryStage.setMaximized(true); // Maximiza a tela ao iniciar
        BorderPane root = new BorderPane();

        // ComboBox para escolher o tipo de busca
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("Aluno", "Livro");
        comboBox.setValue("Aluno");
        comboBox.setPrefWidth(400);

        // Campo de texto para inserir o termo da busca
        TextField txtBuscar = new TextField();
        txtBuscar.setPromptText("Digite o ID ou nome");
        txtBuscar.setMaxWidth(150);

        // Botões
        Button btnBuscar = new Button("Buscar");
        Button btnVoltarInicio = new Button("Voltar ao Início");

        // Área de resultados
        TextArea txtResultados = new TextArea();
        txtResultados.setEditable(false);
        txtResultados.setMaxWidth(900);
        txtResultados.setMaxHeight(110);

        // VBox para centralizar os componentes de busca
        VBox vboxBusca = new VBox(20);
        vboxBusca.setAlignment(Pos.CENTER);  // Centraliza os componentes dentro da VBox
        vboxBusca.setPadding(new Insets(40));  // Adiciona padding à VBox
        vboxBusca.getChildren().addAll(comboBox, txtBuscar, btnBuscar, btnVoltarInicio);

        // VBox para centralizar os resultados
        VBox vboxResultados = new VBox(20);
        vboxResultados.setAlignment(Pos.CENTER);
        vboxResultados.setPadding(new Insets(20));
        vboxResultados.getChildren().add(txtResultados);

        // Alinha a VBox de busca no topo e os resultados na parte inferior
        BorderPane.setAlignment(vboxBusca, Pos.CENTER);
        BorderPane.setAlignment(vboxResultados, Pos.CENTER);

        // Remover a imagem de fundo e aplicar o fundo branco
        BackgroundFill backgroundFill = new BackgroundFill(javafx.scene.paint.Color.WHITE, null, null);
        root.setBackground(new Background(backgroundFill));

        root.setTop(vboxBusca);  // Coloca a VBox de busca no topo do BorderPane
        root.setCenter(vboxResultados);  // Coloca a VBox de resultados no centro do BorderPane

        // Ação do botão de busca
        btnBuscar.setOnAction(e -> {
            String tipoBusca = comboBox.getValue();
            String termoBusca = txtBuscar.getText();

            String resultado = realizarBusca(tipoBusca, termoBusca);
            txtResultados.setText(resultado);
        });

        // Ação do botão de voltar ao início
        btnVoltarInicio.setOnAction(e -> primaryStage.close());

        // Configura a cena
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private String realizarBusca(String tipoBusca, String termoBusca) {
        switch (tipoBusca) {
            case "Aluno":
                return buscarAluno(termoBusca);
            case "Livro":
                return buscarLivro(termoBusca);
            default:
                return "Tipo de busca não suportado.";
        }
    }

    private String buscarAluno(String termoBusca) {
        try (Connection connection = databaseConnect.openConnection()) {
            String sql = "SELECT ra, nome, debito FROM alunos WHERE ra = ? OR nome LIKE ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, termoBusca);
                stmt.setString(2, "%" + termoBusca + "%");
                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    String ra = resultSet.getString("ra");
                    String nome = resultSet.getString("nome");
                    boolean debito = resultSet.getBoolean("debito");
                    return exibirDetalhesAluno(ra, nome, debito);
                } else {
                    return "Aluno não encontrado.";
                }
            }
        } catch (SQLException ex) {
            return "Erro ao acessar o banco de dados.";
        }
    }

    private String exibirDetalhesAluno(String ra, String nome, boolean debito) {
        StringBuilder detalhes = new StringBuilder();
        detalhes.append("Detalhes do Aluno:\n");
        detalhes.append("Nome: ").append(nome).append("\n");
        detalhes.append("RA: ").append(ra).append("\n");
        detalhes.append("Débito: ").append(debito ? "Sim" : "Não").append("\n");

        List<String> emprestimosPendentes = obterEmprestimosPendentes(ra);
        if (emprestimosPendentes.isEmpty()) {
            detalhes.append("Empréstimos Pendentes: Não há empréstimos pendentes.");
        } else {
            detalhes.append("Empréstimos Pendentes:\n");
            for (String emprestimo : emprestimosPendentes) {
                detalhes.append(emprestimo).append("\n");
            }
        }
        return detalhes.toString();
    }

    private List<String> obterEmprestimosPendentes(String ra) {
        List<String> emprestimosPendentes = new ArrayList<>();
        try (Connection connection = databaseConnect.openConnection()) {
            String sql = "SELECT data_emprestimo FROM emprestimo WHERE ra_aluno = ? AND data_devolucao IS NULL";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, ra);
                ResultSet resultSet = stmt.executeQuery();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                while (resultSet.next()) {
                    Date dataEmprestimo = resultSet.getDate("data_emprestimo");
                    String dataFormatada = dateFormat.format(dataEmprestimo);
                    emprestimosPendentes.add("Data do Empréstimo: " + dataFormatada);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return emprestimosPendentes;
    }

    private String buscarLivro(String termoBusca) {
        StringBuilder resultado = new StringBuilder();
        try (Connection connection = databaseConnect.openConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT id, titulo, disponivel, prazo_emprestimo FROM livros WHERE titulo LIKE ? OR id = ?")) {
            statement.setString(1, "%" + termoBusca + "%");
            
            int id = 0;
            try {
                id = Integer.parseInt(termoBusca);
            } catch (NumberFormatException ex) {
                
            }
            statement.setInt(2, id);

            ResultSet resultSet = statement.executeQuery();
            boolean livroEncontrado = false;
            while (resultSet.next()) {
                int idLivro = resultSet.getInt("id");
                String titulo = resultSet.getString("titulo");
                boolean disponibilidade = resultSet.getBoolean("disponivel");
                Date prazoEmprestimo = resultSet.getDate("prazo_emprestimo");

                resultado.append("ID: ").append(idLivro).append("\n")
                        .append("Título: ").append(titulo).append("\n")
                        .append("Disponibilidade: ").append(disponibilidade ? "Sim" : "Não");
                if (!disponibilidade) {
                    resultado.append("\n").append("Prazo de Empréstimo: ");
                    if (prazoEmprestimo != null) {
                        resultado.append(prazoEmprestimo.toString());
                    } else {
                        resultado.append("Indisponível");
                    }
                }
                resultado.append("\n\n");
                livroEncontrado = true;
            }
            if (!livroEncontrado) {
                resultado.append("Livro não encontrado.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

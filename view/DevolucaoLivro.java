package view;

import database.databaseConnect;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import DataAcessObject.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.*;

public class DevolucaoLivro extends Application {

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setMaximized(true);
    GridPane gridPane = new GridPane();
    gridPane.setPadding(new Insets(10));
    gridPane.setHgap(10);
    gridPane.setVgap(10);
    gridPane.setAlignment(Pos.CENTER);  // Alinha os campos dentro do GridPane ao centro

    // Label e TextField para RA do Aluno
    Label lblRA = new Label("RA do Aluno:");
    TextField txtRA = new TextField();
    txtRA.setAlignment(Pos.CENTER);  // Alinha o texto dentro do TextField
    gridPane.add(lblRA, 0, 0);
    gridPane.add(txtRA, 1, 0);

    // Label e TextField para Código do Livro
    Label lblCodigoLivro = new Label("Código do Livro:");
    TextField txtCodigoLivro = new TextField();
    txtCodigoLivro.setAlignment(Pos.CENTER);  // Alinha o texto dentro do TextField
    gridPane.add(lblCodigoLivro, 0, 1);
    gridPane.add(txtCodigoLivro, 1, 1);

    // Botões
    Button btnDevolver = new Button("Devolver Livro");
    Button btnVoltarInicio = new Button("Voltar ao Início");

    // Ações dos botões
    btnDevolver.setOnAction(e -> devolverLivro(txtRA.getText(), Integer.parseInt(txtCodigoLivro.getText())));
    btnVoltarInicio.setOnAction(e -> primaryStage.close());

    // VBox que centraliza os componentes
    VBox vbox = new VBox(10);
    vbox.setAlignment(Pos.CENTER);  // Centraliza todos os itens na VBox
    vbox.setPadding(new Insets(20));  // Adiciona padding à VBox
    vbox.getChildren().addAll(gridPane, btnDevolver, btnVoltarInicio);

    // Ícone do aplicativo
    Image icon = new Image("view\\img\\pngtree-vector-book-icon-png-image_995152.jpg");
    primaryStage.getIcons().add(icon);

    // Remover a imagem de fundo e aplicar o fundo branco
    BackgroundFill backgroundFill = new BackgroundFill(Color.WHITE, null, null);
    vbox.setBackground(new Background(backgroundFill));

    // Configuração da cena
    Scene scene = new Scene(vbox, 300, 200);
    primaryStage.setTitle("Devolução de Livro");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private void devolverLivro(String raAluno, int codigoLivro) {
    try (Connection connection = databaseConnect.openConnection()) {
        String sqlVerificarEmprestimo =
            "SELECT * FROM emprestimo WHERE ra_aluno = ? AND codigo_livro = ? AND data_devolucao IS NULL";

        try (
            PreparedStatement stmtVerificarEmprestimo = connection.prepareStatement(
                sqlVerificarEmprestimo
            )
        ) {
            stmtVerificarEmprestimo.setString(1, raAluno);
            stmtVerificarEmprestimo.setInt(2, codigoLivro);
            ResultSet resultSet = stmtVerificarEmprestimo.executeQuery();

            if (resultSet.next()) {
                Date dataPrevistaDevolucao = resultSet.getDate("data_prevista_devolucao");
                Date dataAtual = new Date(System.currentTimeMillis());

                if (dataAtual.after(dataPrevistaDevolucao)) {
                    DAOaluno.criarDebito(raAluno);
                }

                String sqlDevolverLivro =
                    "UPDATE emprestimo SET data_devolucao = CURRENT_TIMESTAMP WHERE ra_aluno = ? AND codigo_livro = ?";
                DAOlivro.marcarLivroComoDisponivel(codigoLivro);

                try (
                    PreparedStatement stmtDevolverLivro = connection.prepareStatement(
                        sqlDevolverLivro
                    )
                ) {
                    stmtDevolverLivro.setString(1, raAluno);
                    stmtDevolverLivro.setInt(2, codigoLivro);
                    int rowsAffected = stmtDevolverLivro.executeUpdate();
                    if (rowsAffected > 0) {
                        exibirAlerta(
                            "Devolução Bem-Sucedida",
                            "O livro foi devolvido com sucesso."
                        );
                    } else {
                        exibirAlerta(
                            "Erro na Devolução",
                            "Não foi possível devolver o livro. Tente novamente mais tarde."
                        );
                    }
                }
            } else {
                exibirAlerta(
                    "Livro não Emprestado",
                    "O livro não está emprestado para este aluno."
                );
            }
        }
    } catch (SQLException ex) {
        // Exibindo mais detalhes sobre a exceção
        String mensagemErro = "Erro Desconhecido: " + ex.getMessage();
        if (ex.getSQLState() != null) {
            mensagemErro += " | SQLState: " + ex.getSQLState();
        }
        if (ex.getErrorCode() != 0) {
            mensagemErro += " | Código de Erro: " + ex.getErrorCode();
        }

        exibirAlerta(
            "Erro na Devolução",
            mensagemErro
        );

        // Imprimir a stack trace completa para o log
        ex.printStackTrace();
    }
}


  private void exibirAlerta(String titulo, String mensagem) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(titulo);
    alert.setHeaderText(null);
    alert.setContentText(mensagem);
    alert.showAndWait();
  }

  public static void main(String[] args) {
    launch(args);
  }
}

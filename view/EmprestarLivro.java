package view;

import database.databaseConnect;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import DataAcessObject.*;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

public class EmprestarLivro extends Application {

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setMaximized(true);
    
    GridPane gridPane = new GridPane();
    gridPane.setAlignment(Pos.CENTER);  // Centralizando os elementos
    gridPane.setHgap(10);
    gridPane.setVgap(10);

    Label lblRA = new Label("RA do Aluno:");
    TextField txtRA = new TextField();
    gridPane.add(lblRA, 0, 0);
    gridPane.add(txtRA, 1, 0);

    Label lblCodigos = new Label("Código do Livro:");
    TextField txtCodigos = new TextField();
    gridPane.add(lblCodigos, 0, 1);
    gridPane.add(txtCodigos, 1, 1);

    Label lblPrazo = new Label("Data de Devolução:");
    DatePicker datePicker = new DatePicker();
    gridPane.add(lblPrazo, 0, 2);
    gridPane.add(datePicker, 1, 2);

    Button btnEmprestar = new Button("Emprestar");
    Button btnVoltarInicio = new Button("Voltar ao Início");

    btnEmprestar.setOnAction(e -> {
      LocalDate selectedDate = datePicker.getValue();
      Date date = java.sql.Date.valueOf(selectedDate);
      emprestarLivros(txtRA.getText(), txtCodigos.getText(), date);
    });

    btnVoltarInicio.setOnAction(e -> primaryStage.close());

    VBox vbox = new VBox(20);
    vbox.setAlignment(Pos.CENTER);  // Centralizando os botões e os campos
    vbox.getChildren().addAll(gridPane, btnEmprestar, btnVoltarInicio);
    
    // Remover a imagem de fundo e definir o fundo branco
    BackgroundFill backgroundFill = new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, javafx.geometry.Insets.EMPTY);
    vbox.setBackground(new Background(backgroundFill));

    Scene scene = new Scene(vbox);
    primaryStage.setTitle("Emprestar Livro");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private void emprestarLivros(String ra, String codigos, Date date) {
    boolean alunoCadastrado = DAOaluno.verificarAlunoCadastrado(ra);
    boolean debitosAluno = DAOaluno.verificarDebitosAluno(ra);
    boolean emprestimoEfetuado = false;

    if (alunoCadastrado && !debitosAluno) {
      String[] codigosLivros = codigos.split(",");
      for (String codigo : codigosLivros) {
        boolean livroEmprestado = emprestarLivro(
          Integer.parseInt(codigo),
          ra,
          date
        );
        if (!livroEmprestado) {
          exibirMensagem(
            "Livro com código " + codigo + " não pode ser emprestado."
          );
          return;
        }
      }
      emprestimoEfetuado = true;
    }

    if (!alunoCadastrado) {
      exibirMensagem("Aluno não cadastrado!");
    }

    if (debitosAluno) {
      exibirMensagem("Aluno com débito!");
    }

    if (emprestimoEfetuado) {
      exibirMensagem("Empréstimo efetuado com sucesso!");
    } else {
      exibirMensagem(
        "Erro ao efetuar o empréstimo. Verifique os dados informados."
      );
    }
  }

  public static boolean emprestarLivro(int codigoLivro, String ra, Date date) {
    String sql = "SELECT disponivel FROM livros WHERE id = ?";
    try (
      Connection conn = databaseConnect.openConnection();
      PreparedStatement stmt = conn.prepareStatement(sql)
    ) {
      PreparedStatement verificarDisponibilidadeStatement = conn.prepareStatement(
        sql
      );
      verificarDisponibilidadeStatement.setInt(1, codigoLivro);
      ResultSet disponibilidadeResultSet = verificarDisponibilidadeStatement.executeQuery();
      if (disponibilidadeResultSet.next()) {
        boolean disponivel = disponibilidadeResultSet.getBoolean("disponivel");
        if (!disponivel) {
          return false;
        }
      } else {
        return false;
      }

      String inserirEmprestimoQuery =
        "INSERT INTO emprestimo (codigo_livro, ra_aluno, data_prevista_devolucao) VALUES (?, ?, ?)";
      PreparedStatement inserirEmprestimoStatement = conn.prepareStatement(
        inserirEmprestimoQuery
      );
      inserirEmprestimoStatement.setInt(1, codigoLivro);
      inserirEmprestimoStatement.setString(2, ra);
      inserirEmprestimoStatement.setDate(3, date);
      inserirEmprestimoStatement.executeUpdate();

      String atualizarStatusQuery =
        "UPDATE livros SET disponivel = false, prazo_emprestimo = ? WHERE id = ?";
      PreparedStatement atualizarStatusStatement = conn.prepareStatement(
        atualizarStatusQuery
      );
      atualizarStatusStatement.setDate(1, date);
      atualizarStatusStatement.setInt(2, codigoLivro);
      atualizarStatusStatement.executeUpdate();

      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  private void exibirMensagem(String mensagem) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Informação");
    alert.setHeaderText(null);
    alert.setContentText(mensagem);
    alert.showAndWait();
  }

  public static void main(String[] args) {
    launch(args);
  }
}

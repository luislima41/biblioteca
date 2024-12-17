package view;

import database.databaseConnect;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

import DataAcessObject.*;
import javafx.application.Application;
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

public class CadastrarLivro extends Application {

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setMaximized(true);
    primaryStage.setTitle("Cadastro de livro");

    // Criação do GridPane para centralizar as informações
    GridPane gridPane = new GridPane();
    gridPane.setAlignment(Pos.CENTER); // Centraliza o conteúdo
    gridPane.setHgap(20);
    gridPane.setVgap(10);

    Label lblTitulo = new Label("Título:");
    TextField txtTitulo = new TextField();
    gridPane.add(lblTitulo, 0, 0);
    gridPane.add(txtTitulo, 1, 0);

    Label lblId = new Label("ID:");
    TextField txtId = new TextField();
    gridPane.add(lblId, 0, 1);
    gridPane.add(txtId, 1, 1);

    // Criando os botões
    Button btnCadastrar = new Button("Cadastrar");
    Button btnVoltarInicio = new Button("Voltar ao Início");

    btnCadastrar.setOnAction(e -> {
      String titulo = txtTitulo.getText();
      int id = Integer.parseInt(txtId.getText());

      if (DAOlivro.verificarIdExistente(id)) {
        exibirAlerta(Alert.AlertType.ERROR, "Erro", "ID já existente.");
      } else {
        boolean cadastrou = DAOlivro.cadastrarLivro(titulo, id);
        if (cadastrou) {
          exibirAlerta(
            Alert.AlertType.INFORMATION,
            "Sucesso",
            "Livro cadastrado com sucesso!"
          );
        } else {
          exibirAlerta(
            Alert.AlertType.ERROR,
            "Erro",
            "Ocorreu um erro ao cadastrar o livro."
          );
        }
      }
    });

    btnVoltarInicio.setOnAction(e -> primaryStage.close());

    // Centralizando o VBox que contém o GridPane e os botões
    VBox vbox = new VBox(20);
    vbox.setAlignment(Pos.CENTER); // Centraliza o conteúdo do VBox
    vbox.getChildren().addAll(gridPane, btnCadastrar, btnVoltarInicio);

    // Definindo o ícone da janela
    Image icon = new Image("view\\img\\pngtree-vector-book-icon-png-image_995152.jpg");
    primaryStage.getIcons().add(icon);

    // Remover a imagem de fundo e aplicar o fundo branco
    BackgroundFill backgroundFill = new BackgroundFill(Color.WHITE, null, null);
    vbox.setBackground(new Background(backgroundFill));

    Scene scene = new Scene(vbox);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }

  private static void exibirAlerta(
    Alert.AlertType tipo,
    String titulo,
    String mensagem
  ) {
    Alert alerta = new Alert(tipo);
    alerta.setTitle(titulo);
    alerta.setHeaderText(null);
    alerta.setContentText(mensagem);
    alerta.showAndWait();
  }
}

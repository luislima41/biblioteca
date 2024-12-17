package view;

import java.io.FileNotFoundException;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

public class Menu extends Application {

  @Override
  public void start(Stage primaryStage) throws FileNotFoundException {

    // Configura o título e o tamanho inicial da janela
    primaryStage.setTitle("Gerenciador de biblioteca");
    primaryStage.setMaximized(true);

    // Criação dos botões de navegação
    Button btnCadastrarLivro = new Button("Cadastrar Livro");
    Button btnCadastrarAluno = new Button("Cadastrar Aluno");
    Button btnEmprestimo = new Button("Emprestar Livro");
    Button btnDevolver = new Button("Devolver Livro");
    Button btnBuscar = new Button("Buscar");
    Button btnSair = new Button("Sair");

    // Ações dos botões (abrir novas janelas)
    btnCadastrarLivro.setOnAction(e -> {
      CadastrarLivro cadastroLivro = new CadastrarLivro();
      cadastroLivro.start(new Stage());
    });

    btnCadastrarAluno.setOnAction(e -> {
      CadastrarAluno cadastroAluno = new CadastrarAluno();
      cadastroAluno.start(new Stage());
    });

    btnEmprestimo.setOnAction(e -> {
      EmprestarLivro EmprestimoLivro = new EmprestarLivro();
      EmprestimoLivro.start(new Stage());
    });

    btnDevolver.setOnAction(e -> {
      DevolucaoLivro DevolverLivro = new DevolucaoLivro();
      DevolverLivro.start(new Stage());
    });

    btnBuscar.setOnAction(e -> {
      Busca Busca = new Busca();
      Busca.start(new Stage());
    });

    btnSair.setOnAction(e -> primaryStage.close());

    // Criando a VBox e alinhando os botões
    VBox vbox = new VBox(30);
    vbox.setAlignment(Pos.CENTER); // Centralizando os elementos
    vbox.getChildren().addAll(
      new Label("Selecione uma opção:"),
      btnCadastrarLivro,
      btnCadastrarAluno,
      btnEmprestimo,
      btnDevolver,
      btnBuscar,
      btnSair
    );

    // Definindo o fundo branco
    BackgroundFill backgroundFill = new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, javafx.geometry.Insets.EMPTY);
    vbox.setBackground(new Background(backgroundFill));

    // Definindo o tamanho da cena
    Scene scene = new Scene(vbox, 300, 300);

    // Definindo a cena e exibindo a janela
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}

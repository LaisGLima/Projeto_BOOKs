package model;

import java.time.LocalDate;
import javafx.beans.property.*;

public class Locação {
    private IntegerProperty codigoLocacao;
    private IntegerProperty idLivro;
    private StringProperty cpf;
    private ObjectProperty<LocalDate> dataSaida;
    private ObjectProperty<LocalDate> dataEntrega;
    private StringProperty status;

    // Construtor padrão
    public Locação() {
        this.codigoLocacao = new SimpleIntegerProperty(0);
        this.idLivro = new SimpleIntegerProperty(0);
        this.cpf = new SimpleStringProperty("");
        this.dataSaida = new SimpleObjectProperty<>(null);
        this.dataEntrega = new SimpleObjectProperty<>(null);
        this.status = new SimpleStringProperty("");
    }

    public Locação(int codigoLocacao, int idLivro, String cpf, LocalDate dataSaida, LocalDate dataEntrega, String status) {
        this.codigoLocacao = new SimpleIntegerProperty(codigoLocacao);
        this.idLivro = new SimpleIntegerProperty(idLivro);
        this.cpf = new SimpleStringProperty(cpf);
        this.dataSaida = new SimpleObjectProperty<>(dataSaida);
        this.dataEntrega = new SimpleObjectProperty<>(dataEntrega);
        this.status = new SimpleStringProperty(status);
    }

    // Getters e Setters com propriedades

    public IntegerProperty codigoLocacaoProperty() {
        return codigoLocacao;
    }

    public int getCodigoLocacao() {
        return codigoLocacao.get();
    }

    public void setCodigoLocacao(int codigoLocacao) {
        this.codigoLocacao.set(codigoLocacao);
    }

    public IntegerProperty IdLivroProperty() {
        return idLivro;
    }

    public int getIdLivro() {
        return idLivro.get();
    }

    public void setIdLivro(int idLivro) {
        this.idLivro.set(idLivro);
    }

    public StringProperty cpfProperty() {
        return cpf;
    }

    public String getcpf() {
        return cpf.get();
    }

    public void setcpf(String cpf) {
        this.cpf.set(cpf);
    }

    public ObjectProperty<LocalDate> dataSaidaProperty() {
        return dataSaida;
    }

    public LocalDate getDataSaida() {
        return dataSaida.get();
    }

    public void setDataSaida(LocalDate dataSaida) {
        this.dataSaida.set(dataSaida);
    }

    public ObjectProperty<LocalDate> dataEntregaProperty() {
        return dataEntrega;
    }

    public LocalDate getDataEntrega() {
        return dataEntrega.get();
    }

    public void setDataEntrega(LocalDate dataEntrega) {
        this.dataEntrega.set(dataEntrega);
    }

    public StringProperty statusProperty() {
        return status;
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }
    
}

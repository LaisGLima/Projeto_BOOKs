package model;

import java.time.LocalDate;
import javafx.beans.property.*;

public class Pagamento {
    private IntegerProperty codigo;
    private DoubleProperty total;
    private ObjectProperty<LocalDate> data;

    private ObjectProperty<Locação> locacaoProperty;

    public Pagamento() {
        this.codigo = new SimpleIntegerProperty(0);
        this.total = new SimpleDoubleProperty(0.0);
        this.data = new SimpleObjectProperty<>(LocalDate.now());
        this.locacaoProperty = new SimpleObjectProperty<>();
    }

    public IntegerProperty codigoProperty() {
        return codigo;
    }

    public int getCodigo() {
        return codigo.get();
    }

    public void setCodigo(int codigo) {
        this.codigo.set(codigo);
    }

    public DoubleProperty totalProperty() {
        return total;
    }

    public double getTotal() {
        return total.get();
    }

    public void setTotal(double total) {
        this.total.set(total);
    }

    public ObjectProperty<LocalDate> dataProperty() {
        return data;
    }

    public LocalDate getData() {
        return data.get();
    }

    public void setData(LocalDate data) {
        this.data.set(data);
    }

    public ObjectProperty<Locação> locacaoProperty() {
        return locacaoProperty;
    }

    public Locação getLocacao() {
        return locacaoProperty.get();
    }

    public void setLocacao(Locação locacao) {
        this.locacaoProperty.set(locacao);
    }

}

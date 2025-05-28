// ServicoMecanico.java
public class ServicoMecanico {
    private final int sid;
    private final double horas;
    private final double custoHora;
    private final double custoSemDesconto;
    private final double desconto;
    private final double total;

    public ServicoMecanico(int sid, double horas, double custoHora, 
                          double custoSemDesconto, double desconto, double total) {
        this.sid = sid;
        this.horas = horas;
        this.custoHora = custoHora;
        this.custoSemDesconto = custoSemDesconto;
        this.desconto = desconto;
        this.total = total;
    }

    // Getters
    public double getTotal() { return total; }
}
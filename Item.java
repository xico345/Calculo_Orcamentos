// Item.java
public class Item {
    private final int id;
    private final String nome;
    private final String marca;
    private final String tipo;
    private final double custo;
    private final double preco;
    private final int quantidade;

    public Item(int id, String nome, String marca, String tipo,
                double custo, double preco, int quantidade) {
        this.id = id;
        this.nome = nome;
        this.marca = marca;
        this.tipo = tipo;
        this.custo = custo;
        this.preco = preco;
        this.quantidade = quantidade;
    }

    @Override
    public String toString() {
        // Mantém o mesmo formato de saída OCaml
        return String.format("%d; %s; %s; %s; %.2f; %.2f; %d",
                             id, nome, marca, tipo, custo, preco, quantidade);
    }
}
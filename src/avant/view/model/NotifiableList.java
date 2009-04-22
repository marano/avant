package avant.view.model;

/**
 *
 * @author marano
 */
public interface NotifiableList {

    public int getLinhaSelecionada();

    public int[] getLinhasSelecionadas();

    public void notificar();

    public void notificar(int indice);

    public void notificarAdicao(int indice);

    public void notificarRemocao(int indice);
}
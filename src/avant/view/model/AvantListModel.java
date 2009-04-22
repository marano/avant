package avant.view.model;

import avant.view.AvantViewUtil;
import java.awt.Component;
import java.util.List;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author marano
 */
public class AvantListModel<T> extends AvantAbstractModel<T> implements ListModel {

    private JList lista;

    public AvantListModel(JList lista) {
        this(lista, null);
    }

    public AvantListModel(JList lista, List<T> objetos) {
        this(lista, objetos, true);
    }

    public AvantListModel(JList lista, List<T> objetos, boolean ordenada) {
        super(objetos, ordenada);
        setLista(lista);
    }

    @Override
    final public Component getComponente() {
        return getLista();
    }

    final public JList getLista() {
        return lista;
    }

    final public void setLista(JList lista) {
        if (this.lista != null) {
            AvantViewUtil.removerListener(controladorAcao, this.lista);
            AvantViewUtil.removerListener(controladorSelecaoLista, this.lista);
        }
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        AvantViewUtil.adicionarListener(controladorAcao, lista);
        AvantViewUtil.adicionarListener(controladorSelecaoLista, lista);
        this.lista = lista;
        lista.setModel(this);
    }

    final public int getLinhaSelecionada() {
        return lista.getSelectedIndex();
    }

    final public int[] getLinhasSelecionadas() {
        return lista.getSelectedIndices();
    }

    public Object getValor(T objeto) {
        return objeto;
    }

    final public int getSize() {
        return size();
    }

    final public Object getElementAt(int indice) {
        return getValor(getObjeto(indice));
    }

    final public void addListDataListener(ListDataListener l) {
        adicionarListener(ListDataListener.class, l);
    }

    final public void removeListDataListener(ListDataListener l) {
        removerListener(ListDataListener.class, l);
    }

    final public void notificar() {
        notificar(ListDataEvent.CONTENTS_CHANGED, 0, Integer.MAX_VALUE);
    }

    final public void notificar(int indice) {
        notificar(ListDataEvent.CONTENTS_CHANGED, indice, indice);
    }

    final public void notificarAdicao(int indice) {
        notificar(ListDataEvent.INTERVAL_ADDED, indice, indice);
    }

    final public void notificarRemocao(int indice) {
        notificar(ListDataEvent.INTERVAL_REMOVED, indice, indice);
    }

    private void notificar(int tipo, int inicio, int termino) {
        Object[] listeners = AvantListModel.this.listeners.getListenerList();
        ListDataEvent e = new ListDataEvent(this, tipo, inicio, termino);

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                if (tipo == ListDataEvent.CONTENTS_CHANGED) {
                    ((ListDataListener) listeners[i + 1]).contentsChanged(e);
                } else if (tipo == ListDataEvent.INTERVAL_ADDED) {
                    ((ListDataListener) listeners[i + 1]).intervalAdded(e);
                } else if (tipo == ListDataEvent.INTERVAL_REMOVED) {
                    ((ListDataListener) listeners[i + 1]).intervalRemoved(e);
                }
            }
        }
    }
}

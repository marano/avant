/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avant.view.model;

import avant.view.AvantViewUtil;
import java.awt.Component;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author marano
 */
public class AvantComboModel<T> extends AvantAbstractModel<T> implements ComboBoxModel {

    private JComboBox combo;
    private T objetoSelecionado;
    private boolean permitirSelecaoNula;

    public AvantComboModel(JComboBox combo) {
        this(combo, null);
    }

    public AvantComboModel(JComboBox combo, List<T> objetos) {
        this(combo, objetos, true);
    }

    public AvantComboModel(JComboBox combo, boolean ordenada) {
        this(combo, null, ordenada, true);
    }

    public AvantComboModel(JComboBox combo, List<T> objetos, boolean ordenada) {
        this(combo, objetos, ordenada, true);
    }

    public AvantComboModel(JComboBox combo, boolean ordenada, boolean permitirSelecaoNula) {
        this(combo, null, ordenada, permitirSelecaoNula);
    }

    public AvantComboModel(JComboBox combo, List<T> objetos, boolean ordenada, boolean permitirSelecaoNula) {
        this(combo, objetos, ordenada, permitirSelecaoNula, true);
    }

    public AvantComboModel(JComboBox combo, boolean ordenada, boolean permitirSelecaoNula, boolean selecionar) {
        this(combo, null, ordenada, permitirSelecaoNula, selecionar);
    }

    public AvantComboModel(JComboBox combo, List<T> objetos, boolean ordenada, boolean permitirSelecaoNula, boolean selecionar) {
        super(objetos, ordenada, !permitirSelecaoNula || selecionar);
        setPermitirSelecaoNula(permitirSelecaoNula);
        setCombo(combo);
        if (selecionar) {
            if (size() > 0) {
                selecionarPrimeiro();
            }
        }
    }

    final public T selecionarPrimeiro() {
        T objeto = getObjeto(0);
        setObjetoSelecionado(objeto);
        return objeto;
    }

    public boolean isPermitirSelecaoNula() {
        return permitirSelecaoNula;
    }

    public void setPermitirSelecaoNula(boolean permitirSelecaoNula) {
        if (this.permitirSelecaoNula != permitirSelecaoNula) {
            this.permitirSelecaoNula = permitirSelecaoNula;
            filtrar();
        }
    }

    @Override
    final protected void aposFiltrar(List<T> objetosFiltrados) {
        if (isPermitirSelecaoNula()) {
            objetosFiltrados.add(0, null);
        }
    }

    @Override
    final public Component getComponente() {
        return getCombo();
    }

    final public JComboBox getCombo() {
        return combo;
    }

    final public void setCombo(JComboBox combo) {
        if (this.combo != null) {
            AvantViewUtil.removerListener(controladorSelecaoItem, this.combo);
        }
        AvantViewUtil.adicionarListener(controladorSelecaoItem, combo);
        this.combo = combo;
        combo.setModel(this);
    }

    @Override
    public T getObjetoSelecionado() {
        return objetoSelecionado;
    }

    final public T getSelectedItem() {
        return getObjetoSelecionado();
    }

    final public void setObjetoSelecionado(T objeto) {
        if ((objetoSelecionado != null && !objetoSelecionado.equals(objeto)) || (objetoSelecionado == null && objeto != null)) {
            objetoSelecionado = objeto;
            notificar();
        }
    }

    final public void setSelectedItem(Object objeto) {
        setObjetoSelecionado((T) objeto);
    }

    final public int getSize() {
        return size();
    }

    public Object getValor(T objeto) {
        return objeto;
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

    @Override
    public int getLinhaSelecionada() {
        return getIndice(getObjetoSelecionado());
    }

    @Override
    public int[] getLinhasSelecionadas() {
        return new int[]{getLinhaSelecionada()};
    }

    final public void notificar() {
        notificar(ListDataEvent.CONTENTS_CHANGED, -1, Integer.MAX_VALUE);
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
        Object[] listeners = AvantComboModel.this.listeners.getListenerList();
        ListDataEvent e = new ListDataEvent(this, tipo, inicio, termino);

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                ListDataListener listener = (ListDataListener) listeners[i + 1];
                if (tipo == ListDataEvent.CONTENTS_CHANGED) {
                    listener.contentsChanged(e);
                } else if (tipo == ListDataEvent.INTERVAL_ADDED) {
                    listener.intervalAdded(e);
                } else if (tipo == ListDataEvent.INTERVAL_REMOVED) {
                    listener.intervalRemoved(e);
                }
            }
        }
    }
}

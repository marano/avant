package avant.view.model;

import avant.view.AvantViewUtil;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.EventObject;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 *
 * @author marano
 */
public abstract class AvantTableModel<T> extends AvantAbstractModel<T> implements TableModel {

    private JTable tabela;
    private Object[] campos;
    private int[] editaveis = new int[0];
    private static final EditorPadrao editorPadrao = new EditorPadrao();

    public AvantTableModel(JTable tabela) {
        this(tabela, null);
    }

    public AvantTableModel(JTable tabela, String[] campos) {
        this(tabela, campos, true);
    }

    public AvantTableModel(JTable tabela, String[] campos, boolean ordenada) {
        this(tabela, null, campos, ordenada);
    }

    public AvantTableModel(JTable tabela, List<T> objetos, String[] campos) {
        this(tabela, objetos, campos, true);
    }

    public AvantTableModel(JTable tabela, List<T> objetos, String[] campos, boolean ordenada) {
        this(tabela, objetos, campos, ordenada, false);
    }

    public AvantTableModel(JTable tabela, List<T> objetos, String[] campos, boolean ordenada, boolean esperarCarregar) {
        super(objetos, ordenada, esperarCarregar);
        setCampos(campos, false);
        setTabela(tabela);
    }

    final public void grabFocus() {
        selecionar(0);
    }

    @Override
    final public Component getComponente() {
        return getTabela();
    }

    final public JTable getTabela() {
        return tabela;
    }

    final public void selecionar(T objeto) {
        JTable tabela = getTabela();
        selecionar(getObjetosFiltrados().indexOf(objeto));
    }

    final public void selecionar(int indice) {
        JTable tabela = getTabela();
        tabela.grabFocus();
        changeSelection(indice);
    }

    final public void changeSelection(int indiceLinha) {
        if (size() == 0) {
            return;
        }
        if (indiceLinha < 0) {
            indiceLinha = 0;
        }
        if (indiceLinha >= size()) {
            indiceLinha = size() - 1;
        }
        tabela.changeSelection(indiceLinha, indiceLinha, false, false);
    }

    final public void setTabela(JTable tabela) {
        if (this.tabela != null) {
            AvantViewUtil.removerListener(controladorAcao, this.tabela);
            AvantViewUtil.removerListener(controladorSelecaoLista, this.tabela.getSelectionModel());
        }
        this.tabela = tabela;
        if (tabela != null) {
            setSelecaoMultipla(false);
            AvantViewUtil.adicionarListener(controladorAcao, tabela);
            AvantViewUtil.adicionarListener(controladorSelecaoLista, tabela.getSelectionModel());

            tirarEnterTabela(tabela);

            tabela.setModel(this);
        }
    }

    final public void removerTabela() {
        setTabela(null);
    }

    private void tirarEnterTabela(JTable table) {
        InputMap im = table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        KeyStroke enter = KeyStroke.getKeyStroke("ENTER");

        im.put(enter, im.get(KeyStroke.getKeyStroke(KeyEvent.VK_GREATER, 0)));

        Action enterAction = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
            }
        };

        table.getActionMap().put(im.get(enter), enterAction);
    }

    final public void setSelecaoMultipla(boolean selecaoMultipla) {
        if (selecaoMultipla) {
            getTabela().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        } else {
            getTabela().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        }
    }

    final public void setLargura(int indice, int min, int max, int pre, TableCellRenderer renderizador, TableCellEditor editor) {
        if (indice < 0 || indice >= getQuantidadeCampos()) {
            return;
        }
        TableColumn coluna = getTabela().getColumn(getColumnName(indice));
        if (min != -1) {
            coluna.setMinWidth(min);
        }
        if (max != -1) {
            coluna.setMaxWidth(max);
        }
        if (pre != -1) {
            coluna.setPreferredWidth(pre);
        }
        if (renderizador != null) {
            coluna.setCellRenderer(renderizador);
        }
        if (editor != null) {
            coluna.setCellEditor(editor);
        }
    }

    final public void setLargura(int indice, int min, int max, int pre) {
        setLargura(indice, min, max, pre, null, null);
    }

    final public void setLargura(int[] indices, int min, int max, int pre, TableCellRenderer renderizador, TableCellEditor editor) {
        for (int i : indices) {
            setLargura(i, min, max, pre, renderizador, editor);
        }
    }

    final public void setLargura(int[] indices, int min, int max, int pre) {
        setLargura(indices, min, max, pre, null, null);
    }

    final public void setEditaveis(int... indices) {
        editaveis = indices;
    }

    final public void setCampos(Object[] campos) {
        setCampos(campos, true);
    }

    final public void setCampos(Object[] campos, boolean notificar) {
        this.campos = campos;
        if (notificar) {
            notificarEstrutura();
        }
    }

    public abstract Object getValor(T objeto, int indiceColuna);

    public void setValor(T objeto, Object valor, int indiceColuna) {
    }

    protected void aposSetar(T objeto, int indiceLinha, int indiceColuna) {
        notificar(indiceLinha, indiceColuna);
    }

    public boolean isCelulaEditavel(T objeto, int indiceLinha, int indiceColuna) {
        return true;
    }

    protected Object[] getCarregarCampos() {
        return new Object[]{""};
    }

    final public void carregarCampos() {
        carregarCampos(true);
    }

    final public void carregarCampos(boolean notificar) {
        campos = getCarregarCampos();
        if (notificar) {
            notificarEstrutura();
        }
    }

    final public Object[] getCampos() {
        if (campos == null) {
            carregarCampos(false);
        }
        return campos;
    }

    final public int getQuantidadeCampos() {
        return getCampos().length;
    }

    final public Object getCampo(int indiceColuna) {
//        if (indiceColuna >= getQuantidadeCampos()) {
//            return getCampos()[indiceColuna];
//        } else {
//            return "";
//        }
        return getCampos()[indiceColuna];
    }

    public boolean isCampoEditavel(int indiceColuna) {
        for (int i : editaveis) {
            if (indiceColuna == i) {
                return true;
            }
        }
        return false;
    }

    final public int getLinhaSelecionada() {
        return tabela.getSelectedRow();
    }

    final public int[] getLinhasSelecionadas() {
        return tabela.getSelectedRows();
    }

    final public int getRowCount() {
        return size();
    }

    final public int getColumnCount() {
        return getQuantidadeCampos();
    }

    final public Class<?> getColumnClass(int indiceColuna) {
        return getValueAt(0, indiceColuna).getClass();
    }

    final public Object getValueAt(int indiceLinha, int indiceColuna) {
        Object valor = getValor(getObjeto(indiceLinha), indiceColuna);
        return valor != null ? valor : "";
    }

    @Override
    final public String getColumnName(int indiceColuna) {
        return getCampo(indiceColuna).toString();
    }

    final private void tentarSetValor(T objeto, Object valor, int indiceLinha, int indiceColuna) {
        try {
            setValor(objeto, valor, indiceColuna);
        } catch (Throwable t) {
            tratarExcecao(t);
        }
    }

    @Override
    final public void setValueAt(Object valor, int indiceLinha, int indiceColuna) {
        T objeto = getObjeto(indiceLinha);
        if (isCapturarErros()) {
            tentarSetValor(objeto, valor, indiceLinha, indiceColuna);
        } else {
            setValor(objeto, valor, indiceColuna);
        }
        aposSetar(objeto, indiceLinha, indiceColuna);
    }

    @Override
    final public boolean isCellEditable(int indiceLinha, int indiceColuna) {
        if (!isCampoEditavel(indiceColuna)) {
            return false;
        } else {
            return isCelulaEditavel(getObjeto(indiceLinha), indiceLinha, indiceColuna);
        }
    }

    final public void addTableModelListener(TableModelListener l) {
        adicionarListener(TableModelListener.class, l);
    }

    final public void removeTableModelListener(TableModelListener l) {
        removerListener(TableModelListener.class, l);
    }

    final public void notificar() {
        notificar(new TableModelEvent(this));
    }

    final public void notificar(int indiceLinha) {
        notificar(new TableModelEvent(this, indiceLinha, indiceLinha,
                TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
    }

    final public void notificarAdicao(int indiceLinha) {
        notificarAdicao(indiceLinha, indiceLinha);
    }

    final public void notificarAdicao(int indiceLinhaInicio, int indiceLinhaTermino) {
        notificar(new TableModelEvent(this, indiceLinhaInicio, indiceLinhaTermino,
                TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
    }

    final public void notificarRemocao(int indiceLinha) {
        notificar(new TableModelEvent(this, indiceLinha, indiceLinha,
                TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
    }

    final public void notificarEstrutura() {
        notificar(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
    }

    final public void notificar(int indiceLinha, int indiceColuna) {
        notificar(new TableModelEvent(this, indiceLinha, indiceLinha, indiceColuna));
    }

    final public void notificar(TableModelEvent e) {
        Object[] listeners = AvantTableModel.this.listeners.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TableModelListener.class) {
                ((TableModelListener) listeners[i + 1]).tableChanged(e);
            }
        }
    }

    private static class EditorPadrao extends JTextField implements TableCellEditor {

        protected EventListenerList listenerList = new EventListenerList();
        protected ChangeEvent changeEvent = new ChangeEvent(this);
        private Object valor;

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            System.out.println("EditorPadrao.getTableCellEditorComponent()");
            this.valor = value;
            this.setText(valor.toString());
            this.setSelectionStart(0);
            this.setSelectionEnd(this.getText().length());
            return this;
        }

        public Object getCellEditorValue() {
            return valor;
        }

        public boolean isCellEditable(EventObject anEvent) {
            return true;
        }

        public boolean shouldSelectCell(EventObject anEvent) {
            return true;
        }

        public void cancelCellEditing() {
            fireEditingCanceled();
        }

        public boolean stopCellEditing() {
            fireEditingStopped();
            return true;
        }

        protected void fireEditingStopped() {
            CellEditorListener listener;
            Object[] listeners = listenerList.getListenerList();
            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i] == CellEditorListener.class) {
                    listener = (CellEditorListener) listeners[i + 1];
                    listener.editingStopped(changeEvent);
                }
            }
        }

        protected void fireEditingCanceled() {
            CellEditorListener listener;
            Object[] listeners = listenerList.getListenerList();
            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i] == CellEditorListener.class) {
                    listener = (CellEditorListener) listeners[i + 1];
                    listener.editingCanceled(changeEvent);
                }
            }
        }

        public void addCellEditorListener(CellEditorListener listener) {
            listenerList.add(CellEditorListener.class, listener);
        }

        public void removeCellEditorListener(CellEditorListener listener) {
            listenerList.remove(CellEditorListener.class, listener);
        }
    }
}

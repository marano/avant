package avant.view.model;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JTree;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author marano
 */
public abstract class AvantTreeModel implements TreeModel {

    private static final int EXPLORER = 1;
    private static final int SEMPRE_EXPANDIDO = 2;
    private static final int SEMPRE_COMPRIMIDO = 3;
    private int modo = SEMPRE_EXPANDIDO;
    protected EventListenerList listenerList = new EventListenerList();
    private NoArvore raiz = new NoArvore("Raíz");
    private List objetosRaiz;
    private JTree arvore;
    private boolean exibirRaiz = false;

    public AvantTreeModel(JTree arvore) {
        this(arvore, null);
    }

    public AvantTreeModel(JTree arvore, List objetos) {
        setObjetos(objetos);
        setArvore(arvore);
    }

    private NoArvore localizarNo(Object objeto) {
        return localizarNo(objeto, raiz);
    }

    private NoArvore localizarNo(Object objeto, NoArvore no) {
        if (no.getObjeto() == objeto) {
            return no;
        } else {
            for (int i = 0; i < no.nosFilhos.size(); i++) {
                NoArvore noLocalizado = localizarNo(objeto, no.nosFilhos.get(i));
                if (noLocalizado != null) {
                    return noLocalizado;
                }
            }
        }
        return null;
    }

    public boolean isExibirRaiz() {
        return exibirRaiz;
    }

    public void setExibirRaiz(boolean exibirRaiz) {
        if (this.exibirRaiz != exibirRaiz) {
            this.exibirRaiz = exibirRaiz;
            arvore.setRootVisible(exibirRaiz);
        }
    }

    final public void setArvore(JTree arvore) {
        this.arvore = arvore;
        arvore.setRootVisible(exibirRaiz);
        arvore.setShowsRootHandles(true);
        arvore.setModel(this);
        switch (modo) {
            case SEMPRE_EXPANDIDO:
                expandirTudo();
                break;
        }
    }

    public void setObjetos(List objetos) {
        objetosRaiz = objetos;
        raiz.removerNosFilhos();
        for (Object o : objetosRaiz) {
            raiz.adicionarNoFilho(new NoArvore(o, false), false);
        }
        notificar(raiz);
        switch (modo) {
            case SEMPRE_EXPANDIDO:
                expandirTudo();
                break;
        }
    }

    final public void expandirTudo() {
        if (arvore == null) {
            return;
        }
        for (int i = 0; i < arvore.getRowCount(); i++) {
            arvore.expandRow(i);
        }
    }

    public abstract List getFilhos(Object objeto);

    private List chamarGetFilhos(NoArvore no) {
        if (no == raiz) {
            return objetosRaiz;
        } else {
            return getFilhos(no.objeto);
        }
    }

    protected Object getAdicionar(Object pai) {
        return null;
    }

    public void adicionar() {
        NoArvore noSelecionado = getNoSelecionado();
        Object adicionado = getAdicionar(noSelecionado.getObjeto());
        if (adicionado == null) {
            return;
        }
        noSelecionado.adicionarObjetoFilho(adicionado);
    }

    public void adicionar(Object pai, Object filho) {
        if (pai != null) {
            localizarNo(pai).adicionarObjetoFilho(filho);
        } else {
            raiz.adicionarObjetoFilho(filho);
        }
    }

    public void remover() {
        remover(getNoSelecionado());
    }

    public void remover(Object ojeto) {
        remover(localizarNo(ojeto));
    }

    public void remover(NoArvore no) {
        no.removerDoPai();
    }

    public NoArvore getNoSelecionado() {
        TreePath caminho = arvore.getSelectionPath();
        if (caminho == null) {
            return raiz;
        } else {
            return (NoArvore) caminho.getLastPathComponent();
        }
    }

    public Object getObjetoSelecionado() {
        NoArvore no = getNoSelecionado();
        return no != null ? no.getObjeto() : raiz;
    }

    private class NoArvore implements TreeNode {

        private NoArvore pai;
        private List<NoArvore> nosFilhos;
        private Object objeto;

        public NoArvore(Object objeto) {
            setObjeto(objeto, true);
        }

        public NoArvore(Object objeto, boolean notificar) {
            setObjeto(objeto, notificar);
        }

        @Override
        public String toString() {
            return objeto != null ? objeto.toString() : null;
        }

        public void adicionarObjetoFilho(Object filho) {
            adicionarObjetoFilho(filho, true);
        }

        public void adicionarObjetoFilho(Object filho, boolean notificar) {
            chamarGetFilhos(this).add(filho);
            adicionarNoFilho(new NoArvore(filho), notificar);
        }

        public void removerDoPai() {
            if (pai != null) {
                pai.removerNoFilho(this);
            }
        }

        public void removerNoFilho(NoArvore no) {
            int indice = nosFilhos.indexOf(no);
            removerNoFilho(no, indice);
        }

        public void removerNoFilho(int indice) {
            NoArvore no = nosFilhos.get(indice);
            removerNoFilho(no, indice);
        }

        private void removerNoFilho(NoArvore filho, int indice) {
            nosFilhos.remove(indice);
            notificarRemocao(getPathToRoot(this), new int[]{indice}, new Object[]{filho});
        }

        public Object getObjeto() {
            return objeto;
        }

        public void setObjeto(Object objeto) {
            setObjeto(objeto, true);
        }

        public void setObjeto(Object objeto, boolean notificar) {
            this.objeto = objeto;
            try {
                carregar(objeto, notificar);
            } catch (StackOverflowError er) {
                System.err.println("A árvore é infinita!");
            }
        }

        private void carregar(Object objeto, boolean notificar) {
            if (objeto == null) {
                return;
            }
            List filhosObjeto = chamarGetFilhos(this);
            if (filhosObjeto != null && filhosObjeto.size() > 0) {
                for (Object filho : filhosObjeto) {
                    adicionarNoFilho(new NoArvore(filho, false), notificar);
                }
            }
        }

        public void adicionarNoFilho(NoArvore filho) {
            adicionarNoFilho(filho, true);
        }

        public void adicionarNoFilho(NoArvore filho, boolean notificar) {
            if (nosFilhos == null) {
                nosFilhos = new ArrayList<NoArvore>();
            }
            int indice = nosFilhos.size();
            nosFilhos.add(indice, filho);
            if (notificar) {
                notificarAdicao(getPathToRoot(this), new int[]{indice}, new Object[]{filho});
            }
        }

        public void removerNosFilhos() {
            if (nosFilhos == null) {
                return;
            }
            for (NoArvore noArvore : nosFilhos) {
                noArvore.setPai(null);
            }
            nosFilhos = null;
        }

        public void setPai(NoArvore pai) {
            this.pai = pai;
        }

        public NoArvore getChildAt(int indice) {
            return nosFilhos.get(indice);
        }

        public int getChildCount() {
            return nosFilhos != null ? nosFilhos.size() : 0;
        }

        public NoArvore getParent() {
            return pai;
        }

        public int getIndex(TreeNode node) {
            int quantidadeFilhos = nosFilhos.size();
            for (int i = 0; i < quantidadeFilhos; i++) {
                if (nosFilhos.get(i) == node) {
                    return i;
                }
            }
            return -1;
        }

        public boolean getAllowsChildren() {
            return !isLeaf();
        }

        public boolean isLeaf() {
            return getChildCount() == 0;
        }

        public Enumeration children() {
            return new Enumeration<TreeNode>() {

                int contador = 0;

                public boolean hasMoreElements() {
                    return contador < nosFilhos.size();
                }

                public TreeNode nextElement() {
                    if (hasMoreElements()) {
                        return nosFilhos.get(contador++);
                    }
                    return null;
                }
            };
        }
    }

    public TreeNode[] getPathToRoot(TreeNode no) {
        return getPathToRoot(no, 0);
    }

    protected TreeNode[] getPathToRoot(TreeNode no, int profundidade) {
        TreeNode[] nos;

        if (no == null) {
            if (profundidade == 0) {
                return null;
            } else {
                nos = new TreeNode[profundidade];
            }
        } else {
            profundidade++;
            if (no == raiz) {
                nos = new TreeNode[profundidade];
            } else {
                nos = getPathToRoot(no.getParent(), profundidade);
            }
            nos[nos.length - profundidade] = no;
        }
        return nos;
    }

    public TreeNode getRoot() {
        return raiz;
    }

    public Object getChild(Object pai, int indice) {
//        NoArvore no = (NoArvore) pai;
//        return no.getChildAt(indice);
        TreeNode no = (TreeNode) pai;
        return no.getChildAt(indice);
    }

    public int getChildCount(Object pai) {
//        return ((NoArvore) pai).getChildCount();
        return ((TreeNode) pai).getChildCount();
    }

    public boolean isLeaf(Object no) {
//        return ((NoArvore) no).isLeaf();
        return ((TreeNode) no).isLeaf();
    }

    public void valueForPathChanged(TreePath caminho, Object novoValor) {
        NoArvore no = (NoArvore) caminho.getLastPathComponent();
        no.setObjeto(novoValor);
        notificar(no);
    }

    public int getIndexOfChild(Object pai, Object filho) {
//        return ((NoArvore) pai).getIndex((NoArvore) filho);
        return ((TreeNode) pai).getIndex((TreeNode) filho);
    }

    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }

    public void notificar(TreeNode no) {
        if (listenerList != null && no != null) {
            TreeNode pai = no.getParent();

            if (pai != null) {
                int indice = pai.getIndex(no);
                if (indice != -1) {
                    notificar(pai, new int[]{indice});
                }
            } else if (no == raiz) {
                notificar(no, null);
            }
        }
    }

    public void notificar(TreeNode no, int[] indicesFilhos) {
        if (no != null) {
            if (indicesFilhos != null) {
                int quantidadeFilhos = indicesFilhos.length;

                if (quantidadeFilhos > 0) {
                    Object[] filhos = new Object[quantidadeFilhos];

                    for (int i = 0; i < quantidadeFilhos; i++) {
                        filhos[i] = no.getChildAt(indicesFilhos[i]);
                    }
                    notificar(getPathToRoot(no),
                            indicesFilhos, filhos);
                }
            } else if (no == raiz) {
                notificar(getPathToRoot(no), null, null);
            }
        }
    }

    private void notificar(Object[] caminho, int[] indicesFilhos, Object[] filhos) {
        notificar(new TreeModelEvent(this, caminho, indicesFilhos, filhos));
    }

    private void notificar(TreeModelEvent evento) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                ((TreeModelListener) listeners[i + 1]).treeNodesChanged(evento);
            }
        }
    }

//    public void notificarAdicao(TreeNode noPai, int[] indicesFilhos) {
//        if (listenerList != null && noPai != null && indicesFilhos != null && indicesFilhos.length > 0) {
//            int quantidadeFilhos = indicesFilhos.length;
//            Object[] novosFilhos = new Object[quantidadeFilhos];
//
//            for (int i = 0; i < quantidadeFilhos; i++) {
//                novosFilhos[i] = noPai.getChildAt(indicesFilhos[i]);
//            }
//
//            notificarAdicao(getPathToRoot(noPai), indicesFilhos, novosFilhos);
//        }
//    }
    private void notificarAdicao(Object[] caminho, int[] indicesFilhos, Object[] filhosAdicionados) {
        notificarAdicao(new TreeModelEvent(this, caminho, indicesFilhos, filhosAdicionados));
    }

    private void notificarAdicao(TreeModelEvent evento) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                ((TreeModelListener) listeners[i + 1]).treeNodesInserted(evento);
            }
        }
    }

    public void notificarRemocao(TreeNode pai, int[] indicesFilhos, Object[] removidos) {
        if (pai != null && indicesFilhos != null) {
            notificarRemocao(getPathToRoot(pai), indicesFilhos, removidos);
        }
    }

    private void notificarRemocao(Object[] caminho, int[] indicesFilhos, Object[] filhosRemovidos) {
        notificarRemocao(new TreeModelEvent(this, caminho, indicesFilhos, filhosRemovidos));
    }

    private void notificarRemocao(TreeModelEvent evento) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                ((TreeModelListener) listeners[i + 1]).treeNodesRemoved(evento);
            }
        }
    }

    private void notificarEstrutura() {
        Object[] caminho = null;
        notificarEstrutura(caminho);
    }

    private void notificarEstrutura(TreeNode no) {
        notificarEstrutura(getPathToRoot(no), null, null);
    }

    private void notificarEstrutura(Object[] caminho) {
        notificarEstrutura(new TreeModelEvent(this, caminho));
    }

    private void notificarEstrutura(Object[] caminho, int[] indicesFilhos, Object[] filhos) {
        notificarEstrutura(new TreeModelEvent(this, caminho, indicesFilhos, filhos));
    }

    private void notificarEstrutura(TreeModelEvent evento) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                ((TreeModelListener) listeners[i + 1]).treeStructureChanged(evento);
            }
        }
    }
}

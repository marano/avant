package avant.view;

import avant.view.model.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.KeyEventPostProcessor;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

/**
 *
 * @author marano
 */
public class AvantViewUtil {

    public static final TransferidorFocoEnter transferidorFocoEnter = new TransferidorFocoEnter();
    public static final FechadorEsc fechadorEsc = new FechadorEsc();
    public static final SelecionadorTexto selecionadorTexto = new SelecionadorTexto();
    public static final int RESPOSTA1 = 1;
    public static final int RESPOSTA2 = 2;
    public static final int RESPOSTA3 = 3;
    public static final int SEM_RESPOSTA = 4;
    public static final int ADICIONAR = 1;
    public static final int REMOVER = 2;
    public static List<Class> telasSemFechamentoPorEsc = new ArrayList<Class>();
    public static JDesktopPane desktop;

    public static void pararEdicao(JTable tabela) {
        if (tabela.isEditing()) {
            tabela.getCellEditor().stopCellEditing();
        }
    }

    public static boolean perguntar(String pergunta, Component pai) {
        return perguntar(pergunta, new String[]{"Sim", "Não"}, pai) == RESPOSTA1 ? true : false;
    }

    public static JDesktopPane getDesktop() {
        return desktop;
    }

    public static void setDesktop(JDesktopPane desktop) {
        AvantViewUtil.desktop = desktop;
    }

    public static boolean isTelaAtual(JInternalFrame frame) {
        return (desktop != null && frame != null) ? desktop.getSelectedFrame().equals(frame) : false;
    }

    public static int perguntar(String pergunta, String[] respostas, Component pai) {
        int tipo = respostas.length > 2 ? JOptionPane.YES_NO_CANCEL_OPTION : JOptionPane.YES_NO_OPTION;
        int resposta = JOptionPane.showOptionDialog(pai, pergunta, getTitulo(pai), tipo, JOptionPane.QUESTION_MESSAGE, null, respostas, null);
        switch (resposta) {
            case JOptionPane.YES_OPTION:
                resposta = RESPOSTA1;
                break;
            case JOptionPane.NO_OPTION:
                resposta = RESPOSTA2;
                break;
            case JOptionPane.CANCEL_OPTION:
                resposta = RESPOSTA3;
                break;
            case JOptionPane.CLOSED_OPTION:
                resposta = SEM_RESPOSTA;
                break;
        }
        return resposta;
    }

    /*
     * @deprecated Este método deve ser substituído pelos métodos exibirInformacao,
     * exibirAdvertencia e exibirErro conforme cada caso
     */
    @Deprecated
    public static void exibir(String mensagem, Component pai) {
        JOptionPane.showMessageDialog(pai, mensagem, getTitulo(pai), JOptionPane.INFORMATION_MESSAGE);
    }

    public static void exibirInformacao(String mensagem, Component pai) {
        JOptionPane.showMessageDialog(pai, mensagem, getTitulo(pai), JOptionPane.INFORMATION_MESSAGE);
    }

    public static void exibirAdvertencia(String mensagem, Component pai) {
        JOptionPane.showMessageDialog(pai, mensagem, getTitulo(pai), JOptionPane.WARNING_MESSAGE);
    }

    public static void exibirErro(String mensagem, Component pai) {
        JOptionPane.showMessageDialog(pai, mensagem, getTitulo(pai), JOptionPane.ERROR_MESSAGE);
    }

    public static String pegarResposta(String mensagem, Component pai) {
        return JOptionPane.showInputDialog(pai, mensagem, getTitulo(pai), JOptionPane.INFORMATION_MESSAGE);
    }

    public static String pegarResposta(String mensagem, String valorInicial, Component pai) {
        return JOptionPane.showInputDialog(pai, mensagem, valorInicial);
    }

    public static String getTitulo(Component componente) {
        componente = getContainerPai(componente);
        if (componente != null) {
            if (componente instanceof JInternalFrame) {
                return ((JInternalFrame) componente).getTitle();
            } else if (componente instanceof Frame) {
                return ((Frame) componente).getTitle();
            } else if (componente instanceof Dialog) {
                return ((Dialog) componente).getTitle();
            }
        }
        return "Sistema";
    }

    public static void expandirArvore(JTree arvore) {
        int x = 1;
        int linhas = arvore.getRowCount();
        while ((linhas - 1) >= x) {
            arvore.expandPath(arvore.getPathForRow(x));
            linhas = arvore.getRowCount();
            x++;
        }
    }

    public static void adicionarListener(EventListener listener, Object... componentes) {
        for (Object c : componentes) {
            adicionarListener(listener, c);
        }
    }

    public static void adicionarListener(EventListener listener, Object componente) {
        tratarListener(ADICIONAR, listener, componente);
    }

    public static void removerListener(EventListener listener, Object componente) {
        tratarListener(REMOVER, listener, componente);
    }

    public static void tratarListener(int operacao, EventListener listener, Object componente) {
        List<Class<? extends EventListener>> classes = new ArrayList<Class<? extends EventListener>>();
        if (listener instanceof ActionListener) {
            classes.add(ActionListener.class);
        }
        if (listener instanceof MouseListener) {
            classes.add(MouseListener.class);
        }
        if (listener instanceof KeyListener) {
            classes.add(KeyListener.class);
        }
        if (listener instanceof FocusListener) {
            classes.add(FocusListener.class);
        }
        if (listener instanceof ListSelectionListener) {
            classes.add(ListSelectionListener.class);
        }
        if (listener instanceof ItemListener) {
            classes.add(ItemListener.class);
        }
        String nometMetodo = operacao == REMOVER ? "remove" : "add";
        for (Class<? extends EventListener> classe : classes) {
            executar(listener, componente, nometMetodo + classe.getSimpleName(), classe);
        }
    }

    public static void executar(EventListener listener, Object componente, String nomeMetodo, Class classe) {
        try {
            componente.getClass().getMethod(nomeMetodo, classe).invoke(componente, listener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Container getContainerPai(Component componente) {
        if (componente == null) {
            return null;
        }
        return componente instanceof JInternalFrame || componente instanceof Window
                ? (Container) componente : getContainerPai(componente.getParent());
    }

    public static void fechar(Component componente) {
        if (componente instanceof JInternalFrame) {
            ((JInternalFrame) componente).doDefaultCloseAction();
        } else if (componente instanceof Window) {
            ((Window) componente).dispose();
        }
    }

    public static Component[] toArray(List<Component> lista) {
        Component[] array = new Component[lista.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = lista.get(i);
        }
        return array;
    }

    private static class FechadorEsc implements KeyEventPostProcessor {

        private long ultimo;

        public boolean postProcessKeyEvent(KeyEvent e) {
            if (e.getKeyCode() == e.VK_ESCAPE &&
                    e.getID() == KeyEvent.KEY_RELEASED &&
                    System.currentTimeMillis() - ultimo > 300) {
                ultimo = System.currentTimeMillis();
                if (e.getSource() instanceof Component) {
                    Component pai = AvantViewUtil.getContainerPai((Component) e.getSource());
                    if (pai != null && !(telasSemFechamentoPorEsc.contains(pai.getClass()))) {
                        AvantViewUtil.fechar(pai);
                    }
                }
                return true;
            }
            return false;
        }
    }

    private static class TransferidorFocoEnter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                ((Component) e.getSource()).transferFocus();
            }
        }
    }

    private static class SelecionadorTexto implements FocusListener {

        public void focusGained(FocusEvent e) {
            if (e.getSource() instanceof JTextComponent) {
                selecionarTexto((JTextComponent) e.getSource());
            }
        }

        public void focusLost(FocusEvent e) {
        }
    }

    public static void normalizarTexto(JTextComponent texto) {
        //texto.setBorder(new LineBorder(SystemColor.textHighlight));
    }

    public static void destacarTexto(JTextComponent texto) {
        texto.grabFocus();
        selecionarTexto(texto);
    //texto.setBorder(new LineBorder(Color.RED));
    }

    public static void selecionarTexto(JTextComponent texto) {
        texto.select(0, texto.getDocument().getLength());
    }

    public static <T> void set(T objeto, List<T> lista, AvantAbstractModel<T>... modelos) {
        int indice = lista.indexOf(objeto);
        if (indice < 0) {
            lista.add(objeto);
        } else {
            lista.set(indice, objeto);
        }
        for (AvantAbstractModel modelo : modelos) {
            modelo.set(objeto);
        }
    }

    public static <T> void set(T objeto, AvantAbstractModel<T>... modelos) {
        for (AvantAbstractModel modelo : modelos) {
            modelo.set(objeto);
        }
    }

    public static Frame getFrame() {
        return null;
    }

    public static void fecharNoConstrutor(final JInternalFrame frame) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                frame.doDefaultCloseAction();
            }
        });
    }
}

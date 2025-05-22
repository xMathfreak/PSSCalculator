import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class PSSCalculator {
	public static final int MAX_ROOM_SIZE = 5;

	public static double calculateRush(double ABL, double HP) {
		if (ABL == 0 || HP == 0) return 0;
		return 100 * ((1 + (ABL / 50)) / HP);
	}

	public static double calculateBoost(double baseReload, double totalStats) {
		return baseReload / (1 + (totalStats / 100));
	}

	private static void applyFontToContainer(Container container, Font font) {
		for (Component c : container.getComponents()) {
			c.setFont(font);
			if (c instanceof Container) {
				applyFontToContainer((Container) c, font);
			}
		}
	}

	private static JLabel createCenteredLabel(String text) {
		JLabel l = new JLabel(text);
		l.setAlignmentX(Component.CENTER_ALIGNMENT);
		return l;
	}

	private static JFrame createMainFrame() {
		JFrame frame = new JFrame("PSS Calculator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return frame;
	}

	private static JButton createToggleButton(String text, JPanel targetPanel, Container container) {
		JButton button = new JButton(text);
		button.setPreferredSize(new Dimension(120, 40));
		button.addActionListener(e -> {
			for (Component c : container.getComponents()) {
				c.setVisible(c == targetPanel);
			}
		});
		return button;
	}

	private static void createRushPanel(JPanel panel) {
		JPanel rushStatPanel = new JPanel();
		rushStatPanel.setLayout(new BoxLayout(rushStatPanel, BoxLayout.Y_AXIS));

		JTextField[] rushStats = new JTextField[MAX_ROOM_SIZE + 1];
		JTextField rushOutput = new JTextField("0.0");
		rushOutput.setEditable(false);

		for (int i = 0; i <= MAX_ROOM_SIZE; ++i) {
			rushStats[i] = new JTextField(20);
			JLabel label = createCenteredLabel(i == 0 ? "Room HP" : "Rush Stat " + i);
			if (i > 1) rushStats[i].setEnabled(false);
			int index = i;
			rushStats[i].getDocument().addDocumentListener(new DocumentListener() {
				public void insertUpdate(DocumentEvent e) { update(); }
				public void removeUpdate(DocumentEvent e) { update(); }
				public void changedUpdate(DocumentEvent e) { update(); }
				private void update() {
					if (index > 0 && index + 1 < rushStats.length) {
						boolean filled = !rushStats[index].getText().isEmpty();
						rushStats[index + 1].setEnabled(filled);
						if (!filled) rushStats[index + 1].setText("");
					}
					try {
						double total = 0.0;
						double hp = Math.max(Double.parseDouble(rushStats[0].getText()), 0);
						for (int j = 1; j < rushStats.length; j++) {
							if (!rushStats[j].getText().isEmpty()) {
								double val = Math.max(Double.parseDouble(rushStats[j].getText()), 0);
								total += calculateRush(val, hp);
							}
						}
						rushOutput.setText(String.format("%.2f", total));
					} catch (NumberFormatException ignored) {}
				}
			});
			rushStatPanel.add(label);
			rushStatPanel.add(rushStats[i]);
		}

		rushStatPanel.add(createCenteredLabel("Rush%"));
		rushStatPanel.add(rushOutput);
		panel.add(rushStatPanel);
	}

	private static void createBoostPanel(JPanel panel) {
		JPanel boostStatPanel = new JPanel();
		boostStatPanel.setLayout(new BoxLayout(boostStatPanel, BoxLayout.Y_AXIS));

		JTextField[] boostStats = new JTextField[MAX_ROOM_SIZE + 1];
		JTextField boostOutput = new JTextField("0.0");
		boostOutput.setEditable(false);

		for (int i = 0; i <= MAX_ROOM_SIZE; ++i) {
			boostStats[i] = new JTextField(20);
			JLabel label = createCenteredLabel(i == 0 ? "Room base reload speed" : "Crew Stat " + i);
			if (i > 1) boostStats[i].setEnabled(false);
			int index = i;
			boostStats[i].getDocument().addDocumentListener(new DocumentListener() {
				public void insertUpdate(DocumentEvent e) { update(); }
				public void removeUpdate(DocumentEvent e) { update(); }
				public void changedUpdate(DocumentEvent e) { update(); }
				private void update() {
					if (index > 0 && index + 1 < boostStats.length) {
						boolean filled = !boostStats[index].getText().isEmpty();
						boostStats[index + 1].setEnabled(filled);
						if (!filled) boostStats[index + 1].setText("");
					}
					try {
						double reload = Math.max(Double.parseDouble(boostStats[0].getText()), 0);
						double total = 0.0;
						for (int j = 1; j < boostStats.length; j++) {
							if (!boostStats[j].getText().isEmpty()) {
								total += Math.max(Double.parseDouble(boostStats[j].getText()), 0);
							}
						}
						boostOutput.setText(String.format("%.2f", calculateBoost(reload, total)));
					} catch (NumberFormatException ignored) {}
				}
			});
			boostStatPanel.add(label);
			boostStatPanel.add(boostStats[i]);
		}

		JLabel outputLabel = createCenteredLabel("Reload Time");
		boostStatPanel.add(outputLabel);
		boostStatPanel.add(boostOutput);
		panel.add(boostStatPanel);
	}

	private static JDialog createMainDialog(JFrame frame) {
		JDialog dialog = new JDialog(frame, "PSS Calculator", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JPanel contentPanel = new JPanel();

		JPanel rushPanel = new JPanel();
		rushPanel.setLayout(new BoxLayout(rushPanel, BoxLayout.Y_AXIS));
		rushPanel.add(createCenteredLabel("Rush Calculator"));
		createRushPanel(rushPanel);
		contentPanel.add(rushPanel);

		JPanel boostPanel = new JPanel();
		boostPanel.setLayout(new BoxLayout(boostPanel, BoxLayout.Y_AXIS));
		boostPanel.add(createCenteredLabel("Boost Calculator"));
		createBoostPanel(boostPanel);
		boostPanel.setVisible(false);
		contentPanel.add(boostPanel);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(createToggleButton("Rush", rushPanel, contentPanel));
		buttonPanel.add(createToggleButton("Boost", boostPanel, contentPanel));

		mainPanel.add(buttonPanel);
		mainPanel.add(contentPanel);
		dialog.add(mainPanel);

		dialog.setMinimumSize(new Dimension(400, 470));
		dialog.setLocationRelativeTo(frame);
		return dialog;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = createMainFrame();
			JDialog dialog = createMainDialog(frame);
			applyFontToContainer(dialog, new Font("Arial", Font.PLAIN, 18));
			
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					frame.dispose();
					System.exit(0);
				}
			});

			dialog.setVisible(true);
		});
	}
}

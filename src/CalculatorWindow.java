import javax.swing.*;
import javax.imageio.ImageIO;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class CalculatorWindow {

	public static CalculatorWindow Instance;

	private JFrame frame;

	private JTextField expressionTextField;

	private boolean movingWindow;
	private Point dragPos = new Point();

	private final Color textSelectColor = new Color(255, 0, 0);

	private final int pixelWidth = 100;

	public int getPixelSize() {
		return frame.getWidth() / pixelWidth;
	}

	public CalculatorWindow() throws IOException {
		if (Instance == null) {
			Instance = this;
		} else {
			return;
		}

		frame = new JFrame("Medieval Calculator");
		BufferedImage scrollIconImage = ImageIO
				.read(App.class.getResource("/resources/images/icons/scroll-32x32.png"));
		frame.setIconImage(scrollIconImage);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		int size = 500;
		frame.setSize(new Dimension(size, size));
		frame.setResizable(false);
		frame.setUndecorated(true);
		frame.setBackground(new Color(1, 1, 1, 0.0f));
		frame.setLayout(null);

		int sw = Toolkit.getDefaultToolkit().getScreenSize().width;
		int sh = Toolkit.getDefaultToolkit().getScreenSize().height;
		int fw = frame.getSize().width;
		int fh = frame.getSize().height;

		frame.setLocation(sw / 2 - fw / 2, sh / 2 - fh / 2);

		// Font oldStyleFont = getFontFromFile("/resources/fonts/NotJamOldStyle14.ttf");
		Font undeadPixelFont = getFontFromFile("/resources/fonts/Undead Pixel 8.ttf");
		Font blackLetterFont = getFontFromFile("/resources/fonts/NotJamBlkltr16.ttf");

		Font defaultFont = undeadPixelFont;

		BufferedImage mainScrollImage = ImageIO
				.read(App.class.getResource("/resources/images/scroll-main-panel-100x100.png"));
		JPanel mainPanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (mainScrollImage != null) {
					g.drawImage(mainScrollImage, 0, 0, fw, fh, new Color(0, 0, 0, 0), null);
				}
			}
		};
		mainPanel.setLayout(null);
		mainPanel.setBounds(0, 0, fw, fh);
		mainPanel.setOpaque(false);
		mainPanel.setFocusable(false);

		System.setProperty("awt.useSystemAAFontSettings", "off");
		System.setProperty("swing.aatext", "false");

		JLabel titleLabel = getTitleLabel(fw, blackLetterFont);
		frame.add(titleLabel);

		BufferedImage minimizeImage = ImageIO.read(App.class.getResource("/resources/images/minimize-4x4.png"));
		JPanel clickableMinimizePanel = getClickableMinimizePanel(minimizeImage);
		frame.add(clickableMinimizePanel);

		BufferedImage crossImage = ImageIO.read(App.class.getResource("/resources/images/cross-4x4.png"));
		JPanel clickableCloseCross = getClickableCloseCross(crossImage);
		frame.add(clickableCloseCross);

		JTextField resultTextField = getResultTextField(fw, defaultFont);
		frame.add(resultTextField);

		expressionTextField = getExpressionTextField(fw, defaultFont, resultTextField);
		frame.add(expressionTextField);

		// NUMBER CLICKABLE LABELS
		int[][] numberLabelRows = new int[][] {
				{ 7, 8, 9 },
				{ 4, 5, 6 },
				{ 1, 2, 3 }
		};
		int buttonSpacing = 1;
		int buttonWidth = 11;
		for (int i = 0; i < numberLabelRows.length; i++) {
			for (int j = 0; j < numberLabelRows[i].length; j++) {
				int num = numberLabelRows[i][j];
				JLabel clickableLabel = UIElementFactory.createClickableLabel(String.valueOf(num), defaultFont,
						() -> addToExpressionText(String.valueOf(num)));
				setComponentBoundsInPixels(
						clickableLabel,
						8 + (buttonWidth + buttonSpacing) * j,
						46 + (buttonWidth + buttonSpacing) * i,
						buttonWidth,
						buttonWidth);
				frame.add(clickableLabel);
			}
		}

		// 0 CLICKABLE LABEL
		JLabel cLabel0 = UIElementFactory.createClickableLabel("0", defaultFont, () -> {
			addToExpressionText("0");
		});
		setComponentBoundsInPixels(
				cLabel0,
				8,
				46 + (buttonWidth + buttonSpacing) * 3,
				buttonWidth,
				buttonWidth);
		frame.add(cLabel0);

		// OPERATOR CLICKABLE LABELS
		String[][] operatorLabelRows = new String[][] {
				{ "+", "-" },
				{ "*", "/" },
				{ "^" }
		};
		int xoffset = 8 + (buttonWidth + buttonSpacing) * 3 + 1;
		for (int i = 0; i < operatorLabelRows.length; i++) {
			for (int j = 0; j < operatorLabelRows[i].length; j++) {
				String value = operatorLabelRows[i][j];
				JLabel clickableLabel = UIElementFactory.createClickableLabel(value, defaultFont,
						() -> addToExpressionText(value));
				setComponentBoundsInPixels(
						clickableLabel,
						xoffset + (buttonWidth + buttonSpacing) * j,
						46 + (buttonWidth + buttonSpacing) * i,
						buttonWidth,
						buttonWidth);
				frame.add(clickableLabel);
			}
		}

		// BRACKETS CLICKABLE LABELS
		JLabel cLabelLeftBracket = UIElementFactory.createClickableLabel("(", defaultFont,
				() -> addToExpressionText("("));
		setComponentBoundsInPixels(
				cLabelLeftBracket,
				xoffset,
				46 + (buttonWidth + buttonSpacing) * 3,
				buttonWidth,
				buttonWidth);
		frame.add(cLabelLeftBracket);
		JLabel cLabelRightBracket = UIElementFactory.createClickableLabel(")", defaultFont,
				() -> addToExpressionText(")"));
		setComponentBoundsInPixels(
				cLabelRightBracket,
				xoffset + buttonWidth + buttonSpacing,
				46 + (buttonWidth + buttonSpacing) * 3,
				buttonWidth,
				buttonWidth);
		frame.add(cLabelRightBracket);

		JLabel cLabelClear = UIElementFactory.createClickableLabel("CLEAR", defaultFont, 29f, () -> {
			expressionTextField.setText("");
		});
		setComponentBoundsInPixels(
				cLabelClear,
				xoffset + (buttonWidth + buttonSpacing) * 2 + 1,
				46,
				fw / getPixelSize() - 18
						- (buttonWidth + buttonSpacing)
								* (numberLabelRows[0].length + operatorLabelRows[0].length),
				buttonWidth);
		frame.add(cLabelClear);

		movingWindow = false;
		int topBarHeight = 12 * getPixelSize();
		frame.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				int y = e.getY();
				if (y <= topBarHeight) {
					movingWindow = true;
					dragPos = e.getPoint();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				movingWindow = false;
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

		});
		frame.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				if (movingWindow && dragPos != null) {
					int dx = x - dragPos.x;
					int dy = y - dragPos.y;
					frame.setLocation(frame.getLocation().x + dx, frame.getLocation().y + dy);
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
			}

		});

		mainPanel.repaint();
		frame.add(mainPanel);
	}

	private JLabel getTitleLabel(int fw, Font blackLetterFont) {
		JLabel titleLabel = new JLabel("Medieval Calculator");
		int titlePos = 8;
		setComponentBoundsInPixels(titleLabel, titlePos, 3, fw / getPixelSize() - titlePos * 2, 6);
		titleLabel.setFont(blackLetterFont.deriveFont(32f));
		titleLabel.setForeground(new Color(128, 0, 0));
		titleLabel.setHorizontalAlignment(JLabel.CENTER);
		return titleLabel;
	}

	private JTextField getExpressionTextField(int fw, Font defaultFont, JTextField resultTextField) {
		JTextField expressionTextField = new JTextField("");
		setComponentBoundsInPixels(expressionTextField, 8, 30, fw / getPixelSize() - 16, 14);
		expressionTextField.setFont(defaultFont.deriveFont(32f));
		expressionTextField.setHorizontalAlignment(JTextField.CENTER);
		expressionTextField.setBorder(new LineBorder(new Color(0.25f, 0, 0, 0.4f), getPixelSize()));
		expressionTextField.setBackground(new Color(182, 142, 58));
		expressionTextField.setForeground(new Color(96, 0, 0));
		expressionTextField.setSelectionColor(textSelectColor);
		expressionTextField.setSelectedTextColor(Color.BLACK);
		expressionTextField.setFocusTraversalKeysEnabled(false);
		expressionTextField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				OnChange();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				OnChange();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}

			private void OnChange() {
				String text = expressionTextField.getText();
				Expression expression = new Expression(text);
				expression.listAllsubexpressionStrings();
				double result = expression.getValue();
				if (result == (int) result) {
					resultTextField.setText(String.valueOf((int) result));
				} else {
					resultTextField.setText(String.valueOf(result));
				}
			}

		});
		return expressionTextField;
	}

	private JTextField getResultTextField(int fw, Font defaultFont) {
		JTextField resultTextField = new JTextField("NaN");
		setComponentBoundsInPixels(resultTextField, 8, 14, fw / getPixelSize() - 16, 14);
		resultTextField.setFont(defaultFont.deriveFont(32f));
		resultTextField.setForeground(new Color(64, 0, 0));
		resultTextField.setBackground(new Color(182, 142, 58));
		resultTextField.setSelectionColor(textSelectColor);
		resultTextField.setSelectedTextColor(Color.BLACK);
		resultTextField.setHorizontalAlignment(JTextField.CENTER);
		resultTextField.setBorder(
				BorderFactory.createDashedBorder(new Color(0, 0, 0, 0.2f), getPixelSize(), 3,
						2, false));
		resultTextField.setEditable(false);
		return resultTextField;
	}

	private JPanel getClickableCloseCross(BufferedImage crossImage) {
		JPanel clickableCloseCross = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (crossImage != null) {
					g.drawImage(crossImage, 0, 0, 4 * getPixelSize(), 4 * getPixelSize(), new Color(0, 0, 0, 0), null);
				}
			}
		};
		setComponentBoundsInPixels(clickableCloseCross, 93, 4, 4, 4);
		clickableCloseCross.setOpaque(false);
		clickableCloseCross.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				System.exit(0);
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}

		});
		return clickableCloseCross;
	}

	private JPanel getClickableMinimizePanel(BufferedImage minimizeImage) {
		JPanel clickableMinimizePanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (minimizeImage != null) {
					g.drawImage(minimizeImage, 0, 0, 4 * getPixelSize(), 4 * getPixelSize(), new Color(0, 0, 0, 0),
							null);
				}
			}
		};
		clickableMinimizePanel.setOpaque(false);
		clickableMinimizePanel.setBackground(Color.RED);
		setComponentBoundsInPixels(clickableMinimizePanel, 87, 4, 4, 4);
		clickableMinimizePanel.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				frame.setState(Frame.ICONIFIED);
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}

		});
		return clickableMinimizePanel;
	}

	public void show() {
		frame.setVisible(true);
		expressionTextField.requestFocusInWindow();
	}

	private void setComponentBoundsInPixels(Component c, int x, int y, int w, int h) {
		c.setBounds(getPixelSize() * x, getPixelSize() * y, getPixelSize() * w, getPixelSize() * h);
	}

	private Font getFontFromFile(String fontFilePath) {
		Font resultFont;
		try {
			InputStream fontStream = App.class.getResourceAsStream(fontFilePath);
			resultFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(18f);
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(resultFont);
		} catch (IOException | FontFormatException e) {
			e.printStackTrace();
			resultFont = new Font("Serif", Font.PLAIN, 18);
		}
		return resultFont;
	}

	private void addToExpressionText(String text) {
		if (expressionTextField.isFocusOwner()) {
			int caretPosition = expressionTextField.getCaretPosition();
			String preText = expressionTextField.getText().substring(0, caretPosition);
			String postText = expressionTextField.getText().substring(caretPosition);
			expressionTextField.setText(preText + text + postText);
			expressionTextField.setCaretPosition(caretPosition + 1);
		} else {
			expressionTextField.setText(expressionTextField.getText().concat(text));
			expressionTextField.requestFocus();
		}
	}
}

import javax.swing.*;
import javax.swing.border.LineBorder;

import java.awt.*;
import java.awt.event.*;

public class UIElementFactory {

	private static final Color defaultTextColor = new Color(64, 0, 0);
	private static final Color hoverTextColor = new Color(96, 0, 0);
	private static final Color pressTextColor = new Color(144, 0, 0);

	private static final Color defaultBorderColor = new Color(0.1f, 0, 0, 0.25f);
	private static final Color hoverBorderColor = new Color(0.2f, 0, 0, 0.5f);
	private static final Color pressBorderColor = new Color(0.25f, 0, 0, 0.75f);

	public static JLabel createClickableLabel(String text, Font font, float fontSize,
			Runnable clickAction) {
		JLabel button = new JLabel(text) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();

				String text = getText();
				Font font = getFont();
				g2.setFont(font);

				FontMetrics fm = g2.getFontMetrics();
				Rectangle textBounds = fm.getStringBounds(text, g2).getBounds();

				int x = (getWidth() - textBounds.width) / 2 - textBounds.x;
				int y = (getHeight() - textBounds.height) / 2 + fm.getAscent();

				// Clear background (optional)
				g2.setColor(getBackground());
				g2.fillRect(0, 0, getWidth(), getHeight());

				// Draw the text in foreground color
				g2.setColor(getForeground());
				g2.drawString(text, x + 2, y + 3);

				g2.dispose();
			}
		};
		button.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				clickAction.run();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				button.setForeground(pressTextColor);
				button.setBorder(
						new LineBorder(pressBorderColor, CalculatorWindow.Instance.getPixelSize()));
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				Point mousePos = button.getMousePosition();
				if (mousePos != null && button.contains(mousePos)) {
					button.setForeground(hoverTextColor);
					button.setBorder(new LineBorder(hoverBorderColor,
							CalculatorWindow.Instance.getPixelSize()));
				} else {
					button.setForeground(defaultTextColor);
					button.setBorder(new LineBorder(defaultBorderColor,
							CalculatorWindow.Instance.getPixelSize()));
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				button.setForeground(hoverTextColor);
				button.setBorder(
						new LineBorder(hoverBorderColor, CalculatorWindow.Instance.getPixelSize()));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				button.setForeground(defaultTextColor);
				button.setBorder(new LineBorder(defaultBorderColor,
						CalculatorWindow.Instance.getPixelSize()));
			}

		});
		button.setOpaque(true);
		button.setFont(font.deriveFont(fontSize));
		button.setFocusable(false);
		button.setBackground(new Color(182, 142, 58));
		button.setForeground(defaultTextColor);
		button.setBorder(
				new LineBorder(defaultBorderColor, CalculatorWindow.Instance.getPixelSize()));
		button.setHorizontalAlignment(JLabel.CENTER);
		return button;
	}

	public static JLabel createClickableLabel(String text, Font font, Runnable clickAction) {
		return createClickableLabel(text, font, 32f, clickAction);
	}

}

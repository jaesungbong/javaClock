import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class ClockApp extends JFrame implements Runnable {
	int nowTimeSec = 0, setTimeSec = 0, remainTimeSec = -1;	// ����ð�, �˶��ð�, �����ð��� �ʷ� ���
	boolean waitAlarm;	// �˶� ����ġ. �˶��� ������ ��� true, �˶��� ���� ��� false
	String todayH, todayM, todayS, ampm;	// ���� �ð��� �ð�, ��, ��, �������ĸ� ��Ÿ�� String
	AudioClip clip; // �˶� ������ ��� AudioClip
	BufferedImage biH, biM, biS; // ��ħ, ��ħ, ������ �̹���
	DragWindowListener dwl;	// ���콺 �巡�׽� �ð谡 ���󰡵��� �� ���콺������
	Thread timeTh;	// 1�ʸ��� �ð��� �̹����� �˶��� �����ð��� ǥ���ϴ� ������  
	JLabel iconAlarm, iconMini, iconClose;	// �˶�������, �ּ�ȭ������, ��������� �׸��� ��� ��
	JLabel nowTime, ClockPane, ampmLa, setTimeBtn, setAmPm; // ����ð�, �ð��� �׸�, �����������, �˶�������ư, �˶��������ļ��� ��
	JPanel setAlarm;	// �˶� ����â, �˶� ���������� ���߱� �� ���̱� �ϴ� â
	JTextField setHHTx, setMMTx, setSSTx, setHHTxRe, setMMTxRe, setSSTxRe;	// �˶� ������ ��, ��, �ʸ� �Է¹ް� �����ð��� ��Ÿ���� �ؽ�Ʈ�ʵ�

	public ClockApp() {	// �ð� JFrame�� ������. ������Ʈ���� �����ϰ� �ʱ�ȭ�Ѵ�.
		super("�Ƴ��α� �ð�");	// JFrame�� Ÿ��Ʋ
		setSize(new Dimension(400, 500));	// JFrame�� ũ�� ����400 ����500
		setLocationRelativeTo(null);	// ó�� â�� ����ɶ� �߰��� ��ġ�ϵ��� �ϴ� �ڵ�
		setUndecorated(true); // ������ â�� �׵θ� ����
		setBackground(new Color(0, 0, 0, 0));	// JFrame�� ���� ����(����ȭ ��Ű�� ���Ͽ�)
		dwl = new DragWindowListener();	// ���콺�巡�� �����ʸ� ����
		JPanel panel = new JPanel() {	// ������ ȿ���� �� �ǳ��� �����.
			protected void paintComponent(Graphics g) {
				if (g instanceof Graphics2D) {	// �׷��� g�� �����´�. 
					final int R = 255;	// RGB �ڵ��� R �� 255�� ����
					final int G = 255;	// RGB �ڵ��� G �� 255�� ����
					final int B = 255;	// RGB �ڵ��� B �� 255�� ����
					Paint p = new GradientPaint(0.0f, 0.0f, new Color(R, G, B,
							0), 0.0f, getHeight(), new Color(R, G, B, 0), true);	// �׶��̼� ȿ���� �ִ� ����Ʈ p ����
					Graphics2D g2d = (Graphics2D) g;	// g�� Graphics2D Ÿ������ ����ȯ ��Ų��
					g2d.setPaint(p);	// ����Ʈp�� ĥ���ش�
					g2d.fillRect(0, 0, getWidth(), getHeight());	//����Ʈ�� ĥ���� ������ �簢���� �ǳڿ� ������ �׸���. 
				}
			}
		};	// ������ �ǳ� ���� ����
		setContentPane(panel);	// ����Ʈ�濡 ����Ʈ�� ĥ�� �ǳ��� �ٿ��ش�
		setLayout(null);	// ��ġ �����ڸ� null�� ����
		try {	
			clock();	// �ð� �� ������Ʈ���� ���̰� GUI�� �ʱ�ȭ�ϴ� �Լ�
		} catch (IOException e) {	// ����� ���ܰ� �߻��ϸ�
			e.printStackTrace();	// �� ���ܸ� ����Ʈ�Ѵ�.
		}
	} // �ð� JFrame�� �������� ��

	public void clock() throws IOException {	// �ð� �� ������Ʈ���� ���̰� GUI�� �ʱ�ȭ�ϴ� �Լ�
		clip = null;	// ������ ���� �����Ŭ���� �ʱ�ȭ�Ѵ�.
		URL audioURL = getClass().getResource("ring.wav");	// ���������� URL��θ� ����
		clip = Applet.newAudioClip(audioURL);	// ���������� ������ ������ �� �ֵ��� �غ�
		Calendar calendar = Calendar.getInstance();	// ���� �ð��� �����´�
		java.util.Date date = calendar.getTime();	// date��ü�� ����ð��� ��´�
		waitAlarm = false;	// �˶� ���� üũ ����ġ ����
		todayH = (new SimpleDateFormat("HH").format(date));	// ���� �ð��� SimpleDateFormat�� �̿��Ͽ� �����´�.(�� �ڸ��� ǥ�õ�)
		todayM = (new SimpleDateFormat("mm").format(date));	// ���� ���� SimpleDateFormat�� �̿��Ͽ� �����´�.(�� �ڸ��� ǥ�õ�)
		todayS = (new SimpleDateFormat("ss").format(date));	// ���� ���� SimpleDateFormat�� �̿��Ͽ� �����´�.(�� �ڸ��� ǥ�õ�)
		if (Integer.parseInt(todayH) > 12) {	// ������ ����ð��� 12���� ū ���
			ampm = " PM";	// �������� String�� ���ķ� ǥ��
		} else {	// ������ ����ð��� 12���� ũ�� ���� ���
			ampm = " AM";	// �������� String�� �������� ǥ��
		}
		ampmLa = new JLabel(ampm);	// �������� ǥ�� �󺧿� ���� �������� ��Ʈ���� �ٿ� ����
		ampmLa.setFont(new Font(null, Font.BOLD, 22));	// �������� ǥ�� ���� �۾�ü ����
		ampmLa.setForeground(new Color(243, 243, 230));	// �������� ǥ�� ���� ���ڻ� ����
		ampmLa.setOpaque(true);	// �������� ǥ�� ���� ����� ���̵��� ����
		ampmLa.setBackground(new Color(210, 210, 164));	// �������� ǥ�� ���� ���� ����
		iconAlarm = new JLabel(new ImageIcon("images/icalarm.png"));	// �˶��������� �̹����� ������ �󺧷� ����
		iconAlarm.addMouseListener(new MouseAdapter() {	// �󺧿� ���콺 ������ ����
			public void mouseClicked(MouseEvent arg0) {	// ���콺 Ŭ���� �̺�Ʈ
				if (setAlarm.isVisible()) {	// �˶� ���� â�� ���̴� ���
					setAlarm.setVisible(false);	// �˶� ����â�� ������ �ʵ��� ����
					repaint();	// �ٽ� �׷��ش�
				} else {	// �˶� ���� â�� ������ �ʴ� ���
					setAlarm.setVisible(true);	// �˶� ���� â�� ���̵��� ����
				}
			}	// ���콺 Ŭ���� �̺�Ʈ�� ��
		});
		iconMini = new JLabel(new ImageIcon("images/icmini.png"));	// �ּ�ȭ�������� �̹����� ������ �󺧷� ����
		iconMini.addMouseListener(new MouseAdapter() {	// �󺧿� ���콺 ������ ����
			public void mouseClicked(MouseEvent arg0) {	// ���콺 Ŭ���� �̺�Ʈ
				setState(JFrame.ICONIFIED);	// â�� ���¸� �ּ�ȭ ���·� ����
			}	// ���콺 Ŭ���� �̺�Ʈ�� ��
		});
		iconClose = new JLabel(new ImageIcon("images/icclose.png"));	// ����������� �̹����� ������ �󺧷� ����
		iconClose.addMouseListener(new MouseAdapter() {	// �󺧿� ���콺 ������ ����
			public void mouseClicked(MouseEvent arg0) {	// ���콺 Ŭ���� �̺�Ʈ
				System.exit(0);	// �ý����� ���� ��Ų��
			}	// ���콺 Ŭ���� �̺�Ʈ�� ��
		});
		nowTime = new JLabel(todayH + " : " + todayM + " : " + todayS);	// ����ð��� ǥ���ϴ� �� ����
		nowTime.setFont(new Font(null, Font.BOLD, 22));	// ����ð��� ǥ���ϴ� ���� ��Ʈ ����
		nowTime.setForeground(new Color(180, 180, 134));	// ����ð��� ǥ���ϴ� ���� ���ڻ� ����
		nowTimeSec = Integer.parseInt(todayH) * 3600 + Integer.parseInt(todayM)
				* 60 + Integer.parseInt(todayS);	// ����ð��� �ʷ� ���
		ImageIcon clockP = new ImageIcon("images/clockPane.jpg");	// �ð��� �׸��� �����´�
		ClockPane = new JLabel(clockP);	// �ð��� �׸��� ���Ե� �� ����
		biH = ImageIO.read(new File("images/clockH.png"));	// �ð����� ��ħ �̹��� ����
		JPanel biHH = new JPanel() {	// ��ħ �̹����� �׸��� �ǳ� ����
			public Dimension getPreferredSize() {	// ��ħ �̹����� ũ���� �ǳ� ũ�⸦ ����
				return new Dimension(biH.getWidth(), biH.getHeight());
			}
			protected void paintComponent(Graphics g) {	// �ǳ��� �׷���
				super.paintComponent(g);	// �ǳڿ� ������ ������Ʈ�� �׷���
				Graphics2D g2 = (Graphics2D) g;
				g2.rotate(
						((Math.PI / 6) * Integer.parseInt(todayH) + ((Math.PI / 30) * Integer
								.parseInt(todayM)) / 12), biH.getWidth() / 2,
						biH.getHeight() / 2);	// rotate()�Լ��� �̿��Ͽ� �׸��� ȸ����Ų��
				g2.drawImage(biH, 0, 0, null);	// ȸ����Ų �׸��� �ǳڿ� �׷��ش�.
			}
		};
		biHH.addMouseListener(dwl); // ��ħ �̹����� ���콺 �巡�� ������ �ޱ�
		biHH.addMouseMotionListener(dwl); // ��ħ �̹����� ���콺 �巡�� ������ �ޱ�
		biHH.setFocusable(false);	// ��ħ �̹����� ��Ŀ���� ����
		biHH.setOpaque(false);		// ��ħ �̹����� ����� ����
		biM = ImageIO.read(new File("images/clockM.png"));	// �ð����� ��ħ �̹��� ����
		JPanel biMM = new JPanel() {	// ��ħ �̹����� �׸��� �ǳ� ����
			public Dimension getPreferredSize() {	// ��ħ �̹����� ũ���� �ǳ� ũ�⸦ ����
				return new Dimension(biM.getWidth(), biM.getHeight());
			}
			protected void paintComponent(Graphics g) {	// �ǳ��� �׷���
				super.paintComponent(g);	// �ǳڿ� ������ ������Ʈ�� �׷���
				Graphics2D g2 = (Graphics2D) g;
				g2.rotate((Math.PI / 30) * Integer.parseInt(todayM),
						biM.getWidth() / 2, biM.getHeight() / 2);	// rotate()�Լ��� �̿��Ͽ� �׸��� ȸ����Ų��
				g2.drawImage(biM, 0, 0, null);	// ȸ����Ų �׸��� �ǳڿ� �׷��ش�.
			}
		};
		biMM.setOpaque(false);	//��ħ �̹����� ����� ����
		biS = ImageIO.read(new File("images/clockT.png"));	// �ð����� ��ħ �̹��� ����
		JPanel biSS = new JPanel() {	// ��ħ �̹����� �׸��� �ǳ� ����
			public Dimension getPreferredSize() {	// ��ħ �̹����� ũ���� �ǳ� ũ�⸦ ����
				return new Dimension(biS.getWidth(), biS.getHeight());
			}
			protected void paintComponent(Graphics g) {	// �ǳ��� �׷���
				super.paintComponent(g);	// �ǳڿ� ������ ������Ʈ�� �׷���
				Graphics2D g2 = (Graphics2D) g;
				g2.rotate((Math.PI / 30) * Integer.parseInt(todayS),
						biS.getWidth() / 2, biS.getHeight() / 2);	// rotate()�Լ��� �̿��Ͽ� �׸��� ȸ����Ų��
				g2.drawImage(biS, 0, 0, null);	// ȸ����Ų �׸��� �ǳڿ� �׷��ش�.
			}
		};
		biSS.setOpaque(false);	// ��ħ �̹����� ����� ����
	//**** �˶� ���� â
		setAlarm = new JPanel(null);	// �˶� ���� â�� �ǳ� ����
		setAlarm.setOpaque(true);	// �˶� ���� â�� ����� ���̵��� ����
		setAlarm.setBackground(new Color(146, 208, 80));	// �˶� ���� â�� ���� ����
		setAlarm.setPreferredSize(new Dimension(250, 110));	// �˶� ���� â�� ũ�⸦ ����
		JLabel setHHLa = new JLabel("HH");	// HH��� ���� �� ����
		setHHLa.setForeground(Color.WHITE);	// ���� ���ڻ� ����
		JLabel setMMLa = new JLabel("MM");	// MM��� ���� �� ����
		setMMLa.setForeground(Color.WHITE);	// ���� ���ڻ� ����
		JLabel setSSLa = new JLabel("SS");	// SS��� ���� �� ����
		setSSLa.setForeground(Color.WHITE);	// ���� ���ڻ� ����
		setAmPm = new JLabel("AM");		// AM�̶�� ���� �� ����, �˶��� �������ĸ� ǥ�� 
		setAmPm.setForeground(Color.WHITE);	// �������� ���� ���ڻ� ����
		setAmPm.setBorder(new LineBorder(Color.WHITE, 2));	// �������� ���� �׵θ� ����
		JLabel remainText = new JLabel("Remain Time");		// Remain Time�̶�� ���� �� ����
		remainText.setFont(new Font(null, Font.BOLD, 12));	// ���� ���� ��� ����
		remainText.setForeground(Color.WHITE);	// ���� ���ڻ� ����
		setTimeBtn = new JLabel(" SET");	// �˶� ���� ��ư ��Ȱ�� �� ����
		setTimeBtn.setOpaque(true);	// �˶� ���� ��ư ��Ȱ�� ���� ����� ���̵��� ����
		setTimeBtn.setBackground(Color.WHITE);	// �˶� ���� ��ư ��Ȱ�� ���� ���� ����
		setTimeBtn.setFont(new Font(null, Font.BOLD, 16));	// �˶� ���� ��ư ��Ȱ�� ���� ���� ��� ����
		setTimeBtn.setForeground(new Color(146, 208, 80));	// �˶� ���� ��ư ��Ȱ�� ���� ���ڻ� ����
		setHHTx = new JTextField();	// �˶� �ð��� �ð� �ؽ�Ʈ�ʵ� ����
		setHHTx.setBorder(null);	// �˶� �ð��� �ð� �ؽ�Ʈ�ʵ��� �׵θ� ����
		setHHTx.setFont(new Font(null, Font.BOLD, 20));	// �˶� �ð��� �ð� �ؽ�Ʈ�ʵ��� ���ڸ�� ����
		setHHTx.setForeground(new Color(49, 89, 179));	// �˶� �ð��� �ð� �ؽ�Ʈ�ʵ��� ���ڻ� ����
		setMMTx = new JTextField();	// �˶� �ð��� �� �ؽ�Ʈ�ʵ� ����
		setMMTx.setBorder(null);	// �˶� �ð��� �� �ؽ�Ʈ�ʵ��� �׵θ� ����
		setMMTx.setFont(new Font(null, Font.BOLD, 20));	// �˶� �ð��� �� �ؽ�Ʈ�ʵ��� ���ڸ�� ����
		setMMTx.setForeground(new Color(188, 43, 23));	// �˶� �ð��� �� �ؽ�Ʈ�ʵ��� ���ڻ� ����
		setSSTx = new JTextField();	// �˶� �ð��� �� �ؽ�Ʈ�ʵ� ����
		setSSTx.setBorder(null);	// �˶� �ð��� �� �ؽ�Ʈ�ʵ��� �׵θ� ����
		setSSTx.setFont(new Font(null, Font.BOLD, 20));	// �˶� �ð��� �� �ؽ�Ʈ�ʵ��� ���ڸ�� ����
		setSSTx.setForeground(Color.GRAY);	// �˶� �ð��� �� �ؽ�Ʈ�ʵ��� ���ڻ� ����
		setHHTxRe = new JTextField();	// ���� �ð��� �ð� ǥ�� �ؽ�Ʈ�ʵ� ����
		setHHTxRe.setBorder(null);	// ���� �ð��� �ð� ǥ�� �ؽ�Ʈ�ʵ��� �׵θ�����
		setHHTxRe.setFont(new Font(null, Font.BOLD, 20));	// ���� �ð��� �ð� ǥ�� �ؽ�Ʈ�ʵ��� ���ڸ�� ����
		setHHTxRe.setEditable(false);	// ���� �ð��� �ð� ǥ�� �ؽ�Ʈ�ʵ��� ���� �Ұ��� �ϵ��� ����
		setHHTxRe.setForeground(new Color(0, 124, 7));	// ���� �ð��� �ð� ǥ�� �ؽ�Ʈ�ʵ��� ���ڻ� ����
		setMMTxRe = new JTextField();	// ���� �ð��� �� ǥ�� �ؽ�Ʈ�ʵ� ����
		setMMTxRe.setBorder(null);	// ���� �ð��� �� ǥ�� �ؽ�Ʈ�ʵ��� �׵θ� ����
		setMMTxRe.setFont(new Font(null, Font.BOLD, 20));	// ���� �ð��� �� ǥ�� �ؽ�Ʈ�ʵ��� ���ڸ�� ����
		setMMTxRe.setEditable(false);	// ���� �ð��� �� ǥ�� �ؽ�Ʈ�ʵ��� ������ �Ұ����ϰ� ����
		setMMTxRe.setForeground(new Color(49, 89, 179));	// ���� �ð��� �� ǥ�� �ؽ�Ʈ�ʵ��� ���ڻ� ����
		setSSTxRe = new JTextField();	// ���� �ð��� �� ǥ�� �ؽ�Ʈ�ʵ� ����
		setSSTxRe.setBorder(null);	// ���� �ð��� �� ǥ�� �ؽ�Ʈ�ʵ� �׵θ�����
		setSSTxRe.setFont(new Font(null, Font.BOLD, 20));	// ���� �ð��� �� ǥ�� �ؽ�Ʈ�ʵ� ���ڸ�� ����
		setSSTxRe.setEditable(false);	// ���� �ð��� �� ǥ�� �ؽ�Ʈ�ʵ� ������ �Ұ����ϵ��� ����
		setSSTxRe.setForeground(new Color(213, 173, 0));	// ���� �ð��� �� ǥ�� �ؽ�Ʈ�ʵ� ���ڻ� ����
		setHHLa.setBounds(20, 5, 30, 20);	// �˶� �ð� ���� ��ġ ����
		setMMLa.setBounds(70, 5, 30, 20);	// �˶� �� ���� ��ġ ����
		setSSLa.setBounds(120, 5, 30, 20);	// �˶� �� ���� ��ġ ����
		setHHTx.setBounds(10, 25, 40, 30);	// �˶� �ð� �ؽ�Ʈ �ʵ��� ��ġ ����
		setMMTx.setBounds(60, 25, 40, 30);	// �˶� �� �ؽ�Ʈ �ʵ��� ��ġ ����
		setSSTx.setBounds(110, 25, 40, 30);	// �˶� �� �ؽ�Ʈ �ʵ��� ��ġ ����
		setAmPm.setBounds(160, 25, 25, 30);		// �˶� �������� ���� ��ġ ����
		setTimeBtn.setBounds(195, 25, 40, 30);	// �˶� ��ư ���� ��ġ ����
		setHHTxRe.setBounds(10, 60, 40, 30);	// �����ð� �ð� �ؽ�Ʈ �ʵ��� ��ġ ����
		setMMTxRe.setBounds(60, 60, 40, 30);	// �����ð� �� �ؽ�Ʈ �ʵ��� ��ġ ����
		setSSTxRe.setBounds(110, 60, 40, 30);	// �����ð� �� �ؽ�Ʈ �ʵ��� ��ġ ����
		remainText.setBounds(160, 60, 80, 30);	// �����ð� �ؽ�Ʈ �ʵ��� ��ġ ����
		setAlarm.add(setHHLa);	// �˶� ���� â�� �ð� �� ����
		setAlarm.add(setMMLa);	// �˶� ���� â�� �� �� ����
		setAlarm.add(setSSLa);	// �˶� ���� â�� �� �� ����
		setAlarm.add(setHHTx);	// �˶� ���� â�� �����ð� ĭ ����
		setAlarm.add(setMMTx);	// �˶� ���� â�� ���� �� ĭ ����
		setAlarm.add(setSSTx);	// �˶� ���� â�� ���� �� ĭ ����
		setAlarm.add(setAmPm);	// �˶� ���� â�� ���� �������� ����
		setAlarm.add(setTimeBtn);	// �˶� ���� â�� ���� ��ư ����
		setAlarm.add(setHHTxRe);	// �˶� ���� â�� ���� �ð� ĭ ����
		setAlarm.add(setMMTxRe);	// �˶� ���� â�� ���� �� ĭ ����
		setAlarm.add(setSSTxRe);	// �˶� ���� â�� ���� �� ĭ ����
		setAlarm.add(remainText);	// �˶� ���� â�� �����ð� ���� ����
		setAmPm.addMouseListener(new MouseAdapter() {	//	���콺 ������ ����
			public void mouseClicked(MouseEvent e) {	// ���콺 Ŭ����
				if (!waitAlarm) {	// �˶� ���� ����ġ�� ���� �̸�
					if (setAmPm.getText().equals("AM"))	// �˶� �������İ� AM�϶�
						setAmPm.setText("PM");	// PM���� ����
					else	// �ƴ� ���
						setAmPm.setText("AM");	// AM���� ����
				}	
			}	// ���콺 Ŭ������ ��
		});
		setTimeBtn.addMouseListener(new MouseAdapter() {	// ���콺 ������ ����
			public void mouseClicked(MouseEvent e) {	// ���콺 Ŭ�� �̺�Ʈ
				if (!setHHTx.getText().isEmpty()	// �˶� ���� �ð��� �ؽ�Ʈ�ʵ尡 ������� ���� ���
						&& !setMMTx.getText().isEmpty()	//�׸��� �˶� ���� ���� �ؽ�Ʈ�ʵ尡 ������� ���� ���
						&& !setSSTx.getText().isEmpty()) {	// �˶� ���� ���� �ؽ�Ʈ�ʵ尡 ������� ���� ���
					if (Integer.parseInt(setMMTx.getText()) < 60	
							&& Integer.parseInt(setSSTx.getText()) < 60
							&& Integer.parseInt(setHHTx.getText()) < 13) {	// �ð��� 13�����̰� ��, �ʰ� 60���� �϶�
						int hour = Integer.parseInt(setHHTx.getText()) * 3600;	// �����ð��� �ð��� �ʷ� ���
						if (setAmPm.getText().equals("PM")) {	// ���� �ð��� �����̸�
							hour += 12 * 3600;	// 12�ð��� �����ش�
						}
						setTimeSec = hour + Integer.parseInt(setMMTx.getText())
								* 60 + Integer.parseInt(setSSTx.getText());	// �˶� ���� �ð��� �ʷ� ���
						setHHTx.setEditable(false);	// �˶��Է� â ���� �Ұ��� �ϵ��� ����
						setMMTx.setEditable(false);	// �˶��Է� â ���� �Ұ��� �ϵ��� ����
						setSSTx.setEditable(false);	// �˶��Է� â ���� �Ұ��� �ϵ��� ����
						waitAlarm = true;	// �˶� ���� ����ġ�� ��
					} else {	// �ð��� 13�̻��̰� ��, �ʰ� 60�ʰ� �϶�
						JOptionPane.showMessageDialog(null,
								"�߸��� ���� �Դϴ�.(�ð� 0~12:�� 0~59:�� 0~59)",	
								"Message", JOptionPane.ERROR_MESSAGE);	// �����̾�α׸� �ٿ�
						setHHTx.setText("");	// �˶� ���� ĭ�� �ʱ�ȭ
						setMMTx.setText("");	// �˶� ���� ĭ�� �ʱ�ȭ
						setSSTx.setText("");	// �˶� ���� ĭ�� �ʱ�ȭ
					}
				}
			}	// ���콺 Ŭ�� �̺�Ʈ�� ��
		});
		setAlarm.setVisible(false);	// �˶� ���� â�� ���߱�
	//**** �˶� ���� â�� ��
		setAlarm.setBounds(70, 320, 250, 110);	// �˶� ���� â�� ��ġ ����
		iconAlarm.setBounds(230, 45, 36, 36);	// �˶��������� ��ġ ����
		iconMini.setBounds(270, 45, 36, 36);	// �ּ�ȭ�������� ��ġ ����
		iconClose.setBounds(310, 45, 36, 36);	// ����������� ��ġ ����
		ampmLa.setBounds(230, 180, 45, 30);	// �˶� ���� ��ġ ����
		biHH.setBounds(50, 50, 288, 288);	// ��ħ�� ��ġ ����
		biMM.setBounds(50, 50, 288, 288);	// ��ħ�� ��ġ ����
		biSS.setBounds(50, 50, 288, 288);	// ��ħ�� ��ġ ����
		ClockPane.setBounds(50, 50, 288, 288);	// �ð����� ��ġ ����
		nowTime.setBounds(140, 220, 200, 60);	// ����ð� ���� ��ġ ����
		add(setAlarm);	// �ֻ��� �����̳ʿ� ����
		add(iconAlarm);	// �ֻ��� �����̳ʿ� ����
		add(iconMini);	// �ֻ��� �����̳ʿ� ����
		add(iconClose);	// �ֻ��� �����̳ʿ� ����
		add(biHH);	// �ֻ��� �����̳ʿ� ����
		add(biMM);	// �ֻ��� �����̳ʿ� ����
		add(biSS);	// �ֻ��� �����̳ʿ� ����
		add(ampmLa);	// �ֻ��� �����̳ʿ� ����
		add(nowTime);	// �ֻ��� �����̳ʿ� ����
		add(ClockPane);	// �ֻ��� �����̳ʿ� ����
		if (timeTh == null) {	// Ÿ�� �����尡 null�̸�
			timeTh = new Thread(this);	// �����带 ����
			timeTh.start();	// �����带 ����
		}
	}	// �ð� �� ������Ʈ���� ���̰� GUI�� �ʱ�ȭ�ϴ� �Լ��� ��
	public void run() {	// ��� ���� Runnable�� �޼ҵ�. 1�ʸ��� ���ŵǴ� ������
		while (true) {	// ���� �ݺ�
			Calendar calendar = Calendar.getInstance();	// ���� �ð��� �����´�
			java.util.Date date = calendar.getTime();	// date��ü�� ���� �ð��� ����
			todayH = (new SimpleDateFormat("HH").format(date));	// ����ð��� ���ڸ����� �ð����� ǥ��
			todayM = (new SimpleDateFormat("mm").format(date));	// ����ð��� ���ڸ����� ������ ǥ��
			todayS = (new SimpleDateFormat("ss").format(date));	// ����ð��� ���ڸ����� �ʷ� ǥ��
			if (Integer.parseInt(todayH) > 12) {	// ���� �ð��� 12���� ũ�ٸ�
				ampm = " PM";	// �������� ������ ���ķ� ǥ��
			} else {	// �ݴ��� ���
				ampm = " AM";	// �������� ǥ��
			}
			ampmLa.setText(ampm);	// �󺧿� ���ڸ� ����
			nowTime.setText(todayH + " : " + todayM + " : " + todayS);	// ����ð� ǥ��
			repaint();	// �ٽ� �׷��ֱ�
			try {
				Thread.sleep(1000);	// 1�ʰ� �����尡 ����(����)
			} catch (InterruptedException e) {	// ���� ���ܰ� �����
				e.printStackTrace();	// ����Ʈ
			}
			if (waitAlarm) {	// �˶� ���� ����ġ�� �� �̶��
				nowTimeSec = Integer.parseInt(todayH) * 3600
						+ Integer.parseInt(todayM) * 60
						+ Integer.parseInt(todayS);	// ����ð��� �ʷ� ���
				remainTimeSec = setTimeSec - nowTimeSec;	// �˶� ���� �ð����� ����ð��� ����
				if (remainTimeSec == 0) {	// ���� �ʰ� 0 �̶��(�˶�)
					waitAlarm = false;	// �˶� ���� ����ġ�� ����
					clip.play();	// ����� Ŭ�� ������ ����
					setHHTx.setEditable(true);	// �˶� ���� ĭ�� ���� �� �� �ֵ��� ����
					setMMTx.setEditable(true);	// �˶� ���� ĭ�� ���� �� �� �ֵ��� ����
					setSSTx.setEditable(true);	// �˶� ���� ĭ�� ���� �� �� �ֵ��� ����
					setHHTx.setText("");	// �˶� ���� ĭ�� �ʱ�ȭ
					setMMTx.setText("");	// �˶� ���� ĭ�� �ʱ�ȭ
					setSSTx.setText("");	// �˶� ���� ĭ�� �ʱ�ȭ
					setHHTxRe.setText("");	// ���� �ð� ĭ�� �ʱ�ȭ
					setMMTxRe.setText("");	// ���� �ð� ĭ�� �ʱ�ȭ
					setSSTxRe.setText("");	// ���� �ð� ĭ�� �ʱ�ȭ
					setState(JFrame.NORMAL);	// ������ â�� ���(�ּ�ȭ �� ���)
					JOptionPane.showMessageDialog(null, "������! ������!", "Message",
							JOptionPane.INFORMATION_MESSAGE);	// �˶� ���̾�α׸� �ٿ�
					clip.stop();	// ������ ����
				} else if (remainTimeSec < 0) {	// ���� �ð��� 0���� ���� ���(����� �����ε� �˶��� �������� �� ���)
					remainTimeSec += 24 * 3600;	// ���� �ð��� 24�ð��� �����ش�(+�Ϸ�)
					int remainHH = (remainTimeSec / 3600);	// ���� �ð��� �ð��� ���
					int remainMM = ((remainTimeSec / 60) - ((remainTimeSec / 3600) * 60));	// ���� �ð��� ���� ���
					int remainSS = (remainTimeSec % 60);	// ���� �ð��� �ʸ� ���
					if (remainHH < 10)	// ���� �ð��� �ð��� 1�ڸ��� �� ��� 
						setHHTxRe.setText("0" + remainHH);	// �տ� 0�� ǥ��
					else	// �ƴ� ���
						setHHTxRe.setText("" + remainHH);	// �״�� ǥ��
					if (remainMM < 10)	// ���� �ð��� ���� 1�ڸ��� �� ��� 
						setMMTxRe.setText("0" + remainMM);	// �տ� 0�� ǥ��
					else	// �ƴ� ���
						setMMTxRe.setText("" + remainMM);	// �״�� ǥ��
					if (remainSS < 10)	// ���� �ð��� �ʰ� 1�ڸ��� �� ��� 
						setSSTxRe.setText("0" + remainSS);	// �տ� 0�� ǥ��
					else	// �ƴ� ���
						setSSTxRe.setText("" + remainSS);	// �״�� ǥ��
				} else if (remainTimeSec > 0) {	// ���� �ð��� �ð���0 ���� Ŭ ���
					int remainHH = (remainTimeSec / 3600);
					int remainMM = ((remainTimeSec / 60) - ((remainTimeSec / 3600) * 60));
					int remainSS = (remainTimeSec % 60);
					if (remainHH < 10)	// ���� �ð��� �ð��� 1�ڸ��� �� ��� 
						setHHTxRe.setText("0" + remainHH);	// �տ� 0�� ǥ��
					else
						setHHTxRe.setText("" + remainHH);
					if (remainMM < 10)	// ���� �ð��� ���� 1�ڸ��� �� ��� 
						setMMTxRe.setText("0" + remainMM);	// �տ� 0�� ǥ��
					else
						setMMTxRe.setText("" + remainMM);
					if (remainSS < 10)	// ���� �ð��� �ʰ� 1�ڸ��� �� ��� 
						setSSTxRe.setText("0" + remainSS);	// �տ� 0�� ǥ��
					else
						setSSTxRe.setText("" + remainSS);
				}
			}
		}	// ���� �ݺ� ���� ��
	}	// ��� ���� Runnable�� �޼ҵ��� ��. 1�ʸ��� ���ŵǴ� �������� ��
	public static void main(String[] args) {	// ���� �Լ�
		// GraphicsDevice�� ����� ĸ���� ���̵��� �ϴ� ���� �����ϴ��� üũ
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		JFrame frame = new JFrame();	// JFrame�� �����
		frame.setUndecorated(true);		// ���� JFrame�� �׵θ��� �����Ѵ�.
		SwingUtilities.invokeLater(new Runnable() {	// �����带 �̿��Ͽ� â�� ��� ���̵��� �Ѵ�(������ ����� ����)
			public void run() {
				ClockApp gtw = new ClockApp();
				gtw.setVisible(true);	// ���� ClockApp�� ȭ�鿡 ���̱�
			}
		});
	}	// ���� �Լ��� ��
	class DragWindowListener extends MouseAdapter { // �巡�� ���콺�� ���� â�� �����̴� ������
		private final transient Point startPt = new Point(); // ���콺�� ��ġ�� �޴� Point
		private Window window;	// ������ ������ â
		public void mousePressed(MouseEvent me) { // ���콺�� �������� �̺�Ʈ
			if (window == null) { // window�� ����ٸ�
				Object o = me.getSource(); // ������ ��Ҹ� ������
				if (o instanceof Window) {	// ������ ��Ұ� Window�� ��ӹ޴� ��Ҷ��
					window = (Window) o; // Window�� ���¸� �ٲ�
				} else if (o instanceof JComponent) { // ������ ��Ұ� JComponent���
					window = SwingUtilities.windowForComponent(me.getComponent()); // ������Ʈ�� �پ��ִ� �����츦 �ҷ��´�
				}
			}
			startPt.setLocation(me.getPoint());	// ���콺�� ��ġ�� �޴� Point�� ���� �̺�Ʈ�� �Ͼ �����ͷ� ����ġ
		}	// ���콺�� �������� �̺�Ʈ�� ��
		public void mouseDragged(MouseEvent me) { // ���콺�� �巡�� �Ҷ� �̺�Ʈ
			if (window != null) { // window�� null�� �ƴϸ�
				Point pt = new Point();	// ���ο� �����͸� ����
				pt = window.getLocation(pt);	// window��ü�� ��ġ�� �޾ƿ� �����Ϳ� �ִ´�
				int x = pt.x - startPt.x + me.getX();	// ������ x��ǥ ���
				int y = pt.y - startPt.y + me.getY();	// ������ y��ǥ ���
				window.setLocation(x, y); // ������ x, y ��ŭ �����츦 ������
			}
		}	// ���콺�� �巡�� �Ҷ� �̺�Ʈ�� ��
	}	// �巡�� ���콺�� ���� â�� �����̴� �������� ��
}
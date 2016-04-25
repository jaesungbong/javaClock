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
	int nowTimeSec = 0, setTimeSec = 0, remainTimeSec = -1;	// 현재시간, 알람시간, 남은시간을 초로 계산
	boolean waitAlarm;	// 알람 스위치. 알람이 설정된 경우 true, 알람이 꺼진 경우 false
	String todayH, todayM, todayS, ampm;	// 현재 시간의 시간, 분, 초, 오전오후를 나타낸 String
	AudioClip clip; // 알람 음악을 담는 AudioClip
	BufferedImage biH, biM, biS; // 시침, 분침, 초짐의 이미지
	DragWindowListener dwl;	// 마우스 드래그시 시계가 따라가도록 한 마우스리스너
	Thread timeTh;	// 1초마다 시계의 이미지와 알람의 남은시간을 표시하는 스레드  
	JLabel iconAlarm, iconMini, iconClose;	// 알람아이콘, 최소화아이콘, 종료아이콘 그림을 담는 라벨
	JLabel nowTime, ClockPane, ampmLa, setTimeBtn, setAmPm; // 현재시간, 시계판 그림, 현재오전오후, 알람설정버튼, 알람오전오후설정 라벨
	JPanel setAlarm;	// 알람 설정창, 알람 아이콘으로 감추기 및 보이기 하는 창
	JTextField setHHTx, setMMTx, setSSTx, setHHTxRe, setMMTxRe, setSSTxRe;	// 알람 설정시 시, 분, 초를 입력받고 남은시간을 나타내는 텍스트필드

	public ClockApp() {	// 시계 JFrame의 생성자. 컴포넌트들을 생성하고 초기화한다.
		super("아날로그 시계");	// JFrame의 타이틀
		setSize(new Dimension(400, 500));	// JFrame의 크기 가로400 세로500
		setLocationRelativeTo(null);	// 처음 창이 실행될때 중간에 위치하도록 하는 코드
		setUndecorated(true); // 윈도우 창의 테두리 제거
		setBackground(new Color(0, 0, 0, 0));	// JFrame의 배경색 지정(투명화 시키기 위하여)
		dwl = new DragWindowListener();	// 마우스드래그 리스너를 생성
		JPanel panel = new JPanel() {	// 투명한 효과를 줄 판넬을 만든다.
			protected void paintComponent(Graphics g) {
				if (g instanceof Graphics2D) {	// 그래픽 g를 가져온다. 
					final int R = 255;	// RGB 코드중 R 을 255로 설정
					final int G = 255;	// RGB 코드중 G 을 255로 설정
					final int B = 255;	// RGB 코드중 B 을 255로 설정
					Paint p = new GradientPaint(0.0f, 0.0f, new Color(R, G, B,
							0), 0.0f, getHeight(), new Color(R, G, B, 0), true);	// 그라데이션 효과가 있는 페인트 p 생성
					Graphics2D g2d = (Graphics2D) g;	// g를 Graphics2D 타입으로 형변환 시킨다
					g2d.setPaint(p);	// 페인트p로 칠해준다
					g2d.fillRect(0, 0, getWidth(), getHeight());	//페인트로 칠해진 상태의 사각형을 판넬에 꽉차게 그린다. 
				}
			}
		};	// 투명한 판넬 생성 종료
		setContentPane(panel);	// 컨텐트펜에 페인트로 칠한 판넬을 붙여준다
		setLayout(null);	// 배치 관리자를 null로 설정
		try {	
			clock();	// 시계 안 컴포넌트들을 붙이고 GUI를 초기화하는 함수
		} catch (IOException e) {	// 입출력 예외가 발생하면
			e.printStackTrace();	// 그 예외를 프린트한다.
		}
	} // 시계 JFrame의 생성자의 끝

	public void clock() throws IOException {	// 시계 안 컴포넌트들을 붙이고 GUI를 초기화하는 함수
		clip = null;	// 음악을 담을 오디오클립을 초기화한다.
		URL audioURL = getClass().getResource("ring.wav");	// 음악파일의 URL경로를 설정
		clip = Applet.newAudioClip(audioURL);	// 음악파일을 가져와 실행할 수 있도록 준비
		Calendar calendar = Calendar.getInstance();	// 현재 시간을 가져온다
		java.util.Date date = calendar.getTime();	// date객체에 현재시간을 담는다
		waitAlarm = false;	// 알람 설정 체크 스위치 오프
		todayH = (new SimpleDateFormat("HH").format(date));	// 현재 시간을 SimpleDateFormat를 이용하여 가져온다.(두 자리로 표시됨)
		todayM = (new SimpleDateFormat("mm").format(date));	// 현재 분을 SimpleDateFormat를 이용하여 가져온다.(두 자리로 표시됨)
		todayS = (new SimpleDateFormat("ss").format(date));	// 현재 초을 SimpleDateFormat를 이용하여 가져온다.(두 자리로 표시됨)
		if (Integer.parseInt(todayH) > 12) {	// 가져온 현재시간이 12보다 큰 경우
			ampm = " PM";	// 오전오후 String을 오후로 표시
		} else {	// 가져온 현재시간이 12보다 크지 않은 경우
			ampm = " AM";	// 오전오후 String을 오전으로 표시
		}
		ampmLa = new JLabel(ampm);	// 오전오후 표시 라벨에 현재 오전오후 스트링을 붙여 생성
		ampmLa.setFont(new Font(null, Font.BOLD, 22));	// 오전오후 표시 라벨의 글씨체 변경
		ampmLa.setForeground(new Color(243, 243, 230));	// 오전오후 표시 라벨의 글자색 변경
		ampmLa.setOpaque(true);	// 오전오후 표시 라벨의 배경을 보이도록 설정
		ampmLa.setBackground(new Color(210, 210, 164));	// 오전오후 표시 라벨의 배경색 설정
		iconAlarm = new JLabel(new ImageIcon("images/icalarm.png"));	// 알람아이콘의 이미지를 가져와 라벨로 생성
		iconAlarm.addMouseListener(new MouseAdapter() {	// 라벨에 마우스 리스너 부착
			public void mouseClicked(MouseEvent arg0) {	// 마우스 클릭시 이벤트
				if (setAlarm.isVisible()) {	// 알람 설정 창이 보이는 경우
					setAlarm.setVisible(false);	// 알람 설정창이 보이지 않도록 설정
					repaint();	// 다시 그려준다
				} else {	// 알람 설정 창이 보이지 않는 경우
					setAlarm.setVisible(true);	// 알람 설정 창이 보이도록 설정
				}
			}	// 마우스 클릭시 이벤트의 끝
		});
		iconMini = new JLabel(new ImageIcon("images/icmini.png"));	// 최소화아이콘의 이미지를 가져와 라벨로 생성
		iconMini.addMouseListener(new MouseAdapter() {	// 라벨에 마우스 리스너 부착
			public void mouseClicked(MouseEvent arg0) {	// 마우스 클릭시 이벤트
				setState(JFrame.ICONIFIED);	// 창의 상태를 최소화 상태로 변경
			}	// 마우스 클릭시 이벤트의 끝
		});
		iconClose = new JLabel(new ImageIcon("images/icclose.png"));	// 종료아이콘의 이미지를 가져와 라벨로 생성
		iconClose.addMouseListener(new MouseAdapter() {	// 라벨에 마우스 리스너 부착
			public void mouseClicked(MouseEvent arg0) {	// 마우스 클릭시 이벤트
				System.exit(0);	// 시스템을 종료 시킨다
			}	// 마우스 클릭시 이벤트의 끝
		});
		nowTime = new JLabel(todayH + " : " + todayM + " : " + todayS);	// 현재시간을 표시하는 라벨 생성
		nowTime.setFont(new Font(null, Font.BOLD, 22));	// 현재시간을 표시하는 라벨의 폰트 변경
		nowTime.setForeground(new Color(180, 180, 134));	// 현재시간을 표시하는 라벨의 글자색 변경
		nowTimeSec = Integer.parseInt(todayH) * 3600 + Integer.parseInt(todayM)
				* 60 + Integer.parseInt(todayS);	// 현재시간을 초로 계산
		ImageIcon clockP = new ImageIcon("images/clockPane.jpg");	// 시계판 그림을 가져온다
		ClockPane = new JLabel(clockP);	// 시계판 그림이 포함된 라벨 생성
		biH = ImageIO.read(new File("images/clockH.png"));	// 시계판의 시침 이미지 생성
		JPanel biHH = new JPanel() {	// 시침 이미지를 그리는 판넬 생성
			public Dimension getPreferredSize() {	// 시침 이미지의 크기대로 판넬 크기를 설정
				return new Dimension(biH.getWidth(), biH.getHeight());
			}
			protected void paintComponent(Graphics g) {	// 판넬을 그려줌
				super.paintComponent(g);	// 판넬에 부착된 컴포넌트를 그려줌
				Graphics2D g2 = (Graphics2D) g;
				g2.rotate(
						((Math.PI / 6) * Integer.parseInt(todayH) + ((Math.PI / 30) * Integer
								.parseInt(todayM)) / 12), biH.getWidth() / 2,
						biH.getHeight() / 2);	// rotate()함수를 이용하여 그림을 회전시킨다
				g2.drawImage(biH, 0, 0, null);	// 회전시킨 그림을 판넬에 그려준다.
			}
		};
		biHH.addMouseListener(dwl); // 시침 이미지에 마우스 드래그 리스너 달기
		biHH.addMouseMotionListener(dwl); // 시침 이미지에 마우스 드래그 리스너 달기
		biHH.setFocusable(false);	// 시침 이미지의 포커스를 제거
		biHH.setOpaque(false);		// 시침 이미지의 배경을 제거
		biM = ImageIO.read(new File("images/clockM.png"));	// 시계판의 분침 이미지 생성
		JPanel biMM = new JPanel() {	// 분침 이미지를 그리는 판넬 생성
			public Dimension getPreferredSize() {	// 분침 이미지의 크기대로 판넬 크기를 설정
				return new Dimension(biM.getWidth(), biM.getHeight());
			}
			protected void paintComponent(Graphics g) {	// 판넬을 그려줌
				super.paintComponent(g);	// 판넬에 부착된 컴포넌트를 그려줌
				Graphics2D g2 = (Graphics2D) g;
				g2.rotate((Math.PI / 30) * Integer.parseInt(todayM),
						biM.getWidth() / 2, biM.getHeight() / 2);	// rotate()함수를 이용하여 그림을 회전시킨다
				g2.drawImage(biM, 0, 0, null);	// 회전시킨 그림을 판넬에 그려준다.
			}
		};
		biMM.setOpaque(false);	//분침 이미지의 배경을 제거
		biS = ImageIO.read(new File("images/clockT.png"));	// 시계판의 초침 이미지 생성
		JPanel biSS = new JPanel() {	// 초침 이미지를 그리는 판넬 생성
			public Dimension getPreferredSize() {	// 초침 이미지의 크기대로 판넬 크기를 설정
				return new Dimension(biS.getWidth(), biS.getHeight());
			}
			protected void paintComponent(Graphics g) {	// 판넬을 그려줌
				super.paintComponent(g);	// 판넬에 부착된 컴포넌트를 그려줌
				Graphics2D g2 = (Graphics2D) g;
				g2.rotate((Math.PI / 30) * Integer.parseInt(todayS),
						biS.getWidth() / 2, biS.getHeight() / 2);	// rotate()함수를 이용하여 그림을 회전시킨다
				g2.drawImage(biS, 0, 0, null);	// 회전시킨 그림을 판넬에 그려준다.
			}
		};
		biSS.setOpaque(false);	// 초침 이미지의 배경을 제거
	//**** 알람 설정 창
		setAlarm = new JPanel(null);	// 알람 설정 창의 판넬 생성
		setAlarm.setOpaque(true);	// 알람 설정 창의 배경을 보이도록 설정
		setAlarm.setBackground(new Color(146, 208, 80));	// 알람 설정 창의 배경색 설정
		setAlarm.setPreferredSize(new Dimension(250, 110));	// 알람 설정 창의 크기를 지정
		JLabel setHHLa = new JLabel("HH");	// HH라고 적힌 라벨 생성
		setHHLa.setForeground(Color.WHITE);	// 라벨의 글자색 설정
		JLabel setMMLa = new JLabel("MM");	// MM라고 적힌 라벨 생성
		setMMLa.setForeground(Color.WHITE);	// 라벨의 글자색 설정
		JLabel setSSLa = new JLabel("SS");	// SS라고 적힌 라벨 생성
		setSSLa.setForeground(Color.WHITE);	// 라벨의 글자색 설정
		setAmPm = new JLabel("AM");		// AM이라고 적힌 라벨 생성, 알람의 오전오후를 표시 
		setAmPm.setForeground(Color.WHITE);	// 오전오후 라벨의 글자색 설정
		setAmPm.setBorder(new LineBorder(Color.WHITE, 2));	// 오전오후 라벨의 테두리 설정
		JLabel remainText = new JLabel("Remain Time");		// Remain Time이라고 적힌 라벨 생성
		remainText.setFont(new Font(null, Font.BOLD, 12));	// 라벨의 글자 모양 설정
		remainText.setForeground(Color.WHITE);	// 라벨의 글자색 설정
		setTimeBtn = new JLabel(" SET");	// 알람 설정 버튼 역활의 라벨 생성
		setTimeBtn.setOpaque(true);	// 알람 설정 버튼 역활의 라벨의 배경을 보이도록 설정
		setTimeBtn.setBackground(Color.WHITE);	// 알람 설정 버튼 역활의 라벨의 배경색 설정
		setTimeBtn.setFont(new Font(null, Font.BOLD, 16));	// 알람 설정 버튼 역활의 라벨의 글자 모양 설정
		setTimeBtn.setForeground(new Color(146, 208, 80));	// 알람 설정 버튼 역활의 라벨의 글자색 설정
		setHHTx = new JTextField();	// 알람 시간의 시각 텍스트필드 생성
		setHHTx.setBorder(null);	// 알람 시간의 시각 텍스트필드의 테두리 제거
		setHHTx.setFont(new Font(null, Font.BOLD, 20));	// 알람 시간의 시각 텍스트필드의 글자모양 설정
		setHHTx.setForeground(new Color(49, 89, 179));	// 알람 시간의 시각 텍스트필드의 글자색 설정
		setMMTx = new JTextField();	// 알람 시간의 분 텍스트필드 생성
		setMMTx.setBorder(null);	// 알람 시간의 분 텍스트필드의 테두리 제거
		setMMTx.setFont(new Font(null, Font.BOLD, 20));	// 알람 시간의 분 텍스트필드의 글자모양 설정
		setMMTx.setForeground(new Color(188, 43, 23));	// 알람 시간의 분 텍스트필드의 글자색 설정
		setSSTx = new JTextField();	// 알람 시간의 초 텍스트필드 생성
		setSSTx.setBorder(null);	// 알람 시간의 초 텍스트필드의 테두리 제거
		setSSTx.setFont(new Font(null, Font.BOLD, 20));	// 알람 시간의 초 텍스트필드의 글자모양 설정
		setSSTx.setForeground(Color.GRAY);	// 알람 시간의 초 텍스트필드의 글자색 설정
		setHHTxRe = new JTextField();	// 남은 시간의 시각 표시 텍스트필드 생성
		setHHTxRe.setBorder(null);	// 남은 시간의 시각 표시 텍스트필드의 테두리제거
		setHHTxRe.setFont(new Font(null, Font.BOLD, 20));	// 남은 시간의 시각 표시 텍스트필드의 글자모양 설정
		setHHTxRe.setEditable(false);	// 남은 시간의 시각 표시 텍스트필드의 변경 불가능 하도록 설정
		setHHTxRe.setForeground(new Color(0, 124, 7));	// 남은 시간의 시각 표시 텍스트필드의 글자색 설정
		setMMTxRe = new JTextField();	// 남은 시간의 분 표시 텍스트필드 생성
		setMMTxRe.setBorder(null);	// 남은 시간의 분 표시 텍스트필드의 테두리 제거
		setMMTxRe.setFont(new Font(null, Font.BOLD, 20));	// 남은 시간의 분 표시 텍스트필드의 글자모양 설정
		setMMTxRe.setEditable(false);	// 남은 시간의 분 표시 텍스트필드의 변경을 불가능하게 설정
		setMMTxRe.setForeground(new Color(49, 89, 179));	// 남은 시간의 분 표시 텍스트필드의 글자색 설정
		setSSTxRe = new JTextField();	// 남은 시간의 초 표시 텍스트필드 생성
		setSSTxRe.setBorder(null);	// 남은 시간의 초 표시 텍스트필드 테두리제거
		setSSTxRe.setFont(new Font(null, Font.BOLD, 20));	// 남은 시간의 초 표시 텍스트필드 글자모양 설정
		setSSTxRe.setEditable(false);	// 남은 시간의 초 표시 텍스트필드 변경이 불가능하도록 설정
		setSSTxRe.setForeground(new Color(213, 173, 0));	// 남은 시간의 초 표시 텍스트필드 글자색 설정
		setHHLa.setBounds(20, 5, 30, 20);	// 알람 시간 라벨의 위치 지정
		setMMLa.setBounds(70, 5, 30, 20);	// 알람 분 라벨의 위치 지정
		setSSLa.setBounds(120, 5, 30, 20);	// 알람 초 라벨의 위치 지정
		setHHTx.setBounds(10, 25, 40, 30);	// 알람 시간 텍스트 필드의 위치 지정
		setMMTx.setBounds(60, 25, 40, 30);	// 알람 분 텍스트 필드의 위치 지정
		setSSTx.setBounds(110, 25, 40, 30);	// 알람 초 텍스트 필드의 위치 지정
		setAmPm.setBounds(160, 25, 25, 30);		// 알람 오전오후 라벨의 위치 지정
		setTimeBtn.setBounds(195, 25, 40, 30);	// 알람 버튼 라벨의 위치 지정
		setHHTxRe.setBounds(10, 60, 40, 30);	// 남은시간 시각 텍스트 필드의 위치 지정
		setMMTxRe.setBounds(60, 60, 40, 30);	// 남은시간 분 텍스트 필드의 위치 지정
		setSSTxRe.setBounds(110, 60, 40, 30);	// 남은시간 초 텍스트 필드의 위치 지정
		remainText.setBounds(160, 60, 80, 30);	// 남은시간 텍스트 필드의 위치 지정
		setAlarm.add(setHHLa);	// 알람 설정 창에 시간 라벨 부착
		setAlarm.add(setMMLa);	// 알람 설정 창에 분 라벨 부착
		setAlarm.add(setSSLa);	// 알람 설정 창에 초 라벨 부착
		setAlarm.add(setHHTx);	// 알람 설정 창에 설정시간 칸 부착
		setAlarm.add(setMMTx);	// 알람 설정 창에 설정 분 칸 부착
		setAlarm.add(setSSTx);	// 알람 설정 창에 설정 초 칸 부착
		setAlarm.add(setAmPm);	// 알람 설정 창에 설정 오전오후 부착
		setAlarm.add(setTimeBtn);	// 알람 설정 창에 설정 버튼 부착
		setAlarm.add(setHHTxRe);	// 알람 설정 창에 설정 시간 칸 부착
		setAlarm.add(setMMTxRe);	// 알람 설정 창에 설정 분 칸 부착
		setAlarm.add(setSSTxRe);	// 알람 설정 창에 설정 초 칸 부착
		setAlarm.add(remainText);	// 알람 설정 창에 남은시간 글자 부착
		setAmPm.addMouseListener(new MouseAdapter() {	//	마우스 리스너 부착
			public void mouseClicked(MouseEvent e) {	// 마우스 클릭시
				if (!waitAlarm) {	// 알람 설정 스위치가 오프 이면
					if (setAmPm.getText().equals("AM"))	// 알람 오전오후가 AM일때
						setAmPm.setText("PM");	// PM으로 변경
					else	// 아닌 경우
						setAmPm.setText("AM");	// AM으로 변경
				}	
			}	// 마우스 클릭시의 끝
		});
		setTimeBtn.addMouseListener(new MouseAdapter() {	// 마우스 리스너 부착
			public void mouseClicked(MouseEvent e) {	// 마우스 클릭 이벤트
				if (!setHHTx.getText().isEmpty()	// 알람 설정 시각의 텍스트필드가 비어있지 않은 경우
						&& !setMMTx.getText().isEmpty()	//그리고 알람 설정 분의 텍스트필드가 비어있지 않은 경우
						&& !setSSTx.getText().isEmpty()) {	// 알람 설정 초의 텍스트필드가 비어있지 않은 경우
					if (Integer.parseInt(setMMTx.getText()) < 60	
							&& Integer.parseInt(setSSTx.getText()) < 60
							&& Integer.parseInt(setHHTx.getText()) < 13) {	// 시간시 13이하이고 분, 초가 60이하 일때
						int hour = Integer.parseInt(setHHTx.getText()) * 3600;	// 설정시간의 시간을 초로 계산
						if (setAmPm.getText().equals("PM")) {	// 설정 시각이 오후이면
							hour += 12 * 3600;	// 12시간을 더해준다
						}
						setTimeSec = hour + Integer.parseInt(setMMTx.getText())
								* 60 + Integer.parseInt(setSSTx.getText());	// 알람 설정 시간을 초로 계산
						setHHTx.setEditable(false);	// 알람입력 창 변경 불가능 하도록 설정
						setMMTx.setEditable(false);	// 알람입력 창 변경 불가능 하도록 설정
						setSSTx.setEditable(false);	// 알람입력 창 변경 불가능 하도록 설정
						waitAlarm = true;	// 알람 설정 스위치를 온
					} else {	// 시간시 13이살이고 분, 초가 60초과 일때
						JOptionPane.showMessageDialog(null,
								"잘못된 포맷 입니다.(시간 0~12:분 0~59:초 0~59)",	
								"Message", JOptionPane.ERROR_MESSAGE);	// 경고다이얼로그를 뛰움
						setHHTx.setText("");	// 알람 설정 칸을 초기화
						setMMTx.setText("");	// 알람 설정 칸을 초기화
						setSSTx.setText("");	// 알람 설정 칸을 초기화
					}
				}
			}	// 마우스 클릭 이벤트의 끝
		});
		setAlarm.setVisible(false);	// 알람 설정 창을 감추기
	//**** 알람 설정 창의 끝
		setAlarm.setBounds(70, 320, 250, 110);	// 알람 설정 창의 위치 지정
		iconAlarm.setBounds(230, 45, 36, 36);	// 알람아이콘의 위치 지정
		iconMini.setBounds(270, 45, 36, 36);	// 최소화아이콘의 위치 지정
		iconClose.setBounds(310, 45, 36, 36);	// 종료아이콘의 위치 지정
		ampmLa.setBounds(230, 180, 45, 30);	// 알람 라벨의 위치 지정
		biHH.setBounds(50, 50, 288, 288);	// 시침의 위치 지정
		biMM.setBounds(50, 50, 288, 288);	// 분침의 위치 지정
		biSS.setBounds(50, 50, 288, 288);	// 초침의 위치 지정
		ClockPane.setBounds(50, 50, 288, 288);	// 시계판의 위치 지정
		nowTime.setBounds(140, 220, 200, 60);	// 현재시간 라벨의 위치 지정
		add(setAlarm);	// 최상위 컨테이너에 부착
		add(iconAlarm);	// 최상위 컨테이너에 부착
		add(iconMini);	// 최상위 컨테이너에 부착
		add(iconClose);	// 최상위 컨테이너에 부착
		add(biHH);	// 최상위 컨테이너에 부착
		add(biMM);	// 최상위 컨테이너에 부착
		add(biSS);	// 최상위 컨테이너에 부착
		add(ampmLa);	// 최상위 컨테이너에 부착
		add(nowTime);	// 최상위 컨테이너에 부착
		add(ClockPane);	// 최상위 컨테이너에 부착
		if (timeTh == null) {	// 타임 쓰레드가 null이면
			timeTh = new Thread(this);	// 쓰레드를 생성
			timeTh.start();	// 쓰레드를 실행
		}
	}	// 시계 안 컴포넌트들을 붙이고 GUI를 초기화하는 함수의 끝
	public void run() {	// 상속 같은 Runnable의 메소드. 1초마다 갱신되는 쓰레드
		while (true) {	// 무한 반복
			Calendar calendar = Calendar.getInstance();	// 현재 시간을 가져온다
			java.util.Date date = calendar.getTime();	// date객체에 현재 시간을 담음
			todayH = (new SimpleDateFormat("HH").format(date));	// 현재시간을 두자리수의 시각으로 표시
			todayM = (new SimpleDateFormat("mm").format(date));	// 현재시간을 두자리수의 분으로 표시
			todayS = (new SimpleDateFormat("ss").format(date));	// 현재시간을 두자리수의 초로 표시
			if (Integer.parseInt(todayH) > 12) {	// 현재 시각이 12보다 크다면
				ampm = " PM";	// 오전오후 레벨을 오후로 표시
			} else {	// 반대의 경우
				ampm = " AM";	// 오전으로 표시
			}
			ampmLa.setText(ampm);	// 라벨에 글자를 붙임
			nowTime.setText(todayH + " : " + todayM + " : " + todayS);	// 현재시각 표시
			repaint();	// 다시 그려주기
			try {
				Thread.sleep(1000);	// 1초간 쓰레드가 잠든다(정지)
			} catch (InterruptedException e) {	// 방해 예외가 생기면
				e.printStackTrace();	// 프린트
			}
			if (waitAlarm) {	// 알람 설정 스위치가 온 이라면
				nowTimeSec = Integer.parseInt(todayH) * 3600
						+ Integer.parseInt(todayM) * 60
						+ Integer.parseInt(todayS);	// 현재시간을 초로 계산
				remainTimeSec = setTimeSec - nowTimeSec;	// 알람 설정 시간에서 현재시간을 뺀다
				if (remainTimeSec == 0) {	// 남은 초가 0 이라면(알람)
					waitAlarm = false;	// 알람 설정 스위치를 오프
					clip.play();	// 오디오 클립 음악을 시작
					setHHTx.setEditable(true);	// 알람 설정 칸을 변경 할 수 있도록 설정
					setMMTx.setEditable(true);	// 알람 설정 칸을 변경 할 수 있도록 설정
					setSSTx.setEditable(true);	// 알람 설정 칸을 변경 할 수 있도록 설정
					setHHTx.setText("");	// 알람 설정 칸을 초기화
					setMMTx.setText("");	// 알람 설정 칸을 초기화
					setSSTx.setText("");	// 알람 설정 칸을 초기화
					setHHTxRe.setText("");	// 남은 시간 칸을 초기화
					setMMTxRe.setText("");	// 남은 시간 칸을 초기화
					setSSTxRe.setText("");	// 남은 시간 칸을 초기화
					setState(JFrame.NORMAL);	// 윈도우 창을 띄움(최소화 된 경우)
					JOptionPane.showMessageDialog(null, "따르릉! 따르릉!", "Message",
							JOptionPane.INFORMATION_MESSAGE);	// 알람 다이얼로그를 뛰움
					clip.stop();	// 음악을 종료
				} else if (remainTimeSec < 0) {	// 남은 시간이 0보다 작은 경우(현재는 오후인데 알람은 오전으로 한 경우)
					remainTimeSec += 24 * 3600;	// 남은 시간에 24시간을 더해준다(+하루)
					int remainHH = (remainTimeSec / 3600);	// 남은 시간의 시각을 계산
					int remainMM = ((remainTimeSec / 60) - ((remainTimeSec / 3600) * 60));	// 남은 시간의 분을 계산
					int remainSS = (remainTimeSec % 60);	// 남은 시간의 초를 계산
					if (remainHH < 10)	// 남은 시간의 시각이 1자리수 일 경우 
						setHHTxRe.setText("0" + remainHH);	// 앞에 0을 표시
					else	// 아닌 경우
						setHHTxRe.setText("" + remainHH);	// 그대로 표시
					if (remainMM < 10)	// 남은 시간의 분이 1자리수 일 경우 
						setMMTxRe.setText("0" + remainMM);	// 앞에 0을 표시
					else	// 아닌 경우
						setMMTxRe.setText("" + remainMM);	// 그대로 표시
					if (remainSS < 10)	// 남은 시간의 초가 1자리수 일 경우 
						setSSTxRe.setText("0" + remainSS);	// 앞에 0을 표시
					else	// 아닌 경우
						setSSTxRe.setText("" + remainSS);	// 그대로 표시
				} else if (remainTimeSec > 0) {	// 남은 시간의 시각이0 보다 클 경우
					int remainHH = (remainTimeSec / 3600);
					int remainMM = ((remainTimeSec / 60) - ((remainTimeSec / 3600) * 60));
					int remainSS = (remainTimeSec % 60);
					if (remainHH < 10)	// 남은 시간의 시각이 1자리수 일 경우 
						setHHTxRe.setText("0" + remainHH);	// 앞에 0을 표시
					else
						setHHTxRe.setText("" + remainHH);
					if (remainMM < 10)	// 남은 시간의 분이 1자리수 일 경우 
						setMMTxRe.setText("0" + remainMM);	// 앞에 0을 표시
					else
						setMMTxRe.setText("" + remainMM);
					if (remainSS < 10)	// 남은 시간의 초가 1자리수 일 경우 
						setSSTxRe.setText("0" + remainSS);	// 앞에 0을 표시
					else
						setSSTxRe.setText("" + remainSS);
				}
			}
		}	// 무한 반복 문의 끝
	}	// 상속 같은 Runnable의 메소드의 끝. 1초마다 갱신되는 쓰레드의 끝
	public static void main(String[] args) {	// 메인 함수
		// GraphicsDevice가 배경을 캡쳐후 보이도록 하는 것을 지원하는지 체크
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		JFrame frame = new JFrame();	// JFrame을 만든다
		frame.setUndecorated(true);		// 만든 JFrame의 테두리를 제거한다.
		SwingUtilities.invokeLater(new Runnable() {	// 쓰레드를 이용하여 창을 계속 보이도록 한다(투명한 배경을 위해)
			public void run() {
				ClockApp gtw = new ClockApp();
				gtw.setVisible(true);	// 만든 ClockApp을 화면에 보이기
			}
		});
	}	// 메인 함수의 끝
	class DragWindowListener extends MouseAdapter { // 드래그 마우스를 따라 창을 움직이는 리스너
		private final transient Point startPt = new Point(); // 마우스의 위치를 받는 Point
		private Window window;	// 움직일 윈도우 창
		public void mousePressed(MouseEvent me) { // 마우스를 눌렀을때 이벤트
			if (window == null) { // window가 비었다면
				Object o = me.getSource(); // 눌러진 요소를 가져옴
				if (o instanceof Window) {	// 눌러진 요소가 Window를 상속받는 요소라면
					window = (Window) o; // Window로 형태를 바꿈
				} else if (o instanceof JComponent) { // 눌러진 요소가 JComponent라면
					window = SwingUtilities.windowForComponent(me.getComponent()); // 컴포넌트가 붙어있는 윈도우를 불러온다
				}
			}
			startPt.setLocation(me.getPoint());	// 마우스의 위치를 받는 Point를 현재 이벤트가 일어난 포인터로 재위치
		}	// 마우스를 눌렀을때 이벤트의 끝
		public void mouseDragged(MouseEvent me) { // 마우스를 드래그 할때 이벤트
			if (window != null) { // window가 null이 아니면
				Point pt = new Point();	// 새로운 포인터를 생성
				pt = window.getLocation(pt);	// window객체의 위치를 받아와 포인터에 넣는다
				int x = pt.x - startPt.x + me.getX();	// 움직인 x좌표 계산
				int y = pt.y - startPt.y + me.getY();	// 움직인 y좌표 계산
				window.setLocation(x, y); // 움직인 x, y 만큼 윈도우를 움직임
			}
		}	// 마우스를 드래그 할때 이벤트의 끝
	}	// 드래그 마우스를 따라 창을 움직이는 리스너의 끝
}
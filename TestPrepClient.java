import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/*
 * Ava Zhang
 * Period 6
 * 
 * TestPrepClient - GUI client of TestPrepServer
 * 
 * Launches GUI displaying test questions and answer choices.
 * After the user chooses 1 answer for the question provided and submits it by pressing the button,
 *  the program will create a pop-up informing the user if their answer was correct.
 * Once there are no more questions, the submit button is disabled
 */

public class TestPrepClient extends JFrame implements ActionListener, ListSelectionListener {

	private int currentANum;
	private int numCorrect;

	private JLabel QNum;
	private JTextArea askQuestion;
	private JList<String> answerChoices;
	private DefaultListModel<String> answerModel;
	private JButton submit;
	private JLabel percentCorrect;

	private int testSize;
	private Scanner in;
	private PrintWriter out;

	private static final String SERVER_IP = "localhost"; //"10.104.7.243";   
	private static final int SERVER_PORT = 4242;

	public TestPrepClient(){

		Scanner keyboard = new Scanner(System.in);
		System.out.print("Input your name: ");
		String name =  keyboard.nextLine();

		//Finds its server and establish sockets
		try {

			Socket sock = new Socket(SERVER_IP, SERVER_PORT);
			in = new Scanner(sock.getInputStream());
			out = new PrintWriter(sock.getOutputStream());
			out.println(name); //sends the servor its name
			out.flush();

			testSize = Integer.parseInt(in.nextLine());

		}catch(IOException e) {
			e.printStackTrace();
		}

		currentANum = -1;

		setSize(500, 750);
		setTitle("AP Prep");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);

		//setting up the picture background
		PicPanel background = new PicPanel("quiz.jpg");
		background.setLayout(null);
		background.setBounds(0, 0, 500, 750);

		QNum = new JLabel("Question 1 of " +testSize);
		QNum.setFont(new Font("Serif", Font.PLAIN, 25));
		QNum.setBounds(180, 30, 175, 40);
		background.add(QNum);

		//Setting up the JTextArea asking the question
		askQuestion = new JTextArea(in.nextLine());
		askQuestion.setEditable(false);
		askQuestion.setLineWrap(true);
		askQuestion.setWrapStyleWord(true);
		askQuestion.setOpaque(true);
		askQuestion.setBounds(50, 90, 400, 200);
		askQuestion.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY),"Question"));
		background.add(askQuestion); 

		//setting up the JList displaying the answer choices 
		answerModel = new DefaultListModel<String>();
		for(int i = 0; i < 4; i++)
			answerModel.addElement(in.nextLine());

		answerChoices = new JList(answerModel);
		answerChoices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		answerChoices.addListSelectionListener(this);

		JScrollPane answerScroll = new JScrollPane(answerChoices); 
		answerScroll.setBounds(50, 330, 400, 175);
		answerScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY),"Answer"));
		background.add(answerScroll);

		//creating the JButton for submitting the answer
		submit = new JButton("Submit");
		submit.addActionListener(this);
		submit.setBounds(190, 530, 115, 35);
		background.add(submit);

		percentCorrect = new JLabel("Percentage Correct: 0.0");
		percentCorrect.setFont(new Font("Serif", Font.PLAIN, 22));
		percentCorrect.setBounds(150, 595, 230, 40);
		background.add(percentCorrect);

		add(background);

		setVisible(true);
	}

	/*
	 * PicPanel - inner class of the background picture
	 */
	class PicPanel extends JPanel{

		private BufferedImage image;
		private int w,h;
		public PicPanel(String fname){

			//reads image
			try {
				image = ImageIO.read(new File(fname));
				w = image.getWidth();
				h = image.getHeight();

			} catch (IOException ioe) {
				System.out.println("Could not read in the pic");
				System.exit(0);
			}

		}

		public Dimension getPreferredSize() {
			return new Dimension(w,h);
		}
		//draws image
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			g.drawImage(image,0,0,this);
		}
	}

	//If the user selected a different answer choice, the currentANum will reflect that choice
	public void valueChanged(ListSelectionEvent le){

		int index = answerChoices.getSelectedIndex();
		if(index != -1)
			currentANum = index;
	}

	//if the button submit is pressed, a pop-up will be displayed stating the result
	//the GUI will then update so that the next question will show if there are more questions
	//otherwise the submit button will be disabled
	public void actionPerformed(ActionEvent ae){

		if(currentANum == -1)
			return;
		
		//sends out the answer the user picked
		out.println(currentANum);
		out.flush();
		
		//receives whether or not the answer was a success
		String message = in.nextLine();
		JOptionPane.showMessageDialog(null, message);
		System.out.println(message);
		numCorrect = Integer.parseInt(in.nextLine()); //
		int currentQNum = Integer.parseInt(in.nextLine());

		//if the end of the test is reached, the submit button is disabled and the final score shown on the GUI
		if(currentQNum == testSize){

			submit.setEnabled(false);
			percentCorrect.setText("Percentage Correct: " + String.format("%.1f", (double)numCorrect/currentQNum*100));
			
			JOptionPane.showMessageDialog(null, in.nextLine());
			System.out.println("Game over.");
			return;
		}

		currentANum = -1;
		updateGUI(currentQNum);

	}

	//a helper method that updates the GUI to the next question + answer choices that it receives from server
	//also updates the percentage with the (possibly) changed numCorrect
	private void updateGUI(int currentQNum){

		//takes in new question + answer selections
		QNum.setText("Question " +(currentQNum+1)+ " of " +testSize);
		askQuestion.setText(in.nextLine());

		answerModel.removeAllElements();
		for(int i = 0; i < 4; i++)
			answerModel.addElement(in.nextLine());

		percentCorrect.setText("Percentage Correct: " + String.format("%.1f", (double)numCorrect/currentQNum*100));
	}

	public static void main(String[] args) {

		new TestPrepClient();
	}

}

import java.io.*;
import java.net.*;
import java.util.*;

/*
 * Ava Zhang
 * Period 6
 * 
 * TestPrepServer - server
 * 
 * Maintains the file of all test questions.  Runs on port 4242 and accepts clients forever.  
 * Whenever a client connects, the server will spawn a new thread to interact with them.
 * Maintains the highest score + person with the highest score.
 */

public class TestPrepServer {

	private AllQuestions test;
	private String highestPlayer;
	private int numWinners;
	private int highestScore;

	public TestPrepServer(){

		test = new AllQuestions("questions.txt");

		if(test.size() == 0){
			System.out.println("There are no Questions in this text");
			System.exit(-1);
		}

		highestScore = Integer.MIN_VALUE;
		highestPlayer = "";

		//creates and launches the server socket
		try {
			System.out.println("Server: ");
			ServerSocket server = new ServerSocket(4242);   
			System.out.println(server.getLocalPort());
			System.out.println(InetAddress.getLocalHost().getHostAddress());

			//spawns new Client Handler threads forever
			while(true) {

				Thread newPlayer = new Thread(new ClientHandler(server.accept()));
				newPlayer.start();

			}

		}catch(IOException e) {
			e.printStackTrace();
		}
	}

	/* 
	 * ClientHandler - inner class
	 * 
	 * handles the client that connects to the server
	 * provides answer selections and questions from the test
	 * informs the client whether they chose correctly or not
	 */
	private class ClientHandler implements Runnable{

		private Scanner in;
		private PrintWriter out;

		//creates a thread that handles a client
		public ClientHandler(Socket s){

			try {

				in = new Scanner(s.getInputStream());
				out = new PrintWriter(s.getOutputStream());
			} catch (IOException e) {

				e.printStackTrace();
				System.exit(2);
			}

		}

		//facilitates the test prep "game"
		//sends questions, receives answers, sends back results
		public void run(){

			int numCorrect = 0;
			String name = in.nextLine();

			//sends out the size of the test
			out.println(test.size());
			out.flush();

			for(int i = 0; i < test.size(); i++) {

				//sends out the question + answer choices
				out.println(test.get(i).question);

				for(String ans: test.get(i).answers) 
					out.println(ans);	
				out.flush();

				//takes in the user's choice
				String choice = in.nextLine();
				String message = "Nope.";

				if(Integer.parseInt(choice) == test.get(i).correctAns) {
					message = "Correct!";
					numCorrect++;
				}
				
				//sends out the pop-up message and the current test number 
				out.println(message);
				out.println(numCorrect);
				out.println(i+1);
				out.flush();
			}

			String message;
			//makes sure that a player isn't set as the highest player if 
			//this thread is put to sleep while another thread has a player that scored higher
			synchronized(highestPlayer) {

				if(numCorrect > highestScore) {

					highestScore = numCorrect;
					highestPlayer = name + " has ";
					numWinners = 1;
				}
				else if(numCorrect == highestScore) {

					numWinners++;
					highestPlayer = numWinners + " players have ";
				}
				message = highestPlayer+ "the high score with " +highestScore+ " correct.";
			}

			out.println(message);
			out.flush();

		}
	}

	/*
	 * AllQuestions - inner ArrayList of Questions
	 * Takes in the file "questions.txt" to create a test.  Assumes that the file is in the correct format
	 */
	public class AllQuestions extends ArrayList<Question> {

		public AllQuestions(String fName){

			Scanner fileIn = null;

			//looking for the file
			try{
				fileIn = new Scanner(new File(fName));

			}catch(FileNotFoundException e){

				System.out.println("Can't find file");
				System.exit(-1);
			}

			//reading in the file to create the object
			while(fileIn.hasNextLine()){

				String q = fileIn.nextLine();
				ArrayList<String> a = new ArrayList<String>();

				for(int i = 0; i < 4; i++)
					a.add(fileIn.nextLine());

				add(new Question(q, a, fileIn.nextInt()));

				if(fileIn.hasNextLine())
					fileIn.nextLine();
			}

			shuffle();
		}

		//shuffles the order of questions [0, 50] times
		private void shuffle(){

			int numRepeats = (int)(Math.random()*51);

			for(int i = 0; i < numRepeats; i++){

				int ranIndex = (int)(Math.random()*size());
				add(remove(ranIndex));
			}


		}

	}

	/* 
	 * Question - inner class
	 * Contains a test question, 4 possible answer choices and an int representing the index of the correct answer
	 */
	public class Question {

		private String question;
		private ArrayList<String> answers;
		private int correctAns;

		public Question(String q, ArrayList<String> a, int c){

			if(c < 0 || c >= a.size()){
				throw new IllegalArgumentException("Out of bounds answer");
			}

			question = q;
			correctAns = c;

			answers = new ArrayList<String>();

			for(String ans: a)
				answers.add(ans);

			shuffle();

		}

		//shuffles 4 possible answers a random [0,50] number of times
		private void shuffle(){

			int numRepeats = (int)(Math.random()*51);

			for(int i = 0; i <= numRepeats; i++){

				int ranIndex = (int)(Math.random()*4);

				if(correctAns == ranIndex)
					correctAns = 3;
				else if(ranIndex < correctAns)
					correctAns--;

				answers.add(answers.get(ranIndex));
				answers.remove(ranIndex);
			}

		}

		public String getQuestion(){
			return question;
		}

		public ArrayList<String> getAnswerChoices(){
			return answers;
		}

		public int getCorrectAns(){
			return correctAns;
		}

	}

	//starts the server
	public static void main(String[] args){
		new TestPrepServer();
	}
}
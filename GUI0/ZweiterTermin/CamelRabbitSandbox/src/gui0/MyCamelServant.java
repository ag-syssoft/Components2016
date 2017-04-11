package gui0;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessOrder;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.git.GitConstants;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.xerial.snappy.Snappy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;

public class MyCamelServant {

	// Connection Strings

	// Target -> Broker
	final static String broker_out = "rabbitmq://136.199.51.111/inExchange?username=kompo&password=kompo&skipQueueDeclare=true&exchangeType=fanout&autoDelete=false";

	// Connection to jsgui
	final static String jsgui = "websocket://myUri?sendToAll=true";
	final static String windowsFormIn = "file://../Forms/SudokuGui/bin/Debug/InMessages/?fileName=${date:now:yyyyMMdd}.json";
	final static String windowsFormOut = "file://../Forms/SudokuGui/bin/Debug/OutMessages";
	final static String windowsFormGitIn = "file://../Forms/SudokuGui/bin/Debug/GitInMessages";
	final static String windowsFormGitOut = "file://../Forms/SudokuGui/bin/Debug/GitOutMessages";
	final static String irc_nickname = RandomStringUtils.random(5,"ABCDEFGHIJKLMNOPQRSTUVWXYZ");
	final static String broker_in_irc = "irc:"+irc_nickname+"@irc.freenode.net/#MyCamelTest&nickname="+irc_nickname;
	final static String broker_in_netty = "netty4:tcp://0.0.0.0:8888?textline=true";
	final static String dummy_out = "stream:out";
	
	final static String git_clone = "git://../sudoku_communicate?remotePath=https://github.com/sudokuGit/sudoku_communicate.git&operation=clone&username=sudokuGit&password=i<3sudoku";
	final static String git_in = "file://../sudoku_communicate";
	final static String git = "git://../sudoku_communicate?remoteName=origin&remotePath=https://github.com/sudokuGit/sudoku_communicate.git&type=commit&username=sudokuGit&password=i<3sudoku";
	final static String git_pull = "git://../sudoku_communicate?remoteName=origin&remotePath=https://github.com/sudokuGit/sudoku_communicate.git&operation=pull";
	final static String git_create_branch = "git://../sudoku_communicate?remotePath=https://github.com/sudokuGit/sudoku_communicate.git&operation=createBranch";
	final static String git_add = "git://../sudoku_communicate?remotePath=https://github.com/sudokuGit/sudoku_communicate.git&operation=add";
	final static String git_commit = "git://../sudoku_communicate?remotePath=https://github.com/sudokuGit/sudoku_communicate.git&operation=commit";
	final static String git_push = "git://../sudoku_communicate?remoteName=origin&remotePath=https://github.com/sudokuGit/sudoku_communicate.git&operation=push&username=sudokuGit&password=i<3sudoku";
	static String branch = "master";
	static String my_sender;
	
	static boolean pull_completed = true;

	public static void main(String[] args) {
		CamelContext context = new DefaultCamelContext();
		System.out.println("Context created.");
		
		try {
			//jsgui IN
			context.addRoutes(new RouteBuilder() {
				public void configure() {

					from(jsgui).convertBodyTo(byte[].class).process(new Processor() {

						@Override
						public void process(Exchange arg0) throws Exception {

							// incoming message compressed with google snappy

							Message m = arg0.getIn();
							byte[] s = (byte[]) m.getBody();
							byte[] unc = Snappy.uncompress(s);

							String str_uncompressed = new String(unc, Charset.forName("UTF-8"));
							System.out.println("uncompressed string received: >" + str_uncompressed.trim() + "<");

							// processing
														
							// generate message object out of the str_uncompressed string from js
							Gson gson = new Gson();
							MyMessage msg = new MyMessage();
							msg.generateDummy(1);
							msg.setSender("irc:@irc.freenode.net/#MyCamelTest&nickname=MyJames");
							//msg.setSender("irc:MyJames@136.199.53.49:6667/#MyCamelTest&nickname=MyJames");
							System.out.println(str_uncompressed);
							switch(str_uncompressed.trim().split(":")[0]) {
							
							case "register":
								
								System.out.println("jsgui sent us a register query");
								msg.setInstruction("register:gui");
								m.setBody(msg.toJSON());
								m.setHeader("valid-message", true);
								my_sender = msg.getSender();
								break;
								
							case "unregister":
								System.out.println("jsgui sent us an unregister query");
								msg.setInstruction("unregister");
								msg.setSender(my_sender);
								m.setBody(msg.toJSON());
								m.setHeader("valid-message", true);
								break;
								
							case "ping":
							
								System.out.println("jsgui sent us an unregister query");
								msg.setInstruction("ping");
								msg.setSender(my_sender);
								m.setBody(msg.toJSON());
								m.setHeader("valid-message", true);
								break;
								
							case "pong":
								
								msg.setInstruction("jsgui sent us a pong answer");
								msg.setSender(my_sender);
								m.setBody(msg.toJSON());
								m.setHeader("valid-message", true);
								break;
								
							case "generate":
								System.out.println("jsgui sent us a generate");
								msg.generateDummy(Integer.valueOf(str_uncompressed.trim().split(":")[1]));
								msg.setInstruction("generate:"+str_uncompressed.trim().split(":")[2]);
								msg.setSender(my_sender);
								m.setBody(msg.toJSON());
								m.setHeader("valid-message", true);
								break;
								
							case "solve":
								System.out.println("jsgui sent us a solve");
								String[] solve_sudoku = str_uncompressed.trim().split(":")[1].split(",");
								int[] solve_array = Arrays.stream(solve_sudoku).mapToInt(Integer::parseInt).toArray();
								msg.setSudoku(solve_array);
								msg.setInstruction("solve");
								msg.setSender(my_sender);
								m.setBody(msg.toJSON());
								m.setHeader("valid-message", true);
								break;
							case "push":
								String[] strings = str_uncompressed.trim().split(":")[1].split(",");
								int[] array = Arrays.stream(strings).mapToInt(Integer::parseInt).toArray();
								msg.setSudoku(array);
								String dir = str_uncompressed.trim().split(":")[2];
								msg.setInstruction("display");
								
								try{
									File push_file = new File("../sudoku_communicate/"+dir+"/sudoku.json");
									synchronized (push_file) {
										if(push_file.exists()){
											push_file.delete();
										}
										push_file.createNewFile();
										FileOutputStream fStream = new FileOutputStream(push_file);
										BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fStream));
										writer.write(msg.toJSON());
										writer.close();
									}
								}catch (IOException e) {
									// TODO: handle exception
								}
								m.setHeader("git", "push");
								m.setHeader(GitConstants.GIT_COMMIT_MESSAGE, new Date().toString());
								m.setHeader("valid-message", true);
								break;
							case "pull":
								m.setHeader("git", "pull");
								branch = str_uncompressed.trim().split(":")[1];
								m.setHeader("valid-message", true);
								//pull_completed = false;
								break;
								default:
									System.err.println("jsgui sent us the following unsupported message: " + str_uncompressed);
									break;
																
							}
							
						}
					}).choice()
					.when(header("git").isEqualTo("push"))
						.setHeader(GitConstants.GIT_FILE_NAME, constant(".")).to(git_add)
						.to(git_commit)
						.to(git_push)
					.endChoice()
					.when(header("git").isEqualTo("pull"))
						.to(git_pull).process(new Processor() {
							
							@Override
							public void process(Exchange exchange) throws Exception {
								pull_completed = false;
							}
						}).endChoice()
					.otherwise()
						.to(broker_out);

				}
			});
			
			//WForms IN
			context.addRoutes(new RouteBuilder() {
				public void configure() {

					from(windowsFormOut).convertBodyTo(byte[].class).process(new Processor() {

						@Override
						public void process(Exchange arg0) throws Exception {

							// incoming message compressed with google snappy

							Message m = arg0.getIn();
							byte[] s = (byte[]) m.getBody();
							
							String str_uncompressed = new String(s, Charset.forName("UTF-8"));
							System.out.println("Uncompressed String:>" + str_uncompressed.trim() + "<");

							// processing
														
							// generate message object out of the str_uncompressed string from js
							MyMessage msg = new MyMessage();
							msg.generateDummy(1);
							try{
								JSONObject jObj = XML.toJSONObject(str_uncompressed).getJSONObject("message");
								String json = XML.toJSONObject(str_uncompressed).get("message").toString();
								
								msg.setSender(jObj.getString("sender"));
								msg.setInstruction(jObj.getString("instruction"));
								String sudoku = jObj.getString("sudoku");
								sudoku.replace("[","");
								String[] digits = sudoku.split(",");
								int[] sudokuVals = new int[digits.length];
								for(int i = 0; i < sudokuVals.length; i++){
									sudokuVals[i] = Integer.parseInt(digits[i]);
								}
								
								msg.setSudoku(sudokuVals);
								m.setBody(msg.toJSON());
								m.setHeader("valid-message", true);
							} catch(JSONException e){
								try{
								msg = new Gson().fromJson(str_uncompressed, MyMessage.class);
								m.setHeader("valid-message", true);
								m.setBody(msg.toJSON());
								} catch(Exception ex){
									msg = null;
									m.setHeader("valid-message", false);
								}
							}
						}
					}).choice().when(header("valid-message").isEqualTo(true)).to(broker_out).otherwise().to(dummy_out);

				}
			});
			
			//Broker Netty IN
			context.addRoutes(new RouteBuilder() {
				public void configure() {

					from(broker_in_netty).convertBodyTo(String.class).process(new InProcessor())
					.choice().when(header("valid-message").isEqualTo(true))
					.to(windowsFormIn).otherwise().to(dummy_out);
					

				}
			});
			
			//Broker IRC IN
			context.addRoutes(new RouteBuilder() {
				public void configure() {

					from(broker_in_irc).convertBodyTo(String.class).process(new InProcessor())
					.choice().when(header("valid-message").isEqualTo(true)).process(new Processor() {
						@Override
						public void process(Exchange arg0) throws Exception {
							Message m = arg0.getIn();
							m.setBody(Snappy.compress((String) m.getBody()));
							arg0.setOut(m);
						}
					}).to(jsgui).otherwise().to(dummy_out);
					

				}
			});
			
			//Init Git Repo on Startup
			context.addRoutes(new RouteBuilder() {
				public void configure() {
					from("timer://foo?delay=-1&fixedRate=true&repeatCount=1").doTry().to(git_clone).doCatch(IllegalArgumentException.class).to(git_pull);
				}
			});
			
			//Git local rep to GUI
			context.addRoutes(new RouteBuilder() {
				public void configure() {
					from(git_in+"?noop=true&readLock=none&recursive=true&idempotent=false&autoCreate=false&include=.*\\.json").to(windowsFormGitIn).process(new Processor() {
						@SuppressWarnings("unchecked")
						@Override
						public void process(Exchange arg0) throws Exception {
							Message m = arg0.getIn();
							GenericFile<File> gf = (GenericFile<File>) m.getBody();
							File f = gf.getFile();
							if(!pull_completed && f.getPath().contains(branch+"\\")){
								synchronized (f) {
									FileReader reader = new FileReader(f);
									BufferedReader br = new BufferedReader(reader);
									String msg_rcvd = "";
									String line = null;
									do{
										line = br.readLine();
										if(line != null)
											msg_rcvd += line;
									}while(line != null);
									br.close();
									msg_rcvd = msg_rcvd.trim();
									m.setBody(Snappy.compress(msg_rcvd));
									m.setHeader("redundant", false);	
									pull_completed = true;
								}
							} else {
								m.setHeader("redundant", true);	
							}						
							arg0.setOut(m);
						}
					}).choice().when(header("redundant").isEqualTo(false)).to(jsgui);
					
				}
			});
			
			//WForms Git Operations
			context.addRoutes(new RouteBuilder() {
				public void configure() {
					from(windowsFormGitOut).convertBodyTo(byte[].class).process(new Processor() {

						@Override
						public void process(Exchange arg0) throws Exception {
							Message m = arg0.getIn();
							byte[] s = (byte[]) m.getBody();
							
							String in_string = new String(s, Charset.forName("UTF-8"));
							
							Gson gson = new Gson();
							try {
								GitMessage recv_msg_obj = gson.fromJson(in_string, GitMessage.class);
								branch = recv_msg_obj.getBranch();
								m.setHeader("operation", recv_msg_obj.getOperation());
								if(recv_msg_obj.getOperation().equals("push")){
									m.setHeader(GitConstants.GIT_COMMIT_MESSAGE, new Date().toString());
								}
								
							} catch (JsonSyntaxException ex) {
								System.err.println("Message was RUBBISH and will be dropped");
							}
						}
					}).choice().when(header("operation").contains("pull")).to(git_pull).endChoice().otherwise()
					.setHeader(GitConstants.GIT_FILE_NAME, constant(".")).to(git_add)
					.to(git_commit)
					.to(git_push).endChoice().end();
				}
			});
			
			//Git Commit Counter
			context.addRoutes(new RouteBuilder() {
				public void configure() {
					from(git).process(new Processor() {

						@Override
						public void process(Exchange arg0) throws Exception {
							Message m = arg0.getIn();
							MyMessage msg = new MyMessage();
							msg.generateDummy(1);
							msg.setSender(git);
							msg.setInstruction("commit");
							m.setBody(msg.toJSON());
							
							arg0.setOut(m);
						}
						
					}).to(windowsFormIn);
				}
			});
				
			System.out.println("Routes added.");
			context.start();
			Thread.currentThread().join();
			System.out.println("Context started.");

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

}

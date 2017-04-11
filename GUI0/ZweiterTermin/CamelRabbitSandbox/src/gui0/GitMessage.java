package gui0;

public class GitMessage {
	private String branch;
	private String operation;
	
	public GitMessage(){
		branch = "master";
		operation = "pull";
	}
	
	public GitMessage(String branch, String operation){
		this.branch = branch;
		this.operation = operation;
	}
	
	public String getBranch(){
		return branch;
	}
	
	public void setBranch(String branch){
		this.branch = branch;
	}
	
	public String getOperation(){
		return operation;
	}
	
	public void setOperation(String operation){
		this.operation = operation;
	}
}

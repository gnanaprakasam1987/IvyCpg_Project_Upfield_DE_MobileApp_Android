package com.ivy.sd.png.bo;

import com.ivy.ui.task.model.TaskDataBO;

import java.util.ArrayList;
import java.util.Vector;

public class InStoreActivityBO {

	public String rid;
	private ArrayList<String> taskid;
	private ArrayList<String> taskidNon;
	private Vector<TaskDataBO> taskVector;
	

	public int gandola = 0, self = 0, asset = 0, instore = 0, display = 0;

	public String getRid() {
		return rid;
	}

	public void setRid(String rid) {
		this.rid = rid;
	}

	public int getGandola() {
		return gandola;
	}

	public void setGandola(int gandola) {
		this.gandola = gandola;
	}

	public int getSelf() {
		return self;
	}

	public void setSelf(int self) {
		this.self = self;
	}

	public int getAsset() {
		return asset;
	}

	public void setAsset(int asset) {
		this.asset = asset;
	}

	public int getInstore() {
		return instore;
	}

	public void setInstore(int instore) {
		this.instore = instore;
	}

	public int getDisplay() {
		return display;
	}

	public void setDisplay(int display) {
		this.display = display;
	}

	/*
	 * public void setTaskid(Vector taskid) { this.taskid = taskid; }
	 * 
	 * public Vector getTaskid() { return taskid; }
	 */
	
	

	public void setTaskid(ArrayList<String> taskid) {
		this.taskid = taskid;
	}

	public ArrayList<String> getTaskid() {
		return taskid;
	}

	public ArrayList<String> getTaskidNon() {
		return taskidNon;
	}

	public void setTaskidNon(ArrayList<String> taskidNon) {
		this.taskidNon = taskidNon;
	}



	public Vector<TaskDataBO> getTaskVector() {
		return taskVector;
	}

	public void setTaskVector(Vector<TaskDataBO> taskVector) {
		this.taskVector = taskVector;
	}

}

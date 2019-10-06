package KTPM;

//import java.io.FileWriter;
import java.io.IOException;

import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;

public class PatientsApplet extends SimState
{
	public Continuous2D env;
	public int numPatients = 10000;
	public int yearlyAdditions;
	public boolean equalLiving = false;
	public double livingF = 0.0;
	public double deceasedF = 0.0;
	public double deceasedMult = 1.0;
	
	public static int runTime = 15;
	public int patientCounter = 1;
	
	// Variable that measure the current state of the Process
	public int currentWait = numPatients;
	public int lTrans = 0;
	public int dTrans = 0;
	public int dead = 0;
	public int removed = 0;
	public int dieWait = 0;
	public int prevWait = numPatients;
	
	Bag transPatients = new Bag();
	Bag removePatients = new Bag();
	Bag keepPatients = new Bag();
	
	public int getNumPatients() {return numPatients;}
	public void setNumPatients(int val) { numPatients = val;}
	public double getLivingFactor() {return livingF;}
	public void setLivingFactor(double val) { livingF = val;}
	public double getDeceasedFactor() {return deceasedF;}
	public void setDeceasedFactor(double val) { deceasedF = val;}
	public boolean getEqualLiving() {return equalLiving;}
	public void setEqualLiving(boolean val) { equalLiving = val;}
	
	
	//FileWriter statsFile;
	//FileWriter agentsFile;
	//FileWriter yearlyStatsFile; 
	
	public PatientsApplet(long seed)
	{
		super(seed);
	}
	
	public void start()
	{
		super.start();
		env = new Continuous2D(1.0,100,100);
		currentWait = numPatients;
		//env.clear();
		//this.setupFiles();
		patientCounter = 0;
		yearlyAdditions = (int)((double)numPatients * .3565);
		System.out.println("Simulaiton Beginning...");
		System.out.println("Num Agents: "+ numPatients);
		
		removePatients.clear();
		transPatients.clear();
		removePatients.shrink(0);
		transPatients.shrink(0);
		keepPatients.clear();
		keepPatients.shrink(0);
		
		System.out.println("Adding Patients: "+ numPatients);
		for(int i = 0; i < numPatients; i++)
		{
			System.out.println(i);
			Patient patient = new Patient(this, patientCounter, true);
			env.setObjectLocation(patient, 
					new Double2D(env.getWidth() * 0.5 + random.nextDouble() - 0.5, 
							env.getHeight() * 0.5 + random.nextDouble() - 0.5));
			
			transPatients.add(patient);
			schedule.scheduleRepeating(patient,4,1);
			patientCounter++;
		}
		//System.out.println("Patient Counter: "+ patientCounter);
		
		Steppable ageGroupUpdater = new Steppable() {
			public void step(SimState state) {
				updateAgeGroups();
			}
		};
		schedule.scheduleRepeating(ageGroupUpdater, 5,1);
		
		Steppable assigner = new Steppable() {
			public void step(SimState state) {
				assignTransplants();
			}
		};
		schedule.scheduleRepeating(assigner, 6,1);
		
		Steppable ager = new Steppable() {
			public void step(SimState state) {
				agePatients();
			}
		};
		schedule.scheduleRepeating(ager, 7,1);
		
		Steppable additions = new Steppable() {
			public void step(SimState state) {
				addToWaitList(yearlyAdditions);
			}
		};
		schedule.scheduleRepeating(additions, 8,1);
		
		Steppable gatherStats = new Steppable() {
			public void step(SimState state) {
				getWLSize();
			}
		};
		schedule.scheduleRepeating(gatherStats, 9,1);
	}
	
	public void finish(){
		//recordPatients();
		System.out.println("Simulation has ended");
		/*writeStats();
		try {
			agentsFile.close();
			yearlyStatsFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
	
	public static void main(String[] args)
	{
		/*if(args.length > 0){
			numPatients = Integer.parseInt(args[0]);
			livingF = Double.parseDouble(args[1]);
			deceasedF = Double.parseDouble(args[2]);
			equalLiving = Boolean.parseBoolean(args[3]);
		}*/
				
		
		SimState state = new Patients(System.currentTimeMillis());
		state.start();
		
		do {
			if(!state.schedule.step(state)) break;
			System.out.println("Step:" + state.schedule.getSteps());
		} while(state.schedule.getSteps() < runTime);
		state.finish();
		
		System.exit(0);
	}
	
	public void updateAgeGroups(){
		for(int i = 0; i < transPatients.size(); i++){
			Patient patient = (Patient) transPatients.get(i);
			patient.updateAgeGroup(patient);
		}
	}
	
	public void assignTransplants(){
		deceasedMult = (double) numPatients / (double) currentWait;
		
		System.out.println("Number of Available Deceased Donor Kidneys: " + Math.ceil(currentWait*.112*(deceasedF+1)*deceasedMult));
		System.out.println(deceasedMult);
		for(int i = 0; i < transPatients.size(); i++){
			Patient patient = (Patient) transPatients.get(i);
			patient.assignTransplant(patient, random.nextDouble(), livingF, 
					deceasedF, deceasedMult, equalLiving);
		}
	}
	
	public void agePatients(){
		for(int i = 0; i < transPatients.size(); i++){
			Patient patient = (Patient) transPatients.get(i);
			patient.getOlder(patient, random.nextDouble());
		}
	}
	
	public void recordPatients(){
		for(int i = 0; i < transPatients.size(); i++){
			Patient patient = (Patient) transPatients.get(i);
			String toWrite = patient.record(patient);
			//writeToFile(toWrite);
		}
	}
	
	public void recordRemoved(){
		for(int i = 0; i < removePatients.size(); i++){
			Patient patient = (Patient) removePatients.get(i);
			String toWrite = patient.record(patient);
			//writeToFile(toWrite);
		}
		removePatients.clear();
	}
	
	public void addToWaitList(int newPatients){
		int startNum = transPatients.size();
		for(int i = startNum; i < startNum+newPatients; i++)
		{
			//System.out.println("Adding Agent: " + i);
			Patient patient = new Patient(this, patientCounter, false);
			env.setObjectLocation(patient, 
					new Double2D(env.getWidth() * 0.5 + random.nextDouble() - 0.5, 
							env.getHeight() * 0.5 + random.nextDouble() - 0.5));
			
			transPatients.add(patient);
			schedule.scheduleRepeating(patient,4,1);
			patientCounter++;
		}
	}
	
	public void getWLSize(){
		int Wait = 0;
		int TransLiving = 0;
		int TransDeceased = 0;
		int Removed = 0;
		int Deceased = 0;
		int DWait = 0;
		System.out.println("Current Wait List Size: " + currentWait);
		prevWait = currentWait;
		for(int i = 0; i < transPatients.size(); i++){
			Patient patient = (Patient) transPatients.get(i);
			if(patient.Waiting(patient)) {
				Wait++;
			}
			if(patient.LivingTrans(patient)) {
				TransLiving++;
			}
			if(patient.DeceasedTrans(patient)) {
				//keepPatients.add(patient);
				TransDeceased++;
			}
			if(patient.Removed(patient)){
				Removed++;
				//transPatients.remove(patient);
			}
			if(patient.Deceased(patient)){
				//transPatients.remove(patient);
				Deceased++;
			}
			if(patient.DieWait(patient)){
				//transPatients.remove(patient);
				DWait++;
			}
			if(patient.Deceased(patient)==true || patient.Removed(patient)==true){
				removePatients.add(patient);
			}
			else {
				keepPatients.add(patient);
			}
		}
		
		recordRemoved();	
		removePatients.clear();
		transPatients.clear();
		removePatients.shrink(0);
		transPatients.shrink(0);
		for(int i = 0; i < keepPatients.size(); i++){
			Patient patient = (Patient) keepPatients.get(i);
			transPatients.add(patient);
		}
		keepPatients.clear();
		keepPatients.shrink(0);
		
		System.out.println("Current Wait List Size: " + Wait);
		System.out.println("Prev: " + prevWait);
		
		currentWait = Wait;
		lTrans = TransLiving;
		dTrans = TransDeceased;
		dead = Removed;
		removed = Deceased;
		dieWait = DWait;
		//writeYearlyToFile();
		System.out.println("Deceased Transplant: " + dTrans);
	}
	
	/*public void setupFiles() {
			// create files
			try {
				statsFile = new FileWriter("stats.csv");
			} catch (IOException e) {
				System.out.println("Could not open stats file");
			}
			try {
				agentsFile = new FileWriter("agents.csv");
			} catch (IOException e) {
				System.out.println("Couldn't open agents file.");
			}
			try {
				yearlyStatsFile = new FileWriter("yearlyStats.csv");
			} catch (IOException e) {
				System.out.println("Couldn't open yearly stats file.");
			}
			try {
				statsFile.write("Patients, LivingFactor, DeceasedFactor, " +
						"WaitList, LivingTrans, DeceasedTrans, Deceased, Removed, ChangeFactor\n");
				
				yearlyStatsFile.write("Year, WaitList, LivingTrans, DeceasedTrans, Deceased, Removed\n");

				agentsFile.write("AgentID, Age, Ethnicity, Transplant, TransType, TransYears, " +
						"WaitYears, Dead, RemovalReason, RemovalYear\n");
			} catch (IOException e) {
				System.out.println("Couldn't write to output");
			}

	}
	
	public void writeStats(){
		try {
			statsFile.write( numPatients + "," + livingF + "," + deceasedF + "," +
					currentWait + "," + lTrans + "," + dTrans + "," + dead + "," + removed + ","
					+ (double) currentWait / (double) numPatients + "\n");
			statsFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeYearlyToFile(){
		try{
			yearlyStatsFile.write(schedule.getSteps() + "," + currentWait + "," + lTrans 
					+ "," + dTrans + "," + dead + "," + removed + "\n");
		} catch (IOException e) {
			System.out.println("Coutln't write to output");
		}
	}
	
	public void writeToFile(String s) {
		try {
			agentsFile.write(s +"\n");
		} catch (IOException e) {
			System.out.println(e);
		}
	}*/
	
}
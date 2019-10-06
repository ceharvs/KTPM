package KTPM;

import java.io.FileWriter;
import java.io.IOException;

import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;

public class Patients extends SimState
{
	
	//Initalize Variables
	public Continuous2D env;
	public static int numPatients = 30000; 		//Number of Initial Patients
	public int yearlyAdditions;					//Number of yearly additions to the list
	public static boolean equalLiving = true;  //Variable to determine if equal odds of 
												//receiving a living donor transplant
	public static double livingF = 0.0;			//Living Factor (When set to 0, process 
													//continues as normal)
	public static double deceasedF = 0.0;		//Deceased Factor (When set to 0, process 
													//continues as normal)
	public double deceasedMult = 1.0;			//Deceased Multiplier = 
													//number of initial patients/current waiting list size
												//This parameter is to ensure that a consistent number 
												//of deceased donor kidneys are available every year. 
	
	public static int runTime = 15;				//Number of years for the model to run
	public int patientCounter = 1;					//Counter for the number of patients
	
	// Variable that measure the current state of the Process
	public int currentWait = numPatients;		//Initally the current wait is = numPatients
	public int lTrans = 0;						//Number that have recieved a living transplant
	public int dTrans = 0;						//Number that have recieved a deceased transplant
	public int dead = 0;						//Number that have died
	public int removed = 0;						//Number that have been removed
	public int dieWait = 0;						//Number that have died waiting for a transplant
	
	Bag transPatients = new Bag();				//Bag of ALL agents that haven't been removed
	Bag removePatients = new Bag();				//Bag of patients to be removed (died or otherwise removed)
	Bag keepPatients = new Bag();				//Bag of patients to keep (haven't been removed)
	
	public int getNumPatients() {return numPatients;}
	public void setNumPatients(int val) { numPatients = val;}
	public double getLivingFactor() {return livingF;}
	public void setLivingFactor(double val) { livingF = val;}
	public double getDeceasedFactor() {return deceasedF;}
	public void setDeceasedFactor(double val) { deceasedF = val;}
	public boolean getEqualLiving() {return equalLiving;}
	public void setEqualLiving(boolean val) { equalLiving = val;}
	
	//File Writing Methods
	FileWriter statsFile; 		//Contains overall statistics over time
	FileWriter agentsFile;		//File of all agents and their outomes
	FileWriter yearlyStatsFile; //Statistics updated every year
	
	public Patients(long seed) {
		super(seed);
	}
	
	/**
	 * Start Method, is executed at the beginning to 
	 * initialize the program
	 */
	public void start()
	{
		super.start();
		env = new Continuous2D(1.0,100,100);
		currentWait = numPatients;
		//env.clear();
		this.setupFiles();
		patientCounter = 0;
		
		//Estimate the number of yearly additions to be a constant.
		//NEEDS FIXING
		yearlyAdditions = (int)((double)numPatients * .3565);
		
		//Clear and shrink all of the bags
		removePatients.clear();
		transPatients.clear();
		removePatients.shrink(0);
		transPatients.shrink(0);
		keepPatients.clear();
		keepPatients.shrink(0);
		
		//Add patients to the transPatients Bag
		for(int i = 0; i < numPatients; i++)
		{
			
			PatientRegular patient = new PatientRegular(this, patientCounter, true);
			env.setObjectLocation(patient, 
					new Double2D(env.getWidth() * 0.5 + random.nextDouble() - 0.5, 
							env.getHeight() * 0.5 + random.nextDouble() - 0.5));
			
			//Add the newly created patient to the bag
			transPatients.add(patient);
			//Schedule patients to take a step every one second
			schedule.scheduleRepeating(patient,4,1);
			patientCounter++;
		}
		
		//Schedule the agent's age group to be updated if necessary
		Steppable ageGroupUpdater = new Steppable() {
			public void step(SimState state) {
				updateAgeGroups();
			}
		};
		schedule.scheduleRepeating(ageGroupUpdater, 5,1);
		
		//Schedule agent to be assigned a transplant (or not be assigned a transplant)
		Steppable assigner = new Steppable() {
			public void step(SimState state) {
				assignTransplants();
			}
		};
		schedule.scheduleRepeating(assigner, 6,1);
		
		//Schedule agent to be aged one year)
		Steppable ager = new Steppable() {
			public void step(SimState state) {
				agePatients();
			}
		};
		schedule.scheduleRepeating(ager, 7,1);
		
		//Schedule new patients to be added to the waiting list
		Steppable additions = new Steppable() {
			public void step(SimState state) {
				addToWaitList(yearlyAdditions);
			}
		};
		schedule.scheduleRepeating(additions, 8,1);
		
		//Collect statistics on the agents and current state
		Steppable gatherStats = new Steppable() {
			public void step(SimState state) {
				getWLSize();
			}
		};
		schedule.scheduleRepeating(gatherStats, 9,1);
	}
	
	/** 
	 * Finish method, 
	 * write out the statistics and close files
	 */
	public void finish(){
		recordPatients();
		System.out.println("Simulation has ended");
		writeStats();
		try {
			agentsFile.close();
			yearlyStatsFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** 
	 * Finish method, 
	 * write out the statistics and close files
	 */
	public static void main(String[] args)
	{
		//Take in command line arguements to set the number of patients, 
		//the living factor, and the deceased factor
		if(args.length > 0){
			numPatients = Integer.parseInt(args[0]);
			livingF = Double.parseDouble(args[1]);
			deceasedF = Double.parseDouble(args[2]);
			equalLiving = Boolean.parseBoolean(args[3]);
		}
		
		//Begin the new Simstate				
		SimState state = new Patients(System.currentTimeMillis());
		state.start();
		
		//Mason loop to execute the simulation
		do {
			if(!state.schedule.step(state)) break;
			System.out.println("Step:" + state.schedule.getSteps());
		} while(state.schedule.getSteps() < runTime);
		state.finish();
		
		System.exit(0);
	}

	// Update the age groups of the patients.  
	public void updateAgeGroups(){
		for(int i = 0; i < transPatients.size(); i++){
			PatientRegular patient = (PatientRegular) transPatients.get(i);
			patient.updateAgeGroup(patient);
		}
	}
	
	// For each Agent, assign whether or not they recieve a transplant
	public void assignTransplants(){
		//Compute the ratio of the initial size to the current waiting list size
		deceasedMult = (double) numPatients / (double) currentWait;
		for(int i = 0; i < transPatients.size(); i++){
			PatientRegular patient = (PatientRegular) transPatients.get(i);
			//Assign the patient a transplant based on the random.nextDouble call
			patient.assignTransplant(patient, random.nextDouble(), livingF, 
					deceasedF, deceasedMult, equalLiving);
		}
	}
	
	//Age each of the patients one year
	public void agePatients(){
		for(int i = 0; i < transPatients.size(); i++){
			PatientRegular patient = (PatientRegular) transPatients.get(i);
			patient.getOlder(patient, random.nextDouble());
		}
	}
	
	//Record information about patients
	//this is only called at the end in the finish method
	public void recordPatients(){
		for(int i = 0; i < transPatients.size(); i++){
			PatientRegular patient = (PatientRegular) transPatients.get(i);
			String toWrite = patient.record(patient);
			writeToFile(toWrite);
		}
	}
	
	//Record which patients have been removed, these are
	//patients in the removePatients Bag.
	public void recordRemoved(){
		for(int i = 0; i < removePatients.size(); i++){
			PatientRegular patient = (PatientRegular) removePatients.get(i);
			String toWrite = patient.record(patient);
			writeToFile(toWrite);
		}
		removePatients.clear();
	}
	
	//Add a certain number of patients to the waiting list.  
	public void addToWaitList(int newPatients){
		int startNum = transPatients.size();
		for(int i = startNum; i < startNum+newPatients; i++)
		{
			//System.out.println("Adding Agent: " + i);
			PatientRegular patient = new PatientRegular(this, patientCounter, false);
			env.setObjectLocation(patient, 
					new Double2D(env.getWidth() * 0.5 + random.nextDouble() - 0.5, 
							env.getHeight() * 0.5 + random.nextDouble() - 0.5));
			
			transPatients.add(patient);
			schedule.scheduleRepeating(patient,4,1);
			patientCounter++;
		}
	}

	//Get statistics about the current waiting list
	public void getWLSize(){
		int Wait = 0;
		int TransLiving = 0;
		int TransDeceased = 0;
		int Removed = 0;
		int Deceased = 0;
		int DWait = 0;
		for(int i = 0; i < transPatients.size(); i++){
			PatientRegular patient = (PatientRegular) transPatients.get(i);
			if(patient.Waiting(patient))
				Wait++;
			if(patient.LivingTrans(patient))
				TransLiving++;
			if(patient.DeceasedTrans(patient))
				TransDeceased++;
			if(patient.Removed(patient))
				Removed++;
			if(patient.Deceased(patient))
				Deceased++;
			if(patient.DieWait(patient))
				DWait++;
			if(patient.Deceased(patient)==true || patient.Removed(patient)==true)
				removePatients.add(patient);
			else 
				keepPatients.add(patient);
		}
		
		//Record the patients that need to be removed
		recordRemoved();	
		//Clear out the transPatients and Remove Patients Bags
		removePatients.clear();
		transPatients.clear();
		removePatients.shrink(0);
		transPatients.shrink(0);
		//Add all of the keepPatients to the transPatients Bag
		for(int i = 0; i < keepPatients.size(); i++){
			PatientRegular patient = (PatientRegular) keepPatients.get(i);
			transPatients.add(patient);
		}
		//Clear the keepPatients Bag
		keepPatients.clear();
		keepPatients.shrink(0);
		
		//Print out the current size of the waiting list		
		System.out.println("Current Wait List Size: " + Wait);
		System.out.println("Living: " + TransLiving);
		
		currentWait = Wait;
		lTrans = TransLiving;
		dTrans = TransDeceased;
		dead = Removed;
		removed = Deceased;
		dieWait = DWait;
		writeYearlyToFile();
		System.out.println("Deceased Transplant: " + dTrans);
	}
	
	public void setupFiles() {
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
	}
	
}
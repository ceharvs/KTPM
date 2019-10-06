package KTPM;

import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;


public class Patient implements Steppable{
	
	//Characteristics of every patient
	public int id = 0;
	public int age = 0;
	public int ethnicity = 0;
	public boolean transplant = false;
	public boolean dead = false;
	public int waitYears = 0;
	public int transYears = 0;
	public int ageGroup = 0;
	public String transType = "";
	public String removalReason = "";
	public int removalYear = 0;
	public boolean removed = false;
	
	//Statistics Data
	public double ethnicGroups [] = {0.3753,0.7153,0.9059,0.9786};
	public double ethnicGroupsADD [] = {0.4578,0.7496,0.9179,0.9816};
	public int ageGroups [] = {18,35,50,65};
	public int checkTrans [] = {0,1,5,10};
	//Odds of being in an age group at the start of the sim
	public double ageGroupOdds [][] = {{0.0,0.0839,0.2973,0.7333}
	 									,{0.0,0.1002,0.4160,0.8438}
	 									,{0.0,0.1242,0.3921,0.8341}
	 									,{0.0,0.0867,0.3198,0.7653}
	 									,{0.0,0.0974,0.3886,0.8264}};
	//Odds of being in an age group in each yearly update
	public double ageGroupOddsADD [][] = {{0.0, 0.1069, 0.3456, 0.7879},
										{0.0, 0.1284, 0.4587, 0.8794},
										{0.0, 0.1582, 0.4545, 0.8835},
										{0.0, 0.1190, 0.3639, 0.8111},
										{0.0, 0.1226, 0.4408, 0.8644}};
	public double probLivngTrans [][] = {{0.2725,0.1864,0.1208,0.0849,0.0540},
										{0.0971,0.0417,0.0262,0.0183,0.0123},
										{0.1641,0.0907,0.0492,0.0280,0.0213},
										{0.4848,0.0872,0.0486,0.0233,0.0162},
										{0.1449,0.0488,0.0428,0.0236,0.0189}};
	public double probDeceasedTrans [][] = {{0.5024,0.1257,0.1282,0.1305,0.1281}
										,{0.501,0.1081,0.1032,0.1069,0.096}
										,{0.6598,0.0979,0.0864,0.083,0.0823}
										,{0.90,0.1028,0.1062,0.093,0.094}
										,{0.3333,0.0943,0.0833,0.0914,0.0840}};
	public double probDieWait [][] = {{0.0122,0.0329,0.0603,0.0935,0.1262}
										,{0.0152,0.0352,0.0435,0.0780,0.1223}
										,{0.0104,0.0217,0.0375,0.0711,0.1174}
										,{0.0303,0.0089,0.0269,0.0563,0.1098}
										,{0.0145,0.037,0.0377,0.0824,0.1303}};
	public double probOther [][] = {{0.0852,0.1198,0.1294,0.0897,0.0842}
										,{0.0571,0.0920,0.0817,0.0668,0.0726}
										,{0.0587,0.0998,0.0809,0.0616,0.0661}
										,{0.4242,0.0939,0.0825,0.0601,0.0663}
										,{0.0290,0.0808,0.0631,0.0562,0.0718}};
	public double survivalLiving [][] =	{{0.9810,0.9817,0.8536,0.7324},
										{0.9770,0.9765,0.7673,0.6189},
										{0.9780,0.9908,0.8751,0.7748},
										{0.9900,0.9869,0.9099,0.7750},
										{0.9930,0.9789,0.8354,0.7143}};
	public double survivalDeceased [][] ={{0.9570,0.9561,0.7749,0.6319},
										{0.9420,0.9480,0.6965,0.5177},
										{0.9590,0.9687,0.8073,0.6627},
										{0.9680,0.9711,0.8117,0.7104},
										{0.9710,0.9629,0.8053,0.5976}};

	/**
	 * Constructor for patients
	 * @param state - state of the simulation
	 * @param ID - Patient ID
	 * @param isInitial - boolean whether or not the patient 
	 * 	is being added in the initial stages of the progrss.
	 * 
	 * Randomly assign age and ethnicity to each patient. 
	 */
	public Patient(SimState state, int ID, boolean isInitial) {
		//This line is the one that should be changed when switching between GUI and REgular
		Patients patients = (Patients) state;	
		//Patients patients = (Patients) state;	
		this.id = ID;
		assignEthnicity(this, patients.random.nextDouble(), isInitial);
		assignAge(this, patients.random.nextInt(15), patients.random.nextDouble(), isInitial);
		this.transplant = false;
		this.dead = false;
		this.transYears = 0;
		this.waitYears = 0;
		this.transType = "";
		this.removalYear = 0;
		this.removed = false;
	}
		
	/**
	 * Sim State
	 */
	public void step(SimState state){
		Schedule schedule = state.schedule;
	}
	
	//Update the age group of the agent
	public void updateAgeGroup(Patient patient){
		
		for( int i = 0; i < 4; i++){
			if(patient.age >= ageGroups[i])
				patient.ageGroup = i+1;
		}
	}
	
	//Assign age to the patient depending on their ethnicity.
	public void assignAge(Patient patient, int tempAddAge, double temp, boolean isInitial){
		//Row = ethnicity number
		double aGO [][] = ageGroupOddsADD;
		if(isInitial) aGO = ageGroupOdds;
		//System.out.println(aGO[1][1]);
		for( int i = 0; i < 4; i++)
		{
			if (temp > ageGroupOdds[patient.ethnicity][i])
				patient.age = ageGroups[i] + tempAddAge;
		}
	}

	//Assign an ethnicity to each patient, based on the input odds
	public void assignEthnicity(Patient patient, double temp, boolean isInitial){
		double eG [] = ethnicGroupsADD;
		if(isInitial) eG = ethnicGroups;
		for(int i = 0; i < eG.length; i++){
			if ( temp > eG[i])
				patient.ethnicity = i+1;
		}
	}
	
	//Determine whether or not an agent should be removed
	//Dead and otherwise removed agents should be removed from the simulation
	public boolean shouldRemove(Patient patient){
		if (patient.dead ==true || patient.removed == true) return true;
		else return false;
	}
	
	/**
	 * Method to assign transplants to patients.
	 * @param patient
	 * @param temp - randomly generated number, determines which 
	 *  range the patient will fall into when it comes with go/no-go on treatment
	 * @param LF - Living Factor
	 * @param DF - Deceased Factor
	 * @param DM - Deceased Multiplier
	 * @param EQ - Whether all odds are equal
	 */
	public void assignTransplant(Patient patient, double temp, double LF, double DF, double DM, boolean EQ){
		if (patient.transplant == false && patient.dead == false && patient.removed == false){
			/**
			 * Computes the probabilities of each possible outcome happening to th agent.  
			 * This probability is based on the agent's own ethnicity
			 * and the LF and DF used in the simulation.
			 */
			
			double getLiving = (1.0 + LF) * probLivngTrans[patient.ethnicity][patient.ageGroup];
			double getDeceased = (DM * (1.0 + DF)) * probDeceasedTrans[patient.ethnicity][patient.ageGroup];
			double getDieWait = probDieWait[patient.ethnicity][patient.ageGroup];
			double getOther = probOther[patient.ethnicity][patient.ageGroup];
			//Account for Equalizer
			if(EQ == true) getLiving = probLivngTrans[0][patient.ageGroup];
			
			//Determine which category the patient falls into
			if(temp < getLiving){
				patient.transType = "Living";
				getTransplant(patient);
				//System.out.println("Patient "+ patient.id + " got a living trans");
			} else if(temp < (getLiving + getDeceased)){
				patient.transType = "Deceased";
				getTransplant(patient);
				//System.out.println("Patient "+ patient.id + " got a deceased trans");
			} else if(temp < (getLiving + getDeceased + getDieWait)){
				patient.removalReason = "Died Waiting/Too Sick";
				becomeDead(patient);
				//System.out.println("Patient "+ patient.id + " died Waiting");
			}else if(temp < (getLiving + getDeceased + getDieWait + getOther)){
				remove(patient);
				//System.out.println("Patient "+ patient.id + " was Removed");
			}
		}
	}
	
	//Age patients by one year
	public void getOlder(Patient patient, double temp){
		if (patient.dead == false && patient.removed == false){
			patient.age++;
			
			//Transplanted Patients
			if(patient.transplant == true){
				//if Patient is in a checking Year
				if(isTransYears(patient.transYears)){
					//System.out.println("checking agent "+patient.id+" at year " + patient.transYears);
					double odds = 0.0;
					int checkYear = getCheckYear(patient.transYears);
					if(patient.transType.equals("Living")){
						odds = survivalLiving[patient.ethnicity][checkYear];
					}else{
						odds = survivalDeceased[patient.ethnicity][checkYear];
					}
					if(temp > odds){
						patient.removalReason = "Died Post-Transplant";
						becomeDead(patient);
					} 
				}
				if(patient.dead == false) patient.transYears++;
				if(patient.transYears == 11 && patient.dead == false){
					patient.removalReason = "Over 10 Year Survival";
					patient.removed = true;
					//record
				}
			}else{
				patient.waitYears++;
			}
		}
	}
	
	//Check the number of years since Transplant 
	//(Used for post-transplant survival)
	private boolean isTransYears(int tY) {
		boolean checkit = false;
		for(int i = 0; i < checkTrans.length; i++){
			if(tY == checkTrans[i]) checkit = true;
		}
		return checkit;
	}
	
	//Check the number of years since Transplant 
	//(Used for post-transplant survival)
	private int getCheckYear(int tY) {
		int checkYear = tY;
		if(tY == 5) checkYear = 2;
		if(tY == 10) checkYear = 3;
		
		return checkYear;
	}
	
	//Assigne a patient to get a transplant	
	public void getTransplant(Patient patient){
		patient.transplant = true;
	}

	//Assign a patient as dead
	public void becomeDead(Patient patient){
		patient.dead = true;
		//record
	}
	
	//Remove a patient fromt he waiting list for some other reason	
	public void remove(Patient patient){
		patient.removalReason = "Other";
		patient.removed = true;
	}
	
	//Return if patient should remain on the Waiting List
	public boolean Waiting(Patient patient){
		if (patient.dead == false && patient.removed == false && patient.transplant == false) return true;
		else return false;
	}
	
	//Determine if the patient is dead
	public boolean Deceased(Patient patient){
		if (patient.dead == true){
			//System.out.println(patient.id + " should be removed (Dead)");
			return true;
		}
		else return false;
	}
	
	//Determine if the patient dies while waiting
	public boolean DieWait(Patient patient){
		if (patient.dead == true && patient.transplant == false){
			//System.out.println(patient.id + " should be removed (Dead)");
			return true;
		}
		else return false;
	}
	
	//Determine if the patient has been removed
	public boolean Removed(Patient patient){
		if (patient.removed == true){
			//System.out.println("Patient "+ patient.id + " should be removed");
			return true;
		}
		else return false;
	}
	
	//Determine if patient recieved a Living Transplant
	public boolean LivingTrans(Patient patient){
		if (patient.transplant == true && patient.transType.equals("Living") && patient.transYears == 1) return true;
		else return false;
	}
	
	//Determine if patient recieved a Living Transplant	
	public boolean DeceasedTrans(Patient patient){
		if (patient.transplant == true && patient.transType.equals("Deceased") && patient.transYears == 1) {
			//System.out.println(patient.id);
			return true;
		}
		
		else return false;
		
	}

	public String record(Patient patient){
		patient.removalYear = patient.transYears + patient.waitYears;
		String removal = "" + patient.removalYear;
		if(patient.dead == false && patient.removed == false) removal = null;
		String Output = patient.id + "," + patient.age + "," + ethnicWord(patient) + "," + patient.transplant 
				+ "," + patient.transType + "," + patient.transYears + "," + patient.waitYears
				+ "," + patient.dead + "," + patient.removalReason + "," + removal;
		return Output;
	}
	
	public String ethnicWord(Patient patient){
		String ethnicity = "White";
		if(patient.ethnicity == 1) ethnicity = "Black";
		else if(patient.ethnicity == 2) ethnicity = "Hispanic";
		else if(patient.ethnicity == 3) ethnicity = "Asian";
		else if(patient.ethnicity == 4) ethnicity = "Multi-Racial/Other";
		return ethnicity;
	}
}


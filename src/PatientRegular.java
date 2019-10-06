package KTPM;

import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;


public class PatientRegular implements Steppable {
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
		public double ethnicGroups [] = {0.3753,0.7153,0.9059,0.9786}; //Initial Distribution
		public double ethnicGroupsADD [] = {0.4578,0.7496,0.9179,0.9816}; //Additions to the list
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
		
		/**
		 * Change Removal Reason to be Equal across all ethnicities
		 */
		public double probLivngTrans [][] = {{0.1953,0.1033,0.0615,0.0457,0.0344},
											{0.1953,0.1033,0.0615,0.0457,0.0344},
											{0.1953,0.1033,0.0615,0.0457,0.0344},
											{0.1953,0.1033,0.0615,0.0457,0.0344},
											{0.1953,0.1033,0.0615,0.0457,0.0344}};
		public double probDeceasedTrans [][] = {{0.5523,0.1107,0.1073,0.1097,0.1091}
											,{0.5523,0.1107,0.1073,0.1097,0.1091}
											,{0.5523,0.1107,0.1073,0.1097,0.1091}
											,{0.5523,0.1107,0.1073,0.1097,0.1091}
											,{0.5523,0.1107,0.1073,0.1097,0.1091}};
		public double probDieWait [][] = {{0.0128,0.0295,0.0462,0.0810,0.1226}
											,{0.0128,0.0295,0.0462,0.0810,0.1226}
											,{0.0128,0.0295,0.0462,0.0810,0.1226}
											,{0.0128,0.0295,0.0462,0.0810,0.1226}
											,{0.0128,0.0295,0.0462,0.0810,0.1226}};
		public double probOther [][] = {{0.0740,0.1028,0.0958,0.0737,0.0767}
											,{0.0740,0.1028,0.0958,0.0737,0.0767}
											,{0.0740,0.1028,0.0958,0.0737,0.0767}
											,{0.0740,0.1028,0.0958,0.0737,0.0767}
											,{0.0740,0.1028,0.0958,0.0737,0.0767}};
		public double survivalLiving [][] =	{{0.9810,0.9827,0.8444,0.7236},
											{0.9810,0.9827,0.8444,0.7236},
											{0.9810,0.9827,0.8444,0.7236},
											{0.9810,0.9827,0.8444,0.7236},
											{0.9810,0.9827,0.8444,0.7236}};
		public double survivalDeceased [][] ={{0.9540,0.9560,0.7577,0.6049},
											{0.9540,0.9560,0.7577,0.6049},
											{0.9540,0.9560,0.7577,0.6049},
											{0.9540,0.9560,0.7577,0.6049},
											{0.9540,0.9560,0.7577,0.6049}};

		/**
		 * Constructor for patients
		 * @param state - state of the simulation
		 * @param ID - Patient ID
		 * @param isInitial - boolean whether or not the patient 
		 * 	is being added in the initial stages of the progrss.
		 * 
		 * Randomly assign age and ethnicity to each patient. 
		 */
		public PatientRegular(SimState state, int ID, boolean isInitial) {
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
		public void updateAgeGroup(PatientRegular patient){
			
			for( int i = 0; i < 4; i++){
				if(patient.age >= ageGroups[i])
					patient.ageGroup = i+1;
			}
		}
		
		//Assign age to the patient depending on their ethnicity.
		public void assignAge(PatientRegular patient, int tempAddAge, double temp, boolean isInitial){
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
		public void assignEthnicity(PatientRegular patient, double temp, boolean isInitial){
			double eG [] = ethnicGroupsADD;
			if(isInitial) eG = ethnicGroups;
			for(int i = 0; i < eG.length; i++){
				if ( temp > eG[i])
					patient.ethnicity = i+1;
			}
		}
		
		//Determine whether or not an agent should be removed
		//Dead and otherwise removed agents should be removed from the simulation
		public boolean shouldRemove(PatientRegular patient){
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
		public void assignTransplant(PatientRegular patient, double temp, double LF, double DF, double DM, boolean EQ){
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
		public void getOlder(PatientRegular patient, double temp){
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
		public void getTransplant(PatientRegular patient){
			patient.transplant = true;
		}

		//Assign a patient as dead
		public void becomeDead(PatientRegular patient){
			patient.dead = true;
			//record
		}
		
		//Remove a patient fromt he waiting list for some other reason	
		public void remove(PatientRegular patient){
			patient.removalReason = "Other";
			patient.removed = true;
		}
		
		//Return if patient should remain on the Waiting List
		public boolean Waiting(PatientRegular patient){
			if (patient.dead == false && patient.removed == false && patient.transplant == false) return true;
			else return false;
		}
		
		//Determine if the patient is dead
		public boolean Deceased(PatientRegular patient){
			if (patient.dead == true){
				//System.out.println(patient.id + " should be removed (Dead)");
				return true;
			}
			else return false;
		}
		
		//Determine if the patient dies while waiting
		public boolean DieWait(PatientRegular patient){
			if (patient.dead == true && patient.transplant == false){
				//System.out.println(patient.id + " should be removed (Dead)");
				return true;
			}
			else return false;
		}
		
		//Determine if the patient has been removed
		public boolean Removed(PatientRegular patient){
			if (patient.removed == true){
				//System.out.println("Patient "+ patient.id + " should be removed");
				return true;
			}
			else return false;
		}
		
		//Determine if patient recieved a Living Transplant
		public boolean LivingTrans(PatientRegular patient){
			if (patient.transplant == true && patient.transType.equals("Living") && patient.transYears == 1) return true;
			else return false;
		}
		
		//Determine if patient recieved a Living Transplant	
		public boolean DeceasedTrans(PatientRegular patient){
			if (patient.transplant == true && patient.transType.equals("Deceased") && patient.transYears == 1) {
				//System.out.println(patient.id);
				return true;
			}
			
			else return false;
			
		}

		public String record(PatientRegular patient){
			patient.removalYear = patient.transYears + patient.waitYears;
			String removal = "" + patient.removalYear;
			if(patient.dead == false && patient.removed == false) removal = null;
			String Output = patient.id + "," + patient.age + "," + ethnicWord(patient) + "," + patient.transplant 
					+ "," + patient.transType + "," + patient.transYears + "," + patient.waitYears
					+ "," + patient.dead + "," + patient.removalReason + "," + removal;
			return Output;
		}
		
		public String ethnicWord(PatientRegular patient){
			String ethnicity = "White";
			if(patient.ethnicity == 1) ethnicity = "Black";
			else if(patient.ethnicity == 2) ethnicity = "Hispanic";
			else if(patient.ethnicity == 3) ethnicity = "Asian";
			else if(patient.ethnicity == 4) ethnicity = "Multi-Racial/Other";
			return ethnicity;
		}
}

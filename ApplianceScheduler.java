import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
public class ApplianceScheduler {

	public static void main(String[] args) throws IOException {
		
		//INPUTS=======================================
		double dishwasherNotPresentProb = .5120; //probability of there not being a dishwasher in the home
		double sit1prob = .327; //probability for zero runs 
		double sit2prob = .188 + sit1prob; //probability for one run
		double sit3prob = .278 + sit2prob; //probability for 2 to 3 runs
		double sit4prob = .131 + sit3prob; //probability for 4 to 6 runs
		int timeStepInMinutes = 60; //time step length in minutes !!!MAKE EASILY DIVISIBLE INTO 60!!!
		int duration = 2; //number of timesteps the dishwasher is run 
		boolean schedulingWeek = true; //will we be scheduling entire weeks?
		// ===============================================

		//DECLARING VARIABLES ===============================
		double determinePresenceOfDishwasher;
		double weekClassification;
		double determineLoadsThisWeek;
		double determineTimeStep;
		int loadsThisWeek = 0;
		boolean dishwasherPresent;
		int occupied = 0;
		double probNew = 0;
		double probOld;
		boolean trip = false;
		int timeStep = 60/timeStepInMinutes; //time step length in minutes !!!NEEDS TO BE WHOLE NUMBER!!!
		ArrayList<Integer> activeTimeSteps = new ArrayList<Integer>();
		ArrayList<Integer> turnOnTimeSteps = new ArrayList<Integer>();
		ArrayList<Integer> occupancyData = new ArrayList<Integer>();
		ArrayList<Integer> dailySchedule = new ArrayList<Integer>();
		int dayCount=0;
		int Ncount=3;
		FileWriter dailyWriter = new FileWriter("C:\\Users\\Hannah\\Desktop\\DailySchedules.csv");
		//=================================================
		
		//CHECK VALIDITY OF TIME STEP======================
		if (60%timeStepInMinutes != 0){
			System.out.println("=========================================================");
			System.out.println("ERROR: Occupancy data does not represent a whole number of days.");
			System.out.println("Results will not be valid.");
			System.out.println("=========================================================");
		}
		//=================================================

		//WRITING INITIAL FILES============================
		dailyWriter.append("============================\n");
		dailyWriter.append("Paste the Below Schedules Into the\n 'SCHEDULE:DAY:LIST' \nSection in IDF File\n");
		dailyWriter.append("============================\n\n");

		//GET OCCUPANCY DATA ==============================
			//reading file
			File data = new File("C:\\Users\\Hannah\\Desktop\\Occupancy.csv");
			Scanner scanner = new Scanner(data);
			scanner.useDelimiter(",");
			while (scanner.hasNext()) {
				occupancyData.add(scanner.nextInt());
			}
			scanner.close();
			System.out.println("OCCUPANCY:");
			System.out.println(occupancyData);
			System.out.println("size: " + occupancyData.size());
			//making sure it is a proper length
			if (occupancyData.size()%(24*timeStep) != 0){
				System.out.println("=========================================================");
				System.out.println("ERROR: Occupancy data does not represent a whole number of days.");
				System.out.println("=========================================================");
			}
			//how many time steps are occupied? (add awake later)
			for (int i = 0; i<occupancyData.size();i++) {
				if (occupancyData.get(i)==1) {
					occupied++;	
				}
			}
		//======================================================

		//DISHWASHER PRESENCE IN HOUSEHOLD=================
		determinePresenceOfDishwasher = Math.random();
		if (determinePresenceOfDishwasher <= dishwasherNotPresentProb) {
			dishwasherPresent = false;
			//set to always off
		}else {
			dishwasherPresent = true;
		}
		System.out.println("dishwasher presence");
		System.out.println(dishwasherPresent);
		//=========================================================

		//NUMBER OF DISHWASHER LOADS THIS WEEK======================
		if (dishwasherPresent == true) {
			weekClassification = Math.random();
			if (weekClassification<sit1prob) {
				loadsThisWeek = 0;
			}else if(weekClassification >= sit1prob && weekClassification < sit2prob) {
				loadsThisWeek = 1;
			}else if(weekClassification >= sit2prob && weekClassification < sit3prob) {
				determineLoadsThisWeek = Math.random();
				if (determineLoadsThisWeek < .5) {
					loadsThisWeek = 2;
				}else {
					loadsThisWeek = 3;
				}
			}else if(weekClassification >= sit3prob && weekClassification < sit4prob) {
				determineLoadsThisWeek = Math.random();
				if (determineLoadsThisWeek < 0.333) {
					loadsThisWeek = 4;
				}else if(determineLoadsThisWeek >= .333 && determineLoadsThisWeek < .666) {
					loadsThisWeek = 5;
				}else {
					loadsThisWeek =  6;
				}
			}else if(weekClassification >= sit4prob) {
				loadsThisWeek = 7;
			}
		}
		System.out.println("loads");
		System.out.println(loadsThisWeek);
		//===================================================
		
		//DISTRIBUTING PROBABILITY FOR EACH TIMESTEP==================
		double timeStepProb = 1d/occupied; //for now, everything has same probability CHANGE TO POSSIBLE COUNT LATER ON
		//============================================================
	
		//SCHEDULING THE APPLIANCE====================================
		if (dishwasherPresent == true) {
			//getting which occupied timesteps are activated
			int j = 1;
			int safeGuard = 0;
			while(j<=loadsThisWeek) { //go until number of loads reached 
				safeGuard++;
				if (safeGuard == 100000000) { //prevents infinite loop in case loading not possible.
					System.out.println("=========================================================");
					System.out.println("ERROR: SafeGuard Reached. Schedule output will not be correct. Ensure that loading is possible.");
					System.out.println("Safeguard may be erroneously reached due to high frequency of appliance operation");
					System.out.println("in relation to # of occupied timesteps. Can increase safeguard if neccessary. Otherwise,");
					System.out.println("please consider decreasing # of loads/increasing operational time steps");
					System.out.println("=========================================================");
					break;
				}
				determineTimeStep = Math.random(); //roll random number for this load
				for (int i = 0; i < occupied; i++) {
					probOld = probNew;
					probNew = probOld + timeStepProb;
					if (determineTimeStep > probOld && determineTimeStep <= probNew) { // when the load random number lands within time step probability
						for (int k=i;k<=i+duration-1;k++) {
							if(activeTimeSteps.contains(k)==true || k>=occupancyData.size() ) { //ensuring loads don't overlap or get cutoff by schedule length
								trip = true; //re roll to avoid overlap or cutoff
							}
						}
						if (trip==false) {
							for (int m = i; m<=i+duration-1;m++) { //if we can schedule here, schedule the entire duration
								activeTimeSteps.add(m);
								turnOnTimeSteps.add(i);
							}
							j++;
						}
						trip = false; //reset trip boolean for next load
					}
				}
			probNew = 0; //reset probability counter for next load
			}	
			System.out.println("Active Time Steps:");
			System.out.println(activeTimeSteps);
			//finalizing schedule
			int[] applianceSchedule = new int[occupancyData.size()];
			int occupiedCount=0; //CHANGE TO POSSIBLE COUNT LATER ON
			for(int i = 0; i < occupancyData.size();i++) {
				if (applianceSchedule[i] == 1) {
					if(occupancyData.get(i) == 1) {
						occupiedCount++;
					}
				}else if(occupancyData.get(i) == 1) {
					if(turnOnTimeSteps.contains(occupiedCount)) {
						for (int p = i; p<=i+duration-1;p++)
						applianceSchedule[p] = 1;
					}else {
						applianceSchedule[i] = 0;
					}
					occupiedCount++;
				}else if(occupancyData.get(i) == 0) {
					applianceSchedule[i] = 0;
				}
			}
			System.out.println(Arrays.toString(applianceSchedule));
			//================================================

			//PARSE SCHEDULE INTO DAYS AND WRITE TO CSV ==============================
			for (int r = 0; r < applianceSchedule.length;r++){
				dailySchedule.add(applianceSchedule[r]);
				if (dailySchedule.size()==24*timeStep){
					dayCount++;
					dailyWriter.append("Schedule:Day:List,\n");
					dailyWriter.append("\tDay_"+dayCount+",			!- Name\n");
					dailyWriter.append("\tany number,		!- Schedule Type Limits Name\n");
					dailyWriter.append("\tNo,			!- Interpolate to Timestep\n");
					dailyWriter.append("\t"+timeStepInMinutes+",			!- Minutes per Item\n");
					dailyWriter.append("\t"+Integer.toString(dailySchedule.get(0))+",			!- Value 1\n");
					for (int w = 1; w < dailySchedule.size(); w++) {
						dailyWriter.append("\t"+Integer.toString(dailySchedule.get(w))+",			!- N"+Ncount+"\n");
						Ncount++;
					}
					dailyWriter.append("\n");
					dailySchedule.clear();
					Ncount = 3;
				}
			}
			//==================================================

			//WRITE TO WEEKLY SCHEDULE CSV============================== move to after everything
			schedulingWeek = true;
			dayCount = 1;
			int weekCount = 1; //CHANGE LATER
			if (schedulingWeek = true){
				FileWriter weeklyWriter = new FileWriter("C:\\Users\\Hannah\\Desktop\\WeeklySchedules.csv");
				weeklyWriter.append("============================\n");
				weeklyWriter.append("Paste the Below Schedules Into the\n 'SCHEDULE:WEEK:DAILY' \nSection in IDF File\n");
				weeklyWriter.append("============================\n\n");
				weeklyWriter.append("Schedule:Week:Daily,\n");
				weeklyWriter.append("\tWeek_"+weekCount+",			!- Name\n");
				while (dayCount <= 7){
					weeklyWriter.append("\tDay_"+dayCount+",			!- Day "+dayCount+" Schedule:Day Name \n");
					dayCount++;
				}
				System.out.println("WEEKLY WRITTEN");
				weeklyWriter.close();
			}
			//====================================================

		}
		dailyWriter.close();

		//------------------------------------------------------------------
		
	}

}

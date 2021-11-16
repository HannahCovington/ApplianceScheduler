import java.util.ArrayList;
import java.util.Arrays;
import java.io.FileWriter;
import java.io.IOException;
public class ApplianceScheduler {

	public static void main(String[] args) throws IOException {
		
		//DECLARING VARIABLES--------------------
		//dishwasher use
		double dishwasherNotPresentProb = .5120;
		double sit1prob = .327; //zero runs 
		double sit2prob = .188 + sit1prob; //one run
		double sit3prob = .278 + sit2prob; //2 to 3 runs
		double sit4prob = .131 + sit3prob; //4 to 6 runs
		double sit5prob = .082 + sit4prob; //7+ runs
		int duration = 2; //number of timesteps the dishwasher is run 
		//random numbers that determine use
		double determinePresenceOfDishwasher;
		double weekClassification;
		double determineLoadsThisWeek;
		double determineTimeStep;
		//variables for holding information
		int loadsThisWeek = 0;
		int numOperationalHours;
		int operationSchedule[];
		boolean dishwasherPresent;
		int occupied=0;
		double probNew = 0;
		double probOld;
		boolean trip = false;
		ArrayList<Integer> activeTimeSteps = new ArrayList<Integer>();
		ArrayList<Integer> turnOnTimeSteps = new ArrayList<Integer>();
		//----------------------------------------------------
		
		//DISHWASHER PRESENCE IN HOUSEHOLD ------------
		determinePresenceOfDishwasher = Math.random();
		if (determinePresenceOfDishwasher <= dishwasherNotPresentProb) {
			dishwasherPresent = false;
			//set to always off
		}else {
			dishwasherPresent = true;
		}
		System.out.println("dishwasher presence");
		System.out.println(dishwasherPresent);
		//------------------------------------
		
		//NUMBER OF DISHWASHER LOADS THIS WEEK---------------
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
		//----------------------------------------------------------
		
		//GETTING OCCUPANCY DATA (hard coded for now)---------------------
		
		//hard coded occupancy data, will later be replaced with read file
		int[] occupancyData = new int[] {0,0,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0};
		System.out.println(Arrays.toString(occupancyData));
		//how many time steps are occupied? (add awake later)
		for (int i = 0; i<occupancyData.length;i++) {
			if (occupancyData[i]==1) {
				occupied++;	
			}
		}
		//------------------------------------------------------------
		
		//DISTRIBUTING PROBABILITY FOR EACH TIMESTEP-------------------
		double timeStepProb = 1d/occupied; //for now, everything has same probability
		//------------------------------------------------------------

		//SCHEDULING THE APPLIANCE--------------------------------------
		if (dishwasherPresent == true) {
			//getting which occupied timesteps are activated
			int j = 1;
			int safeGuard = 0;
			while(j<=loadsThisWeek) { 
				safeGuard++;
				if (safeGuard == 10000) { //prevents infinite loop in case that loading not possible. DOES NOT WORK
					System.out.println("SafeGuard Reached. Ensure that loading is possible");
					break;
				}
				determineTimeStep = Math.random();
				for (int i = 0; i < occupied; i++) {
					probOld = probNew;
					probNew = probOld + timeStepProb;
					if (determineTimeStep > probOld && determineTimeStep <= probNew) {
						for (int k=i;k<=i+duration-1;k++) {
							if(activeTimeSteps.contains(k)==true) {
								trip = true;
							}
						}
						if (trip==false) {
							for (int m = i; m<=i+duration-1;m++) {
								activeTimeSteps.add(m);
								turnOnTimeSteps.add(i);
							}
							j++;
						}
						trip = false;
					}
				}
			probNew = 0;
			}	
			System.out.println(activeTimeSteps);
			//finalizing schedule
			int[] applianceSchedule = new int[occupancyData.length];
			int occupiedCount=0;
			for(int i = 0; i< occupancyData.length;i++) {
				System.out.println(occupiedCount);
				if (applianceSchedule[i] == 1) {
					if(occupancyData[i] == 1) {
						occupiedCount++;
					}
				}else if(occupancyData[i] == 1) {
					if(turnOnTimeSteps.contains(occupiedCount)) {
						for (int p = i; p<=i+duration-1;p++)
						applianceSchedule[p] = 1;
					}else {
						applianceSchedule[i] = 0;
					}
					occupiedCount++;
				}else if(occupancyData[i] == 0) {
					applianceSchedule[i] = 0;
				}
			}
			System.out.println(Arrays.toString(applianceSchedule));
			
			//Write to .csv file
			FileWriter writer = new FileWriter("C:\\Users\\Hannah\\Desktop\\results.csv");
			for (int w = 0; w < applianceSchedule.length; w++) {
				writer.append(String.valueOf(applianceSchedule[w])+",			!- Timestep " + w);
				writer.append("\n");
			}
			writer.close();
		}
		
		//------------------------------------------------------------------
		
	}

}

package com.swinestudios.unusualweapon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.mini2Dx.core.geom.Rectangle;

public class CaveSystem{

	/*public int[][] terrain = { {1, 0, 0, 1, 0}, //temporary
			{1, 1, 0, 0, 0},
			{1, 1, 0, 0, 0},
			{1, 0, 0, 0, 1},
			{1, 0, 0, 1, 1}};*/

	public int[][] terrain;

	public float x, y; //the top-left corner of the entire cave system
	public final int tileSize = 16;

	public Gameplay level;
	public Random random;

	public ArrayList<Miner> miners;
	public Miner miner1;

	public final int maxSteps = 500; //How many iterations each miner will carry out
	public final int trimSteps = 3; //How many iterations of removing idle blocks
	public final int spawnChance = 10; //The percent probability of another miner spawning
	public final int minimumNeighborsNeeded = 4; //The minimum # of neighbors needed for a solid to be removed

	public CaveSystem(float x, float y, Gameplay level){
		this.x = x;
		this.y = y;
		this.level = level;
		terrain = new int[200][100]; //TODO temporary size
		miners = new ArrayList<Miner>();
		miner1 = new Miner(terrain.length / 2, terrain[0].length / 2);
		miners.add(miner1);
		random = new Random();
	}

	/*
	 * TODO Generates the terrain and saves it in the 2D array.
	 * 
	 * 1 = wall
	 * 0 = empty
	 */
	public void generateTerrain(){
		for(int[] row : terrain){ //make everything solid
			Arrays.fill(row, 1);
		}

		for(int n = 0; n < maxSteps; n++){
			for(int i = 0; i < miners.size(); i++){
				if(miners.get(i).isActive){
					miners.get(i).moveMiner();
				}
			}
			int spawner = random.nextInt(100) + 1;
			if(spawner <= spawnChance){
				miners.add(new Miner(random.nextInt(terrain.length), random.nextInt(terrain[0].length)));
			}
		}
		miners.clear();

		for(int i = 0; i < trimSteps; i++){
			trimTerrain();
		}

		//TODO temporary code to box in the terrain
		for(int i = 0; i < terrain.length; i++){
			for(int j = 0; j < terrain[i].length; j++){
				if(i == 0 || i == terrain.length - 1 || j == 0 || j == terrain[i].length -1){
					terrain[i][j] = 1;
				}
			}
		}
	}

	/*
	 * Adds the generated terrain to the level.
	 */
	public void addTerrain(){
		for(int i = 0; i < terrain.length; i++){
			for(int j = 0; j < terrain[i].length; j++){
				if(terrain[i][j] == 1){ //if a solid
					level.solids.add(new Rectangle(x + j * tileSize, y + i * tileSize, tileSize, tileSize));
				}
			}
		}
	}
	
	/*
	 * Adds the generated terrain to the level with some optimization.
	 */
	public void addOptimizedTerrain(){
		for(int i = 0; i < terrain.length; i++){
			int currentRun = 0;
			int startRunX = 0;
			int startRunY = 0;
			for(int j = 0; j < terrain[i].length; j++){
				if(terrain[i][j] == 1){ //if a solid
					if(currentRun == 0){ //if starting a new run
						startRunX = j;
						startRunY = i;
					}
					currentRun++;
				}
				else{ //the current run of solids is done
					level.solids.add(new Rectangle(x + startRunX * tileSize, y + startRunY * tileSize, currentRun * tileSize, tileSize));
					currentRun = 0;
				}
			}
		}
	}

	/*
	 * Removes idle blocks of terrain
	 */
	public void trimTerrain(){
		for(int y = 0; y < terrain.length; y++){
			for(int x = 0; x < terrain[y].length; x++){
				int counter = 0;
				int leftX = Math.abs((x - 1) % terrain[0].length);
				int rightX = Math.abs((x + 1) % terrain[0].length);
				int upY = Math.abs((y - 1) % terrain.length);
				int downY = Math.abs((y + 1) % terrain.length);

				//count how many neighbors there are
				counter += Math.signum(terrain[y][leftX]);
				counter += Math.signum(terrain[y][rightX]);
				counter += Math.signum(terrain[upY][x]);
				counter += Math.signum(terrain[downY][x]);
				counter += Math.signum(terrain[upY][leftX]);
				counter += Math.signum(terrain[upY][rightX]);
				counter += Math.signum(terrain[downY][leftX]);
				counter += Math.signum(terrain[downY][rightX]);

				if(counter < minimumNeighborsNeeded){
					terrain[y][x] = 0;
				}
			}
		}
	}

	class Miner{

		public int x, y;

		public int choice;

		public Random random;

		public boolean isActive;

		public Miner(int x, int y){
			this.x = x;
			this.y = y;
			isActive = true;
			random = new Random();
			choice = random.nextInt(4) + 1;
		}

		public void moveMiner(){
			if(choice == 1){ //north
				y--;
			}
			else if(choice == 2){ //east
				x++;
			}
			else if(choice == 3){ //south
				y++;
			}
			else if(choice == 4){ //west
				x--;
			}

			//make sure the miner's position is within the terrain
			x = Math.abs(x % terrain[0].length);
			y = Math.abs(y % terrain.length);

			choice = random.nextInt(4) + 1;

			//"Mine" the solid
			if(terrain[y][x] == 1){
				terrain[y][x] = 0;
			}

			/* TODO not sure if needed or not
			int leftX = Math.abs((x - 1) % terrain[0].length);
			int rightX = Math.abs((x + 1) % terrain[0].length);
			int upY = Math.abs((y - 1) % terrain.length);
			int downY = Math.abs((y + 1) % terrain.length);

			//Stop this miner if it is not bordering a wall 
			if(	   terrain[y][leftX] != 1 
				&& terrain[y][rightX] != 1 
				&& terrain[downY][x] != 1 
				&& terrain[upY][x] != 1 
				&& terrain[upY][leftX] != 1 
				&& terrain[upY][rightX] != 1
				&& terrain[downY][leftX] != 1 
				&& terrain[downY][rightX] != 1){
				isActive = false;
				miners.remove(this);
			}*/
		}

	}

}
